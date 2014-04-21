package com.atlassian.jira.portal.portlets;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.component.TableLayoutFactory;
import com.atlassian.query.order.SortOrder;
import com.opensymphony.user.User;

public class InProgressIssuesPortlet extends AbstractSearchResultsPortlet
{

    public InProgressIssuesPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, ConstantsManager constantsManager, SearchProvider searchProvider, ApplicationProperties applicationProperties, TableLayoutFactory tableLayoutFactory)
    {
        super(authenticationContext, permissionManager, constantsManager, searchProvider, applicationProperties, tableLayoutFactory);
    }

    protected SearchRequest getSearchRequest(PortletConfiguration portletConfiguration)
    {
        User remoteUser = authenticationContext.getUser();
        if (remoteUser == null) return null;

        //todo - this is dodgy, and hard-coding a particular workflow state.  If someone has customised there workflow, this won't work
        final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        queryBuilder.where().defaultAnd()
                    .status().eq((long)IssueFieldConstants.INPROGRESS_STATUS_ID)
                    .unresolved()
                    .assigneeUser(remoteUser.getName());

        queryBuilder.orderBy().priority(SortOrder.DESC).createdDate(SortOrder.ASC);
        SearchRequest sr = new SearchRequest(queryBuilder.buildQuery());
        sr.setOwnerUserName(remoteUser.getName());
        return sr;
    }

    protected String getLinkToSearch(SearchRequest searchRequest, PortletConfiguration portletConfiguration)
    {
        return "secure/IssueNavigator.jspa?reset=true&mode=hide&status=3&assigneeSelect=issue_current_user&resolution=-1&sorter/order=DESC&sorter/field=priority";
    }

    protected String getSearchName(SearchRequest searchRequest)
    {
        return authenticationContext.getI18nHelper().getText("portlet.inprogressissues.inprogress");
    }

    protected String getSearchTypeName()
    {
        return authenticationContext.getI18nHelper().getText("portlet.savedfilter.openissues");
    }

    protected String getNoIssuesText()
    {
        return authenticationContext.getI18nHelper().getText("portlet.inprogressissues.noissues");
    }

}
