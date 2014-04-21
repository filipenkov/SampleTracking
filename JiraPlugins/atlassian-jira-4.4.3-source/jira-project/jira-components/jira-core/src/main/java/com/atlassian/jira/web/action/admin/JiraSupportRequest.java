package com.atlassian.jira.web.action.admin;

import com.atlassian.core.util.DataUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.util.ByteArrayDataSource;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.startup.PluginInfoProvider;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.log.JiraLogLocator;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtils;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtilsImpl;
import com.atlassian.jira.util.system.check.SystemEnvironmentChecklist;
import com.atlassian.jira.util.system.patch.AppliedPatchInfo;
import com.atlassian.jira.util.system.patch.AppliedPatches;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@WebSudoRequired
public class JiraSupportRequest extends JiraWebActionSupport
{
    private static final Logger LOG = Logger.getLogger(JiraSupportRequest.class);

    private static final String SUBJECT_PREFIX = "[JIRA Support Request]";
    private static final String JSR_VERSION_NAME = "X-JIRA-Support-Request-Version";
    private static final String JSR_VERSION = "1.0";
    private static final String JSR_PROPERTIES_NAME = "JiraSupportRequestProperties.properties";
    private static final String JSR_LEVEL_SEP = "_";

    private static final String VIEW_DONE = "done";
    private static final String EMAIL_TEMPLATES = "templates/email/";
    private static final String EMAIL_TEMPLATE = "text/jirasupportrequest.vm";
    private static final String PLUGIN_ENABLED_KEY = "admin.systeminfo.plugin.enabled";
    private static final String PLUGIN_DISABLED_KEY = "admin.systeminfo.plugin.disabled";

    private final ExtendedSystemInfoUtils systemInfoUtils;
    private final JiraLicenseService jiraLicenseService;
    private final JiraLogLocator locator;
    private final LocaleManager localeManager;
    private final FileFactory fileFactory;

    private String to;
    private String cc;
    private String subject;
    private boolean attachzipexport = false;
    private boolean attachLogs = false;
    private String description;

    private String supportIssueKey;
    private String invalidAddresses = null;
    // contact info
    private String name;

    private String email;
    private String phone;
    private List<String> warningMessages;
    private LicenseDetails licenseDetails;
    private final PluginInfoProvider pluginInfoProvider;

    public JiraSupportRequest(final JiraLicenseService jiraLicenseService, final JiraLogLocator locator, final LocaleManager localeManager, FileFactory fileFactory, final PluginInfoProvider pluginInfoProvider)
    {
        // JRA-14021 support request emails will always send the system info in English. Note that this should NOT affect
        // other presentation in the view JSP - we still want other form elements to display in the correct language.
        // The only section that should be affected is everything displayed inside "Support Request Environment"
        this(new ExtendedSystemInfoUtilsImpl(new I18nBean(Locale.ENGLISH)), jiraLicenseService, locator, localeManager, fileFactory, pluginInfoProvider);
    }

    protected JiraSupportRequest(final ExtendedSystemInfoUtils extendedSystemInfoUtil,
            final JiraLicenseService jiraLicenseService, final JiraLogLocator locator, final LocaleManager localeManager,
            final FileFactory fileFactory, final PluginInfoProvider pluginInfoProvider)
    {
        this.localeManager = localeManager;
        this.pluginInfoProvider = notNull("pluginInfoProvider", pluginInfoProvider);
        this.systemInfoUtils = notNull("extendedSystemInfoUtil", extendedSystemInfoUtil);
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.locator = notNull("locator", locator);
        this.fileFactory = notNull("fileFactory", fileFactory);

        //have to initialise these here
        name = getLoggedInUser().getDisplayName();
        email = getLoggedInUser().getEmailAddress();
    }

    @Override
    public String doDefault() throws Exception
    {
        if (!getLicenseDetails().isEntitledToSupport())
        {
            return "nosupport";
        }
        final ExternalLinkUtil externalLinkUtil = new ExternalLinkUtilImpl();

        to = externalLinkUtil.getProperty("external.link.jira.support.mail.to");
        cc = getLoggedInUser().getEmailAddress();
        attachzipexport = true;
        attachLogs = true;
        return super.doDefault();
    }

