package com.atlassian.support.tools.salext;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sisyphus.RemoteXmlPatternSource;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.support.tools.ThreadDumpBundle;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.cenqua.fisheye.util.LargeNumberFormatter;
import com.cenqua.fisheye.util.StringUtil;
import com.opensymphony.module.sitemesh.util.Container;

public abstract class AbstractSupportApplicationInfo implements SupportApplicationInfo
{
	protected static final String ENABLED_PLUGINS = "Enabled Plugins";
	protected static final String PROPERTIES_DELIMITER = "=";
	protected static final String DEFAULT_CATEGORY = "Other";

	private boolean isTomcat;
	protected final ApplicationProperties applicationProperties;
	
	private final I18nResolver i18nResolver;

	protected final List<ApplicationInfoBundle> applicationInfoBundles = new ArrayList<ApplicationInfoBundle>();
	protected ServletContext servletContext;
	protected final UserManager userManager;
	protected final TemplateRenderer renderer;

	protected final Map<String,Map<String,String>> propertiesByCategory = new LinkedHashMap<String,Map<String,String>>();

	private final DecimalFormat df = new DecimalFormat("###");
	private final DecimalFormat pcf = new DecimalFormat("###%");
	private RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
	private MemoryPoolMXBean permgenBean;
    
    
	@Override
	public String getUserName()
	{
		return this.userManager.getRemoteUsername();
	}

	public AbstractSupportApplicationInfo(ApplicationProperties applicationProperties, I18nResolver i18nResolver, UserManager userManager, TemplateRenderer renderer)
	{
		this.applicationProperties = applicationProperties;
		this.i18nResolver = i18nResolver;
		this.userManager = userManager;
		this.renderer = renderer;

		this.applicationInfoBundles.add(new ApplicationPropertiesInfoBundle("application-properties", "stp.zip.include.application.properties", "stp.zip.include.application.properties.description", this));
		this.applicationInfoBundles.add(new ThreadDumpBundle("application-threadDump", "stp.zip.include.threadDump", "stp.zip.include.threadDump.description", this));
		
		// get the heap and permgen MemoryPoolMXBean objects
		for (MemoryPoolMXBean bean : ManagementFactory.getMemoryPoolMXBeans()) {
			if (bean.getName().contains("Perm Gen")) { permgenBean = bean; }
		}
		
	}

	@Override
	public final List<ApplicationInfoBundle> getApplicationFileBundles()
	{
		return this.applicationInfoBundles;
	}

	@Override
	public void initServletInfo(ServletConfig config)
	{
		this.servletContext = config.getServletContext();
		this.isTomcat = config.getServletContext().getServerInfo().contains("Tomcat");
	}

	protected void loadJavaProperties(Map<String, String> javaProps) {
		javaProps.put(JAVA_RUNTIME,System.getProperty("java.runtime.name"));
		javaProps.put(JAVA_VENDOR,System.getProperty("java.vendor"));
    	javaProps.put(JAVA_VERSION,System.getProperty("java.version"));
    	javaProps.put(JAVA_VM,System.getProperty("java.vm.name"));
    	javaProps.put(JAVA_VM_ARGUMENTS, getJVMInputArguments());
    	javaProps.put(JAVA_VM_VENDOR,System.getProperty("java.vm.specification.vendor"));
    	javaProps.put(JAVA_VM_VERSION,System.getProperty("java.vm.version"));
    	javaProps.put(OPERATING_SYSTEM_ARCHITECTURE,System.getProperty("os.arch"));
    	javaProps.put(OPERATING_SYSTEM,System.getProperty("os.name"));
    	javaProps.put(OPERATING_SYSTEM_VERSION,System.getProperty("os.version"));
    	javaProps.put(USER_TIMEZONE,System.getProperty("user.timezone"));
    	javaProps.put(USER_NAME,System.getProperty("user.name"));
    	javaProps.put(WORKING_DIRECTORY,System.getProperty("user.dir"));
    	javaProps.put(TEMP_DIRECTORY,System.getProperty("java.io.tmpdir"));
    	
    	javaProps.put(APPLICATION_SERVER, getAppServer());
    	javaProps.put(FILE_SYSTEM_ENCODING, System.getProperty("file.encoding"));

    	// Memory properties
    	javaProps.put(JAVA_HEAP_USED, getTotalHeap());
    	javaProps.put(JAVA_HEAP_AVAILABLE, getFreeHeap());
    	javaProps.put(JAVA_HEAP_PERCENT_USED, getPctHeapUsed());		
    	javaProps.put(JAVA_HEAP_MAX, getMaxHeap());		

    	javaProps.put(JAVA_PERMGEN_USED, getPermgenUsed());
    	javaProps.put(JAVA_PERMGEN_MAX, getMaxPermgen());		

	}
	

