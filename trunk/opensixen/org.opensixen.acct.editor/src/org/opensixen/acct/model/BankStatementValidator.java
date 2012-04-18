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
 *  Nexis Servicios Informáticos S.L. - http://www.nexis.es
 *
 * Contribuyente(s):
 *  Alejandro González <alejandro@opensixen.org> 
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
package org.opensixen.acct.model;

import java.util.List;

import org.compiere.acct.DocLine_Allocation;
import org.compiere.acct.DocLine_Bank;
import org.compiere.acct.Fact;
import org.compiere.acct.FactLine;
import org.compiere.model.FactsValidator;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MClient;
import org.compiere.model.MInvoice;
import org.compiere.model.MPayment;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.PO;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.opensixen.osgi.interfaces.IModelValidator;

public class BankStatementValidator implements IModelValidator, FactsValidator {


	public static final  String[] acctTables = { "C_BankStatement"};
	
	public BankStatementValidator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(ModelValidationEngine engine, MClient client) {
		
		for (String table:acctTables)	{
			engine.addFactsValidate(table, this);
		}
		
	}

	@Override
	public int getAD_Client_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String modelChange(PO po, int type) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String docValidate(PO po, int timing) {
		// TODO Auto-generated method stub
		return null;
	}


	public String factsValidate(MAcctSchema schema, List<Fact> facts, PO po) {
		for (Fact fact:facts)	{
			for (FactLine line: fact.getLines())	{
				// First line, set doc.

					DocLine_Bank docLine = (DocLine_Bank) line.getDocLine();
					// Get invoice lines		
					if(docLine.getC_Payment_ID()>0){
						MPayment payment = new MPayment(Env.getCtx(), docLine.getC_Payment_ID(), null);
						line.setDescription(line.getDescription()+","+Msg.translate(Env.getCtx(), "C_Payment_ID")+" "+payment.getDocumentNo());
					
						if(payment.getC_Invoice_ID()>0){
							MInvoice invoice = new MInvoice(Env.getCtx(), payment.getC_Invoice_ID(), null);
							line.setDescription(line.getDescription()+","+Msg.translate(Env.getCtx(), "C_Invoice_ID")+" "+invoice.getDocumentNo());

						}
					}

				
			}
		}
		return null;
	}

	@Override
	public String factsValidate(MAcctSchema schema, List<Fact> facts, PO po,
			int factsTiming) {
		for (Fact fact:facts)	{
			for (FactLine line: fact.getLines())	{
				// First line, set doc.

					DocLine_Bank docLine = (DocLine_Bank) line.getDocLine();
					// Get invoice lines		
					if(docLine.getC_Payment_ID()>0){
						MPayment payment = new MPayment(Env.getCtx(), docLine.getC_Payment_ID(), null);
						line.setDescription(line.getDescription()+","+Msg.translate(Env.getCtx(), "C_Payment_ID")+" "+payment.getDocumentNo());
					
						if(payment.getC_Invoice_ID()>0){
							MInvoice invoice = new MInvoice(Env.getCtx(), payment.getC_Invoice_ID(), null);
							line.setDescription(line.getDescription()+","+Msg.translate(Env.getCtx(), "C_Invoice_ID")+" "+invoice.getDocumentNo());

						}
					}

				
			}
		}
		return null;
	}

}
