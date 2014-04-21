<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="issue_type_screen_scheme"/>
	<title><ww:text name="'admin.itss.configure.issue.type.screen.scheme'"/></title>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.itss.configure.issue.type.screen.scheme'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">issuetype_screenschemes</page:param>
    <page:param name="postTitle">
        <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
            <ui:param name="'projects'" value="/usedIn"/>
        </ui:component>
    </page:param>
    <p>
        <ww:text name="'admin.itss.configure.page.desc'">
            <ww:param name="'value0'"><b id="issue-type-screen-scheme-name"><ww:property value="/issueTypeScreenScheme/name" /></b></ww:param>
        </ww:text>
    </p>
    <p>
        <ww:text name="'admin.itss.configure.instruction'">
            <ww:param name="'value0'"><a href="ViewFieldScreens.jspa"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text>
    </p>
    <p>
        <ww:text name="'admin.itss.configure.instruction2'"/>
    </p>
    <ul class="optionslist">
        <li><ww:text name="'admin.itss.configure.view.all.itss'">
            <ww:param name="'value0'"><b><a id="view_issuetypescreenschemes" href="ViewIssueTypeScreenSchemes.jspa"></ww:param>
            <ww:param name="'value1'"></a></b></ww:param>
        </ww:text></li>
    </ul>
</page:applyDecorator>

<ww:if test="/issueTypeScreenSchemeEntities/empty == false">
<table id="issue-type-table" class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th width="20%">
                <ww:text name="'gadget.filterstats.field.statistictype.issuetype'"/>
            </th>
            <th width="65%">
                <ww:text name="'admin.projects.screen.scheme'"/>
            </th>
            <th width="15%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="/issueTypeScreenSchemeEntities" status="'status'">
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
                    <i><ww:text name="'admin.common.words.default'"/></i>
                    <div class="description"><ww:text name="'admin.itss.configure.used.for.all.unmapped.issue.types'"/></div>
                </ww:else>
            </td>
            <td>
                <ww:property value="./fieldScreenScheme">
                    <a id="configure_fieldscreenscheme" href="ConfigureFieldScreenScheme.jspa?id=<ww:property value="./id" />"><ww:property value="./name" /></a>
                </ww:property>
            </td>
            <td>
                <ul class="operations-list">
                <ww:if test="./issueType == null">
                    <li><a id="edit_issuetypescreenschemeentity_default" href="ViewEditIssueTypeScreenSchemeEntity.jspa?id=<ww:property value="/id" />" title="<ww:text name="'admin.issuefields.fieldconfigschemes.edit.default.mapping'"/>"><ww:text name="'common.words.edit'"/></a></li>
                </ww:if>
                <ww:else>
                    <li><a id="edit_issuetypescreenschemeentity_<ww:property value="./issueType/string('name')" />" href="ViewEditIssueTypeScreenSchemeEntity.jspa?id=<ww:property value="/id" />&issueTypeId=<ww:property value="./issueType/string('id')" />" title="<ww:text name="'admin.issuefields.fieldconfigschemes.edit.entry'"/>"><ww:text name="'common.words.edit'"/></a></li>
                    <li><a id="delete_issuetypescreenschemeentity_<ww:property value="./issueType/string('name')" />" href="<ww:url page="DeleteIssueTypeScreenSchemeEntity.jspa"><ww:param name="'id'" value="/id" /><ww:param name="'issueTypeId'" value="./issueType/string('id')" /></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.delete.entry'"/>"><ww:text name="'common.words.delete'"/></a></li>
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
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'"><ww:text name="'admin.no.issue.mappings.alert'"/></aui:param>
    </aui:component>
</ww:else>

<ww:if test="/addableIssueTypes/empty == false">
    <page:applyDecorator name="jiraform">
        <page:param name="action">AddIssueTypeScreenSchemeEntity.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="title"><ww:text name="'admin.itss.configure.add.issue.type.to.screen.scheme'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <ww:text name="'admin.itss.configure.association.instruction'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"><ww:text name="'common.forms.add'"/></ww:param>
                <ww:param name="'value2'"></b></ww:param>
            </ww:text>
        </page:param>

        <ui:select label="text('common.concepts.issuetype')" name="'issueTypeId'" list="/addableIssueTypes" listKey="'./genericValue/string('id')'" listValue="'./nameTranslation'" />

        <ui:select label="text('admin.menu.issuefields.screen.scheme')" name="'fieldScreenSchemeId'" list="/fieldScreenSchemes" listKey="'./id'" listValue="'./name'">
            <ui:param name="'description'"><ww:text name="'admin.itss.configure.field.configuration.to.use.for.chosen.issue.type'"/></ui:param>
        </ui:select>

        <ui:component name="'id'" template="hidden.jsp" theme="'single'"/>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.itss.configure.add.issue.type.to.screen.scheme'"/></page:param>
        <page:param name="width">100%</page:param>
            <p><ww:text name="'admin.itss.configure.long.instruction'">
                <ww:param name="'value0'"><ww:text name="'common.words.edit'"/></ww:param>
            </ww:text></p>
    </page:applyDecorator>
</ww:else>
<ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
    <ui:param name="'projects'" value="/usedIn"/>
    <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.scheme'"/></ui:param>
</ui:component>

</body>
</html>
