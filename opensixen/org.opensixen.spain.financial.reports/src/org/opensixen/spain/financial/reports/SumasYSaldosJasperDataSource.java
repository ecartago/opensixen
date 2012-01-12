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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 * 
 * SumasYSaldosJasperDataSource 
 *
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 */
public class SumasYSaldosJasperDataSource implements JRDataSource {

	/** Cuenta inicio				*/
	private int			p_1_ElementValue_ID = 0;
	/** Cuenta Fin			*/
	private int			p_2_ElementValue_ID = 0;
	
	/** Date Acct From			*/
	private Timestamp	p_DateAcct_From = null;
	/** Date Acct To			*/
	private Timestamp	p_DateAcct_To = null;
	
	/** Esqueme contable		*/
	private int p_C_AcctSchema_ID = 0;
	
	private boolean		groupAccounts = false;
	
	/**	Properties				*/
	private Properties p_ctx = null;
	
	/** Trx Name	*/
	private String trxName;
	
	/** Log	*/
	protected CLogger log = CLogger.getCLogger( getClass());
	
	
	/** Lineas del informe		*/
	private M_SumasYSaldos[] m_reportLines;
		
	/** Registro Actual */
	private int m_currentRecord = -1; // -1 porque lo primero que se hace es un ++
	
	/** Saldos de las cuentas	*/
	HashMap<String, M_SumasYSaldos> m_saldosIniciales;
	HashMap<String, M_SaldoAcumulado> m_saldosAcumulados;
	HashMap<String, M_SumasYSaldos> m_saldosCuentas;
	
	private int p_C_BPartner_ID;
	private int p_M_Product_ID;
		
