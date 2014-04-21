/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.JiraActionSupport;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatUtils;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.hints.Hint;
import com.atlassian.jira.hints.HintManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.UriValidator;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.web.util.AuthorizationSupport;
import com.atlassian.jira.web.util.CookieUtils;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;
import webwork.action.CoreActionContext;
import webwork.action.ServletActionContext;
import webwork.util.ValueStack;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * All web actions should extend this class - it provides basic common functionality for all web actions.
 * <p/>
 * When adding to this class, be sure that what you are adding is used by a large number of actions - otherwise add it
 * to a sub class of this.
 */
@NotThreadSafe
public class JiraWebActionSupport extends JiraActionSupport implements ErrorCollection, AuthorizationSupport
{
    /**
     * Represents a type of message that the browser will display after the next page load.
     */
    public enum MessageType
    {
        ERROR, WARNING, SUCCESS;

        public String asWebParameter()
        {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }

    public static final String RETURN_URL_PARAMETER = "returnUrl";
    public static final String PERMISSION_VIOLATION_RESULT = "permissionviolation";
    public static final String ISSUE_NOT_FOUND_RESULT = "issuenotfound";
    private static final String X_ATLASSIAN_DIALOG_CONTROL = "X-Atlassian-Dialog-Control";
    private static final String X_ATLASSIAN_DIALOG_MSG_HTML = "X-Atlassian-Dialog-Msg-Html";
    private static final String X_ATLASSIAN_DIALOG_MSG_CLOSEABLE = "X-Atlassian-Dialog-Msg-Closeable";
    private static final String X_ATLASSIAN_DIALOG_MSG_TYPE = "X-Atlassian-Dialog-Msg-Type";
    private static final String X_ATLASSIAN_DIALOG_MSG_TARGET = "X-Atlassian-Dialog-Msg-Target";
    private static final Pattern SELECTED_ISSUE_PATTERN = Pattern.compile("(&|&amp;)?selectedIssueId=[0-9]*");

    protected HttpServletRequest request = ServletActionContext.getRequest();
    private Map assignableUsers;
    private OutlookDate outlookDate;
    private String returnUrl;
    protected Collection savedFilters;
    private Project selectedProject;
    private boolean inline = false;

    private PermissionManager permissionManager;
    private AuthorizationSupport authorizationSupport;
    private GlobalPermissionManager globalPermissionManager;
    private ProjectManager projectManager;
    private VersionManager versionManager;
    private UserProjectHistoryManager userProjectHistoryManager;
    private HintManager hintManager;
    private FieldManager fieldManager;
    private SearchHandlerManager searchHandlerManager;
    private SearchSortUtil searchSortUtil;
    private JiraLicenseService jiraLicenseService;
    private PermissionSchemeManager permissionSchemeManager;
    private ApplicationProperties applicationProperties;
    private ConstantsManager constantsManager;
    private XsrfTokenGenerator xsrfTokenGenerator;
    private OutlookDateManager outlookDateManager;
    private UriValidator uriValidator;
    private PermissionContextFactory permissionContextFactory;
    private SafeRedirectChecker safeRedirectChecker;
    private Set<Reason> reasons = new HashSet<Reason>();
    private DateTimeFormatterFactory dateTimeFormatterFactory;
    private DateTimeFormatter dateTimeFormatter;
    private DateTimeFormatter dmyDateFormatter;
    private JiraContactHelper jiraContactHelper;
    private UserManager userManager;

    public JiraWebActionSupport()
    {
        // do NOT call #getComponentManager or #getComponentInstanceOfType in here, since that will prevent the class
        // from being loaded if the ComponentManager has not been initialised yet. that can seriously fuck up plugin
        // devs whose unit tests need to load this class (i.e. ones that test actions).
    }

    @Override
    public User getLoggedInUser()
    {
        return ComponentAccessor.getComponent(JiraAuthenticationContext.class).getLoggedInUser();
    }

    public String getXsrfToken()
    {
        return getXsrfTokenGenerator().generateToken(ActionContext.getRequest());
    }

