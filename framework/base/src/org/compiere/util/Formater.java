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
package org.compiere.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MCurrency;
import org.compiere.model.MLocation;

/**
 * Formater 
 *
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 */
public class Formater {

	private static Locale s_locale = new Locale(Env.getAD_Language(Env.getCtx())); 
	
	private static HashMap<Integer, MCurrency> s_cacheCurrency = new HashMap<Integer, MCurrency>();
	
	public static String formatQty(BigDecimal qty) {		
		return formatQty(s_locale, qty);
	}
	
	public static String formatQty(Locale locale, BigDecimal qty) {
		NumberFormat formater = NumberFormat.getInstance(locale);
		String str = formater.format(qty);
		return str;
	}
	
	public static String formatAmt(BigDecimal amount, String isoCode) {
		return formatAmt(s_locale, amount, isoCode);
	}
	
	public static String formatAmt(BigDecimal amount, int C_Currency_ID) {
		String isoCode = null;
		if (s_cacheCurrency.containsKey(C_Currency_ID))	{
			isoCode = s_cacheCurrency.get(C_Currency_ID).getISO_Code();
		}
		else {
			MCurrency currency = MCurrency.get(Env.getCtx(), C_Currency_ID);
			s_cacheCurrency.put(C_Currency_ID, currency);
			isoCode = currency.getISO_Code();
		}
		return formatAmt(s_locale, amount, isoCode);
	}
	
	public static String formatAmt(BigDecimal amount) {
		return formatAmt(s_locale, amount, null);
	}
	
	public static String formatAmt(Locale locale, BigDecimal amount, String isoCode) {
		NumberFormat formater = NumberFormat.getCurrencyInstance();
		if (isoCode != null)	{
			Currency currency = Currency.getInstance(isoCode);		
			formater.setCurrency(currency);
		}
		return formater.format(amount);				
	}
	
	public static String formatDate(Timestamp date) {
		return formatDate(s_locale, date);
	}

	
	public static String formatDate(Locale locale, Timestamp date) {
		DateFormat df = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, locale);
		String str = df.format(new Date(date.getTime()));
		return str;
	}
	
	public static String formatLoc(Integer C_BPartner_Location_ID) {
		MBPartnerLocation bplocation = new MBPartnerLocation(Env.getCtx(), C_BPartner_Location_ID, null);
		MLocation location = bplocation.getLocation(false);
		StringBuffer address = new StringBuffer();
		address.append(location.toStringCR());
		String country = location.getCountry(true);
		if (country != null)	{
			address.append("\n").append(country);
		}
		return address.toString();
	}

	
	public static String formatLocation(int C_BPartner_ID, int C_Bpartner_Location_ID,
			boolean addTaxID) {
		StringBuffer address = new StringBuffer();
		MBPartner bp = new MBPartner(Env.getCtx(), C_BPartner_ID, null);
		address.append("<b>").append(bp.getName()).append("</b>\n");
		if (addTaxID && bp.getTaxID() != null)	{
			address.append(bp.getTaxID()).append("\n");
		}
		address.append(formatLoc(C_Bpartner_Location_ID));
		String str = address.toString();
		return str.replaceAll("\\n", "<br/>");		
	}

}
