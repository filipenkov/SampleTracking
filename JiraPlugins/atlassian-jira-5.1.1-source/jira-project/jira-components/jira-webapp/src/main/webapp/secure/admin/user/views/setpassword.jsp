<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.setpassword.set.password'"/>: <ww:property value="user/displayName" /></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>

<body>

<page:applyDecorator name="jiraform">
	<page:param name="title"><ww:text name="'admin.setpassword.set.password'"/>: <ww:property value="user/displayName" /></page:param>
	<page:param name="width">100%</page:param>
	<page:param name="description"><ww:text name="'admin.setpassword.instruction'"/></page:param>
	<page:param name="action">SetPassword.jspa</page:param>
	<page:param name="submitId">update_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
	<page:param name="cancelURI"><ww:url value="'ViewUser.jspa'"><ww:param name="'name'" value="user/name" /></ww:url></page:param>

	<ui:password label="text('common.words.password')" name="'password'" size="12">
        <ui:param name="'autocomplete'" value="'off'"/>
    </ui:password>

    <ui:password label="text('common.forms.confirm')" name="'confirm'"  size="12">
        <ui:param name="'autocomplete'" value="'off'"/>
    </ui:password>

    <ui:component name="'name'" template="hidden.jsp" theme="'single'"  />
</page:applyDecorator>

</body>
</html>
