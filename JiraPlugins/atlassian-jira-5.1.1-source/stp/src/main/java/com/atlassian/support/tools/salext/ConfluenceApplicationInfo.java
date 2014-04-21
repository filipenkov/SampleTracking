package com.atlassian.support.tools.salext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.cluster.ClusterManager;
import com.atlassian.confluence.languages.Language;
import com.atlassian.confluence.languages.LanguageManager;
import com.atlassian.confluence.setup.ConfluenceBootstrapConstants;
import com.atlassian.confluence.setup.settings.Settings;
import com.atlassian.confluence.setup.settings.beans.CaptchaSettings;
import com.atlassian.confluence.status.service.SystemInformationHelper;
import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.status.service.systeminfo.ConfluenceInfo;
import com.atlassian.confluence.status.service.systeminfo.DatabaseInfo;
import com.atlassian.confluence.status.service.systeminfo.MemoryInfo;
import com.atlassian.confluence.status.service.systeminfo.UsageInfo;
import com.atlassian.confluence.util.UserChecker;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.license.License;
import com.atlassian.license.LicenseManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.support.tools.format.ByteSizeFormat;
import com.atlassian.support.tools.hercules.ScanItem;
import com.atlassian.support.tools.properties.PropertyStore;
import com.atlassian.support.tools.salext.bundle.BundleManifest;
import com.atlassian.support.tools.salext.bundle.DefaultApplicationFileBundle;
import com.atlassian.support.tools.salext.bundle.WildcardApplicationFileBundle;
import com.atlassian.support.tools.zip.FileSanitizer;
import com.atlassian.templaterenderer.TemplateRenderer;

public class ConfluenceApplicationInfo extends AbstractSupportApplicationInfo
{
	public static final String CONFLUENCE_CLUSTERED = "clustered"; 
	public static final String CONFLUENCE_SITE_HOME_PAGE = "site.home.page"; 
	public static final String CONFLUENCE_USAGE_CONTENT_CURRENT = "usage.content.current"; 
	public static final String CONFLUENCE_USAGE_CONTENT_TOTAL = "usage.content.total"; 
	public static final String CONFLUENCE_USAGE_GLOBAL_SPACES = "usage.global.spaces"; 
	public static final String CONFLUENCE_USAGE_PERSONAL_SPACES = "usage.personal.spaces"; 
	public static final String CONFLUENCE_USAGE_TOTAL_SPACES = "usage.total.spaces"; 
	public static final String ZIP_INCLUDE_CONFLUENCE_LOGS_DESCRIPTION = "stp.zip.include.confluence.logs.description";
	public static final String ZIP_INCLUDE_CONFLUENCE_LOGS = "stp.zip.include.confluence.logs";
	public static final String ZIP_INCLUDE_CONFLUENCE_CFG = "stp.zip.include.confluence.cfg";

    static
	{
		FILE_PATTERNS.put("confluence.cfg.xml", Arrays.asList( 
				Pattern.compile("(?:.*<property name=\"confluence\\.license\\.message\">)(.*)(?:</property>.*)"), 
				Pattern.compile("(?:.*<property name=\"hibernate\\.connection\\.username\">)(.*)(?:</property>.*)"), 
				Pattern.compile("(?:.*<property name=\"hibernate\\.connection\\.password\">)(.*)(?:</property>.*)"), 
				Pattern.compile("(?:.*<property name=\"license\\.string\">)(.*)(?:</property>.*)"))); 

		FILE_PATTERNS.put("atlassian-user.xml", Arrays.asList( 
				Pattern.compile("(?:securityPrincipal\\s*>)(.*)(?:</\\s*securityPrincipal)"), 
				Pattern.compile("(?:securityCredential\\s*>)(.*)(?:</\\s*securityCredential)"))); 
	}

    private final I18NBeanFactory i18NBeanFactory;
	private final SystemInformationService sysInfoService;
	private final LanguageManager languageManager;
	private final ClusterManager clusterManager;

