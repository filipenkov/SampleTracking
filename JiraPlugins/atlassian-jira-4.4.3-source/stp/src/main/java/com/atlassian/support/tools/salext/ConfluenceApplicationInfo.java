package com.atlassian.support.tools.salext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.cluster.ClusterManager;
import com.atlassian.confluence.core.ConfluenceActionSupport;
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
import com.atlassian.support.tools.zip.FileSanitizer;
import com.atlassian.templaterenderer.TemplateRenderer;

public class ConfluenceApplicationInfo extends AbstractSupportApplicationInfo
{
	public static final String CLUSTERED = "clustered";
	public static final String SITE_HOME_PAGE = "site.home.page";
	public static final String USAGE_CONTENT_CURRENT = "usage.content.current";
	public static final String USAGE_CONTENT_TOTAL = "usage.content.total";
	public static final String USAGE_GLOBAL_SPACES = "usage.global.spaces";
	public static final String USAGE_PERSONAL_SPACES = "usage.personal.spaces";
	public static final String USAGE_TOTAL_SPACES = "usage.total.spaces";
	
	private static final Map<String, List<Pattern>> FILE_PATTERNS = new HashMap<String, List<Pattern>>();
	private static final Pattern TOMCAT_USERS_SANITIZER_PATTERN = 
		Pattern.compile(".*?<user\\b(?>username=(?:\"|')([^'\"]*)(?:\"|')|password=(?:\"|')([^'\"]*)(?:\"|')|[^\\s>]+|\\s+)+/?>.*?",
					Pattern.CASE_INSENSITIVE);

