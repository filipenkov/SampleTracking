<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="issueValid == true && ableToComment == 'true'"><meta content="issueaction" name="decorator" /></ww:if>
    <ww:else><meta content="message" name="decorator" /></ww:else>
    <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponent(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
    <title><ww:text name="'comment.add.title'"/>: <ww:property value="summary" /> <ww:if test="issueValid == 'true'">[<ww:property value="issue/string('key')" />]</ww:if></title>
</head>
<body class="type-a">
<ww:if test="issueValid == true && ableToComment == true">
    <div class="content intform">
        <page:applyDecorator id="comment-add" name="auiform">
            <page:param name="action">AddComment.jspa</page:param>
            <page:param name="showHint">true</page:param>
            <ww:property value="/hint('comment')">
                <ww:if test=". != null">
                    <page:param name="hint"><ww:property value="./text" escape="false" /></page:param>
                    <page:param name="hintTooltip"><ww:property value="./tooltip" escape="false" /></page:param>
                </ww:if>
            </ww:property>
            <page:param name="submitButtonName">Add</page:param>
            <page:param name="submitButtonText"><ww:text name="'comment.add.submitname'"/></page:param>
            <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false"/></page:param>

            <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
                <aui:param name="'title'"><ww:text name="'comment.add.title'"/></aui:param>
                <aui:param name="'subtaskTitle'"><ww:text name="'comment.add.title.subtask'"/></aui:param>
                <aui:param name="'issueKey'"><ww:property value="/issueObject/key" escape="false"/></aui:param>
                <aui:param name="'issueSummary'"><ww:property value="/issueObject/summary" escape="false"/></aui:param>
                <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
                <aui:param name="'cameFromParent'" value="/cameFromParent"/>
            </aui:component>

            <aui:component name="'id'" template="hidden.jsp" theme="'aui'"  />

            <page:applyDecorator name="auifieldset">
                <page:param name="legend"><ww:text name="'comment.details.legend'" /></page:param>

                <ww:property value="/fieldScreenRendererLayoutItemForField(/field('comment'))/fieldLayoutItem/orderableField/createHtml(/fieldScreenRendererLayoutItemForField(/field('comment'))/fieldLayoutItem, /, /, /issueObject, /displayParams)" escape="'false'" />

            </page:applyDecorator>

        </page:applyDecorator>
    </div>
</ww:if>
<ww:else>
    <page:applyDecorator name="auiissueerrorpanel">
        <page:param name="title"><ww:text name="'comment.add.title'"/></page:param>
    </page:applyDecorator>
</ww:else>
</body>
</html>
