package org.opensixen.server.manager.ui;

import java.sql.SQLException;
import java.util.Properties;

import org.compiere.util.CustomConnection;
import org.compiere.util.Ini;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.opensixen.server.manager.ui.db.DB;
import org.opensixen.server.manager.ui.views.ConnectionView;
import org.opensixen.server.manager.ui.views.RepositoriesView;
import org.opensixen.server.manager.ui.views.ServerInstalledSoftwareView;

public class Perspective implements IPerspectiveFactory {

	/**
	 * The ID of the perspective as specified in the extension.
	 */
	public static final String ID = "org.opensixen.server.manager.ui.perspective";

	public void createInitialLayout(IPageLayout layout) {
		
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
	
		/*
		layout.addStandaloneView(NavigationView.ID,  false, IPageLayout.BOTTOM, 0.25f, editorArea);
		layout.getViewLayout(NavigationView.ID).setCloseable(false);
		*/
		IFolderLayout folder = layout.createFolder("messages", IPageLayout.TOP, 0.5f, editorArea);
		
		folder.addPlaceholder(ConnectionView.ID + ":*");
		folder.addPlaceholder(RepositoriesView.class.getName() + ":*");
		folder.addPlaceholder(ServerInstalledSoftwareView.ID + ":*");
		
		// Check config
		try {
			boolean firstTime = Ini.loadProperties(Activator.getServerHome() +"/" + Activator.PROPERTIES_FILENAME);
			
			if (firstTime) {
				folder.addView(ConnectionView.ID);
			}		
			
			else {
				Properties prop = CustomConnection.getProperties(Ini.getProperty(Ini.P_CONNECTION));
				try {				
					DB.init(prop);
				}
				catch (SQLException e)	{
					e.printStackTrace();
					folder.addView(ConnectionView.ID);
				}
			}
		}
		catch (Exception ex)	{
			
		}
		//folder.addView(ServerInstalledSoftwareView.ID);				
		//layout.addView(ServerInstalledSoftwareView.ID, IPageLayout.TOP, IPageLayout.RATIO_MAX, IPageLayout.ID_EDITOR_AREA);
		
	}
}
