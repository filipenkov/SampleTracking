package com.atlassian.support.tools.salext;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.license.LicenseHandler;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.sisyphus.SisyphusPatternSourceDecorator;
import com.atlassian.stash.mail.MailHostConfiguration;
import com.atlassian.stash.project.Project;
import com.atlassian.stash.project.ProjectService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryService;
import com.atlassian.stash.server.ApplicationPropertiesService;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PageRequestImpl;
import com.atlassian.support.tools.hercules.ScanItem;
import com.atlassian.support.tools.properties.DefaultPropertyStore;
import com.atlassian.support.tools.properties.MultiValuePropertyStore;
import com.atlassian.support.tools.properties.PropertyStore;
import com.atlassian.support.tools.salext.bundle.BundleManifest;
import com.atlassian.support.tools.salext.bundle.DefaultApplicationFileBundle;
import com.atlassian.support.tools.salext.bundle.WildcardApplicationFileBundle;
import com.atlassian.support.tools.zip.FileSanitizer;
import com.atlassian.templaterenderer.TemplateRenderer;

public class StashApplicationInfo extends AbstractSupportApplicationInfo {
    public static final String STASH_COMMIT_HASH = "stp.properties.stash.commit.hash"; 
	private static final String ZIP_INCLUDE_STASH_LOGS_DESCRIPTION = "stp.zip.include.stash.logs.description";
	private static final String ZIP_INCLUDE_STASH_LOGS = "stp.zip.include.stash.logs";
	private static final String ZIP_INCLUDE_STASH_CFG_DESCRIPTION = "stp.zip.include.stash.cfg.description";
	private static final String ZIP_INCLUDE_STASH_CFG = "stp.zip.include.stash.cfg";

    // Stash API classes.  Who's a good dog(fooder)?
    private final ApplicationLinkService linkService;
    private final PluginAccessor pluginAccessor;
    private final ProjectService projectService;
    private final ApplicationPropertiesService propertiesService;
    private final RepositoryService repositoryService;

    // TODO:  When all products are at SAL 2.7.0, move this and the methods that use it to the abstract parent
    private final LicenseHandler licenseHandler;

    public StashApplicationInfo(ApplicationProperties applicationProperties,
                                I18nResolver i18nResolver,
                                UserManager userManager,
                                TemplateRenderer renderer,
                                ApplicationLinkService linkService,
                                ApplicationPropertiesService propertiesService,
                                LicenseHandler licenseHandler,
                                ProjectService projectService,
                                RepositoryService repositoryService,
                                PluginAccessor pluginAccessor) {
        super(applicationProperties, i18nResolver, userManager, renderer);
        this.licenseHandler = licenseHandler;
        this.linkService = linkService;
        this.pluginAccessor = pluginAccessor;
        this.projectService = projectService;
        this.propertiesService = propertiesService;
        this.repositoryService = repositoryService;
    }

    @Override
    public List<ScanItem> getApplicationLogFilePaths() {
        String logFilePath = applicationProperties.getHomeDirectory() + "/log/atlassian-stash.log"; 

        if (new File(logFilePath).exists()) {
            return Collections.singletonList(ScanItem.createDefaultItem(logFilePath));
        }

        return Collections.emptyList();
    }

    @Override
    public SisyphusPatternSource getPatternSource() throws IOException, ClassNotFoundException {
        SisyphusPatternSourceDecorator source = new SisyphusPatternSourceDecorator();
        source.add(getPatternSourceByURL("https://confluence.atlassian.com/download/attachments/179443532/stash_regex.xml")); 
        return source;
    }

    @Override
    public String getApplicationSEN() {
        return licenseHandler.getSupportEntitlementNumber();
    }

    @Override
    public String getApplicationServerID() {
        return licenseHandler.getServerId();
    }

    @Override
    public String getCreateSupportRequestEmail() {
        return "stash-autosupportrequests@atlassian.com"; 
    }

