<%@ page import="com.atlassian.jira.ManagerFactory,
                 com.atlassian.jira.config.properties.APKeys,
                 com.atlassian.jira.config.properties.ApplicationProperties"%>
<%
	ApplicationProperties ap = ManagerFactory.getApplicationProperties();

    if ("true".equalsIgnoreCase(ap.getString(APKeys.JIRA_SETUP)))
    {
        response.sendRedirect(request.getContextPath() + "/secure/project/ViewProjects.jspa");
        return;
    }
%>
