/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.apps.search;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.logging.Level;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.adempiere.plaf.AdempierePLAF;
import org.compiere.apps.AEnv;
import org.compiere.apps.ConfirmPanel;
import org.compiere.grid.ed.VPAttributeDialog;
import org.compiere.minigrid.ColumnInfo;
import org.compiere.minigrid.IDColumn;
import org.compiere.minigrid.MiniTable;
import org.compiere.swing.CButton;
import org.compiere.swing.CCheckBox;
import org.compiere.swing.CDialog;
import org.compiere.swing.CLabel;
import org.compiere.swing.CPanel;
import org.compiere.swing.CTextField;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.opensixen.osgi.Service;
import org.opensixen.osgi.interfaces.IPAttributeInstance;
import org.opensixen.osgi.interfaces.IVPAttributeDialog;


/**
 *	Display Product Attribute Instance Info
 *
 *  @author     Jorg Janke
 *  @version    $Id: PAttributeInstance.java,v 1.3 2006/07/30 00:51:27 jjanke Exp $
 */
public class PAttributeInstance extends CDialog 
	implements ListSelectionListener, IPAttributeInstance
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3743466565716139916L;

	private int m_C_BPartner_ID;
	
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	Dimension screensize = toolkit.getScreenSize();

	protected final int        INFO_WIDTH = screensize.width > 1100 ? 1100 : screensize.width - 200;
	protected final int        SCREEN_HEIGHT = screensize.height;

	public PAttributeInstance()	{
		super();
		setTitle(Msg.getMsg(Env.getCtx(), "PAttributeInstance"));
		setModal(true);
	}
	
	/**
	 * 	Constructor
	 * 	@param parent frame parent
	 * 	@param title title
	 * 	@param M_Warehouse_ID warehouse key name pair
	 * 	@param M_Locator_ID locator
	 * 	@param M_Product_ID product key name pair
	 * 	@param C_BPartner_ID bp
	 */
	public PAttributeInstance(JFrame parent, String title,
		int M_Warehouse_ID, int M_Locator_ID, int M_Product_ID, int C_BPartner_ID)
	{
		super (parent, Msg.getMsg(Env.getCtx(), "PAttributeInstance") + title, true);
		init (M_Warehouse_ID, M_Locator_ID, M_Product_ID, C_BPartner_ID);
		AEnv.showCenterWindow(parent, this);
	}
	
	/**
	 * 	Constructor
	 * 	@param parent dialog parent
	 * 	@param title title
	 * 	@param M_Warehouse_ID warehouse key name pair
	 * 	@param M_Locator_ID locator
	 * 	@param M_Product_ID product key name pair
	 * 	@param C_BPartner_ID bp
	 */
	protected PAttributeInstance(JDialog parent, String title,
		int M_Warehouse_ID, int M_Locator_ID, int M_Product_ID, int C_BPartner_ID)
	{
		super (parent, Msg.getMsg(Env.getCtx(), "PAttributeInstance") + title, true);
		init (M_Warehouse_ID, M_Locator_ID, M_Product_ID, C_BPartner_ID);
		AEnv.showCenterWindow(parent, this);
	}

	/**
	 * 	Initialization
	 *	@param M_Warehouse_ID wh
	 *	@param M_Locator_ID loc
	 *	@param M_Product_ID product
	 *	@param C_BPartner_ID partner
	 */
	public void init (int M_Warehouse_ID, int M_Locator_ID, int M_Product_ID, int C_BPartner_ID)
	{
		log.info("M_Warehouse_ID=" + M_Warehouse_ID 
			+ ", M_Locator_ID=" + M_Locator_ID
			+ ", M_Product_ID=" + M_Product_ID);
		m_M_Warehouse_ID = M_Warehouse_ID;
		m_M_Locator_ID = M_Locator_ID;
		m_M_Product_ID = M_Product_ID;
		m_C_BPartner_ID = C_BPartner_ID;
		try
		{
			jbInit();
			dynInit(C_BPartner_ID);
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "", e);
		}
	}

	private CPanel mainPanel = new CPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private CPanel northPanel = new CPanel();
	private FlowLayout northLayout = new FlowLayout();
	private JScrollPane centerScrollPane = new JScrollPane();
	private ConfirmPanel confirmPanel = new ConfirmPanel (true, true, false, false, false, false, false);
	private CCheckBox showAll = new CCheckBox (Msg.getMsg(Env.getCtx(), "ShowAll"));
	private CLabel labelVendorLot = new CLabel(Msg.translate(Env.getCtx(), "VendorLot"));
	private CTextField fieldVendorLot = new CTextField(10);
	private CLabel labelSpec = new CLabel(Msg.translate(Env.getCtx(), "C_Specification_ID"));
	private CTextField fieldSpec = new CTextField(10);
	//
	private MiniTable 			m_table = new MiniTable();
	//	Parameter
	private int			 		m_M_Warehouse_ID;
	private int			 		m_M_Locator_ID;
	private int			 		m_M_Product_ID;
	private boolean				createAttribute;
	//
	private int					m_M_AttributeSetInstance_ID = -1;
	private String				m_M_AttributeSetInstanceName = null;
	private String				m_sql;
	/** Cancel pressed - need to differentiate between OK - Cancel - Exit	*/
	private boolean			   	m_cancel = false;
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(PAttributeInstance.class);

	/**
	 * 	Static Init
	 * 	@throws Exception
	 */
	private void jbInit() throws Exception
	{		

		mainPanel.setLayout(mainLayout);
		mainPanel.setPreferredSize(new Dimension(INFO_WIDTH, SCREEN_HEIGHT > 600 ? 500 : 300));
        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		//	North
        fieldSpec.setBackground(AdempierePLAF.getInfoBackground());
        fieldSpec.addActionListener(this);
		fieldVendorLot.setBackground(AdempierePLAF.getInfoBackground());
		fieldVendorLot.addActionListener(this);
		northPanel.setLayout(northLayout);
		northPanel.add(labelSpec);
		northPanel.add(fieldSpec);
		northPanel.add(labelVendorLot);
		northPanel.add(fieldVendorLot);
		northPanel.add(showAll);
		showAll.addActionListener(this);
		mainPanel.add(northPanel, BorderLayout.NORTH);
		//	Center
		mainPanel.add(centerScrollPane, BorderLayout.CENTER);
		centerScrollPane.getViewport().add(m_table, null);
		//	South
		if (createAttribute) {
			CButton bNew = ConfirmPanel.createNewButton(true);
			bNew.addActionListener(this);
			confirmPanel.addComponent(bNew);
		}
		mainPanel.add(confirmPanel, BorderLayout.SOUTH);
		confirmPanel.addActionListener(this);
	}

	/**	Table Column Layout Info			*/
	private static ColumnInfo[] s_layout = new ColumnInfo[] 
	{
		new ColumnInfo(" ", "asi.M_AttributeSetInstance_ID", IDColumn.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "specscode"), "spec.value", String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "specsname"), "spec.name", String.class),
