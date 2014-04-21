package com.atlassian.support.tools.salext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.IllegalAddException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.util.NonLazyElement;

import com.atlassian.jira.util.I18nHelper.BeanFactory;
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sisyphus.RemoteXmlPatternSource;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.support.tools.properties.DefaultPropertyStore;
import com.atlassian.support.tools.properties.MultiValuePropertyStore;
import com.atlassian.support.tools.properties.PropertyStore;
import com.atlassian.support.tools.salext.bundle.ApplicationInfoBundle;
import com.atlassian.support.tools.salext.bundle.ApplicationPropertiesInfoBundle;
import com.atlassian.support.tools.salext.bundle.BundleManifest;
import com.atlassian.support.tools.salext.bundle.ThreadDumpBundle;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.opensymphony.module.sitemesh.util.Container;

public abstract class AbstractSupportApplicationInfo implements SupportApplicationInfo {
	private static final Logger log = Logger.getLogger(AbstractSupportApplicationInfo.class);
	private static final String UNKNOWN = "unknown";
	protected static final String ENABLED_PLUGINS = "stp.properties.plugins.enabled";
	protected static final String PROPERTIES_DELIMITER = "=";
	protected static final String DEFAULT_CATEGORY = "stp.properties.default";

	private boolean isTomcat;
	protected final ApplicationProperties applicationProperties;

	protected final I18nResolver i18nResolver;

	protected final List<ApplicationInfoBundle> applicationInfoBundles = new ArrayList<ApplicationInfoBundle>();
	protected ServletContext servletContext;
	protected final UserManager userManager;
	protected final TemplateRenderer renderer;

	protected final Map<String, Map<String, String>> propertiesByCategory = new LinkedHashMap<String, Map<String, String>>();

	private final DecimalFormat pcf = new DecimalFormat("###%");
	private RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
	private MemoryPoolMXBean permgenBean;
	private Properties xmlElementNames = new Properties();

	public static final Map<String, List<Pattern>> FILE_PATTERNS = new HashMap<String, List<Pattern>>();
	private static final Pattern TOMCAT_USERS_SANITIZER_PATTERN = Pattern.compile(
			"(?:.*(?:username|password|name)[ ]*=[ ]*[\"']?([^\"'> ]*)[\"']?.*)", Pattern.CASE_INSENSITIVE);

	static {
		FILE_PATTERNS
				.put("server.xml",
						Arrays.asList(Pattern
								.compile(
										"(?:.*(?:username|password|keystorePass|truststorePass|connectionPassword|connectionName)[ ]*=[ ]*[\"']?([^\"'> ]*)[\"']?.*)",
										Pattern.CASE_INSENSITIVE)));

		FILE_PATTERNS
				.put("crowd.properties", Arrays.asList(Pattern.compile("application\\.(?:name|password)\\s+(.+)\\s*",
						Pattern.CASE_INSENSITIVE)));

		FILE_PATTERNS.put("tomcat-users.xml", Arrays.asList(TOMCAT_USERS_SANITIZER_PATTERN));
	}

	@Override
	public String getUserName() {
		return this.userManager.getRemoteUsername();
	}

	@Override
	public String getUserEmail() {
		return this.userManager.getUserProfile(getUserName()).getEmail();
	}

	protected AbstractSupportApplicationInfo() {
		this.applicationProperties = null;
		this.i18nResolver = null;
		this.userManager = null;
		this.renderer = null;
		
		loadXmlElementNames();
	}

	private void loadXmlElementNames() {
        InputStream stream = ClassLoaderUtils.getResourceAsStream("xml/xml-element-names.properties", this.getClass());
        if (stream == null) {
        		log.error("Error loading xml elements from resource file.");
        }
        else {
	        	try {
	        		xmlElementNames.load(stream);
	        	} catch (IOException e) {
	        		log.error("Error loading xml element names for use in application property files:", e);
	        	}
        }
	}

	public AbstractSupportApplicationInfo(ApplicationProperties applicationProperties,
			I18nResolver i18nResolver,
			UserManager userManager,
			TemplateRenderer renderer) {
		this.applicationProperties = applicationProperties;
		this.i18nResolver = i18nResolver;
		this.userManager = userManager;
		this.renderer = renderer;

		this.applicationInfoBundles.add(new ApplicationPropertiesInfoBundle(BundleManifest.APPLICATION_PROPERTIES,
				"stp.zip.include.application.properties", "stp.zip.include.application.properties.description", this));
		this.applicationInfoBundles.add(new ThreadDumpBundle(BundleManifest.THREAD_DUMP, "stp.zip.include.threadDump",
				"stp.zip.include.threadDump.description", null, this));

		// get the heap and permgen MemoryPoolMXBean objects
		for (MemoryPoolMXBean bean : ManagementFactory.getMemoryPoolMXBeans()) {
			if (bean.getName().contains("Perm Gen")) {
				permgenBean = bean;
			}
		}

		loadXmlElementNames();
	}

