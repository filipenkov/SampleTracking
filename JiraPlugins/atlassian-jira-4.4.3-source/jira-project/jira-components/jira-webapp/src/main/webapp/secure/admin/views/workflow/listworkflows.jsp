<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflows.view.workflows'"/></title>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.workflows.view.workflows'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">workflow</page:param>
        <p>
        <ww:text name="'admin.workflows.table.below.shows'"/>
        </p>
        <p>
            <ww:text name="'admin.workflows.workflow.modes'"/>
            <ul>
                <li><ww:text name="'admin.workflows.active.description2'">
                    <ww:param name="'value0'"><strong class="status-active"></ww:param>
                    <ww:param name="'value1'"></strong></ww:param>
                    </ww:text>
                </li>
                <li><ww:text name="'admin.workflows.editing.description2'">
                    <ww:param name="'value0'"><strong class="status-draft"></ww:param>
                    <ww:param name="'value1'"></strong></ww:param>
                    </ww:text>
                </li>
                <li><ww:text name="'admin.workflows.inactive.description2'">
                    <ww:param name="'value0'"><strong class="status-inactive"></ww:param>
                    <ww:param name="'value1'"></strong></ww:param>
                    </ww:text>
                </li>
            </ul>
            <ww:text name="'admin.workflows.delete.workflow.instruction'"/>
        </p>
        <ul class="optionslist">
            <li><ww:text name="'admin.workflows.view.current.workflow.schemes'">
                <ww:param name="'value0'"><b><a href="<%= request.getContextPath() %>/secure/admin/ViewWorkflowSchemes.jspa"></ww:param>
                <ww:param name="'value1'"></a></b></ww:param>
            </ww:text></li>
        </ul>