    private XsrfTokenGenerator getXsrfTokenGenerator()
    {
        if (xsrfTokenGenerator == null)
        {
            xsrfTokenGenerator = getComponentInstanceOfType(XsrfTokenGenerator.class);
        }
        return xsrfTokenGenerator;
    }

    public ApplicationProperties getApplicationProperties()
    {
        if (applicationProperties == null)
        {
            applicationProperties = getComponentInstanceOfType(ApplicationProperties.class);
        }
        return applicationProperties;
    }

    public UriValidator getUriValidator()
    {
        if (uriValidator == null)
        {
            uriValidator = getComponentInstanceOfType(UriValidator.class);
        }
        return uriValidator;
    }

    protected SafeRedirectChecker getRedirectChecker()
    {
        if (safeRedirectChecker == null)
        {
            safeRedirectChecker = getComponentInstanceOfType(SafeRedirectChecker.class);
        }
        return safeRedirectChecker;
    }

    protected GlobalPermissionManager getGlobalPermissionManager()
    {
        if (globalPermissionManager == null)
        {
            globalPermissionManager = getComponentInstanceOfType(GlobalPermissionManager.class);
        }
        return globalPermissionManager;
    }

    protected PermissionManager getPermissionManager()
    {
        if (permissionManager == null)
        {
            permissionManager = getComponentInstanceOfType(PermissionManager.class);
        }
        return permissionManager;
    }

    protected UserProjectHistoryManager getUserProjectHistoryManager()
    {
        if (userProjectHistoryManager == null)
        {
            userProjectHistoryManager = getComponentInstanceOfType(UserProjectHistoryManager.class);
        }
        return userProjectHistoryManager;
    }

    public ConstantsManager getConstantsManager()
    {
        if (constantsManager == null)
        {
            constantsManager = getComponentInstanceOfType(ConstantsManager.class);
        }
        return constantsManager;
    }

    private PermissionContextFactory getPermissionContextFactory()
    {
        if (permissionContextFactory == null)
        {
            permissionContextFactory = getComponentInstanceOfType(PermissionContextFactory.class);
        }
        return permissionContextFactory;
    }

    private PermissionSchemeManager getPermissionSchemeManager()
    {
        if (permissionSchemeManager == null)
        {
            permissionSchemeManager = getComponentInstanceOfType(PermissionSchemeManager.class);
        }
        return permissionSchemeManager;
    }

    public ProjectManager getProjectManager()
    {
        if (projectManager == null)
        {
            projectManager = getComponentInstanceOfType(ProjectManager.class);
        }
        return projectManager;
    }

    public VersionManager getVersionManager()
    {
        if (versionManager == null)
        {
            versionManager = getComponentInstanceOfType(VersionManager.class);
        }
        return versionManager;
    }

    private FieldManager getFieldManager()
    {
        if (fieldManager == null)
        {
            fieldManager = getComponentInstanceOfType(FieldManager.class);
        }
        return fieldManager;
    }

    private SearchSortUtil getSearchSortUtil()
    {
        if (searchSortUtil == null)
        {
            searchSortUtil = getComponentInstanceOfType(SearchSortUtil.class);
        }
        return searchSortUtil;
    }

    private SearchHandlerManager getSearchHandlerManager()
    {
        if (searchHandlerManager == null)
        {
            searchHandlerManager = getComponentInstanceOfType(SearchHandlerManager.class);
        }
        return searchHandlerManager;
    }

    /**
     * @deprecated Use {@link #getDateTimeFormatter()} instead. Since v5.0.
     */
    @Deprecated
    public OutlookDate getOutlookDate()
    {
        if (outlookDate == null)
        {
            outlookDate = getOutlookDateManager().getOutlookDate(getLocale());
        }
        return outlookDate;
    }

    private OutlookDateManager getOutlookDateManager()
    {
        if (outlookDateManager == null)
        {
            this.outlookDateManager = getComponentInstanceOfType(OutlookDateManager.class);
        }
        return outlookDateManager;
    }

