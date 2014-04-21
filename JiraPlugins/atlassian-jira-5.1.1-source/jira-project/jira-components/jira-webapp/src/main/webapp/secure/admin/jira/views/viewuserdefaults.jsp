<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.userdefaults.user.defaults'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_configuration"/>
    <meta name="admin.active.tab" content="user_defaults"/>
</head>

<body>

<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.userdefaults.user.default.settings'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="titleColspan">2</page:param>
    <page:param name="description">
        <p><ww:text name="'admin.userdefaults.set.default.values'"/></p>
    </page:param>
</page:applyDecorator>

<table id="view_user_defaults" class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th width="40%">
                <ww:text name="'common.words.name'"/>
            </th>
            <th>
                <ww:text name="'admin.common.words.value'"/>
            </th>
            <th width="10%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>
                <b><ww:text name="'admin.userdefaults.outgoing.email.format'"/></b>
            </td>
            <td>
                <ww:property value="applicationProperties/defaultBackedString('user.notifications.mimetype')"/>
            </td>
            <td>
                <ul class="operations-list">
                    <li><a href="SetGlobalEmailPreference!default.jspa" title="<ww:text name="'admin.userdefaults.force.user.defaults'"/>"><ww:text name="'admin.common.words.apply'"/></a></li>
                </ul>
            </td>
        </tr>
        <tr>
            <td>
                <b><ww:text name="'admin.userdefaults.number.of.issues'"/></b>
            </td>
            <td>
                <ww:property value="applicationProperties/defaultBackedString('user.issues.per.page')"/>
            </td>
            <td>
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>
                <b><ww:text name="'admin.userdefaults.notify.users.of.own.changes'"/></b>
            </td>
            <td>
                <ww:if test="applicationProperties/option('user.notify.own.changes') == true">
                    <span class="status-active"><ww:text name="'admin.common.words.yes'"/></span>
                </ww:if>
                <ww:else>
                    <span class="status-inactive"><ww:text name="'admin.common.words.no'"/></span>
                </ww:else>
            </td>
            <td>
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>
                <b><ww:text name="'admin.userdefaults.default.share'"/></b>
            </td>
            <td>
            <ww:if test="applicationProperties/option('user.default.share.private') == false">
                <b><ww:text name="'admin.common.words.public'"/></b>
            </ww:if>
            <ww:else>
                <b><ww:text name="'admin.common.words.private'"/></b>
            </ww:else>
            </td>
            <td>
                &nbsp;
            </td>
        </tr>
    </tbody>
</table>
<div class="buttons-container aui-toolbar form-buttons noprint">
    <div class="toolbar-group">
        <span class="toolbar-item">
            <a class="toolbar-trigger" href="EditUserDefaultSettings!default.jspa"><ww:text name="'admin.userdefaults.edit.default.values'"/></a>
        </span>
    </div>
</div>
</body>
</html>
