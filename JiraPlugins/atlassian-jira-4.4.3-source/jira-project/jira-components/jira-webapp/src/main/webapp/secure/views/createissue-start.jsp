<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="allowedProjects/size < 1"><meta content="message" name="decorator" /></ww:if>
    <title><ww:text name="'createissue.title'"/></title>
    <content tag="section">find_link</content>
</head>
<body class="type-a">
<ww:if test="allowedProjects/size > 0">
    <div class="content intform">
        <page:applyDecorator id="issue-create" name="auiform">
            <page:param name="action">CreateIssue.jspa</page:param>
            <page:param name="submitButtonName">Next</page:param>
            <page:param name="submitButtonText"><ww:text name="'common.forms.next'" /></page:param>
            <page:param name="cancelLinkURI"><ww:url value="'default.jsp'" atltoken="false"/></page:param>

            <aui:component template="formHeading.jsp" theme="'aui'">
                <aui:param name="'text'"><ww:text name="'createissue.title'"/></aui:param>
            </aui:component>

            <ww:property value="/field('project')/createHtml(null, /, /, /issueObject, /displayParams)" escape="'false'" />

            <ww:property value="/field('issuetype')/createHtml(null, /, /, /issueObject, /displayParams)" escape="'false'" />

        </page:applyDecorator>
    </div>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <%@ include file="/includes/createissue-notloggedin.jsp" %>
        </aui:param>
    </aui:component>
</ww:else>
</body>
</html>
