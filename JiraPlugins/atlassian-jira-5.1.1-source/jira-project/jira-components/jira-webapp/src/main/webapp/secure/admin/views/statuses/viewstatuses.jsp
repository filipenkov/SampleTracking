<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/issue_attributes"/>
    <meta name="admin.active.tab" content="statuses"/>
	<title><ww:text name="'admin.issuesettings.statuses.view.statuses'"/></title>
    <script language="JavaScript">
        function openWindow()
        {
            var vWinUsers = window.open('<%= request.getContextPath() %>/secure/popups/IconPicker.jspa?fieldType=status&formName=jiraform','IconPicker', 'status=no,resizable=yes,top=100,left=200,width=580,height=650,scrollbars=yes');
            vWinUsers.opener = self;
            vWinUsers.focus();
        }
    </script>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.issuesettings.statuses.view.statuses'"/></page:param>
    <page:param name="width">100%</page:param>
    <p><ww:text name="'admin.issuesettings.statuses.the.table.below'"/></p>
    <ww:text name="'admin.issuesettings.statuses.all.statuses.have.one.of.two.modes'"/>
    <ul>
        <li><ww:text name="'admin.issuesettings.statuses.active'">
            <ww:param name="'value0'"><strong class="status-active"><ww:text name="'admin.common.words.active'"/></strong></ww:param>
        </ww:text>
        <li><ww:text name="'admin.issuesettings.statuses.inactive'">
            <ww:param name="'value0'"><strong class="status-inactive"><ww:text name="'admin.common.words.inactive'"/></strong></ww:param>
        </ww:text>
    </ul>
    <p><ww:text name="'admin.issuesettings.statuses.to.delete.a.status'"/></p>
    <ww:if test="/translatable == true">
        <ul class="optionslist">
            <li>
                <b><ww:text name="'admin.issuesettings.statuses.translate.statuses'">
                    <ww:param name="'value0'"><a href="ViewTranslations!default.jspa?issueConstantType=status" id="view-translation"/></ww:param>
                    <ww:param name="'value1'"></a></b></ww:param>
                </ww:text>
            </li>
        </ul>
    </ww:if>
</page:applyDecorator>
<table class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th>
                <ww:text name="'admin.issuesettings.statuses.status.details'"/>
            </th>
            <th>
                <ww:text name="'admin.issuesettings.statuses.mode'"/>
            </th>
            <th>
                <ww:text name="'admin.issuesettings.statuses.workflows'"/>
            </th>
            <th>
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="/constantsManager/statuses" status="'status'">
        <tr>
            <td>
                <ww:component name="'status'" template="constanticon.jsp">
                    <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                    <ww:param name="'iconurl'" value="./string('iconurl')" />
                    <ww:param name="'alt'"><ww:property value="./string('name')" /></ww:param>
                </ww:component>
                <strong><ww:property value="string('name')"/></strong>
                <ww:if test="string('description')/length > 1">
                    <div class="description"><ww:property value="string('description')"/></div>
                </ww:if>
            </td>
            <!-- Mode column - is staus active? -->
            <td>
                <ww:if test="/associatedWorkflows(.)/empty == false">
                    <strong class="status-active"><ww:text name="'admin.common.words.active'"/></strong>
                </ww:if>
                <ww:else>
                    <strong class="status-inactive"><ww:text name="'admin.common.words.inactive'"/></strong>
                </ww:else>
            </td>
            <!-- Workflows column - display associated workflows -->
            <td>
                <ww:if test="/associatedWorkflows(.)/empty == false">
                    <ul>
                    <ww:iterator value="associatedWorkflows(.)" status="'iteratorStatus'">
                        <li><a href="<ww:url page="ViewWorkflowSteps.jspa"><ww:param name="'workflowMode'" value="'live'" /><ww:param name="'workflowName'" value="." /></ww:url>"><ww:property value="."/></a></li>
                    </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    &nbsp;
                </ww:else>
            </td>
            <td>
                <ul class="operations-list">
                    <li><a id="<ww:property value="'edit_' + string('id')"/>" href="EditStatus!default.jspa?id=<ww:property value="string('id')"/>"><ww:text name="'common.words.edit'"/></a></li>
                <ww:if test="/associatedWorkflows(.)/empty == true">
                    <li><a id="del_<ww:property value="string('id')"/>" href="DeleteStatus!default.jspa?id=<ww:property value="string('id')"/>"><ww:text name="'common.words.delete'"/></a></li>
                </ww:if>
                </ul>
            </td>
        </tr>
        </ww:iterator>
    </tbody>
</table>
<aui:component template="module.jsp" theme="'aui'">
    <aui:param name="'contentHtml'">
        <page:applyDecorator name="jiraform">
            <page:param name="action">AddStatus.jspa</page:param>
            <page:param name="submitId">add_submit</page:param>
            <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
            <page:param name="helpURL">statuses</page:param>
            <page:param name="title"><ww:text name="'admin.issuesettings.statuses.add.new.status'"/></page:param>
            <page:param name="width">100%</page:param>

            <ui:textfield label="text('common.words.name')" name="'name'" size="'30'" />

            <ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />

        <ui:component label="text('admin.common.phrases.icon.url')" name="'iconurl'" template="textimagedisabling.jsp">
            <ui:param name="'imagefunction'">openWindow()</ui:param>
            <ui:param name="'size'">60</ui:param>
            <ui:param name="'mandatory'">true</ui:param>
            <ui:param name="'description'"><ww:text name="'admin.common.phrases.relative.to.jira'"/></ui:param>
        </ui:component>
        </page:applyDecorator>
    </aui:param>
</aui:component>
</body>
</html>
