<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.navigation.FooterModuleDescriptor" %>
<%@ page import="com.atlassian.jira.plugin.navigation.PluggableFooter" %>
<%@ page import="com.atlassian.jira.plugin.util.ModuleDescriptorComparator" %>
<%@ page import="com.atlassian.jira.web.filters.accesslog.AccessLogImprinter" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>

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
<%= pluggableFooter.getFullFooterHtml(request) %>
<%
    }
    final AccessLogImprinter imprinter = new AccessLogImprinter(request);
    out.print(imprinter.imprintHiddenHtml());
    out.print(imprinter.imprintHTMLComment());
%>
<jsp:include page="/includes/decorators/global-translations.jsp" />