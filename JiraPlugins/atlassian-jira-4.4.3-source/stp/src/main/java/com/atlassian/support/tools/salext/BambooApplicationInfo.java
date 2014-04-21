package com.atlassian.support.tools.salext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.atlassian.support.tools.zip.FileSanitizer;
import com.atlassian.templaterenderer.TemplateRenderer;

public class BambooApplicationInfo extends AbstractSupportApplicationInfo
{
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
	private final BambooAuthenticationContext authenticationContext;
	
	public BambooApplicationInfo(ApplicationProperties applicationProperties, 
			I18nResolver i18nResolver, UserManager userManager, TemplateRenderer renderer, 
			PluginAccessor pluginAccessor,
			BambooAuthenticationContext authenticationContext)
	{
		super(applicationProperties, i18nResolver, userManager, renderer);
		this.pluginAccessor = pluginAccessor;
		this.authenticationContext = authenticationContext;
	}

	@Override
	public void initServletInfo(ServletConfig config)
	{
		super.initServletInfo(config);

		this.applicationInfoBundles.add(new DefaultApplicationFileBundle("bamboo-cfg", "stp.zip.include.bamboo.cfg", "stp.zip.include.bamboo.cfg.description", 
				new File(SystemDirectory.getApplicationHome(), "bamboo.cfg.xml").getAbsolutePath(),
				new File(SystemDirectory.getApplicationHome(), "bamboo-mail.cfg.xml").getAbsolutePath(),
				new File(SystemDirectory.getConfigDirectory(), "administration.xml").getAbsolutePath()
				));
		this.applicationInfoBundles.add(new DefaultApplicationFileBundle("bamboo-log", "stp.zip.include.bamboo.log", "stp.zip.include.bamboo.log.description", SystemDirectory.getLogFile().getAbsolutePath()));
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
		return super.getPatternSourceByURL("http://confluence.atlassian.com/download/attachments/179443532/bamboo_regex.xml");
	}

