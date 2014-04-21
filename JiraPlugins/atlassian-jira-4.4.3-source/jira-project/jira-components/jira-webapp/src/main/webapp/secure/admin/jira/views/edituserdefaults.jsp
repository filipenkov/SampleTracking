<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.userdefaults.edit.user.defaults'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_configuration"/>
    <meta name="admin.active.tab" content="user_defaults"/>     
</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="jiraformId">edit_user_defaults</page:param>
    <page:param name="action">EditUserDefaultSettings.jspa</page:param>
	<page:param name="submitId">edit_user_defaults_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
	<page:param name="cancelURI">ViewUserDefaultSettings.jspa</page:param>
	<page:param name="title"><ww:text name="'admin.userdefaults.user.default.settings'"/></page:param>
	<page:param name="width">100%</page:param>

    <ui:select label="text('admin.userdefaults.outgoing.email.format')" name="'preference'" list="emailFormats" listKey="'id'" listValue="'name'" value="selectedEmailFormat"/>
    <ui:textfield label="text('admin.userdefaults.number.of.issues')" name="'numIssues'" size="'5'" value="applicationProperties/defaultBackedString('user.issues.per.page')"/>
    <ui:radio label="text('admin.userdefaults.notify.users.of.own.changes')" name="'emailUser'" list="booleanList" listKey="'id'" listValue="'name'" value="notifyUser"/>
    <ui:radio label="text('admin.userdefaults.default.share')" name="'sharePublic'" list="shareList" listKey="'id'" listValue="'name'" value="shareValue"/>

</page:applyDecorator>
</body>
</html>
