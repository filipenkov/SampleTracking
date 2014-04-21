<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'setup.already.title'"/></title>
    <meta content="message" name="decorator" />
</head>

<body>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'setup.already.error.line1'"/>
            </p>
            <p>
                <ww:text name="'setup.already.error.line2'">
                    <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>

</body>
</html>
