package com.atlassian.support.tools.salext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
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
import com.atlassian.support.tools.properties.PropertyStore;
import com.atlassian.support.tools.salext.bundle.BundleManifest;
import com.atlassian.support.tools.salext.bundle.DefaultApplicationFileBundle;
import com.atlassian.support.tools.salext.bundle.WildcardApplicationFileBundle;
import com.atlassian.support.tools.zip.FileSanitizer;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.apache.commons.lang.StringUtils;

public class JiraApplicationInfo extends AbstractSupportApplicationInfo {
	private static final String JIRA_ATTACHMENT_LOCATION = "stp.properties.jira.attachment.location";
	private static final String JIRA_BACKUP_LOCATION = "stp.properties.jira.backup.location";
	private static final String JIRA_ENTITYENGINE_LOCATION = "stp.properties.jira.entityengine.location";
	private static final String JIRA_HOME = "stp.properties.jira.home.location";
	private static final String JIRA_INDEX_LOCATION = "stp.properties.jira.index.location";
	private static final String JIRA_LOG_LOCATION = "stp.properties.jira.log.location";

	static {
		FILE_PATTERNS.put(
				"osuser.xml",
				Arrays.asList(Pattern.compile("(?:.*<property name=\"principal\">)(.*)(?:</property>.*)"),
						Pattern.compile("(?:.*<property name=\"credentials\">)(.*)(?:</property>.*)")));
		FILE_PATTERNS.put(
				"dbconfig.xml",
				Arrays.asList(Pattern.compile("<username>([^<]+)</username>"),
						Pattern.compile("<password>([^<]+)</password>")));
	}

	private final ExtendedSystemInfoUtils utils;

	private final LocaleManager localeManager;

	private final JiraLicenseService licenseService;

	private final DateTimeFormatterFactory factory;

	public JiraApplicationInfo(JiraLicenseService licenseService,
			ApplicationProperties applicationProperties,
			I18nResolver i18nResolver,
			UserManager userManager,
			TemplateRenderer renderer,
			LocaleManager localeManager,
			DateTimeFormatterFactory factory) {
		// JRA-14021 support request emails will always send the system info in
		// English. Note that this should NOT affect
		// other presentation in the view JSP - we still want other form
		// elements to display in the correct language.
		// The only section that should be affected is everything displayed
		// inside "Support Request Environment"
		super(applicationProperties, i18nResolver, userManager, renderer);
		this.licenseService = licenseService;
		this.localeManager = localeManager;
		this.factory = factory;
		this.utils = new ExtendedSystemInfoUtilsImpl(new I18nBean(Locale.ENGLISH));
	}

	@Override
	public SisyphusPatternSource getPatternSource() throws IOException, ClassNotFoundException, MalformedURLException {
		SisyphusPatternSourceDecorator source = new SisyphusPatternSourceDecorator();
		source.add(getPatternSourceByURL("https://confluence.atlassian.com/download/attachments/179443532/jira_regex.xml"));
		source.add(getPatternSourceByURL("https://confluence.atlassian.com/download/attachments/179443532/greenhopper_regex.xml"));
		return source;
	}

