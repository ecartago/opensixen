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
package org.opensixen.swing;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Properties;

import javax.swing.JTable;

import net.miginfocom.swing.MigLayout;

import org.compiere.apps.AEnv;
import org.compiere.swing.CButton;
import org.compiere.swing.CPanel;
import org.compiere.swing.CScrollPane;
import org.compiere.util.Msg;
import org.opensixen.model.MVFactAcct;
import org.opensixen.model.QParam;

/**
 * AccountDetailViewerPanel 
 *
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 */
public class AccountDetailViewerPanel extends CPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private Properties ctx;
	private OTable table;
	private AccountDetailTableModel tableModel;
	private CButton btnZoomDoc= new CButton();
	private CButton btnZoomFact= new CButton();
	private MVFactAcct selectedFact;

	public AccountDetailViewerPanel(Properties ctx)	{
		super();
		this.ctx = ctx;
		jbInit();
	}
	
	private void jbInit()	{
		// Create main table
		setLayout(new MigLayout("", "[grow]", "[shrink 0]"));
		
		CPanel btnPanel = new CPanel();
		
		btnZoomDoc = new CButton(Msg.translate(ctx, "Open document"));		
		btnZoomDoc.addActionListener(this);
		btnZoomFact = new CButton(Msg.translate(ctx, "Open fact"));
		btnZoomFact.addActionListener(this);
		
		// Disabled by default
		btnZoomDoc.setEnabled(false);
		btnZoomFact.setEnabled(false);
		
		btnPanel.add(btnZoomDoc);
		btnPanel.add(btnZoomFact);
		add(btnPanel, "wrap");
		
		table = new OTable(ctx);
		tableModel = new AccountDetailTableModel(ctx);
		table.setModel(tableModel);
		table.setupTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setFillsViewportHeight(true);		
		table.packAll();
		table.addMouseListener(new MouseListener() {			
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}			
			@Override
			public void mouseExited(MouseEvent e) {}			
			@Override
			public void mouseEntered(MouseEvent e) {}			
			@Override
			public void mouseClicked(MouseEvent e) {				
					int index = table.rowAtPoint(e.getPoint());
					MVFactAcct fact = (MVFactAcct) tableModel.getValueAt(index);
					setSelectedFact(fact);
			}
		});
		
		add(new CScrollPane(table), "wrap, growx");		
	}
			
	/**
	 * Setup params for table viewer
	 * @param params
	 */
	public void setParams(QParam[] params)	{
		setSelectedFact(null);
		tableModel.setParams(params);
		tableModel.reload();
		table.packAll();
	}

	/**
	 * Set selectedFact and enable buttons
	 * @param fact
	 */
	private void setSelectedFact(MVFactAcct fact)	{
		selectedFact = fact;
		boolean status = fact != null;				
		btnZoomDoc.setEnabled(status);
		btnZoomFact.setEnabled(status);
	}
	
	/**
	 * Open a new windows with the doc
	 *  for the fact
	 */
	private void zoomDoc()	{
		AEnv.zoom(selectedFact.getAD_Table_ID(), selectedFact.getRecord_ID());
	}
	
	/**
	 * Open a new windows with the Accouont Viewer
	 */
	private void zoomFact()	{
		AccountViewer viewer = new AccountViewer();
		viewer.view(selectedFact.getAD_Client_ID(), selectedFact.getAD_Table_ID(), selectedFact.getRecord_ID());
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(btnZoomFact))	{
			zoomFact();
		}
		else if (e.getSource().equals(btnZoomDoc))	{
			zoomDoc();
		}
		
	}
	
}
