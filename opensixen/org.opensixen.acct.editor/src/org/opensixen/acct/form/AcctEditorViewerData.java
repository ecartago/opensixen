package org.opensixen.acct.form;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.JComboBox;

//import org.compiere.acct.AcctViewerData;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MAcctSchemaElement;
import org.compiere.model.MFactAcct;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MOrg;
import org.compiere.model.MRefList;
import org.compiere.report.core.RColumn;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Language;
import org.compiere.util.Msg;
import org.compiere.util.ValueNamePair;
import org.opensixen.acct.grid.RModel;

public class AcctEditorViewerData
{
	/**
	 *  Constructor
	 *	@param ctx context
	 *  @param windowNo window no
	 *  @param ad_Client_ID client
	 * 	@param ad_Table_ID table
	 */
	public AcctEditorViewerData (Properties ctx, int windowNo, int ad_Client_ID, int ad_Table_ID)
	{
		WindowNo = windowNo;
		AD_Client_ID = ad_Client_ID;
		if (AD_Client_ID == 0)
			AD_Client_ID = Env.getContextAsInt(Env.getCtx(), WindowNo, "AD_Client_ID");
		if (AD_Client_ID == 0)
			AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "AD_Client_ID");
		AD_Table_ID = ad_Table_ID;
		//
		ASchemas = MAcctSchema.getClientAcctSchema(ctx, AD_Client_ID);
		ASchema = ASchemas[0];
	}   //  AcctViewerData

	/** Window              */
	public int              WindowNo;
	/** Client				*/
	public int              AD_Client_ID;
	/** All Acct Schema		*/
	public MAcctSchema[]    ASchemas = null;
	/** This Acct Schema	*/
	public MAcctSchema      ASchema = null;

	//  Selection Info
	/** Document Query		*/
	public boolean          documentQuery = false;
	/** Acct Schema			*/
	public int              C_AcctSchema_ID = 0;
	/** Posting Type		*/
	public String			PostingType = "";
	/** Organization		*/
	public int              AD_Org_ID = 0;
	/** Date From			*/
	public Timestamp        DateFrom = null;
	/** Date To				*/
	public Timestamp        DateTo = null;

	//  Document Table Selection Info
	/** Table ID			*/
	public int              AD_Table_ID;
	/** Record				*/
	public int              Record_ID;

	/** Containing Column and Query     */
	public HashMap<String,String>	whereInfo = new HashMap<String,String>();
	/** Containing TableName and AD_Table_ID    */
	public HashMap<String,Integer>	tableInfo = new HashMap<String,Integer>();

	//  Display Info
	/** Display Qty			*/
	public boolean          displayQty = false;
	/** Display Source Currency	*/
	public boolean          displaySourceAmt = false;
	/** Display Document info	*/
	public boolean          displayDocumentInfo = false;
	/** Display Balance Mode*/
	public boolean			displayBalanceMode = false;
	//
	public String           sortBy1 = "";
	public String           sortBy2 = "";
	public String           sortBy3 = "";
	public String           sortBy4 = "";
	//
	public boolean          group1 = false;
	public boolean          group2 = false;
	public boolean          group3 = false;
	public boolean          group4 = false;

	/** Leasing Columns		*/
	private int				m_leadingColumns = 0;
	/** UserElement1 Reference	*/
	private String 			m_ref1 = null;
	/** UserElement2 Reference	*/
	private String 			m_ref2 = null;
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(AcctEditorViewerData.class);

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		ASchemas = null;
		ASchema = null;
		//
		whereInfo.clear();
		whereInfo = null;
		//
		Env.clearWinContext(WindowNo);
	}   //  dispose

	
	/**************************************************************************
	 *  Fill Accounting Schema
	 *  @param cb JComboBox to be filled
	 */
	public void fillAcctSchema (JComboBox cb)
	{
		for (int i = 0; i < ASchemas.length; i++)
			cb.addItem(new KeyNamePair(ASchemas[i].getC_AcctSchema_ID(),
				ASchemas[i].getName()));
	}   //  fillAcctSchema

	/**
	 *  Fill Posting Type
	 *  @param cb JComboBox to be filled
	 */
	public void fillPostingType (JComboBox cb)
	{
		int AD_Reference_ID = 125;
		ValueNamePair[] pt = MRefList.getList(Env.getCtx(), AD_Reference_ID, true);
		for (int i = 0; i < pt.length; i++)
			cb.addItem(pt[i]);
	}   //  fillPostingType

	/**
	 *  Fill Table with
	 *      ValueNamePair (TableName, translatedKeyColumnName)
	 *  and tableInfo with (TableName, AD_Table_ID)
	 *  and select the entry for AD_Table_ID
	 *
	 *  @param cb JComboBox to be filled
	 */
	public void fillTable (JComboBox cb)
	{
		ValueNamePair select = null;
		//
		String sql = "SELECT AD_Table_ID, TableName FROM AD_Table t "
			+ "WHERE EXISTS (SELECT * FROM AD_Column c"
			+ " WHERE t.AD_Table_ID=c.AD_Table_ID AND c.ColumnName='Posted')"
			+ " AND IsView='N'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int id = rs.getInt(1);
				String tableName = rs.getString(2);
				String name = Msg.translate(Env.getCtx(), tableName+"_ID");
				//
				ValueNamePair pp = new ValueNamePair(tableName, name);
				cb.addItem(pp);
				tableInfo.put (tableName, new Integer(id));
				if (id == AD_Table_ID)
					select = pp;
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		if (select != null)
			cb.setSelectedItem(select);
	}   //  fillTable

	/**
	 *  Fill Org
	 *
	 *  @param cb JComboBox to be filled
	 */
	public void fillOrg (JComboBox cb)
	{
		KeyNamePair pp = new KeyNamePair(0, "");
		cb.addItem(pp);
		String sql = "SELECT AD_Org_ID, Name FROM AD_Org WHERE AD_Client_ID=? ORDER BY Value";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()){
				cb.addItem(new KeyNamePair(rs.getInt(1), rs.getString(2)));

			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		
		cb.setSelectedItem(new KeyNamePair(Env.getAD_Org_ID(Env.getCtx()),new MOrg(Env.getCtx(),Env.getAD_Org_ID(Env.getCtx()),null).getName()));
	}   //  fillOrg

	/**
	 *  Get Button Text
	 *
	 *  @param tableName table
	 *  @param columnName column
	 *  @param selectSQL sql
	 *  @return Text on button
	 */
	public String getButtonText (String tableName, String columnName, String selectSQL)
	{
		//  SELECT (<embedded>) FROM tableName avd WHERE avd.<selectSQL>
		StringBuffer sql = new StringBuffer ("SELECT (");
		Language language = Env.getLanguage(Env.getCtx());
		sql.append(MLookupFactory.getLookup_TableDirEmbed(language, columnName, "avd"))
			.append(") FROM ").append(tableName).append(" avd WHERE avd.").append(selectSQL);
		String retValue = "<" + selectSQL + ">";
		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql.toString());
			if (rs.next())
				retValue = rs.getString(1);
			rs.close();
			stmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		return retValue;
	}   //  getButtonText

	/**************************************************************************

	/**
	 *  Create Query and submit
	 *  @return Report Model
	 */
	public RModel query()
	{
		//  Set Where Clause
		StringBuffer whereClause = new StringBuffer();
		//  Add Organization
		if (C_AcctSchema_ID != 0)
			whereClause.append(RModel.TABLE_ALIAS)
				.append(".C_AcctSchema_ID=").append(C_AcctSchema_ID);

		//	Posting Type Selected
		if (PostingType != null && PostingType.length() > 0)
		{
			if (whereClause.length() > 0)
				whereClause.append(" AND ");
			whereClause.append(RModel.TABLE_ALIAS)
				.append(".PostingType='").append(PostingType).append("'");
		}
		
		//
		if (documentQuery)
		{
			if (whereClause.length() > 0)
				whereClause.append(" AND ");
			whereClause.append(RModel.TABLE_ALIAS).append(".AD_Table_ID=").append(AD_Table_ID)
				.append(" AND ").append(RModel.TABLE_ALIAS).append(".Record_ID=").append(Record_ID);
		}
		else
		{
			//  get values (Queries)
			Iterator<String> it = whereInfo.values().iterator();
			while (it.hasNext())
			{
				String where = (String)it.next();
				if (where != null && where.length() > 0)    //  add only if not empty
				{
					if (whereClause.length() > 0)
						whereClause.append(" AND ");
					whereClause.append(RModel.TABLE_ALIAS).append(".").append(where);
				}
			}
			if (DateFrom != null || DateTo != null)
			{
				if (whereClause.length() > 0)
					whereClause.append(" AND ");
				if (DateFrom != null && DateTo != null)
					whereClause.append("TRUNC(").append(RModel.TABLE_ALIAS).append(".DateAcct, 'DD') BETWEEN ")
						.append(DB.TO_DATE(DateFrom)).append(" AND ").append(DB.TO_DATE(DateTo));
				else if (DateFrom != null)
					whereClause.append("TRUNC(").append(RModel.TABLE_ALIAS).append(".DateAcct, 'DD') >= ")
						.append(DB.TO_DATE(DateFrom));
				else    //  DateTo != null
					whereClause.append("TRUNC(").append(RModel.TABLE_ALIAS).append(".DateAcct, 'DD') <= ")
						.append(DB.TO_DATE(DateTo));
			}
			//  Add Organization
			if (AD_Org_ID != 0)
			{
				if (whereClause.length() > 0)
					whereClause.append(" AND ");
				whereClause.append(RModel.TABLE_ALIAS).append(".AD_Org_ID=").append(AD_Org_ID);
			}
		}

		RModel rm = getRModel(whereClause.toString());
		
		//  Set Order By Clause
		StringBuffer orderClause = new StringBuffer();
		if (sortBy1.length() > 0)
		{
			RColumn col = rm.getRColumn(sortBy1);
			if (col != null)
				orderClause.append(col.getDisplaySQL());
			else
				orderClause.append(RModel.TABLE_ALIAS).append(".").append(sortBy1);
		}
		if (sortBy2.length() > 0)
		{
			if (orderClause.length() > 0)
				orderClause.append(",");
			RColumn col = rm.getRColumn(sortBy2);
			if (col != null)
				orderClause.append(col.getDisplaySQL());
			else
				orderClause.append(RModel.TABLE_ALIAS).append(".").append(sortBy2);
		}
		if (sortBy3.length() > 0)
		{
			if (orderClause.length() > 0)
				orderClause.append(",");
			RColumn col = rm.getRColumn(sortBy3);
			if (col != null)
				orderClause.append(col.getDisplaySQL());
			else
				orderClause.append(RModel.TABLE_ALIAS).append(".").append(sortBy3);
		}
		if (sortBy4.length() > 0)
		{
			if (orderClause.length() > 0)
				orderClause.append(",");
			RColumn col = rm.getRColumn(sortBy4);
			if (col != null)
				orderClause.append(col.getDisplaySQL());
			else
				orderClause.append(RModel.TABLE_ALIAS).append(".").append(sortBy4);
		}
		if (orderClause.length() == 0)
			orderClause.append(RModel.TABLE_ALIAS).append(".Fact_Acct_ID");

		//  Groups
		if (group1 && sortBy1.length() > 0)
			rm.setGroup(sortBy1);
		if (group2 && sortBy2.length() > 0)
			rm.setGroup(sortBy2);
		if (group3 && sortBy3.length() > 0)
			rm.setGroup(sortBy3);
		if (group4 && sortBy4.length() > 0)
			rm.setGroup(sortBy4);

		//  Totals
		//rm.setFunction("AmtAcctDr", RModel.FUNCTION_SUM);
		//rm.setFunction("AmtAcctCr", RModel.FUNCTION_SUM);
		//rm.setFunction("AmtAcctDr-AmtAcctCr", RModel.FUNCTION_SUM);
		rm.query (Env.getCtx(), whereClause.toString(), orderClause.toString());

		return rm;
	}   //  query

	public int getTable_ID(){
		return AD_Table_ID;
	}
	
	public int getRecord_ID(){
		return Record_ID;
	}
	
	/**
	 *  Create Report Model (Columns)
	 *  @return Report Model
	 */
	public RModel getRModel(String whereClause)
	{
		Properties ctx = Env.getCtx();
		RModel rm = new RModel("Fact_Acct");
		//  Add Key (Lookups)
		ArrayList<String> keys = createKeyColumns();
		int max = m_leadingColumns;
		if (max == 0)
			max = keys.size();
		for (int i = 0; i < max; i++)
		{
			String column = (String)keys.get(i);
			if (column != null && column.startsWith("Date"))
				rm.addColumn(new RColumn(ctx, column, DisplayType.Date));
			else if (column != null && column.endsWith("_ID")){
				if(!(whereClause.contains(column) && ( column.equals("Account_ID"))|| column.equals("AD_Org_ID"))) 
					rm.addColumn(new RColumn(ctx, column, DisplayType.TableDir));
			}
		}
		if(displayBalanceMode){
			rm.addColumn(new RColumn(ctx, "AD_Table_ID", DisplayType.TableDir));
			rm.addColumn(new RColumn(ctx, "Description", DisplayType.String));
		}
		
		//  Main Info
		rm.addColumn(new RColumn(ctx, "AmtAcctDr", DisplayType.Amount));
		rm.addColumn(new RColumn(ctx, "AmtAcctCr", DisplayType.Amount));
		rm.addColumn(new RColumn(ctx, "AmtAcctDr-AmtAcctCr", DisplayType.Amount));

		if (displaySourceAmt)
		{
			if (!keys.contains("DateTrx"))
				rm.addColumn(new RColumn(ctx, "DateTrx", DisplayType.Date));
			rm.addColumn(new RColumn(ctx, "C_Currency_ID", DisplayType.TableDir));
			rm.addColumn(new RColumn(ctx, "AmtSourceDr", DisplayType.Amount));
			rm.addColumn(new RColumn(ctx, "AmtSourceCr", DisplayType.Amount));
			rm.addColumn(new RColumn(ctx, "Rate", DisplayType.Amount,
				"CASE WHEN (AmtSourceDr + AmtSourceCr) = 0 THEN 0"
				+ " ELSE (AmtAcctDr + AmtAcctCr) / (AmtSourceDr + AmtSourceCr) END"));
		}
		//	Remaining Keys
		for (int i = max; i < keys.size(); i++)
		{
			String column = (String)keys.get(i);
			if (column != null && column.startsWith("Date"))
				rm.addColumn(new RColumn(ctx, column, DisplayType.Date));
			else if (column.startsWith("UserElement"))
			{
				if (column.indexOf('1') != -1)
					rm.addColumn(new RColumn(ctx, column, DisplayType.TableDir, null, 0, m_ref1));
				else
					rm.addColumn(new RColumn(ctx, column, DisplayType.TableDir, null, 0, m_ref2));
			}
			else if (column != null && column.endsWith("_ID")){
				if(displayBalanceMode){
					if(!column.equals("C_BPartner_ID") && !column.equals("M_Product_ID") )
						rm.addColumn(new RColumn(ctx, column, DisplayType.TableDir));
				}else{	
					if(!whereClause.contains(column)) 
						rm.addColumn(new RColumn(ctx, column, DisplayType.TableDir));
				}
			}
		}
		//	Info
		if (!keys.contains("DateAcct"))
			rm.addColumn(new RColumn(ctx, "DateAcct", DisplayType.Date));
	//	if (!keys.contains("C_Period_ID"))
	//		rm.addColumn(new RColumn(ctx, "C_Period_ID", DisplayType.TableDir));
		if (displayQty)
		{
			rm.addColumn(new RColumn(ctx, "C_UOM_ID", DisplayType.TableDir));
			rm.addColumn(new RColumn(ctx, "Qty", DisplayType.Quantity));
		}
		if (displayDocumentInfo)
		{
			rm.addColumn(new RColumn(ctx, "AD_Table_ID", DisplayType.TableDir));
			rm.addColumn(new RColumn(ctx, "Record_ID", DisplayType.ID));
			rm.addColumn(new RColumn(ctx, "Description", DisplayType.String));
		}
		
		rm.addColumn(new RColumn(ctx, "JournalNo", DisplayType.String));
		
		return rm;
	}   //  createRModel

	/**
	 *  Create the key columns in sequence
	 *  @return List of Key Columns
	 */
	private ArrayList<String> createKeyColumns()
	{
		ArrayList<String> columns = new ArrayList<String>();
		m_leadingColumns = 0;
		//  Sorting Fields
		columns.add(sortBy1);               //  may add ""
		if (!columns.contains(sortBy2))
			columns.add(sortBy2);
		if (!columns.contains(sortBy3))
			columns.add(sortBy3);
		if (!columns.contains(sortBy4))
			columns.add(sortBy4);

		//  Add Account Segments
		MAcctSchemaElement[] elements = ASchema.getAcctSchemaElements();
		for (int i = 0; i < elements.length; i++)
		{
			if (m_leadingColumns == 0 && columns.contains("AD_Org_ID") && columns.contains("Account_ID"))
				m_leadingColumns = columns.size();
			//
			MAcctSchemaElement ase = elements[i];
			String columnName = ase.getColumnName();
			if (columnName.startsWith("UserElement"))
			{
				if (columnName.indexOf('1') != -1)
					m_ref1 = ase.getDisplayColumnName();
				else
					m_ref2 = ase.getDisplayColumnName();
			}
			if (!columns.contains(columnName))
				columns.add(columnName);
		}
		if (m_leadingColumns == 0 && columns.contains("AD_Org_ID") && columns.contains("Account_ID"))
			m_leadingColumns = columns.size();
		return columns;
	}   //  createKeyColumns

}   //  AcctViewerData
