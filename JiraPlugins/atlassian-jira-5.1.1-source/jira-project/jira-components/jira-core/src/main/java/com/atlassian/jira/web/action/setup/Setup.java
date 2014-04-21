package com.atlassian.jira.web.action.setup;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.export.ExportService;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.UrlValidator;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.util.index.Contexts;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.velocity.VelocityHelper;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class Setup extends AbstractSetupAction
{
    private String nextStep; //The button at the bottom of the page
    private String title;
    private String baseURL;
    private String mode = "public";
    private String licenseString;
    private String attachmentPath;
    private AttachmentPathManager.Mode attachmentMode = AttachmentPathManager.Mode.DEFAULT;
    private IndexPathManager.Mode indexMode = IndexPathManager.Mode.DEFAULT;
    private JiraLicenseService.ValidationResult validationResult;

    private final ExternalLinkUtil externalLinkUtil;

    private static enum DirectoryMode
    {
        DEFAULT,
        DISABLED
    }

    private DirectoryMode backupMode = DirectoryMode.DEFAULT;

    private String indexPath;

    private final IssueIndexManager indexManager;
    private final ServiceManager serviceManager;
    private final IndexPathManager indexPathManager;
    private final AttachmentPathManager attachmentPathManager;
    private final JiraHome jiraHome;
    private final JiraLicenseUpdaterService licenseService;
    private final JiraSystemRestarter jiraSystemRestarter;
    private final BuildUtilsInfo buildUtilsInfo;

    public Setup(final IssueIndexManager indexManager, final ServiceManager serviceManager, final IndexPathManager indexPathManager, final AttachmentPathManager attachmentPathManager, final JiraHome jiraHome, final JiraLicenseUpdaterService licenseService, final BuildUtilsInfo buildUtilsInfo, final JiraSystemRestarter jiraSystemRestarter, FileFactory fileFactory, ExternalLinkUtil externalLinkUtil)
    {
        super(fileFactory);
        this.indexManager = indexManager;
        this.serviceManager = serviceManager;
        this.indexPathManager = indexPathManager;
        this.attachmentPathManager = attachmentPathManager;
        this.jiraHome = jiraHome;
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.licenseService = notNull("licenseService", licenseService);
        this.jiraSystemRestarter = notNull("jiraSystemRestarter", jiraSystemRestarter);
        this.externalLinkUtil = notNull("externalLinkUtil", externalLinkUtil);
    }

    @Override
    public String doDefault() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        final ApplicationProperties applicationProperties = getApplicationProperties();

        title = applicationProperties.getString(APKeys.JIRA_TITLE);
        if (title == null)
        {
            title = "Your Company JIRA";
            applicationProperties.setString(APKeys.JIRA_TITLE, title);
        }

        if (applicationProperties.getString(APKeys.JIRA_BASEURL) != null)
        {
            baseURL = applicationProperties.getString(APKeys.JIRA_BASEURL);
        }

        if (applicationProperties.getString(APKeys.JIRA_MODE) != null)
        {
            mode = applicationProperties.getString(APKeys.JIRA_MODE);
        }

        if (attachmentPathManager.getAttachmentPath() != null)
        {
            attachmentPath = attachmentPathManager.getAttachmentPath();
        }

        if (indexPathManager.getIndexRootPath() != null)
        {
            indexPath = indexPathManager.getIndexRootPath();
        }

        // Check if license has been set yet.
        setupDefaultLicenceString();

        return forceRedirectToInput();
    }

    private void setupDefaultLicenceString()
    {
        final LicenseDetails details = licenseService.getLicense();
        if (details.isLicenseSet())
        {
            licenseString = details.getLicenseString();
        }
        else
        {
            licenseString = new DevModeSecretSauce().getPrefilledLicence();
        }
    }

    @Override
    protected void doValidation()
    {
        // return with no error messages, doExecute() will return the already setup view
        if ((nextStep == null) || setupAlready())
        {
            return;
        }

        if (!TextUtils.stringSet(title))
        {
            addError("title", getText("setup.error.specifytitle"));
        }

        if (!UrlValidator.isValid(baseURL))
        {
            addError("baseURL", getText("setup.error.baseURL"));
        }

        if (!getAllowedModes().keySet().contains(mode))
        {
            addError("mode", getText("setup.error.mode"));
        }

        // try to validate the license key
        validationResult = licenseService.validate(this, licenseString);
        final ErrorCollection errorCollection = validationResult.getErrorCollection();
        if (errorCollection.hasAnyErrors())
        {
            addErrorCollection(errorCollection);
        }
        super.doValidation();

    }
    public String doInput() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        licenseString = new DevModeSecretSauce().getPrefilledLicence();

        // this is a no op for redirecting
        return INPUT;
    }

    private String forceRedirectToInput()
    {
        final HashMap<String, String> params = new HashMap<String, String>();
        try
        {
            if (title != null)
            {
                params.put("title", VelocityHelper.encode(title, ComponentAccessor.getApplicationProperties().getEncoding()));
            }
            if (baseURL != null)
            {
                params.put("baseURL", VelocityHelper.encode(baseURL, ComponentAccessor.getApplicationProperties().getEncoding()));
            }
            if (mode != null)
            {
                params.put("mode", VelocityHelper.encode(mode, ComponentAccessor.getApplicationProperties().getEncoding()));
            }
            if (licenseString != null)
            {
                params.put("license", VelocityHelper.encode(licenseString, ComponentAccessor.getApplicationProperties().getEncoding()));
            }
            if (attachmentPath != null)
            {
                params.put("attachmentPath", VelocityHelper.encode(attachmentPath, ComponentAccessor.getApplicationProperties().getEncoding()));
            }
            if (indexPath != null)
            {
                params.put("indexPath", VelocityHelper.encode(indexPath, ComponentAccessor.getApplicationProperties().getEncoding()));
            }
            if (mode != null)
            {
                params.put("mode", VelocityHelper.encode(mode, ComponentAccessor.getApplicationProperties().getEncoding()));
            }
        }
        catch (final UnsupportedEncodingException e)
        {
            // do nothing
        }
        final StringBuilder redirectStr = new StringBuilder("Setup!input.jspa");
        int i = 0;
        for (final Iterator iterator = params.keySet().iterator(); iterator.hasNext(); i++)
        {
            if (i == 0)
            {
                redirectStr.append("?");
            }
            else
            {
                redirectStr.append("&");
            }
            final String paramName = (String) iterator.next();
            redirectStr.append(paramName);
            redirectStr.append("=");
            redirectStr.append(params.get(paramName));
        }

        return forceRedirect(redirectStr.toString());
    }

    public String doFetchLicense() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        // Store the user inputs so they are retained after going away to MAC
        Map session = ActionContext.getSession();
        session.put(SessionKeys.SETUP_TITLE, title);
        session.put(SessionKeys.SETUP_BASEURL, baseURL);
        session.put(SessionKeys.SETUP_MODE, mode);

        if (attachmentMode == AttachmentPathManager.Mode.DISABLED)
        {
            session.put(SessionKeys.SETUP_ATTACHMENT_MODE, false);
        }
        else
        {
            session.put(SessionKeys.SETUP_ATTACHMENT_MODE, true);
        }

        if (backupMode == DirectoryMode.DISABLED)
        {
            session.put(SessionKeys.SETUP_BACKUP_MODE, false);
        }
        else
        {
            session.put(SessionKeys.SETUP_BACKUP_MODE, true);
        }

        return INPUT;
    }

    public String doReturnFromMAC() throws Exception
    {
        // Re-populate the fields with the values saved in session before left to MAC to get the license
        // If the session doesn't have the keys, then set to a sensible (generally default) value
        Map session = ActionContext.getSession();
        title = getStringFromSession(session, SessionKeys.SETUP_TITLE, "");
        baseURL = getStringFromSession(session, SessionKeys.SETUP_BASEURL, getBaseURL());
        mode = getStringFromSession(session, SessionKeys.SETUP_MODE, "public");

        if (getBooleanFromSession(session, SessionKeys.SETUP_ATTACHMENT_MODE))
        {
            attachmentMode = AttachmentPathManager.Mode.DEFAULT;
        }
        else
        {
            attachmentMode = AttachmentPathManager.Mode.DISABLED;
        }

        if (getBooleanFromSession(session, SessionKeys.SETUP_BACKUP_MODE))
        {
            backupMode = DirectoryMode.DEFAULT;
        }
        else
        {
            backupMode = DirectoryMode.DISABLED;
        }

        return INPUT;
    }

    private String getStringFromSession(Map session, String sessionKey, String defaultValue)
    {
        if (session.get(sessionKey) == null)
        {
            return defaultValue;
        }
        return session.get(sessionKey).toString();
    }

    private boolean getBooleanFromSession(Map session, String sessionKey)
    {
        if (session.get(sessionKey) == null)
        {
            return true;
        }
        return Boolean.valueOf(session.get(sessionKey).toString());
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        if (nextStep == null)
        {
            return forceRedirectToInput();
        }
        else
        {
            getApplicationProperties().setString(APKeys.JIRA_TITLE, title);
            getApplicationProperties().setString(APKeys.JIRA_BASEURL, baseURL);
            getApplicationProperties().setString(APKeys.JIRA_MODE, mode);

            // If we come back to this step then we need to ensure that the indexing is turned off.
            if (indexManager.isIndexingEnabled())
            {
                try
                {
                    indexManager.deactivate();
                    log.info("The Index location has already been set but we need to deactivate it before we can reactivate it.\n" + "This is nothing to worry about it just means that the first stage of the setup has been run more than once.");
                }
                catch (final Exception ignored)
                {
                    // Sink this exception as it will happen most of the time because indexing will be off.
                }
            }

            indexPathManager.setUseDefaultDirectory();

            try
            {
                indexManager.activate(Contexts.percentageLogger(indexManager, log));
            }
            catch (final Exception e)
            {
                log.error("Error activating indexing with path '" + indexPath + "': " + e, e);
                addError("indexPath", getText("setup.error.indexpath.activate_error", e.getMessage()));
            }

            if (attachmentMode == AttachmentPathManager.Mode.DISABLED)
            {
                getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, false);
            }
            else
            {
                attachmentPathManager.setUseDefaultDirectory();
                getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
            }

            // This must be done after the language is set
            if (backupMode != DirectoryMode.DISABLED)
            {
                new BackupServiceHelper().createOrUpdateBackupService(new I18nBean(), getDefaultBackupPath());
            }

            // set the licence ino JIRA.
            licenseService.setLicense(validationResult);

            // cause JIRA to restart with new managers and the like!
            jiraSystemRestarter.ariseSirJIRA();

            return getResult();
        }
    }

    public void setNextStep(final String nextStep)
    {
        this.nextStep = nextStep;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(final String title)
    {
        this.title = title;
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode(final String mode)
    {
        this.mode = mode;
    }

    public String getBaseURL()
    {
        // if the base URL is null, try to guess it from the request
        if (baseURL == null)
        {
            baseURL = JiraUrl.constructBaseUrl(request);
        }

        return baseURL;
    }

    public void setBaseURL(final String baseURL)
    {
        this.baseURL = StringUtils.stripEnd(StringUtils.strip(baseURL), " /");
    }

    public String getLicense()
    {
        return licenseString;
    }

    public void setLicense(final String license)
    {
        this.licenseString = license;
    }

    public Map<String, String> getAllowedModes()
    {
        final Map<String, String> allowedModes = new HashMap<String, String>();

        allowedModes.put("public", "Public");
        allowedModes.put("private", "Private");

        return allowedModes;
    }

    /**
     * @return the absolute path for the home directory.
     */
    public String getHomePath()
    {
        return jiraHome.getHomePath();
    }

    public String getAttachmentPath()
    {
        return attachmentPath;
    }

    public String getAttachmentPathOption()
    {
        return attachmentMode.toString();
    }

    public void setAttachmentPathOption(final String attachmentPathOption)
    {
        attachmentMode = AttachmentPathManager.Mode.valueOf(attachmentPathOption);
    }

    public String getIndexPathOption()
    {
        return indexMode.toString();
    }

    public void setIndexPathOption(final String indexPathOption)
    {
        indexMode = IndexPathManager.Mode.valueOf(indexPathOption);
    }

    public String getIndexPath()
    {
        return indexPath;
    }

    /**
     * Returns the absolute path for the Default Backup directory that lives under the home directory. This is used for
     * read-only info added to the "Use Default Directory" option.
     *
     * @return the absolute path for the Default Backup directory that lives under the home directory.
     */
    public String getDefaultBackupPath()
    {
        return jiraHome.getExportDirectory().getPath();
    }

    public String getBackupPathOption()
    {
        return backupMode.toString();
    }

    public void setBackupPathOption(final String backupPathOption)
    {
        backupMode = DirectoryMode.valueOf(backupPathOption);
    }

    public String getRequestLicenseURL()
    {
        // Generate the callback URL
        StringBuilder url = new StringBuilder();
        url.append(getBaseURL());
        url.append("/secure/Setup!returnFromMAC.jspa");

        // Generate the link to auto fetch license from MAC
        return externalLinkUtil.getProperty("external.link.jira.license.view", Arrays.<String>asList(buildUtilsInfo.getVersion(), buildUtilsInfo.getCurrentBuildNumber(), "enterprise", getServerId(), url.toString()));
    }

    public BuildUtilsInfo getBuildUtilsInfo()
    {
        return buildUtilsInfo;
    }

    public int modulo(int index, int modulus)
    {
        return index % modulus;
    }

    private final class BackupServiceHelper
    {
        private static final String SERVICE_NAME_KEY = "admin.setup.services.backup.service";
        private final long DELAY = DateUtils.HOUR_MILLIS * 12;

        /**
         * Creates a new Backup service, or updates an already existing one with the parameters supplied.
         */
        public void createOrUpdateBackupService(final I18nHelper i18n, final String backupPath)
        {
            try
            {
                final Map<String, String[]> params = new HashMap<String, String[]>();
                params.put(ExportService.USE_DEFAULT_DIRECTORY, new String[] { "true" });
                final String serviceName = geti18nTextWithDefault(i18n, SERVICE_NAME_KEY, "Backup Service");
                if (serviceManager.getServiceWithName(serviceName) == null)
                {
                    serviceManager.addService(serviceName, ExportService.class.getName(), DELAY, params);
                }
                else
                {
                    serviceManager.editServiceByName(serviceName, DELAY, params);
                }
                getApplicationProperties().setString(APKeys.JIRA_PATH_BACKUP, backupPath);
            }
            catch (final Exception e) // intentionally catching RuntimeException as well
            {
                addErrorMessage(getText("admin.errors.setup.error.adding.service", e.toString()));
            }
        }

        String geti18nTextWithDefault(final I18nHelper i18n, final String key, final String defaultString)
        {
            final String result = i18n.getText(key);
            if (key.equals(result))
            {
                return defaultString;
            }
            else
            {
                return result;
            }
        }
    }
}
