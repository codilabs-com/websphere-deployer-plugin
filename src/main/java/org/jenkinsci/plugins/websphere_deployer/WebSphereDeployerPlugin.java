package org.jenkinsci.plugins.websphere_deployer;

import hudson.EnvVars;
import hudson.Extension;
import org.jenkinsci.Symbol;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import hudson.util.FormValidation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.websphere.services.deployment.Artifact;
import org.jenkinsci.plugins.websphere.services.deployment.DeploymentServiceException;
import org.jenkinsci.plugins.websphere.services.deployment.Server;
import org.jenkinsci.plugins.websphere.services.deployment.WebSphereDeploymentService;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.ibm.icu.text.SimpleDateFormat;

/**
 * A Jenkins plugin for deploying to WebSphere either locally or remotely.
 *
 * @author Greg Peters
 */
public class WebSphereDeployerPlugin extends Builder implements SimpleBuildStep {

	private static String OPERATION_REINSTALL = "1";
    private String ipAddress;
    private String connectorType;
    private String port;
    private String artifacts;
    private String earLevel;
    private String deploymentTimeout;
    private String classLoaderOrder;
    private String classLoaderPolicy;
    private String operations;
    private String context;
    private String installPath;
    private String targets;
    private String applicationName;
    private String virtualHost;
    private String sharedLibName;
    private String edition;
    private boolean fullSynchronization;
    private boolean precompile;
    private boolean reloading;
    private boolean jspReloading;
    private boolean verbose;
    private boolean distribute;
    private boolean rollback;
    private boolean unstableDeploy;
    private WebSphereSecurity security;

    @DataBoundConstructor
    public WebSphereDeployerPlugin(String ipAddress,
                                   String connectorType,
                                   String port,
                                   String installPath,
                                   WebSphereSecurity security,
                                   String artifacts,
                                   String earLevel,
                                   String deploymentTimeout,
                                   String operations,
                                   String context,
                                   String targets,
                                   String applicationName,
                                   String virtualHost,
                                   String sharedLibName,
                                   String edition,
                                   boolean fullSynchronization,
                                   boolean precompile,
                                   boolean reloading,
                                   boolean jspReloading,
                                   boolean verbose,
                                   boolean distribute,
                                   boolean rollback,
                                   boolean unstableDeploy,
                                   String classLoaderPolicy,
                                   String classLoaderOrder) {
    	this.context = context;
    	this.targets = targets;
    	this.installPath = installPath;
        this.ipAddress = ipAddress;        
        this.connectorType = connectorType;
        this.artifacts = artifacts;
        this.port = port;
        this.operations = operations;
        this.earLevel = earLevel;
        this.deploymentTimeout = deploymentTimeout;
        this.edition = edition;
        this.fullSynchronization = fullSynchronization;
        this.precompile = precompile;
        this.reloading = reloading;
        this.jspReloading = jspReloading;
        this.verbose = verbose;
        this.distribute = distribute;
        this.rollback = rollback;
        this.unstableDeploy = unstableDeploy;
        this.security = security;
        this.classLoaderPolicy = classLoaderPolicy;
        this.classLoaderOrder = classLoaderOrder;
        this.applicationName = applicationName;
        this.virtualHost = virtualHost;
        this.sharedLibName = sharedLibName;
    }
    
    public String getEdition() {
    	return edition;
    }
    
    public String getClassLoaderOrder() {
    	return classLoaderOrder;
    }
    
    public String getApplicationName() {
    	return applicationName;
    }
    
    public String getClassLoaderPolicy() {
    	return classLoaderPolicy;
    }
    
    public String getTargets() {
    	return targets;
    }

    public String getEarLevel() {
        return earLevel;
    }

    public WebSphereSecurity getSecurity() {
    	return security;
    }
    
    public boolean isDistribute() {
    	return distribute;
    }
    
    public boolean isFullSynchronization() {
    	return fullSynchronization;
    }
    
    public boolean isPrecompile() {
        return precompile;
    }

    public boolean isReloading() {
        return reloading;
    }
    
    public boolean isJspReloading() {
    	return jspReloading;
    }
    
