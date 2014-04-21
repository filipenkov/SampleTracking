<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.fieldconfigschemes.view.field.configuration.schemes'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="issue_fields"/>
</head>
<body>
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigschemes.view.field.configuration.schemes'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="helpURL">issuefieldschemes</page:param>
        <p>
            <ww:text name="'admin.issuefields.fieldconfigschemes.the.table.below'"/>
        </p>
        <p>
            <ww:text name="'admin.issuefields.fieldconfigschemes.description'">
                <ww:param name="'value0'"><a href="ViewFieldLayouts.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </p>
    </page:applyDecorator>

<ww:if test="fieldLayoutScheme/size == 0">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.fieldconfigschemes.no.field.configuration.schemes.configured'"/></aui:param>
    </aui:component>
</ww:if>
<ww:else>
    <table class="aui">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.words.name'"/>
                </th>
                <th>
                    <ww:text name="'common.concepts.projects'"/>
                </th>
                <th>
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="/fieldLayoutScheme" status="'status'">
            <tr>
                <td>
                    <img src="<%= request.getContextPath() %>/images/icons/form_blue_small.gif" width=16 height=16 title="<ww:text name="'admin.issuefields.fieldconfigurations.field.layout.scheme'"/>" STYLE="float:left; padding-right:3;">
                    <a href="<ww:url page="/secure/admin/ConfigureFieldLayoutScheme!default.jspa"><ww:param name="'id'" value="./id"/></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.change.field.layouts'"/>">
                    <b><ww:property value="./name"/></b>
                    </a>
                    <div class="description">
                        <ww:property value="./description"/>
                    </div>
                </td>
                <td>
                <ww:if test="/schemeProjects(.)/empty == false">
                    <ul>
                    <ww:iterator value="/schemeProjects(.)">
                        <li><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="./string('name')" /></a></li>
                    </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    &nbsp;
                </ww:else>
                </td>
                <td>
                    <ul class="operations-list">
                        <li><a id="configure_<ww:property value="./id"/>" href="<ww:url page="/secure/admin/ConfigureFieldLayoutScheme!default.jspa"><ww:param name="'id'" value="./id"/></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.change.field.layouts'"/>"><ww:text name="'admin.common.words.configure'"/></a></li>
                        <li><a id="copy_<ww:property value="./id"/>" href="<ww:url page="/secure/admin/CopyFieldLayoutScheme!default.jspa"><ww:param name="'id'" value="./id"/></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.create.a.copy'"/>"><ww:text name="'common.words.copy'"/></a></li>
                        <li><a id="edit_<ww:property value="./id"/>" href="<ww:url page="/secure/admin/EditFieldLayoutScheme!default.jspa"><ww:param name="'id'" value="./id"/></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.edit.name.and.description'"/>"><ww:text name="'common.words.edit'"/></a></li>
                    <!-- Scheme can only be deleted if it is not associated with an issue type/project -->
                    <ww:if test="/schemeProjects(.)/empty == true">
                        <li><a id="del_<ww:property value="./name"/>" href="<ww:url page="/secure/admin/DeleteFieldLayoutScheme!default.jspa"><ww:param name="'id'" value="./id"/></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.delete.this.scheme'"/>"><ww:text name="'common.words.delete'"/></a></li>
                    </ww:if>
                    </ul>
                </td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
</ww:else>

<page:applyDecorator name="jiraform">
    <page:param name="action">AddFieldLayoutScheme.jspa</page:param>
    <page:param name="submitId">add_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigschemes.add.field.configuration.scheme'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
        <ww:text name="'admin.issuefields.fieldconfigschemes.add.field.configuration.scheme.instruction'">
            <ww:param name="'value0'"><b></ww:param>
            <ww:param name="'value1'"></b></ww:param>
        </ww:text>
    </page:param>

    <ui:textfield label="text('common.words.name')" name="'fieldLayoutSchemeName'" size="'30'">
        <ui:param name="'mandatory'">true</ui:param>
    </ui:textfield>

    <ui:textfield label="text('common.words.description')" name="'fieldLayoutSchemeDescription'" size="'60'" />
</page:applyDecorator>
</body>
</html>
