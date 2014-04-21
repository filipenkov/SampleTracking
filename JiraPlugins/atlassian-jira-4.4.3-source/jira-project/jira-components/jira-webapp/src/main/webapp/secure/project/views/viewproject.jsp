<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'common.words.project'"/>: <ww:property value="project/string('name')" /></title>
    <meta name="admin.active.section" content="admin_project_menu/project_section"/>
    <meta name="admin.active.tab" content="view_projects"/>
</head>
<body>
<div class="aui-message warning">
    <span class="aui-icon icon-warning"></span>
    This screen is the process of being replaced.  Please go to <a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="project/string('key')" />/summary">our new Project Administration page</a> to configure your project
</div>

</body>
</html>