    /**
     * Returns a DateTimeFormatter that can be used to format times and dates in the user's time zone using {@link
     * DateTimeStyle#RELATIVE}.
     *
     * @return a DateTimeFormatter
     */
    public DateTimeFormatter getDateTimeFormatter()
    {
        if (dateTimeFormatter == null)
        {
            dateTimeFormatter = getComponentInstanceOfType(DateTimeFormatterFactory.class).formatter()
                    .forLoggedInUser()
                    .withStyle(DateTimeStyle.RELATIVE);
        }

        return dateTimeFormatter;
    }

    /**
     * Returns a DateTimeFormatter that can be used to format dates in the user's time zone using {@link
     * DateTimeStyle#DATE}.
     *
     * @return a DateTimeFormatter
     */
    public DateTimeFormatter getDmyDateFormatter()
    {
        if (dmyDateFormatter == null)
        {
            dmyDateFormatter = getComponentInstanceOfType(DateTimeFormatterFactory.class).formatter()
                    .forLoggedInUser()
                    .withStyle(DateTimeStyle.DATE);
        }

        return dmyDateFormatter;
    }

    public JiraContactHelper getJiraContactHelper()
    {
        if (jiraContactHelper == null)
        {
            this.jiraContactHelper = getComponentInstanceOfType(JiraContactHelper.class);
        }
        return jiraContactHelper;
    }

    public UserManager getUserManager()
    {
        if (userManager == null)
        {
            this.userManager = getComponentInstanceOfType(UserManager.class);
        }
        return userManager;
    }

    /**
     * Get the link, with Internationalised text for contacting the administrators of JIRA.
     * This link is present on many pages across the bredth of JIRA and so centralised here.
     * @see {@link JiraContactHelper#getAdministratorContactLinkHtml(String, com.atlassian.jira.util.I18nHelper)}
     * @return html String of the contact administrators link.
     */
    public String getAdministratorContactLink()
    {
        return getJiraContactHelper().getAdministratorContactLinkHtml(request.getContextPath(), getI18nHelper());
    }

    private JiraLicenseService getJiraLicenseService()
    {
        if (jiraLicenseService == null)
        {
            jiraLicenseService = getComponentInstanceOfType(JiraLicenseService.class);
        }
        return jiraLicenseService;
    }

    protected final HintManager getHintManager()
    {
        if (hintManager == null)
        {
            hintManager = getComponentInstanceOfType(HintManager.class);
        }
        return hintManager;
    }

    private AuthorizationSupport getAuthorizationSupport()
    {
        if (authorizationSupport == null)
        {
            authorizationSupport = getComponentInstanceOfType(AuthorizationSupport.class);
        }
        return authorizationSupport;
    }


    /**
     * Get a definitive result with a redirect upon success.
     * <p/>
     * Returns {@link #ERROR} if there are error messages, otherwise redirects to another URL if successful.  If URL
     * starts with '/', interpreted as context-relative
     * <p/>
     * Since 3.1 now redirects to a the "returnUrl" if one exists. Will clear this value once this occurs
     *
     * @param defaultUrl default URL to redirect to
     * @return URL to redirect to
     */
    public String getRedirect(final String defaultUrl)
    {
        // Checks whether the return URL has been set
        String redirectUrl;
        if (StringUtils.isNotEmpty(getReturnUrl()))
        {
            redirectUrl = getReturnUrl();
            setReturnUrl(null);
        }
        else
        {
            // Set to null just to be sure
            setReturnUrl(null);
            redirectUrl = defaultUrl;
        }

        if (invalidInput())
        {
            return ERROR;
        }

        if (ServletActionContext.getResponse() == null)
        {
            // If this method is called from a back-end action
            // to avoid a NPE when getResponse() returns null, here we return SUCCESS
            final String message = "Called a web action as if it were non-web";
            log.warn(message, new RuntimeException(message));

            return SUCCESS;
        }

        String redirect;
        if (StringUtils.isNotBlank(redirectUrl) && (redirectUrl.charAt(0) == '/') && request != null)
        {
            redirect = insertContextPath(redirectUrl);
        }
        else
        {
            redirect = redirectUrl;
        }
        return forceRedirect(redirect);
    }

