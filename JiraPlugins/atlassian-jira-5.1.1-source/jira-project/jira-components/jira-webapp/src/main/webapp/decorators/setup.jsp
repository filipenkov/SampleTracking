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
<!DOCTYPE html>
<html>
<head>
    <title><%= TextUtils.htmlEncode(ap.getDefaultBackedString(APKeys.JIRA_TITLE)) %> - <decorator:title default="New Generation Issue Tracking" /></title>
    <meta http-equiv="Content-Type" content="<%= ap.getContentType() %>" />
    <link rel="shortcut icon" href="<%=request.getContextPath()%>/favicon.ico" />
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/images/icons/favicon.png" />
    <decorator:head/>
<%
    WebResourceManager webResourceManager = ComponentManager.getComponent(WebResourceManager.class);
    webResourceManager.requireResource("jira.webresources:jira-setup");
    webResourceManager.includeResources(out, UrlMode.RELATIVE);
%>
</head>
<body id="jira" class="aui-layout aui-theme-default jira-style-setup <decorator:getProperty property="body.class" />">
<div id="page">
    <header id="header" role="banner">
        <div class="global"></div>
        <div class="local"></div>
    </header>
    <section id="content" role="main">
        <div id="jira-setup">
            <div class="setup-header">
                <h1><ww:text name="'setup.welcome.title'" /></h1>
            </div>
            <div class="setup-panel">
                <div class="setup-active-area">
                    <%@ include file="/includes/decorators/unsupported-browsers.jsp" %>
                    <decorator:body />
                </div>
            </div>
        </div>
    </section>
</div>
</body>
</html>
