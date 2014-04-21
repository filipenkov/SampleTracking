<%@ page import="com.atlassian.jira.util.JiraUtils" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    <title><ww:text name="'createissue.cant.browse.converted.issue.title'"/></title>
    <meta name="decorator" content="notitle">
</head>
<body>
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'createissue.cant.browse.converted.issue.title'"/></page:param>

        <ww:text name="'createissue.cant.browse.converted.issue.description'">
            <ww:param name="'value0'"><ww:property value="/issueKey"/></ww:param>
        </ww:text>

        <ww:if test="remoteUser == null">
            <p>
                <ww:text name="'login.required.desc2'">
                    <ww:param name="'value0'"><jira:loginlink><ww:text name="'common.words.login'"/></jira:loginlink></ww:param>
                </ww:text>
                <ww:if test="/allowSignUp == true">
                    <ww:text name="'login.required.desc3'">
                        <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/Signup!default.jspa"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </ww:if>.
            </p>
        </ww:if>

        <p>
            <ww:text name="'contact.admin.for.perm'">
                <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
            </ww:text>
        </p>
    </page:applyDecorator>
</body>
</html>
