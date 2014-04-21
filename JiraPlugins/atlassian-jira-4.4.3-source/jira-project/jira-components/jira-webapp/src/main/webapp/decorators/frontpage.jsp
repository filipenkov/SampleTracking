<!DOCTYPE html>
<%@ page import="com.atlassian.jira.ManagerFactory"%>
<%@ page import="com.atlassian.jira.config.properties.APKeys"%>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties"%>
<%@ page import="com.atlassian.jira.config.properties.LookAndFeelBean"%>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager"%>
<%@ page import="com.atlassian.jira.plugin.navigation.FooterModuleDescriptor"%>
<%@ page import="com.atlassian.jira.plugin.navigation.PluggableFooter"%>
<%@ page import="com.atlassian.jira.plugin.util.ModuleDescriptorComparator"%>
<%@ page import="com.atlassian.jira.util.BrowserUtils" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    final KeyboardShortcutManager shortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
    ApplicationProperties ap = ManagerFactory.getApplicationProperties();
    final LookAndFeelBean lAndF = LookAndFeelBean.getInstance(ap);
    String topBgColour = lAndF.getTopBackgroundColour();
    String topSepBgColour = lAndF.getTopSeparatorBackgroundColor();
    String menuBgColour = lAndF.getMenuBackgroundColour();
    String linkColour = lAndF.getTextLinkColour();
    String linkAColour = lAndF.getTextActiveLinkColour();
    String applicationID = lAndF.getApplicationID();

    //don't use default backed string here, as we will use the large logo
    String jiraLogo = lAndF.getLogoUrl();
    if (jiraLogo != null && !jiraLogo.startsWith("http://") && !jiraLogo.startsWith("https://"))
    {
        jiraLogo = webResourceManager.getStaticResourcePrefix() + jiraLogo;
    }

    String userAgent = TextUtils.noNull(request.getHeader("USER-AGENT"));
    boolean logoNeedsOpacityFix = jiraLogo != null && jiraLogo.endsWith(".png") && BrowserUtils.isFilterBasedPngOpacity(userAgent);
%>
<html>
<head>
	<title><%= TextUtils.htmlEncode(ap.getDefaultBackedString(APKeys.JIRA_TITLE)) %> - <decorator:title default="New Generation Issue Tracking" /></title>
    <meta http-equiv="Content-Type" content="<%= ap.getContentType() %>" />
    <%@ include file="/includes/decorators/xsrftoken.jsp" %>
    <link rel="shortcut icon" href="<%= webResourceManager.getStaticResourcePrefix(UrlMode.RELATIVE)%>/favicon.ico">
    <link rel="icon" type="image/png" href="<%= webResourceManager.getStaticResourcePrefix(UrlMode.RELATIVE)%>/images/icons/favicon.png">

    <script type="text/javascript">var contextPath = "<%=request.getContextPath()%>"</script>
<%
    webResourceManager.requireResource("jira.webresources:global-static");
    webResourceManager.requireResource("jira.webresources:jira-global");
    webResourceManager.requireResourcesForContext("atl.general");
    webResourceManager.requireResourcesForContext("jira.general");
    webResourceManager.requireResource("jira.webresources:set-focus");
    webResourceManager.includeResources(out, UrlMode.AUTO);
%>
    <% if (shortcutManager.isKeyboardShortcutsEnabled()) { %>
        <script type="text/javascript" src="<%=request.getContextPath() + TextUtils.htmlEncode(shortcutManager.includeShortcuts())%>"></script>
    <% } %>
</head>

<body id="<%= applicationID %>" class="lp <decorator:getProperty property="body.class" />">
<%@ include file="/includes/decorators/unsupported-browsers.jsp" %>
<div id="header">
    <div id="header-top">
        <a id="logo" href="<%= request.getContextPath() %>/secure/Dashboard.jspa">
            <ww:component name="'default'" template="logoWithOpacity.jsp" >
                <ww:param name="'needsOpacityFix'"><%= logoNeedsOpacityFix %></ww:param>
                <ww:param name="'logoTitle'"><%= TextUtils.htmlEncode(ap.getString(APKeys.JIRA_TITLE)) %></ww:param>
                <ww:param name="'logoUrl'"><%= jiraLogo %></ww:param>
                <ww:param name="'logoWidth'"><%= lAndF.getLogoWidth() %></ww:param>
                <ww:param name="'logoHeight'"><%= lAndF.getLogoHeight() %></ww:param>
            </ww:component>
        </a>
        <span style="float:right;padding:.5em;">
            <ww:component name="'default'" template="help.jsp">
                <ww:param name="'align'" value="'middle'"/>
            </ww:component>
        </span>
    </div>
</div>
<div id="main-content" >
    <div class="active-area">
        <decorator:body />
    </div>
</div>
<%
    // Get all the footer module descriptions and sort them by order
    List footerPlugins = ComponentManager.getInstance().getPluginAccessor().getEnabledModuleDescriptorsByClass(FooterModuleDescriptor.class);
    Collections.sort(footerPlugins, ModuleDescriptorComparator.COMPARATOR);

    // Render all the footer plugins
    for (Iterator iterator = footerPlugins.iterator(); iterator.hasNext();)
    {
        FooterModuleDescriptor footerModuleDescriptor = (FooterModuleDescriptor) iterator.next();
        PluggableFooter pluggableFooter = footerModuleDescriptor.getModule();
%>
<%= pluggableFooter.getSmallFooterHtml(request) %>
<%
    }
%>
</body>
</html>
