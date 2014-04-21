<%@ taglib prefix="ww" uri="webwork" %>
<%@ page import="com.atlassian.jira.issue.Issue" %>
<%@ page import="com.atlassian.jira.issue.fields.FieldRenderingContext" %>
<%@ page import="com.atlassian.jira.issue.search.SearchException" %>
<%@ page import="com.atlassian.jira.issue.search.SearchResults" %>
<%@ page import="com.atlassian.jira.web.action.issue.IssueSearchResultsAction" %>
<%@ page import="com.atlassian.jira.web.component.IssueTableLayoutBean" %>
<%@ page import="com.atlassian.jira.web.component.IssueTableWebComponent" %>
<%@ page import="java.util.List" %>

<ww:property value="/" id="issueSearchResultsAction" />
<%
    final IssueSearchResultsAction issueSearchResultsAction = (IssueSearchResultsAction) request.getAttribute("issueSearchResultsAction");
    try
    {
        final SearchResults searchResultsPager = issueSearchResultsAction.getSearchResults();
        if (searchResultsPager != null)
        {
            final List<Issue> currentPage = searchResultsPager.getIssues();
            if (currentPage != null && !currentPage.isEmpty())
            {
                // as the table layout bean is expensive to create - lets not create it if we don't need to
                final IssueTableLayoutBean layoutBean = issueSearchResultsAction.getTableLayoutFactory().getStandardLayout(issueSearchResultsAction.getSearchRequest(), issueSearchResultsAction.getLoggedInUser());
                if ("printable".equalsIgnoreCase(request.getParameter("decorator")))
                {
                    layoutBean.setSortingEnabled(false);
                }
                // Set some display hints that lets templates know that we are rendering from the issue navigator
                layoutBean.addCellDisplayParam(FieldRenderingContext.NAVIGATOR_VIEW, Boolean.TRUE);
                %><%= new IssueTableWebComponent().getHtml(layoutBean, currentPage, searchResultsPager, issueSearchResultsAction.getSelectedIssueId()) %><%
            }
        }
    }
    catch (SearchException e)
    {
        // Fix for JRA-15257
        // invalid search input, ignore
    }
%>