    /**
     * This method will force a server redirect. It doesn't clear the return URL and will always go to the redirect URL
     *
     * @param redirect redirect URL
     * @return {@link #NONE}. It'll just redirect to where you've specified
     */
    protected String forceRedirect(String redirect)
    {
        try
        {
            String returnUrl = getReturnUrl();
            if (StringUtils.isNotBlank(returnUrl))
            {
                // Append the returnUrl
                if (redirect.indexOf('?') == -1)
                {
                    redirect = redirect + "?" + "returnUrl=" + JiraUrlCodec.encode(returnUrl);
                }
                else
                {
                    redirect = redirect + "&" + "returnUrl=" + JiraUrlCodec.encode(returnUrl);
                }
            }

            if (ServletActionContext.getResponse() != null)
            {
                ServletActionContext.getResponse().sendRedirect(redirect);
            }
        }
        catch (IOException e)
        {
            log.error("IOException trying to send redirect" + e, e);
        }

        return NONE;
    }

    @Override
    public boolean isHasPermission(String permName)
    {
        return getAuthorizationSupport().isHasPermission(permName);
    }

    @Override
    public boolean isHasPermission(int permissionsId)
    {
        return getAuthorizationSupport().isHasPermission(permissionsId);
    }

    @Override
    public boolean isHasIssuePermission(String permName, GenericValue issue)
    {
        return getAuthorizationSupport().isHasIssuePermission(permName, issue);
    }

    @Override
    public boolean isHasIssuePermission(int permissionsId, GenericValue issue)
    {
        return getAuthorizationSupport().isHasIssuePermission(permissionsId, issue);
    }

    @Override
    public boolean isHasProjectPermission(String permName, GenericValue project)
    {
        return getAuthorizationSupport().isHasProjectPermission(permName, project);
    }

    @Override
    public boolean isHasProjectPermission(int permissionsId, GenericValue project)
    {
        return getAuthorizationSupport().isHasProjectPermission(permissionsId, project);
    }

    @Override
    @Deprecated
    public boolean isHasPermission(String permName, GenericValue entity)
    {
        return getAuthorizationSupport().isHasPermission(permName, entity);
    }

    public boolean isSystemAdministrator()
    {
        User currentUser = getLoggedInUser();
        return (currentUser != null) && getGlobalPermissionManager().hasPermission(Permissions.SYSTEM_ADMIN, currentUser);
    }

    public boolean isUserExists(String username)
    {
        return UserUtils.userExists(username);
    }

    public String getUserFullName(String username)
    {
        User user = getUserManager().getUser(username);
        if (username == null)
        {
            log.warn("Could not retrieve full name for user '" + username + "'! User does not exist!");
            return username;
        }
        return user.getDisplayName();
    }

    public void addErrorCollection(ErrorCollection errors)
    {
        addErrorMessages(errors.getErrorMessages());
        addErrors(errors.getErrors());
        addReasons(errors.getReasons());
    }

    @Override
    public void addError(String field, String message, Reason reason)
    {
        addError(field, message);
        addReason(reason);
    }

    @Override
    public void addErrorMessage(String message, Reason reason)
    {
        addErrorMessage(message);
        addReason(reason);
    }

    @Override
    public void addReason(Reason reason)
    {
        this.reasons.add(reason);
    }

    @Override
    public void addReasons(Set<Reason> reasons)
    {
        this.reasons.addAll(reasons);
    }

    @Override
    public void setReasons(Set<Reason> reasons)
    {
        this.reasons = reasons;
    }

    @Override
    public Set<Reason> getReasons()
    {
        return reasons;
    }

    public Field getField(String id)
    {
        return getFieldManager().getField(id);
    }


