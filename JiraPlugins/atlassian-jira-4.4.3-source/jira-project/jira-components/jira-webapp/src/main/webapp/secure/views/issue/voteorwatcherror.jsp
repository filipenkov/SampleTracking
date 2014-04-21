<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    <meta content="message" name="decorator" />
</head>
<body>
    <h1><ww:text name="'common.words.error'" /></h1>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <ww:if test="hasErrorMessages == 'true'">
                <ww:iterator value="flushedErrorMessages">
                    <p><ww:property value="." /></p>
                </ww:iterator>
            </ww:if>
        </aui:param>
    </aui:component>
</body>
</html>
