<!DOCTYPE html>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties" %>
<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="com.atlassian.jira.config.properties.APKeys" %>
<%@ page import="com.atlassian.jira.config.properties.LookAndFeelBean" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%
    ApplicationProperties ap = ComponentAccessor.getComponentOfType(ApplicationProperties.class);
    final LookAndFeelBean lAndF = LookAndFeelBean.getInstance(ap);
    String applicationID = lAndF.getApplicationID();
%>
<html>
<head>
    <title><%= TextUtils.htmlEncode(ap.getDefaultBackedString(APKeys.JIRA_TITLE)) %> - <decorator:title default="New Generation Issue Tracking" /></title>
    <meta http-equiv="Content-Type" content="<%= ap.getContentType() %>" />
    <link rel="shortcut icon" href="<%=request.getContextPath()%>/favicon.ico">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/images/icons/favicon.png">

    <decorator:head/>
<%
    WebResourceManager webResourceManager = ComponentManager.getComponent(WebResourceManager.class);
    webResourceManager.requireResource("jira.webresources:jira-setup-greenhopper");
    webResourceManager.includeResources(out, UrlMode.RELATIVE);
%>
</head>
<body id="<%= applicationID %>" class="setupwizard <decorator:getProperty property="body.class" />">
<div id="header">
    <div id="header-top"></div>
    <div id="header-bottom"></div>
</div>
<div id="main-content">
    <div id="gh-setup">
        <div class="setup-header">
            <h1><ww:text name="'setup.greenhopper.title'" /></h1>
        </div>
        <div class="setup-panel">
            <div class="setup-active-area">
                <decorator:body />
            </div>
        </div>
    </div>
</div>
</body>
</html>
