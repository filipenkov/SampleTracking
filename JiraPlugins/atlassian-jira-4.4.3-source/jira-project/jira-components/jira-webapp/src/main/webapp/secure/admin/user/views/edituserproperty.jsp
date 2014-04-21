<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.edituserproperties.edit.property'"><ww:param name="'value0'" value="/key"/></ww:text></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>

<p>
<page:applyDecorator name="jiraform">
    <page:param name="action">EditUserProperty!update.jspa</page:param>
    <page:param name="submitId">update_submit</page:param>
    <page:param name="submitName"> <ww:text name="'common.forms.update'"/> </page:param>
    <page:param name="title"><ww:text name="'admin.edituserproperties.edit.property'"><ww:param name="'value0'" value="/key"/></ww:text></page:param>
    <page:param name="cancelURI"><ww:url page="EditUserProperties.jspa"><ww:param name="'name'" value="name"/></ww:url></page:param>
    <page:param name="width">90%</page:param>
    <page:param name="autoSelectFirst">true</page:param>
    <page:param name="description">
        <ww:text name="'admin.edituserproperty.description'">
            <ww:param name="'value0'"><b><ww:property value="/key"/></b></ww:param>
            <ww:param name="'value0'"><b><ww:property value="/user/displayName"/></b></ww:param>
        </ww:text>
    </page:param>

    <ui:textfield label="text('common.words.value')" name="'value'" size="20" />

    <ui:component name="'name'" template="hidden.jsp" theme="'single'" />
    <ui:component name="'key'" template="hidden.jsp" theme="'single'" />

</page:applyDecorator>
</p>

</body>
</html>
