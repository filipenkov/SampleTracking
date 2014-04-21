<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<html>
<head>
	<title><ww:text name="'admin.projectroles.delete.name'"/>: <ww:property value="/role/name" /></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="project_role_browser"/>
</head>
<body>
<page:applyDecorator name="jiraform">
	<page:param name="title"><ww:text name="'admin.projectroles.delete.name'"/>: <ww:property value="/role/name" /></page:param>
	<page:param name="description">
        <p><ww:text name="'admin.projectroles.delete.confirm'">
           <ww:param name="'value0'"><ww:property value="/role/name"/></ww:param>
        </ww:text>
        </p>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'"><ww:text name="'admin.projectroles.delete.warning'"/></aui:param>
        </aui:component>
    </page:param>
	<page:param name="autoSelectFirst">false</page:param>
	<page:param name="columns">1</page:param>
    <page:param name="width">100%</page:param>
    <page:param name="action">DeleteProjectRole.jspa</page:param>
	<page:param name="submitId">delete_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
	<page:param name="cancelURI">ViewProjectRoles.jspa</page:param>
    <page:param name="helpURL">project_roles</page:param>
    <page:param name="helpURLFragment">#Deleting+a+project+role</page:param>
    <input type="hidden" value="<ww:property value="/role/id"/>" name="id"/>
    <tr>
        <td>
            <jsp:include page="associatedschemestables.jsp"/>
        </td>
    </tr>
</page:applyDecorator>
</body>
</html>
