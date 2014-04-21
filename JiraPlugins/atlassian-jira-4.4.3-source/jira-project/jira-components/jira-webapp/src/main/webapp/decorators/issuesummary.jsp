<%@ page import="com.atlassian.jira.exception.IssueNotFoundException" %>
<%@ page import="com.atlassian.jira.exception.IssuePermissionException" %>
<%@ page import="com.atlassian.jira.issue.Issue" %>
<%@ page import="com.atlassian.jira.web.action.issue.IssueSummaryAware" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%--
    Use this decorator for Issue-related pages that do not have the headersummaryblock (project avatar, issue title, etc)
--%>
<ww:property value="/" id="action"/>
<%
    Object action = pageContext.getAttribute("action");
    if (action instanceof IssueSummaryAware)
    {
        IssueSummaryAware issueSummaryAware = (IssueSummaryAware) action;
        Issue issue;
        try
        {
            //This call may raise an IssueNotFoundException or an IssuePermissionException. In this case, the
            //input HTML is completely ignored even though it may contain some data. Changing the decorator
            //to expose the generated HTML on Exception will expose information that actions did not expect. The actions seem to make the
            //assumption that when an Exception is thrown this decorator will not display the generated html.
            //
            //For example, the CommentAssignIssue action actually generates some valid HTML when trying to assign an
            //issue that a user does not have permission to see. It stops the user from seeing this HTML by throwing
            //an IssueNotFoundException or IssuePermissionException.
            issue = issueSummaryAware.getSummaryIssue();
%>
<%@ include file="/includes/decorators/header.jsp" %>
<decorator:body/>
<%@ include file="/includes/decorators/footer.jsp" %>
<%
        }
        catch (IssueNotFoundException e)
        {
%>
<page:applyDecorator name="message">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'admin.errors.issues.current.issue.null'"/></p>
        </aui:param>
    </aui:component>
</page:applyDecorator>
<%
        }
        catch (IssuePermissionException e)
        {
%>
<page:applyDecorator name="message">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'titleText'"><ww:text name="'generic.notloggedin.title'"/></aui:param>
        <aui:param name="'messageHtml'">
            <%@ include file="/includes/generic-notloggedin.jsp" %>
        </aui:param>
    </aui:component>
</page:applyDecorator>
<%
        }
    }
    else
    {
        throw new UnsupportedOperationException("Action " + action + " does not implement IssueSummaryAware");
    }
%>
