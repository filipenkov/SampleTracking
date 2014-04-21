<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'viewissue.commentdelete.title'"/></title>
    <meta content="message" name="decorator" />
</head>
<body>
<ww:if test="/issueValid == true">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'comment.service.error.no.comment.visibility.no.user'"/></p>
        </aui:param>
    </aui:component>
</ww:if>
<ww:elseIf test="/hasErrorMessages == true">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <ww:iterator value="flushedErrorMessages">
                <p><ww:property /></p>
            </ww:iterator>
        </aui:param>
    </aui:component>
</ww:elseIf>
</body>
</html>
