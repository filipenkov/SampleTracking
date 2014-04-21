package com.atlassian.jira.web.action.admin;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.mail.JiraMailUtils;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.timezone.RegionInfo;
import com.atlassian.jira.timezone.RegionInfoImpl;
import com.atlassian.jira.timezone.TimeZoneInfo;
import com.atlassian.jira.timezone.TimeZoneInfoImpl;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@WebSudoRequired
public class EditApplicationProperties extends ViewApplicationProperties
{
    // ------------------------------------------------------------------------------------------------------- Constants

    public static final String LOGOUT_CONFIRM_ALWAYS = "always";
    public static final String LOGOUT_CONFIRM_NEVER = "never";
    public static final String LOGOUT_CONFIRM_COOKIE = "cookie";

    private static final long serialVersionUID = 3888401310969767356L;

    // --------------------------------------------------------------------------------------------------------- Members

    private String title;
    private String mode;
    private String introduction;
    private String baseURL;
    private String emailFromHeaderFormat;
    private String language;
    private String defaultLocale;

    // options
    private boolean captcha;
    private boolean voting;
    private boolean watching;
    private boolean allowUnassigned;
    private boolean externalUM;
    private boolean cacheIssues;
    private String logoutConfirm;
    private boolean useGzip;
    private boolean allowRpc;
    private boolean excludePrecedenceHeader;
    private String emailVisibility;

    private int unassignedIssueCount = -1;
    private int projectsWithDefaultUnassignedCount = -1;
    private String maximumAuthenticationAttemptsAllowed;

    private boolean groupVisibility;
    private boolean projectRoleVisibility;
    private boolean ajaxIssuePicker;
    private boolean ajaxUserPicker;
    private boolean jqlAutocompleteDisabled;

    private String ieMimeSniffer;
    private final Map /*<String, String>*/ validMimeSnifferOptions = EasyMap.build(APKeys.MIME_SNIFFING_OWNED, getText("admin.generalconfiguration.ie.mime.sniffing.owned"),
            APKeys.MIME_SNIFFING_PARANOID, getText("admin.generalconfiguration.ie.mime.sniffing.paranoid"),
            APKeys.MIME_SNIFFING_WORKAROUND, getText("admin.generalconfiguration.ie.mime.sniffing.workaround"));

    private boolean showContactAdministratorsForm;
    private String contactAdministratorsMessage;

    private final ReindexMessageManager reindexMessageManager;
    private String timeZoneId;

    public EditApplicationProperties(UserPickerSearchService searchService, final ReindexMessageManager reindexMessageManager,
            final LocaleManager localeManager, TimeZoneService timeZoneManager, final RendererManager rendererManager)
    {
        super(searchService, localeManager, timeZoneManager, rendererManager);
        this.reindexMessageManager = notNull("reindexMessageManager", reindexMessageManager);
    }

    // -------------------------------------------------------------------------------------------------- action support

