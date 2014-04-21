<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.security.JiraAuthenticationContext" %>
<%@ page import="webwork.action.ActionContext" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="jiratags" prefix="jira" %>

<%
    // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "atl.userprofile"
    final WebResourceManager wrm = ComponentManager.getInstance().getWebResourceManager();
    wrm.requireResourcesForContext("atl.userprofile");
    wrm.requireResourcesForContext("jira.userprofile");

%>
<%@ include file="/includes/decorators/header-deprecated.jsp" %>
<%
    JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
    final HttpServletRequest originalRequest = ActionContext.getRequest();
    try
    {
        //full user format requires the request to be set in the ActionContext :(
        ActionContext.setRequest(request);
        if(authenticationContext.getUser() != null)
        {
            request.setAttribute("username", authenticationContext.getUser().getName());
        }
%>
<body class="nl iss-nav user-profile">
<div id="iss-wrap">
    <div id="main-content">
        <div id="primary" class="column">
            <a class="toggle-lhc">Click to open or close</a>
            <div class="content rounded">
                <div class="user-details-container">
                    <jira:formatuser user="@username" type="'fullProfile'" id="'view_profile'"/>
                </div>
            </div>
        </div>
        <div id="secondary" class="column">
            <div class="content rounded">
                <decorator:body />
            </div>
        </div>
    </div>
</div>
<%
    }
    finally
    {
        ActionContext.setRequest(originalRequest);
    }
%>
<%@ include file="/includes/decorators/footer.jsp" %>
