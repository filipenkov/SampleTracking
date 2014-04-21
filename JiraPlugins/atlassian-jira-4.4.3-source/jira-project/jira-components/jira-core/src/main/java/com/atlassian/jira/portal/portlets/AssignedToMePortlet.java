package com.atlassian.jira.portal.portlets;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.component.TableLayoutFactory;
import com.atlassian.query.order.SortOrder;
import com.opensymphony.user.User;

public class AssignedToMePortlet extends AbstractSearchResultsPortlet
{
    public AssignedToMePortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, ConstantsManager constantsManager, SearchProvider searchProvider, ApplicationProperties applicationProperties, TableLayoutFactory tableLayoutFactory)
    {
        super(authenticationContext, permissionManager, constantsManager, searchProvider, applicationProperties, tableLayoutFactory);
    }

    protected SearchRequest getSearchRequest(PortletConfiguration portletConfiguration)
    {
        final User user = authenticationContext.getUser();
        if (user == null)
        {
            return null;
        }

        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where().assigneeUser(user.getName()).and().unresolved();
        builder.orderBy().priority(SortOrder.DESC).createdDate(SortOrder.ASC);
        return new SearchRequest(builder.buildQuery());
    }

    protected String getLinkToSearch(SearchRequest searchRequest, PortletConfiguration portletConfiguration)
    {
        return new StringBuffer("secure/IssueNavigator.jspa")
                .append("?reset=true")
                .append("&mode=hide")
                .append("&assigneeSelect=").append(DocumentConstants.ISSUE_CURRENT_USER)
                .append("&resolution=-1")
                .append("&sorter/field=").append(IssueFieldConstants.CREATED)
                .append("&sorter/order=").append(NavigableField.ORDER_ASCENDING)
                .append("&sorter/field=").append(IssueFieldConstants.PRIORITY)
                .append("&sorter/order=").append(NavigableField.ORDER_DESCENDING)
                .toString();
    }

    protected String getSearchName(SearchRequest searchRequest)
    {
        return authenticationContext.getI18nHelper().getText("portlet.assignedissues.assigned");
    }

    protected String getSearchTypeName()
    {
        return authenticationContext.getI18nHelper().getText("portlet.savedfilter.openissues");
    }

    protected String getNoIssuesText()
    {
        return authenticationContext.getI18nHelper().getText("portlet.assignedissues.noissues");
    }

}
