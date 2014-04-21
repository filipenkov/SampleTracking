<%@ page import="java.util.*,
                 com.atlassian.jira.config.properties.APKeys,
                 com.atlassian.jira.ManagerFactory"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.menu.usersandgroups.group.browser'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="group_browser"/>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.menu.usersandgroups.group.browser'"/></page:param>
    <page:param name="helpURL">groups</page:param>
    <p><ww:text name="'admin.usersandgroups.group.browser.description.1'"/>
    <ww:if test="/externalUserManagementEnabled == false">
        <ww:text name="'admin.usersandgroups.group.browser.description.2'"/>
        <ww:text name="'admin.usersandgroups.group.browser.description.3'"/>
    </ww:if>
    </p>
    <ww:if test="/externalUserManagementEnabled == false">
        <ul class="optionslist">
            <li><a id="bulk_edit_groups" href="BulkEditUserGroups!default.jspa"><ww:text name="'admin.bulkeditgroups.title'"/></a></li>
            <ww:if test="/nestedGroupsEnabledForAnyDirectory == true">
                <li><a id="edit_nested_groups" href="EditNestedGroups!default.jspa"><ww:text name="'admin.editnestedgroups.title'"/></a></li>
            </ww:if>
        </ul>
    </ww:if>
</page:applyDecorator>
<form class="aui top-label filter-form" action="GroupBrowser.jspa" method="post">
    <h3><ww:text name="'admin.usersandgroups.filter.group'"/></h3>
    <div class="content-body">
        <ww:property value="filter">
            <div class="field-group">
                <table>
                    <tr>
                        <td><ww:text name="'admin.usersandgroups.groups.per.page'"/></td>
                        <td><ww:text name="'admin.usersandgroups.name.contains'"/></td>
                    </tr>
                    <tr>
                        <ui:select label="text('admin.usersandgroups.groups.per.page')"  name="'max'" theme="'single'" list="/maxValues" listKey="'.'" listValue="'.'" >
                            <ui:param name="'headerrow'" value="''" />
                        </ui:select>
                        <ui:textfield label="text('admin.usersandgroups.name.contains')"  name="'nameFilter'" theme="'single'" size="20" />
                    </tr>
                </table>
            </div>
        </ww:property>
    </div>
    <div class="buttons-container content-footer">
        <div class="buttons">
            <input type="submit" class="button" value="<ww:text name="'admin.usersandgroups.filter'"/>">
            <a class="cancel" href="GroupBrowser.jspa?nameFilter="><ww:text name="'admin.usersandgroups.reset.filter'"/></a>
        </div>
    </div>
</form>
<div class="results-count">
    <ww:text name="'admin.usersandgroups.displaying.x.to.y.of.z'">
        <ww:param name="'value0'"><span id="results-count-start"><ww:property value="niceStart" /></span></ww:param>
        <ww:param name="'value1'"><span><ww:property value="niceEnd" /></span></ww:param>
        <ww:param name="'value2'"><strong id="results-count-total"><ww:property value="browsableItems/size" /></strong></ww:param>
    </ww:text>
</div>
<p class="pagination">
    <ww:if test="filter/start > 0">
		<a class="icon icon-previous" title="<ww:text name="'common.forms.previous'"/>" href="<ww:url page="GroupBrowser.jspa"><ww:param name="'start'" value="filter/previousStart" /><ww:param name="'max'" value="filter/max" /><ww:param name="'nameFilter'" value="filter/nameFilter"/></ww:url>"><span>&lt&lt; <ww:text name="'common.forms.previous'"/></span></a>
	</ww:if>
	<ww:if test="filter/end < browsableItems/size">
		<a class="icon icon-next" title="<ww:text name="'common.forms.next'"/>" href="<ww:url page="GroupBrowser.jspa"><ww:param name="'start'" value="filter/nextStart" /><ww:param name="'max'" value="filter/max" /><ww:param name="'nameFilter'" value="filter/nameFilter"/></ww:url>"><span><ww:text name="'common.forms.next'"/> &gt;&gt;</span></a>
	</ww:if>