    public List<String> getSearchSortDescriptions(SearchRequest searchRequest)
    {
        return getSearchSortUtil().getSearchSortDescriptions(searchRequest, this, getLoggedInUser());
    }


    public String getNameTranslation(GenericValue issueConstantGV)
    {
        if (issueConstantGV != null)
        {
            IssueConstant issueConstant = getConstantsManager().getIssueConstant(issueConstantGV);
            return issueConstant.getNameTranslation();
        }
        else
        {
            return null;
        }
    }

    public String getDescTranslation(GenericValue issueConstantGV)
    {
        IssueConstant issueConstant = getConstantsManager().getIssueConstant(issueConstantGV);
        if (issueConstant == null)
        {
            throw new RuntimeException("No issue constant associated with " + issueConstantGV);
        }
        return issueConstant.getDescTranslation();
    }

    public String getReturnUrl()
    {
        return returnUrl;
    }

    /**
     * The cancel links should not included the selectedIssueId, otherwise when returning to the issue navigator an
     * issue updated notification will be shown.
     *
     * @return the returnUrl with selectedIssueId parameter stripped out.
     */
    public String getReturnUrlForCancelLink()
    {
        if (StringUtils.contains(returnUrl, "selectedIssueId"))
        {
            return SELECTED_ISSUE_PATTERN.matcher(returnUrl).replaceFirst("");
        }
        else
        {
            return returnUrl;
        }
    }

    public void setReturnUrl(String returnUrl)
    {
        String safeReturnUrl = returnUrl != null ? getUriValidator().getSafeUri(JiraUrl.constructBaseUrl(request), returnUrl) : null;

        // JRA-23190: only set the returnURL if we allow redirects to it. otherwise it ends up on the page in the submit
        // and cancel links, not to mention some hidden elements used by Javascript code.
        if (safeReturnUrl == null || getRedirectChecker().canRedirectTo(safeReturnUrl))
        {
            this.returnUrl = safeReturnUrl;
        }
        else
        {
            this.returnUrl = null;
        }
    }


    public Collection<String> getFlushedErrorMessages()
    {
        Collection<String> errors = getErrorMessages();
        errorMessages = new ArrayList<String>();
        return errors;
    }

    public String getLanguage() throws IOException
    {

        if (isEnglishCompatibleLocale())
        {
            return "en";
        }
        else
        {
            return getLocale().getLanguage();
        }
    }

    /**
     * Some locales in Java like jp_AU use English date formats (22-Aug-2004), but the jscalendar date picker uses JP
     * date formats. This code returns true for locales that use English dates.
     *
     * @return true if the JIRA locale uses English dates
     */
    private boolean isEnglishCompatibleLocale()
    {
// JRA-8603 - need find a better way to check for language display for calendar
//        OutlookDate localeOutlookDate = ManagerFactory.getOutlookDateManager().getOutlookDate(getLocale());
//        String localeDate = localeOutlookDate.formatDatePicker(new Date());
//        OutlookDate enOutlookDate = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
//        String enDate = enOutlookDate.formatDatePicker(new Date());
//        return enDate.equals(localeDate);
        return false;
    }

    /**
     * Gets the last viewed project that the user visited and still has permission to see.
     *
     * @return the last project the user visited.
     *
     * @see UserProjectHistoryManager#getCurrentProject(int, User)
     */
    public Project getSelectedProjectObject()
    {
        if (selectedProject == null)
        {
            selectedProject = getUserProjectHistoryManager().getCurrentProject(Permissions.BROWSE, getLoggedInUser());
        }

        return selectedProject;
    }

    /**
     * Gets the "active" project.
     * <p>
     * Projects become active when your browse, search for a single project or create an issue.
     *
     * @return the "active" project.
     *
     * @deprecated Use {@link #getSelectedProjectObject()} instead. Since v5.0.
     */
    public GenericValue getSelectedProject()
    {
        final Project project = getSelectedProjectObject();
        if (project != null)
        {
            return project.getGenericValue();
        }
        return null;
    }