	public ConfluenceApplicationInfo(ApplicationProperties applicationProperties, I18nResolver i18nResolver, 
			UserManager userManager, TemplateRenderer renderer, SystemInformationService sysInfoService, 
			LanguageManager languageManager, ClusterManager clusterManager, I18NBeanFactory i18NBeanFactory)
	{
		super(applicationProperties, i18nResolver, userManager, renderer);
		this.sysInfoService = sysInfoService;
		this.languageManager = languageManager;
		this.clusterManager = clusterManager;
        this.i18NBeanFactory = i18NBeanFactory;
	}

	@Override
	public SisyphusPatternSource getPatternSource() throws IOException, ClassNotFoundException, MalformedURLException
	{
		return getPatternSourceByURL(CONFLUENCE_REGEX_XML);
	}

	@Override
	public String getApplicationSEN()
	{
		ConfluenceInfo confluenceInfo = this.sysInfoService.getConfluenceInfo();
		return confluenceInfo.getSupportEntitlementNumber();
	}

	@Override
	public String getApplicationServerID()
	{
		ConfluenceInfo confluenceInfo = this.sysInfoService.getConfluenceInfo();
		return confluenceInfo.getServerId();
	}
	
	@Override
	public PropertyStore loadProperties()
	{
		PropertyStore store = super.loadProperties();
		
		// We use a range of standard formatters to present data in a reasonable format
		ByteSizeFormat byteFormat = new ByteSizeFormat();
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		
		DatabaseInfo dbInfo = this.sysInfoService.getDatabaseInfo();

		PropertyStore dbProperties = store.addCategory(DB);
		dbProperties.setValue(DB_DRIVER_CLASS, dbInfo.getClass().getName());
		dbProperties.setValue(DB_DIALECT, dbInfo.getDialect());
		dbProperties.setValue(DB_DRIVER_NAME, dbInfo.getDriverName());
		dbProperties.setValue(DB_DRIVER_VERSION, dbInfo.getDriverVersion());
		dbProperties.setValue(DB_EXAMPLE_LATENCY, dbInfo.getExampleLatency().toString());
		dbProperties.setValue(DB_CONNECTION_TRANSACTION_ISOLATION, dbInfo.getIsolationLevel());
		dbProperties.setValue(DB_NAME, dbInfo.getName());
		dbProperties.setValue(DB_CONNECTION_URL, dbInfo.getUrl());
		dbProperties.setValue(DB_VERSION, dbInfo.getVersion());

		ConfluenceInfo confluenceInfo = this.sysInfoService.getConfluenceInfo();
		PropertyStore confluenceProperties = store.addCategory(APPLICATION_INFO);

		confluenceProperties.setValue(APPLICATION_BASE_URL, confluenceInfo.getBaseUrl());
		confluenceProperties.setValue(APPLICATION_BUILD_NUMBER, confluenceInfo.getBuildNumber());
		confluenceProperties.setValue(APPLICATION_HOME, confluenceInfo.getHome());
		confluenceProperties.setValue(LICENSE_SERVER_ID, confluenceInfo.getServerId());
		confluenceProperties.setValue(APPLICATION_START_TIME, new Date(confluenceInfo.getStartTime()).toString());
		confluenceProperties.setValue(LICENSE_SEN, confluenceInfo.getSupportEntitlementNumber());
		confluenceProperties.setValue(APPLICATION_UPTIME, confluenceInfo.getUpTime());
		confluenceProperties.setValue(APPLICATION_VERSION, confluenceInfo.getVersion());
		
		Settings globalSettings = confluenceInfo.getGlobalSettings();
		confluenceProperties.setValue(ATTACHMENT_DATA_STORE, globalSettings.getAttachmentDataStore());
		confluenceProperties.setValue(ATTACHMENT_MAX_SIZE, String.valueOf(globalSettings.getAttachmentMaxSize()));
		confluenceProperties.setValue(BACKUP_PATH, globalSettings.getBackupPath());
		CaptchaSettings captchaSettings = globalSettings.getCaptchaSettings();
		confluenceProperties.setValue(CAPTCHA_ENABLED, String.valueOf(captchaSettings.isEnableCaptcha()));
		confluenceProperties.setValue(CAPTCHA_GROUPS, StringUtils.join(captchaSettings.getCaptchaGroups(), ','));
		confluenceProperties.setValue(BACKUP_DATE_FORMAT_PATTERN, globalSettings.getDailyBackupDateFormatPattern());
		confluenceProperties.setValue(BACKUP_FILE_PREFIX, globalSettings.getDailyBackupFilePrefix());
		confluenceProperties.setValue(DEFAULT_ENCODING, globalSettings.getDefaultEncoding());
		confluenceProperties.setValue(GLOBAL_DEFAULT_LOCALE, globalSettings.getGlobalDefaultLocale());
		confluenceProperties.setValue(INDEXING_LANGUAGE, globalSettings.getIndexingLanguage());
		confluenceProperties.setValue(ATTACHMENT_UI_MAX, String.valueOf(globalSettings.getMaxAttachmentsInUI()));
		confluenceProperties.setValue(RSS_MAX_ITEMS, String.valueOf(globalSettings.getMaxRssItems()));
		confluenceProperties.setValue(QUICKNAV_MAX_REQUESTS, String.valueOf(globalSettings.getMaxSimultaneousQuickNavRequests()));
		confluenceProperties.setValue(CONFLUENCE_SITE_HOME_PAGE, globalSettings.getSiteHomePage());
		confluenceProperties.setValue(APPLICATION_TIME_ZONE, globalSettings.getTimeZone().toString());
		
		
		SystemInformationHelper helper = new SystemInformationHelper(this.i18NBeanFactory.getI18NBean(Locale.ENGLISH), this.sysInfoService);
		confluenceProperties.setValue(SYSTEM_DATE, helper.getSystemSummary().get("system.date")); 
		confluenceProperties.setValue(SYSTEM_TIME, helper.getSystemSummary().get("system.time")); 
		
		PropertyStore systemSummaryStore = store.addCategory(SYSTEM);
		systemSummaryStore.putValues(helper.getSystemSummary());

		PropertyStore pluginProperties = store.addCategory(AbstractSupportApplicationInfo.ENABLED_PLUGINS);
		for(Plugin plugin: confluenceInfo.getEnabledPlugins())
		{
	        	PluginInformation pluginInformation = plugin.getPluginInformation();
			PropertyStore pluginStore = pluginProperties.addCategory(PLUGINS_PLUGIN);

			pluginStore.setValue(PLUGIN_KEY, plugin.getKey());
	        	pluginStore.setValue(PLUGIN_NAME, plugin.getName());  // this is important for the SysInfo page 
	        	pluginStore.setValue(PLUGIN_VERSION, pluginInformation.getVersion()); 
	        	pluginStore.setValue(PLUGIN_VENDOR, pluginInformation.getVendorName()); 
	        	pluginStore.setValue(PLUGIN_STATUS, plugin.getPluginState().toString()); 
	        	pluginStore.setValue(PLUGIN_VENDOR_URL, pluginInformation.getVendorUrl()); 
	        	pluginStore.setValue(PLUGIN_FRAMEWORK_VERSION, String.valueOf(plugin.getPluginsVersion())); 
		}
		
		MemoryInfo memoryInfo = this.sysInfoService.getMemoryInfo();
		PropertyStore memoryProperties = store.addCategory(MEMORY);
		memoryProperties.setValue(JAVA_HEAP_ALLOCATED, byteFormat.format(memoryInfo.getAllocatedHeap().bytes()));
		memoryProperties.setValue(JAVA_HEAP_AVAILABLE, byteFormat.format(memoryInfo.getAvailableHeap().bytes()));
		memoryProperties.setValue(JAVA_PERMGEN_AVAILABLE, byteFormat.format(memoryInfo.getAvailablePermGen().bytes()));
		memoryProperties.setValue(JAVA_HEAP_FREE_ALLOCATED, byteFormat.format(memoryInfo.getFreeAllocatedHeap().bytes()));
		memoryProperties.setValue(JAVA_HEAP_PERCENT_USED, percentFormat.format(((double) memoryInfo.getFreeAllocatedHeap().bytes())/memoryInfo.getMaxHeap().bytes()) + i18nResolver.getText(JAVA_MEMORY_FREE)); 
		memoryProperties.setValue(JAVA_HEAP_MAX, byteFormat.format(memoryInfo.getMaxHeap().bytes()));
		memoryProperties.setValue(JAVA_HEAP_USED, byteFormat.format(memoryInfo.getUsedHeap().bytes()));		
		memoryProperties.setValue(JAVA_PERMGEN_PERCENT_USED, percentFormat.format(((double) memoryInfo.getAvailablePermGen().bytes())/memoryInfo.getMaxPermGen().bytes()) + i18nResolver.getText(JAVA_MEMORY_FREE)); 
		memoryProperties.setValue(JAVA_PERMGEN_MAX, byteFormat.format(memoryInfo.getMaxPermGen().bytes()));
		memoryProperties.setValue(JAVA_PERMGEN_USED, byteFormat.format(memoryInfo.getUsedPermGen().bytes()));

		Map<String, String> modifications = this.sysInfoService.getModifications();
		PropertyStore modificationProperties = store.addCategory(MODZ);
		modificationProperties.putValues(modifications);

		UsageInfo usageInfo = this.sysInfoService.getUsageInfo();
		PropertyStore usageProperties = store.addCategory(USAGE);
		usageProperties.setValue(CONFLUENCE_USAGE_CONTENT_TOTAL, String.valueOf(usageInfo.getAllContent()));
		usageProperties.setValue(CONFLUENCE_USAGE_CONTENT_CURRENT, String.valueOf(usageInfo.getCurrentContent()));
		usageProperties.setValue(USAGE_INDEX_SIZE, String.valueOf(usageInfo.getIndexSize()));
		usageProperties.setValue(USAGE_LOCAL_GROUPS, String.valueOf(usageInfo.getLocalGroups()));
		usageProperties.setValue(USAGE_LOCAL_USERS, String.valueOf(usageInfo.getLocalUsers()));
		usageProperties.setValue(CONFLUENCE_USAGE_GLOBAL_SPACES, String.valueOf(usageInfo.getGlobalSpaces()));
		usageProperties.setValue(CONFLUENCE_USAGE_PERSONAL_SPACES, String.valueOf(usageInfo.getPersonalSpaces()));
		usageProperties.setValue(CONFLUENCE_USAGE_TOTAL_SPACES, String.valueOf(usageInfo.getTotalSpaces()));
		
		PropertyStore languageProperties = store.addCategory(INSTALLED_LANGUAGES);
		for(Language language : this.languageManager.getLanguages()) 
		{
			PropertyStore languageStore = languageProperties.addCategory(language.getName());
			languageStore.setValue(LANGUAGE_NAME, language.getName());
			languageStore.setValue(LANGUAGE_COUNTRY, language.getCountry());
		}

		PropertyStore licenseProperties = store.addCategory(LICENSE);

		License license = LicenseManager.getInstance().getLicense(ConfluenceBootstrapConstants.DEFAULT_LICENSE_REGISTRY_KEY);
		licenseProperties.setValue(LICENSE_ORGANISATION, license.getOrganisation());
		if(license.getLicenseType() != null)
				licenseProperties.setValue(LICENSE_TYPE, license.getLicenseType().toString());
		if(license.getExpiryDate() != null)
			licenseProperties.setValue(LICENSE_SUPPORT_PERIOD, license.getExpiryDate().toString());
		licenseProperties.setValue(LICENSE_USERS, String.valueOf(license.getUsers()));
		licenseProperties.setValue(LICENSE_PARTNER, license.getPartnerName());
		
		licenseProperties.setValue(LICENSE_SERVER_ID, confluenceInfo.getServerId());
		licenseProperties.setValue(LICENSE_SEN, confluenceInfo.getSupportEntitlementNumber());
		licenseProperties.setValue(CONFLUENCE_CLUSTERED, String.valueOf(this.clusterManager.isClustered()));

		UserChecker userChecker = (UserChecker) ContainerManager.getComponent("userChecker"); 
		if(userChecker != null)
		{
	        int userCount = userChecker.getNumberOfRegisteredUsers();
	        if (userCount > 0) // negative values indicate that the calculation is still in progress, or you have an unlimited user license
	        {
	            licenseProperties.setValue(LICENSE_ACTIVE_USERS, String.valueOf(userCount));
	        }
		}
		
		return store;
	}

