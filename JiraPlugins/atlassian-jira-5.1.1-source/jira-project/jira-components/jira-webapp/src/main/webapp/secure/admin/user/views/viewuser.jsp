<%@ page import="com.atlassian.jira.ManagerFactory,
                 com.atlassian.jira.config.properties.APKeys"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<ww:if test="user != null">
<ww:property value="user">
<html>
<head>
	<title><ww:text name="'common.words.user'"/>: <ww:property value="displayName" /></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>

<body>
</ww:property>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'common.words.user'"/>: <ww:property value="user/displayName" /> <ww:if test="user/active == false">(<ww:text name="'admin.common.words.inactive'"/>)</ww:if></page:param>
    <page:param name="width">100%</page:param>
    <ww:property value="user">

    <p><a href="<ww:url page="/secure/admin/user/UserBrowser.jspa" atltoken="false"/>"><ww:text name="'admin.userbrowser.return'" /></a></p>

    <ww:if test="/showPasswordUpdateMsg == true">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">success</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.setpassword.success.desc'"><ww:param name="param1"><ww:property value="user/name"/></ww:param></ww:text></p>
            </aui:param>
        </aui:component>
    </ww:if>
    <div id="viewUserDetails" class="vcard"><%-- hCard microformat --%>
        <ul class="item-details">
            <li data-userdata-group="user-details">
                <dl data-userdata-row="username">
                    <dt><ww:text name="'common.words.username'"/>:</dt>
                    <dd id="username"><ww:property value="name" /></dd>
                </dl>
                <dl data-userdata-row="fullname">
                    <dt><ww:text name="'common.words.fullname'"/>:</dt>
                    <dd id="displayName" class="fn"><ww:property value="displayName" /></dd>
                </dl>
                <dl data-userdata-row="email">
                    <dt><ww:text name="'common.words.email'"/>:</dt>
                    <dd><a class="email" href="mailto:<ww:property value="emailAddress" />"><ww:property value="emailAddress" /></a></dd>
                </dl>
            </li>
            <li data-userdata-group="login-details">
                <dl data-userdata-row="login-count">
                    <dt><ww:text name="'login.count'"/>:</dt>
                    <dd id="loginCount"><ww:property value="/loginCount(.)" /></dd>
                </dl>
                <dl data-userdata-row="last-login">
                    <dt><ww:text name="'login.last.login'"/>:</dt>
                    <dd id="lastLogin"><ww:property value="/lastLogin(.)" /></dd>
                </dl>
                <dl data-userdata-row="previous-login">
                    <dt><ww:text name="'login.prev.login'"/>:</dt>
                    <dd id="previousLogin"><ww:property value="/previousLogin(.)" /></dd>
                </dl>
            <ww:if test="/elevatedSecurityCheckRequired(.) == true">
                <dl data-userdata-row="elevated-security-check-required">
                    <dt>&nbsp;</dt>
                    <dd><em><ww:text name="'login.elevated.security.check.required'"/></em></dd>
                </dl>
            </ww:if>
                <dl data-userdata-row="last-failed-login">
                    <dt><ww:text name="'login.last.failed.login'"/>:</dt>
                    <dd id="lastFailedLogin"><ww:property value="/lastFailedLogin(.)" /></dd>
                </dl>
                <dl data-userdata-row="current-failed-login-count">
                    <dt><ww:text name="'login.current.failed.login.count'"/>:</dt>
                    <dd id="currentFailedLoginCount"><ww:property value="/currentFailedLoginCount(.)" /></dd>
                </dl>
                <dl data-userdata-row="total-failed-login-count">
                    <dt><ww:text name="'login.total.failed.login.count'"/>:</dt>
                    <dd id="totalFailedLoginCount"><ww:property value="/totalFailedLoginCount(.)" /></dd>
                </dl>
            </li>
            <li data-userdata-row="user-directory">
                <dl data-userdata-row="directory">
                    <dt><ww:text name="'admin.user.directory'"/>:</dt>
                    <dd id="directory"><ww:property value="/directoryName" /></dd>
                </dl>
                <dl data-userdata-row="groups">
                    <dt><ww:text name="'common.words.groups'"/>:</dt>
                    <dd id="groups" class="user-group-info">
                        <ww:if test="userGroups/empty==true">
                            <aui:component template="auimessage.jsp" theme="'aui'">
                                <aui:param name="'messageType'">warning</aui:param>
                                <aui:param name="'messageHtml'">
                                    <p>
                                        <ww:text name="'admin.viewuser.user.not.in.group'">
                                            <ww:param name="param0"><ww:property value="user/displayName" /></ww:param>
                                        </ww:text>
                                    </p>
                                </aui:param>
                            </aui:component>
                        </ww:if>
                        <ww:else>
                            <ul>
                            <ww:iterator value="userGroups">
                                <li><ww:property value="." /></li>
                            </ww:iterator>
                            </ul>
                        </ww:else>
                    </dd>
                </dl>
            </li>
        </ul>
    <ww:if test="/userProperties != null && /userProperties/empty == false">
        <h3><ww:text name="'common.words.properties'"/>:</h3>
        <ul class="item-details">
            <li>
                <ww:iterator value="/userProperties">
                <dl>
                    <dt><ww:property value="key" />:</dt>
                    <dd><ww:property value="value" /></dd>
                </dl>
                </ww:iterator>
            </li>
        </ul>
    </ww:if>
    </div>
    </ww:property>
    <ww:if test="/remoteUserPermittedToEditSelectedUser == false">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'"><p><ww:text name="'admin.viewuser.user.is.sysadmin.and.you.are.admin'"/></p></aui:param>
        </aui:component>
    </ww:if>
