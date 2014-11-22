package org.compiere.model;

import java.util.List;
import java.util.Properties;

import org.compiere.util.CLogger;
import org.compiere.util.Msg;

public class BPartnerLocationModelValidator implements ModelValidator {

	protected CLogger log = CLogger.getCLogger (getClass());

	private int m_AD_Client_ID;
	private String trxName;
	private Properties ctx;
	
	@Override
	public void initialize(ModelValidationEngine engine, MClient client) {
		log.info("Regiter BPartnerLocation Model Validator");
		if (client != null) {	
			m_AD_Client_ID = client.getAD_Client_ID();
			trxName = client.get_TrxName();
			ctx = client.getCtx();
		}
		engine.addModelChange(MBPartnerLocation.Table_Name, this);
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
		MBPartnerLocation m_BPartnerLocation = (MBPartnerLocation) po;
		if (type == TYPE_BEFORE_NEW || type == TYPE_BEFORE_CHANGE) {
			log.info("Validate: "+m_BPartnerLocation);
			if (m_BPartnerLocation.isBillTo() && m_BPartnerLocation.isActive()) {
				// Check have only one bill to address
				List<X_C_BPartner_Location> locations = new Query(ctx, X_C_BPartner_Location.Table_Name , "c_bpartner_id=? and isbillto = 'Y' and isactive = 'Y' and c_bpartner_location_id <> ?", trxName)
				  .setParameters(m_BPartnerLocation.getC_BPartner_ID(), m_BPartnerLocation.getC_BPartner_Location_ID())
				  .list();
				if (locations.size() > 0) {
					result = Msg.translate(ctx, "BPartnerLocationHaveOtherBillAddress") + ": "+locations.get(0).getName();
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
