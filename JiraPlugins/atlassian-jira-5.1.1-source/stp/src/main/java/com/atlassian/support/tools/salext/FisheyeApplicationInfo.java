package com.atlassian.support.tools.salext;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.crowd.util.build.BuildUtils;
import com.atlassian.crucible.spi.data.RepositoryData;
import com.atlassian.fisheye.spi.data.RepositoryDataFE;
import com.atlassian.modzdetector.ModzRegistryException;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.sisyphus.SisyphusPatternSourceDecorator;
import com.atlassian.support.tools.hercules.ScanItem;
import com.atlassian.support.tools.properties.PropertyStore;
import com.atlassian.support.tools.salext.bundle.BundleManifest;
import com.atlassian.support.tools.salext.bundle.DefaultApplicationFileBundle;
import com.atlassian.support.tools.salext.bundle.ListApplicationFileBundle;
import com.atlassian.support.tools.salext.bundle.WildcardApplicationFileBundle;
import com.atlassian.support.tools.salext.mail.AbstractMailUtility;
import com.atlassian.support.tools.zip.FileSanitizer;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.cenqua.crucible.CrucibleVersionInfo;
import com.cenqua.crucible.hibernate.DBControlFactoryImpl;
import com.cenqua.crucible.hibernate.DatabaseConfig;
import com.cenqua.fisheye.AppConfig;
import com.cenqua.fisheye.FisheyeVersionInfo;
import com.cenqua.fisheye.config1.ConfigDocument;
import com.cenqua.fisheye.config1.CrowdAuthConfigType;
import com.cenqua.fisheye.config1.CustomSecurityConfigType;
import com.cenqua.fisheye.config1.HostauthConfigType;
import com.cenqua.fisheye.config1.LdapConfigType;
import com.cenqua.fisheye.config1.ResourcesType;
import com.cenqua.fisheye.config1.SvnConfigType;
import com.cenqua.fisheye.license.LicenseInfo;
import com.cenqua.fisheye.logging.Logs;
import com.cenqua.fisheye.support.ModzDetectorCache;
import com.cenqua.fisheye.util.PropertiesUtil;
import com.cenqua.fisheye.web.admin.actions.plugins.PluginDataFactory;
import com.cenqua.fisheye.web.admin.actions.plugins.PluginDataFactory.PluginData;

public class FisheyeApplicationInfo extends AbstractSupportApplicationInfo
{
	private static final String FISHEYE_AJP_USER_MANAGEMENT = "stp.properties.fisheye.ajp.user.management";
	private static final String FISHEYE_BUILTIN_SIGNUP_ENABLED = "stp.properties.fisheye.builtin.signup.enabled";
	private static final String FISHEYE_BUILTIN_USER_MANAGEMENT = "stp.properties.fisheye.builtin.user.management";
	private static final String FISHEYE_CROWD_NAME = "stp.properties.fisheye.crowd.name";
	private static final String FISHEYE_CROWD_SSL_ENABLED = "stp.properties.fisheye.crowd.ssl.enabled";
	private static final String FISHEYE_CROWD_URL = "stp.properties.fisheye.crowd.url";
	private static final String FISHEYE_CROWD_USER_MANAGEMENT = "stp.properties.fisheye.crowd.user.management";
	private static final String FISHEYE_CROWD_VERSION = "stp.properties.fisheye.crowd.version";
	private static final String FISHEYE_CUSTOM_USER_MANAGEMENT = "stp.properties.fisheye.custom.user.management";
	private static final String FISHEYE_HOST_USER_MANAGEMENT = "stp.properties.fisheye.host.user.management";
	private static final String FISHEYE_LDAP_USER_MANAGEMENT = "stp.properties.fisheye.ldap.user.management";
	private static final String HERCULES_SCANITEM_FECRU_DEBUG_LOG = "stp.hercules.scanitem.fecru.debugLog";
	private static final String HERCULES_SCANITEM_FECRU_ERROR_LOG = "stp.hercules.scanitem.fecru.errorLog";
	private static final String HERCULES_SCANITEM_FISHEYE_LOG = "stp.hercules.scanitem.fisheye.log";
	private static final String ZIP_INCLUDE_FECRU_CFG_DESCRIPTION = "stp.zip.include.fecru.cfg.description";
	private static final String ZIP_INCLUDE_FECRU_LOGS = "stp.zip.include.fecru.logs";
	private static final String ZIP_INCLUDE_FECRU_LOGS_DESCRIPTION = "stp.zip.include.fecru.logs.description";
	private static final String ZIP_INCLUDE_FECRU_OUT = "stp.zip.include.fecru.out";
	private static final String ZIP_INCLUDE_FECRU_OUT_DESCRIPTION = "stp.zip.include.fecru.out.description";
	private static final String ZIP_INCLUDE_FECRU_PLUGIN_CFG = "stp.zip.include.fecru.plugin.cfg";
	private static final String ZIP_INCLUDE_FECRU_PLUGIN_CFG_DESCRIPTION = "stp.zip.include.fecru.plugin.cfg.description";
	private static final String ZIP_INCLUDE_FECRU_PLUGINSTATE_PROPERTIES = "stp.zip.include.fecru.pluginstate.properties";
	private static final String ZIP_INCLUDE_FECRU_PLUGINSTATE_PROPERTIES_DESCRIPTION = "stp.zip.include.fecru.pluginstate.properties.description";
	public static final String ZIP_INCLUDE_FECRU_CFG = "stp.zip.include.fecru.cfg";
	
