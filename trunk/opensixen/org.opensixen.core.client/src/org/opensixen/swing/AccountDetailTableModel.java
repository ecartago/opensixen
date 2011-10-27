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

package org.opensixen.swing;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MCurrency;
import org.compiere.model.MQuery;
import org.compiere.model.PO;
import org.compiere.util.Env;
import org.compiere.util.Formater;
import org.opensixen.model.ColumnDefinition;
import org.opensixen.model.GroupDefinition;
import org.opensixen.model.GroupVariable;
import org.opensixen.model.I_V_Fact_Acct;
import org.opensixen.model.MVFactAcct;
import org.opensixen.model.POFactory;
import org.opensixen.model.QParam;

/**
 * 
 * 
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 *
 */
public class AccountDetailTableModel extends POTableModel<MVFactAcct> {	
	//private Properties ctx;
	
	private QParam[] m_params = null;
	
	/**
	 * @param ctx
	 */
	public AccountDetailTableModel(Properties ctx) {
		super(ctx, MVFactAcct.class);
		// Add custom cell renders
		addCustomCellRender(I_V_Fact_Acct.COLUMNNAME_AmtAcctDr, new DebitCellRender());
		addCustomCellRender(I_V_Fact_Acct.COLUMNNAME_AmtAcctCr, new CreditCellRender());
		
		initTableModel();			
	}

	class DebitCellRender implements CustomCellRender {		
		@Override
		public Object render(PO po) {
			I_V_Fact_Acct fact = (I_V_Fact_Acct) po;
			if (fact.getAmtAcctDr().equals(fact.getAmtSourceDr()) == false)	{
				StringBuffer sb = new StringBuffer();
				sb.append(Formater.formatAmt(fact.getAmtAcctDr()));
				sb.append(" (").append(Formater.formatAmt(fact.getAmtSourceDr(), fact.getC_Currency_ID())).append(")");
				return sb.toString();
			}
			else 
			return Formater.formatAmt(fact.getAmtAcctDr());
		}
		
	}
	
	class CreditCellRender implements CustomCellRender {		
		@Override
		public Object render(PO po) {
			I_V_Fact_Acct fact = (I_V_Fact_Acct) po;
			if (fact.getAmtAcctCr().equals(fact.getAmtSourceCr()) == false)	{
				StringBuffer sb = new StringBuffer();
				sb.append(Formater.formatAmt(fact.getAmtAcctCr()));				
				sb.append(" (").append(Formater.formatAmt(fact.getAmtSourceCr(), fact.getC_Currency_ID())).append(")");
				return sb.toString();
			}
			else 
			return Formater.formatAmt(fact.getAmtAcctCr());
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.opensixen.interfaces.OTableModel#getColumnDefinitions()
	 */
	@Override
	public ColumnDefinition[] getColumnDefinitions() {
		ColumnDefinition[] cols = {				
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_DateAcct, "Fecha", Timestamp.class ),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_JournalNo, "Asiento", Integer.class),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_Value, "Cuenta" ,  String.class),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_Name, "Nombre", String.class),
				//new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_Description, "Descripcion", String.class),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_TR_TableName, "Documento", String.class),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_DocumentNo, "Doc. No", String.class),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_AmtAcctDr, "Debe", String.class),
				new ColumnDefinition(I_V_Fact_Acct.COLUMNNAME_AmtAcctCr, "Haber", String.class) };
		return cols;
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
		String[] columns = { I_V_Fact_Acct.COLUMNNAME_C_ElementValue_ID };
		GroupVariable[] footer = {
				new GroupVariable(I_V_Fact_Acct.COLUMNNAME_AmtAcctDr, GroupVariable.SUM),
				new GroupVariable(I_V_Fact_Acct.COLUMNNAME_AmtAcctCr,GroupVariable.SUM) };
		def.setGroupColumns(columns);
		def.setFooterVariables(footer);
		definitions.add(def);
		return definitions;
	}
	

	/* (non-Javadoc)
	 * @see org.opensixen.swing.POTableModel#getModel(org.compiere.model.MQuery)
	 */
	@Override
	protected PO[] getModel() {	
		if (m_params == null)	{
			return new PO[0];			
		}				
		String[] order = {MVFactAcct.COLUMNNAME_DateAcct + " desc", MVFactAcct.COLUMNNAME_JournalNo + " desc"};
		List<MVFactAcct> list = POFactory.getList(ctx, MVFactAcct.class, m_params , order, null);				
		return list.toArray(new PO[list.size()]);
	}	
	
	
	public void setParams(QParam[] params)	{	
		this.m_params = params;
	}
}
