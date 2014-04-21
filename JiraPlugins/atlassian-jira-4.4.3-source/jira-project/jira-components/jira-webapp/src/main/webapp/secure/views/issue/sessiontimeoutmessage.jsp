<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'session.timeout.message.title'"/></title>
    <meta content="message" name="decorator" />
</head>
<body>
<h1><ww:text name="'session.timeout.message.title'"/></h1>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">warning</aui:param>
    <aui:param name="'messageHtml'">
		<p>
            <ww:property value="/errorMessage"/>
        </p>
    </aui:param>
</aui:component>
</body>
</html>
