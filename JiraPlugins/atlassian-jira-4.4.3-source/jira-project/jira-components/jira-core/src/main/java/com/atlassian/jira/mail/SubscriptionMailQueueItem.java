/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.core.util.HTMLUtils;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.fields.FieldRenderingContext;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestUtils;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.BuildUtils;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.IssueTableWebComponent;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.AbstractMailQueueItem;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.query.Query;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SubscriptionMailQueueItem extends AbstractMailQueueItem
{
    private static final Logger log = Logger.getLogger(SubscriptionMailQueueItem.class);

    // As determined from the email-template-id-mappings.xml file
    private final Long FILTER_SUBSCRIPTION_TEMPLATE_ID = new Long("10000");
    private GenericValue subscription;
    private SearchRequest request;
    private User subscriptionCreator;
    private TemplateManager templateManager;
    private final UserManager userManager;
    private final GroupManager groupManager;

    private final MailingListCompiler mailingListCompiler;
    private final SearchService searchService;
    private static final int DEFAULT_MAIL_MAX_ISSUES = 200;

    public SubscriptionMailQueueItem(GenericValue sub, MailingListCompiler mailingListCompiler, final SearchService searchService, final TemplateManager templateManager,
        final UserManager userManager, GroupManager groupManager)
    {
        super();
        this.subscription = sub;
        this.mailingListCompiler = mailingListCompiler;
        this.searchService = searchService;
        this.templateManager = templateManager;
        this.userManager = userManager;
        this.groupManager = groupManager;
    }

    public void send() throws MailException
    {
        incrementSendCount();

        //Retrieve all the users to send the filter to.
        String groupName = subscription.getString("group");
        if (TextUtils.stringSet(groupName))
        {
            Group group = userManager.getGroup(groupName);
            if (group == null)
            {
                log.warn("Group '" + groupName + "' referenced in subscription '" + subscription.getLong("id") + "' of filter '" + subscription.getLong("filterID") + "' does not exist.");
                return;
            }

            try
            {
                Iterable<User> groupUser = groupManager.getUsersInGroup(groupName);
                for (User user : groupUser)
                {
                    sendSearchRequestEmail(user);
                }
            }
            catch (Exception ex)
            {
                log.error(ex, ex);
                throw new MailException(ex);
            }
        }
        else
        {
            String userName = subscription.getString("username");
            User user = getSubscriptionUser();
            if (user == null)
            {

                log.warn("User '" + userName + "' referenced in subscription '" + subscription.getLong("id") + "' of filter '" + subscription.getLong("filterID") + "' does not exist.");
            }
            else
            {
                try
                {
                    sendSearchRequestEmail(user);
                }
                catch (Exception ex)
                {
                    log.error(ex, ex);
                    throw new MailException(ex);
                }
            }
        }
    }

    private void sendSearchRequestEmail(final User user)
            throws Exception, GenericEntityException, VelocityException
    {
        // JRA-16611 : put the current user in the authentication context
        final JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        final User originalUser = jiraAuthenticationContext.getUser();
        jiraAuthenticationContext.setLoggedInUser(user);

        try
        {
            //Send mail to each user with the correctly executed search request
            Map params = getContextParams(subscription, getSearchRequest(), user);
            Set recipient = new HashSet();
            recipient.add(new NotificationRecipient(user));
            if (subscription == null)
            {
                throw new RuntimeException("Null subscription for user " + (user == null ? "null" : user.getName()));
            }
            String emailOnEmptyStr = subscription.getString("emailOnEmpty");
            if (emailOnEmptyStr == null)
            {
                throw new RuntimeException("emailOnEmpty not set for subscription " + subscription + ", user " + user);
            }
            if (params.get("issues") == null)
            {
                throw new RuntimeException("Null list of issues for subscription " + subscription + ", user " + user);
            }
            if (user != null && user.equals(getSubscriptionUser()))
            {
                params.put("recipientIsAuthor", Boolean.TRUE);
            }
            if (Boolean.valueOf(emailOnEmptyStr).booleanValue() || !((Collection) params.get("issues")).isEmpty())
            {
                mailingListCompiler.sendLists(recipient, null, null, FILTER_SUBSCRIPTION_TEMPLATE_ID, ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_BASEURL), params, null);
            }

        }
        finally
        {
            // restore the original user into the JiraAuthenticationContext
            jiraAuthenticationContext.setLoggedInUser(originalUser);
        }
    }

    public IssueTableLayoutBean getTableLayout(User user) throws Exception
    {
        SearchRequest searchRequest = getSearchRequest();
        Collection searchSorts = null;
        if (searchRequest != null)
        {
            final Query query = searchRequest.getQuery();
            if (query.getOrderByClause() != null)
            {
                searchSorts = query.getOrderByClause().getSearchSorts();
            }
        }
        IssueTableLayoutBean bean = new IssueTableLayoutBean(getColumns(user), searchSorts);
        bean.setSortingEnabled(false);
        bean.addCellDisplayParam(FieldRenderingContext.EMAIL_VIEW, Boolean.TRUE);
        return bean;
    }

    public List /*<ColumnLayoutItem>*/ getColumns(User user) throws Exception
    {
        final SearchRequest searchRequest = getSearchRequest();
        ColumnLayout columnLayout = null;

        // Check whether the search request is saved and whether the user has selected to override search requests column layout
        if (searchRequest != null && searchRequest.isLoaded() && searchRequest.useColumns())
        {
            // if not (useColumns for search request is true) use the search request's column layout
            columnLayout = ManagerFactory.getFieldManager().getColumnLayoutManager().getColumnLayout(user, getSearchRequest());
        }
        else
        {
            // if the filter columns are overriden use the user's column layout (or the system default if the user does not have a
            // personal column layout).
            columnLayout = ManagerFactory.getFieldManager().getColumnLayoutManager().getColumnLayout(user);
        }

        if (searchRequest == null)
        {
            return columnLayout.getAllVisibleColumnLayoutItems(user);
        }
        else
        {
            Query query = searchRequest.getQuery();
            final QueryContext queryContext = searchService.getQueryContext(user, query);
            return columnLayout.getVisibleColumnLayoutItems(user, queryContext);

        }
    }

    /**
     * This is the subject as displayed in the Mail Queue Admin page when the mail is BEING sent.
     * The string is retrieved in the default language for the JIRA system.
     *
     * @return String   the subject as displayed on the mail queue admin page
     */
    public String getSubject()
    {
        final I18nHelper.BeanFactory i18nFactory = ComponentManager.getComponentInstanceOfType(I18nHelper.BeanFactory.class);
        I18nHelper i18n = i18nFactory.getInstance(ComponentAccessor.getApplicationProperties().getDefaultLocale());
        try {
            String subjectTemplate = templateManager.getTemplateContent(FILTER_SUBSCRIPTION_TEMPLATE_ID, "subject");
            final Map contextParams = getContextParams(subscription, getSearchRequest(), null);
            contextParams.put("i18n", i18n);
            // Provide an OutlookDate formatter with the users locale
            OutlookDate formatter = new OutlookDate(i18n.getLocale());
            contextParams.put("dateformatter", formatter);
            return ManagerFactory.getVelocityManager().getEncodedBodyForContent(
                    subjectTemplate,
                    ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_BASEURL),
                    contextParams);
        }
        catch (Exception e)
        {
            log.error("Could not determine subject", e);
            return i18n.getText("bulk.bean.initialise.error");
        }
    }

    private SearchRequest getSearchRequest() throws GenericEntityException
    {
        //Retrieve the search request for this subscription
        if (request == null)
        {
            final JiraServiceContext ctx = new JiraServiceContextImpl(getSubscriptionUser());
            request = ComponentManager.getInstance().getSearchRequestService().getFilter(ctx, subscription.getLong("filterID"));
        }
        return request;
    }

    private User getSubscriptionUser()
    {
        if (subscriptionCreator == null)
        {
            subscriptionCreator = userManager.getUser(subscription.getString("username"));
        }

        return subscriptionCreator;
    }

    private Map getContextParams(GenericValue sub, SearchRequest sr, User u) throws Exception
    {
        String baseURL = ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_BASEURL);
        String contextPath = getContextPath(baseURL);
        SearchProvider searcher = ComponentManager.getInstance().getSearchProvider();

        final Map<String, Object> contextParams = new HashMap<String, Object>();
        IssueTableLayoutBean tableLayout = getTableLayout(u);
        IssueTableWebComponent iwtc = new IssueTableWebComponent();
        // now we need to fake out the action context (bear in mind we keep the old one around - in case they're running from the web not a job, ie Flush mail queue operation)
        ActionContext oldCtx = ActionContext.getContext();
        ActionContext.setContext(new ActionContext());
        ActionContext.setRequest(new SubscriptionMailQueueMockRequest(contextPath)); // our faked request
        // now put back the old context to be nice
        ActionContext.setContext(oldCtx);
        if(u != null)
        {
            SearchResults results = searcher.search((sr != null) ? sr.getQuery() : null, u, getPageFilter());
            List issues = results.getIssues();
            String issueTableHtml = iwtc.getHtml(tableLayout, issues, null);
            contextParams.put("totalIssueCount", new Integer(results.getTotal()));
            contextParams.put("actualIssueCount", new Integer(issues.size()));
            contextParams.put("issueTableHtml", issueTableHtml);
            contextParams.put("issues", issues);
            contextParams.put("user", u);
        }
        contextParams.put("baseHREF", getBaseURLWithoutContext(baseURL));
        contextParams.put("constantsManager", ManagerFactory.getConstantsManager());
        // TODO EMBCWD The following is deprecated and should be removed.
        contextParams.put("userManager", com.opensymphony.user.UserManager.getInstance());
        // TODO EMBCWD
        contextParams.put("req", new SubscriptionMailQueueMockRequest(contextPath));
        contextParams.put("searchRequest", sr);
        contextParams.put("SRUtils", new SearchRequestUtils());
        contextParams.put("subscription", sub);
        contextParams.put("StringUtils", new StringUtils());
        contextParams.put("HTMLUtils", new HTMLUtils());
        contextParams.put("buildutils", new BuildUtils());
        contextParams.put("textutils", new TextUtils());
        contextParams.put("webResourceManager", ComponentManager.getInstance().getWebResourceManager());
        contextParams.put("urlModeAbsolute", UrlMode.ABSOLUTE);

        return contextParams;
    }

    // Extracts the context path (if any) from a base URL. 
    private String getContextPath(String baseURL)
    {
        try
        {
            URL url = new URL(baseURL);
            return url.getPath();
        }
        catch (MalformedURLException e)
        {
            log.error("Incorrect baseURL format: " + baseURL);
            return "";
        }
    }

    /**
     * Return's the base url minus the context. This is intended to be used as a
     * for a base href.
     */
    private String getBaseURLWithoutContext(String baseURL)
    {
        String path = getContextPath(baseURL);

        return StringUtils.chomp(baseURL, path);
    }

    private PagerFilter getPageFilter()
    {
        final String application = ManagerFactory.getApplicationProperties().getDefaultBackedString(APKeys.JIRA_MAIL_MAX_ISSUES);

        int maxEmail;
        if (StringUtils.isBlank(application))
        {
            log.warn("The maximum number of issues to include in subscription email '(" + APKeys.JIRA_MAIL_MAX_ISSUES + ")' is not configured. Using default of " + DEFAULT_MAIL_MAX_ISSUES);
            maxEmail = DEFAULT_MAIL_MAX_ISSUES;
        }
        else
        {
            try
            {
                maxEmail = Integer.parseInt(application);

                if (maxEmail == 0)
                {
                    log.warn("The maximum number of issues to include in subscription email '(" + APKeys.JIRA_MAIL_MAX_ISSUES + ")' cannot be zero. Using default of " + DEFAULT_MAIL_MAX_ISSUES);
                    maxEmail = DEFAULT_MAIL_MAX_ISSUES;
                }
                else if (maxEmail < 0)
                {
                    maxEmail = -1;
                }
            }
            catch (NumberFormatException e)
            {
                log.warn("The maximum number of issues to include in subscription email '(" + APKeys.JIRA_MAIL_MAX_ISSUES + ")' is not a valid number. Using default of " + DEFAULT_MAIL_MAX_ISSUES);
                maxEmail = DEFAULT_MAIL_MAX_ISSUES;
            }
        }

        return new PagerFilter(maxEmail);
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof SubscriptionMailQueueItem))
        {
            return false;
        }

        final SubscriptionMailQueueItem subscriptionMailQueueItem = (SubscriptionMailQueueItem) o;

        if (request != null ? !request.equals(subscriptionMailQueueItem.request) : subscriptionMailQueueItem.request != null)
        {
            return false;
        }
        if (!subscription.equals(subscriptionMailQueueItem.subscription))
        {
            return false;
        }
        if (subscriptionCreator != null ? !subscriptionCreator.equals(subscriptionMailQueueItem.subscriptionCreator) : subscriptionMailQueueItem.subscriptionCreator != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = subscription.hashCode();
        result = 29 * result + (request != null ? request.hashCode() : 0);
        result = 29 * result + (subscriptionCreator != null ? subscriptionCreator.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        User subscriptionUser;
        subscriptionUser = getSubscriptionUser();
        return this.getClass().getName() + " owner: '" + subscriptionUser + "'";
    }
}
