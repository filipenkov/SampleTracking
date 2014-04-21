<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.fieldconfigschemes.configure.field.configuration.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="issue_fields"/>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigschemes.configure.field.configuration.scheme'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">fieldscreenschemes</page:param>
    <page:param name="postTitle">
        <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
            <ui:param name="'projects'" value="/usedIn"/>
        </ui:component>
    </page:param>
    <p>
        <ww:text name="'admin.issuefields.fieldconfigschemes.configure.description'">
            <ww:param name="'value0'"><b id="field-scheme-name"><ww:property value="/fieldLayoutScheme/name" /></b></ww:param>
        </ww:text>
    </p>
    <p><ww:text name="'admin.issuefields.fieldconfigschemes.configure.instructions'"/></p>
    <p><ww:text name="'admin.issuefields.fieldconfigschemes.scheme.association'"/></p>
    <ul class="optionslist">
        <li><ww:text name="'admin.issuefields.fieldconfigschemes.view.all.field.layout.schemes'">
            <ww:param name="'value0'"><a id="view_fieldlayoutschemes" href="ViewFieldLayoutSchemes.jspa"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text></li>
    </ul>
</page:applyDecorator>
<ww:if test="/fieldLayoutSchemeItems/empty == false">
    <table id="scheme_entries" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th width="20%">
                    <ww:text name="'common.concepts.issuetype'"/>
                </th>
                <th width="65%">
                    <ww:text name="'admin.issuefields.fieldconfigschemes.field.configuration'"/>
                </th>
                <th width="15%">
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
            <ww:iterator value="/fieldLayoutSchemeItems" status="'status'">
            <ww:if test="/shouldDisplay(.) == true">
            <tr>
                <td>
                    <ww:if test="./issueType">
                        <ww:property value="./issueTypeObject">
                            <ww:component name="'issuetype'" template="constanticon.jsp">
                                <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                <ww:param name="'iconurl'" value="./iconUrl" />
                                <ww:param name="'alt'"><ww:property value="./nameTranslation" /></ww:param>
                                <ww:param name="'title'"><ww:property value="./nameTranslation" /> - <ww:property value="./descTranslation" /></ww:param>
                            </ww:component>
                            <ww:property value="./nameTranslation" />
                        </ww:property>
                    </ww:if>
                    <ww:else>
                        <span class="field-name"><ww:text name="'admin.common.words.default'"/></span>
                        <p class="field-description fieldDescription"><ww:text name="'admin.issuefields.fieldconfigschemes.used.for.all.unmapped.issue.types'"/></p>
                    </ww:else>
                </td>
                <td>
                    <ww:property value="/fieldLayout(./fieldLayoutId)">
                        <ww:if test="./type">
                            <a id="configure_fieldlayout" href="ViewIssueFields.jspa"><ww:property value="./name" /></a>
                        </ww:if>
                        <ww:else>
                            <a id="configure_fieldlayout" href="ConfigureFieldLayout.jspa?id=<ww:property value="./id" />"><ww:property value="./name" /></a>
                        </ww:else>
                    </ww:property>
                </td>
                <td>
                    <ul class="operations-list">
                    <ww:if test="./issueType == null">
                        <li><a id="edit_fieldlayoutschemeentity" href="<ww:url page="/secure/admin/ViewEditFieldLayoutSchemeEntity.jspa"><ww:param name="'id'" value="/id"/></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.edit.default.mapping'"/>"><ww:text name="'common.words.edit'"/></a></li>
                    </ww:if>
                    <ww:else>
                        <li><a id="edit_fieldlayoutschemeentity_<ww:property value="./issueType/string('id')"/>" href="<ww:url page="/secure/admin/ViewEditFieldLayoutSchemeEntity.jspa"><ww:param name="'id'" value="/id"/><ww:param name="'issueTypeId'" value="./issueType/string('id')" /></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.edit.entry'"/>"><ww:text name="'common.words.edit'"/></a></li>
                        <li><a id="delete_fieldlayoutschemeentity_<ww:property value="./issueType/string('id')" />" href="<ww:url page="/secure/admin/DeleteFieldLayoutSchemeEntity.jspa"><ww:param name="'id'" value="/id"/><ww:param name="'issueTypeId'" value="./issueType/string('id')" /></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.delete.entry'"/>"><ww:text name="'common.words.delete'"/></a></li>
                    </ww:else>
                    </ul>
                </td>
            </tr>
            </ww:if>
            </ww:iterator>
        </tbody>
    </table>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'"><ww:text name="'admin.no.issue.mappings.alert'"/></aui:param>
    </aui:component>
</ww:else>
<ww:if test="/addableIssueTypes/empty == false">
    <page:applyDecorator name="jiraform">
        <page:param name="action">AddFieldLayoutSchemeEntity.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigschemes.add.issue.type'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <ww:text name="'admin.issuefields.fieldconfigschemes.associate.instruction'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
            </ww:text>
        </page:param>
        <ui:select label="text('common.concepts.issuetype')" name="'issueTypeId'" list="/addableIssueTypes" listKey="'./genericValue/string('id')'" listValue="'./nameTranslation'" />
        <ui:select label="text('admin.issuefields.fieldconfigschemes.field.configuration')" name="'fieldConfigurationId'" list="/fieldLayouts" listKey="'/fieldLayoutId(.)'" listValue="'./name'">
            <ui:param name="'description'"><ww:text name="'admin.issuefields.fieldconfigschemes.field.configuration.chosen'"/></ui:param>
        </ui:select>
        <ui:component name="'id'" template="hidden.jsp" theme="'single'"/>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigschemes.add.issue.type'"/></page:param>
        <page:param name="width">100%</page:param>
            <p><ww:text name="'admin.issuefields.fieldconfigschemes.all.issue.types.have.associations'"/></p>
    </page:applyDecorator>
</ww:else>
<ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
    <ui:param name="'projects'" value="/usedIn"/>
    <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.scheme'"/></ui:param>
</ui:component>

</body>
</html>