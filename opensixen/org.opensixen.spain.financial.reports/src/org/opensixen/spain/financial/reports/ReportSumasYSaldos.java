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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.Env;
import org.opensixen.model.ColumnDefinition;
import org.opensixen.osgi.interfaces.ICommand;
import org.opensixen.report.AbstractDynamicReport;

/**
 * 
 * ReportSumasYSaldos 
 *
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 */
public class ReportSumasYSaldos extends AbstractDynamicReport {

	
	private Timestamp p_DateFrom;
	private Timestamp p_DateTo;
	private int p_C_BPartner_ID;
	private int p_M_Product_ID;
	private int p_C_ElementFrom_ID;
	private int p_C_ElementTo_ID;
	private ProcessInfoParameter[] parameters;


	/**
	 * @param ctx
	 */
	protected ReportSumasYSaldos(Properties ctx) {
		super(ctx);
	}
	
	public ReportSumasYSaldos() {
		super(Env.getCtx());
	}

	/* (non-Javadoc)
	 * @see org.opensixen.report.AbstractDynamicJasperReport#getColumns()
	 */
	@Override
	protected ColumnDefinition[] getColumns() {
		ColumnDefinition[] cols = { new ColumnDefinition("value", "Cuenta", String.class, 80),
								new ColumnDefinition("name", "", String.class, 240),
								new ColumnDefinition("saldoinicial", "Inicial", BigDecimal.class, 90),								
								new ColumnDefinition("debe", "Debe", BigDecimal.class, 90),
								new ColumnDefinition("haber", "Haber", BigDecimal.class, 90),
								new ColumnDefinition("saldo", "saldo", BigDecimal.class, 90)
		};
		
		return cols;
		
	}

	/* (non-Javadoc)
	 * @see org.opensixen.report.AbstractDynamicJasperReport#getTitle()
	 */
	@Override
	public String getTitle() {
		return "Sumas y Saldos";
	}


	/* (non-Javadoc)
	 * @see org.opensixen.report.AbstractDynamicJasperReport#getDataSource()
	 */
	@Override
	protected JRDataSource getDataSource() {
		SumasYSaldosJasperDataSource ds = new SumasYSaldosJasperDataSource(Env.getCtx(), parameters);
		ds.loadData();
		return ds;
		
	}

	/* (non-Javadoc)
	 * @see org.opensixen.osgi.interfaces.ICommand#prepare()
	 */
		
	public void prepare(ProcessInfoParameter[] para) {
		
		parameters = para;
		
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
