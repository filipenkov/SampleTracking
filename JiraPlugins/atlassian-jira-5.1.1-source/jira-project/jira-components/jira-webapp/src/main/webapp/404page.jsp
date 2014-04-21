<!DOCTYPE html>
<%@ page import="com.atlassian.jira.web.filters.steps.senderror.CaptureSendErrorMessageResponseWrapper" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.security.JiraAuthenticationContext" %>
<%@ page import="com.atlassian.jira.util.I18nHelper" %>
<%@ page import="com.atlassian.jira.config.properties.APKeys" %>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties" %>
<html>
<%
    final I18nHelper i18nBean = ComponentManager.getComponent(JiraAuthenticationContext.class).getI18nHelper();
    WebResourceManager webResourceManager = ComponentManager.getComponent(WebResourceManager.class);
    webResourceManager.requireResourcesForContext("atl.general");
    webResourceManager.requireResourcesForContext("jira.general");
    String msg = (String) request.getAttribute(CaptureSendErrorMessageResponseWrapper.CAPTURED_MESSAGE_REQUEST_ATTR);
    msg = TextUtils.htmlEncode(msg);

    ApplicationProperties applicationProperties = ComponentManager.getComponent(ApplicationProperties.class);
    final String jiraTitle = applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE);
    final String jiraLogoUrl = applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_URL);
    final String jiraLogoWidth = applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_WIDTH);
    final String jiraLogoHeight = applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_HEIGHT);
%>
<head>
    <title><%=TextUtils.htmlEncode(i18nBean.getText("404.title"))%> (404)</title>
    <%@ include file="/includes/decorators/aui-layout/head-resources.jsp" %>
</head>
<body id="jira" class="aui-layout aui-theme-default page-type-message">
<div id="page">
    <header id="header" role="banner">
        <nav class="global" role="navigation">
            <div class="primary">
                <h1 id="logo"><a href="<%=request.getContextPath()%>/secure/MyJiraHome.jspa"><img src="<%=request.getContextPath() + TextUtils.htmlEncode(jiraLogoUrl)%>" width="<%=TextUtils.htmlEncode(jiraLogoWidth)%>" height="<%=TextUtils.htmlEncode(jiraLogoHeight)%>" alt="<%=TextUtils.htmlEncode(jiraTitle)%>" /></a></h1>
            </div>
        </nav>
    </header>
    <section id="content" role="main">
        <div class="content-container">
            <div class="content-body aui-panel">
                <header><h1><%=TextUtils.htmlEncode(i18nBean.getText("404.title"))%> (404)</h1></header>
                <div class="aui-message warning">
                    <span class="aui-icon icon-warning"></span>
                    <p class="title"><%=msg%></p>
                    <p><%=TextUtils.htmlEncode(i18nBean.getText("404.message"))%></p>
                    <p><a href="<%=request.getContextPath()%>/secure/MyJiraHome.jspa"><%=TextUtils.htmlEncode(i18nBean.getText("common.concepts.jira.home"))%></a></p>
                </div>
            </div>
        </div>
    </section>
</div>
</body>
</html>