	private static final Logger log = Logger.getLogger(AbstractMailUtility.class);
	
	// additional i18n keys required for Fisheye-specific features
	private static final String API_ENABLED = "stp.properties.fisheye.api.enabled"; 
	private static final String AUTO_ADD_USERS = "stp.properties.fisheye.auth.users.auto.add"; 
	private static final String CACHE_TTL = "stp.properties.fisheye.cache.ttl"; 
	private static final String CRUCIBLE_VERSION = "stp.properties.crucible.version"; 
	private static final String CUSTOM_SECURITY_CLASS = "stp.properties.fisheye.auth.custom.classname"; 
	private static final String CUSTOM_SECURITY_PROPERTIES = "stp.properties.fisheye.auth.custom.properties"; 
	private static final String DEBUG_ENABLED = "stp.properties.fisheye.debug.enabled"; 
	private static final String FISHEYE_BUILD = "stp.properties.fisheye.build"; 
	private static final String FISHEYE_INSTANCE_DIRECTORY = "stp.properties.fisheye.instance.dir"; 
	private static final String FISHEYE_VERSION = "stp.properties.fisheye.version"; 
	private static final String HOST_AUTH_DOMAIN_SERVICE = "stp.properties.fisheye.auth.domain.service"; 
	private static final String HOST_AUTH_REQUIRED_GROUP = "stp.properties.fisheye.auth.required.group"; 
	private static final String HTTP_BIND = "stp.properties.fisheye.http.bind"; 
	private static final String HTTP_CONTEXT = "stp.properties.fisheye.http.context"; 
	private static final String INCREMENTAL_THREADS_MAX = "stp.properties.fisheye.threads.incremental.max"; 
	private static final String INCREMENTAL_THREADS_MIN = "stp.properties.fisheye.threads.incremental.min"; 
	private static final String INITIAL_THREADS_MAX = "stp.properties.fisheye.threads.initial.max"; 
	private static final String INITIAL_THREADS_MIN = "stp.properties.fisheye.threads.initial.min"; 
	private static final String LDAP_BASE_DN = "stp.properties.fisheye.auth.ldap.base.dn"; 
	private static final String LDAP_BIND_USER = "stp.properties.fisheye.auth.ldap.initial.bind.user"; 
	private static final String LDAP_DISPLAY_NAME_ATTRIBUTE = "stp.properties.fisheye.auth.ldap.display.name.attribute"; 
	private static final String LDAP_EMAIL_ATTRIBUTE = "stp.properties.fisheye.auth.ldap.email.attribute"; 
	private static final String LDAP_SYNC_PERIOD = "stp.properties.fisheye.auth.ldap.sync.period"; 
	private static final String LDAP_UID_ATTRIBUTE = "stp.properties.fisheye.auth.ldap.uid.attribute"; 
	private static final String LDAP_URL = "stp.properties.fisheye.auth.ldap.url"; 
	private static final String LDAP_USER_FILTER = "stp.properties.fisheye.auth.ldap.user.filter"; 
	private static final String LICENSE_DESCRIPTION_CRUCIBLE = "stp.properties.crucible.license.description"; 
	private static final String MAX_CRUCIBLE_USERS = "stp.properties.crucible.max.users"; 
	private static final String MAX_FE_USERS = "stp.properties.fisheye.fisheye.max.users"; 
	private static final String P4_CLIENT = "stp.properties.fisheye.p4.client"; 
	private static final String SITE_CONTEXT = "stp.properties.fisheye.site.context"; 
	private static final String SITE_PROXY_HOST = "stp.properties.fisheye.site.proxy.host"; 
	private static final String SITE_PROXY_PORT = "stp.properties.fisheye.site.proxy.port"; 
	private static final String SITE_PROXY_SCHEME = "stp.properties.fisheye.site.proxy.scheme"; 
	private static final String SITE_URL = "stp.properties.fisheye.site.url"; 
	private static final String SVN_CLIENT_JAR = "stp.properties.fisheye.svn.client.jar"; 
	private static final String SVN_CLIENT_LIB = "stp.properties.fisheye.svn.client.libs"; 
	private static final String USER_MGMT_TYPE = "stp.properties.user.management.type"; 
	
	
	public static final Map<String, List<Pattern>> FILE_PATTERNS;


