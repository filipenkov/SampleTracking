<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.adduser.create.new.user'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>
    <page:applyDecorator name="jiraform">
        <page:param name="description">
            <p><ww:text name="'admin.adduser.enter.details.below'"/></p>
            <p><ww:text name="'admin.adduser.if.you.do.not.enter.a.password'"/></p>
        </page:param>
        <page:param name="instructions">
        <ww:if test="/hasReachedUserLimit == true">
                <div class="warningBox">
                    <ww:text name="'admin.adduser.user.limit.warning'">
                        <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/ViewLicense!default.jspa"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </div>
                <br>
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
        </page:param>
        <page:param name="title"><ww:text name="'admin.adduser.create.new.user'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="action">AddUser.jspa</page:param>
        <page:param name="submitId">create_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.create'"/></page:param>
        <page:param name="cancelURI">UserBrowser.jspa</page:param>

        <ui:textfield label="text('common.words.username')" name="'username'" size="40" maxlength="255"/>
        <ww:if test="/directories/size > 1">
            <ui:select label="text('admin.user.directory')" name="'directoryId'"
                list="/directories" listKey="'id'" listValue="'name'" onchange="'directoryChanged();'" >
            </ui:select>
        </ww:if>
        <!-- Hide the password fields if the user is being created in a user directory which cannot set a password -->
        <ww:if test="/hasPasswordWritableDirectory == true">
            <ui:password label="text('common.words.password')" name="'password'" size="40">
                <ui:param name="'autocomplete'" value="'off'"/>
            </ui:password>
            <ui:password label="text('common.forms.confirm')" name="'confirm'" size="40">
                <ui:param name="'autocomplete'" value="'off'"/>
            </ui:password>
        </ww:if>
        <ui:textfield label="text('common.words.fullname')" name="'fullname'" size="40" maxlength="255"/>
        <ui:textfield label="text('common.words.email')" name="'email'" size="40" maxlength="255"/>
        <ui:checkbox label="text('admin.adduser.send.password.email')" name="'sendEmail'" fieldValue="'true'">
            <ui:param name="'description'">
                <ww:text name="'admin.adduser.send.password.email.description'"/>
            </ui:param>
        </ui:checkbox>

    </page:applyDecorator>

    <script language="JavaScript" type="text/javascript">

        var canDirectoryUpdatePasswordMap = {};

        function directoryChanged()
        {
            var directorySelect = document.getElementById("directoryId_select");
            if (directorySelect)
            {
                var passwordField = AJS.$("input[name='password']");
                var confirmField = AJS.$("input[name='confirm']");

                var directoryId = directorySelect.options[directorySelect.selectedIndex].value;
                var passwordEnabled = canDirectoryUpdatePasswordMap[directoryId];
                passwordField.attr("disabled", !passwordEnabled);
                confirmField.attr("disabled", !passwordEnabled);
            }
        }

        window.onload = function()
        {
            <ww:iterator value="/canDirectoryUpdatePasswordMap/entrySet">
                canDirectoryUpdatePasswordMap['<ww:property value="./key"/>'] = <ww:property value="./value"/>;
            </ww:iterator>

            directoryChanged();
        }

    </script>

</body>
</html>
