<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/parentIssuePath" atltoken="false"/>" />
	<title><ww:text name="'createsubtaskissue.title'"/></title>
    <ww:if test="/requiresLogin && /requiresLogin == true">
        <meta content="message" name="decorator"/>
    </ww:if>
</head>
<body class="type-a">
<ww:if test="!/requiresLogin || /requiresLogin == false">
    <div class="content intform">
        <page:applyDecorator id="subtask-create-details" name="auiform">
            <page:param name="action">CreateSubTaskIssueDetails.jspa</page:param>
            <page:param name="submitButtonName">Create</page:param>
            <page:param name="submitButtonText"><ww:text name="'common.forms.create'" /></page:param>
            <page:param name="cancelLinkURI"><ww:url value="/parentIssuePath" atltoken="false"/></page:param>
            <page:param name="isMultipart">true</page:param>

            <aui:component template="formHeading.jsp" theme="'aui'">
                <aui:param name="'text'"><ww:text name="'createsubtaskissue.title'"/></aui:param>
            </aui:component>

            <page:applyDecorator name="auifieldgroup">
                <aui:component id="'project-name'" label="text('issue.field.project')" name="'project/string('name')'" template="formFieldValue.jsp" theme="'aui'" />
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <aui:component id="'issue-type'" label="text('issue.field.issuetype')" name="'issueType'" template="formIssueType.jsp" theme="'aui'">
                    <aui:param name="'issueType'" value="/constantsManager/issueType(issuetype)" />
                </aui:component>
            </page:applyDecorator>

            <ww:component template="issuefields.jsp" name="'createissue'">
                <ww:param name="'displayParams'" value="/displayParams"/>
                <ww:param name="'issue'" value="/issueObject"/>
                <ww:param name="'tabs'" value="/fieldScreenRenderTabs"/>
                <ww:param name="'errortabs'" value="/tabsWithErrors"/>
                <ww:param name="'selectedtab'" value="/selectedTab"/>
                <ww:param name="'ignorefields'" value="/ignoreFieldIds"/>
                <ww:param name="'create'" value="'true'"/>
            </ww:component>

            <aui:component name="'issuetype'" template="hidden.jsp" theme="'aui'"  />
            <aui:component name="'viewIssueKey'" template="hidden.jsp" theme="'aui'"  />
            <aui:component name="'pid'" template="hidden.jsp" theme="'aui'" />
            <aui:component name="'parentIssueId'" template="hidden.jsp" theme="'aui'" />
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