	static
	{
		Map<String,List<Pattern>> map = new HashMap<String, List<Pattern>>();
		map.put("config.xml", Collections.unmodifiableList(Arrays.asList( 
				Pattern.compile("password%3D(.+?)%0A"), // UAL properties 
				Pattern.compile("password=[\"]([^\"]+?)\""), // catches keystore-password and truststore-password too 
				Pattern.compile("initial-secret=\"(.+?)\""), // LDAP auth 
				Pattern.compile("application.password=(.+?)$"), // crowd config 
				Pattern.compile("<publicKey>(.*?)</publicKey>"), // trusted apps 
	            Pattern.compile("<privateKey>(.*?)</privateKey>") // trusted apps				 
	    )));
		FILE_PATTERNS = Collections.unmodifiableMap(map);
	}
	
	private ConfigDocument.Config cfg;
	
	private PluginDataFactory pluginDataFactory;

    private ModzDetectorCache modz = new ModzDetectorCache();
    
    private DatabaseConfig dbConfig = null;

	private DBControlFactoryImpl factory;
	
	private com.atlassian.crucible.spi.services.RepositoryService crucibleRepositoryService;
	private com.atlassian.fisheye.spi.services.RepositoryService feRepositoryService;

	public FisheyeApplicationInfo(ApplicationProperties applicationProperties, I18nResolver i18nResolver, UserManager userManager, TemplateRenderer renderer, PluginAccessor pluginAccessor, com.atlassian.crucible.spi.services.RepositoryService crucibleRepositoryService, com.atlassian.fisheye.spi.services.RepositoryService feRepositoryService)
	{
		super(applicationProperties, i18nResolver, userManager, renderer);
		this.pluginDataFactory = new PluginDataFactory(pluginAccessor);
		this.factory =  new DBControlFactoryImpl();

		// TODO: Investigate replacing these with injected classes
		this.cfg = AppConfig.getsConfig().getConfig();
        this.dbConfig = new DatabaseConfig(this.factory.getCurrentControl().getInfo().getConnectionInfo());
        this.crucibleRepositoryService = crucibleRepositoryService;
        this.feRepositoryService = feRepositoryService;
	}

