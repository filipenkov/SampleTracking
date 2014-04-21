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
import org.jfree.util.Log;

import com.atlassian.crowd.util.build.BuildUtils;
import com.atlassian.modzdetector.ModzRegistryException;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.sisyphus.SisyphusPatternSourceDecorator;
import com.atlassian.support.tools.hercules.ScanItem;
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
import com.cenqua.fisheye.user.FEUser;
import com.cenqua.fisheye.util.PropertiesUtil;
import com.cenqua.fisheye.web.admin.actions.plugins.PluginDataFactory;
import com.cenqua.fisheye.web.admin.actions.plugins.PluginDataFactory.PluginData;

public class FisheyeApplicationInfo extends AbstractSupportApplicationInfo
{
	private static final String API_ENABLED = "api.enabled";
	private static final String AUTO_ADD_USERS = "auth.users.auto.add";
	private static final String CACHE_TTL = "cache.ttl";
	private static final String CRUCIBLE_VERSION = "crucible.version";
	private static final String CUSTOM_SECURITY_CLASS = "auth.custom.classname";
	private static final String CUSTOM_SECURITY_PROPERTIES = "auth.custom.properties";
	private static final String DEBUG_ENABLED = "debug.enabled";
	private static final String FISHEYE_VERSION = "fisheye.version";
	private static final String HOST_AUTH_DOMAIN_SERVICE = "Domain/Service";
	private static final String HOST_AUTH_REQUIRED_GROUP = "Required Group";
	private static final String HTTP_BIND = "Http Bind";
	private static final String HTTP_CONTEXT = "Http Context";
	private static final String INCREMENTAL_THREADS_MAX = "threads.incremental.max";
	private static final String INCREMENTAL_THREADS_MIN = "threads.incremental.min";
	private static final String INITIAL_THREADS_MAX = "threads.initial.max";
	private static final String INITIAL_THREADS_MIN = "threads.initial.min";
	private static final String LDAP_BASE_DN = "auth.ldap.base.dn";
	private static final String LDAP_BIND_USER = "auth.ldap.initial.bind.user";
	private static final String LDAP_DISPLAY_NAME_ATTRIBUTE = "auth.ldap.display.name.attribute";
	private static final String LDAP_EMAIL_ATTRIBUTE = "auth.ldap.email.attribute";
	private static final String LDAP_SYNC_PERIOD = "auth.ldap.sync.period";
	private static final String LDAP_UID_ATTRIBUTE = "auth.ldap.uid.attribute";
	private static final String LDAP_URL = "auth..ldap.url";
	private static final String LDAP_USER_FILTER = "auth.ldap.user.filter";
	private static final String LICENSE_DESCRIPTION_CRUCIBLE = "crucible.license.description";
	private static final String MAX_CRUCIBLE_USERS = "crucible.max.users";
	private static final String MAX_FE_USERS = "fisheye.max.users";
	private static final String P4_CLIENT = "p4.client";
	private static final String SITE_CONTEXT = "site.context";
	private static final String SITE_PROXY_HOST = "site.proxy.host";
	private static final String SITE_PROXY_PORT = "site.proxy.port";
	private static final String SITE_PROXY_SCHEME = "site.proxy.scheme";
	private static final String SITE_URL = "site.url";
	private static final String SVN_CLIENT_JAR = "svn.client.jar";
	private static final String SVN_CLIENT_LIB = "svn.client.libs";
	private static final String USER_MGMT_TYPE = "user.management.type";
	
	
	public static final Map<String, List<Pattern>> FILE_PATTERNS;

	static
	{
		Map<String,List<Pattern>> map = new HashMap<String, List<Pattern>>();
		map.put("config.xml", Collections.unmodifiableList(Arrays.asList(
				Pattern.compile("password%3D(.+?)%0A"), // UAL properties
				Pattern.compile("password=[\"]([^\"]+?)\""), // catches keystore-password and truststore-password too
				Pattern.compile("initial-secret=\"(.+?)\""), // LDAP auth
				Pattern.compile("application.password=(.+?)$") // crowd config
				)));
		FILE_PATTERNS = Collections.unmodifiableMap(map);
	}
	
	private ConfigDocument.Config cfg;
	
	private PluginDataFactory pluginDataFactory;

    private ModzDetectorCache modz = new ModzDetectorCache();
    
    private DatabaseConfig dbConfig = null;

    private com.cenqua.fisheye.user.UserManager fisheyeUserManager;
	private DBControlFactoryImpl factory;

