package com.atlassian.support.tools.salext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jdk.utilities.runtimeinformation.MemoryInformation;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.UnloadableJiraServiceContainer;
import com.atlassian.jira.upgrade.UpgradeHistoryItem;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtils;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtilsImpl;
import com.atlassian.jira.util.system.check.SystemEnvironmentChecklist;
import com.atlassian.jira.util.system.patch.AppliedPatchInfo;
import com.atlassian.jira.util.system.patch.AppliedPatches;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.metadata.PluginMetadataManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.sisyphus.SisyphusPatternSourceDecorator;
import com.atlassian.support.tools.hercules.ScanItem;
import com.atlassian.support.tools.zip.FileSanitizer;
import com.atlassian.templaterenderer.TemplateRenderer;

public class JiraApplicationInfo extends AbstractSupportApplicationInfo
{
	private static final Map<String, List<Pattern>> FILE_PATTERNS = new HashMap<String, List<Pattern>>();

	static final Pattern TOMCAT_USERS_SANITIZER_PATTERN = Pattern
			.compile(
					".*?<user\\b(?>username=(?:\"|')([^'\"]*)(?:\"|')|password=(?:\"|')([^'\"]*)(?:\"|')|[^\\s>]+|\\s+)+/?>.*?",
					Pattern.CASE_INSENSITIVE);

	static
	{
		FILE_PATTERNS.put(
				"server.xml",
				Arrays.asList(Pattern.compile("(?:.*username=[\"']?([^\"'>]*)[\"']?.*)"),
						Pattern.compile("(?:.*password=[\"']?([^\"'>]*)[\"'].*)")));
		FILE_PATTERNS.put(
				"osuser.xml",
				Arrays.asList(Pattern.compile("(?:.*<property name=\"principal\">)(.*)(?:</property>.*)"),
						Pattern.compile("(?:.*<property name=\"credentials\">)(.*)(?:</property>.*)")));

		FILE_PATTERNS.put("tomcat-users.xml", Arrays.asList(TOMCAT_USERS_SANITIZER_PATTERN));
	}

	private final ExtendedSystemInfoUtils utils;

	private final LocaleManager localeManager;

	private final JiraLicenseService licenseService;

	public JiraApplicationInfo(JiraLicenseService licenseService, ApplicationProperties applicationProperties, I18nResolver i18nResolver, UserManager userManager, TemplateRenderer renderer, LocaleManager localeManager)
	{
		// JRA-14021 support request emails will always send the system info in
		// English. Note that this should NOT affect
		// other presentation in the view JSP - we still want other form
		// elements to display in the correct language.
		// The only section that should be affected is everything displayed
		// inside "Support Request Environment"
		super(applicationProperties, i18nResolver,
				userManager, renderer);
		this.licenseService = licenseService;
		this.localeManager = localeManager;
		this.utils = new ExtendedSystemInfoUtilsImpl(new I18nBean(Locale.ENGLISH));
	}

	@Override
	public SisyphusPatternSource getPatternSource() throws IOException, ClassNotFoundException, MalformedURLException
	{
		SisyphusPatternSourceDecorator source = new SisyphusPatternSourceDecorator();
		source.add(getPatternSourceByURL("http://confluence.atlassian.com/download/attachments/179443532/jira_regex.xml"));
		source.add(getPatternSourceByURL("http://confluence.atlassian.com/download/attachments/179443532/greenhopper_regex.xml"));
		return source;
	}

