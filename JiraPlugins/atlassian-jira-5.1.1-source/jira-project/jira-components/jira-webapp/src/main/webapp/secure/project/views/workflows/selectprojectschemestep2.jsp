<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.selectworkflowscheme.select.workflow.scheme'"/></title>
    <meta name="admin.active.section" content="atl.jira.proj.config"/>
</head>
<body>
    <page:applyDecorator name="jiraform">
        <page:param name="title"><ww:text name="'admin.selectworkflowscheme.associate.scheme.to.project'"/>: <ww:property value="project/string('name')" /></page:param>
        <page:param name="description">
            <p><ww:text name="'admin.selectworkflowscheme.step2'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
            </ww:text></p>

            <ww:if test="/haveIssuesToMigrate == true">
                <p>
                    <ww:text name="'admin.selectworkflowscheme.step2.reallybackupnote.line1'">
                        <ww:param name="'value0'"><span class="warning"></ww:param>
                        <ww:param name="'value1'"></span></ww:param>
                        <ww:param name="'value2'"><ww:property value="project/string('name')" /></ww:param>
                    </ww:text>
                    <br />
                    <ww:if test="/systemAdministrator == true">
                        <ww:text name="'admin.selectworkflowscheme.step2.reallybackupnote.line2'">
                            <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/XmlBackup!default.jspa"></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.selectworkflowscheme.step2.reallybackupnote.line2.admin'">
                            <ww:param name="'value0'"> </ww:param>
                            <ww:param name="'value1'"> </ww:param>
                        </ww:text>
                    </ww:else>
                    <ww:if test="/scheme != null">
                        <ww:text name="'admin.selectworkflowscheme.step2.reallybackupnote.associate.scheme'">
                            <ww:param name="'value0'"><ww:property value="/scheme/string('name')" /></ww:param>
                            <ww:param name="'value1'"><ww:property value="/project/string('name')" /></ww:param>
                        </ww:text>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.selectworkflowscheme.step2.reallybackupnote.associate.default'">
                            <ww:param name="'value0'"><ww:property value="/project/string('name')" /></ww:param>
                        </ww:text>
                    </ww:else>
                    <ww:if test="/systemAdministrator == true">
                        <ww:text name="'admin.selectworkflowscheme.step2.reallybackupnote.restore'" />
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.selectworkflowscheme.step2.reallybackupnote.restore.admin'">
                            <ww:param name="'value0'"> </ww:param>
                            <ww:param name="'value1'"> </ww:param>
                        </ww:text>
                    </ww:else>
                </p>
            </ww:if>
        </page:param>
        <%-- TODO - until we have an All Tasks Page this is disabled --%>
        <%--<page:param name="instructions">--%>
             <%--<ww:if test="anyLiveTasks == true">--%>
                 <%--<ui:component template="taskwarning.jsp"/>--%>
             <%--</ww:if>--%>
          <%--</page:param>--%>
        <ww:if test="currentTask">
             <tr><td colspan="2">
             <ww:text name="'admin.selectworkflowscheme.blocked.by.user'">
                 <ww:param name="'value0'"><a href="<ww:property value="currentTask/userURL"/>"><ww:property value="currentTask/user"/></a></ww:param>
             </ww:text>

             <ww:text name="'admin.selectworkflowscheme.goto.progressbar'">
                 <ww:param name="'value0'"><a href="<ww:property value="currentTask/progressURL"/>"></ww:param>
                 <ww:param name="'value1'"><ww:text name="'common.words.here'"/></ww:param>
                 <ww:param name="'value2'"></a></ww:param>
             </ww:text>
             </td></tr>
        </ww:if>
        <ww:else>
            <page:param name="action">SelectProjectWorkflowSchemeStep2.jspa</page:param>
            <page:param name="submitId">associate_submit</page:param>
            <page:param name="submitName"><ww:text name="'admin.projects.schemes.associate'"/></page:param>
            <page:param name="autoSelectFirst">false</page:param>

            <ww:if test="/haveIssuesToMigrate == true">
            <ww:iterator value="/migrationHelper/typesNeedingMigration">
                <tr>
                    <td>
                        <ww:component name="'status'" template="constanticon.jsp">
                            <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                            <ww:param name="'iconurl'" value="./string('iconurl')" />
                            <ww:param name="'alt'"><ww:property value="./string('name')" /></ww:param>
                            <ww:param name="'title'"><ww:property value="./string('name')" /> - <ww:property value="./string('description')" /></ww:param>
                        </ww:component>
                        <ww:property value="./string('name')" />
                        <br>
                        (<span class="secondary-text"><ww:text name="'admin.selectworkflowscheme.num.affected.issues'">
                            <ww:param name="'value0'"><ww:property value ="/numAffectedIssues(.)"/></ww:param>
                            <ww:param name="'value1'"><ww:property value ="/totalAffectedIssues(.)"/></ww:param>
                        </ww:text></span>)
                    </td>
                    <td>
                        <table id="statusmapping_<ww:property value="./string('id')" />">
                        <tr>
                            <td>
                                <ww:property value="/existingWorkflow(.)/name" />
                            </td>
                            <td>&raquo;</td>
                            <td>
                                <ww:property value="/targetWorkflow(.)/name" />
                            </td>
                        </tr>
                        <%-- Call the getStatusesNeedingMigration(issueType) method on the WebWork action so that the statuses are
                         sorted correctly --%>
                        <ww:iterator value="/statusesNeedingMigration(.)">
                        <tr>
                            <td>
                            <ww:component name="'status'" template="constanticon.jsp">
                                <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                <ww:param name="'iconurl'" value="./string('iconurl')" />
                                <ww:param name="'alt'"><ww:property value="./string('name')" /></ww:param>
                                <ww:param name="'title'"><ww:property value="./string('name')" /> - <ww:property value="./string('description')" /></ww:param>
                            </ww:component>
                            <ww:property value="./string('name')" />
                            </td>

                            <td>&raquo;</td>

                            <%-- the name of the select list will be mapping_issueTypeId_statusId (generated by action as I can't concat here) --%>
                            <ui:select label="text('issue.field.status')" name="/selectListName(.., .)" list="/targetStatuses(..)" listKey="'string('id')'" listValue="'string('name')'" theme="'single'">
                                <ui:param name="'mandatory'" value="true"/>
                            </ui:select>
                        </tr>
                        </ww:iterator>
                        </table>
                    </td>
                 </tr>
            </ww:iterator>
            <ww:if test="/migrationHelper/typesNeedingMigration/size == 0">
                <tr>
                    <td colspan="2"><ww:text name="'admin.selectworkflowscheme.all.issues.automatic'"/></td>
                </tr>
            </ww:if>
            </ww:if>
            <ww:else>
                <tr>
                    <td colspan="2"><ww:text name="'admin.selectworkflows.no.issues.to.migrate'"/></td>
                </tr>
            </ww:else>
        </ww:else>
        <page:param name="cancelURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/workflows</page:param>

        <ui:component name="'projectId'" template="hidden.jsp"/>
        <ui:component name="'schemeId'" template="hidden.jsp"/>
        <ww:if test="origSchemeId != null">
            <ui:component name="'origSchemeId'" template="hidden.jsp"/>
        </ww:if>
    </page:applyDecorator>
</body>
</html>