    public void setSelectedProjectId(Long id)
    {
        selectedProject = null;
        if (id != null)
        {
            final Project project = getProjectManager().getProjectObj(id);
            if (project != null)
            {
                getUserProjectHistoryManager().addProjectToHistory(getLoggedInUser(), project);
            }
        }
    }

    public String getDateFormat()
    {
        return DateTimeFormatUtils.getDateFormat();
    }

    public String getDateTimeFormat()
    {
        return DateTimeFormatUtils.getDateTimeFormat();
    }

    public String getTimeFormat()
    {
        return DateTimeFormatUtils.getTimeFormat();
    }

    /**
     * For debugging JSPs; prints the webwork stack, highlighting the specified node. Eg. called with: <webwork:property
     * value="/webworkStack('../../..')" escape="false"/>
     *
     * @param selected selected value in the webwork stack
     * @return HTML string of the webwork stack
     */
    public String getWebworkStack(String selected)
    {
        ValueStack stack = CoreActionContext.getValueStack();
        Object selectedObj = stack.findValue(selected);
        StringBuilder buf = new StringBuilder();
        buf.append("<pre>");
        Iterator iter = stack.iterator();
        boolean highlighted = false;
        while (iter.hasNext())
        {
            Object o = iter.next();
            buf.append("<ul><li>");
            if (o == selectedObj)
            {
                highlighted = true;
                buf.append("<font color='red'>");
                buf.append(o);
                buf.append("</font>");
            }
            else
            {
                buf.append(o);
            }
        }
        buf.append("</pre>");
        if (!highlighted && selected != null)
        {
            buf.append("<font color='red'>");
            buf.append(selected);
            buf.append(" resolves to: ");
            buf.append(selectedObj);
            buf.append("</font>");
        }
        return buf.toString();
    }

    /**
     * For debugging JSPs; prints the webwork stack. Eg. called with: <webwork:property value="/webworkStack"
     * escape="false"/>
     *
     * @return HTML string of the webwork stack
     */
    public String getWebworkStack()
    {
        return getWebworkStack(null);
    }

    public String getServerId()
    {
        return getJiraLicenseService().getServerId();
    }


    /**
     * Provides a service context with the current user which contains this action as its {@link
     * com.atlassian.jira.util.ErrorCollection}.
     *
     * @return the JiraServiceContext.
     */
    public JiraServiceContext getJiraServiceContext()
    {
        return new JiraServiceContextImpl(getLoggedInUser(), this);
    }

    /**
     * Convenience instance method to call static utility from webwork EL.
     *
     * @param encodeMe a String to be HTML encoded.
     * @return the HTML encoded string.
     */
    public String htmlEncode(String encodeMe)
    {
        return TextUtils.htmlEncode(encodeMe);
    }

    /**
     * This returns true if the action has been invoked as an inline dialog.  This changes the way that the action sends
     * back its responses, namely when the action is submitted and completed
     *
     * @return true if the action was invoked as an inline dialog
     */
    public boolean isInlineDialogMode()
    {
        return inline;
    }

    /**
     * This is the web parameter setter for invoking an action as an inline dialog
     *
     * @param inline true if the action should act as an inline dialog
     */
    public void setInline(boolean inline)
    {
        this.inline = inline;
    }

    public String returnComplete()
    {
        return returnComplete(null);
    }

    public String returnComplete(String url)
    {
        if (isInlineDialogMode())
        {
            return inlineDialogControl("DONE");
        }

        return returnWebResponse(url);
    }

    /**
     * This will return success response with body containing url to redirect. An appropriately configured client side
     * control should perform redirect to the desired url.
     *
     * @param url URL to redirect to
     * @return action mapping string
     */
    protected final String returnCompleteWithInlineRedirect(String url)
    {
        if (isInlineDialogMode())
        {
            return inlineDialogControl("redirect:" + insertContextPath(url));
        }

        return returnWebResponse(url);
    }