    static
	{
		FILE_PATTERNS.put("confluence.cfg.xml", Arrays.asList(
				Pattern.compile("(?:.*<property name=\"confluence\\.license\\.message\">)(.*)(?:</property>.*)"),
				Pattern.compile("(?:.*<property name=\"hibernate\\.connection\\.username\">)(.*)(?:</property>.*)"),
				Pattern.compile("(?:.*<property name=\"hibernate\\.connection\\.password\">)(.*)(?:</property>.*)"),
				Pattern.compile("(?:.*<property name=\"license\\.string\">)(.*)(?:</property>.*)")));

		FILE_PATTERNS.put("atlassian-user.xml", Arrays.asList(
				Pattern.compile("(?:.*<securityPrincipal>)(.*)(?:</securityPrincipal>.*)"),
				Pattern.compile("(?:.*<securityCredential>)(.*)(?:</securityCredential>.*)")));

		FILE_PATTERNS.put("tomcat-users.xml", Arrays.asList(TOMCAT_USERS_SANITIZER_PATTERN));
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
	public void loadProperties()
	{
		// We use a range of standard formatters to present data in a reasonable format
		ByteSizeFormat byteFormat = new ByteSizeFormat();
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		
		DatabaseInfo dbInfo = this.sysInfoService.getDatabaseInfo();
		Map<String,String> dbProperties = new HashMap<String,String>();
		dbProperties.put(DB_DRIVER_CLASS, dbInfo.getClass().getName());
		dbProperties.put(DB_DIALECT, dbInfo.getDialect());
		dbProperties.put(DB_DRIVER_NAME, dbInfo.getDriverName());
		dbProperties.put(DB_DRIVER_VERSION, dbInfo.getDriverVersion());
		dbProperties.put(DB_EXAMPLE_LATENCY, dbInfo.getExampleLatency().toString());
		dbProperties.put(DB_CONNECTION_TRANSACTION_ISOLATION, dbInfo.getIsolationLevel());
		dbProperties.put(DB_NAME, dbInfo.getName());
		dbProperties.put(DB_CONNECTION_URL, dbInfo.getUrl());
		dbProperties.put(DB_VERSION, dbInfo.getVersion());

		ConfluenceInfo confluenceInfo = this.sysInfoService.getConfluenceInfo();
		Map<String,String> confluenceProperties = new HashMap<String,String>();

		confluenceProperties.put(APPLICATION_BASE_URL, confluenceInfo.getBaseUrl());
		confluenceProperties.put(APPLICATION_BUILD_NUMBER, confluenceInfo.getBuildNumber());
		confluenceProperties.put(APPLICATION_HOME, confluenceInfo.getHome());
		confluenceProperties.put(SERVER_ID, confluenceInfo.getServerId());
		confluenceProperties.put(APPLICATION_START_TIME, new Date(confluenceInfo.getStartTime()).toString());
		confluenceProperties.put(SUPPORT_ENTITLEMENT_NUMBER, confluenceInfo.getSupportEntitlementNumber());
		confluenceProperties.put(APPLICATION_UPTIME, confluenceInfo.getUpTime());
		confluenceProperties.put(APPLICATION_VERSION, confluenceInfo.getVersion());
		
		Settings globalSettings = confluenceInfo.getGlobalSettings();
		confluenceProperties.put(ATTACHMENT_DATA_STORE, globalSettings.getAttachmentDataStore());
		confluenceProperties.put(ATTACHMENT_MAX_SIZE, String.valueOf(globalSettings.getAttachmentMaxSize()));
		confluenceProperties.put(BACKUP_PATH, globalSettings.getBackupPath());
		CaptchaSettings captchaSettings = globalSettings.getCaptchaSettings();
		confluenceProperties.put(CAPTCHA_ENABLED, String.valueOf(captchaSettings.isEnableCaptcha()));
		confluenceProperties.put(CAPTCHA_GROUPS, StringUtils.join(captchaSettings.getCaptchaGroups(), ','));
		confluenceProperties.put(BACKUP_DATE_FORMAT_PATTERN, globalSettings.getDailyBackupDateFormatPattern());
		confluenceProperties.put(BACKUP_FILE_PREFIX, globalSettings.getDailyBackupFilePrefix());
		confluenceProperties.put(DEFAULT_ENCODING, globalSettings.getDefaultEncoding());
		confluenceProperties.put(GLOBAL_DEFAULT_LOCALE, globalSettings.getGlobalDefaultLocale());
		confluenceProperties.put(INDEXING_LANGUAGE, globalSettings.getIndexingLanguage());
		confluenceProperties.put(MAX_ATTACHMENTS_IN_UI, String.valueOf(globalSettings.getMaxAttachmentsInUI()));
		confluenceProperties.put(MAX_RSS_ITEMS, String.valueOf(globalSettings.getMaxRssItems()));
		confluenceProperties.put(MAX_QUICK_NAV_REQUESTS, String.valueOf(globalSettings.getMaxSimultaneousQuickNavRequests()));
		confluenceProperties.put(SITE_HOME_PAGE, globalSettings.getSiteHomePage());
		confluenceProperties.put(APPLICATION_TIME_ZONE, globalSettings.getTimeZone().toString());
		
		SystemInformationHelper helper = new SystemInformationHelper(this.i18NBeanFactory.getI18NBean(Locale.ENGLISH), this.sysInfoService);
		confluenceProperties.put(SYSTEM_DATE, helper.getSystemSummary().get("system.date"));
		confluenceProperties.put(SYSTEM_TIME, helper.getSystemSummary().get("system.time"));

		Map<String,String> pluginProperties = new HashMap<String, String>();
        int pluginCount = 0;
		for(Plugin plugin: confluenceInfo.getEnabledPlugins())
		{
        	PluginInformation pluginInformation = plugin.getPluginInformation();

        	String pluginPrefix = PLUGIN_PLUGIN_PREFIX + "." + pluginCount++;
        	
        	pluginProperties.put(pluginPrefix + "." + PLUGIN_NAME_SUFFIX, plugin.getName());  // this is important for the SysInfo page
        	pluginProperties.put(pluginPrefix + "." + PLUGIN_VERSION_SUFFIX, pluginInformation.getVersion());
        	pluginProperties.put(pluginPrefix + "." + PLUGIN_VENDOR_SUFFIX, pluginInformation.getVendorName());
        	pluginProperties.put(pluginPrefix + "." + PLUGIN_STATUS_SUFFIX, plugin.getPluginState().toString());
        	pluginProperties.put(pluginPrefix + "." + PLUGIN_VENDOR_URL_SUFFIX, pluginInformation.getVendorUrl());
        	pluginProperties.put(pluginPrefix + "." + PLUGIN_FRAMEWORK_VERSION_SUFFIX, String.valueOf(plugin.getPluginsVersion()));
		}
		pluginProperties.put(PLUGIN_PLUGIN_PREFIX + "." + PLUGIN_COUNT_SUFFIX, String.valueOf(pluginCount));

		
		
		MemoryInfo memoryInfo = this.sysInfoService.getMemoryInfo();
		Map<String,String> memoryProperties = new HashMap<String,String>();
		memoryProperties.put(JAVA_HEAP_ALLOCATED, byteFormat.format(memoryInfo.getAllocatedHeap().bytes()));
		memoryProperties.put(JAVA_HEAP_AVAILABLE, byteFormat.format(memoryInfo.getAvailableHeap().bytes()));
		memoryProperties.put(JAVA_PERMGEN_AVAILABLE, byteFormat.format(memoryInfo.getAvailablePermGen().bytes()));
		memoryProperties.put(JAVA_HEAP_FREE_ALLOCATED, byteFormat.format(memoryInfo.getFreeAllocatedHeap().bytes()));
		memoryProperties.put(JAVA_HEAP_PERCENT_USED, percentFormat.format(((double) memoryInfo.getFreeAllocatedHeap().bytes())/memoryInfo.getMaxHeap().bytes()) + " Free");
		memoryProperties.put(JAVA_HEAP_MAX, byteFormat.format(memoryInfo.getMaxHeap().bytes()));
		memoryProperties.put(JAVA_HEAP_USED, byteFormat.format(memoryInfo.getUsedHeap().bytes()));		
		memoryProperties.put(JAVA_PERMGEN_PERCENT_USED, percentFormat.format(((double) memoryInfo.getAvailablePermGen().bytes())/memoryInfo.getMaxPermGen().bytes()) + " Free");
		memoryProperties.put(JAVA_PERMGEN_MAX, byteFormat.format(memoryInfo.getMaxPermGen().bytes()));
		memoryProperties.put(JAVA_PERMGEN_USED, byteFormat.format(memoryInfo.getUsedPermGen().bytes()));

		Map<String, String> modifications = this.sysInfoService.getModifications();
		Map<String,String> modificationProperties = new HashMap<String,String>();
		for(String key: modifications.keySet())
		{
			modificationProperties.put(key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase(),
					modifications.get(key));
		}

		Map<String,String> javaProperties = new HashMap<String,String>();
		loadJavaProperties(javaProperties);

		UsageInfo usageInfo = this.sysInfoService.getUsageInfo();
		Map<String,String> usageProperties = new HashMap<String,String>();
		usageProperties.put(USAGE_CONTENT_TOTAL, String.valueOf(usageInfo.getAllContent()));
		usageProperties.put(USAGE_CONTENT_CURRENT, String.valueOf(usageInfo.getCurrentContent()));
		usageProperties.put(USAGE_INDEX_SIZE, String.valueOf(usageInfo.getIndexSize()));
		usageProperties.put(USAGE_LOCAL_GROUPS, String.valueOf(usageInfo.getLocalGroups()));
		usageProperties.put(USAGE_LOCAL_USERS, String.valueOf(usageInfo.getLocalUsers()));
		usageProperties.put(USAGE_GLOBAL_SPACES, String.valueOf(usageInfo.getGlobalSpaces()));
		usageProperties.put(USAGE_PERSONAL_SPACES, String.valueOf(usageInfo.getPersonalSpaces()));
		usageProperties.put(USAGE_TOTAL_SPACES, String.valueOf(usageInfo.getTotalSpaces()));
		
		Map<String,String> languageProperties = new HashMap<String,String>();
		StringBuffer lang = new StringBuffer();
		lang.append("[");
		for(Language language : this.languageManager.getLanguages()) 
		{
			lang.append(language.getName()).append(" (").append(language.getCountry()).append(")").append(",");
		}

		// FIXME: the above is done to conform to the old formatting. 
		// It would be better to add each language as a separate property somehow.
		languageProperties.put(INSTALLED_LANGUAGES, lang.toString());
	
		Map<String,String> licenseProperties = new HashMap<String,String>();

		License license = LicenseManager.getInstance().getLicense(ConfluenceBootstrapConstants.DEFAULT_LICENSE_REGISTRY_KEY);
		licenseProperties.put(LICENSE_ORGANISATION, license.getOrganisation());
		if(license.getLicenseType() != null)
				licenseProperties.put(LICENSE_TYPE, license.getLicenseType().toString());
		if(license.getExpiryDate() != null)
			licenseProperties.put(LICENSE_SUPPORT_PERIOD, license.getExpiryDate().toString());
		licenseProperties.put(LICENSE_USERS, String.valueOf(license.getUsers()));
		licenseProperties.put(LICENSE_PARTNER, license.getPartnerName());
		
		licenseProperties.put(SERVER_ID, confluenceInfo.getServerId());
		licenseProperties.put(SUPPORT_ENTITLEMENT_NUMBER, confluenceInfo.getSupportEntitlementNumber());
		licenseProperties.put(CLUSTERED, String.valueOf(this.clusterManager.isClustered()));

		UserChecker userChecker = (UserChecker) ContainerManager.getComponent("userChecker");
		if(userChecker != null)
		{
	        int userCount = userChecker.getNumberOfRegisteredUsers();
	        if (userCount > 0) // negative values indicate that the calculation is still in progress, or you have an unlimited user license
	        {
	            licenseProperties.put(LICENSE_ACTIVE_USERS, String.valueOf(userCount));
	        }
		}
		
		addApplicationProperties("Confluence Information",confluenceProperties);
		addApplicationProperties("Confluence Usage",usageProperties);
		addApplicationProperties("Java Runtime Environment",javaProperties);
		addApplicationProperties("Java VM Memory Statistics",memoryProperties);
		addApplicationProperties("Database Information",dbProperties);
		addApplicationProperties("Modification",modificationProperties);
		addApplicationProperties(AbstractSupportApplicationInfo.ENABLED_PLUGINS,pluginProperties);
		addApplicationProperties("License Details",licenseProperties);
		addApplicationProperties("System Properties",helper.getSystemSummary());
		addApplicationProperties(INSTALLED_LANGUAGES,languageProperties);
	}

	@Override
	public void initServletInfo(ServletConfig config)
	{
		super.initServletInfo(config);
		String webInfClassesDir = getServletContextPath("WEB-INF/classes");
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle("confluence-cfg", "stp.zip.include.confluence.cfg",
				"stp.zip.include.confluence.cfg.description", getApplicationHome() + "/confluence.cfg.xml",
				webInfClassesDir + "/confluence-init.properties", webInfClassesDir + "/log4j.properties"));
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle("tomcat-conf", "stp.zip.include.tomcat.conf",
				"stp.zip.include.tomcat.conf.description", findTomcatFileOrDirectory("conf"),
				"^.*\\.(xml|properties|policy)$"));
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle("auth-cfg", "stp.zip.include.auth.cfg",
				"stp.zip.include.auth.cfg.description", webInfClassesDir + "/atlassian-user.xml", webInfClassesDir
						+ "/osuser.xml", webInfClassesDir + "/crowd.properties"));
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle("cache-cfg", "stp.zip.include.cache.cfg",
				"stp.zip.include.cache.cfg.description", webInfClassesDir + "/crowd-ehcache.xml", getApplicationHome()
						+ "/config/ehcache.xml"));
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle("confluence-logs",
				"stp.zip.include.confluence.logs", "stp.zip.include.confluence.logs.description", getApplicationHome()
				+ "/logs", "^.*\\.log.*"));
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle("tomcat-logs", "stp.zip.include.tomcat.logs",
				"stp.zip.include.tomcat.logs.description", findTomcatFileOrDirectory("logs"), "^.*\\.(log|out)$"));
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
	public String getUserEmail()
	{
		ConfluenceActionSupport action = new ConfluenceActionSupport();
		return action.getRemoteUser().getEmail();
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
}
