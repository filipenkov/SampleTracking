<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.portal.PortletConfiguration" %>
<%@ page import="com.atlassian.jira.web.portlet.bean.PortletRenderer" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="webwork.action.CoreActionContext" %>
<%@ taglib uri="webwork" prefix="ww" %>

<ww:property value="portletConfiguration">
    <%= new PortletRenderer().renderPortlet(request, response, (PortletConfiguration) CoreActionContext.getValueStack().findValue((String) null), false) %>
</ww:property>
