 /******* BEGIN LICENSE BLOCK *****
 * Versión: GPL 2.0/CDDL 1.0/EPL 1.0
 *
 * Los contenidos de este fichero están sujetos a la Licencia
 * Pública General de GNU versión 2.0 (la "Licencia"); no podrá
 * usar este fichero, excepto bajo las condiciones que otorga dicha 
 * Licencia y siempre de acuerdo con el contenido de la presente. 
 * Una copia completa de las condiciones de de dicha licencia,
 * traducida en castellano, deberá estar incluida con el presente
 * programa.
 * 
 * Adicionalmente, puede obtener una copia de la licencia en
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Este fichero es parte del programa opensiXen.
 *
 * OpensiXen es software libre: se puede usar, redistribuir, o
 * modificar; pero siempre bajo los términos de la Licencia 
 * Pública General de GNU, tal y como es publicada por la Free 
 * Software Foundation en su versión 2.0, o a su elección, en 
 * cualquier versión posterior.
 *
 * Este programa se distribuye con la esperanza de que sea útil,
 * pero SIN GARANTÍA ALGUNA; ni siquiera la garantía implícita 
 * MERCANTIL o de APTITUD PARA UN PROPÓSITO DETERMINADO. Consulte 
 * los detalles de la Licencia Pública General GNU para obtener una
 * información más detallada. 
 *
 * TODO EL CÓDIGO PUBLICADO JUNTO CON ESTE FICHERO FORMA PARTE DEL 
 * PROYECTO OPENSIXEN, PUDIENDO O NO ESTAR GOBERNADO POR ESTE MISMO
 * TIPO DE LICENCIA O UNA VARIANTE DE LA MISMA.
 *
 * El desarrollador/es inicial/es del código es
 *  FUNDESLE (Fundación para el desarrollo del Software Libre Empresarial).
 *  Indeos Consultoria S.L. - http://www.indeos.es
 *
 * Contribuyente(s):
 *  Eloy Gómez García <eloy@opensixen.org> 
 *
 * Alternativamente, y a elección del usuario, los contenidos de este
 * fichero podrán ser usados bajo los términos de la Licencia Común del
 * Desarrollo y la Distribución (CDDL) versión 1.0 o posterior; o bajo
 * los términos de la Licencia Pública Eclipse (EPL) versión 1.0. Una 
 * copia completa de las condiciones de dichas licencias, traducida en 
 * castellano, deberán de estar incluidas con el presente programa.
 * Adicionalmente, es posible obtener una copia original de dichas 
 * licencias en su versión original en
 *  http://www.opensource.org/licenses/cddl1.php  y en  
 *  http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * Si el usuario desea el uso de SU versión modificada de este fichero 
 * sólo bajo los términos de una o más de las licencias, y no bajo los 
 * de las otra/s, puede indicar su decisión borrando las menciones a la/s
 * licencia/s sobrantes o no utilizadas por SU versión modificada.
 *
 * Si la presente licencia triple se mantiene íntegra, cualquier usuario 
 * puede utilizar este fichero bajo cualquiera de las tres licencias que 
 * lo gobiernan,  GPL 2.0/CDDL 1.0/EPL 1.0.
 *
 * ***** END LICENSE BLOCK ***** */

package org.opensixen.spain.financial.reports;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MFactAcct;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.Env;
import org.opensixen.model.ColumnDefinition;
import org.opensixen.model.GroupDefinition;
import org.opensixen.model.GroupVariable;
import org.opensixen.model.I_V_Fact_Acct;
import org.opensixen.model.MVFactAcct;
import org.opensixen.model.QParam;
import org.opensixen.osgi.interfaces.ICommand;
import org.opensixen.report.AbstractPODynamicReport;


/**
 * 
 * ReportLibroDiario 
 *
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 */
public class ReportLibroDiario extends AbstractPODynamicReport {

	private int p_C_BPartner_ID;
	private Timestamp p_DateFrom;
	private Timestamp p_DateTo;
	private int p_C_ElementValue_ID;
	private int p_M_Product_ID;


