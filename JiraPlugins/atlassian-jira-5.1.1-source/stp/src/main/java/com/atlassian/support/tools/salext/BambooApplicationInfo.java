package com.atlassian.support.tools.salext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.bamboo.configuration.SystemInfo;
import com.atlassian.bamboo.configuration.SystemStatisticsBean;
import com.atlassian.bamboo.fileserver.SystemDirectory;
import com.atlassian.bamboo.license.BambooLicenseManager;
import com.atlassian.bamboo.user.BambooAuthenticationContext;
import com.atlassian.extras.api.bamboo.BambooLicense;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.spring.container.LazyComponentReference;
import com.atlassian.support.tools.hercules.ScanItem;
import com.atlassian.support.tools.properties.PropertyStore;
import com.atlassian.support.tools.salext.bundle.BundleManifest;
import com.atlassian.support.tools.salext.bundle.DefaultApplicationFileBundle;
import com.atlassian.support.tools.zip.FileSanitizer;
import com.atlassian.templaterenderer.TemplateRenderer;

public class BambooApplicationInfo extends AbstractSupportApplicationInfo
{
	private static final String LICENSE_BAMBOO_MAX_PLANS = "stp.properties.license.bamboo.max.plans";
	private static final String LICENSE_BAMBOO_MAX_USERS = "stp.properties.license.bamboo.max.users";
	private static final String LICENSE_BAMBOO_MAX_REMOTE = "stp.properties.license.bamboo.max.remote";
	private static final String LICENSE_BAMBOO_MAX_LOCAL = "stp.properties.license.bamboo.max.local";
	private static final String BAMBOO_USAGE_RESULTS = "stp.properties.bamboo.results.count";
	private static final String BAMBOO_USAGE_PLANS = "stp.properties.bamboo.plans.count";
	private static final String BAMBOO_INDEX_TIME = "stp.properties.bamboo.index.time";
	private static final String BAMBOO_BUILD_DATA_DIRECTORY = "stp.properties.bamboo.build.data.directory";
	private static final String BAMBOO_BASE_BUILD_WORK_DIRECTORY = "stp.properties.bamboo.base.build.work.directory";
	private static final String BAMBOO_ARTIFACT_ROOT = "stp.properties.bamboo.artifact.root";
	private static final String BAMBOO_CONFIG_PATH = "stp.properties.bamboo.config.path";
	private static final String BAMBOO_BUILD_WORK_DIRECTORY = "stp.properties.bamboo.build.work.directory";
	private static final String BAMBOO_BUILD_PATH = "stp.properties.bamboo.build.path";
	private static final String BAMBOO_ARTIFACTS_DIRECTORY = "stp.properties.bamboo.artifacts.directory";
	private static final String BAMBOO_APP_SERVER_CONTAINER = "stp.properties.bamboo.app.server.container";
	private static final String BAMBOO_HOME = "stp.properties.bamboo.home";
	private static final String ZIP_INCLUDE_BAMBOO_LOG_DESCRIPTION = "stp.zip.include.bamboo.log.description";
	private static final String ZIP_INCLUDE_BAMBOO_LOG = "stp.zip.include.bamboo.log";
	private static final String ZIP_INCLUDE_BAMBOO_CFG_DESCRIPTION = "stp.zip.include.bamboo.cfg.description";
	private static final String ZIP_INCLUDE_BAMBOO_CFG = "stp.zip.include.bamboo.cfg";
	
	private static final Map<String, List<Pattern>> FILE_PATTERNLIST = new HashMap<String, List<Pattern>>();
	static
	{
		FILE_PATTERNLIST.put("bamboo.cfg.xml", Arrays.asList( 
				Pattern.compile("(?:.*<property name=\"hibernate\\.connection\\.username\">)(.*)(?:</property>.*)"), 
				Pattern.compile("(?:.*<property name=\"hibernate\\.connection\\.password\">)(.*)(?:</property>.*)"), 
				Pattern.compile("(?:.*<property name=\"license\\.string\">)(.*)(?:</property>.*)"))); 

		FILE_PATTERNLIST.put("bamboo-mail.cfg.xml", Arrays.asList( 
				Pattern.compile("(?:.*<password>)(.*)(?:</password>.*)"), 
				Pattern.compile("(?:.*<username>)(.*)(?:</username>.*)"))); 
	}
	
	private final LazyComponentReference<SystemInfo> systemInfoReference = new LazyComponentReference<SystemInfo>("systemInfo"); 
	private final LazyComponentReference<SystemStatisticsBean> systemStatisticsBean = new LazyComponentReference<SystemStatisticsBean>("systemStatisticsBean"); 
	private final LazyComponentReference<BambooLicenseManager> licenseManager = new LazyComponentReference<BambooLicenseManager>("bambooLicenseManager"); 
	
    private final PluginAccessor pluginAccessor;
	
	public BambooApplicationInfo(ApplicationProperties applicationProperties, 
			I18nResolver i18nResolver, UserManager userManager, TemplateRenderer renderer, 
			PluginAccessor pluginAccessor,
			BambooAuthenticationContext authenticationContext)
	{
		super(applicationProperties, i18nResolver, userManager, renderer);
		this.pluginAccessor = pluginAccessor;
	}

