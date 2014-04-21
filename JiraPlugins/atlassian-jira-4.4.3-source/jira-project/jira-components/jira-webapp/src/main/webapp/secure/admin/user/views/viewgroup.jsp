<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.viewgroup.title'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="group_browser"/>
</head>
<body>
<ww:if test="group != null">
    <p><a href="<ww:url page="/secure/admin/user/GroupBrowser.jspa" atltoken="false"/>"><ww:text name="'admin.groupbrowser.return'" /></a></p>

    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.common.words.group'"/>: <ww:property value="group/name" /></page:param>
        <page:param name="width">100%</page:param>
        <ww:property value="group">
        <div class="view-group-container">
            <ul class="item-details">
                <li>
                    <dl>
                        <dt><ww:text name="'admin.usersandgroups.group.name'"/></dt>
                        <dd><ww:property value="./name" /></dd>
                    </dl>
                </li>
                <li>
                    <dl>
                        <dt><ww:property value="./users/size"/> <ww:text name="'admin.common.words.users'"/></dt>
                        <dd>
                            <ul class="operations-list">
                                <li><a id="view_group_members" href="<ww:url value="'UserBrowser.jspa'" ><ww:param name="'emailFilter'" value="''" /><ww:param name="'group'" value="name" /></ww:url>"><ww:text name="'common.words.view'"/></a></li>
                                <ww:if test="/userAbleToDeleteGroup(./name) == true">
                                <li><a id="edit_members_of_<ww:property value="name"/>" href="<ww:url value="'BulkEditUserGroups!default.jspa'" ><ww:param name="'selectedGroupsStr'" value="name" /></ww:url>"><ww:text name="'admin.usersandgroups.edit.members'"/></a></li>
                                </ww:if>
                            </ul>
                        </dd>
                    </dl>
                </li>
            </ul>


            <h4><ww:text name="'admin.schemes.permissions.permission.schemes'"/></h4>
            <ww:property value="/permissionSchemes(./name)">
                <ul>
                <ww:if test="./empty == true">
                    <li>
                    <ww:text name="'admin.viewgroup.not.associated.with.group'">
                        <ww:param name="'value0'"><b><ww:text name="'admin.schemes.permissions.permission.schemes'"/></b></ww:param>
                    </ww:text>
                    </li>
                </ww:if>
                <ww:else>
                    <ww:iterator value=".">
                        <li><a href="<%= request.getContextPath() %>/secure/admin/EditPermissions!default.jspa?schemeId=<ww:property value="./long('id')"/>"><ww:property value="./string('name')" /></a></li>
                    </ww:iterator>
                </ww:else>
                </ul>
            </ww:property>


            <h4><ww:text name="'admin.menu.schemes.notification.schemes'"/></h4>
            <ww:property value="/notificationSchemes(./name)">
                <ul>
                <ww:if test="./empty == true">
                    <li>
                    <ww:text name="'admin.viewgroup.not.associated.with.group'">
                        <ww:param name="'value0'"><b><ww:text name="'admin.schemes.notifications.notification.schemes'"/></b></ww:param>
                    </ww:text>
                    </li>
                </ww:if>
                <ww:else>
                    <ww:iterator value=".">
                        <li><a href="<%= request.getContextPath() %>/secure/admin/EditNotifications!default.jspa?schemeId=<ww:property value="./long('id')"/>"><ww:property value="./string('name')" /></a></li>
                    </ww:iterator>
                </ww:else>
                </ul>
            </ww:property>


            <h4><ww:text name="'admin.schemes.issuesecurity.issue.security.schemes'"/></h4>
            <ww:property value="/issueSecuritySchemes(./name)">
                <ul>
                <ww:if test="./empty == true">
                    <li>
                    <ww:text name="'admin.viewgroup.not.associated.with.group'">
                        <ww:param name="'value0'"><b><ww:text name="'admin.schemes.issuesecurity.issue.security.schemes'"/></b></ww:param>
                    </ww:text>
                    </li>
                </ww:if>
                <ww:else>
                    <ww:iterator value=".">
                        <li><a href="<%= request.getContextPath() %>/secure/admin/EditIssueSecurities!default.jspa?schemeId=<ww:property value="./long('id')"/>"><ww:property value="./string('name')" /></a></li>
                    </ww:iterator>
                </ww:else>
                </ul>
            </ww:property>


            <h4><ww:text name="'admin.viewgroup.savedfilters.title'"/></h4>
            <ww:property value="/savedFilters(.)">
                <ul>
                <ww:if test="./empty == true">
                    <li>
                    <ww:text name="'admin.viewgroup.not.associated.with.group'">
                        <ww:param name="'value0'"><b><ww:text name="'admin.viewgroup.savedfilters.title'"/></b></ww:param>
                    </ww:text>
                    </li>
                </ww:if>
                <ww:else>
                    <ww:iterator value=".">
                        <li><a href="<%= request.getContextPath() %>/secure/IssueNavigator.jspa?mode=hide&requestId=<ww:property value="./id"/>"><ww:property value="./name" /></a> (<ww:text name="'admin.common.words.owner'"/>: <a href="<%= request.getContextPath() %>/secure/admin/user/ViewUser.jspa?name=<ww:property value="./ownerUserName"/>"><ww:property value="/fullUserName(./ownerUserName)"/></a>)</li>
                    </ww:iterator>
                </ww:else>
                </ul>
            </ww:property>
        </div>
        </ww:property>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.viewgroup.group.does.not.exist.title'"/></page:param>
        <page:param name="width">100%</page:param>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'"><p><ww:text name="'admin.viewgroup.group.does.not.exist'"/></p></aui:param>
            </aui:component>
    </page:applyDecorator>
</ww:else>
</body>
</html>