	private String getPermgenUsed()
	{
		if (this.permgenBean != null) {
			return getFormattedNum(this.permgenBean.getUsage().getUsed());
		}
		return "Unknown";
	}
	
	
	private String getMaxPermgen()
	{
		if (this.permgenBean != null) {
			return getFormattedNum(this.permgenBean.getUsage().getMax());
		}
		return "Unknown";
	}
	
	private String getMaxHeap()
	{
		return getFormattedNum(Runtime.getRuntime().maxMemory());
	}

	private String getFormattedNum(long num) {
        return LargeNumberFormatter.formatValue(this.df, num);
    }

    private String getTotalHeap() {
        return getFormattedNum(Runtime.getRuntime().totalMemory());
    }

    private String getFreeHeap() {
        return getFormattedNum(Runtime.getRuntime().freeMemory());
    }

    private String getPctHeapUsed() {
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        if (total == 0) {
            return "100%";
        }

        return this.pcf.format((float) (total - free) / total);
    }

    private String getJVMInputArguments() {
        if (this.runtimeMXBean != null) {
            return StringUtil.join(this.runtimeMXBean.getInputArguments(), " ");
        }
        return "Unknown";
    }

	
	@Override
	public boolean isTomcat()
	{
		return this.isTomcat;
	}

	protected SisyphusPatternSource getPatternSourceByURL(String urlString) throws IOException, ClassNotFoundException, MalformedURLException
	{
		return new RemoteXmlPatternSource(new URL(urlString));
	}

	public String findTomcatFileOrDirectory(String fileOrDirectoryName)
	{
		String catalinaBase = System.getProperty("catalina.base");
		File file = new File(catalinaBase, fileOrDirectoryName);
		if(file.exists())
		{
			return file.getAbsolutePath();

		}

		String catalinaHome = System.getProperty("catalina.home");
		file = new File(catalinaHome, fileOrDirectoryName);
		if(file.exists())
		{
			return file.getAbsolutePath();
		}

		String workingDirectory = System.getProperty("working.dir");
		file = new File(workingDirectory + "../", fileOrDirectoryName);
		if(file.exists())
		{
			return file.getAbsolutePath();
		}

		return null;
	}

	@Override
	public String getApplicationName()
	{
		return this.applicationProperties.getDisplayName();
	}

	@Override
	public String getApplicationVersion()
	{
		return this.applicationProperties.getVersion();
	}
	
	@Override
	public String getApplicationBuildNumber()
	{
		return this.applicationProperties.getBuildNumber();
	}
	
	@Override
	public Date getApplicationBuildDate()
	{
		return this.applicationProperties.getBuildDate();
	}
	
	@Override
	public String getApplicationHome()
	{
		return this.applicationProperties.getHomeDirectory().toString();
	}

	@Override
	public String getText(String key)
	{
		return this.i18nResolver.getText(key);
	}

	@Override
	public String getText(String key, Serializable... arguments)
	{
		return this.i18nResolver.getText(key, arguments);
	}

	@Override
	public String getServletContextPath(String pathToLookup)
	{
		return this.servletContext.getRealPath(pathToLookup);
	}

	@Override
	public void flagSelectedApplicationFileBundles(HttpServletRequest req)
	{
		List<ApplicationInfoBundle> selectedApplicationFileBundles = getSelectedApplicationInfoBundles(req);

		// We do this in the inverse so that the default is true. This supports
		// "check by default" but still preserves "unticked" boxes if we display
		// warnings.
		for(ApplicationInfoBundle bundle: this.applicationInfoBundles)
		{
			if(selectedApplicationFileBundles != null && selectedApplicationFileBundles.size() > 0)
			{
				if( ! selectedApplicationFileBundles.contains(bundle))
				{
					bundle.setSelected(false);
				}
			}
			else
			{
				bundle.setSelected(true);
			}
		}
	}

