<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:if test="subTask == true"><ww:text name="'movesubtask.title'"/></ww:if><ww:else><ww:text name="'moveissue.title'"/></ww:else>: <ww:property value="issue/string('summary')" /></title>
    <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false"/>" />
</head>
<body>
    <header>
        <h1><ww:text name="'moveissue.title'"/></h1>
    </header>
    <div class="content-container" id="stepped-process">
        <div class="content-related">
            <ww:if test="subTask == true">
                <jsp:include page="/secure/views/issue/movetaskpane.jsp" flush="false" />
            </ww:if>
            <ww:else>
                <jsp:include page="/secure/views/issue/moveissuepane.jsp" flush="false" />
            </ww:else>
        </div>
        <div class="content-body aui-panel">
            <ww:if test="targetWorkflow != null">
                <page:applyDecorator name="jiraform">
                    <page:param name="title">
                        <ww:if test="subTask == true">
                            <ww:text name="'movesubtask.title'"/>: <ww:text name="'moveissue.select.status'"/>
                        </ww:if>
                        <ww:else>
                            <ww:text name="'moveissue.title'"/>: <ww:text name="'moveissue.select.status'"/>
                        </ww:else>
                    </page:param>
                    <page:param name="description">
                        <%-- Set status of issue only - no subtasks or valid subtask statuses --%>
                        <ww:if test="issueStatusValid == false && hasSubTasks == false || taskStatusChangeRequired == false">
                            <ww:text name="'moveissue.updateworkflow.desc.ent.issueonly'"/>
                        </ww:if>
                        <%-- Set status of subtasks only - valid issue status--%>
                        <ww:elseIf test="issueStatusValid == true && hasSubTasks == true && taskStatusChangeRequired == true">
                            <ww:text name="'moveissue.updateworkflow.desc.ent.subtaskonly'"/>
                        </ww:elseIf>
                        <%-- Set status of issue and status of subtasks --%>
                        <ww:elseIf test="issueStatusValid == false && hasSubTasks == true && taskStatusChangeRequired == true">
                            <ww:text name="'moveissue.updateworkflow.desc.ent.both'"/>
                        </ww:elseIf>
                        <p>
                        <span class="red-highlight"><b><ww:text name="'common.words.note'"/></b></span>:&nbsp;<ww:text name="'moveissue.status.invalid'"/>.
                        </p>
                    </page:param>
                    <page:param name="columns">1</page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="action">MoveIssueUpdateWorkflow.jspa</page:param>
                    <page:param name="autoSelectFirst">false</page:param>
                    <page:param name="cancelURI"><ww:url value="/issuePath" atltoken="false" /></page:param>
                    <page:param name="submitId">next_submit</page:param>
                    <page:param name="submitName"><ww:text name="'common.forms.next'"/> &gt;&gt;</page:param>

                    <tr>
                        <td colspan="2">
                            <ww:if test="issueStatusValid == false">
                            <table class="aui">
                                <tbody>
                                    <tr class="totals">
                                        <td colspan="5"><ww:text name="'moveissue.currentissue'"/>
                                        <%-- Current Issue Workflow --%>
                                        <span class="secondary-text">(<b><ww:text name="'moveissue.workflow'"/></b>:&nbsp;<ww:property value="currentWorkflow/name"/>&nbsp;<img src="<%= request.getContextPath() %>/images/icons/arrow_right_small_fade.gif" height=16 width=16 border=0 align=absmiddle>&nbsp;
                                        <ww:property value="targetWorkflow/name"/>)</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <%-- Current Issue Status --%>
                                        <td width="20%" class="nowrap"><b><ww:text name="'moveissue.currentstatus'"/></b>:</td>
                                        <td class="nowrap">
                                            <ww:component name="'status'" template="constanticon.jsp">
                                                <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                                <ww:param name="'iconurl'" value="currentStatusGV/string('iconurl')" />
                                                <ww:param name="'alt'"><ww:property value="/nameTranslation(currentStatusGV)" /></ww:param>
                                                <ww:param name="'title'"><ww:property value="/nameTranslation(currentStatusGV)" /> - <ww:property value="/descTranslation(currentStatusGV)" /></ww:param>
                                            </ww:component>
                                            <ww:property value="/nameTranslation(currentStatusGV)"/>
                                        </td>
                                        <td width="1%" class="nowrap"><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" /></td>
                                        <%-- Target Status --%>
                                        <td width="20%" class="nowrap"><b><ww:text name="'moveissue.newstatus'"/></b>:</td>
                                        <td>
                                            <select name="beanTargetStatusId">
                                                <ww:iterator value="targetWorkflowStatuses(/moveIssueBean/targetIssueType)">
                                                    <option value="<ww:property value="./string('id')" />" <ww:if test="beanTargetStatusId == ./string('id')">selected</ww:if> >
                                                        <ww:property value="/nameTranslation(.)" />
                                                    </option>
                                                </ww:iterator>
                                            </select>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                            </ww:if>
                            <ww:else>
                                <ui:component name="'beanTargetStatusId'" value="currentStatusGV/string('id')" template="hidden.jsp" />
                            </ww:else>

                            <%-- SubTasks with invalid statuses --%>
                            <ww:if test="hasSubTasks == true && taskStatusChangeRequired == true">
                                <table class="aui aui-table-rowhover">
                                    <thead>
                                        <tr>
                                            <th colspan="5"><ww:text name="'moveissue.subtasks'"/></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                    <ww:iterator value="taskInvalidTypes">
                                        <tr class="totals">
                                            <td colspan="5"><b>Type: <ww:property value="/nameTranslation(.)"/></b>
                                            <%-- Current Task Workflow --%>
                                            <span class="secondary-text">(<b><ww:text name="'moveissue.workflow'"/></b>:&nbsp;<ww:property value="taskCurrentWorkflow(.)/name"/>&nbsp;<img src="<%= request.getContextPath() %>/images/icons/arrow_right_small_fade.gif" height="16" width="16" border="0" />&nbsp;
                                            <ww:property value="subtaskTargetWorkflow(./string('id'))/name"/>)</span>
                                            </td>
                                        </tr>
                                        <ww:iterator value="../taskInvalidStatuses(./string('id'))">
                                        <tr>
                                            <td width="20%" class="nowrap"><ww:property value="tasksByStatusWorkflowType(./string('id'), taskCurrentWorkflow(..), ../string('id'))/size">
                                                    <ww:property value="."/><ww:if test=". == 1"> <ww:text name="'moveissue.taskwithstatus'"/>:</ww:if>
                                                    <ww:else><ww:text name="'moveissue.taskswithstatus'"/>:</ww:else>
                                                </ww:property>
                                            </td>
                                            <td class="nowrap">
                                            <ww:component name="'status'" template="constanticon.jsp">
                                                <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                                <ww:param name="'iconurl'" value="./string('iconurl')" />
                                                <ww:param name="'alt'"><ww:property value="/nameTranslation(.)" /></ww:param>
                                                <ww:param name="'title'"><ww:property value="/nameTranslation(.)" /> - <ww:property value="/descTranslation(.)" /></ww:param>
                                                </ww:component>
                                            <ww:property value="/nameTranslation(.)"/>
                                            </td>
                                            <td width="1%" class="nowrap"><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" /></td>
                                            <td width="20%" class="nowrap"><b><ww:text name="'moveissue.newstatus'"/></b>:</td>
                                            <td>
                                                <%-- Selection is given name with task type id and status id in order to retrieve it from params later --%>
                                                <select name="<ww:property value=".././prefixTaskStatusId(../string('id'), ./string('id'))"/>" >
                                                    <ww:iterator value="targetWorkflowStatuses(../subtaskTargetIssueType(../string('id')))">
                                                        <option value="<ww:property value="./string('id')" />"> <ww:property value="/nameTranslation(.)" />
                                                        </option>
                                                    </ww:iterator>
                                                </select>
                                            </td>
                                        </tr>
                                        </ww:iterator>
                                    </ww:iterator>
                                    </tbody>
                                </table>
                            </ww:if>
                        </td>
                    </tr>

                    <%-- Do not put these in the MoveIssueBean --%>
                    <ui:component name="'id'" template="hidden.jsp" />
                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'moveissue.updateworkflow.desc.ent.error'"/></p>
                    </aui:param>
                </aui:component>
            </ww:else>
        </div>
    </div>
</body>
</html>
