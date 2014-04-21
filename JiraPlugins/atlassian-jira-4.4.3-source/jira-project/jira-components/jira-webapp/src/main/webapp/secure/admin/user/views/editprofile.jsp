
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.editprofile.edit.profile'"/>: <ww:property value="editedUser/displayName" /></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>

<body>

<page:applyDecorator name="jiraform">
	<page:param name="title"><ww:text name="'admin.editprofile.edit.profile'"/>: <ww:property value="editedUser/displayName" /></page:param>
	<page:param name="width">100%</page:param>
	<page:param name="description"><ww:text name="'admin.editprofile.instructions'"/></page:param>
	<page:param name="action">EditUser.jspa</page:param>
	<page:param name="submitId">update_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
	<page:param name="cancelURI"><ww:url value="'ViewUser.jspa'"><ww:param name="'name'" value="editedUser/name" /></ww:url></page:param>

	<ui:textfield label="text('common.words.fullname')" name="'fullName'" size="40" />
	<ui:textfield label="text('common.words.email')" name="'email'"  size="40"/>
	<ui:component name="'editName'" template="hidden.jsp"  theme="'single'" />
</page:applyDecorator>

</body>
</html>
