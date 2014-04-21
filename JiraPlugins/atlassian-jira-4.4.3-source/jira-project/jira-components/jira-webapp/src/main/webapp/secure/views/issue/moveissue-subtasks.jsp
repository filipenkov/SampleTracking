<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'moveissue.title'"/>: <ww:property value="issue/string('key')"/></title>
    <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
</head>

<body class="nl">
<div id="stepped-process">
    <div class="steps-wrap">
        <div class="steps-container">
            <jsp:include page="/secure/views/issue/moveissuepane.jsp" flush="false"/>
        </div>
        <div class="current-step">
        <page:applyDecorator name="jiraform">
            <page:param name="title">
                <ww:text name="'moveissue.subtasks.issuetypes.title'"/>
            </page:param>
            <page:param name="description">
                <ww:text name="'moveissue.subtasks.issuetypes.desc'"/>
            </page:param>
            <page:param name="columns">1</page:param>
            <page:param name="width">100%</page:param>
            <page:param name="action">MoveIssueSubtasks.jspa</page:param>
            <page:param name="autoSelectFirst">false</page:param>
            <page:param name="cancelURI"><ww:url value="/issuePath" atltoken="false"/></page:param>
            <page:param name="submitId">next_submit</page:param>
            <page:param name="submitName"><ww:text name="'common.forms.next'"/> &gt;&gt;</page:param>
            <tr>
                <td>
                    <table id="issuetypechoices" class="aui">
                        <thead>
                            <th colspan="5"><ww:text name="'moveissue.subtask.choose.issuetypes'"/></th>
                        </thead>
                        <tbody>
                        <ww:iterator value="/migrateIssueTypes">
                            <tr>
                                <%-- Select Issue Type --%>
                                <td class="noWrap" width="20%"><strong><ww:text name="'moveissue.currentissuetype'"/></strong>:</td>
                                <td class="noWrap"><ww:property value="./name"/></td>
                                <td class="noWrap"><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0"></td>
                                    <%-- Target Issue Type --%>
                                <td class="noWrap" width="20%"><strong><ww:text name="'moveissue.newissuetype'"/></strong>:</td>
                                <td class="noWrap"><select name="<ww:property value="/prefixIssueTypeId(./id)"/>"><ww:iterator value="/projectSubtaskIssueTypes"><option value="<ww:property value="./id"/>"><ww:property value="./name"/></option></ww:iterator></select></td>
                            </tr>
                        </ww:iterator>
                        </tbody>
                    </table>
                </td>
            </tr>
            <ui:component name="'id'" template="hidden.jsp" theme="'single'"/>
        </page:applyDecorator>
        </div>
    </div>
</div>
</body>
</html>
