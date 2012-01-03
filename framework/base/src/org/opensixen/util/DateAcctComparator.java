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
package org.opensixen.util;

import java.sql.Timestamp;
import java.util.Comparator;

import org.compiere.model.MBankStatement;
import org.compiere.model.MInventory;
import org.compiere.model.MMovement;
import org.compiere.model.PO;
import org.compiere.model.X_M_Production;

/**
 * DateAcctComparator 
 *
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 */

public class DateAcctComparator implements Comparator<PO> {
    @Override
    public int compare(PO o1, PO o2) {
        Timestamp o1Date = getDate(o1);
        Timestamp o2Date = getDate(o2);
    	return o1Date.compareTo(o2Date);
    }
    
    private Integer[] movementDateTables = {MInventory.Table_ID, MMovement.Table_ID, X_M_Production.Table_ID};
    
    private Timestamp getDate(PO po)	{
    	int table_ID = po.get_Table_ID();
    	for (int special_ID:movementDateTables)		{
    		if (table_ID == special_ID) {
    			Timestamp date = (Timestamp) po.get_Value(MInventory.COLUMNNAME_MovementDate);
    			if (date == null) {
    				throw new RuntimeException ("Date null in " + po.get_TableName());
    			}
    			return date;
    		}
    	}    			    	
    	Timestamp date = (Timestamp) po.get_Value("DateAcct");
    	
    	if (table_ID == MBankStatement.Table_ID) {
    		return (Timestamp) po.get_Value(MBankStatement.COLUMNNAME_StatementDate);
    	}
    	
    	if (date == null) {
			throw new RuntimeException ("Date null in " + po.get_TableName());
		}
		return date;
    }
}