	@Override
	public final List<ApplicationInfoBundle> getApplicationFileBundles() {
		return this.applicationInfoBundles;
	}

	@Override
	public void initServletInfo(ServletConfig config) {
		this.servletContext = config.getServletContext();
		this.isTomcat = config.getServletContext().getServerInfo().contains("Tomcat");
	}

	@Override
	public PropertyStore loadProperties() {
		PropertyStore store = new MultiValuePropertyStore();
		
		PropertyStore javaStore = store.addCategory("stp.properties.java");
		javaStore.putValues(loadJavaProperties());
		
		PropertyStore envVarMap = store.addCategory("stp.properties.environment.variables");
		envVarMap.putValues(System.getenv());

		return store;
	}
	
	private Map<String, String> loadJavaProperties() {
		Map<String,String> props = new HashMap<String,String>();
		
		Properties systemProps = System.getProperties();
		for (Entry<Object, Object> entry : systemProps.entrySet()) {
			props.put(entry.getKey().toString(), entry.getValue().toString());
		}

		props.put(JAVA_VM_ARGUMENTS, getJVMInputArguments());
		props.put(APPLICATION_SERVER, getAppServer());

		// Memory properties
		props.put(JAVA_HEAP_USED, getTotalHeap());
		props.put(JAVA_HEAP_AVAILABLE, getFreeHeap());
		props.put(JAVA_HEAP_PERCENT_USED, getPctHeapUsed());
		props.put(JAVA_HEAP_MAX, getMaxHeap());

		props.put(JAVA_PERMGEN_USED, getPermgenUsed());
		props.put(JAVA_PERMGEN_MAX, getMaxPermgen());
		
		return props;
	}

	private String getPermgenUsed() {
		if (this.permgenBean != null) {
			return getFormattedNum(this.permgenBean.getUsage().getUsed());
		}
		return "Unknown";
	}

	private String getMaxPermgen() {
		if (this.permgenBean != null) {
			return getFormattedNum(this.permgenBean.getUsage().getMax());
		}
		return "Unknown";
	}

	private String getMaxHeap() {
		return getFormattedNum(Runtime.getRuntime().maxMemory());
	}

