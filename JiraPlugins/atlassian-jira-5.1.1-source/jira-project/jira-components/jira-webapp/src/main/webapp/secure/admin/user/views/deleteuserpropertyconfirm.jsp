<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.deleteuserproperty.delete.property'"><ww:param name="'value0'"><ww:property value="/key"/></ww:param></ww:text></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>

<body>
	<page:applyDecorator name="jiraform">
		<page:param name="description">
            <p>
                <ww:text name="'admin.deleteuserproperty.description'">
                    <ww:param name="'value0'"><b><ww:property value="/key"/></b></ww:param>
                    <ww:param name="'value0'"><b><ww:property value="/user/displayName"/></b></ww:param>
                </ww:text>
            </p>

        </page:param>
        <page:param name="title"><ww:text name="'admin.deleteuserproperty.delete.property'"><ww:param name="'value0'"><ww:property value="/key"/></ww:param></ww:text></page:param>
		<page:param name="labelWidth">50%</page:param>
        <page:param name="width">100%</page:param>
		<page:param name="action">DeleteUserProperty.jspa</page:param>
		<page:param name="submitId">delete_submit</page:param>
		<page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
		<page:param name="cancelURI"><ww:url page="EditUserProperties.jspa"><ww:param name="'name'" value="name"/></ww:url></page:param>

		<ui:component name="'name'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'key'" template="hidden.jsp" theme="'single'" />
        <ui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'single'"  />

    </page:applyDecorator>
</body>
</html>
