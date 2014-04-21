
package com.atlassian.support.tools.salext;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.support.tools.hercules.ScanItem;
import com.atlassian.support.tools.zip.FileSanitizer;
import com.atlassian.templaterenderer.TemplateRenderer;

/**
 * @author aatkins The SupportApplicationInfo interface is an
 *         application-independent class for providing enough information about
 *         logs, configuration settings, etc.. The information is a supplement
 *         to the information provided by SAL.
 */
public interface SupportApplicationInfo
{	
	public static final String APPLICATION_BASE_URL = "base.url";
	public static final String APPLICATION_BUILD_NUMBER = "build.number";
	public static final String APPLICATION_HOME = "application.home";
	public static final String APPLICATION_SERVER = "application.server";
	public static final String APPLICATION_START_TIME = "start.time";
	public static final String APPLICATION_TIME_ZONE = "time.zone";
	public static final String APPLICATION_UPTIME = "uptime";
	public static final String APPLICATION_VERSION = "application.version";
	public static final String ATTACHMENT_DATA_STORE = "attachment.data.store";
	public static final String ATTACHMENT_MAX_SIZE = "attachment.max.size";
	public static final String BACKUP_DATE_FORMAT_PATTERN = "backup.date.format.pattern";
	public static final String BACKUP_FILE_PREFIX = "backup.file.prefix";
	public static final String BACKUP_PATH = "backup.path";
	public static final String CAPTCHA_ENABLED = "captcha.enabled";
	public static final String CAPTCHA_GROUPS = "captcha.groups";
	public static final String DB_CONNECTION_TRANSACTION_ISOLATION = "connection.transaction.isolation";
	public static final String DB_CONNECTION_URL = "db.connection.url";
	public static final String DB_DIALECT = "db.dialect";
	public static final String DB_DRIVER_CLASS = "db.driver.class";
	public static final String DB_DRIVER_NAME = "db.driver.name";
	public static final String DB_DRIVER_VERSION = "db.driver.version";
	public static final String DB_EXAMPLE_LATENCY = "db.example.Latency";
	public static final String DB_NAME = "db.name";
	public static final String DB_VERSION = "db.version";
	public static final String DEFAULT_ENCODING = "default.encoding";
	public static final String FILE_SYSTEM_ENCODING = "file.system.encoding";
	public static final String GLOBAL_DEFAULT_LOCALE = "global.default.locale";
	public static final String INDEXING_LANGUAGE = "indexing.Language";
	public static final String INSTALLED_LANGUAGES = "installed.Languages";
	public static final String JAVA_HEAP_ALLOCATED = "java.heap.allocated";
	public static final String JAVA_HEAP_AVAILABLE = "java.heap.available";
	public static final String JAVA_HEAP_FREE_ALLOCATED = "java.heap.free.allocated";
	public static final String JAVA_HEAP_MAX = "java.heap.max";
	public static final String JAVA_HEAP_PERCENT_USED = "java.heap.percent.used";
	public static final String JAVA_HEAP_USED = "java.heap.used";
	public static final String JAVA_PERMGEN_AVAILABLE = "java.permgen.available";
	public static final String JAVA_PERMGEN_MAX = "java.permgen.max";
	public static final String JAVA_PERMGEN_PERCENT_USED = "java.permgen.percent.used";
	public static final String JAVA_PERMGEN_USED = "java.permgen.used";
	public static final String JAVA_RUNTIME = "java.runtime";
	public static final String JAVA_VENDOR = "java.vendor";
	public static final String JAVA_VERSION = "java.version";
	public static final String JAVA_VM = "java.vm";
	public static final String JAVA_VM_VENDOR = "java.vm.vendor";
	public static final String JAVA_VM_VERSION = "java.vm.version";
	public static final String JAVA_VM_ARGUMENTS = "java.vm.arguments";
	public static final String LICENSE_ACTIVE_USERS = "license.active.users";
	public static final String LICENSE_DESCRIPTION = "license.description";
	public static final String LICENSE_EXPIRES = "license.expires";
	public static final String LICENSE_ORGANISATION = "license.organisation";
	public static final String LICENSE_OWNER = "license.owner";
	public static final String LICENSE_PARTNER = "license.partner";
	public static final String LICENSE_PRODUCT = "license.product";
	public static final String LICENSE_SUPPORT_PERIOD = "license.support.period";
	public static final String LICENSE_TYPE = "licensed.type";
	public static final String LICENSE_USERS = "licensed.users";
	public static final String MAX_ATTACHMENTS_IN_UI = "max.attachments.in.ui";
	public static final String MAX_QUICK_NAV_REQUESTS = "max.simultaneous.quicknav.requests";
	public static final String MAX_RSS_ITEMS = "max.rss.items";
	public static final String MODZ_MODIFIED = "modz.modified";
	public static final String MODZ_REMOVED = "modz.removed";
	public static final String OPERATING_SYSTEM = "operating.system";
	public static final String OPERATING_SYSTEM_ARCHITECTURE = "operating.system.architecture";
	public static final String OPERATING_SYSTEM_VERSION = "operating.system.version";
	public static final String PLUGIN_COUNT_SUFFIX = "count";
	public static final String PLUGIN_FRAMEWORK_VERSION_SUFFIX = "framework.version";
	public static final String PLUGIN_NAME_SUFFIX = "name";
	public static final String PLUGIN_PLUGIN_PREFIX = "plugin";
	public static final String PLUGIN_STATUS_SUFFIX = "status";
	public static final String PLUGIN_VENDOR_SUFFIX = "vendor";
	public static final String PLUGIN_VENDOR_URL_SUFFIX = "vendor.url";
	public static final String PLUGIN_VERSION_SUFFIX = "version";
	public static final String SERVER_ID = "server.id";
	public static final String SUPPORT_ENTITLEMENT_NUMBER = "support.entitlement.number";
	public static final String SYSTEM_DATE = "system.date";
	public static final String SYSTEM_LANGUAGE = "system.language";
	public static final String SYSTEM_TIME = "system.time";
	public static final String SYSTEM_TIMEZONE = "system.timezone";
	public static final String TEMP_DIRECTORY = "temp.directory";
	public static final String USAGE_INDEX_SIZE = "usage.index.size";
	public static final String USAGE_LOCAL_GROUPS = "usage.local.groups";
	public static final String USAGE_LOCAL_USERS = "usage.local.users";
	public static final String USER_NAME = "user.name";
	public static final String USER_TIMEZONE = "user.timezone";
	public static final String WORKING_DIRECTORY = "working.directory";

