<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta content="message" name="decorator" />
	<title><ww:text name="'commonfilter.error.fatal.title'"/></title>
</head>
<body>
    <h1><ww:text name="'commonfilter.error.fatal.title'"/></h1>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <ww:iterator value="flushedErrorMessages">
                <p><ww:property value="." /></p>
            </ww:iterator>
        </aui:param>
    </aui:component>
</body>
</html>