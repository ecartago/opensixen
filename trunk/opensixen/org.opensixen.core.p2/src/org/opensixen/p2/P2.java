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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.internal.p2.metadata.repository.SimpleMetadataRepositoryFactory;
import org.eclipse.equinox.internal.p2.operations.IStatusCodes;
import org.eclipse.equinox.p2.core.IAgentLocation;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepository;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.opensixen.core.p2.Activator;

/**
 * 
 * 
 * @author Eloy Gomez Indeos Consultoria http://www.indeos.es
 * 
 */
public class P2 {

	/**
	 * A status code used to indicate that there were no updates found when
	 * looking for updates.
	 */
	public static final int STATUS_NOTHING_TO_UPDATE = IStatusCodes.NOTHING_TO_UPDATE;
	
	
	
	private static P2 instance;
	private CLogger log = CLogger.getCLogger(getClass());
	private static CLogger s_log = CLogger.getCLogger(P2.class);

	private IRepositoryManager<IMetadataRepository> metadataManager;
	private final IProvisioningAgent agent;

	/**
	 * Get the current instance of P2
	 * 
	 * @return
	 */
	public static P2 get() {
		if (instance == null) {
			instance = new P2();
		}

		return instance;
	}

	@SuppressWarnings("unchecked")
	public P2() {
		agent = (IProvisioningAgent) Activator
				.getService(IProvisioningAgent.SERVICE_NAME);
		metadataManager = (IRepositoryManager<IMetadataRepository>) agent
				.getService(IMetadataRepositoryManager.SERVICE_NAME);
	}

	/**
	 * Add a repository to the _SELF_ profile
	 * 
	 * @param location
	 * @param name
	 */
	public void addRepository(URI location, String name) {
		if (!metadataManager.contains(location)) {
			metadataManager.addRepository(location);
			if (name != null) {
				metadataManager.setRepositoryProperty(location,
						IRepository.PROP_NICKNAME, name);
			}
		}

		IRepositoryManager<IArtifactRepository> artifactManager = (IRepositoryManager<IArtifactRepository>) agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
		if (!artifactManager.contains(location)) {
			artifactManager.addRepository(location);
			if (name != null) {
				artifactManager.setRepositoryProperty(location,
						IRepository.PROP_NICKNAME, name);
			}
		}
	}

	/**
	 * Add a repository to the _SELF_ profile
	 * 
	 * @param location
	 * @param name
	 */
	public void removeRepository(URI location) {

		if (metadataManager.contains(location)) {
			metadataManager.removeRepository(location);
		}

		IRepositoryManager<IArtifactRepository> artifactManager = (IRepositoryManager<IArtifactRepository>) agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
		if (artifactManager.contains(location)) {
			artifactManager.removeRepository(location);
		}
	}

	/**
	 * Gel all enabled repositories
	 * 
	 * @return
	 */
	public URI[] getRepositories() {
		return metadataManager
				.getKnownRepositories(IRepositoryManager.REPOSITORIES_ALL);
	}

	public RepositoryModel[] getAllRepositoryModel() {
		URI[] locations = getRepositories();
		RepositoryModel[] model = new RepositoryModel[locations.length];
		for (int i = 0; i < locations.length; i++) {
			String name = metadataManager.getRepositoryProperty(locations[i],
					IMetadataRepository.PROP_NICKNAME);
			model[i] = new RepositoryModel(name, locations[i]);
		}
		return model;
	}

	/**
	 * Get all InstallableUnit of this location
	 * 
	 * @param location
	 * @return
	 * @throws URISyntaxException
	 * @throws ProvisionException
	 */
	@SuppressWarnings("restriction")
	public List<IUnitModel> getAllIUnit(URI location) throws RuntimeException {
		ArrayList<IUnitModel> iunits = new ArrayList<IUnitModel>();
		IInstallableUnit[] units = getAllIInstallableUnit(location);
		for (IInstallableUnit installableUnit : units) {
			iunits.add(new IUnitModel(location, installableUnit));
		}
		return iunits;
	}
	
	
	public IInstallableUnit[] getAllIInstallableUnit(URI location)	{		
		SimpleMetadataRepositoryFactory factory = new SimpleMetadataRepositoryFactory();
		factory.setAgent(agent);
		IMetadataRepository repository;
		try {
			repository = factory.load(location, 0, new P2ProgressMonitor());
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error cargando el repositorio", e);
			return new IInstallableUnit[0];
		}
		IQuery<IInstallableUnit> query = QueryUtil.createIUGroupQuery();
		query = QueryUtil.createLatestQuery(query);
		IQueryResult<IInstallableUnit> result = repository.query(query, null);
		return result.toArray(IInstallableUnit.class);
	}
	
	
	/**
	 * Check for updates
	 * @param agent
	 * @param monitor
	 * @return
	 * @throws OperationCanceledException
	 */
	public IStatus update(IProgressMonitor monitor) throws OperationCanceledException {
		ProvisioningSession session = new ProvisioningSession(agent);
		List<IInstallableUnit> toBeUpdated = P2.get().getInstalled();
		UpdateOperation operation = new UpdateOperation(session, toBeUpdated);
		IStatus status = operation.resolveModal(monitor);
		if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
			return status;
		}
		if (status.getSeverity() == IStatus.CANCEL)
			throw new OperationCanceledException();
		
