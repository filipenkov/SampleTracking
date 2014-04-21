<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<html>
<head>
    <meta name="decorator" content="general" />
    <%
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:jira-fields");
    %>
    <title>
        <ww:text name="'user.profile'"/> : <ww:property value="user/fullName"/>
    </title>
</head>

<body class="type-aa">
<jsp:include page="profile/viewprofile.jsp" />
</body>
</html>

