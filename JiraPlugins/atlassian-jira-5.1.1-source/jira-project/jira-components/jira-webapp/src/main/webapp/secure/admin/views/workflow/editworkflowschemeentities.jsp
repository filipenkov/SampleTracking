<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title>
        <ww:text name="'admin.schemes.workflow.assigned.workflows.for.scheme'">
            <ww:param name="'value0'"><ww:property value="scheme/string('name')" /></ww:param>
        </ww:text>
    </title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
</head>
<body>
    <header class="item-header">
        <nav class="aui-toolbar">
            <div class="toolbar-split toolbar-split-right">
                <ul class="toolbar-group">
                    <ww:if test="/active(scheme) == false">
                    <li class="toolbar-item">
                        <a id="assign-workflow" class="toolbar-trigger trigger-dialog" href="<ww:url page="AddWorkflowSchemeEntity!default.jspa"><ww:param name="'schemeId'" value="scheme/string('id')"/></ww:url>">
                            <span class="icon icon-add16"></span>
                            <ww:text name="'admin.schemes.workflow.assign.workflow'"/>
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
        <ul class="breadcrumbs">
            <li><a href="ViewWorkflowSchemes.jspa"><ww:text name="'admin.schemes.workflow.workflow.schemes'"/></a></li>
            <li><ww:text name="'admin.schemes.workflow.assigned.workflows.for.scheme'"/></li>
        </ul>
        <h2 class="item-summary workflow-name">
            <ww:property value="scheme/string('name')" />
        </h2>
        <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
            <ui:param name="'projects'" value="/usedIn"/>
        </ui:component>
    </header>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">EditWorkflowSchemes</aui:param>
        <aui:param name="'contentHtml'">
            <ww:if test="/active(scheme) == true">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <ww:text name="'admin.schemes.workflow.edit.page.description.disabled'">
                            <ww:param name="'value0'"><ww:property value="scheme/string('name')"/></ww:param>
                        </ww:text>
                    </aui:param>
                </aui:component>
            </ww:if>
            <ww:if test="nonDefaultEntities/empty == true && defaultEntity == null">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <ww:text name="'admin.schemes.workflow.no.associations'" />
                    </aui:param>
                </aui:component>
            </ww:if>
            <ww:else>
                <table class="aui aui-table-rowhover">
                    <thead>
                        <tr>
                            <th>
                                <ww:text name="'common.concepts.issuetype'"/>
                            </th>
                            <th>
                                <ww:text name="'issue.field.workflow'"/>
                            </th>
                        <ww:if test="/active(scheme) == false">
                            <th>
                                <ww:text name="'common.words.operations'"/>
                            </th>
                        </ww:if>
                        </tr>
                    </thead>
                    <tbody>
                    <ww:if test="defaultEntity != null">
                        <ww:property value="defaultEntity">
                        <tr style="background-color:#ffc">
                            <td>
                                <b><ww:text name="'admin.schemes.workflow.all.unassigned.issue.types'"/></b>
                                <div class="description"><ww:text name="'admin.schemes.workflow.issues.of.all.types.not.explicitly.assigned.will.use.this.workflow'"/></div>
                            </td>
                            <td>
                                <ww:property value="/workflow(string('workflow'))">
                                <a title="<ww:text name="'admin.workflows.view.workflow.steps'"/>" href="<ww:url value="'/secure/admin/workflows/ViewWorkflowSteps.jspa'" ><ww:param name="'workflowMode'" value="mode" /><ww:param name="'workflowName'" value="name" /></ww:url>"><ww:property value="name" /></a>
                                </ww:property>
                            </td>
                        <ww:if test="/active(scheme) == false">
                            <td>
                                <ul class="operations-list">
                                    <li><a class="trigger-dialog" href="<ww:url page="DeleteWorkflowSchemeEntity!default.jspa"><ww:param name="'id'" value="long('id')"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                                </ul>
                            </td>
                        </ww:if>
                        </tr>
                        </ww:property>
                    </ww:if>
                    <ww:iterator value="nonDefaultEntities" status="'status'">
                        <tr>
                            <td>
                                <ww:property value="/constantsManager/issueTypeObject(string('issuetype'))">
                                    <ww:component name="'issuetype'" template="constanticon.jsp">
                                        <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                        <ww:param name="'iconurl'" value="./iconUrl" />
                                        <ww:param name="'alt'"><ww:property value="./nameTranslation" /></ww:param>
                                        <ww:param name="'title'"><ww:property value="./nameTranslation" /> - <ww:property value="./descTranslation" /></ww:param>
                                    </ww:component>
                                    <ww:property value="./nameTranslation" />
                                </ww:property>
                            </td>
                            <td>
                                <ww:property value="/workflow(string('workflow'))">
                                    <a title="<ww:text name="'admin.workflows.view.workflow.steps'"/>" href="<ww:url value="'/secure/admin/workflows/ViewWorkflowSteps.jspa'" ><ww:param name="'workflowMode'" value="mode" /><ww:param name="'workflowName'" value="name" /></ww:url>"><ww:property value="name" /></a>
                                </ww:property>
                            </td>
                        <ww:if test="../active(scheme) == false">
                            <td>
                                <ul class="operations-list">
                                    <li><a class="trigger-dialog" id="del_<ww:property value="/constantsManager/issueType(string('issuetype'))/string('name')"/>_<ww:property value="/workflow(string('workflow'))/name"/>" href="<ww:url page="DeleteWorkflowSchemeEntity!default.jspa"><ww:param name="'id'" value="long('id')"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                                </ul>
                            </td>
                        </ww:if>
                        </tr>
                    </ww:iterator>
                    </tbody>
                </table>
            </ww:else>
            <fieldset class="hidden parameters">
                <input type="hidden" id="workflow-scheme-id" value="<ww:property value="/schemeId" />"/>
            </fieldset>
            <ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
                <ui:param name="'projects'" value="/usedIn"/>
                <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.scheme'"/></ui:param>
            </ui:component>
        </aui:param>
    </aui:component>
</body>
</html>