	public FisheyeApplicationInfo(ApplicationProperties applicationProperties, I18nResolver i18nResolver, UserManager userManager, TemplateRenderer renderer, PluginAccessor pluginAccessor)
	{
		super(applicationProperties, i18nResolver, userManager, renderer);
		this.pluginDataFactory = new PluginDataFactory(pluginAccessor);
		this.factory =  new DBControlFactoryImpl();

		this.cfg = AppConfig.getsConfig().getConfig();
		this.fisheyeUserManager = AppConfig.getsConfig().getUserManager();
        this.dbConfig = new DatabaseConfig(this.factory.getCurrentControl().getInfo().getConnectionInfo());
	}

	@Override
	public void initServletInfo(ServletConfig config)
	{
		super.initServletInfo(config);
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle("fecru-cfg", "stp.zip.include.fecru.cfg", "stp.zip.include.fecru.cfg.description", AppConfig.getInstanceDir().getAbsolutePath()+"/config.xml"));
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle("fecru-plugin-cfg", "stp.zip.include.fecru.plugin.cfg", "stp.zip.include.fecru.plugin.cfg.description", AppConfig.getInstanceDir().getAbsolutePath() + "var/plugins/config" ,  ".*\\.config"));
		this.applicationInfoBundles.add(new ListApplicationFileBundle("modz", "stp.zip.include.modz", "stp.zip.include.modz.description", getModifiedFiles()));
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle("fecru-logs", "stp.zip.include.fecru.logs", "stp.zip.include.fecru.logs.description", AppConfig.getLogDir().getAbsolutePath(),  "^fisheye.*\\.log\\..*"));
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle("fecru-out", "stp.zip.include.fecru.out", "stp.zip.include.fecru.out.description", AppConfig.getInstanceDir().getAbsolutePath()+"/var/log/fisheye.out"));
	}
	
	@Override
	public SisyphusPatternSource getPatternSource() throws IOException, ClassNotFoundException, MalformedURLException
	{
		SisyphusPatternSourceDecorator source = new SisyphusPatternSourceDecorator();
		source.add(getPatternSourceByURL("http://confluence.atlassian.com/download/attachments/179443532/fisheye_regex.xml"));
		source.add(getPatternSourceByURL("http://confluence.atlassian.com/download/attachments/179443532/crucible_regex.xml"));
		return source;
	}

	@Override
	public List<ScanItem> getApplicationLogFilePaths() 
	{
		List<ScanItem> logFilePaths = new ArrayList<ScanItem>();

		final String mostRecentErrorLog = mostRecentFile(AppConfig.getLogDir().listFiles((FilenameFilter) new RegexFileFilter("^fisheye-error.*\\.log\\..*")));
		if(!StringUtils.isBlank(mostRecentErrorLog))
			logFilePaths.add(new ScanItem("stp.hercules.scanItem.fecru.errorLog",  mostRecentErrorLog));
		
		final String mostRecentDebugLog = mostRecentFile(AppConfig.getLogDir().listFiles((FilenameFilter) new RegexFileFilter("^fisheye-debug.*\\.log\\..*")));
		if(!StringUtils.isBlank(mostRecentDebugLog))
			logFilePaths.add(new ScanItem("stp.hercules.scanItem.fecru.debugLog", mostRecentDebugLog));
		
		final String tomcatLog = AppConfig.getInstanceDir() + "/var/log/fisheye.out";
		if(new File(tomcatLog).exists())
			logFilePaths.add(new ScanItem("stp.hercules.scanItem.fecru.tomcatLog", tomcatLog));
		
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
					Log.error("Error retrieving canonical path for file.", e);
				}
			}
		}
		
		return mostRecentFile;
	}

	@Override
	public String getUserEmail()
	{
		// Fisheye doesn't seem to have a new enough SAL to get the email address, sadly.  When it upgrades, switch to that.
		try
		{
			FEUser user = this.fisheyeUserManager.getUser(this.userManager.getRemoteUsername());
			if (user != null) return user.getEmail();
		}
		catch(Exception e)
		{
			Log.error("Error while retrieving email address for current user", e);
		}
		
		Log.error("Can't find email address for user '" + this.userManager.getRemoteUsername() + "'.");
		return null;
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
	public void loadProperties()
	{
		// Fisheye Properties
		Map<String,String> appProps = new HashMap<String,String>();
		appProps.put(APPLICATION_HOME, AppConfig.getAppHome().getAbsolutePath());
		appProps.put("FISHEYE_INST", AppConfig.getInstanceDir().getAbsolutePath());

		appProps.put(FISHEYE_VERSION,(new FisheyeVersionInfo()).getReleaseNum());
		appProps.put(CRUCIBLE_VERSION,(new CrucibleVersionInfo()).getReleaseNum());
		
		appProps.put(DEBUG_ENABLED, String.valueOf(Logs.DEBUG_LOG.isDebugEnabled()));
		appProps.put(API_ENABLED, String.valueOf(AppConfig.getsConfig().isApiEnabled()));

		if (this.cfg.getWebServer() != null ) {
			if (this.cfg.getWebServer().isSetSiteUrl()) appProps.put(SITE_URL, this.cfg.getWebServer().getSiteUrl());
			
			if (this.cfg.getWebServer().isSetContext()) appProps.put(SITE_CONTEXT, this.cfg.getWebServer().getContext());
			
			if (this.cfg.getWebServer().isSetHttp()) {
				if (this.cfg.getWebServer().getHttp().isSetContext()) appProps.put(HTTP_CONTEXT, this.cfg.getWebServer().getHttp().getContext());
				appProps.put(HTTP_BIND, this.cfg.getWebServer().getHttp().getBind());
				
				if (this.cfg.getWebServer().getHttp().isSetProxyHost()) appProps.put(SITE_PROXY_HOST, String.valueOf(this.cfg.getWebServer().getHttp().getProxyHost()));
				if (this.cfg.getWebServer().getHttp().isSetProxyPort()) appProps.put(SITE_PROXY_PORT, String.valueOf(this.cfg.getWebServer().getHttp().getProxyPort()));
				if (this.cfg.getWebServer().getHttp().isSetProxyScheme()) appProps.put(SITE_PROXY_SCHEME, this.cfg.getWebServer().getHttp().getProxyScheme());
			}
		}

		
		Map<String,String> userMgmtProps = new HashMap<String,String>();
		if (this.cfg.getSecurity().isSetCrowd()) {
			CrowdAuthConfigType crowdCfg = this.cfg.getSecurity().getCrowd();
			userMgmtProps.put(USER_MGMT_TYPE, "Crowd");

			userMgmtProps.put("Crowd Name", PropertiesUtil.loadFromString(this.cfg.getSecurity().getCrowd().getCrowdProperties()).getProperty("application.name"));
			userMgmtProps.put("Crowd URL", PropertiesUtil.loadFromString(this.cfg.getSecurity().getCrowd().getCrowdProperties()).getProperty("crowd.server.url"));
			userMgmtProps.put("Crowd Version", BuildUtils.BUILD_VERSION);
			userMgmtProps.put(AUTO_ADD_USERS, String.valueOf(crowdCfg.getAutoAdd()));
			userMgmtProps.put("SSO Enabled", String.valueOf(crowdCfg.isSetSsoEnabled()));

			if (crowdCfg.isSetResync()) {
				userMgmtProps.put(LDAP_SYNC_PERIOD, crowdCfg.getResyncPeriod());
			}
			else {
				userMgmtProps.put(LDAP_SYNC_PERIOD, String.valueOf(crowdCfg.getResync()));
			}
		}

		if (this.cfg.getSecurity().isSetBuiltIn()) {
			userMgmtProps.put(USER_MGMT_TYPE, "Built-In");
			userMgmtProps.put("Signup Enabled", String.valueOf(this.cfg.getSecurity().getBuiltIn().getSignup().getEnabled()));
		}
		
		if (this.cfg.getSecurity().isSetLdap()) {
			LdapConfigType ldapCfg = this.cfg.getSecurity().getLdap();
			userMgmtProps.put(USER_MGMT_TYPE, "LDAP");
			userMgmtProps.put(LDAP_URL, ldapCfg.getUrl());
			userMgmtProps.put(LDAP_BASE_DN, ldapCfg.getBaseDn());
			userMgmtProps.put(LDAP_USER_FILTER, ldapCfg.getFilter());
			userMgmtProps.put(LDAP_UID_ATTRIBUTE, ldapCfg.getUidAttr());
			userMgmtProps.put(LDAP_EMAIL_ATTRIBUTE, ldapCfg.getEmailAttr());
			userMgmtProps.put(LDAP_DISPLAY_NAME_ATTRIBUTE, ldapCfg.isSetDisplaynameAttr() ? ldapCfg.getDisplaynameAttr() : "not set");
			userMgmtProps.put(CACHE_TTL, ldapCfg.getPositiveCacheTtl());
			userMgmtProps.put(AUTO_ADD_USERS, String.valueOf(ldapCfg.getAutoAdd()));
			userMgmtProps.put(LDAP_BIND_USER, ldapCfg.isSetInitialDn() ? ldapCfg.getInitialDn() : "not set");
			userMgmtProps.put(LDAP_SYNC_PERIOD, ldapCfg.getResyncPeriod());
		}
		
		if (this.cfg.getSecurity().isSetCustom() && this.cfg.getSecurity().getCustom() != null) {
			CustomSecurityConfigType customCfg = this.cfg.getSecurity().getCustom();
			userMgmtProps.put(USER_MGMT_TYPE, "Custom");
			userMgmtProps.put(CUSTOM_SECURITY_CLASS, customCfg.getClassname());
			userMgmtProps.put(AUTO_ADD_USERS, String.valueOf(customCfg.getAutoAdd()));
			userMgmtProps.put(CACHE_TTL, customCfg.getPositiveCacheTtl());
			userMgmtProps.put(CUSTOM_SECURITY_PROPERTIES, customCfg.getProperties());
		}
		
		if (this.cfg.getSecurity().isSetAjp() && this.cfg.getSecurity().getCustom() != null) {
			userMgmtProps.put(USER_MGMT_TYPE, "AJP");
			CustomSecurityConfigType ajpCfg = this.cfg.getSecurity().getCustom();
			userMgmtProps.put(AUTO_ADD_USERS, String.valueOf(ajpCfg.getAutoAdd()));
			userMgmtProps.put(CACHE_TTL, ajpCfg.getPositiveCacheTtl());
		}
		
		if (this.cfg.getSecurity().isSetHostAuth() &&  this.cfg.getSecurity().getHostAuth() != null) {
			HostauthConfigType hostCfg = this.cfg.getSecurity().getHostAuth();
			userMgmtProps.put(USER_MGMT_TYPE, "Host Auth");
			userMgmtProps.put(HOST_AUTH_REQUIRED_GROUP, hostCfg.getRequiredGroup());
			userMgmtProps.put(HOST_AUTH_DOMAIN_SERVICE, hostCfg.getDomain());
			userMgmtProps.put(AUTO_ADD_USERS, String.valueOf(hostCfg.getAutoAdd()));
			userMgmtProps.put(CACHE_TTL, hostCfg.getPositiveCacheTtl());
		}
		
		// License Information
		Map<String,String> licenseProps = new HashMap<String,String>();
		
		LicenseInfo productLicense = AppConfig.getsConfig().getLicense();
		
		if (productLicense.isCrucibleOnly()) {
			licenseProps.put(SUPPORT_ENTITLEMENT_NUMBER, productLicense.getCrucibleLicense().getSupportEntitlementNumber());
			licenseProps.put(SERVER_ID, productLicense.getCrucibleLicense().getSupportEntitlementNumber());
		}
		else if (productLicense.isFishEye()) {
			licenseProps.put(SUPPORT_ENTITLEMENT_NUMBER, productLicense.getFisheyeLicense().getSupportEntitlementNumber());
			licenseProps.put(SERVER_ID, productLicense.getFisheyeLicense().getSupportEntitlementNumber());
			licenseProps.put(LICENSE_EXPIRES, productLicense.getSoftExpiryValue().toString());
			String upgradesMaintenence = "";
			if (productLicense.getLatestBuildAllowedValue() != null) {
				upgradesMaintenence = "Expires " + productLicense.getLatestBuildAllowedValue().toString();
			} else {
				upgradesMaintenence = "Never Expires";
			}
			licenseProps.put(LICENSE_SUPPORT_PERIOD, upgradesMaintenence);
		}
		
		licenseProps.put(LICENSE_PRODUCT, productLicense.getProductName());
		licenseProps.put(LICENSE_OWNER, productLicense.getOwner());
		licenseProps.put(LICENSE_DESCRIPTION, productLicense.getDescription());
		
		
        if (productLicense.getCrucibleLicense() != null) {
            String maxCRUUsers = productLicense.getCrucibleLicense().isUnlimitedNumberOfUsers() ? "Unlimited" : "" + productLicense.getCrucibleLicense().getMaximumNumberOfUsers();
            licenseProps.put(MAX_CRUCIBLE_USERS, maxCRUUsers);
            licenseProps.put(LICENSE_DESCRIPTION_CRUCIBLE, productLicense.getCrucibleLicense().getDescription());
        }

        licenseProps.put(LICENSE_ORGANISATION, productLicense.getOwnerStatement());
        
        
        if (productLicense.getFisheyeLicense() != null) {
            String maxFEUsers = productLicense.getFisheyeLicense().isUnlimitedNumberOfUsers() ? "Unlimited" : "" + productLicense.getFisheyeLicense().getMaximumNumberOfUsers();
            licenseProps.put(MAX_FE_USERS, maxFEUsers);
        }
        
		Map<String,String> javaProps = new HashMap<String,String>();
		loadJavaProperties(javaProps);

    	
        Collection<PluginData> pluginData = this.pluginDataFactory.getPluginData(null, null);
		Map<String,String> pluginProps = new HashMap<String,String>();
        int pluginCount = 0;
        for (PluginData plugin : pluginData) 
		{
        	String pluginPrefix = PLUGIN_PLUGIN_PREFIX + "." + ++pluginCount;
        	
        	pluginProps.put(pluginPrefix + "." + PLUGIN_NAME_SUFFIX, plugin.getName());
        	pluginProps.put(pluginPrefix + "." + PLUGIN_VERSION_SUFFIX, plugin.getVersion());
        	pluginProps.put(pluginPrefix + "." + PLUGIN_VENDOR_SUFFIX, plugin.getVendor());
        	pluginProps.put(pluginPrefix + "." + PLUGIN_STATUS_SUFFIX, plugin.getState().toString());
		}
		pluginProps.put(PLUGIN_PLUGIN_PREFIX + "." + PLUGIN_COUNT_SUFFIX, String.valueOf(pluginCount));
		
        
		// Database Settings
        Map<String,String> dbProps = new HashMap<String,String>();
        dbProps.put(DB_NAME, this.dbConfig.getType().getDisplayName());
        dbProps.put(DB_DRIVER_NAME, this.dbConfig.getType().getDriver());
        dbProps.put(DB_CONNECTION_URL, this.dbConfig.getJdbcURL());
        dbProps.put(DB_VERSION, this.factory.getCurrentControl().getInfo().currentVersion().toString());

// FIXME:  The required org.hibernate.cfg classes are not exposed.  We will have to find another means to get this from inside the OSGI sandbox.
//        Configuration configuration = Config.getConfig(this.dbConfig);
//		dbProps.put("Database Connection Pool (min)", configuration.getProperty(Environment.C3P0_MIN_SIZE));
//        dbProps.put("Database Connection Pool (max)", configuration.getProperty(Environment.C3P0_MAX_SIZE));

		// Modifications
		Map<String,String> modProps = new HashMap<String,String>();
		addApplicationProperty(MODZ_MODIFIED, StringUtils.join(getModifiedFiles().toArray(),"\n"));
		addApplicationProperty(MODZ_REMOVED, StringUtils.join(getRemovedFiles().toArray(),"\n"));

		Map<String,String> resourceProps = new HashMap<String,String>();
		if (this.cfg.isSetResources()) {
			ResourcesType resourceCfg = this.cfg.getResources();
			if (resourceCfg.isSetInitialIndexThreads()) {
				if (resourceCfg.getInitialIndexThreads().isSetMax()) addApplicationProperty(INITIAL_THREADS_MAX, String.valueOf(resourceCfg.getInitialIndexThreads().getMax()));
				if (resourceCfg.getInitialIndexThreads().isSetMin()) addApplicationProperty(INITIAL_THREADS_MIN, String.valueOf(resourceCfg.getInitialIndexThreads().getMin()));
			}
			if (resourceCfg.isSetIncrementalIndexThreads()) {
				if (resourceCfg.getIncrementalIndexThreads().isSetMax()) addApplicationProperty(INCREMENTAL_THREADS_MAX, String.valueOf(resourceCfg.getIncrementalIndexThreads().getMax()));
				if (resourceCfg.getIncrementalIndexThreads().isSetMin()) addApplicationProperty(INCREMENTAL_THREADS_MIN, String.valueOf(resourceCfg.getIncrementalIndexThreads().getMin()));
			}
		}
		
		Map<String,String> sourceProps = new HashMap<String,String>();
		if (this.cfg.isSetSvnConfig()) {
			SvnConfigType svnCfg = this.cfg.getSvnConfig();
			if (svnCfg.isSetJar()) sourceProps.put(SVN_CLIENT_JAR, svnCfg.getJar());
			if (svnCfg.isSetJnilib()) sourceProps.put(SVN_CLIENT_LIB, svnCfg.getJnilib());
		}

		if (this.cfg.isSetP4Config()) {
			sourceProps.put(P4_CLIENT, this.cfg.getP4Config().getP4Location());
		}
		
		addApplicationProperties("Application Properties",appProps);
		addApplicationProperties("License Properties",licenseProps);
		addApplicationProperties("System Properties",javaProps);
		addApplicationProperties("Database Properties",dbProps);
		addApplicationProperties("Modifications",modProps);
		addApplicationProperties("Plugins",pluginProps);
		addApplicationProperties("External User Management",userMgmtProps);
		addApplicationProperties("Resource Limits", resourceProps);
		addApplicationProperties("Source Control", sourceProps);
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
	
}