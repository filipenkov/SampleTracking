<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<html>
<head>
    <ww:if test="remoteUser == null"><meta name="decorator" content="message" /></ww:if>
    <title><ww:text name="'common.concepts.projects'"/></title>
    <meta name="admin.active.section" content="admin_project_menu/project_section"/>
    <meta name="admin.active.tab" content="view_projects"/>
</head>
<body>
<ww:if test="remoteUser != null">
    <ww:if test = "/admin == true">
        <div class="operations-panel">
            <ul class="operations">
                <li>
                    <a id="add_project" class="operations-item add-project-trigger" href="<%= request.getContextPath() %>/secure/admin/AddProject!default.jspa">
                        <span class="icon icon-add16"></span>
                        <ww:text name="'admin.projects.add.project'"/>
                    </a>
                </li>
            </ul>
        </div>
    </ww:if>
    <ww:if test="projects/size != 0">
        <table id="project-list" class="aui">
            <thead>
                <tr>
                    <th scope="col"></th>
                    <th scope="col"><ww:text name="'common.words.name'"/></th>
                    <th scope="col"><ww:text name="'issue.field.key'"/></th>
                    <th scope="col"><ww:text name="'common.concepts.url'"/></th>
                    <th scope="col"><ww:text name="'common.concepts.projectlead'"/></th>
                    <th scope="col"><ww:text name="'admin.projects.default.assignee'"/></th>
                    <th scope="col"><ww:text name="'common.words.operations'"/></th>
                </tr>
            </thead>
            <tbody>
                <ww:iterator value="projects">
                    <tr data-project-key="<ww:property value="./string('key')" />">
                        <td class="view-projects-avatar" data-cell-type="avatar">
                            <ww:if test="./long('avatar') != null">
                                <img alt="" class="project-avatar-16" height="16" src="<%= request.getContextPath() %>/secure/projectavatar?size=small&pid=<ww:property value="./long('id')"/>&avatarId=<ww:property value="./long('avatar')"/>" width="16" />
                            </ww:if>
                        </td>
                        <td data-cell-type="name">
                            <a id="view-project-<ww:property value="long('id')" />" href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="string('name')" /></a>
                        </td>
                        <td data-cell-type="key"><ww:property value="string('key')"/></td>
                        <td data-cell-type="url">
                            <ww:if test="/stringSet(.,'url') == false">
                                <ww:text name="'browse.projects.no.url'"/>
                            </ww:if>
                            <ww:else>
                                <a href="<ww:property value="./string('url')" />" title="<ww:property value="./string('url')" />"><ww:property value="/abbreviateString(./string('url'), 60)" /></a>
                            </ww:else>
                        </td>
                        <td data-cell-type="lead">
                            <ww:if test="string('lead')">
                                <jira:formatuser user="./string('lead')" type="'profileLink'" id="'view_' + ./string('key') + '_projects'"/>
                            </ww:if>
                            <ww:else>
                               <ww:text name="'browse.projects.no.lead'"/>
                            </ww:else>
                        </td>
                        <td data-cell-type="default-assignee">
                            <ww:if test="/defaultAssigneeAssignable(.) == false"><span class="warning" title="WARNING: User not Assignable."></ww:if>
                            <ww:text name="/prettyAssigneeType(long('assigneetype'))"/>
                            <ww:if test="/defaultAssigneeAssignable(.) == false"></span></ww:if>
                        </td>
                        <td data-cell-type="operations">
                            <ul class="operations-list">
                            <ww:if test="/projectAdmin(.) == true || /admin == true">
                                <li><a class="edit-project" id="edit-project-<ww:property value="long('id')" />" href="<%= request.getContextPath() %>/secure/project/EditProject!default.jspa?pid=<ww:property value="long('id')" />&returnUrl=ViewProjects.jspa"><ww:text name="'common.words.edit'"/></a></li>
                            </ww:if>
                            <ww:if test = "/admin == true">
                                <li><a id="delete_project_<ww:property value="long('id')"/>" href="<%=request.getContextPath() %>/secure/project/DeleteProject!default.jspa?pid=<ww:property value="long('id')"/>&returnUrl=ViewProjects.jspa"><ww:text name="'common.words.delete'"/></a></li>
                            </ww:if>
                            </ul>
                        </td>
                    </tr>
                </ww:iterator>
            </tbody>
        </table>
    </ww:if>
    <ww:else>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'id'" value="'noprojects'"/>
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.projects.nopermission'"/></p>
            </aui:param>
        </aui:component>
    </ww:else>
</ww:if>
<ww:else>
    <h1><ww:text name="'login.required.title'" /></h1>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:if test="/allowSignUp == true">
                    <ww:text name="'admin.projects.login.or.signup'">
                        <ww:param name="'value0'"><jira:loginlink><ww:text name="'admin.common.words.log.in'"/></jira:loginlink></ww:param>
                        <ww:param name="'value1'"><a href="<%= request.getContextPath() %>/secure/Signup!default.jspa"></ww:param>
                        <ww:param name="'value2'"></a></ww:param>
                    </ww:text>
                </ww:if>
                <ww:else>
                    <ww:text name="'admin.projects.login'">
                        <ww:param name="'value0'"><jira:loginlink><ww:text name="'admin.common.words.log.in'"/></jira:loginlink></ww:param>
                    </ww:text>
                </ww:else>
            </p>
        </aui:param>
    </aui:component>
</ww:else>
</body>
</html>
