<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator" %>
<!DOCTYPE html>
<html>
<head>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResourcesForContext("jira.inline.dialog");
        webResourceManager.requireResourcesForContext("atl.inline.dialog");
        webResourceManager.includeResources(out, UrlMode.RELATIVE);
%>
</head>
<body class="aui-layout aui-theme-default page-type-inlinedialog">
<decorator:body />
<%@ include file="/includes/decorators/xsrftoken.jsp" %>
</body>
</html>