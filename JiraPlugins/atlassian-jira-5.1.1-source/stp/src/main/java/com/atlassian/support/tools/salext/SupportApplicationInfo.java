
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
import com.atlassian.support.tools.properties.PropertyStore;
import com.atlassian.support.tools.salext.bundle.ApplicationInfoBundle;
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
	public static final String APPLICATION_BASE_URL = "stp.properties.application.base.url";
	public static final String APPLICATION_BUILD_NUMBER = "stp.properties.application.build.number";
	public static final String APPLICATION_BUILD_TIMESTAMP = "stp.properties.application.build.timestamp";
	public static final String APPLICATION_BUILD_VERSION = "stp.properties.application.build.version"; 
	public static final String APPLICATION_DISPLAY_NAME = "stp.properties.application.display.name"; 
	public static final String APPLICATION_HOME = "stp.properties.application.home";
	public static final String APPLICATION_INFO = "stp.properties.application.info";
	public static final String APPLICATION_PROPERTIES = "stp.properties.application.properties";
	public static final String APPLICATION_SERVER = "stp.properties.application.server";
	public static final String APPLICATION_START_TIME = "stp.properties.application.start.time";
	public static final String APPLICATION_TIME_ZONE = "stp.properties.application.time.zone";
	public static final String APPLICATION_UPTIME = "stp.properties.application.uptime";
	public static final String APPLICATION_VERSION = "stp.properties.application.version";
	public static final String ATTACHMENT_DATA_STORE = "stp.properties.attachment.data.store";
	public static final String ATTACHMENT_MAX_SIZE = "stp.properties.attachment.max.size";
	public static final String ATTACHMENT_UI_MAX = "stp.properties.attachment.ui.max";
	public static final String BACKUP_DATE_FORMAT_PATTERN = "stp.properties.backup.date.format.pattern";
	public static final String BACKUP_FILE_PREFIX = "stp.properties.backup.file.prefix";
	public static final String BACKUP_PATH = "stp.properties.backup.path";
	public static final String CACHE_DIRECTORY = "stp.properties.cache.directory";
	public static final String CAPTCHA_ENABLED = "stp.properties.captcha.enabled";
	public static final String CAPTCHA_GROUPS = "stp.properties.captcha.groups";
	public static final String CAPTCHA_MAX_ATTEMPTS = "stp.properties.captcha.max.attempts"; 
	public static final String CONFIG_DIRECTORY = "stp.properties.config.directory";
	public static final String CONFIG_INFO = "stp.properties.config.info";
	public static final String CURRENT_DIRECTORY = "stp.properties.current.directory";
	public static final String DB = "stp.properties.db";
	public static final String DB_CONNECTION_TRANSACTION_ISOLATION = "stp.properties.db.connection.transaction.isolation";
	public static final String DB_CONNECTION_URL = "stp.properties.db.connection.url";
	public static final String DB_DIALECT = "stp.properties.db.dialect";
	public static final String DB_DRIVER_CLASS = "stp.properties.db.driver.class";
	public static final String DB_DRIVER_NAME = "stp.properties.db.driver.name";
	public static final String DB_DRIVER_VERSION = "stp.properties.db.driver.version";
	public static final String DB_EXAMPLE_LATENCY = "stp.properties.db.example.Latency";
	public static final String DB_NAME = "stp.properties.db.name";
	public static final String DB_STATISTICS = "stp.properties.db.statistics";
	public static final String DB_VERSION = "stp.properties.db.version";
	public static final String DEFAULT_ENCODING = "stp.properties.default.encoding";
	public static final String FILE_SYSTEM_ENCODING = "stp.properties.file.system.encoding";
	public static final String FREE_DISK_SPACE = "stp.properties.free.disk.space";
	public static final String GLOBAL_DEFAULT_LOCALE = "stp.properties.global.default.locale";
	public static final String HOST_NAME = "stp.properties.host.name";
	public static final String INDEX_SIZE = "stp.properties.index.size";
	public static final String INDEXING_LANGUAGE = "stp.properties.indexing.language";
	public static final String INSTALLED_LANGUAGES = "stp.properties.languages.installed";
	public static final String IP_ADDRESS = "stp.properties.ip.address";
	public static final String JAVA_HEAP_ALLOCATED = "stp.properties.java.heap.allocated";
	public static final String JAVA_HEAP_AVAILABLE = "stp.properties.java.heap.available";
	public static final String JAVA_HEAP_FREE_ALLOCATED = "stp.properties.java.heap.free.allocated";
	public static final String JAVA_HEAP_MAX = "stp.properties.java.heap.max";
	public static final String JAVA_HEAP_PERCENT_USED = "stp.properties.java.heap.percent.used";
	public static final String JAVA_HEAP_USED = "stp.properties.java.heap.used";
	public static final String JAVA_MEMORY_FREE = "stp.java.memory.free";
	public static final String JAVA_PERMGEN_AVAILABLE = "stp.properties.java.permgen.available";
	public static final String JAVA_PERMGEN_MAX = "stp.properties.java.permgen.max";
	public static final String JAVA_PERMGEN_PERCENT_USED = "stp.properties.java.permgen.percent.used";
	public static final String JAVA_PERMGEN_USED = "stp.properties.java.permgen.used";
	public static final String JAVA_RUNTIME = "stp.properties.java.runtime";
	public static final String JAVA_VENDOR = "stp.properties.java.vendor";
	public static final String JAVA_VERSION = "stp.properties.java.version";
	public static final String JAVA_VM = "stp.properties.java.vm";
	public static final String JAVA_VM_ARGUMENTS = "stp.properties.java.vm.arguments";
	public static final String JAVA_VM_VENDOR = "stp.properties.java.vm.vendor";
	public static final String JAVA_VM_VERSION = "stp.properties.java.vm.version";
	public static final String LANGUAGE_COUNTRY = "stp.properties.languages.language.country";
	public static final String LANGUAGE_NAME = "stp.properties.languages.language.name";
	public static final String LANGUAGE_ABBREVIATION = "stp.properties.languages.language.abbreviation";
	public static final String LANGUAGES_DEFAULT = "stp.properties.languages.default";
	public static final String LANGUAGES_LANGUAGE = "stp.properties.languages.language";
	public static final String LICENSE = "stp.properties.license";
	public static final String LICENSE_ACTIVE_USERS = "stp.properties.license.users.active";
	public static final String LICENSE_DESCRIPTION = "stp.properties.license.description";
	public static final String LICENSE_EDITION = "stp.properties.license.edition";
	public static final String LICENSE_EXPIRES = "stp.properties.license.expires";
	public static final String LICENSE_EXPIRES_NONE = "stp.properties.license.expires.none";
	public static final String LICENSE_INFO = "stp.properties.license.info";
	public static final String LICENSE_NEVER_EXPIRES = "stp.properties.license.never.expires";
	public static final String LICENSE_ORGANISATION = "stp.properties.license.organisation";
	public static final String LICENSE_OWNER = "stp.properties.license.owner";
	public static final String LICENSE_PARTNER = "stp.properties.license.partner";
	public static final String LICENSE_PRODUCT = "stp.properties.license.product";
	public static final String LICENSE_PURCHASED = "stp.properties.license.purchased";
	public static final String LICENSE_SEN = "stp.properties.license.sen";
	public static final String LICENSE_SERVER_ID = "stp.properties.license.server.id";
	public static final String LICENSE_SUPPORT_PERIOD = "stp.properties.license.period";
	public static final String LICENSE_TYPE = "stp.properties.license.type";
	public static final String LICENSE_UNLIMITED_USERS = "stp.properties.license.unlimited.users";
	public static final String LICENSE_USERS = "stp.properties.license.users";
	public static final String LINK = "stp.properties.links"; 
	public static final String LINK_DISPLAY_URL = "stp.properties.links.url.display"; 
	public static final String LINK_NAME = "stp.properties.links.name"; 
	public static final String LINK_PRIMARY = "stp.properties.links.primary"; 
	public static final String LINK_RPC_URL = "stp.properties.links.url.rpc"; 
	public static final String LINK_TYPE = "stp.properties.links.type"; 
	public static final String LISTENERS = "stp.properties.listeners";
	public static final String LISTENERS_LISTENER = "stp.properties.listeners.listener";
	public static final String LISTENERS_LISTENER_CLAZZ = "stp.properties.listeners.listener.clazz";
	public static final String LISTENERS_LISTENER_NAME = "stp.properties.listeners.listener.name";
	public static final String LOG_DIRECTORY = "stp.properties.log.directory";
	public static final String MAIL = "stp.properties.mail"; 
    public static final String MAIL_SERVER_ADDRESS = "stp.properties.mail.server.address"; 
    public static final String MAIL_SERVER_HOSTNAME = "stp.properties.mail.server.hostname"; 
    public static final String MAIL_SERVER_PORT = "stp.properties.mail.server.port"; 
    public static final String MAIL_USERNAME = "stp.properties.mail.username"; 
    public static final String MAIL_USE_TLS = "stp.properties.mail.use.tls"; 
	public static final String MEMORY = "stp.properties.memory";
	public static final String MEMORY_STATISTICS = "stp.properties.memory.statistics";
	public static final String MEMORY_TOTAL = "stp.properties.memory.total";
	public static final String MEMORY_USED = "stp.properties.memory.used";
	public static final String MODZ = "stp.properties.modz";
	public static final String MODZ_FILE = "stp.properties.modz.file";
	public static final String MODZ_MODIFIED = "stp.properties.modz.modified";
	public static final String MODZ_REMOVED = "stp.properties.modz.removed";
	public static final String NOT_SET = "stp.properties.not.set";
	public static final String PATCHES = "stp.properties.patches";
	public static final String PATCHES_PATCH = "stp.properties.patches.patch";
	public static final String PATCHES_PATCH_DESCRIPTION = "stp.properties.patches.patch.description";
	public static final String PATCHES_PATCH_KEY = "stp.properties.patches.patch.key";
	public static final String PATH_INFO = "stp.properties.path.info";
	public static final String PLUGIN_CACHE_DIRECTORY = "stp.properties.plugin.cache.directory";
	public static final String PLUGIN_DIRECTORY = "stp.properties.plugin.directory";
	public static final String PLUGIN_FRAMEWORK_VERSION = "stp.properties.plugins.plugin.framework.version";
	public static final String PLUGIN_KEY = "stp.properties.plugins.plugin.key";
	public static final String PLUGIN_NAME = "stp.properties.plugins.plugin.name";
	public static final String PLUGIN_STATUS = "stp.properties.plugins.plugin.status";
	public static final String PLUGIN_USER_INSTALLED = "stp.properties.plugins.plugin.user.installed";
	public static final String PLUGIN_VENDOR = "stp.properties.plugins.plugin.vendor";
	public static final String PLUGIN_VENDOR_URL = "stp.properties.plugins.plugin.vendor.url";
	public static final String PLUGIN_VERSION = "stp.properties.plugins.plugin.version";
	public static final String PLUGINS = "stp.properties.plugins";
	public static final String PLUGINS_PLUGIN = "stp.properties.plugins.plugin";
    public static final String PROJECT = "stp.properties.projects.project"; 
    public static final String PROJECT_DESCRIPTION = "stp.properties.projects.project.description"; 
    public static final String PROJECT_KEY = "stp.properties.projects.project.key"; 
    public static final String PROJECT_NAME = "stp.properties.projects.project.name"; 
	public static final String QUICKNAV_MAX_REQUESTS = "stp.properties.quicknav.max.requests";
	public static final String REPOSITORIES = "stp.properties.repositories";
	public static final String REPOSITORIES_REPOSITORY = "stp.properties.repository";
	public static final String REPOSITORIES_REPOSITORY_NAME = "stp.properties.repository.name";
	public static final String REPOSITORIES_REPOSITORY_STATE = "stp.properties.repository.state";
	public static final String REPOSITORIES_REPOSITORY_TYPE = "stp.properties.repository.type";
    public static final String REPOSITORIES_REPOSITORY_SIZE = "stp.properties.repository.size"; 
    public static final String REPOSITORIES_REPOSITORY_SLUG = "stp.properties.repository.slug"; 
    public static final String REPOSITORIES_REPOSITORY_STATUS_MESSAGE = "stp.properties.repository.status-message"; 
	public static final String RESOURCE_LIMITS = "stp.properties.resource.limits";
	public static final String RSS_MAX_ITEMS = "stp.properties.rss.max.items";
	public static final String SERVICES = "stp.properties.services";
	public static final String SERVICES_SERVICE = "stp.properties.services.service";
	public static final String SERVICES_SERVICE_DELAY = "stp.properties.services.service.delay";
	public static final String SERVICES_SERVICE_DESCRIPTION = "stp.properties.services.service.description";
	public static final String SERVICES_SERVICE_LAST_RUN = "stp.properties.services.service.last.run";
	public static final String SERVICES_SERVICE_NAME = "stp.properties.services.service.name";
	public static final String SERVICES_SERVICE_STATUS = "stp.properties.services.service.status";
	public static final String SOURCE_CONTROL = "stp.properties.source.control";
	public static final String STATISTICS = "stp.properties.statistics";
	public static final String SYSTEM = "stp.properties.system";
	public static final String SYSTEM_AVAILABLE_PROCESSORS = "stp.properties.system.available.processors";
	public static final String SYSTEM_DATE = "stp.properties.system.date";
	public static final String SYSTEM_ENCODING = "stp.properties.system.encoding";
	public static final String SYSTEM_INFO = "stp.properties.system.info";
	public static final String SYSTEM_LANGUAGE = "stp.properties.system.language";
	public static final String SYSTEM_OS = "stp.properties.os";
	public static final String SYSTEM_OS_ARCH = "stp.properties.os.architecture";
	public static final String SYSTEM_OS_VERSION = "stp.properties.os.version";
	public static final String SYSTEM_TIME = "stp.properties.system.time";
	public static final String SYSTEM_TIMEZONE = "stp.properties.system.time";
	public static final String SYSTEM_WORKING_DIRECTORY = "stp.properties.system.working.directory";
	public static final String TEMP_DIRECTORY = "stp.properties.system.temp.directory";
	public static final String UPGRADE = "stp.properties.upgrade";
	public static final String UPGRADE_BUILD = "stp.properties.upgrade.build";
	public static final String UPGRADE_TIME = "stp.properties.upgrade.time";
	public static final String UPGRADE_VERSION = "stp.properties.upgrade.version";
	public static final String USAGE = "stp.properties.usage";
	public static final String USAGE_INDEX_SIZE = "stp.properties.usage.index.size";
	public static final String USAGE_LOCAL_GROUPS = "stp.properties.usage.local.groups";
	public static final String USAGE_LOCAL_USERS = "stp.properties.usage.local.users";
	public static final String USER_HOME = "stp.properties.user.home";
	public static final String USER_LOCALE = "stp.properties.user.locale";
	public static final String USER_MANAGEMENT = "stp.properties.user.management";
	public static final String USER_NAME = "stp.properties.user.name";
	public static final String USER_TIMEZONE = "stp.properties.user.timezone";
	public static final String ZIP_INCLUDE_AUTH_CFG = "stp.zip.include.auth.cfg";
	public static final String ZIP_INCLUDE_AUTH_CFG_DESCRIPTION = "stp.zip.include.auth.cfg.description";
	public static final String ZIP_INCLUDE_CACHE_CFG = "stp.zip.include.cache.cfg";
	public static final String ZIP_INCLUDE_CACHE_CFG_DESCRIPTION = "stp.zip.include.cache.cfg.description";
	public static final String ZIP_INCLUDE_JIRA_CFG = "stp.zip.include.jira.cfg";
	public static final String ZIP_INCLUDE_JIRA_CFG_DESCRIPTION = "stp.zip.include.jira.cfg.description";
	public static final String ZIP_INCLUDE_JIRA_LOGS = "stp.zip.include.jira.logs";
	public static final String ZIP_INCLUDE_JIRA_LOGS_DESCRIPTION = "stp.zip.include.jira.logs.description";
	public static final String ZIP_INCLUDE_MODZ = "stp.zip.include.modz";
	public static final String ZIP_INCLUDE_MODZ_DESCRIPTION = "stp.zip.include.modz.description";
	public static final String ZIP_INCLUDE_TOMCAT_CONF = "stp.zip.include.tomcat.conf";
	public static final String ZIP_INCLUDE_TOMCAT_CONF_DESCRIPTION = "stp.zip.include.tomcat.conf.description";
	public static final String ZIP_INCLUDE_TOMCAT_LOGS = "stp.zip.include.tomcat.logs";
	public static final String ZIP_INCLUDE_TOMCAT_LOGS_DESCRIPTION = "stp.zip.include.tomcat.logs.description";

	public static final String CONFLUENCE_REGEX_XML = "https://confluence.atlassian.com/download/attachments/179443532/confluence_regex.xml";
	
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
	public PropertyStore loadProperties();

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
	
	public String saveProperties();
	
	/**
	 * Allows to display warnings on the system info screen of the plugin.
	 * @return
	 */
	public List<String> getSystemWarnings();

	public String getExportDirectory();

	public String getApplicationLogDir();
	
	/**
	 * The reply to address that notifications should appear to come from.
	 * @return A String representing an email address.
	 */
	public String getFromAddress();
}
