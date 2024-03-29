package org.opensixen.server.manager.ui.views;

import java.sql.SQLException;
import java.util.Properties;

import org.compiere.util.CustomConnection;
import org.compiere.util.Ini;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.opensixen.os.PlatformDetails;
import org.opensixen.os.PlatformProvider;
import org.opensixen.os.ProviderFactory;
import org.opensixen.p2.applications.InstallJob;
import org.opensixen.server.manager.ui.Activator;
import org.opensixen.server.manager.ui.Messages;
import org.opensixen.server.manager.ui.db.DB;

public class ConnectionView extends ViewPart implements SelectionListener,
		ModifyListener {

	public static final String ID = "org.opensixen.server.manager.ui.views.ConnectionView";

	private PlatformProvider provider;

	private Composite parent;
	
	private Combo fDbType;
	private Text fDbHost;
	private Text fDBuser;
	private Text fDBPasswd;
	private Text fDbPort;
	private Text fDbName;

	private String[] dbNames = { DB.DB_POSTGRES, DB.DB_ORACLE }; //$NON-NLS-1$ //$NON-NLS-2$
	private Text fASHost;
	private Text fASPort;

	private Properties config;

	private Button btnOk;

	private Button btnReload;

	private Button btnTest;

	/**
	 * @param pageName
	 */
	public ConnectionView() {
		provider = ProviderFactory.getProvider();
	}

	public void createPartControl(Composite parent) {
		this.parent = parent;
		ScrolledComposite scrollComposite = new ScrolledComposite(parent,
				SWT.BORDER | SWT.V_SCROLL);
		scrollComposite.setExpandVertical(true);
		scrollComposite.setExpandHorizontal(true);
		Composite container = new Composite(scrollComposite, SWT.NONE);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		Group setup = new Group(container, SWT.NONE);
		setup.setLayout(new GridLayout(2, false));
		setup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setup.setText(Messages.CONFIG);

		Label l = new Label(setup, SWT.NONE);
		l.setText(Messages.AS_HOST);
		fASHost = new Text(setup, SWT.BORDER);
		fASHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fASHost.addModifyListener(this);

		l = new Label(setup, SWT.NONE);
		l.setText(Messages.AS_PORT);
		fASPort = new Text(setup, SWT.BORDER);
		fASPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		fASPort.setText("8080"); //$NON-NLS-1$
		// Not configurable
		// fASPort.addModifyListener(this);
		fASPort.setEnabled(false);

		l = new Label(setup, SWT.NONE);
		l.setText(Messages.DATABASE_TYPE);
		fDbType = new Combo(setup, SWT.DROP_DOWN);
		fDbType.setItems(dbNames);
		fDbType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		// fDbType.setText(DB_POSTGRES);
		fDbType.select(0);
		fDbType.addSelectionListener(this);

		l = new Label(setup, SWT.NONE);
		l.setText(Messages.DB_PORT);
		fDbPort = new Text(setup, SWT.BORDER);
		fDbPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fDbPort.setText(DB.PORT_POSTGRES);
		fDbPort.addModifyListener(this);

		l = new Label(setup, SWT.NONE);
		l.setText(Messages.DB_HOST);
		fDbHost = new Text(setup, SWT.BORDER);
		fDbHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fDbHost.addModifyListener(this);

		// By default, the hostname is the same as the localhost
		// fDbHost.setText(PlatformDetails.getHostname());
		/*
		 * l = new Label(setup, SWT.NONE); l.setText(Messages.DB_USER_SYSTEM);
		 * 
		 * fDBSystemuser = new Text(setup, SWT.BORDER);
		 * fDBSystemuser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
		 * false)); fDBSystemuser.addModifyListener(this);
		 * 
		 * l = new Label(setup, SWT.NONE); l.setText(Messages.DB_PWD_SYSTEM);
		 * 
		 * fDBSystemPasswd = new Text(setup, SWT.PASSWORD | SWT.BORDER);
		 * fDBSystemPasswd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
		 * false, false)); fDBSystemPasswd.addModifyListener(this);
		 */

		l = new Label(setup, SWT.NONE);
		l.setText(Messages.DB_NAME);
		fDbName = new Text(setup, SWT.BORDER);
		fDbName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fDbName.addModifyListener(this);

		l = new Label(setup, SWT.NONE);
		l.setText(Messages.DB_USER);

		fDBuser = new Text(setup, SWT.BORDER);
		fDBuser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		// Not configurable
		fDBuser.setText("adempiere"); //$NON-NLS-1$
		fDBuser.addModifyListener(this);
		fDBuser.setEnabled(false);

		l = new Label(setup, SWT.NONE);
		l.setText(Messages.DB_PASSWORD);

		fDBPasswd = new Text(setup, SWT.PASSWORD | SWT.BORDER);
		fDBPasswd
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fDBPasswd.addModifyListener(this);

		// Buttons
		Composite btnComposite = new Composite(container, SWT.NONE);
		btnComposite.setLayout(new FillLayout());
		btnOk = new Button(btnComposite, SWT.PUSH);
		btnOk.setText("Save");
		btnOk.addSelectionListener(this);
		btnOk.setEnabled(false);

		btnReload = new Button(btnComposite, SWT.PUSH);
		btnReload.addSelectionListener(this);
		btnReload.setText("Reload");
		btnTest = new Button(btnComposite, SWT.PUSH);
		btnTest.addSelectionListener(this);
		btnTest.setText("Test");

		scrollComposite.setContent(container);
		scrollComposite.pack();

		init();
	}

	private void init() {
		// Load current conf
		try {
			boolean firstTime = Ini.loadProperties(Activator.getServerHome() +"/" + Activator.PROPERTIES_FILENAME);
			
			if (firstTime == false) {
				Properties properties = CustomConnection.getProperties(Ini.getProperty(Ini.P_CONNECTION));
				if (properties != null)	{	
					fASHost.setText(properties.getProperty(CustomConnection.P_AppsHost));
					fASPort.setText(properties.getProperty(CustomConnection.P_AppsPort));
					fDbHost.setText(properties.getProperty(CustomConnection.P_DBHost));
					fDbPort.setText(properties.getProperty(CustomConnection.P_DBPort));
					fDBPasswd.setText(properties
							.getProperty(CustomConnection.P_DBPassword));
					fDbName.setText(properties.getProperty(CustomConnection.P_DBName));
					fDBuser.setText(properties.getProperty(CustomConnection.P_DBUser));
		
					for (int i = 0; i < dbNames.length; i++) {
						if (dbNames[i].equals(properties
								.getProperty(CustomConnection.P_DBType))) {
							fDbType.select(i);
						}
					}
				}
			}
		}
		catch (Exception e)	{
			
		}
		PlatformDetails details = provider.getPlatformDetails();
		if (fASHost.getText().length() == 0) {
			fASHost.setText(details.getHostname());
		}

		if (fDbHost.getText().length() == 0) {
			fDbHost.setText(details.getHostname());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
	 * .events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {

		// If change combo, change default port
		if (e.getSource().equals(fDbType)) {
			if (fDbType.getText().equals(DB.DB_POSTGRES)) { //$NON-NLS-1$
				fDbPort.setText(DB.PORT_POSTGRES); //$NON-NLS-1$
			} else {
				fDbPort.setText(DB.PORT_ORACLE); //$NON-NLS-1$
			}
		}

		else if (e.getSource().equals(btnReload)) {
			init();
		} else if (e.getSource().equals(btnOk)) {
			Properties prop = bindProperties();
			try {
				createProperties(prop, Activator.getServerHome());
			}
			catch (Exception ex)	{
				return;
			}
			try {DB.init(prop);} catch (SQLException ex){}
			btnOk.setEnabled(false);
		}
		else if (e.getSource().equals(btnTest))	{
			test();
		}

	}
	/**
	 * create properties from fields
	 * @return
	 */
	private Properties bindProperties()	{
		Properties conf = new Properties();
		conf.put("name", fASHost.getText()); //$NON-NLS-1$
		conf.put("AppsHost", fASHost.getText()); //$NON-NLS-1$
		conf.put("AppsPort", fASPort.getText()); //$NON-NLS-1$
		conf.put("type", fDbType.getText()); //$NON-NLS-1$
		conf.put("DBhost", fDbHost.getText()); //$NON-NLS-1$
		conf.put("DBport", fDbPort.getText()); //$NON-NLS-1$
		conf.put("DBname", fDbName.getText()); //$NON-NLS-1$

		conf.put("UID", fDBuser.getText()); //$NON-NLS-1$
		conf.put("PWD", fDBPasswd.getText()); //$NON-NLS-1$
		return conf;

	}
	
	private boolean createProperties(Properties prop, String path) throws Exception {
		// Setup as server and setup path as adempiere home
		
		Ini.setClient(false);
		Ini.setAdempiereHome(path);
		Ini.loadProperties(true);
		Ini.setProperty(Ini.P_CONNECTION,
				CustomConnection.getConnectionString(prop));

		Ini.saveProperties(false);
		return true;

	}

	
	private void test()	{
		try {
			if (DB.test(bindProperties()))	{				
				btnOk.setEnabled(true);
				MessageDialog.open(MessageDialog.INFORMATION, parent.getShell(), "Connection Ok", "Connection Ok", SWT.NONE);
			}
		}
		catch (SQLException e)	{
			MessageDialog.open(MessageDialog.ERROR, parent.getShell(), "Connection Fail", e.getMessage(), SWT.NONE);
		}
	}
	
	public void setFocus() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modifyText(ModifyEvent e) {
		btnOk.setEnabled(false);

	}
}
