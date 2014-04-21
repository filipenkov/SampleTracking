<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <ww:if test="/issueValid == true"><meta content="issueaction" name="decorator" /></ww:if>
    <ww:else><meta content="message" name="decorator" /></ww:else>
     <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
	<title><ww:text name="'viewissue.delete.link.title'"/> <ww:property value="del" /></title>
</head>
<body class="type-a">
<ww:if test="/issueValid == true && /hasIssuePermission('link', /issue) == true ">
    <div class="content intform">
        <page:applyDecorator id="issue-link-delete" name="auiform">
            <page:param name="action"><ww:url page="DeleteLink.jspa"><ww:param name="'id'" value="id"/><ww:param name="'destId'" value="destId"/><ww:param name="'sourceId'" value="sourceId"/><ww:param name="'linkType'" value="linkType"/></ww:url></page:param>
            <page:param name="submitButtonName">Delete</page:param>
            <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
            <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false"/></page:param>


            <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
                <aui:param name="'title'"><ww:text name="'viewissue.delete.link.title'"/>: <ww:property value="/issue/string('key')" escape="false"/> <ww:property value="directionName" escape="false" /> <ww:property value="targetIssueKey" escape="false"/></aui:param>
                <aui:param name="'issueKey'"><ww:property value="/issueObject/key" escape="false"/></aui:param>
                <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
                <aui:param name="'cameFromParent'" value="/cameFromParent"/>
            </aui:component>

            <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'viewissue.delete.link.msg'"/></p>
                </aui:param>
            </aui:component>

            <aui:component name="'confirm'" value="true" template="hidden.jsp" theme="'aui'"  />
        </page:applyDecorator>
    </div>
</ww:if>
<ww:else>
    <page:applyDecorator name="auiissueerrorpanel">
        <page:param name="title"><ww:text name="'viewissue.delete.link.title'"/></page:param>
    </page:applyDecorator>
</ww:else>
</body>
</html>