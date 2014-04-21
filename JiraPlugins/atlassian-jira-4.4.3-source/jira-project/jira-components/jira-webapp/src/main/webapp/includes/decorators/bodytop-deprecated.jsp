<%@ page import="com.atlassian.jira.ComponentManager,
                 com.atlassian.jira.config.properties.APKeys,
                 com.atlassian.jira.config.properties.LookAndFeelBean,
                 com.atlassian.jira.plugin.navigation.PluggableTopNavigation" %>
<%@ page import="com.atlassian.jira.plugin.navigation.TopNavigationModuleDescriptor" %>
<%@ page import="com.atlassian.jira.plugin.util.ModuleDescriptorComparator" %>
<%@ page import="com.atlassian.jira.web.action.admin.EditAnnouncementBanner" %>
<%@ page import="com.atlassian.plugin.PluginAccessor" %>
<%@ page import="com.atlassian.seraph.util.SecurityUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="jiratags" prefix="jira" %>

<%-- DEPRECATED IN FAVOUR OF THE NEW bodytop.jsp which provides cleaner/more modern JIRA 4.0 look and feel.--%>

<decorator:usePage id="p"/>
<%
    final Logger log = Logger.getLogger("includes.decorators.bodytop");
    LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(ap);
    String applicationID = lookAndFeelBean.getApplicationID();
    String alertHeader = ap.getDefaultBackedText(APKeys.JIRA_ALERT_HEADER);
    String alertHeaderVisibility = ap.getDefaultBackedString(APKeys.JIRA_ALERT_HEADER_VISIBILITY);

    boolean userLoggedIn;
    try
    {
        userLoggedIn = (SecurityUtils.getAuthenticator(pageContext.getServletContext()).getUser((HttpServletRequest) pageContext.getRequest()) != null);
    }
    catch (Exception e)
    {
        log.error(e);
        throw new RuntimeException(e);
    }
%>

<body id="<%= applicationID%>" <decorator:getProperty property="body.class" writeEntireProperty="true" /> >
<%@ include file="/includes/decorators/unsupported-browsers.jsp" %>
<%@ include file="/includes/decorators/websudo-message.jsp" %>

<%
    PluginAccessor pluginAccessor = ComponentManager.getInstance().getPluginAccessor();

    // Get all the top nav module descriptions and sort them by order
    List topNavPlugins = pluginAccessor.getEnabledModuleDescriptorsByClass(TopNavigationModuleDescriptor.class);
    Collections.sort(topNavPlugins, ModuleDescriptorComparator.COMPARATOR);

    String selectedSection = p.getProperty("page.section");
    if (StringUtils.isNotBlank(selectedSection))
    {
        request.setAttribute("jira.selected.section", selectedSection);
    }

    // Render all the top nav plugins
    for (Iterator iterator = topNavPlugins.iterator(); iterator.hasNext();) {
        TopNavigationModuleDescriptor topNavModuleDescriptor = (TopNavigationModuleDescriptor) iterator.next();
        PluggableTopNavigation pluggableTopNavigation = topNavModuleDescriptor.getModule();

%>
<%= pluggableTopNavigation.getHtml(request) %>
<%
    }
%>

<jsp:include page="/includes/decorators/global-translations.jsp" />

<%--This should always be included as it is not part of a themable plugin, instead it is core functionality--%>
<% if (alertHeader != null && alertHeader.trim().length() > 0 && ("public".equals(alertHeaderVisibility) || userLoggedIn)) { %>
<div class="alertHeader">
<%= alertHeader %>
</div>
<% } %>
