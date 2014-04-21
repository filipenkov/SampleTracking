<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
	<title><ww:text name="'admin.schemes.workflow.workflow.schemes'"/></title>
</head>

<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.schemes.workflow.workflow.schemes'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">workflow</page:param>

    <p>
    <ww:text name="'admin.schemes.workflow.description'"/>
    </p>
    <p>
        <ww:text name="'admin.schemes.workflow.all.schemes.have.one.of.two.modes'"/>
        <ul>
            <li><ww:text name="'admin.schemes.workflow.active.description'">
                <ww:param name="'value0'"><strong class="status-active"></ww:param>
                <ww:param name="'value1'"></strong></ww:param>
            </ww:text></li>
            <li><ww:text name="'admin.schemes.workflow.inactive.description'">
                <ww:param name="'value0'"><strong class="status-inactive"></ww:param>
                <ww:param name="'value1'"></strong></ww:param>
                <ww:param name="'value2'"><b></ww:param>
                <ww:param name="'value3'"></b></ww:param>
            </ww:text></li>
        </ul>
        <ww:text name="'admin.schemes.workflow.to.edit.a.scheme'"/>
    </p>
    <p>
    <ww:text name="'admin.schemes.workflow.for.each.scheme'"/>
    <ul>
        <li><ww:text name="'admin.schemes.workflow.assign.default.workflow'"/></li>
        <li><ww:text name="'admin.schemes.workflow.override.this.default.workflow'"/></li>
    </ul>
    </p>
    <ul class="optionslist">
        <li><ww:text name="'admin.schemes.workflow.add.a.new.workflow.scheme'">
            <ww:param name="'value0'"><b><a id="add_workflowscheme" href="<ww:url page="AddWorkflowScheme!default.jspa"/>"></ww:param>
            <ww:param name="'value1'"></a></b></ww:param>
        </ww:text></li>
        <li><ww:text name="'admin.schemes.workflow.list.current.workflows'">
            <ww:param name="'value0'"><b><a id="list_workflows" href="<%= request.getContextPath() %>/secure/admin/workflows/ListWorkflows.jspa"></ww:param>
            <ww:param name="'value1'"></a></b></ww:param>
        </ww:text></li>
    </ul>
</page:applyDecorator>

<ww:if test="schemes/size == 0">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <ww:text name="'admin.schemes.workflow.no.workflow.schemes.configured'"/>
        </aui:param>
    </aui:component>
</ww:if>
<ww:else>
    <table id="workflow_schemes_table" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.words.name'"/>
                </th>
                <th>
                    <ww:text name="'common.concepts.projects'"/>
                </th>
                <th>
                    <ww:text name="'admin.common.phrases.active.inactive'"/>
                </th>
                <th>
                    <ww:text name="'admin.schemes.workflow.workflows'"/>
                </th>
                <th>
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="schemes" status="'status'">
        <tr>
            <td>
                <a id="workflowscheme_<ww:property value="long('id')"/>" href="<ww:url page="EditWorkflowSchemeEntities!default.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>"><ww:property value="string('name')"/></a>
                <br/><ww:property value="string('description')"/>
            </td>
            <td>
                <ww:if test="/projects(.)/empty == false">
                    <ul>
                    <ww:iterator value="projects(.)">
                        <li><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="string('name')" /></a></li>
                    </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    &nbsp;
                </ww:else>
            </td>
            <td>
               <ww:if test="/active(.) == true">
                   <strong class="status-active"><ww:text name="'admin.common.words.active'"/></strong>
               </ww:if>
               <ww:else>
                   <strong class="status-inactive"><ww:text name="'admin.common.words.inactive'"/></strong>
               </ww:else>
            </td>
            <td>
                <ul class="item-details">
                    <li>
                        <dl>
                            <dt>
                                <ww:text name="'admin.schemes.issuesecurity.unassigned.types'"/>
                            </dt>
                            <dd class="rarr">&rarr;</dd>
                            <dd>
                                <a title="<ww:text name="'admin.workflows.view.workflow.steps'"/>" href="<ww:url value="'/secure/admin/workflows/ViewWorkflowSteps.jspa'" ><ww:param name="'workflowMode'" value="'live'" /><ww:param name="'workflowName'" value="defaultEntity(.)/string('workflow')" /></ww:url>"><ww:property value="defaultEntity(.)/string('workflow')" /></a>
                            </dd>
                        </dl>
                    </li>
                    <ww:iterator value="nonDefaultEntities(.)">
                    <li>
                        <dl>
                            <dt>
                            <ww:property value="/constantsManager/issueTypeObject(string('issuetype'))">
                                <ww:component name="'issuetype'" template="constanticon.jsp">
                                    <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                    <ww:param name="'iconurl'" value="./iconUrl" />
                                    <ww:param name="'alt'"><ww:property value="./nameTranslation" /></ww:param>
                                    <ww:param name="'title'"><ww:property value="./nameTranslation" /> - <ww:property value="./descTranslation" /></ww:param>
                                </ww:component>
                                <ww:property value="./nameTranslation" />
                            </ww:property>
                            </dt>
                            <dd class="rarr">&rarr;</dd>
                            <dd>
                                <a title="<ww:text name="'admin.workflows.view.workflow.steps'"/>" href="<ww:url value="'/secure/admin/workflows/ViewWorkflowSteps.jspa'" ><ww:param name="'workflowMode'" value="'live'" /><ww:param name="'workflowName'" value="string('workflow')" /></ww:url>"><ww:property value="string('workflow')" /></a>
                            </dd>
                        </dl>
                    </li>
                    </ww:iterator>
                </ul>
            </td>
            <td>
                <ul class="operations-list">
                    <li><a id="edit_workflows_<ww:property value="string('name')"/>" href="<ww:url page="EditWorkflowSchemeEntities!default.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>"><ww:text name="'admin.schemes.workflow.workflows'"/></a></li>
                    <li><a id="cp_<ww:property value="string('name')"/>" href="<ww:url page="CopyWorkflowScheme.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>" title="<ww:text name="'admin.schemes.workflow.create.a.copy'"/>"><ww:text name="'common.words.copy'"/></a></li>
                <ww:if test="/active(.) == false">
                    <li><a id="edit_<ww:property value="string('name')"/>" href="<ww:url page="EditWorkflowScheme!default.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                    <li><a id="del_<ww:property value="string('name')"/>" href="<ww:url page="DeleteWorkflowScheme!default.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                </ww:if>
                </ul>
            </td>
        </tr>
        </ww:iterator>
        </tbody>
    </table>
</ww:else>
</body>
</html>
