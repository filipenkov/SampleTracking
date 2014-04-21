<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.view.user.projectroles.title'" /></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>

<body>
<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.view.user.projectroles.title'" />: <ww:property value="/projectRoleEditUser/displayName"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">users</page:param>
    <page:param name="helpURLFragment">#Assigning+a+User+to+a+Project+Role</page:param>
    <page:param name="description">
        <p>
        <ww:text name="'admin.view.user.projectroles.description.1'">
           <ww:param name="'value0'"><b></ww:param>
           <ww:param name="'value1'"><ww:property value="/projectRoleEditUser/displayName"/></ww:param>
           <ww:param name="'value2'"></b></ww:param>
        </ww:text>
        </p>
        <p>
        <span id="username" class="hidden"><ww:property value="/projectRoleEditUser/name"/></span>
        <img src="<%= request.getContextPath() %>/images/icons/emoticons/user_16.gif" alt="On" title="<ww:text name="'admin.view.user.projectroles.user.direct.member'"/>"/> -
        <ww:text name="'admin.view.user.projectroles.description.2'"/><br/>
        <img src="<%= request.getContextPath() %>/images/icons/emoticons/user_bw_16.gif" alt="Off" title="<ww:text name="'admin.view.user.projectroles.user.not.member'"/>"/> -
        <ww:text name="'admin.view.user.projectroles.description.3'"/><br/>
        <img src="<%= request.getContextPath() %>/images/icons/emoticons/group_16.gif" alt="On" title="<ww:text name="'admin.view.user.projectroles.user.group.member'"/>"/> -
        <ww:text name="'admin.view.user.projectroles.description.4'"/><br/>
        <img src="<%= request.getContextPath() %>/images/icons/emoticons/user_16.gif" alt="On" title="<ww:text name="'admin.view.user.projectroles.user.direct.and.group.member'"/>"/>
        <img src="<%= request.getContextPath() %>/images/icons/emoticons/group_16.gif" alt="On" title="<ww:text name="'admin.view.user.projectroles.user.direct.and.group.member'"/>"/> -
        <ww:text name="'admin.view.user.projectroles.description.5'"/>
        </p>
        <ul class="optionslist">
            <li><a id="return_link" href="<ww:url value="returnUrl"><ww:param name="'name'" value="name"/></ww:url>"><ww:text name="'admin.editusergroups.return.to.viewing.user'"><ww:param name="'value0'">'<ww:property value="/projectRoleEditUser/displayName" />'</ww:param></ww:text></a></li>
        </ul>
    </page:param>
</page:applyDecorator>

<ww:if test="/visibleProjectsByCategory/size != 0">
<table id="projecttable" class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th width="25%">
                <ww:text name="'common.words.project'"/>
            </th>
        <ww:iterator value="allProjectRoles">
            <th width="<ww:property value="/projectRoleColumnWidth"/>%">
                <ww:property value="./name"/>
            </th>
        </ww:iterator>
        </tr>
    </thead>

    <ww:iterator value="/visibleProjectsByCategory">
    <tbody>
        <tr class="totals">
            <td>
                <ww:if test="key != null">
                    <strong><ww:text name="'admin.view.user.projectroles.project.category'"/>:</strong> <ww:property value="key/string('name')"/>
                </ww:if>
                <ww:else>
                    <strong><ww:text name="'admin.view.user.projectroles.project.category.uncategorised'"/></strong>
                </ww:else>
            </td>
        <ww:iterator value="allProjectRoles">
            <td width="<ww:property value="/projectRoleColumnWidth"/>%">
                &nbsp;
            </td>
        </ww:iterator>
        </tr>
    </tbody>
    <tbody>
        <ww:iterator value="value">
            <tr name="project">
                <td>
                    <ww:property value="./name"/>
                </td>
            <ww:iterator value="allProjectRoles">
                <td>
                    <ww:if test="/roleForProjectSelected(., ..) == true && /userInProjectRoleOtherType(., ..) != null">
                        <img src="<%= request.getContextPath() %>/images/icons/emoticons/user_16.gif" alt="On" title="<ww:text name="'admin.view.user.projectroles.user.direct.and.group.member'"/>"/><img src="<%= request.getContextPath() %>/images/icons/emoticons/group_16.gif" alt="On" title="<ww:text name="'admin.view.user.projectroles.user.direct.and.group.member'"/>"/>
                    </ww:if>
                    <ww:elseIf test="/roleForProjectSelected(., ..) == true">
                        <img src="<%= request.getContextPath() %>/images/icons/emoticons/user_16.gif" alt="On" title="<ww:text name="'admin.view.user.projectroles.user.direct.member'"/>"/>
                    </ww:elseIf>
                    <ww:elseIf test="/userInProjectRoleOtherType(., ..) != null">
                        <img src="<%= request.getContextPath() %>/images/icons/emoticons/group_16.gif" alt="On" title="<ww:text name="'admin.view.user.projectroles.user.group.member'"/>"/>
                    </ww:elseIf>
                    <ww:else>
                        <img src="<%= request.getContextPath() %>/images/icons/emoticons/user_bw_16.gif" alt="Off" title="<ww:text name="'admin.view.user.projectroles.user.not.member'"/>"/>
                    </ww:else>
                    <ww:if test="/userInProjectRoleOtherType(., ..) != null">
                        <span class="secondary-text" title="<ww:text name="'admin.view.user.projectroles.group.association'"/>: <ww:property value="/userInProjectRoleOtherType(., ..)"/>">(<ww:property value="/userInProjectRoleOtherType(., ..)"/>)</span>
                    </ww:if>
                </td>
            </ww:iterator>
            </tr>
        </ww:iterator>
        </tbody>
    </ww:iterator>
</table>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'"><ww:text name="'admin.view.user.projectroles.noprojects.found'"/></aui:param>
    </aui:component>
</ww:else>
<div class="buttons-container aui-toolbar form-buttons noprint">
    <div class="toolbar-group">
        <span class="toolbar-item">
            <a class="toolbar-trigger" href="<ww:url page="EditUserProjectRoles!default.jspa"><ww:param name="'name'" value="name" /></ww:url>"><ww:text name="'admin.viewuser.edit.project.roles'"/></a>
        </span>
    </div>
</div>
</body>
</html>