	public SumasYSaldosJasperDataSource (Properties ctx, ProcessInfoParameter[] para)	{
		this.p_ctx = ctx;
		p_C_AcctSchema_ID = Env.getContextAsInt(ctx, "$C_AcctSchema_ID");
		
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null) {
				;
			}
			else if (name.equals("C_ElementValue_ID")) {
				p_1_ElementValue_ID = para[i].getParameterAsInt();
				p_2_ElementValue_ID = para[i].getParameter_ToAsInt();
			}
			else if (name.equals("DateAcct")) {
				p_DateAcct_From = (Timestamp)para[i].getParameter();
				p_DateAcct_To = (Timestamp)para[i].getParameter_To();
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
	
		
	private String  getSQLData(boolean saldosIniciales)	{
		return getSQLData(saldosIniciales, false);
	}
	
		
	/**
	 * Obtiene la SQL para obtener los saldos de las cuentas, acumulados, o entre fechas. 
	 * @param saldosIniciales Si es true, devuelve al sql para los saldos acumulados
	 * @return
	 */
	private String  getSQLData(boolean saldosIniciales, boolean saldosAcumulados)	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		
		if (groupAccounts)	{
			sql.append(" substr(ev.name, 0,4) as name ");
		}
		else {
			sql.append(" fa.account_id, ev.name, ev.AccountType, ev.AccountSign ");
		}
		sql.append(" ,sum(fa.amtacctcr)  as haber, sum(fa.amtacctdr) as debe FROM C_ElementValue ev, Fact_acct fa");

		// Añadimos la linea de GLJournal si el AD_Table_ID corresponde con esa tabla.
		sql.append(" left join GL_Journal jo  on ( fa.ad_table_id=224 and fa.record_id=jo.gl_journal_id) ");
		sql.append(" left join GL_JournalBatch gl  on ( fa.ad_table_id=224 and fa.record_id=jo.gl_journal_id and jo.gl_journalbatch_id=gl.gl_journalbatch_id) ");
		
		sql.append(" WHERE fa.account_id=ev.c_elementvalue_id ");
        
		// Añadimos restricciones
		sql.append( " AND fa.AD_Client_ID=").append(Env.getAD_Client_ID(p_ctx));
		sql.append( " AND  fa.C_AcctSchema_ID=").append(p_C_AcctSchema_ID);
		

		// Si queremos saber los saldos acumulados
		if (saldosAcumulados)	{

			// Los saldos se leen desde el dia 1 de enero del año dado como de inicio.
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(p_DateAcct_From.getTime());
						
			cal.set(Calendar.DAY_OF_YEAR, 1);
			Timestamp beginSaldos = new Timestamp(cal.getTimeInMillis());
			
			// Hasta la fecha anterior a la de inicio del informe
			cal.setTimeInMillis(p_DateAcct_From.getTime());			
			cal.add(Calendar.DATE, -1);
			Timestamp endSaldos = new Timestamp(cal.getTimeInMillis());
			
			
			// Si la fecha de inicio del reporte es la misma que la de inicio de los saldos acumulados,
			// Ignoramos 
			if (p_DateAcct_From.equals(beginSaldos))	{
				return null;
			}
			
			sql.append(" AND fa.DateAcct BETWEEN ").append(DB.TO_DATE(beginSaldos)).append(" AND ").append(DB.TO_DATE(endSaldos));									
			sql.append(" and ev.accountsign='N' and ev.accounttype='A' ");
		}
		else {
			sql.append(" AND fa.DateAcct BETWEEN ").append(DB.TO_DATE(p_DateAcct_From)).append(" AND ").append(DB.TO_DATE(p_DateAcct_To));
		}

		// Si solo queremos saldos iniciales, mostraremos solo los del asiento de apertura 
		if (saldosIniciales)	{
			sql.append(" and gl.journaltype='O' ");
		}
		else {
			sql.append(" and (gl.journaltype != 'O' or gl.journaltype is null) ");
		}					
		
		// Obtenemos la clausula de las cuentas basandonos en el nombre.
		if (p_1_ElementValue_ID > 0  && p_2_ElementValue_ID > 0)	{

			String ev1_rest = "(select name from c_elementvalue where c_elementvalue_id="+ p_1_ElementValue_ID+")";			
			String ev2_rest = "(select name from c_elementvalue where c_elementvalue_id="+ p_2_ElementValue_ID+")";
			
			sql.append(" AND ev.Name BETWEEN ").append(ev1_rest).append( " AND ").append(ev2_rest);
		}
		else if (p_1_ElementValue_ID > 0)	{
			sql.append("AND C_ElementValue_ID = ").append(p_1_ElementValue_ID);
		}
		if (p_C_BPartner_ID > 0)	{
			sql.append("AND C_BPartner_ID = ").append(p_C_BPartner_ID);	
		}
		
		if (p_M_Product_ID > 0)	{
			sql.append("AND M_Product_ID = ").append(p_M_Product_ID);
		}
		
		if (groupAccounts)	{
			//sql.append(" GROUP BY rollup(substr(ev.name,0,4)) ORDER BY name ");
			sql.append(" GROUP BY substr(ev.name,0,4) ORDER BY name ");
		}
		else {
			sql.append(" GROUP BY fa.account_id, ev.name, ev.AccountType, ev.AccountSign ORDER BY ev.Name");
		}

		log.info(sql.toString());
		return  sql.toString();
	}
	
	
	private  void loadSaldosIniciales()	{
		m_saldosIniciales = new HashMap<String, M_SumasYSaldos>();
		
		try	{
			String sql = getSQLData(true);
			if (sql == null)	{
				return;
			}
			PreparedStatement pstmt = DB.prepareStatement(sql, trxName);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())	{
				M_SumasYSaldos line = new M_SumasYSaldos(rs.getString("Name"));
				
				BigDecimal debe = rs.getBigDecimal("Debe");
				BigDecimal haber = rs.getBigDecimal("Haber");
				BigDecimal saldo = debe.subtract(haber);
				line.setSaldoInicial(saldo);
				
				m_saldosIniciales.put(rs.getString("Name"), line);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)	{
			log.log(Level.SEVERE, "No se pueden cargar los saldos." + e.toString());
		}
	}
	
	
	