    @Override
    public PropertyStore loadProperties() {
    		PropertyStore store = super.loadProperties();

    		PropertyStore generalProps = store.addCategory("stp.properties.stash.info");
        generalProps.setValue(APPLICATION_BASE_URL, propertiesService.getBaseUrl().toString());
        generalProps.setValue(APPLICATION_BUILD_NUMBER, propertiesService.getBuildNumber());
        generalProps.setValue(APPLICATION_BUILD_TIMESTAMP, propertiesService.getBuildTimestamp().toString());
        generalProps.setValue(APPLICATION_BUILD_VERSION, propertiesService.getBuildVersion());
        generalProps.setValue(APPLICATION_DISPLAY_NAME, propertiesService.getDisplayName());
        generalProps.setValue(APPLICATION_HOME, propertiesService.getHomeDir().getAbsolutePath());
        generalProps.setValue(APPLICATION_TIME_ZONE, propertiesService.getDefaultTimeZone().getID());
        generalProps.setValue(STASH_COMMIT_HASH, propertiesService.getCommitHash());
        generalProps.setValue(CAPTCHA_MAX_ATTEMPTS, String.valueOf(propertiesService.getMaxCaptchaAttempts()));
        generalProps.setValue(LICENSE_SERVER_ID, getApplicationServerID());
        generalProps.setValue(LICENSE_SEN, getApplicationSEN());

        PropertyStore mailProps = store.addCategory("stp.properties.mail");
        mailProps.setValue(MAIL_SERVER_ADDRESS, propertiesService.getServerEmailAddress());

        MailHostConfiguration config = propertiesService.getMailHostConfiguration();
        if (config == null) {
            mailProps.setValue(MAIL_SERVER_HOSTNAME, i18nResolver.getText("stp.properties.not.configured")); 
            mailProps.setValue(MAIL_SERVER_PORT, i18nResolver.getText("stp.properties.not.configured")); 
            mailProps.setValue(MAIL_USERNAME, i18nResolver.getText("stp.properties.not.configured")); 
            mailProps.setValue(MAIL_USE_TLS, i18nResolver.getText("stp.properties.not.configured")); 
        } else {
            mailProps.setValue(MAIL_SERVER_HOSTNAME, config.getHostname());
            mailProps.setValue(MAIL_SERVER_PORT, String.valueOf(config.getPort()));
            mailProps.setValue(MAIL_USERNAME, config.getUsername());
            mailProps.setValue(MAIL_USE_TLS, String.valueOf(config.isUseTls()));
        }

        PropertyStore dbProps = store.addCategory("stp.properties.db.config");
        dbProps.setValue(DB_DRIVER_NAME, propertiesService.getJdbcDriver());
        dbProps.setValue(DB_CONNECTION_URL, propertiesService.getJdbcUrl());

        PropertyStore linkProps = store.addCategory("stp.properties.links");
        buildApplicationLinkInformation(linkProps);
        
        PropertyStore pluginProps = new MultiValuePropertyStore();
        store.addCategory("stp.properties.plugins", pluginProps);
        buildPluginInformation(pluginProps);

        PropertyStore usageProps = store.addCategory("stp.properties.usage");
        buildUsageInformation(usageProps);
        
        return store;
    }

    @Override
    public String getMailQueueURL(HttpServletRequest request) {
        return null;
    }

    @Override
    public String getMailServerConfigurationURL(HttpServletRequest request) {
        return getBaseURL(request) + "/admin/mail-server"; 
    }

    @Override
    public FileSanitizer getFileSanitizer() {
        return new FileSanitizer(FILE_PATTERNS);
    }

