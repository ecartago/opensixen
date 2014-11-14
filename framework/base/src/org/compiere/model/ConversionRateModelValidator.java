package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Msg;

public class ConversionRateModelValidator implements ModelValidator {

	protected CLogger log = CLogger.getCLogger (getClass());

	private int m_AD_Client_ID;
	private String trxName;
	private Properties ctx;
	
	@Override
	public void initialize(ModelValidationEngine engine, MClient client) {
		log.info("Regiter ConversionRate Model Validator");
		if (client != null) {	
			m_AD_Client_ID = client.getAD_Client_ID();
			trxName = client.get_TrxName();
			ctx = client.getCtx();
		}
		engine.addModelChange(MConversionRate.Table_Name, this);
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
		if (type == TYPE_BEFORE_NEW || type == TYPE_BEFORE_CHANGE) {
			PreparedStatement pstmt = null;
			MConversionRate m_ConversionRate = (MConversionRate) po;
			log.info("Validate: "+m_ConversionRate);
			if (m_ConversionRate.isActive()) {
				try
				{
					pstmt = DB.prepareStatement(
							"SELECT * FROM c_conversion_rate "+
					        "WHERE isactive = 'Y' AND c_conversion_rate_id <> ? AND c_conversiontype_id = ?"+
					        " AND c_currency_id = ? AND c_currency_id_to = ? AND validto >= ? AND validfrom <= ? ",
							trxName
					);
					pstmt.setInt(1, m_ConversionRate.getC_Conversion_Rate_ID());
					pstmt.setInt(2, m_ConversionRate.getC_ConversionType_ID());
					pstmt.setInt(3, m_ConversionRate.getC_Currency_ID());
					pstmt.setInt(4, m_ConversionRate.getC_Currency_ID_To());
					pstmt.setDate(5, new java.sql.Date(m_ConversionRate.getValidFrom().getTime()));
					pstmt.setDate(6, new java.sql.Date(m_ConversionRate.getValidTo() != null ? m_ConversionRate.getValidTo().getTime() : new Date().getTime()));
					ResultSet rs = pstmt.executeQuery();
					if (rs != null) {
						if (rs.next()) {
							MConversionRate m_Error = new MConversionRate(ctx, rs, trxName);
							log.info("Overlap with: "+m_Error);
							MCurrency currencyFrom = getCurrency(m_Error.getC_Currency_ID());
							MCurrency currencyTo = getCurrency(m_Error.getC_Currency_ID_To());
							result = Msg.translate(ctx, "ConversionRateOverlap") + ": "+
							"\n  " + Msg.translate(ctx, "ConversionRateOverlapCurrency") + ": "+currencyFrom.getISO_Code()+" - "+currencyTo.getISO_Code()+
							"\n  " + Msg.translate(ctx, "ConversionRateOverlapValid") + ": "+formatDate(m_Error.getValidFrom(), "dd/MM/yyyy")+" - "+formatDate(m_Error.getValidTo(), "dd/MM/yyyy");
						}
						rs.close();
					}
		    		pstmt.close();
				}
				catch (SQLException ex)
				{
					log.log(Level.SEVERE, "ERROR: ", ex);
					result = ex.getMessage();
				}
			}
		}
		return result;
	}

	@Override
	public String docValidate(PO po, int timing) {
		return null;
	}
	
	private MCurrency getCurrency(int c_currency_id) {
		return new Query(ctx, X_C_Currency.Table_Name , "c_currency_id=?", trxName)
		  .setParameters(c_currency_id)
		  .firstOnly();
	}
	
	private String formatDate(Date fec, String format) {
        if (fec == null)
            return "";
        SimpleDateFormat dFormat = new SimpleDateFormat(format);
        return dFormat.format(fec);
    }
}