    public boolean isVerbose() {
    	return verbose;
    }
    
    public boolean isRollback() {
    	return rollback;
    }

    public boolean isUnstableDeploy() {
        return unstableDeploy;
    }

    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getContext() {
    	return context;
    }
    
    public String getInstallPath() {
    	return installPath;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public String getPort() {
        return port;
    }

    public String getArtifacts() {
        return artifacts;
    }

    public String getOperations() {
        return operations;
    }
    
    public String getDeploymentTimeout() {
		return deploymentTimeout;
	}    
    
    public String getVirtualHost() {
    	return virtualHost;
    }
    
    public String getSharedLibName() {
    	return sharedLibName;
    }

    /**
     * Set parameters
     */
    
    
    @DataBoundSetter
    public final void setEdition(String edition) {
    	this.edition = edition;
    }
    
    @DataBoundSetter
    public final void setClassLoaderOrder(String classLoaderOrder) {
    	this.classLoaderOrder = classLoaderOrder;
    }
    
    @DataBoundSetter
    public final void setApplicationName(String applicationName) {
    	this.applicationName = applicationName;
    }
    
    @DataBoundSetter
    public final void setClassLoaderPolicy(String classLoaderPolicy) {
    	this.classLoaderPolicy = classLoaderPolicy;
    }
    
    @DataBoundSetter
    public final void setTargets(String targets) {
    	this.targets = targets;
    }

    @DataBoundSetter
    public final void setEarLevel(String earLevel) {
        this.earLevel = earLevel;
    }

    @DataBoundSetter
    public final void setSecurity(WebSphereSecurity security) {
    	this.security = security;
    }
    
    @DataBoundSetter
    public final void setDistribute(boolean distribute) {
    	this.distribute = distribute;
    }
    
    @DataBoundSetter
    public final void setFullSynchronization(boolean fullSynchronization) {
    	this.fullSynchronization = fullSynchronization;
    }
    
    @DataBoundSetter
    public final void setPrecompile(boolean precompile) {
        this.precompile = precompile;
    }

    @DataBoundSetter
    public final void setReloading(boolean reloading) {
        this.reloading = reloading;
    }
    
    @DataBoundSetter
    public final void setJspReloading(boolean jspReloading) {
    	this.jspReloading = jspReloading;
    }
    
    @DataBoundSetter
    public final void setVerbose(boolean verbose) {
    	this.verbose = verbose;
    }
    
    @DataBoundSetter
    public final void setRollback(boolean rollback) {
    	this.rollback = rollback;
    }

    @DataBoundSetter
    public final void setUnstableDeploy(boolean unstableDeploy) {
        this.unstableDeploy = unstableDeploy;
    }

    @DataBoundSetter
    public final void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    @DataBoundSetter
    public final void setContext(String context) {
    	this.context = context;
    }
    
    @DataBoundSetter
    public final void setInstallPath(String installPath) {
    	this.installPath = installPath;
    }

    @DataBoundSetter
    public final void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    @DataBoundSetter
    public final void setPort(String port) {
        this.port = port;
    }

    @DataBoundSetter
    public final void setArtifacts(String artifacts) {
        this.artifacts = artifacts;
    }

    @DataBoundSetter
    public final void setOperations(String operations) {
        this.operations = operations;
    }
    
    @DataBoundSetter
    public final void setDeploymentTimeout(String deploymentTimeout) {
		this.deploymentTimeout = deploymentTimeout;
	}    
    
    @DataBoundSetter
    public final void setVirtualHost(String virtualHost) {
    	this.virtualHost = virtualHost;
    }
    
    @DataBoundSetter
    public final void setSharedLibName(String sharedLibName) {
    	this.sharedLibName = sharedLibName;
    }

    @Override
    public void perform(Run<?,?> build, FilePath ws, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
    	if(build == null) {
    		throw new IllegalStateException("Build cannot be null");
    	}
        WebSphereDeploymentService service = new WebSphereDeploymentService();
        Artifact artifact = null;
        try {            	
            EnvVars env = build.getEnvironment(listener);
            preInitializeService(listener,service, env);  
            service.connect();                	               
            for(FilePath path:gatherArtifactPaths(ws, build, listener)) {
                artifact = createArtifact(path,listener,service);   
                log(listener,"Artifact is being deployed to virtual host: "+artifact.getVirtualHost());
                stopArtifact(artifact,listener,service);
                if(getOperations().equals(OPERATION_REINSTALL)) {
                    uninstallArtifact(artifact,listener,service);
                    deployArtifact(artifact,listener,service);
                } else { //otherwise update application
                    if(!service.isArtifactInstalled(artifact)) {
                        deployArtifact(artifact, listener, service); //do initial deployment
                    } else {
                        updateArtifact(artifact,listener,service);
                    }
                }
                if(isFullSynchronization()) {
                    service.fullyResynchronizeNodes();
                }
                startArtifact(artifact,listener,service);

                if(rollback) {
                    saveArtifactToRollbackRepository(ws, build, listener, artifact);
                }
            }
        } catch (Exception e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream p = null;
            try {
                p = new PrintStream(out,true,"UTF-8");
            } catch (UnsupportedEncodingException e2) {
                e2.printStackTrace();
            }
            e.printStackTrace(p);
            if(verbose) {
                try {
                    logVerbose(listener,"Error deploying to IBM WebSphere Application Server: "+new String(out.toByteArray(),"UTF-8"));
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
            } else {
                log(listener,"Error deploying to IBM WebSphere Application Server: "+e.getMessage());
            }
            rollbackArtifact(service,ws,build,listener,artifact);
            build.setResult(Result.FAILURE);
        } finally {
            service.disconnect();
        }
    }
    
    private void log(TaskListener listener,String data) {
		listener.getLogger().println(data);
    }
    
    private void logVerbose(TaskListener listener,String data) {
    	if(verbose) {
    		log(listener,data);
    	}
    }
    
    private void rollbackArtifact(WebSphereDeploymentService service, FilePath workspace,Run build,TaskListener listener,Artifact artifact) {
    	if(build == null) {
    		log(listener,"Cannot rollback to previous verions: build is null");
    	}
    	if(artifact == null) {
    		log(listener,"Cannot rollback to previous version: artifact is null");
    		return;
    	}
    	if(workspace == null) {
    		log(listener,"Cannot rollback to previous version: workspace is null");
    		return;
    	}
    	String remote = workspace.getRemote();
    	if(remote == null) {
    		log(listener,"Cannot rollback to previous version: remote path is null");
    		return;
    	}
    	log(listener,"Performing rollback of '"+artifact.getAppName()+"'");    	
    	File installablePath = new File(remote+File.separator+"Rollbacks"+File.separator+artifact.getAppName()+"."+artifact.getTypeName());    	
    	if(installablePath.exists()) {
    		artifact.setSourcePath(installablePath);
    		try {
    			updateArtifact(artifact, listener, service);
    			startArtifact(artifact,listener,service);
    			log(listener,"Rollback of '"+artifact.getAppName()+"' was successful");
    		} catch(Exception e) {
    			e.printStackTrace();
    			log(listener, "Error while trying to rollback to previous version: "+e.getMessage());
    		}
    	} else {
    		log(listener,"WARNING: Artifact doesn't exist rollback repository");
    	}
    }
    
    private void saveArtifactToRollbackRepository(FilePath workspace,Run build,TaskListener listener,Artifact artifact) {
    	listener.getLogger().println("Performing save operations on '" + artifact.getAppName() + "' for future rollbacks");

    	if(workspace == null) {
    		log(listener, "Failed to save rollback to repository: Build workspace is null");
    		throw new IllegalStateException("Failed to save rollback to repository: Build workspace is null");
    	}
    	String remote = workspace.getRemote();
    	if(remote == null) {
    		log(listener,"Failed to save rollback to repository: Build workspace remote path is null");
    		throw new IllegalStateException("Failed to save rollback to repository: Build workspace remote path is null");
    	}
    	File rollbackDir = new File(remote+File.separator+"Rollbacks");
    	createIfNotExists(listener, rollbackDir);
    	logVerbose(listener, "Rollback Path: "+rollbackDir.getAbsolutePath());
    	File destination = new File(rollbackDir,artifact.getAppName()+"."+artifact.getTypeName());
    	if(destination.exists()) {
    		log(listener, "Deleting old rollback version...");	
    		if(!destination.delete()) {
    			log(listener, "Failed to delete old rollback version, permissions?: "+destination.getAbsolutePath());
    			return;
    		}
    	}
    	log(listener, "Saving new rollback version...");
    	if(!artifact.getSourcePath().renameTo(destination)) { 
    		logVerbose(listener, "Failed to save '"+artifact.getAppName() +"' to rollback repository");
    	} else {
    		log(listener,"Saved '"+artifact.getAppName()+"' to rollback repository");
    	}
    }
    
    private void createIfNotExists(TaskListener listener,File directory) {
    	if(directory.exists() || directory.mkdir()) {
    		return;
    	}
    	throw new DeploymentServiceException("Failed to create directory, is write access allowed?: "+directory.getAbsolutePath());
    }

    private void deployArtifact(Artifact artifact,TaskListener listener,WebSphereDeploymentService service) throws Exception {
        listener.getLogger().println("Deploying '" + artifact.getAppName() + "' to IBM WebSphere Application Server");
        service.installArtifact(artifact);
    }

    private void uninstallArtifact(Artifact artifact,TaskListener listener,WebSphereDeploymentService service) throws Exception {
        if(service.isArtifactInstalled(artifact)) {
            listener.getLogger().println("Uninstalling Old Application '"+artifact.getAppName()+"'...");
            service.uninstallArtifact(artifact);
        }
    }

    private void startArtifact(Artifact artifact,TaskListener listener,WebSphereDeploymentService service) throws Exception {
		if(StringUtils.trimToNull(artifact.getEdition()) != null) {
			listener.getLogger().println(artifact.getAppName()+ " will not be started automatically because 'Edition' management was used in the jenkins configuration");
			return;
		}
    	listener.getLogger().println("Starting Application '"+artifact.getAppName()+"'...");
    	try {
    		service.startArtifact(artifact, Integer.parseInt(getDeploymentTimeout()));
    	} catch(NumberFormatException e) {
    		service.startArtifact(artifact);
    	}
    }

    private void stopArtifact(Artifact artifact,TaskListener listener,WebSphereDeploymentService service) throws Exception {
        if(service.isArtifactInstalled(artifact)) {
            listener.getLogger().println("Stopping Existing Application '"+artifact.getAppName()+"'...");
            service.stopArtifact(artifact);
        }
    }
    
    private void updateArtifact(Artifact artifact,TaskListener listener,WebSphereDeploymentService service) throws Exception {
        if(service.isArtifactInstalled(artifact)) {
            listener.getLogger().println("Updating '" + artifact.getAppName() + "' on IBM WebSphere Application Server");
            service.updateArtifact(artifact);
        }
    }      

    private Artifact createArtifact(FilePath path,TaskListener listener,WebSphereDeploymentService service) {
        Artifact artifact = new Artifact();
        if(path.getRemote().endsWith(".ear")) {
            artifact.setType(Artifact.TYPE_EAR);
        } else if(path.getRemote().endsWith(".war")) {
            artifact.setType(Artifact.TYPE_WAR);
        }
        if(StringUtils.trimToNull(context) != null) {
        	artifact.setContext(context);
        }                
        if(StringUtils.trimToNull(virtualHost) == null) {
        	artifact.setVirtualHost("default_host");
        } else {
        	artifact.setVirtualHost(virtualHost);
        }
        artifact.setClassLoaderOrder(classLoaderOrder);
        artifact.setClassLoaderPolicy(classLoaderPolicy);
        artifact.setTargets(targets);
        artifact.setInstallPath(installPath);
        artifact.setJspReloading(reloading);
        artifact.setDistribute(distribute);
        if(StringUtils.trimToNull(edition) != null) {
        	artifact.setEdition(edition);	
        }
        artifact.setPrecompile(isPrecompile());
        artifact.setSourcePath(new File(path.getRemote()));
        if(StringUtils.trimToNull(applicationName) != null) {
        	artifact.setAppName(applicationName);
        } else {
        	artifact.setAppName(getAppName(artifact,service));
        }
        if(artifact.getType() == Artifact.TYPE_WAR) {
            generateEAR(artifact, listener, service);
        }
        return artifact;
    }

    private FilePath[] gatherArtifactPaths(FilePath workspace,Run build,TaskListener listener) throws Exception {
    	if(build == null) {
    		log(listener,"Cannot gather artifact paths: Build is null");
    		throw new IllegalStateException("Cannot gather artifact paths: Build is null");
    	}
    	if(workspace == null) {
    		log(listener,"Cannot gather artifact paths: Build workspace is null");
    		throw new IllegalStateException("Cannot gather artifact paths: Build workspace is null");
    	}
    	FilePath workspaceParent = workspace.getParent();
    	if(workspaceParent == null) {
    		log(listener,"Cannot gather artifact paths: Build workspace's parent folder is null");
    		throw new IllegalStateException("Cannot gather artifact paths: Build workspace's parent folder is null");
    	}
    	String artifacts = getArtifacts();
    	if(artifacts == null) {
    		log(listener,"Cannot gather artifact paths: Artifacts are null");
    		throw new IllegalStateException("Cannot gather artifact paths: Artifacts are null");
    	}
        FilePath[] paths = workspaceParent.list(artifacts);
        if(paths.length == 0) {
            listener.getLogger().println("No deployable artifacts found in path: "+workspaceParent+File.separator+artifacts);
            throw new Exception("No deployable artifacts found!");
        } else {
            listener.getLogger().println("The following artifacts will be deployed in this order...");
            listener.getLogger().println("-------------------------------------------");
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
            for(FilePath path:paths) {
                listener.getLogger().println(path.getRemote()+" Last modified on "+sdf.format(path.lastModified()));
            }
            listener.getLogger().println("-------------------------------------------");
        }
        return paths;
    }

    private void preInitializeService(TaskListener listener,WebSphereDeploymentService service,EnvVars env) throws Exception {
        listener.getLogger().println("Connecting to IBM WebSphere Application Server...");
        service.setVerbose(isVerbose());
        service.setBuildListener(listener);;
        service.setConnectorType(getConnectorType());
        service.setHost(env.expand(getIpAddress()));
        service.setPort(env.expand(getPort()));
        if(security != null) {
        	service.setUsername(env.expand(security.getUsername()));
        	service.setPassword(env.expand(security.getPassword()));
        	service.setKeyStoreLocation(new File(env.expand(security.getClientKeyFile())));
        	service.setKeyStorePassword(env.expand(security.getClientKeyPassword()));
        	service.setTrustStoreLocation(new File(env.expand(security.getClientTrustFile())));
        	service.setTrustStorePassword(env.expand(security.getClientTrustPassword()));
        	service.setTrustAll(security.isTrustAll());
        }
    }

    private String getAppName(Artifact artifact,WebSphereDeploymentService service) {
        if(artifact.getType() == Artifact.TYPE_EAR) {
            return service.getAppName(artifact.getSourcePath().getAbsolutePath());
        } else {
            String filename = artifact.getSourcePath().getName();
            return filename.substring(0,filename.lastIndexOf("."));
        }
    }

    private void generateEAR(Artifact artifact,TaskListener listener,WebSphereDeploymentService service) {
        listener.getLogger().println("Generating EAR For Artifact: "+artifact.getAppName());
        File modified = new File(artifact.getSourcePath().getParent(),artifact.getAppName()+".ear");
        service.generateEAR(artifact, modified, getEarLevel());
        artifact.setSourcePath(modified);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    
    @Extension @Symbol("wasDeploy")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String adminClientPath;
        private String orbClientPath;

        public DescriptorImpl() {
            load();
        }
        
        public FormValidation doLoadTargets(@QueryParameter("ipAddress")String ipAddress,
                @QueryParameter("connectorType")String connectorType,
                @QueryParameter("port")String port,
                @QueryParameter("username")String username,
                @QueryParameter("password")String password,
                @QueryParameter("trustAll")String trustAll) throws IOException, ServletException {
            WebSphereDeploymentService service = new WebSphereDeploymentService();
            try {
                if(!service.isAvailable()) {
                    String destination = "<Jenkins_Root>"+File.separator+"plugins"+File.separator+"websphere-deployer"+File.separator+"WEB-INF"+File.separator+"lib"+File.separator;
                    return FormValidation.warning("Cannot find the required IBM WebSphere Application Server jar files in '"+destination+"'. Please copy them from IBM WebSphere Application Server (see plugin documentation)");
                }
                service.setConnectorType(connectorType);
                service.setHost(ipAddress);
                service.setUsername(username);
                service.setPassword(password);
                service.setPort(port);
                service.setTrustAll(Boolean.valueOf(trustAll));
                service.connect();
                List<Server> servers = service.listServers();
                StringBuffer buffer = new StringBuffer();
                buffer.append("\r\n\r\n");
                for(Server server:servers) {
                	if(buffer.length() > 0) {
                		buffer.append("\r\n");
                	}
                	buffer.append(server.getTarget());
                }
                if(buffer.toString().trim().equals("")) {
                	return FormValidation.warning("No server targets are configured in WebSphere");
                }
                return FormValidation.ok(buffer.toString());
            } catch (Exception e) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PrintStream p = new PrintStream(out,true,"UTF-8");
                e.printStackTrace(p);
                return FormValidation.error("Failed to list targets =>" + new String(out.toByteArray(),"UTF-8"));
            } finally {
                service.disconnect();
            }
        }

        public FormValidation doTestConnection(@QueryParameter("ipAddress")String ipAddress,
                                               @QueryParameter("connectorType")String connectorType,
                                               @QueryParameter("port")String port,
                                               @QueryParameter("username")String username,
                                               @QueryParameter("password")String password,
                                               @QueryParameter("trustAll")String trustAll) throws IOException, ServletException {
            WebSphereDeploymentService service = new WebSphereDeploymentService();
            try {
                if(!service.isAvailable()) {
                    String destination = "<Jenkins_Root>"+File.separator+"plugins"+File.separator+"websphere-deployer"+File.separator+"WEB-INF"+File.separator+"lib"+File.separator;
                    return FormValidation.warning("Cannot find the required IBM WebSphere Application Server jar files in '"+destination+"'. Please copy them from IBM WebSphere Application Server (see plugin documentation)");
                }
                service.setConnectorType(connectorType);
                service.setHost(ipAddress);
                service.setUsername(username);
                service.setPassword(password);
                service.setPort(port);
                service.setTrustAll(Boolean.valueOf(trustAll));
                service.connect();
                return FormValidation.ok("Connection Successful!");
            } catch (Exception e) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PrintStream p = new PrintStream(out,true,"UTF-8");
                e.printStackTrace(p);
                return FormValidation.error("Connection failed from Jenkins to https://"+ipAddress+":"+port+" => " + new String(out.toByteArray(),"UTF-8"));
            } finally {
                service.disconnect();
            }
        }

        public FormValidation doCheckPort(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Select a port");
            if (value.length() > 5)
                return FormValidation.warning("Cannot be greater than 65535");
            return FormValidation.ok();
        }
        
        public FormValidation doCheckApplicationName(@QueryParameter String value) throws IOException, ServletException {
        	if(StringUtils.trimToNull(value) == null) {
        		return FormValidation.warning("This setting is required for rollback support");
        	} else {
        		return FormValidation.ok();
        	}
        }

        public FormValidation doCheckAdminClientPath(@QueryParameter String value)
                throws IOException, ServletException {
            if(!new File(value).exists()) {
                return FormValidation.error("Path '"+value+"' is not found");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckOrbClientPath(@QueryParameter String value)
                throws IOException, ServletException {
            if(!new File(value).exists()) {
                return FormValidation.error("Path '"+value+"' is not found");
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
        	return "Deploy To IBM WebSphere Application Server";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            adminClientPath = formData.getString("adminClientPath");
            orbClientPath = formData.getString("orbClientPath");
            save();
            return super.configure(req,formData);
        }

        public String getAdminClientPath() {
            return adminClientPath;
        }

        public String getOrbClientPath() {
            return orbClientPath;
        }
    }
}