	/**
	 * @param ctx
	 */
	protected ReportLibroDiario(Properties ctx) {
		super(ctx);
	}

	
	/**
	 * @param ctx
	 */
	public ReportLibroDiario() {
		super(Env.getCtx());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opensixen.report.AbstractDynamicJasperReport#getColumns()
	 */
	@Override
	protected ColumnDefinition[] getColumns() {
		ColumnDefinition[] cols = {
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_JournalNo, 80),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_DateAcct, 100 ),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_Value, 80),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_Name, 240),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_Description, 300),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_AmtAcctDr, 100),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_AmtAcctCr, 100) };
		return cols;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opensixen.report.AbstractDynamicJasperReport#getTitle()
	 */
	@Override
	public String getTitle() {
		return "Libro Diario";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opensixen.report.AbstractDynamicJasperReport#getReportClass()
	 */
	@Override
	protected Class<MVFactAcct> getReportClass() {
		return MVFactAcct.class;
	}

	
	
	
	/* (non-Javadoc)
	 * @see org.opensixen.report.AbstractPODynamicReport#getQParams()
	 */
	@Override
	protected QParam[] getQParams() {
		ArrayList<QParam> params = new ArrayList<QParam>();
		params.add(new QParam(MVFactAcct.COLUMNNAME_AD_Client_ID, Env.getAD_Client_ID(getCtx())));
		if (p_DateFrom != null)	{
			String cond = MVFactAcct.COLUMNNAME_DateAcct +" between '" + p_DateFrom + "' and '" + p_DateTo +"'";
			params.add(new QParam(cond));
		}
		if (p_C_ElementValue_ID > 0) {
			params.add(new QParam(MVFactAcct.COLUMNNAME_C_ElementValue_ID, p_C_ElementValue_ID));
		}
		
		if (p_C_BPartner_ID > 0)	{
			params.add(new QParam(MVFactAcct.COLUMNNAME_C_BPartner_ID, p_C_BPartner_ID));
		}
		QParam[] p = new QParam[params.size()];		
		return params.toArray(p);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.compiere.process.SvrProcess#getCtx()
	 */
	@Override
	public Properties getCtx() {
		return Env.getCtx();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opensixen.report.AbstractDynamicJasperReport#getGroupDefinitions()
	 */
	@Override
	protected List<GroupDefinition> getGroupDefinitions() {
		ArrayList<GroupDefinition> definitions = new ArrayList<GroupDefinition>();

		// Agrupamos por numero de asiento
		GroupDefinition def = new GroupDefinition();
		String[] columns = { I_V_Fact_Acct.COLUMNNAME_JournalNo };
		GroupVariable[] footer = {
				new GroupVariable(I_V_Fact_Acct.COLUMNNAME_AmtAcctDr, GroupVariable.SUM),
				new GroupVariable(I_V_Fact_Acct.COLUMNNAME_AmtAcctCr,GroupVariable.SUM) };
		def.setGroupColumns(columns);
		def.setFooterVariables(footer);
		definitions.add(def);
		return definitions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opensixen.report.AbstractDynamicJasperReport#getOrderColumns()
	 */
	@Override
	protected String[] getOrderColumns() {
		String[] order = { I_V_Fact_Acct.COLUMNNAME_JournalNo };
		return order;
	}


	/* (non-Javadoc)
	 * @see org.opensixen.osgi.interfaces.ICommand#prepare()
	 */
	public void prepare(ProcessInfoParameter[] para) {
		
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null) {
				;
			}
			else if (name.equals("C_ElementValue_ID")) {
				p_C_ElementValue_ID = para[i].getParameterAsInt();
			}
			else if (name.equals("DateAcct")) {
				p_DateFrom = (Timestamp)para[i].getParameter();
				p_DateTo = (Timestamp)para[i].getParameter_To();
			}
			else if (name.equals("C_BPartner_ID")) {
				p_C_BPartner_ID = para[i].getParameterAsInt();
			}
			else if (name.equals("M_Product_ID")) {
				p_M_Product_ID = para[i].getParameterAsInt();
			}
			else {
				//log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
		
	}


	/* (non-Javadoc)
	 * @see org.opensixen.osgi.interfaces.ICommand#doIt()
	 */
	public String doIt() throws Exception {
		initReport();
		viewReport();
		return null;
	}


}
