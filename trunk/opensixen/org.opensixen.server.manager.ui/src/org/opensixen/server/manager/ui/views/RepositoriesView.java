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
package org.opensixen.server.manager.ui.views;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.opensixen.market.model.Market;
import org.opensixen.market.model.Package;
import org.opensixen.market.model.Profile;
import org.opensixen.p2.P2Director;
import org.opensixen.p2.applications.InstallJob;
import org.opensixen.p2.applications.InstallableApplication;
import org.opensixen.p2.applications.LoggerProgressMonitor;
import org.opensixen.server.manager.ui.Activator;
import org.opensixen.server.manager.ui.model.MPackage;



/**
 * RepositoriesView 
 *
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 */
public class RepositoriesView extends ViewPart implements SelectionListener{

	private Table table;
	private Composite parent;

	private String[] cols = {"Name", "Description", "Version", "Status"};
	private Button btnInstall;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		// Tabla
		table = new Table (parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible (true);
		table.setHeaderVisible (true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		table.setLayoutData(data);
		
		ArrayList<TableColumn> columns = new ArrayList<TableColumn>();
		/*
		for (String colname:cols)	{
			TableColumn col = new TableColumn (table, SWT.NONE);
			col.setText (colname);	
			
		}
						
		*/
		TableColumn col_name = new TableColumn (table, SWT.NONE);
		col_name.setText ("Name");	
		TableColumn col_desc = new TableColumn (table, SWT.NONE);
		col_desc.setText ("Name");	
		TableColumn col_ver = new TableColumn (table, SWT.NONE);
		col_ver.setText ("Name");	
		TableColumn col_status = new TableColumn (table, SWT.NONE);
		col_status.setText ("Name");	
		load();
		/*
		for (TableColumn col:columns)	{
			col.pack();
		}
		*/
		
		col_name.pack();
		
		col_desc.pack();	
		
		col_ver.pack();	
		
		col_status.pack();
		Composite btnComposite = new Composite(parent, SWT.NONE);
		btnComposite.setLayout(new RowLayout());
		btnInstall = new Button(btnComposite, SWT.PUSH);
		btnInstall.addSelectionListener(this);
		btnInstall.setText("Install");
	}

	public void load()	{		
		// Test code
/*		
		InstallableApplication app = new InstallableApplication();
		app.setID("feature.asesoriahc.feature.group");
		File update_dir = new File("/tmp/asesoria");
		app.setUpdateSite(update_dir.toURI());
		app.setPath("/tmp/server_installer/tomcat/webapps/osx/WEB-INF/eclipse");
		app.setProfile(InstallableApplication.PROFILE_SERVER);
		InstallJob job = InstallJob.getInstance();
		job.addInstallableApplication(app);
		
		P2Director.get().install(job);
*/
		
		
		try {
			List<MPackage> installed = MPackage.getInstalled();
			
			List<Package> packages = Market.getPackages();
			for (Package pkg:packages)	{
				TableItem item = new TableItem (table, SWT.NONE);
				item.setText(0, pkg.getName());
				item.setText(1, pkg.getDescription());
				item.setText(2, pkg.getVersion());
				String status = "Not installed";
				for (MPackage i:installed)	{
					if (i.getId().equals(pkg.getId())) 	{
						if (i.getVersion().equals(pkg.getVersion()))	{
							status = "Installed";
						}
						else {
							status = "Update";
						}
					}
				}
				item.setText(3, status);
			}
		}
		catch (Exception e)	{
			MessageDialog.open(MessageDialog.ERROR, parent.getShell(), "Connection Fail", e.getMessage(), SWT.NONE);
			e.printStackTrace();
		}
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource().equals(btnInstall))	{
			int index = table.getSelectionIndex();
			Package pkg = Market.getPackages().get(index);			
			install(pkg);						
		}
		
	}

	private void install(Package pkg)	{
		Profile profile = null;
		for (Profile p:pkg.getProfiles())	{
			if (p.getProfile() == Profile.PROFILE_SERVER)	{
				profile = p;
			}
		}
		// if this pkg don't have server profiles, 
		// skip installation
		if (profile != null)	{
			InstallJob job = InstallJob.getInstance();
			job.setGlobalRepository(pkg.getURI());
			job.setGlobalProfile(InstallableApplication.PROFILE_SERVER);
			
			for (String feature :profile.getFeatures())	{
				InstallableApplication app = new InstallableApplication(feature, InstallableApplication.PROFILE_SERVER);
				job.addInstallableApplication(app);
			}
			
			try {
				P2Director director = new P2Director(Activator.getServerProfileLocation());
				LoggerProgressMonitor monitor = new LoggerProgressMonitor();
				IStatus status = director.install(job, monitor);	
				String debug = P2Director.debug(status);
				System.out.println(debug);
				
				if (!status.isOK())	{
					MessageDialog.open(MessageDialog.ERROR, parent.getShell(), "Installation failed", status.getMessage(), SWT.NONE);					
				}
				
			}
			catch (ProvisionException e)	{
				MessageDialog.open(MessageDialog.ERROR, parent.getShell(), "Install Fail", e.getStatus().getMessage(), SWT.NONE);
				return;
			}
		}
		
	}
	
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
