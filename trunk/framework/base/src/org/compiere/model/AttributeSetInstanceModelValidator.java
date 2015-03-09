package org.compiere.model;

import java.sql.Timestamp;
import java.util.Properties;

import org.compiere.util.CLogger;
import org.compiere.util.Msg;

public class AttributeSetInstanceModelValidator implements ModelValidator {

	protected CLogger log = CLogger.getCLogger (getClass());

	private int m_AD_Client_ID;
	private String trxName;
	private Properties ctx;
	
	@Override
	public void initialize(ModelValidationEngine engine, MClient client) {
		log.info("Regiter AttributeSetInstance Model Validator");
		if (client != null) {	
			m_AD_Client_ID = client.getAD_Client_ID();
			trxName = client.get_TrxName();
			ctx = client.getCtx();
		}
		engine.addModelChange(MAttributeSetInstance.Table_Name, this);
	}

	@Override
	public int getAD_Client_ID() {
		return m_AD_Client_ID;
	}

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String modelChange(PO po, int type) throws Exception {
		String result = null;
		MAttributeSetInstance m_AttributeSetInstance = (MAttributeSetInstance) po;
/*		if (type == TYPE_BEFORE_DELETE) {
			log.info("Delete: "+m_AttributeSetInstance);
			if (m_AttributeSetInstance.getM_AttributeSetInstance_ID() == 0) {
				result = Msg.translate(ctx, "AttributeSetInstanceNotDeleteZero");
			}
		}
*/
		if (type == TYPE_BEFORE_NEW || type == TYPE_BEFORE_CHANGE) {
			log.info("Validate: "+m_AttributeSetInstance);
			// Check GuaranteeDate >= ManufactureDate
			Timestamp guaranteeDate = m_AttributeSetInstance.getGuaranteeDate();
			Timestamp manufactureDate = (Timestamp) m_AttributeSetInstance.get_Value("ManufactureDate");
			if (guaranteeDate != null && manufactureDate != null && guaranteeDate.compareTo(manufactureDate) < 0) {
				result = Msg.translate(ctx, "AttributeSetInstanceDatesInvalid");
			}
		}
		return result;
	}

	@Override
	public String docValidate(PO po, int timing) {
		return null;
	}
	
}