    @Override
    public void initServletInfo(ServletConfig config) {
        super.initServletInfo(config);
        String webInfClassesDir = getServletContextPath("WEB-INF/classes"); 
        this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.APPLICATION_CONFIG, ZIP_INCLUDE_STASH_CFG, 
                ZIP_INCLUDE_STASH_CFG_DESCRIPTION));  
        this.applicationInfoBundles.add(new WildcardApplicationFileBundle(BundleManifest.TOMCAT_CONFIG, ZIP_INCLUDE_TOMCAT_CONF, 
                ZIP_INCLUDE_TOMCAT_CONF_DESCRIPTION, findTomcatFileOrDirectory("conf"),  
                "^.*\\.(xml|properties|policy)$")); 
        this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.CACHE_CONFIG, ZIP_INCLUDE_CACHE_CFG, 
                ZIP_INCLUDE_CACHE_CFG_DESCRIPTION));  
        this.applicationInfoBundles.add(new WildcardApplicationFileBundle(BundleManifest.APPLICATION_LOGS,
                ZIP_INCLUDE_STASH_LOGS, ZIP_INCLUDE_STASH_LOGS_DESCRIPTION,  
                getApplicationHome() + "/log", "^.*\\.log.*"));  
        this.applicationInfoBundles.add(new WildcardApplicationFileBundle(BundleManifest.TOMCAT_LOGS, ZIP_INCLUDE_TOMCAT_LOGS, 
                ZIP_INCLUDE_TOMCAT_LOGS_DESCRIPTION, findTomcatFileOrDirectory("logs"), "^.*\\.(log|out)$"));   
    }

    @Override
    public String getApplicationLogDir() {
        return getApplicationHome() + "/log"; 
    }

    private void buildApplicationLinkInformation(PropertyStore linkProps) {
        for (ApplicationLink link : linkService.getApplicationLinks()) {
        		PropertyStore linkStore = linkProps.addCategory(LINK);

        		linkStore.setValue(LINK_NAME, link.getName());
        		linkStore.setValue(LINK_PRIMARY, String.valueOf(link.isPrimary()));
        		linkStore.setValue(LINK_TYPE, link.getType().getI18nKey());
        		linkStore.setValue(LINK_DISPLAY_URL, link.getDisplayUrl().toString());
        		linkStore.setValue(LINK_RPC_URL, link.getRpcUrl().toString());
        }
    }

    private void buildPluginInformation(PropertyStore pluginProps) {
        for (Plugin plugin : pluginAccessor.getPlugins()) {
	        	PluginInformation pluginInformation = plugin.getPluginInformation();
			PropertyStore pluginStore = pluginProps.addCategory(PLUGINS_PLUGIN);

			pluginStore.setValue(PLUGIN_KEY, plugin.getKey());
	        	pluginStore.setValue(PLUGIN_VERSION, pluginInformation.getVersion()); 
	        	pluginStore.setValue(PLUGIN_VENDOR, pluginInformation.getVendorName()); 
	        	pluginStore.setValue(PLUGIN_STATUS, plugin.getPluginState().toString()); 
	        	pluginStore.setValue(PLUGIN_VENDOR_URL, pluginInformation.getVendorUrl()); 
	        	pluginStore.setValue(PLUGIN_FRAMEWORK_VERSION, String.valueOf(plugin.getPluginsVersion())); 
		}
    }

    private void buildUsageInformation(PropertyStore usageProps) {
        for (String key : projectService.findAllProjectKeys()) {
            Project project = projectService.findByKey(key);
            if (project == null) {
                //Should be impossible, since we just got the keys, but handle it just in case.
                continue;
            }

            PropertyStore projectStore = usageProps.addCategory(PROJECT);
            projectStore.setValue(PROJECT_DESCRIPTION, project.getDescription());
            projectStore.setValue(PROJECT_NAME, project.getName());
            projectStore.setValue(PROJECT_KEY, project.getKey());

            PageRequest request = new PageRequestImpl(0, 1000);
            int repositoryNumber = 1;
            PropertyStore repositoriesStore = projectStore.addCategory(REPOSITORIES);
            for (Page<? extends Repository> page = repositoryService.findRepositoriesByProjectKey(key, request);
                 /*No break condition*/;
                 page = repositoryService.findRepositoriesByProjectKey(key, page.getNextPageRequest())) {

                for (Repository repository : page.getValues()) {
                		PropertyStore repositoryStore = repositoriesStore.addCategory(REPOSITORIES_REPOSITORY);
                    repositoryStore.setValue(REPOSITORIES_REPOSITORY_NAME, repository.getName());
                    repositoryStore.setValue(REPOSITORIES_REPOSITORY_TYPE, repository.getScmType().getShortName());
                    repositoryStore.setValue(REPOSITORIES_REPOSITORY_SIZE, computeSize(repository));
                    repositoryStore.setValue(REPOSITORIES_REPOSITORY_SLUG, repository.getSlug());
                    repositoryStore.setValue(REPOSITORIES_REPOSITORY_STATE, repository.getState().toString());
                    repositoryStore.setValue(REPOSITORIES_REPOSITORY_STATUS_MESSAGE, repository.getStatusMessage());
                }

                if (page.getIsLastPage()) {
                    break;
                }
            }
        }
    }

    private String computeSize(Repository repository) {

        long bytes = 0;
        bytes = repositoryService.getRepositorySize(repository);

        long kilobytes = bytes / 1024;
        if (kilobytes > 1024) {
            long megabytes = kilobytes / 1024;

            return megabytes + "MB"; 
        }
        return kilobytes + "KB"; 
    }
}
