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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.compiere.apps.ConfirmPanel;
import org.compiere.model.MTable;
import org.compiere.swing.CDialog;
import org.compiere.swing.CLabel;
import org.compiere.swing.CPanel;
import org.compiere.util.CLogger;
import org.compiere.util.Msg;
import org.opensixen.model.MRecordNote;


/**
 * ARecordNote 
 *
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 */
public class ARecordNote extends CDialog {

	private CLogger log = CLogger.getCLogger(getClass());
	
	public static String EKIT_OSX_TOOLBAR = "OP|SV|PR|SP|CT|CP|PS|SP|UN|RE|SP|FN|SP|UC|UM|SP|SR|*|BL|IT|UD|SP|SK|SU|SB|SP|AL|AC|AR|AJ|SP|UL|OL|SP|LK|*|ST|SP|FO";
	private int AD_Table_ID;
	private int Record_ID;
	
	private MRecordNote note;
	
	private JTextArea theText;
	
	private Properties ctx;
	private ConfirmPanel confirmPanelSel = new ConfirmPanel(true);
	
	public ARecordNote(Properties ctx, Frame frame,MRecordNote note, int AD_Table_ID, int Record_ID) throws HeadlessException {
		super(frame, Msg.getMsg(ctx, "Record Note"), true);
		this.ctx = ctx;
		this.note = note;
		this.AD_Table_ID = AD_Table_ID;
		this.Record_ID = Record_ID;
	
		jbInit();
	}

	
	public void jbInit()	{
		CPanel panel = new CPanel();
		this.getContentPane().add(panel);
		
		panel.setLayout(new BorderLayout(10, 10));

		CPanel header = new CPanel();
		header.setLayout(new FlowLayout());
		panel.add(header, BorderLayout.NORTH);
		
		MTable table = MTable.get(ctx, AD_Table_ID);
		CLabel info = new CLabel(table.getTableName() + ": " + Record_ID);
		header.add(info);
		
		CPanel editorPanel = new CPanel();
		editorPanel.setLayout(new BorderLayout());

		theText = new JTextArea(5, 20);
		JScrollPane scrollPane = new JScrollPane(theText);
		editorPanel.add(scrollPane, BorderLayout.CENTER);

		panel.add(editorPanel, BorderLayout.CENTER);
		
		// Load Msg
		if (note != null)	{
			theText.setText(note.getTextMsg());
		}
				
		CPanel btnPanel = new CPanel();
		panel.add(btnPanel, BorderLayout.SOUTH);
		btnPanel.setLayout(new FlowLayout());
		btnPanel.add(confirmPanelSel, BorderLayout.SOUTH);
		confirmPanelSel.addActionListener(this);
	}

	
	/**
	 *	Action Listener
	 *  @param e event
	 */
	public void actionPerformed (ActionEvent e)
	{
		log.info("Cmd=" + e.getActionCommand());
		//
		if (e.getActionCommand().equals(ConfirmPanel.A_CANCEL))
		{
			dispose();
			return;
		}
		else {  //Button OK pressed
			save();
			dispose();
		}
	}	//	actionPerformed
	
	
	
	
	private boolean save()	{
		boolean bSavedOK = true;
		
		if (theText.getText() != null) {
			String trimmedText = theText.getText().trim();
			if ("".equals(trimmedText)) {
				if (note != null)
					note.delete(false);
			}
			else {
				if (note == null) {
					note = new MRecordNote(ctx, 0, null);
					note.setAD_Table_ID(AD_Table_ID);
					note.setRecord_ID(Record_ID);
				}
				note.setTextMsg(trimmedText);
				bSavedOK = note.save();
			}
		}
		return bSavedOK;
	}
	
	public MRecordNote getRecordNote()	{
		return note;
	}
	
}
