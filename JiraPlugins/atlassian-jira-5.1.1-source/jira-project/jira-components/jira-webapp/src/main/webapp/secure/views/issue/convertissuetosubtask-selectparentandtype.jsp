<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%
    KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
    keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
%>
<html>
<head>
    <title><ww:text name="textKey('title')"/>: <ww:property value="issue/key" /></title>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false"/>" />
</head>
<body>
    <div class="content-container" id="stepped-process">
        <div class="content-related">
            <jsp:include page="/secure/views/issue/convertissuetosubtaskpane.jsp" flush="false" />
        </div>
        <div class="content-body aui-panel">
            <ww:if test="errorMessages/empty == true">
                <page:applyDecorator name="jiraform">
                    <page:param name="title"><ww:text name="textKey('title')"/>: <ww:property value="issue/key" /></page:param>
                    <page:param name="description">
                        <ww:text name="textKey('step1.desc.ent')">
                            <ww:param name="value0"><strong></ww:param>
                            <ww:param name="value1"></strong></ww:param>
                        </ww:text>
                    </page:param>
                    <page:param name="columns">1</page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="action"><ww:property value="/actionPrefix" />SetIssueType.jspa</page:param>
                    <page:param name="autoSelectFirst">false</page:param>
                    <page:param name="cancelURI"><%= request.getContextPath() %>/secure/<ww:property value="/actionPrefix"/>!cancel.jspa?id=<ww:property value="issue/id" /></page:param>
                    <page:param name="submitId">next_submit</page:param>
                    <page:param name="submitName"><ww:text name="'common.forms.next'"/> &gt;&gt;</page:param>

                    <tr>
                        <td>
                            <table width="100%" class="aui wizardTable">
                                <tbody>
                                <ww:if test="/issue/subTask == false">
                                    <%-- select parent issue, Only do if the current issue is not a subtask--%>
                                    <tr>
                                        <td width="20%">
                                            <strong><ww:text name="textKey('selectparentissue')"/>:</strong>
                                        </td>
                                        <ui:component label="text('selectparentissue')" name="'parentIssueKey'" template="issuepicker.jsp" theme="'single'">
                                            <ui:param name="'size'" value="18"/>
                                            <ui:param name="'currentIssue'" value="issue/key" />
                                            <ui:param name="'showSubTasks'">false</ui:param>
                                            <ui:param name="'singleSelectOnly'">true</ui:param>
                                            <ui:param name="'sameProjectMessage'">true</ui:param>
                                            <ui:param name="'selectedProjectKey'" value="issue/projectObject/key" />
                                            <ui:param name="'selectedProjectId'" value="issue/projectObject/id" />
                                            <ui:param name="'currentJQL'" value="/currentJQL" />
                                        </ui:component>
                                    </tr>
                                </ww:if>
                                    <tr>
                                        <td width="20%">
                                            <strong><ww:text name="textKey('selectissuetype')"/>:</strong>
                                        </td>
                                        <%-- Current Issue Type --%>
                                        <td>
                                            <table width="100%">
                                                <tbody>
                                                <tr>
                                                    <td class="nowrap" width="20%">
                                                        <ww:text name="textKey('selectissuetype.current')"/>: <strong><ww:property value="./issue/issueTypeObject/name"/></strong>
                                                    </td>
                                                    <td width="1%">
                                                        <img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" alt=""/>
                                                    </td>
                                                    <%-- Target Issue Type --%>
                                                    <td class="nowrap" width="10%"><ww:text name="textKey('selectissuetype.target')"/>:</td>
                                                    <td>
                                                        <table>
                                                            <tbody>
                                                            <tr>
                                                                <ui:select label="" name="'issuetype'" list="./availableIssueTypes" value="./issuetype" listKey="'id'"  listValue="'nameTranslation(../..)'" theme="'single'">
                                                                    <ui:param name="'mandatory'" value="true"/>
                                                                </ui:select>
                                                                <td>
                                                                    <a href="<%= request.getContextPath() %>/secure/ShowConstantsHelp.jspa?decorator=popup#IssueTypes" onclick="var child = window.open('<%= request.getContextPath() %>/secure/ShowConstantsHelp.jspa?decorator=popup#IssueTypes', 'jiraLocalHelp', 'width=600, height=500, resizable, scrollbars=yes'); child.focus(); return false;"><img src="<%= request.getContextPath() %>/images/icons/ico_help.png" title="<ww:text name="'online.help'"/> <ww:text name="'issue.field.issuetype'"/> " /></a>
                                                                </td>
                                                            </tr>
                                                            </tbody>
                                                        </table>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>

                    <ui:component name="'id'" template="hidden.jsp"  theme="'single'" />
                    <ui:component name="'guid'" template="hidden.jsp"  theme="'single'" />
                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <%-- display error message --%>
                <page:applyDecorator name="jiraform" >
                    <%-- Must have body, else NullPointer is thrown --%>
                </page:applyDecorator>
            </ww:else>
        </div>
    </div>
</body>
</html>
