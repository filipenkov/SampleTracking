<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="issue_type_screen_scheme"/>
	<title><ww:text name="'admin.issuefields.issuetypescreenschemes.view.issue.type.screen.schemes'"/></title>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.issuefields.issuetypescreenschemes.view.issue.type.screen.schemes'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">issuetype_screenschemes</page:param>
    <p>
        <ww:text name="'admin.issuefields.issuetypescreenschemes.the.table.below'">
            <ww:param name="'value0'"><a href="ViewFieldScreenSchemes.jspa"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
            <ww:param name="'value2'"><a href="ViewFieldScreens.jspa"></ww:param>
            <ww:param name="'value3'"></a></ww:param>
        </ww:text>
    </p>
    <p>
        <ww:text name="'admin.issuefields.issuetypescreenschemes.instruction'"/>
    </p>
    <p>
        <ww:text name="'admin.issuefields.issuetypescreenschemes.note'">
            <ww:param name="'value0'"><span class="note"></ww:param>
            <ww:param name="'value1'"></span></ww:param>
        </ww:text>

    </p>
</page:applyDecorator>

<ww:if test="/issueTypeScreenSchemes/empty == false">
<table class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th width="20%">
                <ww:text name="'common.words.name'"/>
            </th>
            <th width="40%">
                <ww:text name="'common.words.description'"/>
            </th>
            <th width="25%">
                <ww:text name="'common.concepts.projects'"/>
            </th>
            <th width="15%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="/issueTypeScreenSchemes" status="'status'">
        <tr class="<ww:if test="@status/odd == true">rowNormal</ww:if><ww:else>rowAlternate</ww:else>">
            <td>
                <ww:property value="./name" />
            </td>
            <td>
                <ww:property value="./description" />
            </td>
            <td>
                <ww:if test="/projects(.)/empty == false">
                    <ul>
                    <ww:iterator value="/projects(.)">
                        <li><a id="view_project" href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="./string('name')" /></a></li>
                    </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    &nbsp;
                </ww:else>
            </td>
            <td>
                <ul class="operations-list">
                    <li>
                        <a id="configure_issuetypescreenscheme_<ww:property value="./name" />" href="ConfigureIssueTypeScreenScheme.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.issuetypescreenschemes.configure.the.scheme'">
                        <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                        </ww:text>"><ww:text name="'admin.common.words.configure'"/></a>
                    </li>
                    <li>
                        <a id="edit_issuetypescreenscheme_<ww:property value="./name" />" href="EditIssueTypeScreenScheme!default.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.issuetypescreenschemes.edit'">
                        <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                        </ww:text>"><ww:text name="'common.words.edit'"/></a>
                    </li>
                    <li>
                        <a id="copy_issuetypescreenscheme_<ww:property value="./name" />" href="ViewCopyIssueTypeScreenScheme.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.issuetypescreenschemes.copy.entry'">
                        <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                        </ww:text>"><ww:text name="'common.words.copy'"/></a>
                    </li>
                <ww:if test="/projects(.)/empty == true && ./default == false">
                    <li>
                        <a id="delete_issuetypescreenscheme_<ww:property value="./name" />" href="<ww:url page="ViewDeleteIssueTypeScreenScheme.jspa"><ww:param name="'id'" value="./id" /></ww:url>" title="<ww:text name="'admin.issuefields.issuetypescreenschemes.delete.entry'">
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
        <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.issuetypescreenschemes.no.screen.schemes.configured'"/></aui:param>
    </aui:component>
</ww:else>

<page:applyDecorator name="jiraform">
    <page:param name="action">AddIssueTypeScreenScheme.jspa</page:param>
    <page:param name="submitId">add_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.issuetypescreenschemes.add'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">issuetype_screenschemes</page:param>
    <page:param name="description">
        <ww:text name="'admin.issuefields.issuetypescreenschemes.add.instruction'">
            <ww:param name="'value0'"><b><ww:text name="'common.forms.add'"/></b></ww:param>
        </ww:text>
    </page:param>

    <ui:textfield label="text('common.words.name')" name="'schemeName'" size="'30'">
        <ui:param name="'mandatory'">true</ui:param>
    </ui:textfield>

    <ui:textfield label="text('common.words.description')" name="'schemeDescription'" size="'60'" />

    <ui:select label="text('admin.projects.screen.scheme')" name="'fieldScreenSchemeId'" list="/fieldScreenSchemes" listKey="'./id'" listValue="'./name'">
        <ui:param name="'mandatory'">true</ui:param>
        <ui:param name="'description'"><ww:text name="'admin.issuefields.issuetypescreenschemes.screen.scheme.description'"/></ui:param>
    </ui:select>
</page:applyDecorator>
</body>
</html>
