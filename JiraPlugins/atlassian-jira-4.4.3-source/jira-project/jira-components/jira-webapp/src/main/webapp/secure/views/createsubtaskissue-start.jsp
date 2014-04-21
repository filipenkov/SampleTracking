<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="/parentIssueKey == null || allowedProjects/size < 1"><meta content="message" name="decorator" /></ww:if>
    <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/parentIssuePath" atltoken="false"/>" />
    <title><ww:text name="'createsubtaskissue.title'"/></title>
</head>
<body class="type-a">                
<ww:if test="/parentIssueKey">
    <ww:if test="allowedProjects/size > 0">
        <div class="content intform">
            <page:applyDecorator id="subtask-create-start" name="auiform">
                <page:param name="action">CreateSubTaskIssue.jspa</page:param>
                <page:param name="submitButtonName">Create</page:param>
                <page:param name="submitButtonText"><ww:text name="'common.forms.next'" /></page:param>
                <page:param name="cancelLinkURI"><ww:url value="/parentIssuePath" atltoken="false"/></page:param>

                <aui:component template="formHeading.jsp" theme="'aui'">
                    <aui:param name="'text'"><ww:text name="'createsubtaskissue.title'"/></aui:param>
                </aui:component>

                <aui:component id="'project'" name="'pid'" template="hidden.jsp" theme="'aui'" />
                <aui:component name="'parentIssueId'" template="hidden.jsp" theme="'aui'" />

                <ww:property value="/field('issuetype')/createHtml(null, /, /, /issueObject, /displayParams)" escape="'false'" />

            </page:applyDecorator>
        </div>
    </ww:if>
    <ww:else>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <%@ include file="/includes/noprojects.jsp" %>
            </aui:param>
        </aui:component>
    </ww:else>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'issue.service.issue.wasdeleted'"/></p>
        </aui:param>
    </aui:component>
</ww:else>
</body>
</html>
