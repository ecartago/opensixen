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

	@Override
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

}
