<%@ page import="com.atlassian.jira.ManagerFactory,
                 com.atlassian.jira.config.properties.APKeys"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.menu.usersandgroups.user.browser'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>
<% boolean isExternalUserManagementDisabled = !ManagerFactory.getApplicationProperties().getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT); %>

<page:applyDecorator name="jirapanel">
    <% if (isExternalUserManagementDisabled) { %>
    <div class="operations-panel">
        <ul class="operations">
            <li>
                <a href="<%= request.getContextPath() %>/secure/admin/user/AddUser!default.jspa" class="operations-item" id="add_user">
                    <span class="icon icon-add16"></span>
                    <ww:text name="'admin.userbrowser.add.user'"/>
                </a>
            </li>
        </ul>
    </div>
<% } %>
    <page:param name="title"><ww:text name="'admin.menu.usersandgroups.user.browser'"/></page:param>

        <p>
            <ww:text name="'admin.userbrowser.description'"/>
        </p>
        <ww:if test="/hasReachedUserLimit == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.userbrowser.user.limit.warning'">
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


</page:applyDecorator>
<form class="aui top-label filter-form" action="UserBrowser.jspa" method="post">
    <ww:component name="'atl_token'" value="/xsrfToken" template="hidden.jsp"/>
    <h3><ww:text name="'admin.userbrowser.filter.users'"/></h3>
    <div class="content-body">
        <ww:property value="filter">
            <div class="field-group">
                <table>
                    <tr>
                        <td><ww:text name="'admin.userbrowser.users.per.page'"/></td>
                        <td><ww:text name="'admin.userbrowser.username.contains'"/></td>
                        <td><ww:text name="'admin.userbrowser.userfullname.contains'"/></td>
                        <td><ww:text name="'admin.userbrowser.email.contains'"/></td>
                        <td><ww:text name="'admin.userbrowser.in.group'"/></td>
                    </tr>
                    <tr>
                <ui:select label="text('admin.userbrowser.users.per.page')" name="'max'" theme="'single'" list="/maxValues" listKey="'.'" listValue="'.'" >
                    <ui:param name="'headerrow'" value="text('common.words.all')" />
                    <ui:param name="'headervalue'" value="'1000000'" />
                </ui:select>
                <ui:textfield label="text('admin.userbrowser.username.contains')" name="'userNameFilter'" size="15" theme="'single'" />
                <ui:textfield label="text('admin.userbrowser.userfullname.contains')" name="'fullNameFilter'" size="15" theme="'single'" />
                <ui:textfield label="text('admin.userbrowser.email.contains')" name="'emailFilter'" size="15" theme="'single'" />
                <ui:select label="text('admin.userbrowser.in.group')" name="'group'" theme="'single'" list="/groups" listKey="'name'" listValue="'name'">
                    <ui:param name="'headerrow'" value="text('common.filters.any')" />
                    <ui:param name="'headervalue'" value="''" />
                </ui:select>
                    </tr>
                </table>
            </div>
        </ww:property>
    </div>
    <div class="buttons-container content-footer">
        <div class="buttons">
            <input id="filter_link" class="button" type="submit" value="<ww:text name="'navigator.tabs.filter'"/>">
            <a class="cancel" href="UserBrowser.jspa?emailFilter=&group=&max=<ww:property value="filter/max"/>"><ww:text name="'admin.userbrowser.reset.filter'"/></a>
        </div>
    </div>
</form>
<p class="results-count">
    <ww:text name="'admin.userbrowser.displaying.users'">
        <ww:param name="'value0'"><span id="results-count-start"><ww:property value="niceStart" /></span></ww:param>
        <ww:param name="'value1'"><span><ww:property value="niceEnd" /></span></ww:param>
        <ww:param name="'value2'"><strong id="results-count-total"><ww:property value="users/size" /></strong></ww:param>
    </ww:text>
</p>
<p class="pagination">
    <ww:if test="filter/start > 0">
		<a class="icon icon-previous" href="<ww:url page="UserBrowser.jspa"><ww:param name="'start'" value="filter/previousStart" /></ww:url>"><span>&lt;&lt; <ww:text name="'common.forms.previous'"/></span></a>
	</ww:if>
    <ww:property value = "pager/pages(/browsableItems)">
        <ww:if test="size > 1">
            <ww:iterator value="." status="'pagerStatus'">
                <ww:if test="currentPage == true"><strong><ww:property value="pageNumber" /></strong></ww:if>
                <ww:else>
                    <a href="<ww:url page="UserBrowser.jspa"><ww:param name="'start'" value="start" /></ww:url>"><ww:property value="pageNumber" /></a>
                </ww:else>
            </ww:iterator>
        </ww:if>
    </ww:property>
	<ww:if test="filter/end < users/size">
		<a class="icon icon-next" href="<ww:url page="UserBrowser.jspa"><ww:param name="'start'" value="filter/nextStart" /></ww:url>"><span><ww:text name="'common.forms.next'"/> &gt;&gt;</span></a>
	</ww:if>
