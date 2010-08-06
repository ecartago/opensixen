package org.opensixen;

import org.compiere.Adempiere;
import org.compiere.apps.AMenu;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class OpenSixen implements IApplication {

	
	private void initOpensixen()	{
		Adempiere.startup(true);
		
		AMenu menu = new AMenu();
	}
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		initOpensixen();
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}