	public static final String CONFLUENCE_REGEX_XML = "http://confluence.atlassian.com/download/attachments/179443532/confluence_regex.xml";
	
	/**
	 * NB: Must never return null.
	 * 
	 * @return Hercules will display the list of log files and let the user choose which to scan.
	 */
	public List<ScanItem> getApplicationLogFilePaths();

	/**
	 * @return The Hercules pattern source for this application (generally
	 *         derived from a URL).
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException
	 *             If the pattern URL is not valid, a MalformedURLException will
	 *             be returned.
	 */
	public SisyphusPatternSource getPatternSource() throws IOException, ClassNotFoundException, MalformedURLException;

	/**
	 * A method to collect the information that can only be determined once the
	 * Servlet container is available.
	 * 
	 * @param config
	 *            The servlet configuration (as a ServletConfig).
	 */
	public void initServletInfo(ServletConfig config);

	/**
	 * Whether or not the application is running on tomcat. This is used to
	 * determine which logs and configuration files to include.
	 * 
	 * @return true if the application is running on tomcat, or false if the
	 *         application is running on another application server.
	 */
	public boolean isTomcat();

	/**
	 * @return The location of the application's home directory
	 *         (confluence.home, jira.home).
	 */
	public String getApplicationHome();

	/**
	 * Provides access to files stored within the servlet context (things in the
	 * WEB-INF/lib directory, etc.)
	 * 
	 * @param pathToLookup
	 *            The short path (i.e. WEB-INF, WEB-INF/lib) to look up.
	 * @return The full path of the directory on the filesystem.
	 */
	public String getServletContextPath(String pathToLookup);