</page:applyDecorator>

<ww:property value="user">
	<ul class="operations-list">
        <li><a href="<ww:url page="/secure/ViewProfile.jspa"><ww:param name="'name'" value="name"/></ww:url>"><ww:text name="'admin.viewuser.view.public.profile'"/></a></li>
        <li><a href="<ww:url page="EditUserProperties.jspa"><ww:param name="'name'" value="name" /></ww:url>"><ww:text name="'admin.viewuser.edit.properties'"/></a></li>
        <ww:if test="/selectedUsersGroupsEditable == true">
            <li><a id="editgroups_link" href="<ww:url page="EditUserGroups!default.jspa"><ww:param name="'name'" value="name" /></ww:url>"><ww:text name="'admin.viewuser.edit.groups'"/></a></li>
        </ww:if>
        <li><a id="viewprojectroles_link" href="<ww:url page="ViewUserProjectRoles!default.jspa"><ww:param name="'name'" value="name" /><ww:param name="'returnUrl'" value="'ViewUser.jspa'"/></ww:url>"><ww:text name="'admin.viewuser.view.project.roles'"/></a></li>
    <ww:if test="/selectedUserEditable == true">
        <li><a href="<ww:url page="EditUser!default.jspa"><ww:param name="'editName'" value="name" /></ww:url>"><ww:text name="'admin.viewuser.edit.details'"/></a></li>
        <ww:if test="/canUpdateUserPassword == true">
            <li><a href="<ww:url page="SetPassword!default.jspa"><ww:param name="'name'" value="name" /></ww:url>"><ww:text name="'admin.viewuser.set.password'"/></a></li>
        </ww:if>
        <li><a id="rememberme_link" href="<ww:url page="UserRememberMeCookies!default.jspa"><ww:param name="'name'" value="name" /></ww:url>"><ww:text name="'admin.viewuser.rememberme.user'"/></a></li>
        <li><a id="deleteuser_link" href="<ww:url page="DeleteUser!default.jspa"><ww:param name="'name'" value="name" /></ww:url>"><ww:text name="'admin.viewuser.delete.user'"/></a></li>
    </ww:if>
    <ww:if test="/elevatedSecurityCheckRequired(.) == true">
        <li><a href="<ww:url page="ResetFailedLoginCount.jspa"><ww:param name="'name'" value="name" /><ww:param name="'returnUrl'" value="'secure/admin/user/ViewUser.jspa'"/></ww:url>"><ww:text name="'admin.resetfailedlogin.title'"/></a></li>
    </ww:if>
    </ul>
</ww:property>
</body>
</html>
</ww:if>
<ww:else>
<html>
<head>
	<title><ww:text name="'common.words.user'"/>: <ww:text name="'common.words.none'"/></title>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.viewuser.user.does.not.exist.title'" /></page:param>
    <page:param name="width">100%</page:param>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'admin.viewuser.user.does.not.exist'">
                    <ww:param name="'value0'"><strong><ww:property value="name" /></strong></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>
</page:applyDecorator>
</body>
</html>
</ww:else>
