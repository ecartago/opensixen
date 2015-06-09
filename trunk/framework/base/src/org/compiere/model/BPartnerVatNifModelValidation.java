package org.compiere.model;

import java.util.List;
import java.util.Properties;

import org.compiere.util.CLogger;
import org.compiere.util.Msg;

public class BPartnerVatNifModelValidation implements ModelValidator {

	protected CLogger log = CLogger.getCLogger (getClass());
	private int m_AD_Client_ID;
	private String trxName;
	private Properties ctx;
	
	@Override
	public void initialize(ModelValidationEngine engine, MClient client) {
		log.info("Regiter BPartner Model Validator");
		if (client != null) {	
			m_AD_Client_ID = client.getAD_Client_ID();
			trxName = client.get_TrxName();
			ctx = client.getCtx();
		}
		engine.addModelChange(MBPartner.Table_Name, this);

	}

	@Override
	public int getAD_Client_ID() {
		return m_AD_Client_ID;
	}

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		return null;
	}

	@Override
	public String modelChange(PO po, int type) throws Exception {
		String result = null;
		MBPartner m_BPartner = (MBPartner) po;
		if (type == TYPE_BEFORE_NEW || type == TYPE_BEFORE_CHANGE) {
			log.info("Validate: "+m_BPartner);
			if (m_BPartner.isActive()) {
				
				// Check if exists a c_bpartner with that taxid
				List<X_C_BPartner> partners = new Query(ctx, X_C_BPartner.Table_Name , "isactive = 'Y' and taxid = ?", trxName)
				  .setParameters(m_BPartner.getTaxID())
				  .list();
				if (partners.size() > 0) {
					result = Msg.translate(ctx, "BPartnerVatNifExisting") + ": "+partners.get(0).getName();
				}
				
			}
		}
		return result;
	}

	@Override
	public String docValidate(PO po, int timing) {
		return null;
	}

}
