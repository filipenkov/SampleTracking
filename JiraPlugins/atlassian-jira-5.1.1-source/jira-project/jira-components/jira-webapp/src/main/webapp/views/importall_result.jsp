<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.security.login.LoginManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%
    ComponentManager.getComponentInstanceOfType(LoginManager.class).logout(request, response);
%>
<html>
<head>
    <title><ww:text name="'admin.import.import.project.data'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
    <div class="form-body">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">success</aui:param>
            <aui:param name="'iconText'"><ww:text name="'admin.common.words.success'"/></aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.import.successful.import'"/></p>
            </aui:param>
        </aui:component>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.import.logged.out'"/></p>
                <p><a id="login" href="<%= request.getContextPath() %>/secure/"><ww:text name="'admin.common.phrases.log.in.again'"/></a>.</p>
            </aui:param>
        </aui:component>
        <input type="hidden" id="importresult" value="success" />
    </div>
</body>
</html>