	@Override
	public String getUserEmail()
	{
		if(authenticationContext.getUser() == null)
			return "";
		else
			return authenticationContext.getUser().getEmail();
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
	public void loadProperties()
	{
		Map<String, String> sysInfo = new HashMap<String, String>();
		propertiesByCategory.put("System Information", sysInfo);
		
		SystemInfo systemInfo = systemInfoReference.get();
		sysInfo.put("Bamboo Home", systemInfo.getApplicationHome());
		// systemInfo does not have a ServletContext so this method will cause an NPE
		// instead - just talk to the servletContext we have ourselves:
		// servletContext..getServerInfo()
		// sysInfo.put("App Server Container", systemInfo.getAppServerContainer()); 
		sysInfo.put("App Server Container", servletContext.getServerInfo());
		sysInfo.put("Artifacts Directory", systemInfo.getArtifactsDirectory());
		sysInfo.put("Build Path", systemInfo.getBuildPath());
		sysInfo.put("Build Work Directory", systemInfo.getBuildWorkingDirectory());
		sysInfo.put("Config Path", systemInfo.getConfigPath());
		sysInfo.put("Current Directory", systemInfo.getCurrentDirectory());
		sysInfo.put("Free Disk Space", systemInfo.getFreeDiskSpace());
		sysInfo.put("Host Name", systemInfo.getHostName());
		sysInfo.put("Index Size", systemInfo.getIndexSize());
		sysInfo.put("IP Address", systemInfo.getIpAddress());
		sysInfo.put("OS", systemInfo.getOperatingSystem());
		sysInfo.put("OS Architecture", systemInfo.getOperatingSystemArchitecture());
		sysInfo.put("System Date", systemInfo.getSystemDate());
		sysInfo.put("System Encoding", systemInfo.getSystemEncoding());
		sysInfo.put("System Time", systemInfo.getSystemTime());
		sysInfo.put("Temp Directory", systemInfo.getTempDir());
		sysInfo.put("Uptime", systemInfo.getUptime());
		sysInfo.put("User Home", systemInfo.getUserHome());
		sysInfo.put("User Locale", systemInfo.getUserLocale());
		sysInfo.put("User Name", systemInfo.getUserName());
		sysInfo.put("User Timezone", systemInfo.getUserTimezone());
		sysInfo.put("Available Processors", String.valueOf(systemInfo.getAvailableProcessors()));
		sysInfo.put("Total Memory", String.valueOf(systemInfo.getTotalMemory()));
		sysInfo.put("Used Memory", String.valueOf(systemInfo.getUsedMemory()));
		
		
		Map<String, String> sysProps = new HashMap<String, String>();
		propertiesByCategory.put("System Properties", sysProps);
		Properties properties = System.getProperties();
		for(Object key: properties.keySet())
		{
			sysProps.put((String)key, properties.getProperty((String)key));
		}

		Map<String, String> envVarMap = new HashMap<String, String>();
		propertiesByCategory.put("Environment Variables", envVarMap);
		for(Entry<String, String> envEntry: System.getenv().entrySet())
		{
			envVarMap.put(envEntry.getKey(), envEntry.getValue());
		}
		
		Map<String, String> pathInfo = new HashMap<String, String>();
		propertiesByCategory.put("Path Information", pathInfo);
		pathInfo.put("Bamboo Home", SystemDirectory.getApplicationHome().getAbsolutePath());
		pathInfo.put("Artifact Root", SystemDirectory.getArtifactRootStorageDirectory().getAbsolutePath());
		pathInfo.put("Base Build Work Directory", SystemDirectory.getBaseBuildWorkingDirectory().getAbsolutePath());
		pathInfo.put("Build Data Directory", SystemDirectory.getBuildDataDirectory().getAbsolutePath());
		pathInfo.put("Cache Directory", SystemDirectory.getCacheDirectory().getAbsolutePath());
		pathInfo.put("Config Directory", SystemDirectory.getConfigDirectory().getAbsolutePath());
		pathInfo.put("Log file", SystemDirectory.getLogFile().getAbsolutePath());
		pathInfo.put("Plugin Cache Directory", SystemDirectory.getPluginCacheDirectory().getAbsolutePath());
		pathInfo.put("Plugin Directory", SystemDirectory.getPluginDirectory().getAbsolutePath());
		
		Map<String, String> statsInfo = new HashMap<String, String>();
		propertiesByCategory.put("Statistics Information", statsInfo);
		SystemStatisticsBean statisticsBean = systemStatisticsBean.get();
		statsInfo.put("Index time", String.valueOf(statisticsBean.getApproximateIndexTime()));
		statsInfo.put("Number of plans", String.valueOf(statisticsBean.getNumberOfPlans()));
		statsInfo.put("Number of results", String.valueOf(statisticsBean.getNumberOfResults()));
		
		
		Map<String, String> pluginInfo = new HashMap<String, String>();
		propertiesByCategory.put("Plugins", statsInfo);
		int pluginCount = 0;
		for(Plugin plugin: pluginAccessor.getPlugins())
		{
			PluginInformation pluginInformation = plugin.getPluginInformation();
			pluginInfo.put("Plugin.Name."+(pluginCount++), plugin.getName());
			pluginInfo.put(plugin.getName()+".Version", pluginInformation.getVersion());
			pluginInfo.put(plugin.getName()+".Vendor", pluginInformation.getVendorName());
			pluginInfo.put(plugin.getName()+".Status", plugin.getPluginState().toString());
			pluginInfo.put(plugin.getName()+".VendorURL", pluginInformation.getVendorUrl());
			pluginInfo.put(plugin.getName()+".FrameworkVersion", String.valueOf(plugin.getPluginsVersion()));

		
			StringBuffer buf = new StringBuffer();
			buf.append("{");
			buf.append("Plugin Framework Version=").append(plugin.getPluginsVersion());
			buf.append(", Plugin Version=").append(pluginInformation.getVersion());
			buf.append(", Vendor Name=").append(pluginInformation.getVendorName());
			buf.append(", Vendor URL=").append(pluginInformation.getVendorUrl());
			buf.append(", Status=").append(plugin.getPluginState().toString());
			buf.append("}");
			pluginInfo.put(plugin.getName(), buf.toString());
		}
		
		BambooLicense license = licenseManager.get().getLicense();
        Map<String, String> licenseInfo = new LinkedHashMap<String, String>();
        propertiesByCategory.put("License Information", licenseInfo);
        licenseInfo.put("Date purchased", license.getPurchaseDate().toString());
        licenseInfo.put("License Description", license.getDescription());
        licenseInfo.put("SEN", license.getSupportEntitlementNumber());
        licenseInfo.put("ServerID", license.getServerId());
        licenseInfo.put("License Edition", license.getLicenseEdition().name());
        licenseInfo.put("License Type", license.getLicenseType().name());
        licenseInfo.put("Expiry Date", license.getExpiryDate().toString());
        licenseInfo.put("Max Local Agents", String.valueOf(license.getMaximumNumberOfLocalAgents()));
        licenseInfo.put("Max Remote Agents", String.valueOf(license.getMaximumNumberOfRemoteAgents()));
        licenseInfo.put("Max Users", String.valueOf(license.getMaximumNumberOfUsers()));
        licenseInfo.put("Max Plans", String.valueOf(license.getMaximumNumberOfPlans()));
	}
}