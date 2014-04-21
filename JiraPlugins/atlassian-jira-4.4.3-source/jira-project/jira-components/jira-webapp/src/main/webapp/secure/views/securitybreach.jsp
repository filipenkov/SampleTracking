<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>

<html>
<head>
	<title><ww:text name="'access.denied.title'"/></title>
    <meta content="message" name="decorator" />
</head>
<body>
    <h1><ww:text name="'access.denied.title'"/></h1>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'perm.violation.desc'"/></p>
            <p>
                <ww:text name="'contact.admin.for.perm'">
                    <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>
</body>
</html>
