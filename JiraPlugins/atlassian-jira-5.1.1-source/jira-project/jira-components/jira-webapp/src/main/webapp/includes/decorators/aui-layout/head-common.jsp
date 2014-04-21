<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.atlassian.jira.config.properties.APKeys" %>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties" %>
<%@ page import="com.atlassian.jira.plugin.webresource.JiraWebResourceManager" %>
<%@ page import="com.atlassian.jira.security.JiraAuthenticationContext" %>
<%@ page import="com.atlassian.jira.util.BuildUtilsInfo" %>
<%@ page import="com.atlassian.plugin.web.WebInterfaceManager" %>
<%@ page import="com.atlassian.plugin.web.model.WebPanel" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.atlassian.jira.plugin.webfragment.DefaultWebFragmentContext" %>
<%@ page import="java.lang.Boolean" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="webwork" prefix="ww" %>
<decorator:usePage id="originalPage" />
<% ApplicationProperties ap = ComponentAccessor.getComponentOfType(ApplicationProperties.class); %>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=Edge;chrome=1" />
<%

    final JiraWebResourceManager headWebResourceManager = (JiraWebResourceManager) ComponentManager.getInstance().getWebResourceManager();
    String pageTitle = originalPage.getTitle();
    String appTitle = TextUtils.htmlEncode(ap.getDefaultBackedString(APKeys.JIRA_TITLE));
    final String actualTitle;
    if (pageTitle == null)
    {
        actualTitle = appTitle;
    }
    else if (pageTitle.equals(appTitle))
    {
        actualTitle = pageTitle;
    }
    else
    {
        actualTitle = pageTitle + " - " + appTitle;
    }
%>
<title><%= actualTitle %></title>
<%
    final BuildUtilsInfo headBuildUtilsInfo = ComponentManager.getComponentInstanceOfType(BuildUtilsInfo.class);
    final JiraAuthenticationContext headAuthenticationContext = ComponentManager.getComponentInstanceOfType(JiraAuthenticationContext.class);

    if(Boolean.getBoolean("atlassian.disable.issue.collector"))
    {
        headWebResourceManager.putMetadata("disable-issue-collector", "true");
    }
    headWebResourceManager.putMetadata("context-path", request.getContextPath());
    headWebResourceManager.putMetadata("version-number", headBuildUtilsInfo.getVersion());
    headWebResourceManager.putMetadata("build-number", headBuildUtilsInfo.getCurrentBuildNumber());
    headWebResourceManager.putMetadata("remote-user", headAuthenticationContext.getLoggedInUser() != null ? headAuthenticationContext.getLoggedInUser().getName() : "");
    headWebResourceManager.putMetadata("remote-user-fullname", headAuthenticationContext.getLoggedInUser() != null ? headAuthenticationContext.getLoggedInUser().getDisplayName() : "");
    headWebResourceManager.putMetadata("user-locale", headAuthenticationContext.getLocale().toString());
    headWebResourceManager.putMetadata("app-title", appTitle);

    final KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
    headWebResourceManager.putMetadata("keyboard-shortcuts-enabled", Boolean.toString(keyboardShortcutManager.isKeyboardShortcutsEnabled()));

    final Map<String, String> metadata = headWebResourceManager.getMetadata();
    for (Map.Entry<String, String> metaDataEntry : metadata.entrySet())
    {
        %>
<meta name="ajs-<%=TextUtils.htmlEncode(metaDataEntry.getKey())%>" content="<%=TextUtils.htmlEncode(metaDataEntry.getValue())%>" >
        <%
    }

    final WebInterfaceManager headWebInterfaceManager = ComponentManager.getComponentInstanceOfType(WebInterfaceManager.class);

    Map<String, Object> context = DefaultWebFragmentContext.get("atl.header");
    List<WebPanel> displayableWebPanels = headWebInterfaceManager.getDisplayableWebPanels("atl.header", context);
    for (WebPanel webPanel : displayableWebPanels) {%>
        <%=webPanel.getHtml(context)%>
    <%}
%>
<%--<meta http-equiv="Pragma" content="no-cache" />--%>
<%--<meta http-equiv="Expires" content="-1" />--%>
<%@ include file="/includes/decorators/xsrftoken.jsp" %>

<link rel="shortcut icon" href="<%= headWebResourceManager.getStaticResourcePrefix(UrlMode.RELATIVE)%>/favicon.ico">
<link rel="search" type="application/opensearchdescription+xml" href="<%= request.getContextPath()%>/osd.jsp" title="<%= TextUtils.htmlEncode(ap.getDefaultBackedString(APKeys.JIRA_TITLE)) %>"/>
