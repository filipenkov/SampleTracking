<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta content="message" name="decorator" />
	<title><ww:text name="'invalid.workflow.action.title'"/></title>
</head>
<body>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'titleText'"><ww:text name="'invalid.workflow.action.title'" /></aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'invalid.workflow.action.desc'" >
                    <ww:param name="'value0'"><a id="refreshIssue" href="<%=request.getContextPath()%>/browse/<ww:property value="issueObject/key" />"><ww:text name="'invalid.workflow.action.link.text'"/></a></ww:param>
                    <ww:param name="'value1'"><ww:property value="issueObject/statusObject/nameTranslation" /></ww:param>
                    <ww:param name="'value2'"><ww:property value="workflowTransitionDisplayName" /></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>
</body>
</html>