</p>
<table id="group_browser_table" class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th width="40%">
                <ww:text name="'admin.usersandgroups.group.name'"/>
            </th>
            <th width="20%">
                <ww:text name="'admin.common.words.users'"/>
            </th>
            <th width="30%">
                <ww:text name="'admin.menu.schemes.permission.schemes'"/>
            </th>
            <ww:if test="/externalUserManagementEnabled == false">
                <th width="10%">
                    <ww:text name="'common.words.operations'"/>
                </th>
            </ww:if>
        </tr>
    </thead>
    <tbody>
	<ww:iterator value="currentPage" status="'status'">
	<tr>
		<td>
            <a href="<ww:url page="ViewGroup.jspa">
		    <ww:param name="'name'" value="name"/></ww:url>"><ww:property value="name"/></a>
        </td>
		<td>
            <a href="<ww:url value="'UserBrowser.jspa'" ><ww:param name="'group'" value="name" /><ww:param name="'emailFilter'" value="''" /></ww:url>"><ww:property value="users/size"/></a>
        </td>
        <td>
            <ww:if test="/permissionSchemes(./name)/empty == false">
                <ul>
                <ww:iterator value="/permissionSchemes(./name)">
                   <li><a href="<%= request.getContextPath() %>/secure/admin/EditPermissions!default.jspa?schemeId=<ww:property value="./long('id')"/>"><ww:property value="./string('name')" /></a></li>
                </ww:iterator>
                </ul>
            </ww:if>
            <ww:else>
                &nbsp;
            </ww:else>
        </td>
        <ww:if test="/externalUserManagementEnabled == false && /userAbleToDeleteGroup(name) == true">
            <td>
                <ul class="operations-list">
                    <li><a id="del_<ww:property value="name"/>" href="<ww:url value="'DeleteGroup!default.jspa'" ><ww:param name="'name'" value="name" /></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                    <li><a id="edit_members_of_<ww:property value="name"/>" href="<ww:url value="'BulkEditUserGroups!default.jspa'" ><ww:param name="'selectedGroupsStr'" value="name" /></ww:url>"><ww:text name="'admin.usersandgroups.edit.members'"/></a></li>
                </ul>
            </td>
        </ww:if>
    </tr>
	</ww:iterator>
    </tbody>
</table>
<p class="pagination">
    <ww:if test="filter/start > 0">
		<a class="icon icon-previous" title="<ww:text name="'common.forms.previous'"/>" href="<ww:url page="GroupBrowser.jspa"><ww:param name="'start'" value="filter/previousStart" /><ww:param name="'max'" value="filter/max" /><ww:param name="'nameFilter'" value="filter/nameFilter"/></ww:url>"><span>&lt&lt; <ww:text name="'common.forms.previous'"/></span></a>
	</ww:if>
	<ww:if test="filter/end < browsableItems/size">
		<a class="icon icon-next" title="<ww:text name="'common.forms.next'"/>" href="<ww:url page="GroupBrowser.jspa"><ww:param name="'start'" value="filter/nextStart" /><ww:param name="'max'" value="filter/max" /><ww:param name="'nameFilter'" value="filter/nameFilter"/></ww:url>"><span><ww:text name="'common.forms.next'"/> &gt;&gt;</span></a>
	</ww:if>
</p>
<ww:if test="/externalUserManagementEnabled == false">
    <form action="GroupBrowser!add.jspa" method="post" id="add-group" class="aui add-group">
        <ww:component name="'atl_token'" value="/xsrfToken" template="hidden.jsp"/>
        <h3><ww:text name="'admin.usersandgroups.add.group'"/></h3>
        <div class="content-body">
            <div class="field-group">
                <ui:textfield label="text('common.words.name')" name="'addName'" theme="'aui'" size="20" />
            </div>
            <div class="buttons-container content-footer">
                <div class="buttons">
                    <input type="submit" class="button" name="add_group" value="<ww:text name="'admin.usersandgroups.add.group'"/>">
                </div>
            </div>
        </div>
    </form>
</ww:if>
</body>
</html>