	@Override
	public void initServletInfo(ServletConfig config)
	{
		super.initServletInfo(config);

		String webInfClassesDir = getServletContextPath("WEB-INF/classes");
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle("jira-cfg", "stp.zip.include.jira.cfg",
				"stp.zip.include.jira.cfg.description", webInfClassesDir + "/jira-application.properties",
				webInfClassesDir + "/entityengine.xml", webInfClassesDir + "/log4j.properties"));
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle("tomcat-conf", "stp.zip.include.tomcat.conf",
				"stp.zip.include.tomcat.conf.description", findTomcatFileOrDirectory("conf"),
				"^.*.(xml|properties|policy)$"));
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle("auth-cfg", "stp.zip.include.auth.cfg",
				"stp.zip.include.auth.cfg.description", webInfClassesDir + "/osuser.xml", webInfClassesDir
						+ "/crowd.properties"));
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle("cache-cfg", "stp.zip.include.cache.cfg",
				"stp.zip.include.cache.cfg.description", webInfClassesDir + "/crowd-ehcache.xml", webInfClassesDir
						+ "/cache.properties", webInfClassesDir + "/oscache.properties"));
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle("jira-logs", "stp.zip.include.jira.logs",
				"stp.zip.include.jira.logs.description", getApplicationHome() + "/log", "^.*\\.log.*"));
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle("tomcat-logs", "stp.zip.include.tomcat.logs",
				"stp.zip.include.tomcat.logs.description", findTomcatFileOrDirectory("logs"), "^.*\\.(log|out)$"));
	}

	@Override
	public List<ScanItem> getApplicationLogFilePaths()
	{
		String logFilePath = this.applicationProperties.getHomeDirectory() + "/log/atlassian-jira.log";

		if(new File(logFilePath).exists())
		{
			return Collections.singletonList(ScanItem.createDefaultItem(logFilePath));
		}

		return Collections.emptyList();
	}
	
	@Override
	public String getCreateSupportRequestEmail()
	{
		return "jira-support@atlassian.com";
	}

	@Override
	public String getUserEmail()
	{
		ComponentManager instance = ComponentManager.getInstance();

		if(instance != null)
		{
			JiraAuthenticationContext jiraAuthenticationContext = instance.getJiraAuthenticationContext();

			if(jiraAuthenticationContext != null)
			{
				User remoteUser = jiraAuthenticationContext.getLoggedInUser();

				if(remoteUser != null)
				{
					return remoteUser.getEmailAddress();
				}
			}
		}

		return "";
	}

	@Override
	public String getApplicationSEN()
	{
		return this.licenseService.getLicense().getSupportEntitlementNumber();
	}

	@Override
	public String getApplicationServerID()
	{
		return this.licenseService.getServerId();
	}

	@Override
	public void loadProperties()
	{
		// FIXME: Standardize all property keys, especially Server ID, SEN, and JVM options.
		
		// System Info
		addApplicationProperties("System Info", this.utils.getProps());
		addApplicationProperties("Java VM Memory Statitistics", cloneNullSafe(this.utils.getJvmStats()));

		List<MemoryInformation> memoryPoolInformation = this.utils.getMemoryPoolInformation();
		Map<String, String> memInfo = new HashMap<String, String>();
		int count = 1;
		for(MemoryInformation memoryInformation: memoryPoolInformation)
		{
			memInfo.put("MemoryInformation_"+count++, memoryInformation.toString());
		}
		addApplicationProperties("Memory Pool Info", memInfo);

		// JIRA info
		addApplicationProperties("JIRA Info", cloneNullSafe(this.utils.getBuildStats()));

		// Last Upgrade & Upgrade History (new)
		List<UpgradeHistoryItem> upgradeHistory = this.utils.getUpgradeHistory();
		UpgradeHistoryItem lastUpgrade = null;
		
		StringBuffer upgradeHistoryBuffer = new StringBuffer();
		for (UpgradeHistoryItem upgrade : upgradeHistory) {
			upgradeHistoryBuffer.append(upgrade.getTimePerformed() + "(" + upgrade.getTargetVersion() + " #" + upgrade.getTargetBuildNumber() + ")\n");
			lastUpgrade = upgrade;
		}
		
		if (lastUpgrade != null) {
			addApplicationProperty("JIRA Info", "Last Upgrade", lastUpgrade.getTimePerformed() + "(" + lastUpgrade.getTargetVersion() + " #" + lastUpgrade.getTargetBuildNumber() + ")");
			addApplicationProperty("JIRA Info", "Upgrade History", upgradeHistoryBuffer.toString());
		}
		
		
		StringBuffer installedLanguages = new StringBuffer();
		for (Locale language : this.localeManager.getInstalledLocales()) {
			installedLanguages.append(language.getDisplayLanguage() + " (" + language.getDisplayCountry() + "), \n");
		}
		addApplicationProperty("JIRA Info", "Installed languges", installedLanguages.toString());
		addApplicationProperty("JIRA Info", "Default Language", this.utils.getDefaultLanguage() + (this.utils.isUsingSystemLocale()?" - System Default":""));
		

		
		addApplicationProperties("License Info", this.utils.getLicenseInfo());

		// Configuration Info?
		addApplicationProperties("Configuration Info", cloneNullSafe(this.utils.getCommonConfigProperties()));
		

		// Database Statistics
		if(this.utils.getUsageStats() != null)
		{
			addApplicationProperties("Database Statistics", this.utils.getUsageStats());
		}

		
		// File Paths
		addApplicationProperty("File Paths", "Location of JIRA Home", this.utils.getJiraHomeLocation());
		addApplicationProperty("File Paths", "Location of entityengine.xml", this.utils.getEntityEngineXmlPath());
		addApplicationProperty("File Paths", "Location of atlassian-jira.log", this.utils.getLogPath());
		addApplicationProperty("File Paths", "Location of indexes", this.utils.getIndexLocation());
		addApplicationProperty("File Paths", "Location of attachments", this.utils.getAttachmentsLocation());
		addApplicationProperty("File Paths", "Location of backups", this.utils.getBackupLocation());

		
		// Listeners
		for (Object listenerObject : this.utils.getListeners().toArray()) 
		{
			Map listener = (Map) listenerObject;
			if(listener.get("clazz") != null && listener.get("name") != null)
				addApplicationProperty("Listeners", listener.get("name").toString(), listener.get("clazz").toString());
		}

		// Services
		Collection<JiraServiceContainer> services = this.utils.getServices();
		Map<String,String> servicesProperties = new HashMap<String,String>();
		for (JiraServiceContainer service : services) 
		{
			StringBuffer buf = new StringBuffer();
			buf.append("{");
			buf.append("Delay=").append(service.getDelay());
			if(service instanceof UnloadableJiraServiceContainer)
			{
				// EACJ:SUPPORT-1797 - someone removed the plugin or unloaded it but forgot to delete the service.
				buf.append(", Status=Unloaded");
			}
			else
			{
				buf.append(", Last Run=").append(service.getLastRun());
				if(service.getDescription() != null)
					buf.append(", Description=").append(service.getDescription());
			}
			
			buf.append("}");
			servicesProperties.put(service.getName(), buf.toString());
			// FIXME: fix the damn formatting of this
		}
		addApplicationProperties("Services", servicesProperties);
		
		Map<String,String> userPluginProperties = new HashMap<String,String>();
		Map<String,String> systemPluginProperties = new HashMap<String,String>();
		Collection<Plugin> plugins = this.utils.getPlugins();
		PluginMetadataManager pluginMetadataManager = ComponentManager.getComponent(PluginMetadataManager.class);

		int userPluginCount = 0;
		int systemPluginCount = 0;
		for(Plugin plugin:plugins) 
		{
			PluginInformation pluginInformation = plugin.getPluginInformation();

			if (pluginMetadataManager.isUserInstalled(plugin))
			{
				String pluginPrefix = PLUGIN_PLUGIN_PREFIX + "." + userPluginCount++;
				
				userPluginProperties.put(pluginPrefix + "." + PLUGIN_NAME_SUFFIX, plugin.getName());  // this is important for the SysInfo page
				userPluginProperties.put(pluginPrefix + "." + PLUGIN_VERSION_SUFFIX, pluginInformation.getVersion());
				userPluginProperties.put(pluginPrefix + "." + PLUGIN_VENDOR_SUFFIX, pluginInformation.getVendorName());
				userPluginProperties.put(pluginPrefix + "." + PLUGIN_STATUS_SUFFIX, plugin.getPluginState().toString());
				userPluginProperties.put(pluginPrefix + "." + PLUGIN_VENDOR_URL_SUFFIX, pluginInformation.getVendorUrl());
				userPluginProperties.put(pluginPrefix + "." + PLUGIN_FRAMEWORK_VERSION_SUFFIX, String.valueOf(plugin.getPluginsVersion()));
			}
			else
			{
				String pluginPrefix = PLUGIN_PLUGIN_PREFIX + "." + systemPluginCount++;
				
				systemPluginProperties.put(pluginPrefix + "." + PLUGIN_NAME_SUFFIX, plugin.getName());  // this is important for the SysInfo page
				systemPluginProperties.put(pluginPrefix + "." + PLUGIN_VERSION_SUFFIX, pluginInformation.getVersion());
				systemPluginProperties.put(pluginPrefix + "." + PLUGIN_VENDOR_SUFFIX, pluginInformation.getVendorName());
				systemPluginProperties.put(pluginPrefix + "." + PLUGIN_STATUS_SUFFIX, plugin.getPluginState().toString());
				systemPluginProperties.put(pluginPrefix + "." + PLUGIN_VENDOR_URL_SUFFIX, pluginInformation.getVendorUrl());
				systemPluginProperties.put(pluginPrefix + "." + PLUGIN_FRAMEWORK_VERSION_SUFFIX, String.valueOf(plugin.getPluginsVersion()));
			}
		}
		
		userPluginProperties.put(PLUGIN_PLUGIN_PREFIX + "." + PLUGIN_COUNT_SUFFIX, String.valueOf(userPluginCount));
		systemPluginProperties.put(PLUGIN_PLUGIN_PREFIX + "." + PLUGIN_COUNT_SUFFIX, String.valueOf(systemPluginCount));

		// User Installed Plugins
		addApplicationProperties("User Installed Plugins",userPluginProperties);

		// System Plugins
		addApplicationProperties("System Plugins",systemPluginProperties);

		// Application Properties?
		addApplicationProperties("Application Properties", this.utils.getApplicationPropertiesFormatted(""));
		
		// System Properties?
		// FIXME:  Add System Properties
		
		// Trusted Applications?
		// FIXME:  Add Trusted Application information
		
		// Patches (NEW)
		for (AppliedPatchInfo patch : AppliedPatches.getAppliedPatches()) 
		{
			addApplicationProperty("Applied Patches", patch.getIssueKey(), patch.getDescription());
		}
	}


	/**
	 * If the object given is null returns an empty string, otherwise returns
	 * the result of toString() method call
	 * 
	 * @param o
	 *            object to check
	 * @return empty string or string representation of the given object
	 */
	private String getStringNotNull(final Object o)
	{
		return o == null ? "" : o.toString();
	}

	/**
	 * Creates and returns a new map that is a copy of the given map. If map
	 * contains null as a key or a value, this is replaced by empty string.
	 * 
	 * @param map
	 *            map to clone
	 * @return new map
	 */
	private Map<String, String> cloneNullSafe(final Map<?, ?> map)
	{
		final Map<String, String> retMap = new HashMap<String, String>(map.size());
		for(Map.Entry<?, ?> entry: map.entrySet())
		{
			final String key = getStringNotNull(entry.getKey());
			final String value = getStringNotNull(entry.getValue());
			retMap.put(key, value);
		}
		return retMap;
	}

	@Override
	public String getMailQueueURL(HttpServletRequest req)
	{
		return getBaseURL(req) + "/secure/admin/MailQueueAdmin!default.jspa";
	}

	@Override
	public String getMailServerConfigurationURL(HttpServletRequest request)
	{
		return getBaseURL(request) + "/secure/admin/ViewMailServers.jspa";
	}

	@Override
	public FileSanitizer getFileSanitizer()
	{
		return new FileSanitizer(FILE_PATTERNS);
	}
	
	@Override
	public List<String> getSystemWarnings()
	{
		// FIXME: find out why velocity is escaping our HTML and fix it. Then turn this flag to 'true'
		return SystemEnvironmentChecklist.getWarningMessages(Locale.getDefault(), false);
	}
}