	@Override
	public List<ApplicationInfoBundle> getSelectedApplicationInfoBundles(HttpServletRequest req)
	{
		List<ApplicationInfoBundle> selectedApplicationInfoBundles = new ArrayList<ApplicationInfoBundle>();
		for(ApplicationInfoBundle applicationInfoBundle: this.applicationInfoBundles)
		{
			final String flagValue = req.getParameter(applicationInfoBundle.getKey());
			if(flagValue != null && Boolean.parseBoolean(flagValue))
			{
				selectedApplicationInfoBundles.add(applicationInfoBundle);
			}
		}

		return selectedApplicationInfoBundles;
	}

	@Override
	public Map<String,String> getPropertiesByCategory(String category)
	{
		return this.propertiesByCategory.get(category);
	}
	
	@Override
	public Set<String> getPropertyCategories() 
	{
		return this.propertiesByCategory.keySet();
	}
	
	@Override
	public String savePropertiesForMail()
	{
		loadProperties();

		// Create the data we will save as an attached file
		StringBuffer sb = new StringBuffer();
		for (String category : this.propertiesByCategory.keySet()) 
		{
			Map<String,String> properties = this.propertiesByCategory.get(category);
			for (Map.Entry<String, String> entry: properties.entrySet()) 
			{
				sb.append(entry.getKey());
				sb.append(PROPERTIES_DELIMITER);
				sb.append(entry.getValue());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	@Override
	public String savePropertiesForZip()
	{
		loadProperties();
		
		StringBuffer sb = new StringBuffer();
		for (String category : this.propertiesByCategory.keySet()) 
		{
			Map<String, String> properties = this.propertiesByCategory.get(category);
			for (Map.Entry<String, String> entry: properties.entrySet()) 
			{
				sb.append(category.replaceAll(" ","."));
				sb.append(".");
				sb.append(entry.getKey().replaceAll(" ","."));
				sb.append("=");
				sb.append(String.valueOf(entry.getValue()));
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	@Override
	public TemplateRenderer getTemplateRenderer()
	{
		return this.renderer;
	}

	
	public void addApplicationProperty(String category, String key, String value) {
		if(key != null && value != null)
		{
			Map<String, String> categoryProperties = this.propertiesByCategory.get(category);
			if (categoryProperties == null) 
			{
				categoryProperties = new HashMap<String,String>();
				this.propertiesByCategory.put(category, categoryProperties);
			}
			
			categoryProperties.put(key, value);
		}
	}
	
	public void addApplicationProperty(String key, String value)
	{
		addApplicationProperty(DEFAULT_CATEGORY, key, value);
	}

	protected void addApplicationProperties(String category, Map<String,String> map)
	{
		Map<String, String> categoryProperties = this.propertiesByCategory.get(category);
		if (categoryProperties == null) 
		{
			categoryProperties = new HashMap<String,String>();
			this.propertiesByCategory.put(category, categoryProperties);
		}
		
		categoryProperties.putAll(map);
	}
	
	protected void addApplicationProperties(Map<String, String> map)
	{
		addApplicationProperties(DEFAULT_CATEGORY, map);
	}

	@Override
	public String getBaseURL(HttpServletRequest req)
	{
		return req.getRequestURI().replaceFirst(req.getServletPath() + ".*", "");
	}
	
	@Override
	public List<String> getSystemWarnings()
	{
		return Collections.emptyList();
	}
	
    /**
	 * @return a readable version of the current container, or "Unknown".
	 */
    public String getAppServer()
    {
        switch (Container.get())
        {
            case Container.TOMCAT:
                return "Apache Tomcat";
            case Container.ORION:
                return "Orion";
            case Container.WEBLOGIC:
                return "IBM WebLogic";
            case Container.JRUN:
                return "JRUN";
            case Container.RESIN:
                return "RESIN" + Container.get();
            case Container.HPAS:
                return "HPAS";
            case Container.UNKNOWN:
                return "Unknown";
        }
        return "Unknown";
    }
}