    /**
     * This will redirect like {@link #returnCompleteWithInlineRedirect(String)}, and will also populate the response
     * with the details of a pop-up message to be displayed on the redirected page. An appropriately configured client
     * side control should perform the displaying of the message.
     *
     * @param url URL to redirect to
     * @param msg message HTML
     * @param type type of message.
     * @param closeable if true, message pop-up has an 'X' button, otherwise pop-up fades away automatically
     * @param target the target to prepend the message pop-up to. If null, the message is shown in a global spot
     * @return action mapping string
     */
    protected String returnCompleteWithInlineRedirectAndMsg(String url, String msg, MessageType type, boolean closeable, @Nullable String target)
    {
        return returnCompleteWithInlineRedirectAndMsg(url, msg, type.asWebParameter(), closeable, target);
    }

    /**
     * This will redirect like {@link #returnCompleteWithInlineRedirect(String)}, and will also populate the response
     * with the details of a pop-up message to be displayed on the redirected page. An appropriately configured client
     * side control should perform the displaying of the message.
     *
     * @param url URL to redirect to
     * @param msg message HTML
     * @param type type of message, see JIRA.Messages.Types
     * @param closeable if true, message pop-up has an 'X' button, otherwise pop-up fades away automatically
     * @param target the target to prepend the message pop-up to. If null, the message is shown in a global spot
     * @return action mapping string
     * @deprecated since 5.1. Use {@link #returnCompleteWithInlineRedirectAndMsg(String, String, MessageType, boolean, String)}
     *  instead.
     */
    protected String returnCompleteWithInlineRedirectAndMsg(String url, String msg, String type, boolean closeable, @Nullable String target)
    {
        if (isInlineDialogMode())
        {
            addMessageToResponse(msg, type, closeable, target);
            return inlineDialogControl("redirect:" + insertContextPath(url));
        }

        return returnWebResponse(url);
    }

    /**
     * This will redirect like {@link #returnComplete()}, and will also populate the response
     * with the details of a pop-up message to be displayed on the redirected page. An appropriately configured client
     * side control should perform the displaying of the message.
     *
     * @param url URL to redirect to.  Not used in dialogs
     * @param msg message HTML
     * @param type type of message, see JIRA.Messages.Types
     * @param closeable if true, message pop-up has an 'X' button, otherwise pop-up fades away automatically
     * @param target the target to prepend the message pop-up to. If null, the message is shown in a global spot
     * @return action mapping string
     *
     * @deprecated since 5.1. Use {@link #returnMsgToUser(String, String, MessageType, boolean, String)} instead.
     */
    @Deprecated
    protected String returnMsgToUser(String url, String msg, String type, boolean closeable, @Nullable String target)
    {
        addMessageToResponse(msg, type, closeable, target);
        return returnComplete(url);
    }

    /**
     * This will redirect like {@link #returnComplete()}, and will also populate the response
     * with the details of a pop-up message to be displayed on the redirected page. An appropriately configured client
     * side control should perform the displaying of the message.
     *
     * @param url URL to redirect to.  Not used in dialogs
     * @param msg message HTML
     * @param type type of message
     * @param closeable if true, message pop-up has an 'X' button, otherwise pop-up fades away automatically
     * @param target the target to prepend the message pop-up to. If null, the message is shown in a global spot
     * @return action mapping string
     */
    protected String returnMsgToUser(String url, String msg, MessageType type, boolean closeable, @Nullable String target)
    {
        return returnMsgToUser(url, msg, type.asWebParameter(), closeable, target);
    }

    /**
     * Prepends the context path to the URL if it begins with a forward slash (this is commonly used for redirects
     * within a JIRA instance).
     *
     * @param url a String containing a URL
     * @return a String with the context path prepended
     */
    protected String insertContextPath(String url)
    {
        if (url.startsWith("/"))
        {
            String contextPath = request.getContextPath();
            url = (contextPath == null ? url : contextPath + url);
        }
        return url;
    }

