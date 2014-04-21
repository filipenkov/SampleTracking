<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.edituserproperties.edit.properties'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>

<page:applyDecorator name="jirapanel">
<page:param name="action">EditUserProperties.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.edituserproperties.edit.properties'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description"> </page:param>
    <page:param name="title"><ww:text name="'admin.edituserproperties.edit.properties'"/>: <ww:property value="/user/displayName" /></page:param>

    <p><ww:text name="'admin.edituserproperties.page.description'">
        <ww:param name="'value0'"><b><ww:property value="/user/displayName"/></b></ww:param>
    </ww:text></p>
    <p><ww:text name="'admin.edituserproperties.available.properties.description'"/></p>

    <ul class="optionslist">
        <li><a id="view_user" href="<ww:url page="ViewUser.jspa"><ww:param name="'name'" value="name"/></ww:url>"><ww:text name="'admin.userbrowser.view.user'"/></a></li>
    </ul>
</page:applyDecorator>

<ww:if test="userProperties != null && userProperties/size > 0">
    <table class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th width="40%">
                    <ww:text name="'common.words.key'"/>
                </th>
                <th width="40%">
                    <ww:text name="'common.words.value'"/>
                </th>
                <th>
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="userProperties">
            <tr>
                <td>
                    <ww:property value="key" />
                </td>
                <td>
                    <ww:property value="value" />
                </td>
                <td>
                    <ul class="operations-list">
                        <li><a id="edit_<ww:property value="key"/>" href="<ww:url page="EditUserProperty.jspa"><ww:param name="'name'" value="user/name" /><ww:param name="'key'" value="key" /></ww:url>"><ww:text name="'admin.common.words.edit'"/></a></li>
                        <li><a href="<ww:url page="DeleteUserProperty!default.jspa"><ww:param name="'name'" value="user/name" /><ww:param name="'key'" value="key"/></ww:url>" id="delete_<ww:property value="key"/>"><ww:text name="'admin.common.words.delete'"/></a></li>
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
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'admin.edituserproperties.user.has.no.properties'">
                    <ww:param name="'value0'"><b><ww:property value="/user/displayName"/></b></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>
</ww:else>
<page:applyDecorator name="jiraform">
    <page:param name="action">EditUserProperties!add.jspa</page:param>
    <page:param name="submitId">submit_add</page:param>
    <page:param name="submitName"> <ww:text name="'common.forms.add'"/> </page:param>
    <page:param name="title"><ww:text name="'admin.edituserproperties.add.property'"/></page:param>
    <page:param name="description"><ww:text name="'admin.edituserproperties.example'"/></page:param>

    <ui:textfield label="text('common.words.key')" name="'key'" size="20" />

    <ui:textfield label="text('common.words.value')" name="'value'" size="20" />

    <ui:component name="'name'" template="hidden.jsp" theme="'single'" />

</page:applyDecorator>
</body>
</html>
