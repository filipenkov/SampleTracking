<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
    <jira:web-resource-require modules="jira.webresources:workflow-administration"/>
	<title><ww:text name="'admin.workflows.view.workflows'"/></title>
</head>
<body>
    <header>
        <nav class="aui-toolbar">
            <div class="toolbar-split toolbar-split-right">
                <ul class="toolbar-group">
                    <li class="toolbar-item">
                        <a id="add-workflow" class="toolbar-trigger trigger-dialog" href="AddNewWorkflow.jspa">
                            <span class="icon icon-add16"></span>
                            <ww:text name="'admin.workflows.add.new.workflow'"/>
                        </a>
                    </li>
                    <ww:if test="/systemAdministrator == true">
                        <li class="toolbar-item">
                            <a id="import-workflow" class="toolbar-trigger trigger-dialog" href="ImportWorkflowFromXml!default.jspa">
                                <ww:text name="'admin.workflows.import.from.xml'"/>
                            </a>
                        </li>
                    </ww:if>
                    <li class="toolbar-item">
                        <aui:component name="'workflow'" template="toolbarHelp.jsp" theme="'aui'">
                            <aui:param name="'cssClass'">toolbar-trigger</aui:param>
                        </aui:component>
                    </li>
                </ul>
            </div>
        </nav>
        <h2><ww:text name="'admin.workflows.view.workflows'"/></h2>
    </header>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">ViewWorkflows</aui:param>
        <aui:param name="'contentHtml'">
            <ww:property value="/flushedErrorMessages">
                <ww:if test=". && ./empty == false">
                    <aui:component template="auimessage.jsp" theme="'aui'">
                        <aui:param name="'messageType'">error</aui:param>
                        <aui:param name="'messageHtml'">
                            <ww:iterator value=".">
                                <p><ww:property value="."/></p>
                            </ww:iterator>
                        </aui:param>
                    </aui:component>
                </ww:if>
            </ww:property>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <ww:text name="'admin.workflows.delete.workflow.instruction'"/>
                </aui:param>
            </aui:component>
            <div id="active-workflows-module" class="module toggle-wrap">
                <div id="active-workflows-module-heading" class="mod-header">
                    <ul class="ops"></ul>
                    <h3 class="toggle-title"><ww:text name="'admin.common.words.active'"/></h3></div>
                <div class="mod-content">
                    <div id="active-workflows-val">
                        <ww:if test="activeWorkflows/empty == true">
                        <p><ww:text name="'admin.workflows.no.active'"/></p>
                        </ww:if>
                        <ww:else>
                        <table id="active-workflows-table" class="aui aui-table-rowhover list-workflow-table">
                            <thead>
                            <tr>
                                <th class="workflow-name"><ww:text name="'common.words.name'"/></th>
                                <th class="workflow-last-modified"><ww:text name="'admin.workflows.last.modified'"/></th>
                                <th class="workflow-schemes"><ww:text name="'admin.menu.schemes.assigned.schemes'"/></th>
                                <th class="workflow-steps"><ww:text name="'admin.workflows.steps'"/></th>
                                <th class="workflow-operations"><ww:text name="'common.words.operations'"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <ww:iterator value="activeWorkflows" status="'status'">
                                <tr data-workflow-name="<ww:property value="name"/>">
                                <td class="workflow-name cell-type-value" data-cell-type="name">
                                    <%--<ww:if test="./draftWorkflow == true">--%>
                                        <%--<img src="<%= request.getContextPath()%>/images/icons/draft-workflow-information.png" --%>
                                    <%--</ww:if>--%>
                                    <strong><ww:property value="name"/></strong>
                                    <ww:if test="./systemWorkflow == true">
                                        <ww:text name="'admin.workflows.readonly.system.workflow'"/>
                                    </ww:if>
                                    <ww:if test="default == true">
                                        <span class="status-lozenge status-default" title="<ww:text name="'admin.workflows.default.workflow.description'"/>"><ww:text name="'common.words.default'"/></span>
                                    </ww:if>
                                    <ww:if test="./hasDraftWorkflow == true">
                                        <span>
                                            <img src="<%= request.getContextPath()%>/images/icons/draft-workflow-information.png"
                                                 title="<ww:text name="'admin.workflows.draft.information.tooltip'">
                                                            <ww:param name="'value0'" value="/draftFor(./name)/updateAuthorName"/>
                                                            <ww:param name="'value1'" value="/lastModifiedDateForDraft(.)"/>
                                                        </ww:text>"/>
                                        </span>
                                    </ww:if>
                                    <div class="secondary-text">
                                        <ww:property value="description"/>
                                    </div>
                                </td>
                                <td class="workflow-last-modified cell-type-value" data-cell-type="last-modified">
                                    <ww:if test="./updateAuthorName != null && ./updatedDate != null">
                                        <div class="secondary-text">
                                            <strong><ww:property value="/outlookDate/formatDMY(./updatedDate)"/></strong>
                                            <br/>
                                            <ww:if test="./updateAuthorName == ''">
                                                <ww:text name="'admin.workflows.last.modified.anonymous'"/>
                                            </ww:if>
                                            <ww:else>
                                                <ww:property value="/userFullName(./updateAuthorName)"/>
                                            </ww:else>
                                        </div>
                                    </ww:if>
                                </td>
                                <td class="workflow-schemes cell-type-value" data-cell-type="schemes">
                                    <ww:property value="/schemesForWorkflow(.)" id="workflowSchemes"/>
                                    <ww:if test="@workflowSchemes && @workflowSchemes/empty == false">
                                        <ul>
                                            <ww:iterator value="@workflowSchemes">
                                                <li>
                                                    <a href="<%= request.getContextPath() %>/secure/admin/EditWorkflowSchemeEntities!default.jspa?schemeId=<ww:property value="long('id')" />"><ww:property value="string('name')"/></a>
                                                </li>
                                            </ww:iterator>
                                        </ul>
                                    </ww:if>
                                </td>
                                <td class="workflow-steps" data-cell-type="steps">
                                    <ww:property value="descriptor/steps/size"/>
                                </td>
                                <td class="workflow-operations" data-cell-type="operations">
                                    <ul class="operations-list">
                                        <li>
                                            <a data-operation=view id="steps_<ww:property value="mode"/>_<ww:property value="name"/>" href="<ww:url page="ViewWorkflowSteps.jspa"><ww:param name="'workflowMode'" value="mode" /><ww:param name="'workflowName'" value="name" /></ww:url>"><ww:text name="'common.words.view'"/></a>
                                        </li>
                                        <ww:if test="./systemWorkflow == false">
                                            <li>
                                                <a data-operation="edit" id="edit_<ww:property value="mode"/>_<ww:property value="name"/>" href="<ww:url page="EditWorkflowDispatcher.jspa"><ww:param name="'wfName'" value="name" /></ww:url>"><ww:text name="'common.words.edit'"/></a>
                                            </li>
                                        </ww:if>
                                        <li>
                                            <a data-operation="copy" class="trigger-dialog" id="copy_<ww:property value="name"/>" href="<ww:url page="CloneWorkflow!default.jspa"><ww:param name="'workflowMode'" value="mode" /><ww:param name="'workflowName'" value="name" /></ww:url>"><ww:text name="'common.words.copy'"/></a>
                                        </li>
                                    </ul>
                                </td>
                                </tr>
                            </ww:iterator>
                            </tbody>
                        </table>
                        </ww:else>
                    </div>
                </div>
            </div>
            <div id="inactive-workflows-module" class="module toggle-wrap collapsed">
                <div id="inactive-workflows-module-heading" class="mod-header">
                    <ul class="ops"></ul>
                    <h3 class="toggle-title"><ww:text name="'admin.common.words.inactive'"/></h3></div>
                <div class="mod-content">
                    <div id="inactive-workflows-val">
                        <ww:if test="inactiveWorkflows/empty == true">
                        <p><ww:text name="'admin.workflows.no.inactive'"/></p>
                        </ww:if>
                        <ww:else>
                        <table id="inactive-workflows-table" class="aui aui-table-rowhover list-workflow-table">
                            <thead>
                            <tr>
                                <th class="workflow-name"><ww:text name="'common.words.name'"/></th>
                                <th class="workflow-last-modified"><ww:text name="'admin.workflows.last.modified'"/></th>
                                <th class="workflow-schemes"><ww:text name="'admin.menu.schemes.assigned.schemes'"/></th>
                                <th class="workflow-steps"><ww:text name="'admin.workflows.steps'"/></th>
                                <th class="workflow-operations"><ww:text name="'common.words.operations'"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <ww:iterator value="inactiveWorkflows" status="'status'">
                            <tr data-workflow-name="<ww:property value="name"/>">
                                <td class="workflow-name cell-type-value" data-cell-type="name">
                                    <strong><ww:property value="name"/></strong>
                                    <ww:if test="./systemWorkflow == true">
                                        <ww:text name="'admin.workflows.readonly.system.workflow'"/>
                                    </ww:if>
                                    <ww:if test="default == true">
                                        <span class="status-lozenge status-default" title="<ww:text name="'admin.workflows.default.workflow.description'"/>"><ww:text name="'common.words.default'"/></span>
                                    </ww:if>
                                    <ww:if test="./hasDraftWorkflow == true">
                                        <span>
                                            <img src="<%= request.getContextPath()%>/images/icons/draft-workflow-information.png"
                                                 title="<ww:text name="'admin.workflows.draft.information.tooltip'">
                                                            <ww:param name="'value0'" value="/draftFor(./name)/updateAuthorName"/>
                                                            <ww:param name="'value1'" value="/lastModifiedDateForDraft(.)"/>
                                                        </ww:text>"/>
                                        </span>
                                    </ww:if>
                                    <div class="secondary-text">
                                        <ww:property value="description"/>
                                    </div>
                                </td>
                                <td class="workflow-last-modified cell-type-value" data-cell-type="last-modified">
                                    <ww:if test="./updateAuthorName != null && ./updatedDate != null">
                                        <div class="secondary-text">
                                            <strong><ww:property value="/outlookDate/formatDMY(./updatedDate)"/></strong>
                                            <br/>
                                            <ww:if test="./updateAuthorName == ''">
                                                <ww:text name="'admin.workflows.last.modified.anonymous'"/>
                                            </ww:if>
                                            <ww:else>
                                                <ww:property value="/userFullName(./updateAuthorName)"/>
                                            </ww:else>
                                        </div>
                                    </ww:if>
                                </td>
                                <td class="workflow-schemes cell-type-value" data-cell-type="schemes">
                                    <ww:property value="/schemesForWorkflow(.)" id="workflowSchemes"/>
                                    <ww:if test="@workflowSchemes && @workflowSchemes/empty == false">
                                        <ul>
                                            <ww:iterator value="@workflowSchemes">
                                                <li><a href="<%= request.getContextPath() %>/secure/admin/EditWorkflowSchemeEntities!default.jspa?schemeId=<ww:property value="long('id')" />"><ww:property value="string('name')" /></a></li>
                                            </ww:iterator>
                                        </ul>
                                    </ww:if>
                                </td>
                                <td class="workflow-steps" data-cell-type="steps">
                                    <ww:property value="descriptor/steps/size" />
                                </td>
                                <td class="workflow-operations" data-cell-type="operations">
                                    <ul class="operations-list">
                                        <ww:if test="./systemWorkflow == true">
                                            <li>
                                                <a data-operation=view id="steps_<ww:property value="mode"/>_<ww:property value="name"/>" href="<ww:url page="ViewWorkflowSteps.jspa"><ww:param name="'workflowMode'" value="mode" /><ww:param name="'workflowName'" value="name" /></ww:url>"><ww:text name="'common.words.view'"/></a>
                                            </li>
                                        </ww:if>
                                        <ww:if test="./systemWorkflow == false">
                                            <li>
                                                <a data-operation="edit" id="edit_<ww:property value="mode"/>_<ww:property value="name"/>" href="<ww:url page="EditWorkflowDispatcher.jspa"><ww:param name="'wfName'" value="name" /></ww:url>"><ww:text name="'common.words.edit'"/></a>
                                            </li>
                                        </ww:if>
                                        <li>
                                            <a data-operation="copy" class="trigger-dialog" id="copy_<ww:property value="name"/>" href="<ww:url page="CloneWorkflow!default.jspa"><ww:param name="'workflowMode'" value="mode" /><ww:param name="'workflowName'" value="name" /></ww:url>"><ww:text name="'common.words.copy'"/></a>
                                        </li>
                                        <ww:if test="./editable == true">
                                            <ww:if test="@workflowSchemes/empty == true || draftWorkflow == true">
                                                <li>
                                                    <a data-operation="delete" class="trigger-dialog" id="del_<ww:property value="name"/>" href="<ww:url page="DeleteWorkflow.jspa"><ww:param name="'workflowMode'" value="mode" /><ww:param name="'workflowName'" value="name" /></ww:url>"><ww:text name="'common.words.delete'"/></a>
                                                </li>
                                            </ww:if>
                                        </ww:if>
                                    </ul>
                                </td>
                                </tr>
                            </ww:iterator>
                            </tbody>
                        </table>
                        </ww:else>
                    </div>
                </div>
            </div>
        </aui:param>
    </aui:component>
</body>
</html>