    private String returnWebResponse(final String url)
    {
        if (StringUtils.isNotBlank(url))
        {
            if (url.equals(SUCCESS) || url.equals(ERROR) || url.equals(INPUT) || url.equals(LOGIN) || url.equals(NONE))
            {
                return url;
            }
            return getRedirect(url);
        }
        return SUCCESS;
    }

    /**
     * This will send back a 200 but with the custom Atlassian header.  This tells our client side javascript to stop
     * showing dialogs and do something else like refresh or redirect to a new page
     *
     * @param headerValue the value to be plaved in the magic header
     * @return Action.NONE
     */
    private String inlineDialogControl(String headerValue)
    {
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setStatus(200);
        response.setHeader(X_ATLASSIAN_DIALOG_CONTROL, headerValue);
        return NONE;
    }

    /**
     * This will populate the the custom Atlassian header with the details of a pop-up message.
     *
     * @param msg message HTML
     * @param type type of message, see JIRA.Messages.Types
     * @param closeable if true, message pop-up has an 'X' button, otherwise pop-up fades away automatically
     * @param target the target to prepend the message pop-up to. If null, the message is shown in a global spot.
     */
    private void addMessageToResponse(String msg, String type, boolean closeable, String target)
    {
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setHeader(X_ATLASSIAN_DIALOG_MSG_HTML, msg);
        response.setHeader(X_ATLASSIAN_DIALOG_MSG_TYPE, type);
        response.setHeader(X_ATLASSIAN_DIALOG_MSG_CLOSEABLE, String.valueOf(closeable));
        response.setHeader(X_ATLASSIAN_DIALOG_MSG_TARGET, target);
    }

    protected final boolean hasErrorMessage(String errorMsg)
    {
        return getErrorMessages().contains(errorMsg);
    }

    protected final boolean hasErrorMessageByKey(String errorMsgKey)
    {
        return hasErrorMessage(getText(errorMsgKey));
    }

    protected final void addErrorMessageIfAbsent(String errorMsg)
    {
        if (!hasErrorMessage(errorMsg))
        {
            addErrorMessage(errorMsg);
        }
    }

    protected final void addErrorMessageByKeyIfAbsent(String errorMsgKey)
    {
        addErrorMessageIfAbsent(getText(errorMsgKey));
    }

    public final Hint getHint(final String context)
    {
        final HintManager.Context realContext;
        try
        {
            realContext = HintManager.Context.valueOf(context.toUpperCase());
            return getHintManager().getHintForContext(getLoggedInUser(), new JiraHelper(ActionContext.getRequest()), realContext);
        }
        catch (IllegalArgumentException e)
        {
            log.warn("Illegal hint context '" + context + "' specified!");
            return null;
        }
    }

    public final Hint getRandomHint()
    {
        return getHintManager().getRandomHint(getLoggedInUser(), new JiraHelper(ActionContext.getRequest()));
    }


    /**
     * Retrieve the value from a conglomerate Cookie from the request.
     *
     * @param cookieName The name of the conglomerate cookie
     * @param key The key of the value
     * @return the value (or the empty-string if it did not exist)
     */
    public String getConglomerateCookieValue(String cookieName, String key)
    {
        Map<String, String> map = CookieUtils.parseConglomerateCookie(cookieName, ActionContext.getRequest());
        String value = map.get(key);
        return value != null ? value : "";
    }


    /**
     * Set the value key/value pair in a conglomerate Cookie.
     *
     * @param cookieName The name of the conglomerate cookie
     * @param key The key of the value
     * @param value The value
     */
    public void setConglomerateCookieValue(String cookieName, String key, String value)
    {
        Map<String, String> map = CookieUtils.parseConglomerateCookie(cookieName, ActionContext.getRequest());
        if (StringUtils.isNotBlank(value))
        {
            map.put(key, value);
        }
        else
        {
            map.remove(key);
        }
        Cookie cookie = CookieUtils.createConglomerateCookie(cookieName, map, ActionContext.getRequest());
        ActionContext.getResponse().addCookie(cookie);
    }
}
