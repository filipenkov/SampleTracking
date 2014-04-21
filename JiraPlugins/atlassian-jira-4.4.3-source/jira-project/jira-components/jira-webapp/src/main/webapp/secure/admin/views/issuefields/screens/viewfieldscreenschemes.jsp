<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screen_scheme"/>
	<title><ww:text name="'admin.issuefields.screenschemes.view.screen.schemes'"/></title>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.issuefields.screenschemes.view.screen.schemes'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">fieldscreenschemes</page:param>
    <p>
        <ww:text name="'admin.issuefields.screenschemes.the.table.below'"/>
    </p>
    <p>
        <ww:text name="'admin.issuefields.screenschemes.you.can.add'"/>
        <ww:text name="'admin.issuefields.screenschemes.screens.schemes.are.mapped'">
            <ww:param name="'value0'"><a href="ViewIssueTypeScreenSchemes.jspa"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text>
    </p>
    <p>
        <ww:text name="'admin.issuefields.screenschemes.note1'">
            <ww:param name="'value0'"><span class="note"></ww:param>
            <ww:param name="'value1'"></span></ww:param>
        </ww:text>
    </p>
</page:applyDecorator>

<ww:if test="/fieldScreenSchemes/empty == false">
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
                    <ww:text name="'admin.menu.issuefields.issue.type.screen.schemes'"/>
                </th>
                <th width="15%">
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="/fieldScreenSchemes" status="'status'">
            <tr>
                <td >
                    <ww:property value="./name" />
                </td>
                <td>
                    <ww:property value="./description" />
                </td>
                <td>
                    <ww:if test="/issueTypeScreenSchemes(.)/empty == false">
                        <ul>
                        <ww:iterator value="/issueTypeScreenSchemes(.)">
                            <li><a id="configure_issuetypescreenscheme" href="ConfigureIssueTypeScreenScheme.jspa?id=<ww:property value="./id" />"><ww:property value="./name" /></a></li>
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
                            <a id="configure_fieldscreenscheme_<ww:property value="./name" />" href="ConfigureFieldScreenScheme.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screenschemes.configure.screens.for'"><ww:param name="'value0'"><ww:property value="./name" /></ww:param></ww:text>">
                                <ww:text name="'admin.common.words.configure'"/>
                            </a>
                        </li>
                        <li>
                            <a id="edit_fieldscreenscheme_<ww:property value="./name" />" href="EditFieldScreenScheme!default.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screenschemes.edit.value'"><ww:param name="'value0'"><ww:property value="./name" /></ww:param></ww:text>">
                                <ww:text name="'common.words.edit'"/>
                            </a>
                        </li>
                        <li>
                            <a id="copy_fieldscreenscheme_<ww:property value="./name" />" href="ViewCopyFieldScreenScheme.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screenschemes.copy.value'"><ww:param name="'value0'"><ww:property value="./name" /></ww:param></ww:text>">
                                <ww:text name="'common.words.copy'"/>
                            </a>
                        </li>
                    <ww:if test="/issueTypeScreenSchemes(.)/empty == true">
                        <li>
                            <a id="delete_fieldscreenscheme_<ww:property value="./name" />" href="ViewDeleteFieldScreenScheme.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screenschemes.delete.value'"><ww:param name="'value0'"><ww:property value="./name" /></ww:param></ww:text>">
                                <ww:text name="'common.words.delete'"/>
                            </a>
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
        <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.screenschemes.no.screen.schemes.configured'"/></aui:param>
    </aui:component>
</ww:else>

<page:applyDecorator name="jiraform">
    <page:param name="action">AddFieldScreenScheme.jspa</page:param>
    <page:param name="submitId">add_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.screenschemes.add.screen.scheme'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
        <ww:text name="'admin.issuefields.screenschemes.add.instruction'">
            <ww:param name="'value0'"><b><ww:text name="'common.forms.add'"/></b></ww:param>
        </ww:text>
    </page:param>

    <ui:textfield label="text('common.words.name')" name="'fieldScreenSchemeName'" size="'30'">
        <ui:param name="'mandatory'">true</ui:param>
    </ui:textfield>

    <ui:textfield label="text('common.words.description')" name="'fieldScreenSchemeDescription'" size="'60'" />

    <ui:select label="text('admin.issuefields.screenschemes.default.screen')" name="'fieldScreenId'" list="/fieldScreens" listKey="'./id'" listValue="'./name'">
        <ui:param name="'description'"><ww:text name="'admin.issuefields.screenschemes.default.screen.description'"/></ui:param>
    </ui:select>
</page:applyDecorator>

</body>
</html>
