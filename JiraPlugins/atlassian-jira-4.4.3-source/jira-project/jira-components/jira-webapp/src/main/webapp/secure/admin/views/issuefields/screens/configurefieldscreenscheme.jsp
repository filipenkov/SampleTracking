<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screen_scheme"/>
	<title><ww:text name="'admin.issuefields.screenschemes.configure.screen.scheme'"/></title>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.issuefields.screenschemes.configure.screen.scheme'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">fieldscreenschemes</page:param>
    <page:param name="postTitle">
        <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
            <ui:param name="'projects'" value="/usedIn"/>
        </ui:component>
    </page:param>
    <p>
        <ww:text name="'admin.issuefields.screenschemes.configure.page.description'">
            <ww:param name="'value0'"><b id="screen-scheme-name"><ww:property value="/fieldScreenScheme/name" /></b></ww:param>
        </ww:text>
    </p>
    <p>
        <ww:text name="'admin.issuefields.screenschemes.configure.instruction'"/>
    </p>
    <p>
    <ww:text name="'admin.issuefields.screenschemes.configure.instruction2'">
        <ww:param name="'value0'"><a href="ViewIssueTypeScreenSchemes.jspa"><ww:text name="'admin.projects.issue.type.screen.scheme'"/></a></ww:param>
    </ww:text>
    </p>
    <p>
        <ww:text name="'admin.issuefields.screenschemes.note2'">
            <ww:param name="'value0'"><span class="warning"></ww:param>
            <ww:param name="'value1'"></span></ww:param>
            <ww:param name="'value2'"><i></ww:param>
            <ww:param name="'value3'"></i></ww:param>
            <ww:param name="'value4'"><b></ww:param>
            <ww:param name="'value5'"></b></ww:param>
            <ww:param name="'value6'"><a href="ViewCustomFields.jspa"></ww:param>
            <ww:param name="'value7'"></a></ww:param>
        </ww:text>
    </p>
    <ul class="optionslist">
        <li><ww:text name="'admin.issuefields.screenschemes.view.all'">
            <ww:param name="'value0'"><b><a id="view_fieldscreenschemes" href="ViewFieldScreenSchemes.jspa"></ww:param>
            <ww:param name="'value1'"></a></b></ww:param>
        </ww:text></li>
    </ul>
</page:applyDecorator>

<ww:property value="/fieldScreenScheme">
    <ww:if test="./fieldScreenSchemeItems/empty == false">
        <table id="screens-table" class="aui aui-table-rowhover">
            <thead>
                <tr>
                    <th width="20%">
                        <ww:text name="'admin.issuefields.screenschemes.issue.operation'"/>
                    </th>
                    <th width="65%">
                        <ww:text name="'admin.common.words.screen'"/>
                    </th>
                    <th width="15%">
                        <ww:text name="'common.words.operations'"/>
                    </th>
                </tr>
            </thead>
            <tbody>
            <ww:iterator value="./fieldScreenSchemeItems" status="'status'">
                <tr>
                    <td>
                        <ww:if test="./issueOperation == null"><i><ww:property value="/text(./issueOperationName)" /></i><div class="description"><ww:text name="'admin.issuefields.screenschemes.used.for.unmapped'"/></div></ww:if>
                        <ww:else><ww:property value="/text(./issueOperationName)" /></ww:else>
                    </td>
                    <td>
                        <a id="configure_fieldscreen" href="ConfigureFieldScreen.jspa?id=<ww:property value="./fieldScreen/id" />"><ww:property value="./fieldScreen/name" /></a>
                    </td>
                    <td>
                        <ul class="operations-list">
                            <li>
                                <a id="edit_fieldscreenscheme_<ww:property value="text(./issueOperationName)"/>" href="EditFieldScreenSchemeItem!default.jspa?id=<ww:property value="/id" /><ww:if test="./issueOperation">&issueOperationId=<ww:property value="./issueOperation/id" /></ww:if>" title="<ww:text name="'admin.issuefields.screenschemes.edit.value'">
                                <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                                </ww:text>"><ww:text name="'common.words.edit'"/></a>
                            </li>
                            <%-- Only allow to delete non-default scheme entities --%>
                            <ww:if test="./issueOperation">
                                <li>
                                    <a id="delete_fieldscreenscheme_<ww:property value="text(./issueOperationName)"/>" href="<ww:url page="DeleteFieldScreenSchemeItem.jspa"><ww:param name="'id'" value="/id" /><ww:if test="./issueOperation"><ww:param name="'issueOperationId'" value="./issueOperation/id" /></ww:if></ww:url>" title="<ww:text name="'admin.issuefields.screenschemes.delete.value'">
                                    <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                                    </ww:text>"><ww:text name="'common.words.delete'"/></a>
                                </li>
                            </ww:if>
                        </ul>
                    </td>
                </tr>
            </ww:iterator>
            </tbody>
        </table>
    </ww:if>
    <ww:else>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.screenschemes.no.issue.operation'"/></aui:param>
        </aui:component>
    </ww:else>
</ww:property>

<ww:if test="/addableIssueOperations/empty == false && /fieldScreens/empty == false">
    <page:applyDecorator name="jiraform">
        <page:param name="action">AddFieldScreenSchemeItem.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="title"><ww:text name="'admin.issuefields.screenschemes.add.issue.operation'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <ww:text name="'admin.issuefields.screenschemes.association.instruction'">
                <ww:param name="'value0'"><b><ww:text name="'common.forms.add'"/></b></ww:param>
            </ww:text>
        </page:param>

        <ui:select label="text('admin.issuefields.screenschemes.issue.operation')" name="'issueOperationId'" list="/addableIssueOperations" listKey="'/issueOperaionId(.)'" listValue="'/text(./nameKey)'" />

        <ui:select label="text('admin.common.words.screen')" name="'fieldScreenId'" list="/fieldScreens" listKey="'./id'" listValue="'./name'">
            <ui:param name="'description'"><ww:text name="'admin.issuefields.screenschemes.add.description'"/></ui:param>
        </ui:select>

        <ui:component name="'id'" template="hidden.jsp" theme="'single'"/>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.issuefields.screenschemes.add.issue.operation'"/></page:param>
        <page:param name="width">100%</page:param>
        <ww:if test="/addableIssueOperations/empty == true">
            <p><ww:text name="'admin.issuefields.screenschemes.all.operations.have.an.association'"/></p>
        </ww:if>
        <ww:elseIf test="/fieldScreens/empty == true">
            <p><ww:text name="'admin.issuefields.screenschemes.no.screens.exist'">
                <ww:param name="'value0'"><b><a id="create_fieldscreen" href="ViewFieldScreens.jspa"></ww:param>
                <ww:param name="'value1'"></a></b></ww:param>
            </ww:text></p>
        </ww:elseIf>
    </page:applyDecorator>
</ww:else>
<ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
    <ui:param name="'projects'" value="/usedIn"/>
    <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.fields'"/></ui:param>
</ui:component>

</body>
</html>