	@Override
	public void initServletInfo(ServletConfig config)
	{
		super.initServletInfo(config);
		String webInfClassesDir = getServletContextPath("WEB-INF/classes"); 
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.APPLICATION_CONFIG, ZIP_INCLUDE_CONFLUENCE_CFG, 
				"stp.zip.include.confluence.cfg.description", webInfClassesDir + "/confluence-init.properties",  
				webInfClassesDir + "/log4j.properties"));  
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle(BundleManifest.TOMCAT_CONFIG, ZIP_INCLUDE_TOMCAT_CONF, 
				ZIP_INCLUDE_TOMCAT_CONF_DESCRIPTION, findTomcatFileOrDirectory("conf"),  
				"^.*\\.(xml|properties|policy)$")); 
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.AUTH_CONFIG, ZIP_INCLUDE_AUTH_CFG, 
				ZIP_INCLUDE_AUTH_CFG_DESCRIPTION,  
				webInfClassesDir + "/osuser.xml",  
				webInfClassesDir + "/seraph-config.xml",  
				webInfClassesDir + "/seraph-paths.xml",  
				webInfClassesDir + "/crowd.properties")); 
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.CACHE_CONFIG, ZIP_INCLUDE_CACHE_CFG, 
				ZIP_INCLUDE_CACHE_CFG_DESCRIPTION, getApplicationHome()  
						+ "/config/ehcache.xml")); 
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle(BundleManifest.APPLICATION_LOGS,
				ZIP_INCLUDE_CONFLUENCE_LOGS, ZIP_INCLUDE_CONFLUENCE_LOGS_DESCRIPTION, getApplicationHome()  
				+ "/logs", "^.*\\.log.*"));  
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle(BundleManifest.TOMCAT_LOGS, ZIP_INCLUDE_TOMCAT_LOGS, 
				ZIP_INCLUDE_TOMCAT_LOGS_DESCRIPTION, findTomcatFileOrDirectory("logs"), "^.*\\.(log|out)$"));   
	}

	@Override
	public List<ScanItem> getApplicationLogFilePaths()
	{
		String logFilePath = this.applicationProperties.getHomeDirectory() + "/logs/atlassian-confluence.log"; 

		if(new File(logFilePath).exists())
		{
			return Collections.singletonList(ScanItem.createDefaultItem(logFilePath));
		}

		return Collections.emptyList();
	}

	@Override
	public String getCreateSupportRequestEmail()
	{
		return "confluence-autosupportrequests@atlassian.com"; 
	}

	@Override
	public String getMailQueueURL(HttpServletRequest req)
	{
		return getBaseURL(req) + "/admin/mail/viewmailqueue.action"; 
	}

	@Override
	public String getMailServerConfigurationURL(HttpServletRequest request)
	{
		return getBaseURL(request) + "/admin/mail/viewmailservers.action"; 
	}

	@Override
	public FileSanitizer getFileSanitizer()
	{
		return new FileSanitizer(FILE_PATTERNS);
	}
	
	@Override
	public String getApplicationLogDir() {
		return getApplicationHome() + "/logs"; 
	}
}
