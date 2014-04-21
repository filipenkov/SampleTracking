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
    <header>
        <nav class="aui-toolbar">
            <div class="toolbar-split toolbar-split-right">
                <ul class="toolbar-group">
                    <li class="toolbar-item">
                        <a id="add_workflowscheme" class="toolbar-trigger trigger-dialog" href="<ww:url page="AddWorkflowScheme!default.jspa"/>">
                            <span class="icon icon-add16"></span>
                            <ww:text name="'admin.schemes.workflow.add.a.new.workflow.scheme'"/>
                        </a>
                    </li>
                    <li class="toolbar-item">
                        <aui:component name="'workflow'" template="toolbarHelp.jsp" theme="'aui'">
                            <aui:param name="'cssClass'">toolbar-trigger</aui:param>
                        </aui:component>
                    </li>
                </ul>
            </div>
        </nav>
        <h2><ww:text name="'admin.schemes.workflow.workflow.schemes'"/></h2>
    </header>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">WorkflowSchemes</aui:param>
        <aui:param name="'contentHtml'">
            <p><ww:text name="'admin.schemes.workflow.description'"/></p>
            <ww:if test="schemes/size == 0">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <ww:text name="'admin.schemes.workflow.no.workflow.schemes.configured'"/>
                    </aui:param>
                </aui:component>
            </ww:if>
            <ww:else>
                <p><ww:text name="'admin.schemes.workflow.for.each.scheme'"/></p>
                <ul>
                    <li><ww:text name="'admin.schemes.workflow.assign.default.workflow'"/></li>
                    <li><ww:text name="'admin.schemes.workflow.override.this.default.workflow'"/></li>
                </ul>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <ww:text name="'admin.schemes.workflow.to.edit.a.scheme'"/>
                    </aui:param>
                </aui:component>
                <table id="workflow_schemes_table" class="aui aui-table-rowhover">
                    <thead>
                        <tr>
                            <th>
                                <ww:text name="'common.words.status'"/>
                            </th>
                            <th>
                                <ww:text name="'common.words.name'"/>
                            </th>
                            <th>
                                <ww:text name="'common.concepts.projects'"/>
                            </th>
                            <th>
                                <ul class="item-details">
                                    <li>
                                        <dl>
                                            <dt style="color:inherit">
                                                <ww:text name="'admin.issue.constant.issuetype'"/>
                                            </dt>
                                            <dd class="rarr">&rarr;</dd>
                                            <dd>
                                                <ww:text name="'admin.common.words.workflow'"/>
                                            </dd>
                                        </dl>
                                    </li>
                                </ul>
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
                            <ww:if test="/active(.) == true">
                                <span class="status-lozenge status-active" title="<ww:text name="'admin.schemes.workflow.active.description'"/>"><ww:text name="'admin.common.words.active'"/></span>
                            </ww:if>
                            <ww:else>
                                <span class="status-lozenge status-inactive" title="<ww:text name="'admin.schemes.workflow.inactive.description'"/>"><ww:text name="'admin.common.words.inactive'"/></span>
                            </ww:else>
                        </td>
                        <td>
                            <strong><ww:property value="string('name')"/></strong>
                            <div class="secondary-text"><ww:property value="string('description')"/></div>
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
                                <li><a id="edit_workflows_<ww:property value="long('id')"/>" href="<ww:url page="EditWorkflowSchemeEntities!default.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>" title="<ww:text name="'admin.schemes.workflow.assign.issue.types'"/>"><ww:text name="'common.words.assign'"/></a></li>
                                <li><a id="cp_<ww:property value="long('id')"/>" href="<ww:url page="CopyWorkflowScheme.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>" title="<ww:text name="'admin.schemes.workflow.create.a.copy'"/>"><ww:text name="'common.words.copy'"/></a></li>
                            <ww:if test="/active(.) == false">
                                <li><a class="trigger-dialog" id="edit_<ww:property value="long('id')"/>" href="<ww:url page="EditWorkflowScheme!default.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                                <li><a class="trigger-dialog" id="del_<ww:property value="long('id')"/>" href="<ww:url page="DeleteWorkflowScheme!default.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                            </ww:if>
                            </ul>
                        </td>
                    </tr>
                    </ww:iterator>
                    </tbody>
                </table>
            </ww:else>
        </aui:param>
    </aui:component>
</body>
</html>