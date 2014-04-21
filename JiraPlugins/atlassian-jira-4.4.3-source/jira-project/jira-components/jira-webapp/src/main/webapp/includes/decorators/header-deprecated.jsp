<!DOCTYPE html>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties"%>
<%@ page import="com.atlassian.jira.config.properties.APKeys"%>
<%@ page import="com.atlassian.jira.ManagerFactory"%>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager"%>
<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.jira.config.properties.LookAndFeelBean" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%-- IF YOU EDIT THIS - BE SURE TO CHECK IF YOU NEED TO EDIT DECORATORS/FRONTPAGE.JSP && popups.jsp &&
DEPRECATED IN FAVOUR OF THE NEW header.jsp which provides cleaner/more modern JIRA 4.0 look and feel.
--%>
<decorator:usePage id="originalPage" />
<%
    //this is all to ensure that the title prefix is not the same as the title.
    //See JRA-404
    ApplicationProperties ap = ManagerFactory.getApplicationProperties();
    String origTitle = originalPage.getTitle();
    String titlePrefix = ap.getDefaultBackedString(APKeys.JIRA_TITLE);
    if (origTitle != null && origTitle.equals(titlePrefix))
    {
        titlePrefix = null;
    }

    final WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    final KeyboardShortcutManager shortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
%>
<html>
<head>
    <title><decorator:title default="Welcome" /><% if (titlePrefix != null) { %> - <%= TextUtils.htmlEncode(titlePrefix) %> <% } %></title>

    <decorator:head/>
    <meta http-equiv="Content-Type" content="<%= ap.getContentType() %>" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Expires" content="-1" />
    <%@ include file="/includes/decorators/xsrftoken.jsp" %>
    <link rel="shortcut icon" href="<%= webResourceManager.getStaticResourcePrefix()%>/favicon.ico">
    <link rel="icon" type="image/png" href="<%= webResourceManager.getStaticResourcePrefix()%>/images/icons/favicon.png">
    <%-- TODO: Do this nicer --%>
    <script type="text/javascript">var contextPath = '<%=request.getContextPath()%>';</script>
<%
    webResourceManager.requireResource("jira.webresources:global-static");
    webResourceManager.requireResource("jira.webresources:jira-global");
    webResourceManager.requireResource("jira.webresources:key-commands");
    webResourceManager.requireResource("jira.webresources:header");
    //    
    // if we are not already an admin page then we want to include alt.general resources
    if (! "atl.admin".equals(request.getAttribute("jira.web.resource.context")))
    {
        webResourceManager.requireResourcesForContext("atl.general");
        webResourceManager.requireResourcesForContext("jira.general");
    }
    webResourceManager.requireResource("jira.webresources:set-focus");

    webResourceManager.includeResources(out, UrlMode.AUTO);
%>
    <% if (shortcutManager.isKeyboardShortcutsEnabled()) { %>
        <script type="text/javascript" src="<%=request.getContextPath() + TextUtils.htmlEncode(shortcutManager.includeShortcuts())%>"></script>
    <% } %>
</head>

<%@ include file="/includes/decorators/bodytop-deprecated.jsp" %>

