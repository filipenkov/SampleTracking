<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="jiratags" prefix="jira" %>

<html>
<head>
	<title><ww:text name="'webfragments.user.profile.links.item.view.profile'"/></title>
    <meta content="message" name="decorator"/>
</head>
<body>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">error</aui:param>
    <aui:param name="'messageHtml'">
        <p><ww:text name="'user.profile.no.user'"/></p>
    </aui:param>
</aui:component>
</body>
</html>