	@Override
	public void initServletInfo(ServletConfig config) {
		super.initServletInfo(config);

		String webInfClassesDir = getServletContextPath("WEB-INF/classes");
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.APPLICATION_CONFIG,
				ZIP_INCLUDE_JIRA_CFG, ZIP_INCLUDE_JIRA_CFG_DESCRIPTION, webInfClassesDir + "/entityengine.xml", webInfClassesDir
						+ "/log4j.properties", getApplicationHome() + "/dbconfig.xml"));
        final String conf = findTomcatFileOrDirectory("conf");
        if(StringUtils.isNotBlank(conf))
        {
            this.applicationInfoBundles.add(new WildcardApplicationFileBundle(BundleManifest.TOMCAT_CONFIG,
                    ZIP_INCLUDE_TOMCAT_CONF, ZIP_INCLUDE_TOMCAT_CONF_DESCRIPTION,
                    conf, "^.*.(xml|properties|policy)$"));
        }
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.AUTH_CONFIG,
				ZIP_INCLUDE_AUTH_CFG, ZIP_INCLUDE_AUTH_CFG_DESCRIPTION, webInfClassesDir + "/crowd.properties"));
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle(BundleManifest.CACHE_CONFIG,
				ZIP_INCLUDE_CACHE_CFG, ZIP_INCLUDE_CACHE_CFG_DESCRIPTION, webInfClassesDir + "/cache.properties", webInfClassesDir
						+ "/oscache.properties"));
		this.applicationInfoBundles.add(new WildcardApplicationFileBundle(BundleManifest.APPLICATION_LOGS,
				ZIP_INCLUDE_JIRA_LOGS, ZIP_INCLUDE_JIRA_LOGS_DESCRIPTION, getApplicationHome() + "/log",
				"^.*\\.log.*"));
        final String logs = findTomcatFileOrDirectory("logs");
        if (StringUtils.isNotBlank(logs))
        {
            this.applicationInfoBundles.add(new WildcardApplicationFileBundle(BundleManifest.TOMCAT_LOGS,
                    ZIP_INCLUDE_TOMCAT_LOGS, ZIP_INCLUDE_TOMCAT_LOGS_DESCRIPTION,
                    logs, "^.*\\.(log|out)$"));
        }
	}

	@Override
	public List<ScanItem> getApplicationLogFilePaths() {
		String logFilePath = this.applicationProperties.getHomeDirectory() + "/log/atlassian-jira.log";

		if (new File(logFilePath).exists()) {
			return Collections.singletonList(ScanItem.createDefaultItem(logFilePath));
		}

		return Collections.emptyList();
	}

	@Override
	public String getCreateSupportRequestEmail() {
		return "jira-support@atlassian.com";
	}

	@Override
	public String getApplicationSEN() {
		return this.licenseService.getLicense().getSupportEntitlementNumber();
	}

	@Override
	public String getApplicationServerID() {
		return this.licenseService.getServerId();
	}

	@Override
	public PropertyStore loadProperties() {
		PropertyStore store = super.loadProperties();

		// System Info
		PropertyStore systemStore = store.addCategory(SYSTEM_INFO);
		systemStore.putValues(this.utils.getProps());

		PropertyStore jvmStatsStore = store.addCategory(MEMORY_STATISTICS);
		jvmStatsStore.putValues(cloneNullSafe(this.utils.getJvmStats()));

		// JIRA info
		PropertyStore appInfoStore = store.addCategory(APPLICATION_INFO);
		appInfoStore.putValues(cloneNullSafe(this.utils.getBuildStats()));

		// Last Upgrade & Upgrade History (new)
		List<UpgradeHistoryItem> upgradeHistory = this.utils.getUpgradeHistory();

		for (UpgradeHistoryItem upgrade : upgradeHistory) {
			PropertyStore upgradeStore = appInfoStore.addCategory(UPGRADE);
			upgradeStore
					.setValue(UPGRADE_TIME, factory.formatter().format(upgrade.getTimePerformed()));
			upgradeStore.setValue(UPGRADE_VERSION, upgrade.getTargetVersion());
			upgradeStore.setValue(UPGRADE_BUILD, upgrade.getTargetBuildNumber());
		}

		PropertyStore languageProperties = store.addCategory(INSTALLED_LANGUAGES);
		for (Locale language : this.localeManager.getInstalledLocales()) {
			PropertyStore languageStore = languageProperties.addCategory(LANGUAGES_LANGUAGE);
			languageStore.setValue(LANGUAGE_ABBREVIATION, language.getLanguage());
			languageStore.setValue(LANGUAGE_NAME, language.getDisplayLanguage());
			languageStore.setValue(LANGUAGE_COUNTRY, language.getDisplayCountry());
			if (language.equals(this.utils.getDefaultLanguage())) {
				languageStore.setValue(LANGUAGES_DEFAULT, "true");
			}
		}

		PropertyStore licenseProperties = store.addCategory(LICENSE_INFO);
		licenseProperties.putValues(this.utils.getLicenseInfo());

		PropertyStore configStore = store.addCategory(CONFIG_INFO);
		configStore.putValues(this.utils.getCommonConfigProperties());

		PropertyStore dbStatsStore = store.addCategory(DB_STATISTICS);
		dbStatsStore.putValues(this.utils.getUsageStats());

		PropertyStore filePathStore = store.addCategory(PATH_INFO);
		filePathStore.setValue(JIRA_HOME, this.utils.getJiraHomeLocation());
		filePathStore.setValue(JIRA_ENTITYENGINE_LOCATION, this.utils.getEntityEngineXmlPath());
		filePathStore.setValue(JIRA_LOG_LOCATION, this.utils.getLogPath());
		filePathStore.setValue(JIRA_INDEX_LOCATION, this.utils.getIndexLocation());
		filePathStore.setValue(JIRA_ATTACHMENT_LOCATION, this.utils.getAttachmentsLocation());
		filePathStore.setValue(JIRA_BACKUP_LOCATION, this.utils.getBackupLocation());

		PropertyStore listenersStore = store.addCategory(LISTENERS);
		for (Object listenerObject : this.utils.getListeners().toArray()) {
			Map listener = (Map) listenerObject;
			if (listener.get("clazz") != null && listener.get("name") != null) {
				PropertyStore listenerStore = listenersStore.addCategory(LISTENERS_LISTENER);
				listenerStore.setValue(LISTENERS_LISTENER_NAME, listener.get("name")
						.toString());
				listenerStore.setValue(LISTENERS_LISTENER_CLAZZ, listener.get("clazz")
						.toString());
			}
		}

		DecimalFormat df = new DecimalFormat("#");
		
		PropertyStore servicesStore = store.addCategory(SERVICES);
		Collection<JiraServiceContainer> services = this.utils.getServices();
		for (JiraServiceContainer service : services) {
			PropertyStore serviceStore = servicesStore.addCategory(SERVICES_SERVICE);
			serviceStore.setValue(SERVICES_SERVICE_NAME, service.getName());
			serviceStore.setValue(SERVICES_SERVICE_DELAY, df.format(service.getDelay()/1000) + "ms");
			if (service instanceof UnloadableJiraServiceContainer) {
				serviceStore.setValue(SERVICES_SERVICE_STATUS, "unloaded");
			} else {
				serviceStore.setValue(SERVICES_SERVICE_LAST_RUN,
						factory.formatter().format(new Date(service.getLastRun())));
                if (service.getDescription() != null)
                {
                    serviceStore.setValue(SERVICES_SERVICE_DESCRIPTION,
                            service.getDescription());
                }
			}
		}

		Collection<Plugin> plugins = this.utils.getPlugins();
		PluginMetadataManager pluginMetadataManager = ComponentManager.getComponent(PluginMetadataManager.class);

		PropertyStore pluginProperties = store.addCategory(AbstractSupportApplicationInfo.ENABLED_PLUGINS);
		for (Plugin plugin : plugins) {
			PluginInformation pluginInformation = plugin.getPluginInformation();
			PropertyStore pluginStore = pluginProperties.addCategory(PLUGINS_PLUGIN);

			pluginStore.setValue(PLUGIN_KEY, plugin.getKey());
			pluginStore.setValue(PLUGIN_NAME, plugin.getName());
			pluginStore.setValue(PLUGIN_VERSION, pluginInformation.getVersion());
			pluginStore.setValue(PLUGIN_VENDOR, pluginInformation.getVendorName());
			pluginStore.setValue(PLUGIN_STATUS, plugin.getPluginState().toString());
			pluginStore.setValue(PLUGIN_VENDOR_URL, pluginInformation.getVendorUrl());
			pluginStore.setValue(PLUGIN_FRAMEWORK_VERSION, String.valueOf(plugin.getPluginsVersion()));
			pluginStore.setValue(PLUGIN_USER_INSTALLED, pluginMetadataManager.isUserInstalled(plugin) ? "true" : "false");
		}

		PropertyStore applicationProperties = store.addCategory(APPLICATION_PROPERTIES);
		applicationProperties.putValues(this.utils.getApplicationPropertiesFormatted(", "));

		PropertyStore patchesStore = store.addCategory(PATCHES);
		for (AppliedPatchInfo patch : AppliedPatches.getAppliedPatches()) {
			PropertyStore patchStore = patchesStore.addCategory(PATCHES_PATCH);
			patchStore.setValue(PATCHES_PATCH_KEY, patch.getIssueKey());
			patchStore.setValue(PATCHES_PATCH_DESCRIPTION, patch.getDescription());
		}

		return store;
	}

	/**
	 * If the object given is null returns an empty string, otherwise returns
	 * the result of toString() method call
	 * 
	 * @param o
	 *            object to check
	 * @return empty string or string representation of the given object
	 */
	private String getStringNotNull(final Object o) {
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
	private Map<String, String> cloneNullSafe(final Map<?, ?> map) {
		final Map<String, String> retMap = new HashMap<String, String>(map.size());
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			final String key = getStringNotNull(entry.getKey());
			final String value = getStringNotNull(entry.getValue());
			retMap.put(key, value);
		}
		return retMap;
	}

	@Override
	public String getMailQueueURL(HttpServletRequest req) {
		return getBaseURL(req) + "/secure/admin/MailQueueAdmin!default.jspa";
	}

	@Override
	public String getMailServerConfigurationURL(HttpServletRequest request) {
		return getBaseURL(request) + "/secure/admin/ViewMailServers.jspa";
	}

	@Override
	public FileSanitizer getFileSanitizer() {
		return new FileSanitizer(FILE_PATTERNS);
	}

	@Override
	public List<String> getSystemWarnings() {
		// FIXME: find out why velocity is escaping our HTML and fix it. Then
		// turn this flag to 'true'
		return SystemEnvironmentChecklist.getWarningMessages(Locale.getDefault(), false);
	}

	@Override
	public String getApplicationLogDir() {
		return getApplicationHome() + "/log";
	}
}
