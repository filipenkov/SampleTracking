<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.adduser.add.new.user'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
    <meta name="decorator" content="panel-admin"/>
</head>
<body>
    <page:applyDecorator id="user-create" name="auiform">
        <page:param name="action">AddUser.jspa</page:param>
        <page:param name="method">post</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.create'"/></page:param>
        <page:param name="submitButtonName"><ww:text name="'common.forms.create'"/></page:param>
        <page:param name="cancelLinkURI">UserBrowser.jspa</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.adduser.add.new.user'"/></aui:param>
        </aui:component>

        <ww:if test="/hasReachedUserLimit == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.adduser.user.limit.warning'">
                            <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/ViewLicense!default.jspa"></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'admin.userbrowser.how.many.users'">
                        <ww:param name="'value0'"><ww:property value="/userUtil/totalUserCount"/></ww:param>
                        <ww:param name="'value1'"><ww:property value="/userUtil/activeUserCount"/></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.username')" mandatory="true" maxlength="255" id="'username'" name="'username'" theme="'aui'" />
        </page:applyDecorator>
        <ww:if test="/directories/size > 1">
            <page:applyDecorator name="auifieldgroup">
                <aui:select label="text('admin.user.directory')" id="'directoryId'" name="'directoryId'" list="/directories" listKey="'id'" listValue="'name'" theme="'aui'" />
            </page:applyDecorator>
        </ww:if>

        <!-- Hide the password fields if the user is being created in a user directory which cannot set a password -->
        <ww:if test="/hasPasswordWritableDirectory == true">
            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'admin.adduser.if.you.do.not.enter.a.password'"/></page:param>
                <aui:password label="text('common.words.password')" id="'password'" name="'password'" theme="'aui'">
                    <aui:param name="'autocomplete'" value="'off'"/>
                </aui:password>
            </page:applyDecorator>
            <page:applyDecorator name="auifieldgroup">
                <aui:password label="text('common.forms.confirm')" id="'confirm'" name="'confirm'" theme="'aui'">
                    <aui:param name="'autocomplete'" value="'off'"/>
                </aui:password>
            </page:applyDecorator>
        </ww:if>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.fullname')" id="'fullname'" mandatory="true" maxlength="255" name="'fullname'" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.email')" id="'email'" mandatory="true" maxlength="255" name="'email'" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.adduser.send.password.email.description'"/></page:param>
            <page:param name="cssClass">checkbox</page:param>
            <aui:checkbox label="text('admin.adduser.send.password.email')"  id="'sendEmail'" name="'sendEmail'" fieldValue="'true'" theme="'aui'">
                <aui:param name="'labelBefore'">false</aui:param>
            </aui:checkbox>
        </page:applyDecorator>

        <ww:property value="/webPanelHtml" escape="false"/>

    </page:applyDecorator>
    <ww:if test="/directories/size > 1">
    <script>

        var canDirectoryUpdatePasswordMap = {};

        <ww:iterator value="/canDirectoryUpdatePasswordMap/entrySet">
            canDirectoryUpdatePasswordMap['<ww:property value="./key"/>'] = <ww:property value="./value"/>;
        </ww:iterator>
    
    // Disable the password fields depending on the Directory option selected
    function directoryChanged() {
        var directorySelect = AJS.$('#user-create-directoryId');
        if (directorySelect)
        {
            var passwordField = AJS.$("#user-create-password");
            var confirmField = AJS.$("#user-create-confirm");

            var directoryId = directorySelect.val();
            var passwordEnabled = canDirectoryUpdatePasswordMap[directoryId];

            passwordField.attr("disabled", !passwordEnabled);
            confirmField.attr("disabled", !passwordEnabled);
        }
    }

    AJS.$('#user-create-directoryId').change(directoryChanged).change();

</script>
</ww:if>
</body>
</html>