    @Override
    protected void doValidation()
    {
        final LicenseDetails licenseDetails = getLicenseDetails();
        if (!licenseDetails.isEntitledToSupport())
        {
            addErrorMessage(getText("admin.license.type.not.supported.error", licenseDetails.getDescription()));
        }

        if (!isHasMailServer())
        {
            addErrorMessage(getText("admin.supportrequest.no.mailserver.configured"));
        }

        if (TextUtils.stringSet(to))
        {
            addErrorIfEmailNotValid(to, "to", "admin.errors.must.specify.valid.to.address");
        }
        else
        {
            addError("to", getText("admin.errors.must.specify.at.least.one.to.address"));
        }

        if (TextUtils.stringSet(cc))
        {
            addErrorIfEmailNotValid(cc, "cc", "admin.errors.must.specify.valid.cc.address");
        }

        addErrorIfStringNotSet(subject, "subject", "admin.errors.must.specify.subject");
        addErrorIfStringNotSet(description, "description", "admin.errors.must.specify.description");
        addErrorIfStringNotSet(name, "name", "admin.errors.must.specify.name");

        if (TextUtils.stringSet(email))
        {
            addErrorIfEmailNotValid(email, "email", "admin.errors.must.specify.valid.contact.address");
        }
        else
        {
            addError("email", getText("admin.errors.must.specify.contact.email.address"));
        }

        super.doValidation();
    }

    private void addErrorIfStringNotSet(final String string, final String title, final String messageKey)
    {
        if (!TextUtils.stringSet(string))
        {
            addError(title, getText(messageKey));
        }
    }

    private void addErrorIfEmailNotValid(final String email, final String title, final String messageKey)
    {
        final StringTokenizer tokenzier = new StringTokenizer(email, ",");
        while (tokenzier.hasMoreTokens())
        {
            final String singleEmail = tokenzier.nextToken();
            if (!TextUtils.verifyEmail(singleEmail))
            {
                addError(title, getText(messageKey));
            }
        }
    }

    @Override
    protected String doExecute() throws Exception
    {
        final Map<String, Object> contextParams = new HashMap<String, Object>();

        // create multipart that includes the export and log
        final Multipart multipart = new MimeMultipart();

//        if (attachzipexport)
//        {
//            final String filename = getTempFileName();
//
//            try
//            {
//                final ActionResult aResult = CoreFactory.getActionDispatcher().execute(ActionNames.EXPORT,
//                        EasyMap.build("filename", filename, "useZip", Boolean.TRUE, "anonymiseData", Boolean.TRUE));
//                ActionUtils.checkForErrors(aResult);
//            }
//            catch (final Exception e)
//            {
//                log.error("Exception occurred exporting zip for Support Request: " + e, e);
//                addErrorMessage(TextUtils.plainTextToHtml("Exception occurred exporting zip for Support Request:" + e));
//                return ERROR;
//            }
//
//            multipart.addBodyPart(MailUtils.createAttachmentMimeBodyPart(filename));
//        }
//
//        if (attachLogs)
//        {
//            final String s = getLogPath();
//            if (s != null)
//            {
//                multipart.addBodyPart(MailUtils.createZippedAttachmentMimeBodyPart(s));
//            }
//            else
//            {
//                MimeBodyPart mimeBodyPart = new MimeBodyPart();
//                mimeBodyPart.setText("Unable to find atlassian-jira.log.");
//                multipart.addBodyPart(mimeBodyPart);
//            }
//        }

        final String baseURL = systemInfoUtils.getBaseUrl();
        final String encoding = "UTF-8";

        // pass system info as context params into velocity template
        contextParams.put("baseUrl", baseURL);
        contextParams.put("warningMessages", SystemEnvironmentChecklist.getEnglishWarningMessages());
        contextParams.put("appliedPatches", AppliedPatches.getAppliedPatches());
        contextParams.put("sysinfo", cloneNullSafe(systemInfoUtils.getProps()));
        contextParams.put("licenseinfo", systemInfoUtils.getLicenseInfo());
        contextParams.put("jvmstats", cloneNullSafe(systemInfoUtils.getJvmStats()));
        contextParams.put("memoryPools", systemInfoUtils.getMemoryPoolInformation());
        contextParams.put("buildstats", cloneNullSafe(systemInfoUtils.getBuildStats()));
        contextParams.put("commonConfig", cloneNullSafe(systemInfoUtils.getCommonConfigProperties()));
        contextParams.put("description", description);
        contextParams.put("name", getName());
        contextParams.put("email", getEmail());
        contextParams.put("phone", getPhone());
        contextParams.put("exportattached", attachzipexport);
        contextParams.put("listeners", getListenersForEmail());
        contextParams.put("services", getServicesForEmail());
        contextParams.put("plugins", getPluginsForEmail());
        contextParams.put("supportIssueKey", getSupportIssueKey());
        contextParams.put("installedLanguages", localeManager.getInstalledLocales());
        contextParams.put("defaultLanguage", systemInfoUtils.getDefaultLanguage());
        contextParams.put("isUsingSystemLocale", systemInfoUtils.isUsingSystemLocale());
        contextParams.put("paths", MapBuilder.newBuilder()
                .add("JIRA Home", systemInfoUtils.getJiraHomeLocation())
                .add("Entity Engine", systemInfoUtils.getEntityEngineXmlPath())
                .add("Index", systemInfoUtils.getIndexLocation())
                .add("Logs", systemInfoUtils.getLogPath())
                .toMap());

        if (systemInfoUtils.getUsageStats() != null)
        {
            contextParams.put("usageStats", systemInfoUtils.getUsageStats());
        }

        // this will save all the properties as an attachment to the email for easy parsing on our end.
        multipart.addBodyPart(createAttachmentMimeBodyPart(JSR_PROPERTIES_NAME, persistProperties(contextParams)));

        // we add HTML-ised properties after we built the properties attachment since there is no real need to include them
        contextParams.put("applicationPropertiesHTML", systemInfoUtils.getApplicationPropertiesFormatted("\n"));

        // we add HTML-ised properties after we built the properties attachment since there is no real need to include them
        contextParams.put("systemPropertiesHTML", systemInfoUtils.getSystemPropertiesFormatted("\n"));

        // now generate the body of the email - this process will use HTML-ised properties
        final String body = ManagerFactory.getVelocityManager().getEncodedBody(EMAIL_TEMPLATES, EMAIL_TEMPLATE, baseURL, encoding, contextParams);

        final Email email = new Email(to);
        email.setCc(cc);
        email.setSubject(SUBJECT_PREFIX + " " + subject);
        email.setBody(body);
        email.setMimeType("text/plain");
        email.setEncoding(encoding);
        email.setMultipart(multipart);
        email.addHeader(JSR_VERSION_NAME, JSR_VERSION);

        if (!sendEmail(email))
        {
            return ERROR;
        }

        return VIEW_DONE;
    }