	@Override
	public void initServletInfo(ServletConfig config)
	{
		super.initServletInfo(config);
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.APPLICATION_CONFIG, ZIP_INCLUDE_FECRU_CFG, ZIP_INCLUDE_FECRU_CFG_DESCRIPTION));   
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle(BundleManifest.PLUGIN_CONFIG, ZIP_INCLUDE_FECRU_PLUGIN_CFG, ZIP_INCLUDE_FECRU_PLUGIN_CFG_DESCRIPTION, AppConfig.getInstanceDir().getAbsolutePath() + "var/plugins/config" ,  ".*\\.config"));    
		this.applicationInfoBundles.add(new ListApplicationFileBundle(BundleManifest.MODZ, ZIP_INCLUDE_MODZ, ZIP_INCLUDE_MODZ_DESCRIPTION, null, getModifiedFiles()));  
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle(BundleManifest.APPLICATION_LOGS, ZIP_INCLUDE_FECRU_LOGS, ZIP_INCLUDE_FECRU_LOGS_DESCRIPTION, getApplicationLogDir(),  "^fisheye.*\\.log.*"));   
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.FECRU_OUT, ZIP_INCLUDE_FECRU_OUT, ZIP_INCLUDE_FECRU_OUT_DESCRIPTION));   
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.FECRU_PLUGIN_STATE, ZIP_INCLUDE_FECRU_PLUGINSTATE_PROPERTIES, ZIP_INCLUDE_FECRU_PLUGINSTATE_PROPERTIES_DESCRIPTION));   
	}
	
	@Override
	public SisyphusPatternSource getPatternSource() throws IOException, ClassNotFoundException, MalformedURLException
	{
		SisyphusPatternSourceDecorator source = new SisyphusPatternSourceDecorator();
		source.add(getPatternSourceByURL("https://confluence.atlassian.com/download/attachments/179443532/fisheye_regex.xml")); 
		source.add(getPatternSourceByURL("https://confluence.atlassian.com/download/attachments/179443532/crucible_regex.xml")); 
		return source;
	}

	@Override
	public List<ScanItem> getApplicationLogFilePaths() 
	{
		List<ScanItem> logFilePaths = new ArrayList<ScanItem>();

		final String mostRecentErrorLog = mostRecentFile(AppConfig.getLogDir().listFiles((FilenameFilter) new RegexFileFilter("^fisheye-error.*\\.log.*"))); 
		if(!StringUtils.isBlank(mostRecentErrorLog))
			logFilePaths.add(new ScanItem(HERCULES_SCANITEM_FECRU_ERROR_LOG,  mostRecentErrorLog)); 
		
		final String mostRecentDebugLog = mostRecentFile(AppConfig.getLogDir().listFiles((FilenameFilter) new RegexFileFilter("^fisheye-debug.*\\.log	.*"))); 
		if(!StringUtils.isBlank(mostRecentDebugLog))
			logFilePaths.add(new ScanItem(HERCULES_SCANITEM_FECRU_DEBUG_LOG, mostRecentDebugLog)); 
		
		final String fisheyeLog = AppConfig.getInstanceDir() + "/var/log/fisheye.out"; 
		if(new File(fisheyeLog).exists())
			logFilePaths.add(new ScanItem(HERCULES_SCANITEM_FISHEYE_LOG, fisheyeLog)); 
		
		return logFilePaths;
	}
	
	private String mostRecentFile(File[] errorLogs)
	{
		String mostRecentFile =	null;
		long lastModified = 0;
		
		for (File file : errorLogs) {
			if (file.lastModified() > lastModified) {
				try
				{
					mostRecentFile = file.getCanonicalPath();
					lastModified = file.lastModified();
				}
				catch(IOException e)
				{
					log.error("Error retrieving canonical path for file "+file.getPath(), e); 
				}
			}
		}
		
		return mostRecentFile;
	}

	@Override
	public String getCreateSupportRequestEmail()
	{
		return "fisheye-autosupportrequests@atlassian.com"; 
	}

	@Override
	public String getApplicationSEN()
	{
		LicenseInfo productLicense = AppConfig.getsConfig().getLicense();
		if(productLicense.isFishEye())
			return productLicense.getFisheyeLicense().getSupportEntitlementNumber();
		else
			return productLicense.getCrucibleLicense().getSupportEntitlementNumber();
	}

	@Override
	public String getApplicationServerID()
	{
		LicenseInfo productLicense = AppConfig.getsConfig().getLicense();
		if(productLicense.isFishEye())
			return productLicense.getFisheyeLicense().getServerId();
		else
			return productLicense.getCrucibleLicense().getServerId();
	}

	@Override
	public PropertyStore loadProperties()
	{
		PropertyStore store = super.loadProperties();
		
		PropertyStore appProps = store.addCategory(APPLICATION_PROPERTIES);
		appProps.setValue(APPLICATION_HOME, AppConfig.getAppHome().getAbsolutePath());
		appProps.setValue(FISHEYE_INSTANCE_DIRECTORY, AppConfig.getInstanceDir().getAbsolutePath());

		FisheyeVersionInfo fisheyeVersionInfo = new FisheyeVersionInfo();
		appProps.setValue(FISHEYE_VERSION,fisheyeVersionInfo.getReleaseNum());
		appProps.setValue(FISHEYE_BUILD, fisheyeVersionInfo.getBuildNumber());
		appProps.setValue(CRUCIBLE_VERSION,new CrucibleVersionInfo().getReleaseNum());
		
		appProps.setValue(DEBUG_ENABLED, String.valueOf(Logs.DEBUG_LOG.isDebugEnabled()));
		appProps.setValue(API_ENABLED, String.valueOf(AppConfig.getsConfig().isApiEnabled()));

		if (this.cfg.getWebServer() != null ) {
			if (this.cfg.getWebServer().isSetSiteUrl()) appProps.setValue(SITE_URL, this.cfg.getWebServer().getSiteUrl());
			
			if (this.cfg.getWebServer().isSetContext()) appProps.setValue(SITE_CONTEXT, this.cfg.getWebServer().getContext());
			
			if (this.cfg.getWebServer().isSetHttp()) {
				if (this.cfg.getWebServer().getHttp().isSetContext()) appProps.setValue(HTTP_CONTEXT, this.cfg.getWebServer().getHttp().getContext());
				appProps.setValue(HTTP_BIND, this.cfg.getWebServer().getHttp().getBind());
				
				if (this.cfg.getWebServer().getHttp().isSetProxyHost()) appProps.setValue(SITE_PROXY_HOST, String.valueOf(this.cfg.getWebServer().getHttp().getProxyHost()));
				if (this.cfg.getWebServer().getHttp().isSetProxyPort()) appProps.setValue(SITE_PROXY_PORT, String.valueOf(this.cfg.getWebServer().getHttp().getProxyPort()));
				if (this.cfg.getWebServer().getHttp().isSetProxyScheme()) appProps.setValue(SITE_PROXY_SCHEME, this.cfg.getWebServer().getHttp().getProxyScheme());
			}
		}
		
		PropertyStore userMgmtProps = store.addCategory(USER_MANAGEMENT);
		if (this.cfg.getSecurity().isSetCrowd()) {
			CrowdAuthConfigType crowdCfg = this.cfg.getSecurity().getCrowd();
			userMgmtProps.setValue(USER_MGMT_TYPE, FISHEYE_CROWD_USER_MANAGEMENT); 

			userMgmtProps.setValue(FISHEYE_CROWD_NAME, PropertiesUtil.loadFromString(this.cfg.getSecurity().getCrowd().getCrowdProperties()).getProperty("application.name"));  
			userMgmtProps.setValue(FISHEYE_CROWD_URL, PropertiesUtil.loadFromString(this.cfg.getSecurity().getCrowd().getCrowdProperties()).getProperty("crowd.server.url"));  
			userMgmtProps.setValue(FISHEYE_CROWD_VERSION, BuildUtils.BUILD_VERSION); 
			userMgmtProps.setValue(AUTO_ADD_USERS, String.valueOf(crowdCfg.getAutoAdd()));
			userMgmtProps.setValue(FISHEYE_CROWD_SSL_ENABLED, String.valueOf(crowdCfg.isSetSsoEnabled())); 

			if (crowdCfg.isSetResync()) {
				userMgmtProps.setValue(LDAP_SYNC_PERIOD, crowdCfg.getResyncPeriod());
			}
			else {
				userMgmtProps.setValue(LDAP_SYNC_PERIOD, String.valueOf(crowdCfg.getResync()));
			}
		}

		if (this.cfg.getSecurity().isSetBuiltIn()) {
			userMgmtProps.setValue(USER_MGMT_TYPE, getText(FISHEYE_BUILTIN_USER_MANAGEMENT)); 
			userMgmtProps.setValue(FISHEYE_BUILTIN_SIGNUP_ENABLED, String.valueOf(this.cfg.getSecurity().getBuiltIn().getSignup().getEnabled())); 
		}
		
		if (this.cfg.getSecurity().isSetLdap()) {
			LdapConfigType ldapCfg = this.cfg.getSecurity().getLdap();
			userMgmtProps.setValue(USER_MGMT_TYPE, FISHEYE_LDAP_USER_MANAGEMENT); 
			userMgmtProps.setValue(LDAP_URL, ldapCfg.getUrl());
			userMgmtProps.setValue(LDAP_BASE_DN, ldapCfg.getBaseDn());
			userMgmtProps.setValue(LDAP_USER_FILTER, ldapCfg.getFilter());
			userMgmtProps.setValue(LDAP_UID_ATTRIBUTE, ldapCfg.getUidAttr());
			userMgmtProps.setValue(LDAP_EMAIL_ATTRIBUTE, ldapCfg.getEmailAttr());
			userMgmtProps.setValue(LDAP_DISPLAY_NAME_ATTRIBUTE, ldapCfg.isSetDisplaynameAttr() ? ldapCfg.getDisplaynameAttr() : NOT_SET); 
			userMgmtProps.setValue(CACHE_TTL, ldapCfg.getPositiveCacheTtl());
			userMgmtProps.setValue(AUTO_ADD_USERS, String.valueOf(ldapCfg.getAutoAdd()));
			userMgmtProps.setValue(LDAP_BIND_USER, ldapCfg.isSetInitialDn() ? ldapCfg.getInitialDn() : NOT_SET); 
			userMgmtProps.setValue(LDAP_SYNC_PERIOD, ldapCfg.getResyncPeriod());
		}
		
		if (this.cfg.getSecurity().isSetCustom() && this.cfg.getSecurity().getCustom() != null) {
			CustomSecurityConfigType customCfg = this.cfg.getSecurity().getCustom();
			userMgmtProps.setValue(USER_MGMT_TYPE, FISHEYE_CUSTOM_USER_MANAGEMENT); 
			userMgmtProps.setValue(CUSTOM_SECURITY_CLASS, customCfg.getClassname());
			userMgmtProps.setValue(AUTO_ADD_USERS, String.valueOf(customCfg.getAutoAdd()));
			userMgmtProps.setValue(CACHE_TTL, customCfg.getPositiveCacheTtl());
			userMgmtProps.setValue(CUSTOM_SECURITY_PROPERTIES, customCfg.getProperties());
		}
		
		if (this.cfg.getSecurity().isSetAjp() && this.cfg.getSecurity().getCustom() != null) {
			userMgmtProps.setValue(USER_MGMT_TYPE, FISHEYE_AJP_USER_MANAGEMENT); 
			CustomSecurityConfigType ajpCfg = this.cfg.getSecurity().getCustom();
			userMgmtProps.setValue(AUTO_ADD_USERS, String.valueOf(ajpCfg.getAutoAdd()));
			userMgmtProps.setValue(CACHE_TTL, ajpCfg.getPositiveCacheTtl());
		}
		
		if (this.cfg.getSecurity().isSetHostAuth() &&  this.cfg.getSecurity().getHostAuth() != null) {
			HostauthConfigType hostCfg = this.cfg.getSecurity().getHostAuth();
			userMgmtProps.setValue(USER_MGMT_TYPE, FISHEYE_HOST_USER_MANAGEMENT); 
			userMgmtProps.setValue(HOST_AUTH_REQUIRED_GROUP, hostCfg.getRequiredGroup());
			userMgmtProps.setValue(HOST_AUTH_DOMAIN_SERVICE, hostCfg.getDomain());
			userMgmtProps.setValue(AUTO_ADD_USERS, String.valueOf(hostCfg.getAutoAdd()));
			userMgmtProps.setValue(CACHE_TTL, hostCfg.getPositiveCacheTtl());
		}
		
		// License Information
		PropertyStore licenseProps = store.addCategory(LICENSE);
		LicenseInfo productLicense = AppConfig.getsConfig().getLicense();
		
		if (productLicense.isCrucibleOnly()) {
			licenseProps.setValue(LICENSE_SEN, productLicense.getCrucibleLicense().getSupportEntitlementNumber());
			licenseProps.setValue(LICENSE_SERVER_ID, productLicense.getCrucibleLicense().getServerId());
		}
		else if (productLicense.isFishEye()) {
			licenseProps.setValue(LICENSE_SEN, productLicense.getFisheyeLicense().getSupportEntitlementNumber());
			licenseProps.setValue(LICENSE_SERVER_ID, productLicense.getFisheyeLicense().getServerId());
			licenseProps.setValue(LICENSE_EXPIRES, productLicense.getSoftExpiryValue().toString());
			String upgradesMaintenence = ""; 
			if (productLicense.getLatestBuildAllowedValue() != null) {
				upgradesMaintenence = this.getText(LICENSE_EXPIRES) + " " + productLicense.getLatestBuildAllowedValue().toString(); 
			} else {
				upgradesMaintenence = this.getText(LICENSE_NEVER_EXPIRES); 
			}
			licenseProps.setValue(LICENSE_SUPPORT_PERIOD, upgradesMaintenence);
		}
		
		licenseProps.setValue(LICENSE_PRODUCT, productLicense.getProductName());
		licenseProps.setValue(LICENSE_OWNER, productLicense.getOwner());
		licenseProps.setValue(LICENSE_DESCRIPTION, productLicense.getDescription());
		
		
        if (productLicense.getCrucibleLicense() != null) {
            String maxCRUUsers = productLicense.getCrucibleLicense().isUnlimitedNumberOfUsers() ? LICENSE_UNLIMITED_USERS : "" + productLicense.getCrucibleLicense().getMaximumNumberOfUsers();  
            licenseProps.setValue(MAX_CRUCIBLE_USERS, maxCRUUsers);
            licenseProps.setValue(LICENSE_DESCRIPTION_CRUCIBLE, productLicense.getCrucibleLicense().getDescription());
        }

        licenseProps.setValue(LICENSE_ORGANISATION, productLicense.getOwnerStatement());
        
        
        if (productLicense.getFisheyeLicense() != null) {
            String maxFEUsers = productLicense.getFisheyeLicense().isUnlimitedNumberOfUsers() ? LICENSE_UNLIMITED_USERS : "" + productLicense.getFisheyeLicense().getMaximumNumberOfUsers();  
            licenseProps.setValue(MAX_FE_USERS, maxFEUsers);
        }
        
        Collection<PluginData> pluginData = this.pluginDataFactory.getPluginData(null, null);
        PropertyStore pluginsStore = store.addCategory("stp.properties.plugins");

		PropertyStore pluginProperties = store.addCategory(AbstractSupportApplicationInfo.ENABLED_PLUGINS);
		for (PluginData plugin : pluginData) 
		{
			PropertyStore pluginStore = pluginProperties.addCategory(PLUGINS_PLUGIN);

			pluginStore.setValue(PLUGIN_KEY, plugin.getKey());
	        	pluginStore.setValue(PLUGIN_NAME, plugin.getName());  // this is important for the SysInfo page 
	        	pluginStore.setValue(PLUGIN_VERSION, plugin.getVersion()); 
	        	pluginStore.setValue(PLUGIN_VENDOR, plugin.getVendor()); 
	        	pluginStore.setValue(PLUGIN_STATUS, plugin.getState().toString()); 
		}
		
		// Database Settings
		PropertyStore dbProps = store.addCategory(DB);
        dbProps.setValue(DB_NAME, this.dbConfig.getType().getDisplayName());
        dbProps.setValue(DB_DRIVER_NAME, this.dbConfig.getType().getDriver());
        dbProps.setValue(DB_CONNECTION_URL, this.dbConfig.getJdbcURL());
        dbProps.setValue(DB_VERSION, this.factory.getCurrentControl().getInfo().currentVersion().toString());

// FIXME:  The required org.hibernate.cfg classes are not exposed.  We will have to find another means to get this from inside the OSGI sandbox.
//        Configuration configuration = Config.getConfig(this.dbConfig);
//		dbProps.put("Database Connection Pool (min)", configuration.getProperty(Environment.C3P0_MIN_SIZE));
//        dbProps.put("Database Connection Pool (max)", configuration.getProperty(Environment.C3P0_MAX_SIZE));

		// Modifications
        PropertyStore modProps = store.addCategory(MODZ);
		if (getModifiedFiles() != null && getModifiedFiles().size() > 0) {
			PropertyStore modifiedStore = modProps.addCategory(MODZ_MODIFIED);
			for (Object file : getModifiedFiles()) {
				modifiedStore.setValue(MODZ_FILE, file.toString());
			}
		}
		
		if (getRemovedFiles() != null && getRemovedFiles().size() > 0) {
			PropertyStore removedStore = modProps.addCategory(MODZ_REMOVED);
			for (Object file : getRemovedFiles()) {
				removedStore.setValue(MODZ_FILE, file.toString());
			}
		}

		PropertyStore resourceProps = store.addCategory(RESOURCE_LIMITS);
		if (this.cfg.isSetResources()) {
			ResourcesType resourceCfg = this.cfg.getResources();
			if (resourceCfg.isSetInitialIndexThreads()) {
				if (resourceCfg.getInitialIndexThreads().isSetMax()) {
					resourceProps.setValue(INITIAL_THREADS_MAX, String.valueOf(resourceCfg.getInitialIndexThreads().getMax()));
				}
				if (resourceCfg.getInitialIndexThreads().isSetMin()) {
					resourceProps.setValue(INITIAL_THREADS_MIN, String.valueOf(resourceCfg.getInitialIndexThreads().getMin()));
				}
			}
			if (resourceCfg.isSetIncrementalIndexThreads()) {
				if (resourceCfg.getIncrementalIndexThreads().isSetMax()) {
					resourceProps.setValue(INCREMENTAL_THREADS_MAX, String.valueOf(resourceCfg.getIncrementalIndexThreads().getMax()));
				}
				if (resourceCfg.getIncrementalIndexThreads().isSetMin()) {
					resourceProps.setValue(INCREMENTAL_THREADS_MIN, String.valueOf(resourceCfg.getIncrementalIndexThreads().getMin()));
				}
			}
		}
		
		PropertyStore sourceProps = store.addCategory(SOURCE_CONTROL);
		if (this.cfg.isSetSvnConfig()) {
			SvnConfigType svnCfg = this.cfg.getSvnConfig();
			if (svnCfg.isSetJar()) sourceProps.setValue(SVN_CLIENT_JAR, svnCfg.getJar());
			if (svnCfg.isSetJnilib()) sourceProps.setValue(SVN_CLIENT_LIB, svnCfg.getJnilib());
		}

		if (this.cfg.isSetP4Config()) {
			sourceProps.setValue(P4_CLIENT, this.cfg.getP4Config().getP4Location());
		}
	
		PropertyStore repositories = store.addCategory(REPOSITORIES);

		for (RepositoryDataFE repo : feRepositoryService.listRepositories()) 
		{
			PropertyStore repoProps = repositories.addCategory(REPOSITORIES_REPOSITORY);
			repoProps.setValue(REPOSITORIES_REPOSITORY_NAME, repo.getName());
			repoProps.setValue(REPOSITORIES_REPOSITORY_STATE, repo.getState().name());

			RepositoryData crucibleRepo = crucibleRepositoryService.getRepository(repo.getName());
			if (crucibleRepo != null) {
				repoProps.setValue(REPOSITORIES_REPOSITORY_TYPE, crucibleRepo.getType());
			}
		}

		return store;
	}

	@Override
	public String getMailQueueURL(HttpServletRequest request)
	{
		// FIXME: FeCru doesn't have a mail queue, the message won't make sense unless we return null and add a check for that upstream
		return null;
	}

	@Override
	public String getMailServerConfigurationURL(HttpServletRequest request)
	{
		return getBaseURL(request) + "/admin/editSmtpConfig-default.do"; 
	}

	@Override
	public FileSanitizer getFileSanitizer()
	{
		return new FileSanitizer(FILE_PATTERNS);
	}

    private List getModifiedFiles() {
    	try {
            if (this.modz.getModifications().modifiedFiles.isEmpty()) {
                return Collections.<String>emptyList();
            }
            return this.modz.getModifications().modifiedFiles;
        } catch (ModzRegistryException e) {
            Logs.APP_LOG.warn("Problems calculating modified files: " + e.getMessage()); 
            return Collections.<String>emptyList();
        }
    }


    private List getRemovedFiles() {
        try {
            if (this.modz.getModifications().removedFiles.isEmpty()) {
                return Collections.<String>emptyList();
            }
            return this.modz.getModifications().removedFiles;
        } catch (ModzRegistryException e) {
            Logs.APP_LOG.warn("Problems calculating removed files: " + e.getMessage()); 
            return Collections.<String>emptyList();
        }
    }
	
    @Override
    public String getApplicationLogDir() {
    		return AppConfig.getLogDir().getAbsolutePath();
    }
}