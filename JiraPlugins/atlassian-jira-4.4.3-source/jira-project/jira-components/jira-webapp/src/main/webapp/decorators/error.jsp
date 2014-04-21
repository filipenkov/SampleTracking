<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww"%>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%
    // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "atl.error"
    // calling variable wrm here because header.jsp already declares a webResourceManager.
    final WebResourceManager wrm = ComponentManager.getInstance().getWebResourceManager();
    wrm.requireResourcesForContext("atl.error");
    wrm.requireResourcesForContext("jira.error");
%>
<%@ include file="/includes/decorators/header.jsp" %>
<div class="jira-error">
    <decorator:body />
</div>
<%@ include file="/includes/decorators/footer.jsp" %>
