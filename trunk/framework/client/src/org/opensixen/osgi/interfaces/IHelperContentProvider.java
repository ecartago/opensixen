/**
 * 
 */
package org.opensixen.osgi.interfaces;

import java.util.Properties;

import org.compiere.swing.CPanel;
import org.opensixen.swing.EPanel;
import org.opensixen.swing.IPanelListener;


/**
 * @author harlock
 *
 */
public interface IHelperContentProvider extends IService, IPanelListener {

	public static final String P_WINDOWNAME="windowName";

	public void initContent(Properties ctx, CPanel parent, EPanel panel ); 
	
}