	private  void loadSaldosAcumulados()	{
		m_saldosAcumulados = new HashMap<String, M_SaldoAcumulado>();
		
		try	{
			String sql = getSQLData(false, true);
			
			if (sql == null)	{
				return;
			}
			
			
			PreparedStatement pstmt = DB.prepareStatement(sql, trxName);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())	{
			M_SaldoAcumulado line = new M_SaldoAcumulado(rs);
				
				line.setDebe(rs.getBigDecimal("Debe"));				
				line.setHaber(rs.getBigDecimal("Haber"));
				line.setAccountSign(rs.getString("AccountSign"));
				line.setAccountType(rs.getString("AccountType"));
				
				m_saldosAcumulados.put(rs.getString("Name"), line);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)	{
			log.severe("No se pueden cargar los saldos." + e.toString());
		}
	}
	
	
	private void loadSaldosCuentas()	{
		m_saldosCuentas = new HashMap<String, M_SumasYSaldos>();
		try {
			String sql = getSQLData(false);
			if (sql == null)	{
				return;
			}
			PreparedStatement pstmt = DB.prepareStatement(sql, trxName);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next())	{
				M_SumasYSaldos line = new M_SumasYSaldos(rs);
				
				/*
				// Si habiamos guardado una linea con los saldos iniciales
				if (m_saldosIniciales.containsKey(line.getName()))	{
					M_SumasYSaldos saldoInicial = (M_SumasYSaldos)m_saldosIniciales.get(line.getName());
					line.setSaldoInicial(saldoInicial.getSaldoInicial());
				}
				
				list.add(line);
				*/
				m_saldosCuentas.put(line.getName(), line);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
					
		}
		catch (SQLException e)	{
			throw new RuntimeException("No se puede ejecutar la consulta para crear las lineas del informe.");
		}	
	}
	
	
	public void loadData() throws RuntimeException {
		
		// Cargamos los saldos de las cuentas
		loadSaldosIniciales();
		loadSaldosAcumulados();
		loadSaldosCuentas();
		
		// ArrayList donde guardaremos los datos del informe
		ArrayList<M_SumasYSaldos> list = new ArrayList<M_SumasYSaldos>();
		StringBuffer sql = new StringBuffer ( "SELECT  fa.account_id, ev.name, ev.value FROM  C_ElementValue ev ,Fact_acct fa WHERE fa.account_id=ev.c_elementvalue_id  ");
		// Añadimos restricciones
		sql.append( " AND fa.AD_Client_ID=").append(Env.getAD_Client_ID(p_ctx));
		sql.append( " AND  fa.C_AcctSchema_ID=").append(p_C_AcctSchema_ID);
		sql.append(" AND fa.DateAcct BETWEEN ").append(DB.TO_DATE(p_DateAcct_From)).append(" AND ").append(DB.TO_DATE(p_DateAcct_To));
		
		sql.append(" GROUP BY fa.account_id, ev.name, ev.value ORDER BY ev.value");
		log.info("SQL: " + sql.toString());
		try {
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), trxName);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next())	{
				M_SumasYSaldos line = new M_SumasYSaldos(rs.getString("name"));
				line.setValue(rs.getString("value"));
				
			
				// Si habiamos guardado una linea con los saldos iniciales
				if (m_saldosIniciales.containsKey(line.getName()))	{
					M_SumasYSaldos saldoInicial = m_saldosIniciales.get(line.getName());
					line.setSaldoInicial(saldoInicial.getSaldoInicial());
				}
				
				// Si habiamos guardado una linea con los saldos acumulados
				if (m_saldosAcumulados.containsKey(line.getName()))	{
					M_SaldoAcumulado saldoAcumulado = m_saldosAcumulados.get(line.getName());
					line.setSaldoAcumulado(saldoAcumulado);
				}
				
				
				// Si tiene saldo
				if (m_saldosCuentas.containsKey(line.getName()))	{
					M_SumasYSaldos saldoCuenta = m_saldosCuentas.get(line.getName());
					line.setDebe(saldoCuenta.getDebe());
					line.setHaber(saldoCuenta.getHaber());
				}
				
				list.add(line);
				
			}
			
			rs.close();
			pstmt.close();
			pstmt = null;
					
		}
		catch (SQLException e)	{
			throw new RuntimeException("No se puede ejecutar la consulta para crear las lineas del informe.");
		}
			
		// Guardamos la lista en m_reportLines
		m_reportLines = new M_SumasYSaldos[list.size()];
		list.toArray(m_reportLines);
		
	}	
	
	
	@Override
	public Object getFieldValue(JRField jrf) throws JRException {		
		return getFieldValue(jrf.getName());		
	}
 
	public Object getFieldValue(String name) throws JRException {
		
		if (name.toUpperCase().equals("NAME"))	{
			return m_reportLines[m_currentRecord].getName();
		}
		if (name.toUpperCase().equals("VALUE"))	{
			return m_reportLines[m_currentRecord].getValue();
		}
		else if (name.toUpperCase().equals("DEBE"))	{
			return m_reportLines[m_currentRecord].getDebe();
		}
		else if (name.toUpperCase().equals("HABER"))	{
			return m_reportLines[m_currentRecord].getHaber();
		}
		else if (name.toUpperCase().equals("SALDO"))	{
			return m_reportLines[m_currentRecord].getSaldo();
		}
		else if (name.toUpperCase().equals("SALDOINICIAL"))	{
			return m_reportLines[m_currentRecord].getSaldoInicial();
		}
		else if (name.toUpperCase().equals("GROUPNAME3"))	{
			return m_reportLines[m_currentRecord].getGroupName3();
		}
		else if (name.toUpperCase().equals("GROUPNAME4"))	{
			return m_reportLines[m_currentRecord].getGroupName4();
		}		
		else {
			throw new JRException("No se ha podidod obtener el valor de la columna " + name);
		}
	}
 
	/**
	 * Salta al siguiente registro del DataSource
	 */
	@Override
	public boolean next() throws JRException {
		m_currentRecord++;
		
		if (m_currentRecord >= m_reportLines.length )	{
			return false;
		}
		
		return true;
	}

}

