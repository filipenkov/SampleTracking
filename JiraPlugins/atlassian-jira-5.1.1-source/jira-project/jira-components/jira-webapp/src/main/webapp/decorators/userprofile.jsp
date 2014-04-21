<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.security.JiraAuthenticationContext" %>
<%@ page import="webwork.action.ActionContext" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib prefix="decorator" uri="sitemesh-decorator" %>
<%
    // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "atl.userprofile"
    final WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResourcesForContext("atl.userprofile");
    webResourceManager.requireResourcesForContext("jira.userprofile");
    webResourceManager.requireResourcesForContext("atl.general");
    webResourceManager.requireResourcesForContext("jira.general");
%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="/includes/decorators/aui-layout/head-common.jsp" %>
    <%@ include file="/includes/decorators/aui-layout/head-resources.jsp" %>
    <decorator:head/>
</head>
<body id="jira" class="aui-layout aui-theme-default page-type-userprofile <decorator:getProperty property="body.class" />">
<div id="page">
    <header id="header" role="banner">
        <%@ include file="/includes/decorators/aui-layout/notifications-header.jsp" %>
        <%@ include file="/includes/decorators/unsupported-browsers.jsp" %>
        <%@ include file="/includes/decorators/aui-layout/header.jsp" %>
    </header>
    <%@ include file="/includes/decorators/aui-layout/notifications-content.jsp" %>
    <section id="content" role="main">
        <div class="content-container">
            <%
                JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
                final HttpServletRequest originalRequest = ActionContext.getRequest();
                try
                {
                    //full user format requires the request to be set in the ActionContext :(
                    ActionContext.setRequest(request);
                    if(authenticationContext.getLoggedInUser() != null)
                    {
                        request.setAttribute("username", authenticationContext.getLoggedInUser().getName());
                    }
            %>
            <div class="content-related">
                <jira:formatuser user="@username" type="'fullProfile'" id="'view_profile'"/>
            </div>
            <%
                }
                finally
                {
                    ActionContext.setRequest(originalRequest);
                }
            %>
            <div class="content-body aui-panel">
                <decorator:body />
            </div>
        </div>
    </section>
    <footer id="footer" role="contentinfo">
        <%--<%@ include file="/includes/decorators/aui-layout/notifications-footer.jsp" %>--%>
        <%@ include file="/includes/decorators/aui-layout/footer.jsp" %>
    </footer>
</div>
</body>
</html>