</page:applyDecorator>

    <table id="workflows_table" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.words.name'"/>
                </th>
                <th>
                    <ww:text name="'common.words.description'"/>
                </th>
                <th>
                    <ww:text name="'common.words.status'"/>
                </th>
                <th>
                    <ww:text name="'admin.menu.schemes.schemes'"/>
                </th>
                <th>
                    <ww:text name="'admin.workflows.number.of.steps'"/>
                </th>
                <th>
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="workflows" status="'status'">
        <ww:if test="./draftWorkflow == true && /parentWorkflowActive(.) == false">
            <tr style="background-color:#fcc">
            <ww:property id="bgcolour" value="'ffcccc'"/>
        </ww:if>
        <ww:else>
            <tr>
        </ww:else>
                <td>
                     <ww:if test="./draftWorkflow == true">
                         <img src="<%= request.getContextPath()%>/images/icons/link_out_bot.gif" alt="<ww:text name="'common.words.draft'"/>" style="float:left;"/>
                         <div style="padding-left:20px;" class="boldTop"><ww:property value="name"/></div>
                    </ww:if>
                    <ww:else>
                        <span class="boldTop"><ww:property value="name"/></span>
                    </ww:else>
                    <ww:if test="./systemWorkflow == true">
                        &nbsp;<span class="smallgrey"><ww:text name="'admin.workflows.readonly.system.workflow'"/></span>
                    </ww:if>
                    <ww:if test="./updateAuthorName != null && ./updatedDate != null">
                        <ww:if test="./draftWorkflow == false">
                            <br/>
                        </ww:if>
                        <ww:if test="./updateAuthorName == ''">
                            <span class="smallgrey"><ww:text name="'admin.workflows.last.modified.anonymous'">
                                <ww:param name="'value0'"><ww:property value="/outlookDate/formatDMY(./updatedDate)"/></ww:param>
                            </ww:text></span>
                        </ww:if>
                        <ww:else>
                            <span class="smallgrey"><ww:text name="'admin.workflows.last.modified'">
                                <ww:param name="'value0'"><ww:property value="/outlookDate/formatDMY(./updatedDate)"/></ww:param>
                                <ww:param name="'value1'"><ww:property value="/userFullName(./updateAuthorName)"/></ww:param>
                            </ww:text></span>
                        </ww:else>
                    </ww:if>
                </td>
                <td><ww:property value="description"/></td>
                <td>
                    <ww:if test="./draftWorkflow == true">
                        <strong class="status-draft"><ww:text name="'common.words.draft'"/></strong>
                    </ww:if>
                    <ww:else>
                        <ww:if test="./active == true">
                            <strong class="status-active"><ww:text name="'admin.common.words.active'"/></strong>
                        </ww:if>
                        <ww:else>
                            <strong class="status-inactive"><ww:text name="'admin.common.words.inactive'"/></strong>
                        </ww:else>
                    </ww:else>
                </td>
                <td>
                    <ww:property value="/schemesForWorkflow(.)" id="workflowSchemes"/>
                    <ww:if test="@workflowSchemes && @workflowSchemes/empty == false">
                        <ul>
                        <ww:iterator value="@workflowSchemes">
                            <li><a href="<%= request.getContextPath() %>/secure/admin/EditWorkflowSchemeEntities!default.jspa?schemeId=<ww:property value="long('id')" />"><ww:property value="string('name')" /></a></li>
                        </ww:iterator>
                        </ul>
                    </ww:if>
                    <ww:if test="default == true">
                        <div class="description"><ww:text name="'admin.workflows.default.workflow.description'"/></div>
                    </ww:if>
                </td>
                <td><ww:property value="descriptor/steps/size" /></td>
                <td>
                    <ul class="operations-list">
                        <li><a id="steps_<ww:property value="mode"/>_<ww:property value="name"/>" href="<ww:url page="ViewWorkflowSteps.jspa"><ww:param name="'workflowMode'" value="mode" /><ww:param name="'workflowName'" value="name" /></ww:url>"><ww:text name="'admin.workflows.steps'"/></a></li>
                        <li><a id="xml_<ww:property value="name"/>" href="<ww:url page="ViewWorkflowXml.jspa"><ww:param name="'workflowMode'" value="mode" /><ww:param name="'workflowName'" value="name" /></ww:url>"><ww:text name="'admin.common.words.xml'"/></a></li>
                        <li><a id="copy_<ww:property value="name"/>" href="<ww:url page="CloneWorkflow!default.jspa"><ww:param name="'workflowMode'" value="mode" /><ww:param name="'workflowName'" value="name" /></ww:url>"><ww:text name="'common.words.copy'"/></a></li>
                    <ww:if test="./editable == true">
                        <li><a id="edit_<ww:property value="mode"/>_<ww:property value="name"/>" href="<ww:url page="EditWorkflow!default.jspa"><ww:param name="'workflowMode'" value="mode" /><ww:param name="'workflowName'" value="name" /></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                        <ww:if test="@workflowSchemes/empty == true || draftWorkflow == true">
                            <li><a id="del_<ww:property value="name"/>" href="<ww:url page="DeleteWorkflow.jspa"><ww:param name="'workflowMode'" value="mode" /><ww:param name="'workflowName'" value="name" /></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                        </ww:if>
                    </ww:if>
                    <ww:if test="./active == true && ./systemWorkflow == false && ./hasDraftWorkflow == false">
                        <li><a id="createDraft_<ww:property value="name"/>" href="<ww:url page="CreateDraftWorkflow.jspa"><ww:param name="'draftWorkflowName'" value="name" /></ww:url>"><ww:text name="'admin.workflows.create.draft'"/></a></li>
                    </ww:if>
                    <ww:if test="./draftWorkflow == true && /parentWorkflowActive(.) == true">
                        <li><a id="publishDraft_<ww:property value="name"/>" href="<ww:url page="PublishDraftWorkflow!default.jspa"><ww:param name="'workflowName'" value="name" /><ww:param name="'workflowMode'" value="mode" /></ww:url>"><ww:text name="'common.words.publish'"/></a></li>
                    </ww:if>
                        <li><a id="designer_<ww:property value="name"/>" href="<ww:url page="WorkflowDesigner.jspa" atltoken="false"><ww:param name="'wfName'" value="name" /><ww:param name="'workflowMode'" value="mode" /></ww:url>"><ww:text name="'workflow.designer.link'"/></a></li>
                    </ul>
                </td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>


    <page:applyDecorator name="jiraform">
        <page:param name="action">AddWorkflow.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="title"><ww:text name="'admin.workflows.add.new.workflow'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <ww:text name="'admin.workflows.instructions1'"/>
            <ul>
                <li><ww:text name="'admin.workflows.provide.a.name'"/>
                <li><ww:text name="'admin.workflows.add.the.steps'"/>
                <li><ww:text name="'admin.workflows.create.transitions'"/>
                <li><ww:text name="'admin.workflows.enable.the.workflow'"/>
            </ul>
            <ww:if test="/systemAdministrator == true">
                <ww:text name="'admin.workflows.create.or.import.from.xml'">
                    <ww:param name="'value0'"><b><a href="<ww:url page="ImportWorkflowFromXml!default.jspa" />"></ww:param>
                    <ww:param name="'value1'"></a></b></ww:param>
                </ww:text>
            </ww:if>
        </page:param>

        <ui:textfield label="text('common.words.name')" name="'newWorkflowName'" size="'30'">
            <ui:param name="'description'"><ww:text name="'admin.common.phrases.use.only.ascii'"/></ui:param>
        </ui:textfield>
        <ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />

    </page:applyDecorator>

</body>
</html>
