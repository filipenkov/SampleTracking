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
<decorator:usePage id="p"/>
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
        if (!topNavModuleDescriptor.getCondition().shouldDisplay(context))
        {
            continue;
        }
        PluggableTopNavigation pluggableTopNavigation = topNavModuleDescriptor.getModule();

%>
<%= pluggableTopNavigation.getHtml(request) %>
<% } %>