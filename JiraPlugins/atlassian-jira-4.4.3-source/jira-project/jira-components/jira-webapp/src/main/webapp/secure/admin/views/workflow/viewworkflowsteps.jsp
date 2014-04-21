<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflows.view.workflow.steps'"/> - <ww:property value="/workflowDisplayName" /></title>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.workflows.view.workflow.steps'"/> &mdash; <ww:property value="/workflowDisplayName" /></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">workflow</page:param>
    <page:param name="postTitle">
        <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
            <ui:param name="'projects'" value="/usedIn"/>
        </ui:component>
    </page:param>
    <%@ include file="/includes/admin/workflow/workflowinfobox.jsp" %>
    <p>
        <ww:text name="'admin.workflows.this.page.shows'">
            <ww:param name="'value0'"><b id="workflow-name"><ww:property value="/workflowDisplayName" /></b></ww:param>
        </ww:text>
        <ww:if test="/workflow/draftWorkflow == true">
            <ww:text name="'admin.workflowstep.steps.exists.on.active'"/>
        </ww:if>
    </p>
    <p>
        <ww:property value="workflow">
            <ww:if test="./editable == false">
                <ww:if test="./active == true"><ww:text name="'admin.workflows.not.editable.because.active'">
                    <ww:param name="'value0'"><strong class="status-active"></ww:param>
                    <ww:param name="'value1'"></strong></ww:param>
                </ww:text></ww:if>
                <ww:elseIf test="./systemWorkflow == true"><ww:text name="'admin.workflows.not.editable.because.system.workflow'">
                    <ww:param name="'value0'"><b><ww:property value="workflow/name" /></b></ww:param>
                </ww:text></ww:elseIf>
            </ww:if>
        </ww:property>
    </p>
    <ul class="optionslist">
        <li><ww:text name="'admin.workflows.view.all.workflows'">
            <ww:param name="'value0'"><a href="ListWorkflows.jspa"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
        </ww:text></li>
        <li><ww:text name="'admin.workflows.view.all.statuses'">
            <ww:param name="'value0'"><a href="ViewStatuses.jspa"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
        </ww:text></li>
        <ww:if test="/workflow/editable == true">
            <li>
                <ww:text name="'admin.workflows.edit.in.designer'">
                    <ww:param name="'value0'"><a id="workflow-designer-lnk" href="<ww:url page="WorkflowDesigner.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'wfName'" value="/workflow/name" /></ww:url>"><strong></ww:param>
                    <ww:param name="'value1'"></strong></a></ww:param>
                </ww:text>
            </li>
        </ww:if>
    </ul>
