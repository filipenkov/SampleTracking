<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
	<title><ww:if test="/active(scheme) == false">
                <ww:text name="'admin.schemes.workflow.edit.edit.workflows.for'">
                    <ww:param name="'value0'"><ww:property value="scheme/string('name')" /></ww:param>
                </ww:text>
           </ww:if>
           <ww:else>
                <ww:text name="'admin.schemes.workflow.view.workflows.for'">
                    <ww:param name="'value0'"><ww:property value="scheme/string('name')" /></ww:param>
                </ww:text>               
           </ww:else></title>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:if test="/active(scheme) == false"><ww:text name="'admin.schemes.workflow.edit.edit.workflows.for'">
        <ww:param name="'value0'"><ww:property value="scheme/string('name')" /></ww:param>
    </ww:text></ww:if><ww:else><ww:text name="'admin.schemes.workflow.view.workflows.for'">
        <ww:param name="'value0'"><ww:property value="scheme/string('name')" /></ww:param>
    </ww:text></ww:else></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">workflow_schemes</page:param>
    <page:param name="postTitle">
        <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
            <ui:param name="'projects'" value="/usedIn"/>
        </ui:component>
    </page:param>
    <p>
    <ww:if test="/active(scheme) == true">
        <ww:text name="'admin.schemes.workflow.edit.page.description.disabled'">
            <ww:param name="'value0'"><ww:property value="scheme/string('name')"/></ww:param>
        </ww:text>
    </ww:if>
    <ww:else>
        <ww:text name="'admin.schemes.workflow.edit.page.description'">
            <ww:param name="'value0'"><ww:property value="scheme/string('name')"/></ww:param>
        </ww:text>
    </ww:else>
    </p>
    <ul class="optionslist">
        <ww:if test="/active(scheme) == false">
            <li><ww:text name="'admin.schemes.workflow.assign.an.issue.to.this.workflow.scheme'">
                <ww:param name="'value0'"><a href="<ww:url page="AddWorkflowSchemeEntity!default.jspa"><ww:param name="'schemeId'" value="scheme/string('id')"/></ww:url>"><b></ww:param>
                <ww:param name="'value1'"></b></a></ww:param>
            </ww:text></li>
        </ww:if>
        <li><ww:text name="'admin.schemes.workflow.view.all.workflow.schemes'">
            <ww:param name="'value0'"><a href="ViewWorkflowSchemes.jspa"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
        </ww:text></li>
    </ul>
</page:applyDecorator>
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
            <ww:if test="../active(scheme) == false">
                <td>
                    <ul class="operations-list">
                        <li><a href="<ww:url page="DeleteWorkflowSchemeEntity!default.jspa"><ww:param name="'id'" value="long('id')"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
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
                        <li><a id="del_<ww:property value="/constantsManager/issueType(string('issuetype'))/string('name')"/>_<ww:property value="/workflow(string('workflow'))/name"/>" href="<ww:url page="DeleteWorkflowSchemeEntity!default.jspa"><ww:param name="'id'" value="long('id')"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                    </ul>
                </td>
            </ww:if>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
    <fieldset class="hidden parameters">
        <input type="hidden" id="workflow-scheme-id" value="<ww:property value="/schemeId" />">
    </fieldset>
    <ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
        <ui:param name="'projects'" value="/usedIn"/>
        <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.scheme'"/></ui:param>
    </ui:component>
</body>
</html>