class M_SumasYSaldos	{
	
	private String Name;
	private String Value;
	private BigDecimal Debe = Env.ZERO;
	private BigDecimal Haber = Env.ZERO;
	private BigDecimal SaldoInicial = Env.ZERO;
	
	private M_SaldoAcumulado SaldoAcumulado;
	
	private String AccountType;
	private String AccountSign;
	

	public M_SumasYSaldos (ResultSet rs)	{
		try {
			Name = rs.getString("Name");
			Debe = rs.getBigDecimal("Debe");
			Haber = rs.getBigDecimal("Haber");
			AccountSign = rs.getString("AccountSign");
			AccountType = rs.getString("AccountType");
		}
		catch (SQLException e)	{
			
		}	
	}
	
	public M_SumasYSaldos (String name)	{
		Name = name;
	}
	
	/**
	 * Devuelve el saldo contandolo como saldoInicial + (debe - haber)
	 * o saldoInicial + (haber - debe)
	 * @return
	 */
	public BigDecimal getSaldo()	{
		
		BigDecimal saldo = getDebe().subtract(getHaber());
					
		if (mustSumAcumulated())	{
			saldo = saldo.add(getSaldoInicial());
			return saldo;
		}
		else {
			return saldo;
		}
	}
	

	public BigDecimal getDebe() {				
		if (SaldoAcumulado != null && mustSumAcumulated())	{
			return Debe.add(SaldoAcumulado.getDebe());
		}
		else {
			return Debe;
		}
	}
	
	
	
	public void setDebe(BigDecimal debe) {
		Debe = debe;
	}
	
	
	public BigDecimal getHaber() {		
		if (SaldoAcumulado != null && mustSumAcumulated())	{
			return Haber.add(SaldoAcumulado.getHaber());
		}
		else {
			return Haber;
		}
	}
	
	
	public void setHaber(BigDecimal haber) {
		Haber = haber;
	}
	
	
	public String getName() {
	
		return Name;
	}
	
	
	public void setName(String name) {
		Name = name;
	}
	
	
	public BigDecimal getSaldoInicial() {
		return SaldoInicial;
	}
	public void setSaldoInicial(BigDecimal saldoInicial) {
		SaldoInicial = saldoInicial;
	}

	/**
	 * @return the saldoAcumulado
	 */
	public M_SaldoAcumulado getSaldoAcumulado() {
		return SaldoAcumulado;
	}



	/**
	 * @param saldoAcumulado the saldoAcumulado to set
	 */
	public void setSaldoAcumulado(M_SaldoAcumulado saldoAcumulado) {
		SaldoAcumulado = saldoAcumulado;
	}

	/**
	 * @return the accountType
	 */
	public String getAccountType() {
		return AccountType;
	}



	/**
	 * @param accountType the accountType to set
	 */
	public void setAccountType(String accountType) {
		AccountType = accountType;
	}



	/**
	 * @return the accountSign
	 */
	public String getAccountSign() {
		return AccountSign;
	}



	/**
	 * @param accountSign the accountSign to set
	 */
	public void setAccountSign(String accountSign) {
		AccountSign = accountSign;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return Value;
	}



	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		Value = value;
	}
	
	public String getGroupName3()	{
		return Value.substring(0, 3);
	}
	
	public String getGroupName4()	{
		return Value.substring(0, 4);
	}
	
	
	public boolean mustSumAcumulated()	{
		if (getAccountSign() == "N")	{

			//if (getAccountType() != "A" || getAccountType() != "E")	{
			if (getAccountType() != "A")	{
				return false;
			}
		}
		return true;
	}
	
	
}

class M_SaldoAcumulado extends M_SumasYSaldos	{

	public M_SaldoAcumulado(ResultSet rs) {
		super(rs);
	}
	
	@Override
	public boolean mustSumAcumulated() {
		return false;
	}
	
}



