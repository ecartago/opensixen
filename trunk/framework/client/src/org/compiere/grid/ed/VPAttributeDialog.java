/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.grid.ed;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.apps.ADialog;
import org.compiere.apps.AEnv;
import org.compiere.apps.ALayout;
import org.compiere.apps.ALayoutConstraint;
import org.compiere.apps.ConfirmPanel;
import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MAttributeValue;
import org.compiere.model.MRole;
import org.compiere.model.MSerNoCtl;
import org.compiere.swing.CButton;
import org.compiere.swing.CComboBox;
import org.compiere.swing.CDialog;
import org.compiere.swing.CEditor;
import org.compiere.swing.CLabel;
import org.compiere.swing.CPanel;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.opensixen.osgi.Service;
import org.opensixen.osgi.interfaces.IVPAttributeDialog;

/**
 *  Product Attribute Set Product/Instance Dialog Editor.
 * 	Called from VPAttribute.actionPerformed
 *
 *  @author Jorg Janke
 *  @version $Id: VPAttributeDialog.java,v 1.4 2006/07/30 00:51:27 jjanke Exp $
 */
public class VPAttributeDialog extends CDialog
	implements ActionListener, IVPAttributeDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1062346984681892620L;

	/**
	 *	Product Attribute Instance Dialog
	 *	@param frame parent frame
	 *	@param M_AttributeSetInstance_ID Product Attribute Set Instance id
	 * 	@param M_Product_ID Product id
	 * 	@param C_BPartner_ID b partner
	 * 	@param productWindow this is the product window (define Product Instance)
	 * 	@param AD_Column_ID column
	 * 	@param WindowNo window
	 */
	protected VPAttributeDialog (Frame frame, int M_AttributeSetInstance_ID, 
		int M_Product_ID, int C_BPartner_ID, 
		boolean productWindow, int AD_Column_ID, int WindowNo)
	{
		super (frame, Msg.translate(Env.getCtx(), "M_AttributeSetInstance_ID") , true);
		init(frame, M_AttributeSetInstance_ID, M_Product_ID, C_BPartner_ID, productWindow, AD_Column_ID, WindowNo);
		
	}	//	VPAttributeDialog

	public void init(Frame frame, int M_AttributeSetInstance_ID, 
			int M_Product_ID, int C_BPartner_ID, 
			boolean productWindow, int AD_Column_ID, int WindowNo)	{
		
		log.config("M_AttributeSetInstance_ID=" + M_AttributeSetInstance_ID 
				+ ", M_Product_ID=" + M_Product_ID
				+ ", C_BPartner_ID=" + C_BPartner_ID
				+ ", ProductW=" + productWindow + ", Column=" + AD_Column_ID);
			m_WindowNo = Env.createWindowNo (this);
			m_M_AttributeSetInstance_ID = M_AttributeSetInstance_ID;
			m_M_Product_ID = M_Product_ID;
			m_C_BPartner_ID = C_BPartner_ID;
			m_productWindow = productWindow;
			m_AD_Column_ID = AD_Column_ID;
			m_WindowNoParent = WindowNo;

			//get columnName from ad_column
	 	 	m_columnName = DB.getSQLValueString(null, "SELECT ColumnName FROM AD_Column WHERE AD_Column_ID = ?", m_AD_Column_ID);
	 	 	if (m_columnName == null || m_columnName.trim().length() == 0)
	 	 	{
	 	 		//fallback
	 	 		m_columnName = "M_AttributeSetInstance_ID";
	 	 	}
	 	 	
			try
			{
				jbInit();
			}
			catch(Exception ex)
			{
				log.log(Level.SEVERE, "VPAttributeDialog" + ex);
			}
			//	Dynamic Init
			if (!initAttributes ())
			{
				dispose();
				return;
			}

			//	Window usually to wide (??)
			Dimension dd = centerPanel.getPreferredSize();
			dd.width = Math.min(500, dd.width);
			centerPanel.setPreferredSize(dd);
			AEnv.showCenterWindow(frame, this);
	}
	
	
	private int						m_WindowNo;
	protected MAttributeSetInstance	m_masi;
	private int 					m_M_AttributeSetInstance_ID;
	private int 					m_M_Locator_ID;
	private String					m_M_AttributeSetInstanceName;
	private int 					m_M_Product_ID;
	private int						m_C_BPartner_ID;
	private int						m_AD_Column_ID;
	protected int					m_WindowNoParent;
	/**	Enter Product Attributes		*/
	protected boolean				m_productWindow = false;
	/**	Change							*/
	private boolean					m_changed = false;
	
	private CLogger					log = CLogger.getCLogger(getClass());
	/** Row Counter					*/
	protected int					m_row = 0;
	/** List of Editors				*/
	private ArrayList<CEditor>		m_editors = new ArrayList<CEditor>();
	/** Length of Instance value (40)	*/
	private static final int		INSTANCE_VALUE_LENGTH = 40;

	//	Lot
	private VString fieldLotString = new VString ("Lot", false, false, true, 20, 20, null, null);
	//	Ser No
	private VString fieldSerNo = new VString ("SerNo", false, false, true, 20, 20, null, null);
	private CButton bSerNo = new CButton(Msg.getMsg (Env.getCtx(), "New"));
	//	Date
	protected VDate fieldGuaranteeDate = new VDate ("GuaranteeDate", false, false, true, DisplayType.Date, Msg.getMsg(Env.getCtx(), "GuaranteeDate"));

	private BorderLayout mainLayout = new BorderLayout();
	protected CPanel centerPanel = new CPanel();
	private ALayout centerLayout = new ALayout(5,5, true);
	private ConfirmPanel confirmPanel = new ConfirmPanel (true);
	
	private String m_columnName = null;

	/**
	 *	Layout
	 * 	@throws Exception
	 */
	private void jbInit () throws Exception
	{
		this.getContentPane().setLayout(mainLayout);
		this.getContentPane().add(centerPanel, BorderLayout.CENTER);
		this.getContentPane().add(confirmPanel, BorderLayout.SOUTH);
		centerPanel.setLayout(centerLayout);
		//
		confirmPanel.addActionListener(this);
	}	//	jbInit

	/**
	 *	Dyanmic Init.
	 *  @return true if initialized
	 */
	protected boolean initAttributes ()
	{
		if (m_M_Product_ID == 0 && !m_productWindow)
			return false;
		
		MAttributeSet as = null;
		
		if (m_M_Product_ID != 0)
		{
			//	Get Model
			m_masi = MAttributeSetInstance.get(Env.getCtx(), m_M_AttributeSetInstance_ID, m_M_Product_ID);
			if (m_masi == null)
			{
				log.severe ("No Model for M_AttributeSetInstance_ID=" + m_M_AttributeSetInstance_ID + ", M_Product_ID=" + m_M_Product_ID);
				return false;
			}
			Env.setContext(Env.getCtx(), m_WindowNo, "M_AttributeSet_ID", m_masi.getM_AttributeSet_ID());
	
			//	Get Attribute Set
			as = m_masi.getMAttributeSet();
		}
		else 
		{
			int M_AttributeSet_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNoParent, "M_AttributeSet_ID");
			m_masi = new MAttributeSetInstance (Env.getCtx(), 0, M_AttributeSet_ID, null);
			as = m_masi.getMAttributeSet();
		}
		
		//	Product has no Attribute Set
		if (as == null)		
		{
			ADialog.error(m_WindowNo, this, "PAttributeNoAttributeSet");
			return false;
		}
		//	Product has no Instance Attributes
		if (!m_productWindow && !as.isInstanceAttribute())
		{
			ADialog.error(m_WindowNo, this, "PAttributeNoInstanceAttribute");
			return false;
		}

		//	Show Product Attributes
		if (m_productWindow)
		{
			MAttribute[] attributes = as.getMAttributes (false);
			log.fine ("Product Attributes=" + attributes.length);
			for (int i = 0; i < attributes.length; i++)
				addAttributeLine (attributes[i], true, !m_productWindow);
		}
		else	//	Set Instance Attributes
		{
			//	All Attributes
			MAttribute[] attributes = as.getMAttributes (true);
			log.fine ("Instance Attributes=" + attributes.length);
			for (int i = 0; i < attributes.length; i++)
				addAttributeLine (attributes[i], false, false);
		}

		//	Lot
		if (!m_productWindow && as.isLot())
		{
			CLabel label = new CLabel (Msg.translate(Env.getCtx(), "Lot"));
			label.setLabelFor (fieldLotString);
			centerPanel.add(label, new ALayoutConstraint(m_row++,0));
			centerPanel.add(fieldLotString, null);
			fieldLotString.setText (m_masi.getLot());
			fieldLotString.setReadWrite(false);
		}	//	Lot

		//	SerNo
		if (!m_productWindow && as.isSerNo())
		{
			CLabel label = new CLabel (Msg.translate(Env.getCtx(), "SerNo"));
			label.setLabelFor(fieldSerNo);
			fieldSerNo.setText(m_masi.getSerNo());
			centerPanel.add(label, new ALayoutConstraint(m_row++,0));
			centerPanel.add(fieldSerNo, null);
			//	New SerNo Button
			if (m_masi.getMAttributeSet().getM_SerNoCtl_ID() != 0)
			{
				if (MRole.getDefault().isTableAccess(MSerNoCtl.Table_ID, false)
					&& !m_masi.isExcludeSerNo(m_AD_Column_ID, Env.isSOTrx(Env.getCtx(), m_WindowNoParent)))
				{
					centerPanel.add(bSerNo, null);
					bSerNo.addActionListener(this);
				}
			}
		}	//	SerNo

		//	GuaranteeDate
		if (!m_productWindow && as.isGuaranteeDate())
		{
			CLabel label = new CLabel (Msg.translate(Env.getCtx(), "GuaranteeDate"));
			label.setLabelFor(fieldGuaranteeDate);
			if (m_M_AttributeSetInstance_ID == 0)
				fieldGuaranteeDate.setValue(m_masi.getGuaranteeDate(true));
			else
				fieldGuaranteeDate.setValue(m_masi.getGuaranteeDate());
			centerPanel.add(label, new ALayoutConstraint(m_row++,0));
			centerPanel.add(fieldGuaranteeDate, null);
		}	//	GuaranteeDate

		if (m_row == 0)
		{
			ADialog.error(m_WindowNo, this, "PAttributeNoInfo");
			return false;
		}

		return true;
	}	//	initAttribute

	/**
	 * 	Add Attribute Line
	 *	@param attribute attribute
	 * 	@param product product level attribute
	 * 	@param readOnly value is read only
	 */
	private void addAttributeLine (MAttribute attribute, boolean product, boolean readOnly)
	{
		log.fine(attribute + ", Product=" + product + ", R/O=" + readOnly);
		CLabel label = new CLabel (attribute.getName());
		if (product)
			label.setFont(new Font(label.getFont().getFontName(), Font.BOLD, label.getFont().getSize()));
		if (attribute.getDescription() != null)
			label.setToolTipText(attribute.getDescription());
		centerPanel.add(label, new ALayoutConstraint(m_row++,0));
		//
		MAttributeInstance instance = attribute.getMAttributeInstance (m_M_AttributeSetInstance_ID);
		if (MAttribute.ATTRIBUTEVALUETYPE_List.equals(attribute.getAttributeValueType()))
		{
			MAttributeValue[] values = attribute.getMAttributeValues();	//	optional = null
			CComboBox editor = new CComboBox(values);
			boolean found = false;
			if (instance != null)
			{
				for (int i = 0; i < values.length; i++)
				{
					if (values[i] != null && values[i].getM_AttributeValue_ID () == instance.getM_AttributeValue_ID ())
					{
						editor.setSelectedIndex (i);
						found = true;
						break;
					}
				}
				if (found)
					log.fine("Attribute=" + attribute.getName() + " #" + values.length + " - found: " + instance);
				else
					log.warning("Attribute=" + attribute.getName() + " #" + values.length + " - NOT found: " + instance);
			}	//	setComboBox
			else
				log.fine("Attribute=" + attribute.getName() + " #" + values.length + " no instance");
			label.setLabelFor(editor);
			centerPanel.add(editor, null);
			if (readOnly)
				editor.setEnabled(false);
			else
				m_editors.add (editor);
		}
		else if (MAttribute.ATTRIBUTEVALUETYPE_Number.equals(attribute.getAttributeValueType()))
		{
			VNumber editor = new VNumber(attribute.getName(), attribute.isMandatory(), 
				false, true, DisplayType.Number, attribute.getName());
			if (instance != null)
				editor.setValue(instance.getValueNumber());
			else
				editor.setValue(Env.ZERO);
			label.setLabelFor(editor);
			centerPanel.add(editor, null);
			if (readOnly)
				editor.setEnabled(false);
			else
				m_editors.add (editor);
		}
		else	//	Text Field
		{
			VString editor = new VString (attribute.getName(), attribute.isMandatory(), 
				false, true, 20, INSTANCE_VALUE_LENGTH, null, null);
			if (instance != null)
				editor.setText(instance.getValue());
			label.setLabelFor(editor);
			centerPanel.add(editor, null);
			if (readOnly)
				editor.setEnabled(false);
			else
				m_editors.add (editor);
		}
	}	//	addAttributeLine

	/* (non-Javadoc)
	 * @see org.compiere.grid.ed.IVPAttributeDialog#dispose()
	 */
	@Override
	public void dispose()
	{
		removeAll();
		Env.clearWinContext(m_WindowNo);
		//
		Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, m_columnName, 
			String.valueOf(m_M_AttributeSetInstance_ID));
		Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "M_Locator_ID", 
			String.valueOf(m_M_Locator_ID));
		//
		super.dispose();
	}	//	dispose

	/* (non-Javadoc)
	 * @see org.compiere.grid.ed.IVPAttributeDialog#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		//	Create New SerNo
		if (e.getSource() == bSerNo)
		{
			fieldSerNo.setText(m_masi.getSerNo(true));
		}
		
		//	OK
		else if (e.getActionCommand().equals(ConfirmPanel.A_OK))
		{
			if (saveSelection())
				dispose();
		}
		//	Cancel
		else if (e.getActionCommand().equals(ConfirmPanel.A_CANCEL))
		{
			m_changed = false;
			m_M_AttributeSetInstance_ID = 0;
			m_M_Locator_ID = 0;
			dispose();
		}
		else
			log.log(Level.SEVERE, "not found - " + e);
	}	//	actionPerformed

	/**
	 *	Save Selection
	 *	@return true if saved
	 */
	protected boolean saveSelection()
	{
		log.info("");
		KeyNamePair pp = m_masi.createLot(m_M_Product_ID);
		if (pp != null)
		{
			fieldLotString.setText (pp.getName());
		}
		
		MAttributeSet as = m_masi.getMAttributeSet();
		if (as == null)
			return true;
		//
		m_changed = false;
		String mandatory = "";
		if (!m_productWindow && as.isLot())
		{
			log.fine("Lot=" + fieldLotString.getText ());
			String text = fieldLotString.getText();
			m_masi.setLot (text);
			if (as.isLotMandatory() && (text == null || text.length() == 0))
				mandatory += " - " + Msg.translate(Env.getCtx(), "Lot");
			m_changed = true;
		}	//	Lot
		if (!m_productWindow && as.isSerNo())
		{
			log.fine("SerNo=" + fieldSerNo.getText());
			String text = fieldSerNo.getText();
			m_masi.setSerNo(text);
			if (as.isSerNoMandatory() && (text == null || text.length() == 0))
				mandatory += " - " + Msg.translate(Env.getCtx(), "SerNo");
			m_changed = true;
		}	//	SerNo
		if (!m_productWindow && as.isGuaranteeDate())
		{
			log.fine("GuaranteeDate=" + fieldGuaranteeDate.getValue());
			Timestamp ts = (Timestamp)fieldGuaranteeDate.getValue();
			m_masi.setGuaranteeDate(ts);
			if (as.isGuaranteeDateMandatory() && ts == null)
				mandatory += " - " + Msg.translate(Env.getCtx(), "GuaranteeDate");
			m_changed = true;
		}	//	GuaranteeDate

		//	***	Save Attributes ***
		//	New Instance
		if (m_changed || m_masi.getM_AttributeSetInstance_ID() == 0)
		{
			m_masi.save ();
			m_M_AttributeSetInstance_ID = m_masi.getM_AttributeSetInstance_ID ();
			m_M_AttributeSetInstanceName = m_masi.getLot();
		}

		//	Save Instance Attributes
		MAttribute[] attributes = as.getMAttributes(!m_productWindow);
		for (int i = 0; i < attributes.length; i++)
		{
			if (MAttribute.ATTRIBUTEVALUETYPE_List.equals(attributes[i].getAttributeValueType()))
			{
				CComboBox editor = (CComboBox)m_editors.get(i);
				MAttributeValue value = (MAttributeValue)editor.getSelectedItem();
				log.fine(attributes[i].getName() + "=" + value);
				if (attributes[i].isMandatory() && value == null)
					mandatory += " - " + attributes[i].getName();
				attributes[i].setMAttributeInstance(m_M_AttributeSetInstance_ID, value);
			}
			else if (MAttribute.ATTRIBUTEVALUETYPE_Number.equals(attributes[i].getAttributeValueType()))
			{
				VNumber editor = (VNumber)m_editors.get(i);
				BigDecimal value = (BigDecimal)editor.getValue();
				log.fine(attributes[i].getName() + "=" + value);
				if (attributes[i].isMandatory() && value == null)
					mandatory += " - " + attributes[i].getName();
				attributes[i].setMAttributeInstance(m_M_AttributeSetInstance_ID, value);
			}
			else
			{
				VString editor = (VString)m_editors.get(i);
				String value = editor.getText();
				log.fine(attributes[i].getName() + "=" + value);
				if (attributes[i].isMandatory() && (value == null || value.length() == 0))
					mandatory += " - " + attributes[i].getName();
				attributes[i].setMAttributeInstance(m_M_AttributeSetInstance_ID, value);
			}
			m_changed = true;
		}	//	for all attributes
		
		//	Save Model
		if (m_changed)
		{
			m_masi.setDescription ();
			m_masi.save ();
		}
		m_M_AttributeSetInstance_ID = m_masi.getM_AttributeSetInstance_ID ();
		m_M_AttributeSetInstanceName = m_masi.getLot();
		//
		if (mandatory.length() > 0)
		{
			ADialog.error(m_WindowNo, this, "FillMandatory", mandatory);
			return false;
		}
		return true;
	}	//	saveSelection

	
	/* (non-Javadoc)
	 * @see org.compiere.grid.ed.IVPAttributeDialog#getM_AttributeSetInstance_ID()
	 */
	@Override
	public int getM_AttributeSetInstance_ID()
	{
		return m_M_AttributeSetInstance_ID;
	}	//	getM_AttributeSetInstance_ID

	/* (non-Javadoc)
	 * @see org.compiere.grid.ed.IVPAttributeDialog#getM_AttributeSetInstanceName()
	 */
	@Override
	public String getM_AttributeSetInstanceName()
	{
		return m_M_AttributeSetInstanceName;
	}	//	getM_AttributeSetInstanceName
	
	/* (non-Javadoc)
	 * @see org.compiere.grid.ed.IVPAttributeDialog#getM_Locator_ID()
	 */
	@Override
	public int getM_Locator_ID()
	{
		return m_M_Locator_ID; 
	}

	/* (non-Javadoc)
	 * @see org.compiere.grid.ed.IVPAttributeDialog#isChanged()
	 */
	@Override
	public boolean isChanged()
	{
		return m_changed;
	}	//	isChanged

	/**
	 * OSGi contructor
	 */
	protected VPAttributeDialog()	{
		super ((Frame)null, Msg.translate(Env.getCtx(), "M_AttributeSetInstance_ID") , true);
	}
	
	/**
	 * Get VPAttribuetDialog from OSGi or return a instance
	 * @param frame
	 * @param M_AttributeSetInstance_ID
	 * @param M_Product_ID
	 * @param C_BPartner_ID
	 * @param productWindow
	 * @param AD_Column_ID
	 * @param WindowNo
	 * @return
	 */
	public static IVPAttributeDialog getDialog(Frame frame, int M_AttributeSetInstance_ID,	int M_Product_ID, int C_BPartner_ID, boolean productWindow, int AD_Column_ID, int WindowNo)	{		
		IVPAttributeDialog dialog = Service.locate(IVPAttributeDialog.class);
		
		if (dialog != null)	{
			dialog.init(frame, M_AttributeSetInstance_ID, M_Product_ID, C_BPartner_ID, productWindow, AD_Column_ID, WindowNo);
			return dialog;
		}
		
		VPAttributeDialog self = new VPAttributeDialog(frame, M_AttributeSetInstance_ID, M_Product_ID, C_BPartner_ID, productWindow, AD_Column_ID, WindowNo);
	
		return self;
	}
} //	VPAttributeDialog
