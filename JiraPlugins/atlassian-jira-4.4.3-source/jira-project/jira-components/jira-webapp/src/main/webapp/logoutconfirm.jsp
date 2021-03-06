<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <meta content="message" name="decorator"/>
    <title><ww:text name="'logout.confirm.title'"/></title>
</head>
<body>
    <h1><ww:text name="'logout.confirm.title'"/></h1>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'logout.confirm.desc'"/></p>
            <form id="confirm-logout" method="post" action="<ww:url value="'/secure/Logout.jspa'"/>" style="margin: 1em 0 0 0;">
                <aui:component name="'confirm'" template="hidden.jsp" theme="'aui'" value="'true'" />
                <input id="confirm-logout-submit" name="Logout" type="submit" value="<ww:text name="'common.concepts.logout'"/>" />
            </form>
        </aui:param>
    </aui:component>
</body>
</html>