</page:applyDecorator>
<table id="steps_table" class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th>
                <ww:text name="'admin.workflows.step.name.id'">
                    <ww:param name="'value0'"><span></ww:param>
                    <ww:param name="'value1'"></span></ww:param>
                    <ww:param name="'value2'"><span></ww:param>
                    <ww:param name="'value3'"></span></ww:param>
                </ww:text>
            </th>
            <th>
                <ww:text name="'admin.workflows.linked.status'"/>
            </th>
            <th>
                <ww:text name="'admin.workflows.transitions'">
                    <ww:param name="'value0'"><span></ww:param>
                    <ww:param name="'value1'"></span></ww:param>
                </ww:text>
            </th>
            <th>
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="workflow/descriptor/steps" status="'status'">
        <tr>
            <td>
                <a href="<ww:url page="ViewWorkflowStep.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="./id" /></ww:url>"
                        id="step_link_<ww:property value="id"/>"><ww:property value="name"/></a>
                <span class="smallgrey">(<ww:property value="id" />)</span>
            </td>
            <td>
                <ww:if test="metaAttributes/('jira.status.id')">
                <ww:property value="metaAttributes/('jira.status.id')">
                    <ww:property value="/status(.)">
                        <ww:component name="'notUsed'" template="constanticon.jsp">
                            <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                            <ww:param name="'iconurl'" value="./iconUrl" />
                            <ww:param name="'alt'"><ww:property value="./nameTranslation" /></ww:param>
                            <ww:param name="'title'"><ww:property value="./nameTranslation" /> - <ww:property value="./descTranslation" /></ww:param>
                        </ww:component>
                        <ww:property value="./nameTranslation" />
                    </ww:property>
                </ww:property>
                </ww:if>
                <ww:else>
                    <ww:text name="'admin.workflowtransition.no.linked.status'"/>
                </ww:else>
            </td>
            <td>
                <ww:iterator value="actions">
                    <ww:if test="./common == true"><em></ww:if>
                    <a href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="../id" /><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                       <ww:if test="./metaAttributes/('jira.description')">title="<ww:property value="./metaAttributes/('jira.description')" />"</ww:if>
                            id="edit_action_<ww:property value="../id" />_<ww:property value="id" />"><ww:property value="name" /></a>
                    <ww:if test="./common == true"></em></ww:if>
                    <span class="smallgrey">(<ww:property value="id"/>)</span>
                    <br>
                    <span class="smallgrey">&gt;&gt;
                        <ww:if test="/transitionWithoutStepChange(.) == true">
                            <ww:property value="../name"/>
                        </ww:if>
                        <ww:else>
                            <ww:property value="/workflow/descriptor/step(unconditionalResult/step)/name" />
                        </ww:else>
                    </span><br>
                </ww:iterator>

                <%-- Global Actions --%>
                <ww:iterator value="workflow/descriptor/globalActions" status="'status'">
                    <em><a href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name"/><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                           <ww:if test="./metaAttributes/('jira.description')">title="<ww:property value="./metaAttributes/('jira.description')" />"</ww:if>><ww:property value="name" /></a></em> <span class="smallgrey">(<ww:property value="id"/>)</span>
                    <br>
                    <span class="smallgrey">&gt;&gt; <ww:property value="/workflow/descriptor/step(unconditionalResult/step)/name" /></span><br>
                </ww:iterator>
            </td>
            <td>
                <ul class="operations-list">
                <ww:if test="workflow/editable == true">
                    <ww:if test="/stepWithoutTransitionsOnDraft(id) == false">
                        <li><a id="add_trans_<ww:property value="id" />" href="<ww:url page="AddWorkflowTransition!default.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:text name="'admin.workflows.add.transition'"/></a></li>
                    </ww:if>
                    <ww:if test="actions/empty == false">
                        <li><a id="del_trans_<ww:property value="id" />" href="<ww:url page="DeleteWorkflowTransitions!default.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:text name="'admin.workflows.delete.transitions'"/></a></li>
                    </ww:if>
                    <li><a id="edit_step_<ww:property value="id" />" href="<ww:url page="EditWorkflowStep!default.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                </ww:if>
                <li><a href="<ww:url page="ViewWorkflowStepMetaAttributes.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:text name="'admin.workflows.view.properties'"/></a></li>
                <%-- canDeleteStep checks if the workflow isEditable --%>
                <ww:if test="canDeleteStep(.) == true">
                    <li><a id="delete_step_<ww:property value="id" />" href="<ww:url page="DeleteWorkflowStep!default.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:text name="'admin.workflows.delete.step'"/></a></li>
                </ww:if>
                </ul>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

    <ww:if test="workflow/editable == true">
        <p>

        <%-- Check if there are any available (unlinked) statuses, as we cannot have more than one step per JIRA status
             in the same workflow --%>
        <ww:if test="unlinkedStatuses && unlinkedStatuses/empty == false">
        <page:applyDecorator name="jiraform">
            <page:param name="action">AddWorkflowStep.jspa</page:param>
            <page:param name="submitId">add_submit</page:param>
            <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
            <page:param name="title"><ww:text name="'admin.workflows.add.new.step'"/></page:param>
            <page:param name="width">100%</page:param>

            <ui:component name="'workflowName'" value="workflow/name"  template="hidden.jsp"  theme="'single'" />
            <ui:component name="'workflowMode'" value="workflow/mode" template="hidden.jsp" />

            <ui:textfield label="text('admin.workflows.step.name')" name="'stepName'" size="'30'" />

            <ui:select label="text('admin.workflows.linked.status')" name="'stepStatus'" list="unlinkedStatuses" listKey="'genericValue/string('id')'" listValue="'nameTranslation'" />

        </page:applyDecorator>
        </ww:if>
        <ww:else>
        <page:applyDecorator name="jirapanel">
            <page:param name="title"><ww:text name="'admin.workflows.add.new.step'"/></page:param>
            <page:param name="width">100%</page:param>
            <p>
            <ww:text name="'admin.workflows.all.existing.issue.statuses.are.used'">
                <ww:param name="'value0'"><br></ww:param>
                <ww:param name="'value1'"><a href="ViewStatuses.jspa"></ww:param>
                <ww:param name="'value2'"></a></ww:param>
            </ww:text>
            </p>
        </page:applyDecorator>
        </ww:else>
        </p>
    </ww:if>

    <ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
        <ui:param name="'projects'" value="/usedIn"/>
        <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.workflow'"/></ui:param>
    </ui:component>
</body>
</html>
