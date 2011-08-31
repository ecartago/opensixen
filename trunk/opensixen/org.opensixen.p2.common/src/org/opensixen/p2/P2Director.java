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
package org.opensixen.p2;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.artifact.repository.ArtifactRepositoryComponent;
import org.eclipse.equinox.internal.p2.core.DefaultAgentProvider;
import org.eclipse.equinox.internal.p2.core.EventBusComponent;
import org.eclipse.equinox.internal.p2.core.ProvisioningEventBus;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.director.PlannerComponent;
import org.eclipse.equinox.internal.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.p2.engine.ActionManager;
import org.eclipse.equinox.internal.p2.engine.EngineComponent;
import org.eclipse.equinox.internal.p2.engine.Profile;
import org.eclipse.equinox.internal.p2.engine.ProfileRegistryComponent;
import org.eclipse.equinox.internal.p2.metadata.repository.MetadataRepositoryComponent;
import org.eclipse.equinox.internal.p2.repository.CacheManager;
import org.eclipse.equinox.internal.p2.repository.CacheManagerComponent;
import org.eclipse.equinox.internal.p2.repository.Transport;
import org.eclipse.equinox.internal.p2.transport.ecf.ECFTransportComponent;
import org.eclipse.equinox.internal.provisional.p2.core.eventbus.IProvisioningEventBus;
import org.eclipse.equinox.internal.provisional.p2.director.IDirector;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IEngine;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.IProvisioningPlan;
import org.eclipse.equinox.p2.engine.PhaseSetFactory;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.VersionedId;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProfileModificationJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.planner.IPlanner;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.opensixen.p2.applications.InstallJob;
import org.opensixen.p2.applications.InstallableApplication;
import org.opensixen.p2.common.Activator;


/**
 * P2Director
 * 
 * Main API class for P2 operations via director.
 * 
 * 
 * @author Eloy Gomez 
 * Indeos Consultoria http://www.indeos.es
 */
@SuppressWarnings("restriction")
public class P2Director {
	
	
	private boolean isSelf = true;
	
	private URI profile_location;
		
	
	
	private IProvisioningAgent agent ;
	private IProfileRegistry profileRegistry;
			
	/**
	 * Create a new P2Director 
	 * with _SELF profile.
	 * @throws ProvisionException
	 */
	public P2Director() throws ProvisionException	{		
		isSelf = true;
		beginSession(null);
		
	}
	
	/**
	 * Create a new P2Dirctor
	 * with the profile locaten in location.
	 * @param location
	 * @throws ProvisionException
	 */
	public P2Director(URI location) throws ProvisionException	{				
		isSelf = false;
		profile_location = location;
		beginSession(location);
	}

	
	/**
	 * Start a Provisioning session
	 * If location is null, _SELF profile is selected.
	 * 
	 * Register all services needed by P2
	 * @throws ProvisionException
	 */
	private void beginSession(URI location) throws ProvisionException	{
		// Load agent		
		IProvisioningAgentProvider provider =  (IProvisioningAgentProvider) Activator.getService(IProvisioningAgentProvider.SERVICE_NAME);
		if (provider == null)	{
			DefaultAgentProvider defprovider = new DefaultAgentProvider();
			defprovider.activate(Activator.getContext());
			provider = defprovider;
		}
		agent = provider.createAgent(location);
		
		// Profile registry
		profileRegistry = (IProfileRegistry) new ProfileRegistryComponent().createService(agent);
		agent.registerService(IProfileRegistry.SERVICE_NAME, profileRegistry);
				
		// Register Event bus
		IProvisioningEventBus eventBus = new ProvisioningEventBus();
		agent.registerService(IProvisioningEventBus.SERVICE_NAME, eventBus);
		
		
		IProfileRegistry retistry = (IProfileRegistry) new ProfileRegistryComponent().createService(agent);
		agent.registerService(IProfileRegistry.SERVICE_NAME, retistry);
		
		new EventBusComponent().createService(agent);
		
		IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) new MetadataRepositoryComponent().createService(agent);
		agent.registerService(IMetadataRepositoryManager.SERVICE_NAME, metadataManager);
		
		IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) new ArtifactRepositoryComponent().createService(agent);
		agent.registerService(IArtifactRepositoryManager.SERVICE_NAME, artifactManager);
		
		IEngine engine = (IEngine) new EngineComponent().createService(agent);
		agent.registerService(IEngine.SERVICE_NAME, engine);
		
		IPlanner planner = (IPlanner) new PlannerComponent().createService(agent);
		agent.registerService(IPlanner.SERVICE_NAME, planner);
		
		Transport transport = (Transport) new ECFTransportComponent().createService(agent);
		agent.registerService(Transport.SERVICE_NAME, transport);
		
		CacheManager cache = (CacheManager) new CacheManagerComponent().createService(agent);
		agent.registerService(CacheManager.SERVICE_NAME, cache);
		
		ActionManager manager = new ActionManager();
		agent.registerService(ActionManager.SERVICE_NAME, manager);
				
	}

	
	/**
	 * Add a metadata repository with
	 * located in location
	 * @param location
	 * @return
	 */
	public IMetadataRepository addMetadataRepository(URI location) {
		IMetadataRepositoryManager manager = getMetadataRepositoryManager();
		if (manager == null)
			throw new IllegalStateException("No metadata repository manager found"); //$NON-NLS-1$
		try {
			return manager.loadRepository(location, null);
		} catch (ProvisionException e) {
			//fall through and create a new repository
		}
 
		// for convenience create and add a repository here
		String repositoryName = location + " - metadata"; //$NON-NLS-1$
		try {			
			IMetadataRepository repository = manager.createRepository(location, repositoryName, IMetadataRepositoryManager.TYPE_SIMPLE_REPOSITORY, null);
			manager.addRepository(repository.getLocation());
			return repository;
		} catch (ProvisionException e) {
			return null;
		}
	}
 
	
	/** 
	 * Return the metadata repository with 
	 * URI == location
	 */
	public IMetadataRepository getMetadataRepository(URI location) {
		IMetadataRepositoryManager manager = getMetadataRepositoryManager();
		if (manager == null)
			throw new IllegalStateException("No metadata repository manager found");
		try {
			return manager.loadRepository(location, null);
		} catch (ProvisionException e) {
			return null;
		}
	}
 
	/**
	 * Remote the metadata repository from profile
	 * @param location
	 */
	public void removeMetadataRepository(URI location) {
		IMetadataRepositoryManager manager = getMetadataRepositoryManager();
		if (manager == null)
			throw new IllegalStateException("No metadata repository manager found");
		manager.removeRepository(location);
	}
 
	/**
	 * Add a new artifact repository
	 * @param location
	 * @return
	 */
	public IArtifactRepository addArtifactRepository(URI location) {		
		IArtifactRepositoryManager manager = getArtifactRepositoryManager();
		if (manager == null)
			// TODO log here
			return null;
		try {
			return manager.loadRepository(location, null);
		} catch (ProvisionException e) {
			//fall through and create a new repository
		}
		// could not load a repo at that location so create one as a convenience
		String repositoryName = location + " - artifacts"; //$NON-NLS-1$
		try {
			IArtifactRepository repository = manager.createRepository(location, repositoryName, IArtifactRepositoryManager.TYPE_SIMPLE_REPOSITORY, null);
			manager.addRepository(repository.getLocation());
			return repository;
		} catch (ProvisionException e) {
			return null;
		}
	}
 
	/**
	 * Remove artifact repository from profile
	 * @param location
	 */
	public void removeArtifactRepository(URI location) {
		IArtifactRepositoryManager manager = getArtifactRepositoryManager();
		if (manager == null)
			// TODO log here
			return;
		manager.removeRepository(location);
	}
 
	
	/**
	 * Add or create a new profile
	 * @param profileId
	 * @param properties
	 * @return
	 * @throws ProvisionException
	 */
	public IProfile addProfile(InstallableApplication app ) throws ProvisionException {
		Properties prop = new Properties();
		prop.put(Profile.PROP_ROAMING, "true");
		prop.put(Profile.PROP_NAME, app.getProfile());
		prop.put(IProfile.PROP_INSTALL_FOLDER, app.getPath());
		//prop.put(IProfile.PROP_PROFILE_ROOT_IU, "true");
		prop.put(IProfile.PROP_CACHE, app.getPath());
		prop.put("eclipse.p2.flavor", "tooling");			

		return addProfile(app.getProfile(), prop);
	}
	
	/**
	 * Add or create a new profile
	 * @param profileId
	 * @param properties
	 * @return
	 * @throws ProvisionException
	 */
	public IProfile addProfile(String profileId, Properties properties) throws ProvisionException {
		// If no profileRegistry, create factory
		if (profileRegistry == null) 
			return null;
		
		IProfile profile = profileRegistry.getProfile(profileId);
		if (profile != null)
			return profile;
 
		Map<String, String> profileProperties = new HashMap<String, String>();
 
		for (Iterator<Object> it = properties.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			profileProperties.put(key, properties.getProperty(key)); 
		}
 
		if (profileProperties.get(IProfile.PROP_ENVIRONMENTS) == null) {
			EnvironmentInfo info = (EnvironmentInfo) ServiceHelper.getService(Activator.getContext(), EnvironmentInfo.class.getName());
			if (info != null)
				profileProperties.put(IProfile.PROP_ENVIRONMENTS, "osgi.os=" + info.getOS() + ",osgi.ws=" + info.getWS() + ",osgi.arch=" + info.getOSArch());
			else
				profileProperties.put(IProfile.PROP_ENVIRONMENTS, "");
		}
 
		return profileRegistry.addProfile(profileId, profileProperties);
	}
 
	/**
	 * Remove profile whith this from location
	 * or _SELF
	 * @param profileId
	 */
	public void removeProfile(String profileId) {
		if (profileRegistry == null)
			return;
		profileRegistry.removeProfile(profileId);
	}
 
	/**
	 * Return all hosted profiles
	 * @return
	 */
	public IProfile[] getProfiles() {
		if (profileRegistry == null)
			return new IProfile[0];
		return profileRegistry.getProfiles();
	}
 
	/**
	 * Return profile with this ID
	 * @param id
	 * @return
	 */
	public IProfile getProfile(String id) {
		if (profileRegistry == null)
			return null;
		return profileRegistry.getProfile(id);					
	}
	
	/**
	 * Refresh repositories
	 * @param location
	 * @param monitor
	 * @throws ProvisionException
	 */
	public void refreshRepositories(URI location, IProgressMonitor monitor)	 throws ProvisionException {
		getArtifactRepositoryManager().refreshRepository(location, monitor);
		getMetadataRepositoryManager().refreshRepository(location, monitor);
	}
 
	/**
	 * Returns the installable units that match the given query
	 * in the given metadata repository.
	 * 
	 * @param location The location of the metadata repo to search.  &lt;code&gt;null</code> indicates
	 *        search all known repos.
	 * @param query The query to perform
	 * @param monitor A progress monitor, or &lt;code&gt;null</code> 
	 * @return The IUs that match the query
	 */
	public  IInstallableUnit[] getInstallableUnits(URI location, IQuery<IInstallableUnit> query, IProgressMonitor monitor) {
		IQueryable<IInstallableUnit> queryable = null;
		if (location == null) {
			queryable = getMetadataRepositoryManager();
		} else {
			queryable = getMetadataRepository(location);
		}
		IQueryResult<IInstallableUnit> result = queryable.query(query, monitor);
		return result.toArray(IInstallableUnit.class);				
	}
 
	/**
	 * Return the metadata repositories of this profile
	 * @return
	 */
	public URI[] getMetadataRepositories() {
		IMetadataRepositoryManager manager = (IMetadataRepositoryManager) ServiceHelper.getService(Activator.getContext(), IMetadataRepositoryManager.class.getName());
		if (manager == null)
			// TODO log here
			return null;
		URI[] repos = manager.getKnownRepositories(IRepositoryManager.REPOSITORIES_ALL);
		if (repos.length > 0)
			return repos;
		return null;
	}
 
	/**
	 *  Install the described UI from 
	 *  the location
	 * @param unitId
	 * @param version
	 * @param profile
	 * @param location
	 * @param progress
	 * @return
	 * @throws ProvisionException
	 */
	public IStatus install(String unitId, String version, IProfile profile, URI location, IProgressMonitor progress) throws ProvisionException {
		addArtifactRepository(location);
		addMetadataRepository(location);
		return install(unitId, version, profile, progress);
	}
	
	
	/**
	 *  Install the described IU
	 *  
	 * @param unitId
	 * @param version
	 * @param profile
	 * @param progress
	 * @return
	 * @throws ProvisionException
	 */
	public IStatus install(String unitId, String version, IProfile profile, IProgressMonitor progress) throws ProvisionException {
		
		if (profile == null)
			return null;
		VersionedId versionedId = new VersionedId(unitId, version);
		IQuery<IInstallableUnit> query = QueryUtil.createIUQuery(unitId, versionedId.getVersion());
		if (version == null)	{
			query = QueryUtil.createLatestQuery(query);
		}
		IInstallableUnit[] units = getInstallableUnits(null, query, progress);
		if (units.length == 0) {
			StringBuffer error = new StringBuffer();
			error.append("Installable unit not found: " + unitId + ' ' + version + '\n');
			error.append("Repositories searched:\n");
			URI[] repos = getMetadataRepositories();
			if (repos != null) {
				for (int i = 0; i < repos.length; i++)
					error.append(repos[i] + "\n");
			}
			throw new ProvisionException(error.toString());
		}
		return install(units, profile, progress);		
	}
	
	public IStatus install(InstallableApplication app, IProgressMonitor progress) throws ProvisionException {
		IProfile profile = addProfile(app);
		addMetadataRepository(app.getUpdateSite());
		addArtifactRepository(app.getUpdateSite());
		
		return install(app.getID(), null, profile, progress);
		
	}
	
	public IStatus install(InstallJob job, IProgressMonitor progress) throws ProvisionException {
		if (job.getGlobalRepository() == null	|| job.getGlobalProfile() == null) {
			throw new ProvisionException("Job must have global repository configured.");
		}
		
		
		// Query location for iu to install
		URI location = job.getGlobalRepository();
		IQuery<IInstallableUnit> query = QueryUtil.createIUGroupQuery();
		query = QueryUtil.createLatestQuery(query);		
		IInstallableUnit[] units = getInstallableUnits(location, query, progress);				
		
		// If no finded, launch exception
		if (units.length == 0)	{
			throw new ProvisionException("Repository " + job.getGlobalRepository().toString() + " empty.");
		}
		
		// Add repositories to profile
		addArtifactRepository(location);
		addMetadataRepository(location);
		
		// Check if repository contains features to install.
		// and convertit to array
		ArrayList<IInstallableUnit> toInstall = new ArrayList<IInstallableUnit>();
		
		// Create an index
		ArrayList<String> apps_index = new ArrayList<String>();
		for (InstallableApplication app:job.getInstallableApplications())	{
			apps_index.add(app.getID());
		}
		
		for (IInstallableUnit unit:units)	{
			if (apps_index.contains(unit.getId()))	{
				toInstall.add(unit);
			}
		}
		
		// Install apps in globalProfile
		IInstallableUnit[] finded = toInstall.toArray(new IInstallableUnit[toInstall.size()]);
		IProfile profile = getProfile(job.getGlobalProfile());
		return install(finded, profile, progress);
	}
	
	/**
	 * Install units into profile
	 * 
	 * @param toInstall
	 * @param profile
	 * @param progress
	 * @return
	 * @throws ProvisionException
	 */
	private IStatus install(IInstallableUnit[] toInstall, IProfile profile, IProgressMonitor progress) throws ProvisionException {
		 if (toInstall == null || toInstall.length == 0)	{
			 throw new ProvisionException("Nothing to install.");
		 }
		IPlanner planner = (IPlanner) agent.getService(IPlanner.SERVICE_NAME);
		if (planner == null)
			throw new ProvisionException("No planner service found.");
 
		IEngine engine = (IEngine) agent.getService(IEngine.SERVICE_NAME);
		if (engine == null)
			throw new ProvisionException("No director service found.");
		
		ProvisioningContext context = new ProvisioningContext(agent);
		ProfileChangeRequest request = new ProfileChangeRequest(profile);
		request.addInstallableUnits(toInstall);
		
		IProvisioningPlan result = planner.getProvisioningPlan(request, context, progress);
		if (!result.getStatus().isOK())
			return result.getStatus();
				
		ProvisioningSession session = new ProvisioningSession(agent);
		ProfileModificationJob job = new ProfileModificationJob("Update", session, profile.getProfileId(), result, context);
		return job.run(progress);

	}
	
 
	/**
	 * Uninstall the described IU
	 */
	public IStatus uninstall(String unitId, String version, IProfile profile, IProgressMonitor progress) throws ProvisionException {
		IDirector director = (IDirector) ServiceHelper.getService(Activator.getContext(), IDirector.class.getName());
		if (director == null)
			throw new ProvisionException("No director service found.");
 
		// return director.uninstall(new InstallableUnit[] {toInstall}, profile,
		// null);
		return null;
	}

	/**
	 * Return all the artifacts repositories in this profile
	 * @return
	 */
	public URI[] getArtifactRepositories() {
		IArtifactRepositoryManager manager = (IArtifactRepositoryManager) ServiceHelper.getService(Activator.getContext(), IArtifactRepositoryManager.class.getName());
		if (manager == null)
			// TODO log here
			return null;
		URI[] repos = manager.getKnownRepositories(IRepositoryManager.REPOSITORIES_ALL);
		if (repos.length > 0)
			return repos;
		return null;
	}
 
	/**
	 * Return the artifact repository with this URL
	 * @param repoURI
	 * @return
	 */
	public IArtifactRepository getArtifactRepository(URI repoURI) {
		IArtifactRepositoryManager manager = (IArtifactRepositoryManager) ServiceHelper.getService(Activator.getContext(), IArtifactRepositoryManager.class.getName());
		try {
			if (manager != null)
				return manager.loadRepository(repoURI, null);
		} catch (ProvisionException e) {
			//for console, just ignore repositories that can't be read
		}
		return null;
	}
				
	private IArtifactRepositoryManager getArtifactRepositoryManager() {		
		IArtifactRepositoryManager manager = (IArtifactRepositoryManager) agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
		return manager;
		
	}
	
	private IMetadataRepositoryManager getMetadataRepositoryManager() {		
		IMetadataRepositoryManager manager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);		
		return manager;		
	}
	
	/**
	 * Debug an IStatus return code
	 * @param status
	 * @return
	 */
	public static String debug(IStatus status)	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(status.getCode()  + ": " +status.getMessage());
		buffer.append("\n");
		for (IStatus s:status.getChildren())	{			
			debug(s, buffer);
		}
		
		return buffer.toString();
		
	}
	/**
	 * Debug a IStatus return code
	 * called recursively from debug(IStatus)
	 * @param status
	 * @param buffer
	 */
	private static void debug(IStatus status, StringBuffer buffer)	{	
		buffer.append(status.getCode()  + ": " +status.getMessage());
		buffer.append("\n");
		for (IStatus s:status.getChildren())	{			
			debug(s, buffer);
		}
	}
	
}
