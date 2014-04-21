<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<ww:bean id="fieldVisibility" name="'com.atlassian.jira.web.bean.FieldVisibilityBean'" />
<html>
<head>
    <title>[#<ww:property value="/issueObject/key" />] <ww:property value="/issueObject/summary" /></title>
    <meta name="decorator" content="message" />
</head>
<body>
    <div class="form-body">
        <header>
            <%-- TODO: SEAN no i18n for the below string --%>
            <h1>Update fields for issue '<ww:property value="/issueObject/summary" />' failed</h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'titleText'">Unable to update fields. Return to your form and fix up the problem.</aui:param>
            <aui:param name="'messageHtml'">
                <ww:if test="/errors && /errors/size() > 0">
                    <p>Validation failed for the fields below:</p>
                    <ul>
                        <ww:iterator value="/errors" status="'status'">
                        <li>For field <strong><ww:property value="/field(./key)/name" /></strong>: <ww:property value="./value" /><li>
                        </ww:iterator>
                    </ul>
                </ww:if>
            </aui:param>
        </aui:component>
    </div>
</body>
</html>
