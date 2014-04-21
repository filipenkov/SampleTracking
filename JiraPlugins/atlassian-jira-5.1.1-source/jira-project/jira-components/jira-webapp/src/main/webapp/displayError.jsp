<!DOCTYPE html>
<%@ page import="com.atlassian.jira.config.properties.APKeys" %>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties" %>
<%@ page import="com.atlassian.jira.security.JiraAuthenticationContext" %>
<%@ page import="com.atlassian.jira.util.I18nHelper" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<html>
<%
    // include the relevant contexts
    WebResourceManager webResourceManager = ComponentManager.getComponent(WebResourceManager.class);
    webResourceManager.requireResourcesForContext("atl.general");
    webResourceManager.requireResourcesForContext("jira.general");
    webResourceManager.requireResourcesForContext("atl.global");
    webResourceManager.requireResourcesForContext("jira.global");

    // figure out the status code
    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    String statusText = HttpStatus.getStatusText(statusCode);

    I18nHelper i18nBean = ComponentManager.getComponent(JiraAuthenticationContext.class).getI18nHelper();

    ApplicationProperties applicationProperties = ComponentManager.getComponent(ApplicationProperties.class);
    final String jiraTitle = applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE);
    final String jiraLogoUrl = applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_URL);
    final String jiraLogoWidth = applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_WIDTH);
    final String jiraLogoHeight = applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_HEIGHT);
%>
<head>
    <title><%=TextUtils.htmlEncode(statusText)%> (<%=statusCode%>)</title>
    <%@ include file="/includes/decorators/aui-layout/head-resources.jsp" %>
</head>
<body id="jira" class="aui-layout aui-style-default page-type-message">
<div id="page">
    <header id="header" role="banner">
        <nav class="global" role="navigation">
            <div class="primary">
                <h1 id="logo"><a href="<%=request.getContextPath()%>/secure/MyJiraHome.jspa"><img src="<%=request.getContextPath() + TextUtils.htmlEncode(jiraLogoUrl)%>" width="<%=TextUtils.htmlEncode(jiraLogoWidth)%>" height="<%=TextUtils.htmlEncode(jiraLogoHeight)%>" alt="<%=TextUtils.htmlEncode(jiraTitle)%>" /></a></h1>
            </div>
        </nav>
    </header>
    <section id="content" role="main">
        <header><h1><%=TextUtils.htmlEncode(statusText)%> (<%=statusCode%>)</h1></header>
        <div class="content-container">
            <div class="content-body">
                <p><%=i18nBean.getText("http.generic.error.message", TextUtils.htmlEncode(statusCode + " - " + statusText))%></p>
                <p><a href="<%=request.getContextPath()%>/secure/MyJiraHome.jspa"><%=TextUtils.htmlEncode(i18nBean.getText("common.concepts.jira.home"))%></a></p>
            </div>
        </div>
    </section>
</div>
</body>
</html>