    /**
     * Produces a mimebodypart object from the data to be used as an attachment.
     *
     * @param attachmentName the name of the attachment
     * @param dataSource the data source that makes up the attachment
     * @return a MIME BodyPart
     * @throws MessagingException if things go wrong
     */
    private static BodyPart createAttachmentMimeBodyPart(final String attachmentName, final DataSource dataSource)
            throws MessagingException
    {
        final MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler(dataSource));
        attachmentPart.setFileName(attachmentName);
        return attachmentPart;
    }

    /**
     * Turns the content of the map into {@link java.util.Properties} and uses the standard store() method to persist it
     * into a String.
     *
     * @param srcMap the source Map of keys and values
     * @return a String created via {@link java.util.Properties#store(java.io.OutputStream, String)}
     */
    DataSource persistProperties(final Map srcMap)
    {
        final Properties targetProperties = new Properties();
        copyProperties(srcMap, targetProperties, "");

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = null;
        try
        {
            try
            {
                targetProperties.store(out, null);
                in = new ByteArrayInputStream(out.toByteArray());
                return new ByteArrayDataSource(in, "text/plain");
            }
            catch (final IOException e)
            {
                throw new IllegalStateException("Problem creating support request properties file. How can this happen with a memory stream?");
            }
        }
        finally
        {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    private void copyProperties(final Map<?, ?> srcMap, final Properties targetProperties, final String path)
    {
        for (Map.Entry<?, ?> entry : srcMap.entrySet())
        {
            final String key = getStringNotNull(entry.getKey());
            final Object value = entry.getValue();
            copyProperty(targetProperties, path + key, value);
        }
    }

    boolean sendEmail(final Email email)
    {
        try
        {
            // Send mail directly, don't place on queue.  Allows for immediate feedback if it fails.
            final SingleMailQueueItem singleMailQueueItem = new SingleMailQueueItem(email);
            singleMailQueueItem.send();
        }
        catch (final Exception e)
        {
            log.error("Unable to send Support Request", e);
            if (e.getCause() instanceof SendFailedException)
            {
                // More than likely the email address was wrong.
                final SendFailedException sfe = (SendFailedException) e.getCause();
                StringBuffer invalidAddressesSB = null;
                final Address[] addresses = sfe.getInvalidAddresses();
                if (addresses != null)
                {
                    for (final Address address : addresses)
                    {
                        if (invalidAddressesSB == null)
                        {
                            invalidAddressesSB = new StringBuffer(address.toString());
                        }
                        else
                        {
                            invalidAddressesSB.append(", ");
                            invalidAddressesSB.append(address.toString());
                        }
                    }
                    invalidAddresses = invalidAddressesSB == null ? null : invalidAddressesSB.toString();
                }
            }

            return false;

        }
        return true;
    }

    private void copyProperty(final Properties targetProperties, String key, final Object value)
    {
        if (value instanceof Map)
        {
            if (key.length() > 0)
            {
                key = key + JSR_LEVEL_SEP;
            }

            copyProperties((Map) value, targetProperties, key);
        }
        else if (value instanceof Collection)
        {
            if (key.length() > 0)
            {
                key = key + JSR_LEVEL_SEP;
            }

            int counter = 1;
            for (final Iterator iterator = ((Collection) value).iterator(); iterator.hasNext(); counter++)
            {
                final Object collectionValue = iterator.next();
                final String childKey = key + String.valueOf(counter);
                copyProperty(targetProperties, childKey, collectionValue);
            }
        }
        else
        {
            targetProperties.setProperty(key, getStringNotNull(value));
        }
    }

    private Collection getListenersForEmail()
    {
        final Collection<GenericValue> listeners = systemInfoUtils.getListeners();
        final Collection<String> listenerStrings = new ArrayList<String>(listeners.size());
        for (GenericValue gv : listeners)
        {
            final StringBuffer sb = new StringBuffer();
            sb.append(gv.getString("name"));
            sb.append(" (");
            sb.append(gv.getString("clazz"));
            sb.append(")");
            final PropertySet propertySet = getPropertySet(gv);
            final Collection keys = propertySet.getKeys("", 5);
            if ((keys != null) && !keys.isEmpty())
            {
                sb.append(" [");
                for (final Iterator j = keys.iterator(); j.hasNext();)
                {
                    final String key = (String) j.next();
                    sb.append(key);
                    sb.append("=");
                    sb.append(propertySet.getString(key));
                    if (j.hasNext())
                    {
                        sb.append(", ");
                    }
                }
                sb.append("]");
            }
            listenerStrings.add(sb.toString());
        }
        return listenerStrings;
    }

    private Collection getServicesForEmail()
    {
        final Collection<JiraServiceContainer> services = systemInfoUtils.getServices();
        final Collection<String> serviceStrings = new ArrayList<String>(services.size());
        for (final JiraServiceContainer service : services)
        {
            final StringBuffer sb = new StringBuffer();
            sb.append(service.getName());
            sb.append(" (");
            sb.append(service.getServiceClass());
            sb.append(") : ");
            sb.append(systemInfoUtils.getMillisecondsToMinutes(service.getDelay()));
            sb.append("min ");
            try
            {
                appendAttributes(systemInfoUtils.getServicePropertyMap(service), sb);
            }
            catch (final Exception ex)
            {
                LOG.debug("Error reading service properties", ex);
            }
            serviceStrings.add(sb.toString());
        }
        return serviceStrings;
    }

    private Collection<String> getPluginsForEmail()
    {
        final Collection<String> pluginStrings = new ArrayList<String>();

        pluginStrings.add("") ;
        pluginStrings.add("----- User Installed Plugins ----------------------\n") ;
        addPluginInfo(pluginInfoProvider.getUserPlugins(), pluginStrings);

        pluginStrings.add("") ;
        pluginStrings.add("----- System Plugins ----------------------\n") ;
        addPluginInfo(pluginInfoProvider.getSystemPlugins(), pluginStrings);
        return pluginStrings;
    }

    private void addPluginInfo(Collection<PluginInfoProvider.Info> plugins, Collection<String> pluginStrings)
    {
        for (final PluginInfoProvider.Info plugin : plugins)
        {
            final PluginInformation pluginInformation = plugin.getPluginInformation();
            final StringBuffer sb = new StringBuffer();
            sb.append(plugin.getName());
            sb.append(" ");
            sb.append(pluginInformation.getVersion());

            appendAttributes(pluginInformation.getParameters(), sb);

            sb.append(" - ");
            sb.append(pluginInformation.getVendorName());
            sb.append(" : ");
            sb.append(getText(plugin.isEnabled() ? PLUGIN_ENABLED_KEY : PLUGIN_DISABLED_KEY));
            pluginStrings.add(sb.toString());
        }
    }

    protected String getTempFileName() throws IOException
    {
        // make zip export
        final File file = fileFactory.getFile("temp").createTempFile("export", "");
        String filename = file.getAbsolutePath();
        filename = DataUtils.getZipFilename(filename.trim());
        return filename;
    }

    public String getSupportRequestMessage()
    {
        return getLicenseDetails().getSupportRequestMessage(this, getOutlookDate());
    }

    private LicenseDetails getLicenseDetails()
    {
        if (licenseDetails == null)
        {
            licenseDetails = jiraLicenseService.getLicense();
        }
        return licenseDetails;
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(final String to)
    {
        this.to = to;
    }

    public String getCc()
    {
        return cc;
    }

    public void setCc(final String cc)
    {
        this.cc = cc;
    }

    public void setSubject(final String subject)
    {
        this.subject = subject;
    }

    public String getSubject()
    {
        return subject;
    }

    public boolean getAttachzipexport()
    {
        return attachzipexport;
    }

    public boolean isMailSendingDisabled()
    {
        return MailFactory.isSendingDisabled();
    }

    public boolean isHasMailServer()
    {
        try
        {
            return (ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer() != null);
        }
        catch (final MailException e)
        {
            addErrorMessage(getText("admin.errors.retrieving.mail.server"));
            log.error("Error occurred while retrieving mail server information.", e);
            return false;
        }
    }

    public void setAttachzipexport(final boolean attachzipexport)
    {
        this.attachzipexport = attachzipexport;
    }

    public boolean getAttachlogs()
    {
        return attachLogs;
    }

    public void setAttachlogs(final boolean attachLogs)
    {
        this.attachLogs = attachLogs;
    }

    public String getLogPath()
    {
        return locator.findJiraLogFile().getAbsolutePath();
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(final String email)
    {
        this.email = email;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(final String phone)
    {
        this.phone = phone;
    }

    /**
     * Appends a list of attribute name-values from the attribute map to the string buffer.
     *
     * @param attributeMap map of attributes
     * @param sb string buffer
     */
    private void appendAttributes(final Map attributeMap, final StringBuffer sb)
    {
        if ((attributeMap != null) && !attributeMap.isEmpty())
        {
            sb.append(" [");
            for (final Iterator j = attributeMap.entrySet().iterator(); j.hasNext();)
            {
                final Map.Entry entry = (Map.Entry) j.next();
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(getText((String) entry.getValue()));
                if (j.hasNext())
                {
                    sb.append(", ");
                }
            }
            sb.append("]");
        }
    }

    /**
     * Creates and returns a new map that is a copy of the given map. If map contains null as a key or a value, this is
     * replaced by empty string.
     *
     * @param map map to clone
     * @return new map
     */
    private Map<String, String> cloneNullSafe(final Map<?, ?> map)
    {
        final Map<String, String> retMap = new HashMap<String, String>(map.size());
        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            final String key = getStringNotNull(entry.getKey());
            final String value = getStringNotNull(entry.getValue());
            retMap.put(key, value);
        }
        return retMap;
    }

    /**
     * If the object given is null returns an empty string, otherwise returns the result of toString() method call
     *
     * @param o object to check
     * @return empty string or string rpresentation of the given object
     */
    private String getStringNotNull(final Object o)
    {
        return o == null ? "" : o.toString();
    }

    public String getSupportIssueKey()
    {
        return supportIssueKey;
    }

    public void setSupportIssueKey(final String supportIssueKey)
    {
        this.supportIssueKey = supportIssueKey;
    }

    public String getInvalidAddresses()
    {
        return invalidAddresses;
    }

    public ExtendedSystemInfoUtils getExtendedSystemInfoUtils()
    {
        return systemInfoUtils;
    }

    public List<String> getWarningMessages()
    {
        if (warningMessages == null)
        {
            warningMessages = SystemEnvironmentChecklist.getEnglishWarningMessages();
        }
        return warningMessages;
    }

    public Set<AppliedPatchInfo> getAppliedPatches()
    {
        return AppliedPatches.getAppliedPatches();
    }
}