</p>
<table class="aui aui-table-rowhover" id="user_browser_table">
    <thead>
        <tr>
            <th>
                <ww:text name="'common.words.username'"/>
            </th>
            <th>
                <ww:text name="'common.words.fullname'"/>
            </th>
            <th>
                <ww:text name="'login.details'"/>
            </th>
            <th>
                <ww:text name="'common.words.groups'"/>
            </th>
            <th>
                <ww:text name="'admin.user.directory'"/>
            </th>
            <th class="minNoWrap">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="currentPage" status="'status'">
    <tr class="vcard">
        <td><div><a id="<ww:property value="name"/>" <ww:if test="/userAvatarEnabled == true">rel="<ww:property value="name"/>" class="user-hover user-avatar" style="background-image:url(<ww:property value="/avatarUrl(name)"/>);"</ww:if> href="<ww:url page="ViewUser.jspa"><ww:param name="'name'" value="name"/></ww:url>"><span class="username"><ww:property value="name"/></span></a></div></td>
        <td>
            <span class="fn"><ww:property value="displayName"/></span><br />
            <a href="<ww:url page="ViewUser.jspa"><ww:param name="'name'" value="name"/></ww:url>"><span class="email"><ww:property value="emailAddress"/></span></a>
        </td>
        <td class="minNoWrap">
            <ww:if test="/everLoggedIn(.) == true">
                <strong><ww:text name="'common.concepts.count'"/>:</strong> <ww:property value="/loginCount(.)" /><br />
                <strong><ww:text name="'common.concepts.last'"/>:</strong> <ww:property value="/lastLogin(.)" /><br />
                <br />
            </ww:if>
            <ww:else>
                <ww:text name="'login.not.recorded'"/>
            </ww:else>
            <ww:if test="/elevatedSecurityCheckRequired(.) == true">
                <strong><i><ww:text name="'login.elevated.security.check.required'"/></i></strong><br />
                <strong><ww:text name="'login.last.failed.login'"/>:</strong> <span id="lastFailedLogin"><ww:property value="/lastFailedLogin(.)" /></span><br />
                <strong><ww:text name="'login.current.failed.login.count'"/>:</strong> <span id="currentFailedLoginCount"><ww:property value="/currentFailedLoginCount(.)" /></span><br />
                <strong><ww:text name="'login.total.failed.login.count'"/>:</strong> <span id="totalFailedLoginCount"><ww:property value="/totalFailedLoginCount(.)" /></span><br />
                <a href="<ww:url page="ResetFailedLoginCount.jspa"><ww:param name="'name'" value="name" /><ww:param name="'returnUrl'" value="'secure/admin/user/UserBrowser.jspa'"/></ww:url>"><ww:text name="'admin.resetfailedlogin.title'"/></a>
            </ww:if>
        </td>
        <td>
            <ul>
            <ww:iterator value="/groupsForUser(.)">
                <li><a href="<ww:url page="ViewGroup.jspa"><ww:param name="'name'" value="."/></ww:url>"><ww:property value="."/></a></li>
            </ww:iterator>
            </ul>
        </td>
        <td><ww:property value="/directoryForUser(.)"/></td>
        <td>
            <ul class="operations-list">
            <% if (isExternalUserManagementDisabled) { %>
                <ww:if test="/remoteUserPermittedToEditSelectedUsersGroups(.) == true">
                    <li><a class="editgroups_link" id="editgroups_<ww:property value="name"/>" href="<ww:url page="EditUserGroups!default.jspa"><ww:param name="'name'" value="name" /><ww:param name="'returnUrl'" value="'UserBrowser.jspa'" /></ww:url>"><ww:text name="'common.words.groups'"/></a></li>
                </ww:if>
            <%  }%>
            <li><a id="projectroles_link_<ww:property value="name"/>" href="<ww:url page="ViewUserProjectRoles!default.jspa"><ww:param name="'name'" value="name" /><ww:param name="'returnUrl'" value="'UserBrowser.jspa'" /></ww:url>"><ww:text name="'common.words.project.roles'"/></a></li>
            <% if (isExternalUserManagementDisabled) { %>
                <ww:if test="/remoteUserPermittedToEditSelectedUser(.) == true">
                    <li><a id="edituser_link_<ww:property value="name"/>" href="<ww:url page="EditUser!default.jspa"><ww:param name="'editName'" value="name" /><ww:param name="'returnUrl'" value="'UserBrowser.jspa'" /></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                    <li><a id="deleteuser_link_<ww:property value="name"/>" href="<ww:url page="DeleteUser!default.jspa"><ww:param name="'name'" value="name" /><ww:param name="'returnUrl'" value="'UserBrowser.jspa'" /></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                </ww:if>
            <%  }%>
            </ul>
        </td>
    </tr>
    </ww:iterator>
    </tbody>
</table>
<p class="pagination">
    <ww:if test="filter/start > 0">
		<a class="icon icon-previous" href="<ww:url page="UserBrowser.jspa"><ww:param name="'start'" value="filter/previousStart" /></ww:url>"><span>&lt;&lt; <ww:text name="'common.forms.previous'"/></span></a>
	</ww:if>
    <ww:property value = "pager/pages(/browsableItems)">
        <ww:if test="size > 1">
            <ww:iterator value="." status="'pagerStatus'">
                <ww:if test="currentPage == true"><strong><ww:property value="pageNumber" /></strong></ww:if>
                <ww:else>
                    <a href="<ww:url page="UserBrowser.jspa"><ww:param name="'start'" value="start" /></ww:url>"><ww:property value="pageNumber" /></a>
                </ww:else>
            </ww:iterator>
        </ww:if>
    </ww:property>
	<ww:if test="filter/end < users/size">
		<a class="icon icon-next" href="<ww:url page="UserBrowser.jspa"><ww:param name="'start'" value="filter/nextStart" /></ww:url>"><span><ww:text name="'common.forms.next'"/> &gt;&gt;</span></a>
	</ww:if>
</p>
</body>
</html>
