<%@ taglib uri="webwork" prefix="ww" %>

<%@ page import="com.atlassian.jira.ManagerFactory,
                 com.atlassian.jira.config.properties.ApplicationProperties"
                contentType="application/vnd.ms-excel" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%
    //these headers need to be set to get around an internet explorer 6 SP1 bug.  See JRA-1738, JRA-14030
    //we need to override the headers that are set either by the server, or the encoding filter
    response.setHeader("Cache-Control", "private, must-revalidate, max-age=5"); // http 1.1
    response.setHeader("Pragma", ""); // http 1.0
    response.setDateHeader("Expires", System.currentTimeMillis() + 300); // prevent proxy caching
    response.setContentType("application/vnd.ms-excel;charset=" + ManagerFactory.getApplicationProperties().getEncoding());
%>

<html>
<head>
<title><ww:property value="report/name"/></title>
<meta name="decorator" content="none">
<META HTTP-EQUIV="Content-Type" Content="application/vnd.ms-excel; charset=<%= ManagerFactory.getApplicationProperties().getEncoding()%>">
</head>
<body>
<ww:property value="generatedReport" escape="false" />
</body>
</html>
