<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<html>
<head>
    <meta content="message" name="decorator" />
    <title><ww:text name="'admin.common.words.error'"/></title>
</head>
<body>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">error</aui:param>
    <aui:param name="'titleText'"><ww:text name="'admin.common.words.error'"/></aui:param>
    <aui:param name="'messageHtml'">
        <ww:if test="hasErrorMessages == 'true'">
            <ul>
                <ww:iterator value="flushedErrorMessages">
                    <li><ww:property value="." /></li>
                </ww:iterator>
            </ul>
        </ww:if>
    </aui:param>
</aui:component>
</body>
</html>