	private String getFormattedNum(long num) {
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		return nf.format(num);
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

			return StringUtils.join(this.runtimeMXBean.getInputArguments(), " ");
		}
		return "Unknown";
	}

	@Override
	public boolean isTomcat() {
		return this.isTomcat;
	}

	protected SisyphusPatternSource getPatternSourceByURL(String urlString) throws IOException, ClassNotFoundException,
			MalformedURLException {
		return new RemoteXmlPatternSource(new URL(urlString));
	}

	public String findTomcatFileOrDirectory(String fileOrDirectoryName) {
		String catalinaBase = System.getProperty("catalina.base");
		File file = new File(catalinaBase, fileOrDirectoryName);
		if (file.exists()) {
			return file.getAbsolutePath();

		}

		String catalinaHome = System.getProperty("catalina.home");
		file = new File(catalinaHome, fileOrDirectoryName);
		if (file.exists()) {
			return file.getAbsolutePath();
		}

		String workingDirectory = System.getProperty("working.dir");
		file = new File(workingDirectory + "../", fileOrDirectoryName);
		if (file.exists()) {
			return file.getAbsolutePath();
		}

		return null;
	}

	@Override
	public String getApplicationName() {
		return this.applicationProperties.getDisplayName();
	}

	@Override
	public String getApplicationVersion() {
		return this.applicationProperties.getVersion();
	}

	@Override
	public String getApplicationBuildNumber() {
		return this.applicationProperties.getBuildNumber();
	}

	@Override
	public Date getApplicationBuildDate() {
		return this.applicationProperties.getBuildDate();
	}

	@Override
	public String getApplicationHome() {
		return this.applicationProperties.getHomeDirectory().toString();
	}

	@Override
	public String getText(String key) {
		return this.i18nResolver.getText(key);
	}

	@Override
	public String getText(String key, Serializable... arguments) {
		return this.i18nResolver.getText(key, arguments);
	}

	@Override
	public String getServletContextPath(String pathToLookup) {
		return this.servletContext.getRealPath(pathToLookup);
	}

	@Override
	public void flagSelectedApplicationFileBundles(HttpServletRequest req) {
		List<ApplicationInfoBundle> selectedApplicationFileBundles = getSelectedApplicationInfoBundles(req);

		// We do this in the inverse so that the default is true. This supports
		// "check by default" but still preserves "unticked" boxes if we display
		// warnings.
		for (ApplicationInfoBundle bundle : this.applicationInfoBundles) {
			if (selectedApplicationFileBundles != null && selectedApplicationFileBundles.size() > 0) {
				if (!selectedApplicationFileBundles.contains(bundle)) {
					bundle.setSelected(false);
				}
			} else {
				bundle.setSelected(true);
			}
		}
	}

	@Override
	public List<ApplicationInfoBundle> getSelectedApplicationInfoBundles(HttpServletRequest req) {
		List<ApplicationInfoBundle> selectedApplicationInfoBundles = new ArrayList<ApplicationInfoBundle>();
		for (ApplicationInfoBundle applicationInfoBundle : this.applicationInfoBundles) {
			final String flagValue = req.getParameter(applicationInfoBundle.getKey());
			if (flagValue != null && Boolean.parseBoolean(flagValue)) {
				selectedApplicationInfoBundles.add(applicationInfoBundle);
			}
		}

		return selectedApplicationInfoBundles;
	}

	@Override
	public Map<String, String> getPropertiesByCategory(String category) {
		return this.propertiesByCategory.get(category);
	}

	@Override
	public Set<String> getPropertyCategories() {
		return this.propertiesByCategory.keySet();
	}

	public String saveProperties() {
		PropertyStore properties = loadProperties();
		Document doc = DocumentHelper.createDocument();
		
		Element root = new NonLazyElement("properties");
		doc.setRootElement(root);

		Element productElement = root.addElement("product");
		productElement.addAttribute("name", getApplicationName());
		productElement.addAttribute("version", getApplicationVersion());
		
		String rawTimezone = System.getProperty("user.timezone");
        TimeZone timeZone = TimeZone.getTimeZone(rawTimezone); // returns GMT if it can't parse (yuck!)
        int offsetMS = timeZone.getRawOffset() + (timeZone.inDaylightTime(new Date()) ? timeZone.getDSTSavings() : 0);
        int offsetHour = offsetMS/1000/60/60; // Note that this rounds timezones *down* to the nearest hour
		
		String timezoneStringInGMT = "GMT"+(offsetHour >= 0 ? "+" : "") + offsetHour;
		Element tzElement = root.addElement("timeZone");
		tzElement.setText(timezoneStringInGMT != null ? timezoneStringInGMT : UNKNOWN);

		Element senElement = root.addElement("sen");
		senElement.setText(getApplicationSEN() != null ? getApplicationSEN() : UNKNOWN);

		Element serverIdElement = root.addElement("serverId");
		serverIdElement.setText(getApplicationServerID() != null ? getApplicationServerID() : UNKNOWN);
		
		loadStore(properties,root);

		OutputFormat format = OutputFormat.createPrettyPrint();
		StringWriter stringWriter = new StringWriter();
		
		try {
			XMLWriter xmlWriter = new XMLWriter(stringWriter, format);
			xmlWriter.write(doc);
		} catch (Exception e) {
			log.error("Couldn't write XML output", e);
		} 

		return stringWriter.toString();
	}

	private void loadStore(PropertyStore store, Element element) {
        for (Entry<String,String> entry : store.getValues().entrySet()) {
			if (entry.getValue() != null && !StringUtils.isEmpty(entry.getValue())) {
				String key = xmlElementNames.getProperty(entry.getKey(),entry.getKey());
				try {
					Element valueElement = element.addElement(key);
					valueElement.setText(entry.getValue());
				}
				catch (IllegalAddException e) {
					log.error("Unable to add child element '" + key + "' to element '" + element.getName() + "'...", e);
				}
			}
		}
		for (Entry entry : store.getCategories().entrySet()) {
			String key = xmlElementNames.getProperty((String)entry.getKey(),(String)entry.getKey());

			if (entry.getValue() instanceof PropertyStore) {
 				Element childElement = element.addElement(key);
				loadStore((PropertyStore) entry.getValue(),childElement);
			}
			else if (entry.getValue() instanceof ArrayList) {
				ArrayList categoryList = (ArrayList) entry.getValue();
				for (Object childObject : categoryList) {
					if (childObject instanceof PropertyStore) {
						Element listChildElement = element.addElement(key);
						loadStore((PropertyStore) childObject,listChildElement);
					}
					else {
						log.warn("Couldn't add child object of type '" + entry.getValue().getClass().getCanonicalName() + "' with key '" + key + "' to PropertyStore.");
					}
				}
			}
			else {
				log.warn("Couldn't add object of type '" + entry.getValue().getClass().getCanonicalName() + "' with key '" + key + "' to PropertyStore.");
			}
		}
	}

	@Override
	public TemplateRenderer getTemplateRenderer() {
		return this.renderer;
	}

	@Override
	public String getBaseURL(HttpServletRequest req) {
		return req.getRequestURI().replaceFirst(req.getServletPath() + ".*", "");
	}

	@Override
	public List<String> getSystemWarnings() {
		return Collections.emptyList();
	}

	/**
	 * @return a readable version of the current container, or "Unknown".
	 */
	public String getAppServer() {
		switch (Container.get()) {
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

	@Override
	public String getExportDirectory() {
		return getApplicationHome() + "/export";
	}

	@Override
	public String getFromAddress() {
		return "noreply@atlassian.com";
	}
}