	@Override
	public void initServletInfo(ServletConfig config)
	{
		super.initServletInfo(config);

		this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.APPLICATION_CONFIG, ZIP_INCLUDE_BAMBOO_CFG, ZIP_INCLUDE_BAMBOO_CFG_DESCRIPTION,   
				new File(SystemDirectory.getApplicationHome(), "bamboo-mail.cfg.xml").getAbsolutePath(), 
				new File(SystemDirectory.getConfigDirectory(), "administration.xml").getAbsolutePath() 
				));
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.APPLICATION_LOGS, ZIP_INCLUDE_BAMBOO_LOG, ZIP_INCLUDE_BAMBOO_LOG_DESCRIPTION));  
	}
	
	@Override
	public List<ScanItem> getApplicationLogFilePaths()
	{
        File logFile = SystemDirectory.getLogFile();
        if (logFile.exists())
        {
            return Collections.singletonList(ScanItem.createDefaultItem(logFile.getAbsoluteFile().toString()));
        }
        else
        {
            return Collections.emptyList();
        }
		
	}

	@Override
	public SisyphusPatternSource getPatternSource() throws IOException, ClassNotFoundException, MalformedURLException
	{
		return super.getPatternSourceByURL("https://confluence.atlassian.com/download/attachments/179443532/bamboo_regex.xml"); 
	}

	@Override
	public String getCreateSupportRequestEmail()
	{
		// FIXME: CreateSupportRequest email for Bamboo does not exist yet.
		// this address is for issue updates. We should make the proxy commenter 
		// create issues if the email it reads is not an update.
		return "bamboo-support-system@atlassian.com"; 
	}

	@Override
	public String getMailQueueURL(HttpServletRequest request)
	{
		return null;
	}

	@Override
	public String getMailServerConfigurationURL(HttpServletRequest request)
	{
		return getBaseURL(request) + "/admin/viewMailServer.action"; 
	}

	@Override
	public FileSanitizer getFileSanitizer()
	{
		return new FileSanitizer(FILE_PATTERNLIST);
	}

	@Override
	public String getApplicationSEN()
	{
		BambooLicense license = licenseManager.get().getLicense();
		return license.getSupportEntitlementNumber();
	}

	@Override
	public String getApplicationServerID()
	{
		BambooLicense license = licenseManager.get().getLicense();
		return license.getServerId();
	}

	@Override
	public PropertyStore loadProperties()
	{
		PropertyStore store = super.loadProperties();

		PropertyStore sysInfo = store.addCategory(SYSTEM_INFO);
		
		SystemInfo systemInfo = systemInfoReference.get();
		sysInfo.setValue(BAMBOO_HOME, systemInfo.getApplicationHome()); 
		// systemInfo does not have a ServletContext so this method will cause an NPE
		// instead - just talk to the servletContext we have ourselves:
		// servletContext..getServerInfo()
		// sysInfo.setValue("App Server Container", systemInfo.getAppServerContainer()); 
		sysInfo.setValue(BAMBOO_APP_SERVER_CONTAINER, servletContext.getServerInfo()); 
		sysInfo.setValue(BAMBOO_ARTIFACTS_DIRECTORY, systemInfo.getArtifactsDirectory()); 
		sysInfo.setValue(BAMBOO_BUILD_PATH, systemInfo.getBuildPath()); 
		sysInfo.setValue(BAMBOO_BUILD_WORK_DIRECTORY, systemInfo.getBuildWorkingDirectory()); 
		sysInfo.setValue(BAMBOO_CONFIG_PATH, systemInfo.getConfigPath()); 
		sysInfo.setValue(CURRENT_DIRECTORY, systemInfo.getCurrentDirectory()); 
		sysInfo.setValue(FREE_DISK_SPACE, systemInfo.getFreeDiskSpace()); 
		sysInfo.setValue(HOST_NAME, systemInfo.getHostName()); 
		sysInfo.setValue(INDEX_SIZE, systemInfo.getIndexSize()); 
		sysInfo.setValue(IP_ADDRESS, systemInfo.getIpAddress()); 
		sysInfo.setValue(SYSTEM_OS, systemInfo.getOperatingSystem()); 
		sysInfo.setValue(SYSTEM_OS_ARCH, systemInfo.getOperatingSystemArchitecture()); 
		sysInfo.setValue(SYSTEM_DATE, systemInfo.getSystemDate()); 
		sysInfo.setValue(SYSTEM_ENCODING, systemInfo.getSystemEncoding()); 
		sysInfo.setValue(SYSTEM_TIME, systemInfo.getSystemTime()); 
		sysInfo.setValue(TEMP_DIRECTORY, systemInfo.getTempDir()); 
		sysInfo.setValue(APPLICATION_UPTIME, systemInfo.getUptime()); 
		sysInfo.setValue(USER_HOME, systemInfo.getUserHome()); 
		sysInfo.setValue(USER_LOCALE, systemInfo.getUserLocale()); 
		sysInfo.setValue(USER_NAME, systemInfo.getUserName()); 
		sysInfo.setValue(USER_TIMEZONE, systemInfo.getUserTimezone()); 
		sysInfo.setValue(SYSTEM_AVAILABLE_PROCESSORS, String.valueOf(systemInfo.getAvailableProcessors())); 
		sysInfo.setValue(MEMORY_TOTAL, String.valueOf(systemInfo.getTotalMemory())); 
		sysInfo.setValue(MEMORY_USED, String.valueOf(systemInfo.getUsedMemory())); 
		
		PropertyStore sysProps = store.addCategory(SYSTEM_INFO);
		Properties properties = System.getProperties();
		for(Object key: properties.keySet())
		{
			sysProps.setValue((String)key, properties.getProperty((String)key));
		}


		PropertyStore pathInfo = store.addCategory(PATH_INFO);
		pathInfo.setValue(BAMBOO_HOME, SystemDirectory.getApplicationHome().getAbsolutePath()); 
		pathInfo.setValue(BAMBOO_ARTIFACT_ROOT, SystemDirectory.getArtifactRootStorageDirectory().getAbsolutePath()); 
		pathInfo.setValue(BAMBOO_BASE_BUILD_WORK_DIRECTORY, SystemDirectory.getBaseBuildWorkingDirectory().getAbsolutePath()); 
		pathInfo.setValue(BAMBOO_BUILD_DATA_DIRECTORY, SystemDirectory.getBuildDataDirectory().getAbsolutePath()); 
		pathInfo.setValue(CACHE_DIRECTORY, SystemDirectory.getCacheDirectory().getAbsolutePath()); 
		pathInfo.setValue(CONFIG_DIRECTORY, SystemDirectory.getConfigDirectory().getAbsolutePath()); 
		pathInfo.setValue(LOG_DIRECTORY, SystemDirectory.getLogFile().getAbsolutePath()); 
		pathInfo.setValue(PLUGIN_CACHE_DIRECTORY, SystemDirectory.getPluginCacheDirectory().getAbsolutePath()); 
		pathInfo.setValue(PLUGIN_DIRECTORY, SystemDirectory.getPluginDirectory().getAbsolutePath()); 
		
		PropertyStore statsInfo = store.addCategory(STATISTICS);
		SystemStatisticsBean statisticsBean = systemStatisticsBean.get();
		statsInfo.setValue(BAMBOO_INDEX_TIME, String.valueOf(statisticsBean.getApproximateIndexTime())); 
		statsInfo.setValue(BAMBOO_USAGE_PLANS, String.valueOf(statisticsBean.getNumberOfPlans())); 
		statsInfo.setValue(BAMBOO_USAGE_RESULTS, String.valueOf(statisticsBean.getNumberOfResults())); 
		
		PropertyStore pluginsStore = store.addCategory(PLUGINS);
        for(Plugin plugin: pluginAccessor.getPlugins())
		{
        		PropertyStore pluginStore = pluginsStore.addCategory(PLUGINS_PLUGIN);
			PluginInformation info = plugin.getPluginInformation();

			pluginStore.setValue(PLUGIN_KEY, plugin.getKey());
			pluginStore.setValue(PLUGIN_NAME, plugin.getName());
			pluginStore.setValue(PLUGIN_VERSION, info.getVersion());
			pluginStore.setValue(PLUGIN_VENDOR, info.getVendorName());
			pluginStore.setValue(PLUGIN_STATUS, plugin.getPluginState().toString());
		}
		
		BambooLicense license = licenseManager.get().getLicense();
		PropertyStore licenseInfo = store.addCategory(LICENSE);
        licenseInfo.setValue(LICENSE_PURCHASED, license.getPurchaseDate().toString()); 
        licenseInfo.setValue(LICENSE_DESCRIPTION, license.getDescription()); 
        licenseInfo.setValue(LICENSE_SEN, license.getSupportEntitlementNumber()); 
        licenseInfo.setValue(LICENSE_SERVER_ID, license.getServerId()); 
        licenseInfo.setValue(LICENSE_EDITION, license.getLicenseEdition().name()); 
        licenseInfo.setValue(LICENSE_TYPE, license.getLicenseType().name()); 
        licenseInfo.setValue(LICENSE_EXPIRES, license.getExpiryDate() != null ? license.getExpiryDate().toString() : LICENSE_EXPIRES_NONE);  
        licenseInfo.setValue(LICENSE_BAMBOO_MAX_LOCAL, String.valueOf(license.getMaximumNumberOfLocalAgents())); 
        licenseInfo.setValue(LICENSE_BAMBOO_MAX_REMOTE, String.valueOf(license.getMaximumNumberOfRemoteAgents())); 
        licenseInfo.setValue(LICENSE_BAMBOO_MAX_USERS, String.valueOf(license.getMaximumNumberOfUsers())); 
        licenseInfo.setValue(LICENSE_BAMBOO_MAX_PLANS, String.valueOf(license.getMaximumNumberOfPlans())); 
        
        return store;
	}

	@Override
	public String getApplicationLogDir() {
        return SystemDirectory.getLogFile().getAbsoluteFile().toString();
	}
}