    public String doDefault() throws Exception
    {
        final ApplicationProperties applicationProperties = getApplicationProperties();

        title = applicationProperties.getString(APKeys.JIRA_TITLE);
        baseURL = applicationProperties.getString(APKeys.JIRA_BASEURL);
        emailFromHeaderFormat = applicationProperties.getDefaultBackedString(APKeys.EMAIL_FROMHEADER_FORMAT);
        mode = applicationProperties.getString(APKeys.JIRA_MODE);
        introduction = applicationProperties.getText(APKeys.JIRA_INTRODUCTION);
        language = applicationProperties.getString(APKeys.JIRA_I18N_LANGUAGE_INPUT);
        defaultLocale = applicationProperties.getString(APKeys.JIRA_I18N_DEFAULT_LOCALE);

        // get the options
        voting = applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING);
        watching = applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING);
        allowUnassigned = applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
        externalUM = applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        logoutConfirm = applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_LOGOUT_CONFIRM);
        useGzip = applicationProperties.getOption(APKeys.JIRA_OPTION_WEB_USEGZIP);
        allowRpc = applicationProperties.getOption(APKeys.JIRA_OPTION_RPC_ALLOW);
        emailVisibility = applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_EMAIL_VISIBLE);
        excludePrecedenceHeader = applicationProperties.getOption(APKeys.JIRA_OPTION_EXCLUDE_PRECEDENCE_EMAIL_HEADER);
        groupVisibility = applicationProperties.getOption(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS);
        captcha = applicationProperties.getOption(APKeys.JIRA_OPTION_CAPTCHA_ON_SIGNUP);
        ajaxIssuePicker = applicationProperties.getOption(APKeys.JIRA_AJAX_ISSUE_PICKER_ENABLED);
        // ask the search service if its on or not
        ajaxUserPicker = canPerformAjaxSearch();
        ieMimeSniffer = applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_IE_MIME_SNIFFING);
        jqlAutocompleteDisabled = applicationProperties.getOption(APKeys.JIRA_JQL_AUTOCOMPLETE_DISABLED);
        maximumAuthenticationAttemptsAllowed = applicationProperties.getDefaultBackedString(APKeys.JIRA_MAXIMUM_AUTHENTICATION_ATTEMPTS_ALLOWED);
        showContactAdministratorsForm = applicationProperties.getOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM);
        contactAdministratorsMessage = applicationProperties.getDefaultBackedText(APKeys.JIRA_CONTACT_ADMINISTRATORS_MESSSAGE);
        return super.doDefault();
    }

    protected void doValidation()
    {
        if (!TextUtils.stringSet(title))
        {
            addError("title", getText("admin.errors.you.must.set.an.application.title"));
        }

        if (!TextUtils.verifyUrl(baseURL))
        {
            addError("baseURL", getText("admin.errors.you.must.set.a.valid.base.url"));
        }

        if (!TextUtils.stringSet(emailFromHeaderFormat))
        {
            addError("emailFromHeaderFormat", getText("admin.errors.you.must.set.a.valid.email.from.header"));
        }

        if (!TextUtils.stringSet(mode))
        {
            addError("mode", getText("admin.errors.invalid.mode.selected"));
        }

        if (!TextUtils.stringSet(language))
        {
            addError("language", getText("admin.errors.invalid.language.selected"));
        }

        // JRA-15966. Mode = Public and External User Management is an invalid combination.
        if (mode != null && mode.equalsIgnoreCase("public") && externalUM)
        {
            // Current JSP page does not allow errors to be shown for the radio button options, but just showing the
            // message on the mode setting is adequate.
            addError("mode", getText("admin.errors.invalid.mode.externalUM.combination"));
        }

        if (!validMimeSnifferOptions.keySet().contains(ieMimeSniffer))
        {
            if (StringUtils.isBlank(ieMimeSniffer))
            {
                addError("ieMimeSniffer", getText("admin.errors.mimesniffer.required"));
            }
            else
            {
                addError("ieMimeSniffer", getText("admin.errors.mimesniffer.invalid", ieMimeSniffer));
            }
        }
        if (TextUtils.stringSet(maximumAuthenticationAttemptsAllowed))
        {
            // is it a number
            try
            {
                final long maxAttemptsAllowed = Long.parseLong(maximumAuthenticationAttemptsAllowed);
                if (maxAttemptsAllowed <= 0)
                {
                    addError("maximumAuthenticationAttemptsAllowed", getText("admin.generalconfiguration.maximum.authentication.attempts.allowed.is.zero"));
                }
            }
            catch (NumberFormatException e)
            {
                addError("maximumAuthenticationAttemptsAllowed", getText("admin.generalconfiguration.maximum.authentication.attempts.allowed.notanumber", maximumAuthenticationAttemptsAllowed));
            }

        }
        if (contactAdministratorsMessage != null && contactAdministratorsMessage.length() > 2000)
        {
            addErrorMessage(getText("admin.generalconfiguration.contact.administrators.message.too.long"));
        }

    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final ApplicationProperties applicationProperties = getApplicationProperties();
        
        applicationProperties.setString(APKeys.JIRA_TITLE, title);
        applicationProperties.setString(APKeys.JIRA_BASEURL, baseURL);
        applicationProperties.setString(APKeys.EMAIL_FROMHEADER_FORMAT, emailFromHeaderFormat);
        applicationProperties.setString(APKeys.JIRA_MODE, mode);
        applicationProperties.setString(APKeys.JIRA_MAXIMUM_AUTHENTICATION_ATTEMPTS_ALLOWED, maximumAuthenticationAttemptsAllowed);

        applicationProperties.setText(APKeys.JIRA_INTRODUCTION, introduction);

        applicationProperties.setString(APKeys.JIRA_I18N_LANGUAGE_INPUT, language);

        if (JiraLocaleUtils.DEFAULT_LOCALE_ID.equals(getDefaultLocale()))
        {
            // If 'Default' was selected check if we have a value recorded
            final String dl = applicationProperties.getString(APKeys.JIRA_I18N_DEFAULT_LOCALE);
            if (dl != null)
            {
                // If we have a value set it to null
                applicationProperties.setString(APKeys.JIRA_I18N_DEFAULT_LOCALE, null);
            }
        }
        else
        {
            applicationProperties.setString(APKeys.JIRA_I18N_DEFAULT_LOCALE, getDefaultLocale());
        }

        if (TimeZoneService.SYSTEM.equals(timeZoneId))
        {
            timeZoneService.clearDefaultTimeZone(getJiraServiceContext());
        }
        else
        {
            if (!StringUtils.isEmpty(timeZoneId))
            {
                timeZoneService.setDefaultTimeZone(timeZoneId, getJiraServiceContext());
            }
        }

        // set the options
        // Check to see if voting changed, if it did flush the Field Manager so that the vote field is available.
        boolean oldVoting = applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING);
        if (oldVoting != voting)
        {
            applicationProperties.setOption(APKeys.JIRA_OPTION_VOTING, voting);
            ManagerFactory.getFieldManager().refresh();

            // if we are enabling voting, we also need to add a reindex message
            if (voting)
            {
                reindexMessageManager.pushMessage(getRemoteUser(), "admin.notifications.task.voting");
            }
        }
        applicationProperties.setOption(APKeys.JIRA_OPTION_WATCHING, watching);
        try
        {
            if (allowUnassigned || isCanSwitchUnassignedOff())
            {
                applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, allowUnassigned);
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Could not retrieve unassigned issues.", e);
        }

        applicationProperties.setOption(APKeys.JIRA_OPTION_CAPTCHA_ON_SIGNUP, captcha);

        applicationProperties.setOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT, externalUM);

        applicationProperties.setString(APKeys.JIRA_OPTION_LOGOUT_CONFIRM, logoutConfirm);

        applicationProperties.setOption(APKeys.JIRA_OPTION_WEB_USEGZIP, useGzip);
        applicationProperties.setOption(APKeys.JIRA_OPTION_RPC_ALLOW, allowRpc);
        applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, emailVisibility);
        applicationProperties.setOption(APKeys.JIRA_OPTION_EXCLUDE_PRECEDENCE_EMAIL_HEADER, excludePrecedenceHeader);

        // Set comment level visibility
        applicationProperties.setOption(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS, groupVisibility);

        applicationProperties.setOption(APKeys.JIRA_AJAX_ISSUE_PICKER_ENABLED, ajaxIssuePicker);
        applicationProperties.setOption(APKeys.JIRA_JQL_AUTOCOMPLETE_DISABLED, jqlAutocompleteDisabled);

        final boolean oldAjaxUserPicker = canPerformAjaxSearch();
        if (ajaxUserPicker != oldAjaxUserPicker)
        {
            int newAjaxUserPicker = ajaxUserPicker ? Integer.MAX_VALUE : -1;
            applicationProperties.setString(APKeys.JIRA_AJAX_USER_PICKER_LIMIT, String.valueOf(newAjaxUserPicker));
        }
        applicationProperties.setString(APKeys.JIRA_OPTION_IE_MIME_SNIFFING, ieMimeSniffer);

        applicationProperties.setOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM, showContactAdministratorsForm);

        applicationProperties.setText(APKeys.JIRA_CONTACT_ADMINISTRATORS_MESSSAGE, contactAdministratorsMessage);

        return getRedirect("ViewApplicationProperties.jspa");
    }

    public void setParameters(Map parameters)
    {
        // this.params = parameters;
    }

    /**
     * Retrieve the installed locales with the default option at the top
     *
     * @return a map of installed locales
     */
    public Map <String, String> getInstalledLocales()
    {
        return getLocaleManager().getInstalledLocalesWithDefault(Locale.getDefault(), this);
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public String getBaseURL()
    {
        return baseURL;
    }

    public void setBaseURL(String baseURL)
    {
        //JRA-13435: Strip trailing slash if there is one.
        this.baseURL = StringUtils.removeEnd(StringUtils.strip(baseURL), "/");
    }

    public String getEmailFromHeaderFormat()
    {
        return emailFromHeaderFormat;
    }

    public void setEmailFromHeaderFormat(String emailFromHeaderFormat)
    {
        this.emailFromHeaderFormat = emailFromHeaderFormat;
    }

    public String getIntroduction()
    {
        return introduction;
    }

    public void setIntroduction(String introduction)
    {
        if (TextUtils.stringSet(TextUtils.noNull(introduction).trim()))
        {
            this.introduction = TextUtils.closeTags(introduction);
        }
        else
        {
            this.introduction = null;
        }
    }

    public boolean isWatching()
    {
        return watching;
    }

    public void setWatching(boolean watching)
    {
        this.watching = watching;
    }

    public boolean isVoting()
    {
        return voting;
    }

    public void setVoting(boolean voting)
    {
        this.voting = voting;
    }

    public boolean isAllowUnassigned()
    {
        return allowUnassigned;
    }

    public void setAllowUnassigned(boolean allowUnassigned)
    {
        this.allowUnassigned = allowUnassigned;
    }

    public boolean isExternalUM()
    {
        return externalUM;
    }

    public void setExternalUM(boolean externalUM)
    {
        this.externalUM = externalUM;
    }

    public Map<String, String> getAllowedModes()
    {
        Map<String, String> allowedModes = new HashMap<String, String>();

        allowedModes.put("public", getText("admin.jira.mode.public"));
        allowedModes.put("private", getText("admin.jira.mode.private"));

        return allowedModes;
    }

    public boolean isCacheIssues()
    {
        return cacheIssues;
    }

    public void setCacheIssues(boolean cacheIssues)
    {
        this.cacheIssues = cacheIssues;
    }

    public Map<String, String> getAllowedLanguages()
    {
        Map<String, String> allowedLanguages = new LinkedHashMap<String, String>();

        allowedLanguages.put(APKeys.Languages.ENGLISH, getText("admin.jira.allowed.language.english"));
        allowedLanguages.put(APKeys.Languages.BRAZILIAN, getText("admin.jira.allowed.language.brazilian"));
        allowedLanguages.put(APKeys.Languages.DUTCH, getText("admin.jira.allowed.language.dutch"));
        allowedLanguages.put(APKeys.Languages.GERMAN, getText("admin.jira.allowed.language.german"));
        allowedLanguages.put(APKeys.Languages.FRENCH, getText("admin.jira.allowed.language.french"));
        allowedLanguages.put(APKeys.Languages.GREEK, getText("admin.jira.allowed.language.greek"));
        allowedLanguages.put(APKeys.Languages.CZECH, getText("admin.jira.allowed.language.czech"));
        allowedLanguages.put(APKeys.Languages.RUSSIAN, getText("admin.jira.allowed.language.russian"));
        allowedLanguages.put(APKeys.Languages.CHINESE, getText("admin.jira.allowed.language.chinese"));
        allowedLanguages.put(APKeys.Languages.CJK, getText("admin.jira.allowed.language.cjk"));
        allowedLanguages.put(APKeys.Languages.THAI, getText("admin.jira.allowed.language.thai"));
        allowedLanguages.put(APKeys.Languages.OTHER, getText("admin.jira.allowed.language.other"));

        return allowedLanguages;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getLogoutConfirm()
    {
        return logoutConfirm;
    }

    public void setLogoutConfirm(String logoutConfirm)
    {
        this.logoutConfirm = logoutConfirm;
    }

    public String getEmailVisibility()
    {
        return emailVisibility;
    }

    public void setEmailVisibility(String emailVisibility)
    {
        this.emailVisibility = emailVisibility;
    }

    public boolean isCanSwitchUnassignedOff() throws GenericEntityException
    {
        return (getUnassignedIssueCount() == 0) && (getProjectsWithDefaultUnassignedCount() == 0);
    }

    public int getUnassignedIssueCount() throws GenericEntityException
    {
        if (unassignedIssueCount == -1)
        {
            unassignedIssueCount = CoreFactory.getGenericDelegator().findByAnd("Issue", EasyMap.build("assignee", null)).size();
        }
        return unassignedIssueCount;
    }

    public int getProjectsWithDefaultUnassignedCount() throws GenericEntityException
    {
        if (projectsWithDefaultUnassignedCount == -1)
        {
            projectsWithDefaultUnassignedCount = CoreFactory.getGenericDelegator().findByAnd("Project",
                    EasyMap.build("assigneetype", new Long(ProjectAssigneeTypes.UNASSIGNED))).size();
            projectsWithDefaultUnassignedCount += CoreFactory.getGenericDelegator().findByAnd("Project", EasyMap.build("assigneetype", null)).size();
        }
        return projectsWithDefaultUnassignedCount;
    }

    public String getDefaultLocale()
    {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale)
    {
        this.defaultLocale = defaultLocale;
    }

    public String getConfiguredTimeZoneId()
    {
        if (timeZoneService.useSystemTimeZone())
        {
            return TimeZoneService.SYSTEM;
        }
        return timeZoneService.getDefaultTimeZoneInfo(getJiraServiceContext()).getTimeZoneId();
    }

    public String getConfiguredTimeZoneRegion()
    {
        return timeZoneService.getDefaultTimeZoneRegionKey();
    }

    public TimeZoneService getTimeZoneManager()
    {
        return timeZoneService;
    }

    public void setDefaultTimeZoneId(String timeZoneId)
    {
        this.timeZoneId = timeZoneId;
    }

    public List<TimeZoneInfo> getTimeZoneInfos()
    {
        List<TimeZoneInfo> timeZoneInfos = timeZoneService.getTimeZoneInfos(getJiraServiceContext());
        TimeZoneInfo jvmTimeZoneInfo = timeZoneService.getJVMTimeZoneInfo(getJiraServiceContext());
        TimeZoneInfoImpl timeZoneInfo = new TimeZoneInfoImpl(TimeZoneService.SYSTEM, jvmTimeZoneInfo.getDisplayName(), jvmTimeZoneInfo.toTimeZone(), getJiraServiceContext().getI18nBean(), TimeZoneService.SYSTEM);
        timeZoneInfos.add(0, timeZoneInfo);
        return timeZoneInfos;
    }

    public List<RegionInfo> getTimeZoneRegions()
    {
        List regions = timeZoneService.getTimeZoneRegions(getJiraServiceContext());
        // Add the system region to the beginning
        regions.add(0, new RegionInfoImpl(TimeZoneService.SYSTEM, getText("timezone.region.system")));
        return regions;
    }

    public boolean isUseGzip()
    {
        return useGzip;
    }

    public void setUseGzip(boolean useGzip)
    {
        this.useGzip = useGzip;
    }

    public boolean isAllowRpc()
    {
        return allowRpc;
    }

    public void setAllowRpc(boolean allowRpc)
    {
        this.allowRpc = allowRpc;
    }

    public boolean isExcludePrecedenceHeader()
    {
        return excludePrecedenceHeader;
    }

    public void setExcludePrecedenceHeader(boolean excludePrecedenceHeader)
    {
        this.excludePrecedenceHeader = excludePrecedenceHeader;
    }

    public boolean isGroupVisibility()
    {
        return groupVisibility;
    }

    public void setGroupVisibility(boolean groupVisibility)
    {
        this.groupVisibility = groupVisibility;
    }

    public boolean isProjectRoleVisibility()
    {
        return projectRoleVisibility;
    }

    public void setProjectRoleVisibility(boolean projectRoleVisibility)
    {
        this.projectRoleVisibility = projectRoleVisibility;
    }

    public boolean isCaptcha()
    {
        return captcha;
    }

    public void setCaptcha(boolean captcha)
    {
        this.captcha = captcha;
    }

    public boolean isAjaxIssuePicker()
    {
        return ajaxIssuePicker;
    }

    public void setAjaxIssuePicker(boolean ajaxIssuePicker)
    {
        this.ajaxIssuePicker = ajaxIssuePicker;
    }

    public boolean isJqlAutocompleteDisabled()
    {
        return jqlAutocompleteDisabled;
    }

    public void setJqlAutocompleteDisabled(boolean jqlAutocompleteDisabled)
    {
        this.jqlAutocompleteDisabled = jqlAutocompleteDisabled;
    }

    public String getContactAdministratorsMessage()
    {
        return contactAdministratorsMessage;
    }

    public void setContactAdministratorsMessage(String contactAdministratorsMessage)
    {
        this.contactAdministratorsMessage = contactAdministratorsMessage;
    }

    /**
     * Renders the input control for the Contact administrators message.
     *
     * @return the HTML of the input control to be displayed on the form.
     */
    public String getContactAdministratorsMessageEditHtml()
    {
        try
        {
            final JiraRendererModuleDescriptor rendererDescriptor = rendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE).getDescriptor();
            final Map<Object,Object> rendererParams = MapBuilder.newBuilder().add("rows", "10").add("cols", "60").add("wrap", "virtual").add("class", "long-field").toMutableMap();

            return rendererDescriptor.getEditVM(getContactAdministratorsMessage(), null, AtlassianWikiRenderer.RENDERER_TYPE, "contactAdministratorsMessage", "contactAdministratorsMessage", rendererParams, false);
        }
        catch (DataAccessException e)
        {
            log.error("Could not render edit template for Contact Administrators Message", e);
            return "";
        }
    }


    public boolean isShowContactAdministratorsForm()
    {
        return showContactAdministratorsForm;
    }

    public void setShowContactAdministratorsForm(boolean showContactAdministratorsForm)
    {
        this.showContactAdministratorsForm = showContactAdministratorsForm;
    }

    public boolean isAjaxUserPicker()
    {
        return ajaxUserPicker;
    }

    public void setAjaxUserPicker(boolean ajaxUserPicker)
    {
        this.ajaxUserPicker = ajaxUserPicker;
    }

    public String getIeMimeSniffer()
    {
        return ieMimeSniffer;
    }

    public void setIeMimeSniffer(String ieMimeSniffer)
    {
        this.ieMimeSniffer = ieMimeSniffer;
    }

    public Map /*<String,String>*/ getValidMimeSnifferOptions() {
        return validMimeSnifferOptions;
    }

    public String getMaximumAuthenticationAttemptsAllowed()
    {
        return maximumAuthenticationAttemptsAllowed;
    }

    public void setMaximumAuthenticationAttemptsAllowed(final String maximumAuthenticationAttemptsAllowed)
    {
        this.maximumAuthenticationAttemptsAllowed = maximumAuthenticationAttemptsAllowed == null ? maximumAuthenticationAttemptsAllowed : maximumAuthenticationAttemptsAllowed.trim();
    }

    public boolean hasMailServer()
    {
        return JiraMailUtils.isHasMailServer();
    }
}
