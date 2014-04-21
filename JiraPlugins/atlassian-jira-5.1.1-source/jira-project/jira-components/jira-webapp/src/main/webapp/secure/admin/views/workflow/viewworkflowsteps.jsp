<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:if test="workflow/editable == true"><ww:text name="'admin.workflows.edit'"/></ww:if><ww:else><ww:text name="'viewworkflow.view.title'"/></ww:else> &mdash; <ww:property value="/workflowDisplayName" /></title>
    <%
        WebResourceManager webResourceManager = ComponentManager.getComponent(WebResourceManager.class);
        webResourceManager.requireResourcesForContext("jira.workflow.view");
    %>
</head>
<body class="page-type-workflow-view">
<ww:property value="/headerHtml" escape="false"/>
<div class="workflow-container">
    <ww:if test="workflow/editable == true">
        <nav class="aui-toolbar">
            <ul class="toolbar-group">
                <li class="toolbar-item">
                    <a class="toolbar-trigger" id="workflow-diagram" href="<ww:url page="WorkflowDesigner.jspa" atltoken="false"><ww:param name="'wfName'" value="/workflow/name" /><ww:param name="'workflowMode'" value="/workflow/mode" /></ww:url>"><ww:text name="'admin.workflows.actions.view.diagram'" /></a>
                </li>
                <li class="toolbar-item active">
                    <a class="toolbar-trigger" id="workflow-text"><ww:text name="'admin.workflows.actions.view.text'" /></a>
                </li>
            </ul>
            <ul class="toolbar-group">
                <li class="toolbar-item">
                    <a class="toolbar-trigger" data-operation="xml" id="xml_<ww:property value="/workflow/name"/>" href="<ww:url page="ViewWorkflowXml.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /></ww:url>"><ww:text name="'admin.workflows.actions.download.xml'" /></a>
                </li>
            </ul>
        </nav>
    </ww:if>
    <ww:else>
        <nav class="aui-toolbar">
            <ul class="toolbar-group">
                <li class="toolbar-item<ww:if test="/diagramMode == true"> active</ww:if>">
                    <a class="toolbar-trigger workflow-view-toggle" id="workflow-diagram" href="#workflow-view-diagram" data-mode="diagram"><ww:text name="'admin.workflows.actions.view.diagram'" /></a>
                </li>
                <li class="toolbar-item<ww:if test="/diagramMode == false"> active</ww:if>">
                    <a class="toolbar-trigger workflow-view-toggle" id="workflow-text" href="#workflow-view-text" data-mode="text"><ww:text name="'admin.workflows.actions.view.text'" /></a>
                </li>
            </ul>
            <ul class="toolbar-group">
                <li class="toolbar-item">
                    <a class="toolbar-trigger" data-operation="xml" id="xml_<ww:property value="/workflow/name"/>" href="<ww:url page="ViewWorkflowXml.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /></ww:url>"><ww:text name="'admin.workflows.actions.download.xml'" /></a>
                </li>
            </ul>
        </nav>
    </ww:else>
    <ww:if test="workflow/editable != true">
    <div class="workflow-view<ww:if test="/diagramMode == false"> hidden</ww:if>" id="workflow-view-diagram">
        <div class="aui-toolbar">
            <div class="toolbar-split toolbar-split-row">
                <ul class="toolbar-group">
                    <li class="toolbar-item">
                        <a id="zoom-in" class="toolbar-trigger workflow-zoom-toggle" href="#">+</a>
                    </li>
                </ul>
            </div>
            <div class="toolbar-split toolbar-split-row">
                <ul class="toolbar-group">
                    <li class="toolbar-item active">
                        <a id="zoom-out" class="toolbar-trigger workflow-zoom-toggle" href="#">&minus;</a>
                    </li>
                </ul>
            </div>
        </div>
        <div id="workflow-diagram-container">
            <div class="workflow-diagram zoomed-out" data-workflow-diagram="<ww:url page="/plugins/servlet/workflow/thumbnail/getThumbnail" atltoken="false"><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'width'" value="'full'" /></ww:url>">
            </div>
        </div>
    </div>
    </ww:if>
    <div class="workflow-view<ww:if test="/diagramMode == true && workflow/editable == false"> hidden</ww:if>" id="workflow-view-text">
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
            <%-- Check if there are any available (unlinked) statuses, as we cannot have more than one step per JIRA status
                 in the same workflow --%>
            <ww:if test="unlinkedStatuses && unlinkedStatuses/empty == false">
                <page:applyDecorator id="workflow-step-add" name="auiform">
                    <page:param name="action">AddWorkflowStep.jspa</page:param>
                    <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
                    <page:param name="submitButtonName"><ww:text name="'common.forms.add'"/></page:param>
                    <page:param name="cancelLinkURI">ListWorkflows.jspa</page:param>

                    <aui:component template="formHeading.jsp" theme="'aui'">
                        <aui:param name="'text'"><ww:text name="'admin.workflows.add.new.step'"/></aui:param>
                    </aui:component>

                    <page:applyDecorator name="auifieldgroup">
                        <aui:textfield label="text('admin.workflows.step.name')" name="'stepName'" mandatory="true" theme="'aui'" />
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldgroup">
                        <aui:select label="text('admin.workflows.linked.status')" name="'stepStatus'" list="unlinkedStatuses" listKey="'genericValue/string('id')'" listValue="'nameTranslation'" theme="'aui'" />
                    </page:applyDecorator>

                    <aui:component name="'workflowName'" value="workflow/name"  template="hidden.jsp" theme="'aui'" />
                    <aui:component name="'workflowMode'" value="workflow/mode" template="hidden.jsp" theme="'aui'" />
                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">error</aui:param>
                    <aui:param name="'messageHtml'">
                            <p>
                                <ww:text name="'admin.workflows.all.existing.issue.statuses.are.used'">
                                    <ww:param name="'value0'"><br></ww:param>
                                    <ww:param name="'value1'"><a href="ViewStatuses.jspa"></ww:param>
                                    <ww:param name="'value2'"></a></ww:param>
                                </ww:text>
                            </p>
                    </aui:param>
                </aui:component>
            </ww:else>
        </ww:if>
    </div>
</div>
</body>
</html>