		if (status.getSeverity() != IStatus.ERROR) {
			// More complex status handling might include showing the user what updates
			// are available if there are multiples, differentiating patches vs. updates, etc.
			// In this example, we simply update as suggested by the operation.
			ProvisioningJob job = operation.getProvisioningJob(null);
			if (job == null)	{
				log.info("No provisioning job: " + status.getCode());
				return status;
			}
			
			status = job.run(monitor);
			if (status.getSeverity() == IStatus.CANCEL)
				throw new OperationCanceledException();
		}
		return status;
	}

	/**
	 * Install instalableUnits into the _SELF_ profile
	 * 
	 * @param iunits
	 * @return
	 */
	public IStatus install(ArrayList<IUnitModel> iunits, IProgressMonitor monitor) {
		ArrayList<IInstallableUnit> installableUnits = new ArrayList<IInstallableUnit>();
		for (IUnitModel iunit : iunits) {
			installableUnits.add(iunit.getInstallableUnit());
		}
		ProvisioningSession session = new ProvisioningSession(agent);
		InstallOperation op = new InstallOperation(session, installableUnits);
		
		IStatus status = op.resolveModal(monitor);
		if (status.getCode() == IStatus.ERROR)	{
			return status;
		}
		
		ProvisioningJob job = op.getProvisioningJob(monitor);
		status = job.run(monitor);
		return status;
	}
	
	/**
	 * Return all installed IU from all profiles
	 * @return
	 */
	public List<IInstallableUnit> getInstalled()	{
		ArrayList<IInstallableUnit> iunits = new ArrayList<IInstallableUnit>();
		IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
		if (profileRegistry == null)	{
			return iunits;
		}
		IProfile[] profiles = profileRegistry.getProfiles();
		for (int i=0; i < profiles.length; i++)	{
			IQueryResult<IInstallableUnit> result = profiles[i].available(QueryUtil.createIUGroupQuery(), null);
			IInstallableUnit[] units = result.toArray(IInstallableUnit.class);
			for (IInstallableUnit installableUnit : units) {
				iunits.add(installableUnit);
			}		
		}
		return iunits;
	}

	/**
	 * Return all installed IU from all profiles
	 * as IUnitModel list
	 * @return
	 */
	public List<IUnitModel> getInstalledModel()	{		
		ArrayList<IUnitModel> models = new ArrayList<IUnitModel>(); 
		List<IInstallableUnit> installed = getInstalled();
		for (IInstallableUnit unit:installed)	{
			models.add(new IUnitModel(unit));
		}
		return models;
	}
	
	public static String debug(IStatus status)	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(status.getCode()  + ": " +status.getMessage());
		buffer.append("\n");
		for (IStatus s:status.getChildren())	{			
			debug(s, buffer);
		}
		
		return buffer.toString();
		
	}
	public static void debug(IStatus status, StringBuffer buffer)	{
		
		buffer.append(status.getCode()  + ": " +status.getMessage());
		buffer.append("\n");
		for (IStatus s:status.getChildren())	{			
			debug(s, buffer);
		}
	}
		
	public String debug()	{
		StringBuffer buffer = new StringBuffer();
		
		IAgentLocation location = (IAgentLocation) agent.getService(IAgentLocation.SERVICE_NAME);
		if (location != null)	{
			buffer.append(location.getRootLocation().toString()).append("\n");			
		}
		else {
			return "No se ha encontrado IAgentLocation";
		}
		
		IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
		if (profileRegistry != null)	{
			buffer.append(profileRegistry.toString());			
		}
		else {
			buffer.append("No se ha encontrado profileRegistry");
		}
		return buffer.toString();
	}
}