//		new ColumnInfo(Msg.translate(Env.getCtx(), "Description"), "asi.Description", String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "Lot"), "asi.Lot", String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "SerNo"), "asi.SerNo", String.class), 
		new ColumnInfo(Msg.translate(Env.getCtx(), "GuaranteeDate"), "asi.GuaranteeDate", Timestamp.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "M_Locator_ID"), "l.Value", KeyNamePair.class, "s.M_Locator_ID"),
//		new ColumnInfo(Msg.translate(Env.getCtx(), "M_Product_ID"), "p.Value", KeyNamePair.class, "p.M_Product_ID"), // @Trifon - Not sure if this need to be shown
//		new ColumnInfo(Msg.translate(Env.getCtx(), "M_AttributeSet_ID"), "st.Name", KeyNamePair.class, "st.M_AttributeSet_ID"), // @Trifon - Not sure if this need to be shown
		new ColumnInfo(Msg.translate(Env.getCtx(), "QtyOnHand"), "s.QtyOnHand", Double.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "QtyReserved"), "s.QtyReserved", Double.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "QtyOrdered"), "s.QtyOrdered", Double.class),
		//	See RV_Storage
//		new ColumnInfo(Msg.translate(Env.getCtx(), "GoodForDays"), "(daysbetween(asi.GuaranteeDate, SYSDATE))-p.GuaranteeDaysMin", Integer.class, true, true, null),
//		new ColumnInfo(Msg.translate(Env.getCtx(), "ShelfLifeDays"), "daysbetween(asi.GuaranteeDate, SYSDATE)", Integer.class),
//		new ColumnInfo(Msg.translate(Env.getCtx(), "ShelfLifeRemainingPct"), "CASE WHEN p.GuaranteeDays > 0 THEN TRUNC(((daysbetween(asi.GuaranteeDate, SYSDATE))/p.GuaranteeDays)*100) ELSE 0 END", Integer.class),
	};
	/**	From Clause							*/
	private static String s_sqlFrom = "M_AttributeSetInstance asi"
		+ " INNER JOIN M_AttributeSet st ON (st.M_AttributeSet_ID=asi.M_AttributeSet_ID )"
		+ " LEFT OUTER JOIN M_Storage s ON (s.M_AttributeSetInstance_ID=asi.M_AttributeSetInstance_ID)"
		+ " LEFT OUTER JOIN M_Locator l ON (s.M_Locator_ID=l.M_Locator_ID)"
		+ " LEFT OUTER JOIN M_Product p ON (s.M_Product_ID=p.M_Product_ID)"
		+ " LEFT OUTER JOIN M_Product pr ON (asi.M_AttributeSet_ID = pr.M_AttributeSet_ID)"
		+ " LEFT OUTER JOIN C_specification spec ON (asi.c_specification_id = spec.c_specification_id)" 
	;
	/** Where Clause						*/ 
	private static String s_sqlWhereWithoutWarehouse =
		" (pr.M_Product_ID=? AND p.M_Product_ID=?)" + 
		" AND (replace(replace(replace(replace(replace(replace(replace(upper(spec.value), ' ', ''), '.', ''), '-', ''), '/', ''), '(', ''), ')', ''), ',', '') LIKE '%' || replace(replace(replace(replace(replace(replace(replace(upper(?), ' ', ''), '.', ''), '-', ''), '/', ''), '(', ''), ')', ''), ',', '') || '%') "+	
		" AND (replace(replace(replace(replace(replace(replace(replace(upper(asi.VendorLot), ' ', ''), '.', ''), '-', ''), '/', ''), '(', ''), ')', ''), ',', '') LIKE '%' || replace(replace(replace(replace(replace(replace(replace(upper(?), ' ', ''), '.', ''), '-', ''), '/', ''), '(', ''), ')', ''), ',', '') || '%') ";	
	// egomez: Cambiamos clausula OR por AND porque salian registros duplicados y no le encontre sentido
	private static String s_sqlWhereSameWarehouse = " AND (l.M_Warehouse_ID=? OR 0=?)";

	private String	m_sqlNonZero = " AND (s.QtyOnHand<>0 OR s.QtyReserved<>0 OR s.QtyOrdered<>0)";
	private String	m_sqlMinLife = "";

	/**
	 * 	Dynamic Init
	 * 	@param C_BPartner_ID BP
	 */
	private void dynInit(int C_BPartner_ID)
	{
		log.config("C_BPartner_ID=" + C_BPartner_ID);
		if (C_BPartner_ID != 0)
		{
			int ShelfLifeMinPct = 0;
			int ShelfLifeMinDays = 0;
			String sql = "SELECT bp.ShelfLifeMinPct, bpp.ShelfLifeMinPct, bpp.ShelfLifeMinDays "
				+ "FROM C_BPartner bp "
				+ " LEFT OUTER JOIN C_BPartner_Product bpp"
				+	" ON (bp.C_BPartner_ID=bpp.C_BPartner_ID AND bpp.M_Product_ID=?) "
				+ "WHERE bp.C_BPartner_ID=?";
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement(sql, null);
				pstmt.setInt(1, m_M_Product_ID);
				pstmt.setInt(2, C_BPartner_ID);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					ShelfLifeMinPct = rs.getInt(1);		//	BP
					int pct = rs.getInt(2);				//	BP_P
					if (pct > 0)	//	overwrite
						ShelfLifeMinDays = pct;
					ShelfLifeMinDays = rs.getInt(3);
				}
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, sql, e);
			}
			finally {
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
			if (ShelfLifeMinPct > 0)
			{
				m_sqlMinLife = " AND COALESCE(TRUNC(((daysbetween(asi.GuaranteeDate, SYSDATE))/p.GuaranteeDays)*100),0)>=" + ShelfLifeMinPct;
				log.config( "PAttributeInstance.dynInit - ShelfLifeMinPct=" + ShelfLifeMinPct);
			}
			if (ShelfLifeMinDays > 0)
			{
				m_sqlMinLife += " AND COALESCE((daysbetween(asi.GuaranteeDate, SYSDATE)),0)>=" + ShelfLifeMinDays;
				log.config( "PAttributeInstance.dynInit - ShelfLifeMinDays=" + ShelfLifeMinDays);
			}
		}	//	BPartner != 0

		m_sql = m_table.prepareTable (getLayoutInfo(), s_sqlFrom, s_sqlWhereWithoutWarehouse, false, "asi")
				+ " ORDER BY asi.GuaranteeDate, s.QtyOnHand";	//	oldest, smallest first
		//
		m_table.setRowSelectionAllowed(true);
		m_table.setMultiSelection(false);
		m_table.addMouseListener(this);
		m_table.getSelectionModel().addListSelectionListener(this);
		//
		refresh();
	}

	/**
	 * 	Refresh Query
	 */
	private void refresh()
	{
		String sql = m_sql;
		int pos = m_sql.lastIndexOf(" ORDER BY ");
		if (!showAll.isSelected())
		{
			sql = m_sql.substring(0, pos) 
				+ m_sqlNonZero + s_sqlWhereSameWarehouse;
			if (m_sqlMinLife.length() > 0)
				sql += m_sqlMinLife;
			sql += m_sql.substring(pos);
		}
		//
		log.finest(sql);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, m_M_Product_ID);
			pstmt.setInt(2, m_M_Product_ID);
			pstmt.setString(3, fieldSpec.getText());
			pstmt.setString(4, fieldVendorLot.getText());

			if ( !showAll.isSelected() ) {
				pstmt.setInt(5, m_M_Warehouse_ID);
				pstmt.setInt(6, m_M_Warehouse_ID);
			}

			rs = pstmt.executeQuery();
			m_table.loadTable(rs);
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		enableButtons();
	}

	/* (non-Javadoc)
	 * @see org.compiere.apps.search.IPAttributeInstance#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals(ConfirmPanel.A_OK))
			dispose();
		else if (e.getActionCommand().equals(ConfirmPanel.A_REFRESH))
		{
			refresh();
		}
		else if (e.getActionCommand().equals(ConfirmPanel.A_NEW))
		{
			IVPAttributeDialog vad = VPAttributeDialog.getDialog(Env.getFrame(this), 0, m_M_Product_ID, m_C_BPartner_ID, false, 0, 0);
			if (vad.isChanged())
			{
				m_M_AttributeSetInstance_ID = vad.getM_AttributeSetInstance_ID();
				m_M_AttributeSetInstanceName = vad.getM_AttributeSetInstanceName();
				dispose();
			}		
		}
		else if (e.getActionCommand().equals(ConfirmPanel.A_CANCEL))
		{
			dispose();
			m_M_AttributeSetInstance_ID = -1;
			m_M_AttributeSetInstanceName = null;
			m_cancel = true;
		}
		else if (e.getSource() == showAll || e.getSource() == fieldVendorLot || e.getSource() == fieldSpec)
		{
			refresh();
		}
	}
 
	/* (non-Javadoc)
	 * @see org.compiere.apps.search.IPAttributeInstance#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged (ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting())
			return;
		enableButtons();
	}

	/**
	 * 	Enable/Set Buttons and set ID
	 */
	private void enableButtons()
	{
		m_M_AttributeSetInstance_ID = -1;
		m_M_AttributeSetInstanceName = null;
		m_M_Locator_ID = 0;
		int row = m_table.getSelectedRow();
		boolean enabled = row != -1;
		if (enabled)
		{
			Integer ID = m_table.getSelectedRowKey();
			if (ID != null)
			{
				m_M_AttributeSetInstance_ID = ID.intValue();
				m_M_AttributeSetInstanceName = (String)m_table.getValueAt(row, 3);
				//
				Object oo = m_table.getValueAt(row, 5);
				if (oo instanceof KeyNamePair)
				{
					KeyNamePair pp = (KeyNamePair)oo;
					m_M_Locator_ID = pp.getKey();
				}
			}
		}
		confirmPanel.getOKButton().setEnabled(enabled);
		log.fine("M_AttributeSetInstance_ID=" + m_M_AttributeSetInstance_ID 
			+ " - " + m_M_AttributeSetInstanceName
			+ "; M_Locator_ID=" + m_M_Locator_ID);
	}

	/* (non-Javadoc)
	 * @see org.compiere.apps.search.IPAttributeInstance#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e)
	{
		//  Double click with selected row => exit
		if (e.getClickCount() > 1 && m_table.getSelectedRow() != -1)
		{
			enableButtons();
			dispose();
		}
	}


	/* (non-Javadoc)
	 * @see org.compiere.apps.search.IPAttributeInstance#getM_AttributeSetInstance_ID()
	 */
	@Override
	public int getM_AttributeSetInstance_ID()
	{
		return m_M_AttributeSetInstance_ID;
	}

	/* (non-Javadoc)
	 * @see org.compiere.apps.search.IPAttributeInstance#getM_AttributeSetInstanceName()
	 */
	@Override
	public String getM_AttributeSetInstanceName()
	{
		return m_M_AttributeSetInstanceName;
	}

	/* (non-Javadoc)
	 * @see org.compiere.apps.search.IPAttributeInstance#getM_Locator_ID()
	 */
	@Override
	public int getM_Locator_ID()
	{
		return m_M_Locator_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.compiere.apps.search.IPAttributeInstance#getLayoutInfo()
	 */
	@Override
	public ColumnInfo[] getLayoutInfo()	{
		return s_layout;
	}
	
	/**
	 *	Is cancelled?
	 *	- if pressed Cancel = true
	 *	- if pressed OK or window closed = false
	 *  @return true if cancelled
	 */
	@Override
	public boolean isCancelled()
	{
		return m_cancel;
	}	//	isCancelled

	/**
	 * Determine if show new button
	 * @param createAttribute True o false
	 */
	@Override
	public void setCreateAttribute(boolean createAttribute) {
		this.createAttribute = createAttribute;
	}

	public static IPAttributeInstance get(JDialog parent, String title,
			int M_Warehouse_ID, int M_Locator_ID, int M_Product_ID, int C_BPartner_ID)	{
		return get(parent, title, M_Warehouse_ID, M_Locator_ID, M_Product_ID, C_BPartner_ID, false);
	}

	public static IPAttributeInstance get(JDialog parent, String title,
			int M_Warehouse_ID, int M_Locator_ID, int M_Product_ID, int C_BPartner_ID, boolean createAttribute)	{
		
		IPAttributeInstance instance = Service.locate(IPAttributeInstance.class);
		if (instance != null)	{
			instance.setCreateAttribute(createAttribute);
			instance.init(M_Warehouse_ID, M_Locator_ID, M_Product_ID, C_BPartner_ID);
			AEnv.showCenterWindow(parent, (Dialog) instance);
			return instance;
		}
		
		instance = new PAttributeInstance(parent, title, M_Warehouse_ID, M_Locator_ID, M_Product_ID, C_BPartner_ID);
		instance.setCreateAttribute(createAttribute);
		return instance;
	}
}