	/**
	 * @return The simple name of the application, used both for display and for
	 *         generating filenames.
	 */
	public String getApplicationName();

	public String getApplicationVersion();
	public String getApplicationSEN();
	public String getApplicationServerID();

	public String getApplicationBuildNumber();

	public Date getApplicationBuildDate();
	
	/**
	 * @return The current user's username. We get this from SAL when we can.
	 */
	public String getUserName();

	/**
	 * @return The current user's email address. We get this from SAL when we
	 *         can.
	 */
	public String getUserEmail();

	/**
	 * @param body
	 *            An i18n key or raw string.
	 * @return The internationalized text if an i18n key is found, otherwise the
	 *         original input.
	 */
	public String getText(String body);

	/**
	 * @param key
	 *            An i18n key
	 * @param arguments
	 *            One or more variables to be substituted for placeholders
	 *            ({0},{1}, etc.) in i18n properties.
	 * @return The internationalized string.
	 */
	public String getText(String key, Serializable... arguments);

	/**
	 * @return The email address to use when creating a support request.
	 */
	public String getCreateSupportRequestEmail();

	/**
	 * @return A list of ApplicationFileBundle objects containing the full range
	 *         of log files and configuration files available for this product.
	 */
	public List<ApplicationInfoBundle> getApplicationFileBundles();

	/**
	 * @param req
	 *            The HttpServletRequest passed by the servlet.
	 * @return A list of the the ApplicationFileBundle objects selected by the
	 *         user in the previous stage.
	 */
	public List<ApplicationInfoBundle> getSelectedApplicationInfoBundles(HttpServletRequest req);

	/**
	 * Load the application properties.
	 */
	// FIXME: ideally these warnings should go into the validaiton log and be displayed through its standard mechanism
	public void loadProperties();

	/**
	 * @return The template renderer provided by the application, we use it
	 *         instead of instantiating our own.
	 */
	public TemplateRenderer getTemplateRenderer();

	/**
	 * @param request
	 *            the HttpServletRequest object
	 * @return The URL of the mail queue view and administration screen within
	 *         the application
	 */
	public String getMailQueueURL(HttpServletRequest request);

	/**
	 * @param request
	 *            the HttpServletRequest object
	 * @return The base URL of the application (if configured).
	 */
	public String getBaseURL(HttpServletRequest request);

	/**
	 * Flag any application bundles passed as part of the request as "selected"
	 * 
	 * @param request
	 *            The HttpServletRequest provided by the servlet
	 */
	public void flagSelectedApplicationFileBundles(HttpServletRequest request);

	/**
	 * @param request
	 *            an HttpServletRequest object
	 * @return The URL to use to configure the mail server within the
	 *         application.
	 */
	public String getMailServerConfigurationURL(HttpServletRequest request);

	/**
	 * @return A FileSanitizer preinitialized with the filenames and patterns
	 *         for the product.
	 */
	public FileSanitizer getFileSanitizer();
	
	/**
	 * @param category The category of properties to return.
	 * @return The properties associated with this category.
	 */
	public Map<String, String> getPropertiesByCategory(String category);
	
	/**
	 * @return a Set of categories associated with our properties.
	 */
	public Set<String> getPropertyCategories();	
	
	// FIXME: merge these two methods of showing the properties
	public String savePropertiesForMail();
	public String savePropertiesForZip();
	
	/**
	 * Allows to display warnings on the system info screen of the plugin.
	 * @return
	 */
	public List<String> getSystemWarnings();
}
