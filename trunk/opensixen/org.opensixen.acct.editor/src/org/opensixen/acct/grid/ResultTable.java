package org.opensixen.acct.grid;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.compiere.report.core.RColumn;
import org.compiere.report.core.ResultTableCellEditor;
import org.compiere.util.CLogger;
import org.compiere.util.MSort;

/**
 *  The Table to present RModel information
 *
 *  @author Jorg Janke
 *  @version  $Id: ResultTable.java,v 1.2 2006/07/30 00:51:06 jjanke Exp $
 */
public class ResultTable extends JTable implements MouseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2633317327407673345L;

	/**
	 *  Constructor
	 */
	public ResultTable()
	{
		super();
		setCellSelectionEnabled(false);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(false);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		//  Default Editor
		ResultTableCellEditor rtce = new ResultTableCellEditor();
		setCellEditor(rtce);

		//  Mouse Listener
		addMouseListener(this);
		getTableHeader().addMouseListener(this);
	}   //  ResultTable

	/** Last model index sorted */
	private int         m_lastSortIndex = -1;
	/** Sort direction          */
	private boolean     m_asc = true;
	/** RModel                  */
	private RModel		m_model = null;
	
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ResultTable.class);

	/**
	 *  Create a JTable Model from ReportModel
	 *  @param reportModel
	 */
	public ResultTable (RModel reportModel)
	{
		this();
		setModel(reportModel);
	}   //  ResultTable

	/**
	 *  Set Model
	 *  @param reportModel
	 */
	public void setModel (RModel reportModel)
	{
		log.config(reportModel.toString());
		m_model = reportModel;
		super.setModel(new ResultTableModel(reportModel));
		//
		TableColumnModel tcm = getColumnModel();
		//  Set Editor/Renderer
		for (int i = 0; i < tcm.getColumnCount(); i++)
		{
			TableColumn tc = tcm.getColumn(i);
			RColumn rc = reportModel.getRColumn(i);
			if (rc.getColHeader().equals(tc.getHeaderValue()))
			{
				ResultTableCellRenderer rtcr = new ResultTableCellRenderer(reportModel, rc);
				tc.setCellRenderer(rtcr);
				//
			}
			else
				log.log(Level.SEVERE, "RColumn=" + rc.getColHeader() + " <> TableColumn=" + tc.getHeaderValue());
		}
		autoSize();
	}   //  setModel
	
	/**
	 * @return RModel
	 */
	public RModel getRModel() {
		return m_model;
	}

	/**
	 *  Set Model
	 *  @param ignored
	 */
	public void setModel (TableModel ignored)
	{
		//  throw new IllegalArgumentException("Requires RModel");  //  default construvtor calls this
		super.setModel(ignored);
	}   //  setModel

	/**
	 *  Table Model Listener
	 *  @param e
	 */
	public void tableChanged(TableModelEvent e)
	{
		super.tableChanged(e);
		log.fine("Type=" + e.getType());
	}   //  tableChanged

	/*************************************************************************/

	/**
	 *  Mouse Clicked
	 *  @param e
	 */
	public void mouseClicked(MouseEvent e)
	{
		int col = getColumnModel().getColumnIndexAtX(e.getX());
		log.fine("Column " + col + " = " + getColumnModel().getColumn(col).getHeaderValue()
			+ ", Table r=" + this.getSelectedRow() + " c=" + this.getSelectedColumn());

		//  clicked Cell
		if (e.getSource() == this)
		{
		}
		//  clicked Header
		else
		{
			int mc = convertColumnIndexToModel(col);
			sort(mc);
			displayrealbalance(this);
		}
	}   //  mouseClicked
	
	/**
	 * Cambia la columna de saldo para que sume o reste en función de las filas anteriores
	 * @param table2
	 */
	private void displayrealbalance(ResultTable table) {
		BigDecimal prevalue=BigDecimal.ZERO;

		BigDecimal actualvalue=BigDecimal.ZERO;
		for(int i=0;i<table.getRowCount();i++){
			BigDecimal initialvaluedr=(BigDecimal)table.getValueAt(i, 2);
			BigDecimal initialvaluecr=(BigDecimal)table.getValueAt(i, 3);
			actualvalue=(BigDecimal)initialvaluedr.subtract(initialvaluecr);
			if(i==0){
				table.setValueAt(initialvaluedr.subtract(initialvaluecr), i, 4);
				prevalue=(BigDecimal)initialvaluedr.subtract(initialvaluecr);
				
			}else{
				prevalue=prevalue.add(actualvalue);
				table.setValueAt(prevalue, i, 4);
			}
		}
	}

	public void mousePressed(MouseEvent e)
	{
	}
	public void mouseReleased(MouseEvent e)
	{
	}
	public void mouseEntered(MouseEvent e)
	{
	}
	public void mouseExited(MouseEvent e)
	{
	}

	
	/**************************************************************************
	 *	Size Columns
	 */
	private void autoSize()
	{
		log.config("");
		//
		final int SLACK = 8;		//	making sure it fits in a column
		final int MAXSIZE = 300;    //	max size of a column
		//
		TableColumnModel tcm = getColumnModel();
		//  For all columns
		for (int col = 0; col < tcm.getColumnCount(); col++)
		{
			TableColumn tc = tcm.getColumn(col);
		//  log.config( "Column=" + col, tc.getHeaderValue());
			int width = 0;

			//	Header
			TableCellRenderer renderer = tc.getHeaderRenderer();
			if (renderer == null)
				renderer = new DefaultTableCellRenderer();
			Component comp = renderer.getTableCellRendererComponent
				(this, tc.getHeaderValue(), false, false, 0, 0);
		//	log.fine( "Hdr - preferred=" + comp.getPreferredSize().width + ", width=" + comp.getWidth());
			width = comp.getPreferredSize().width + SLACK;

			//	Cells
			int maxRow = Math.min(30, getRowCount());       //  first 30 rows
			for (int row = 0; row < maxRow; row++)
			{
				renderer = getCellRenderer(row, col);
				comp = renderer.getTableCellRendererComponent
					(this, getValueAt(row, col), false, false, row, col);
				int rowWidth = comp.getPreferredSize().width + SLACK;
				width = Math.max(width, rowWidth);
			}
			//	Width not greater ..
			width = Math.min(MAXSIZE, width);
			tc.setPreferredWidth(width);
		//	log.fine( "width=" + width);
		}	//	for all columns
	}	//	autoSize

	/**
	 *  Sort Table
	 *  @param modelColumnIndex
	 */
	private void sort (final int modelColumnIndex)
	{
		int rows = getRowCount();
		if (rows == 0)
			return;
		//  other column
		if (modelColumnIndex != m_lastSortIndex)
			m_asc = true;
		else
			m_asc = !m_asc;

		m_lastSortIndex = modelColumnIndex;
		//
		log.config("#" + modelColumnIndex
			+ " - rows=" + rows + ", asc=" + m_asc);
		ResultTableModel model = (ResultTableModel)getModel();

		//  Prepare sorting
		final MSort sort = new MSort(0, null);
		sort.setSortAsc(m_asc);
		// Sort the data list - teo_sarca [ 1734327 ]
		Collections.sort(model.getDataList(), new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				Object item1 = ((ArrayList)o1).get(modelColumnIndex);
				Object item2 = ((ArrayList)o2).get(modelColumnIndex);
				return sort.compare(item1, item2);
			}
		});
	}   //  sort

}   //  ResultTable
