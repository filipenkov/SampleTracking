
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.projects.select.project.category'"/></title>
    <meta name="admin.active.section" content="atl.jira.proj.config"/>
</head>

<body>

<ww:if test="projectCategories == null || projectCategories/size == 0">
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.projects.no.project.category'"/></page:param>
        <page:param name="width">100%</page:param>

        <p>
        <ww:text name="'admin.projects.no.categories.created'"/>
        </p>
        <p>
        <ww:text name="'admin.projects.add.new.project.category'">
            <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/projectcategories/ViewProjectCategories!default.jspa"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text>
        </p>
    </page:applyDecorator>
</ww:if>
<ww:else>
	<page:applyDecorator name="jiraform">
		<page:param name="title"><ww:text name="'admin.projects.select.project.category'"/></page:param>
        <page:param name="description">
            <ww:text name="'admin.projects.select.category.description'"/>
        </page:param>
        <page:param name="width">100%</page:param>
        <page:param name="cancelURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/summary</page:param>
        <page:param name="action">SelectProjectCategory.jspa</page:param>
        <page:param name="submitId">select_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.select'"/></page:param>

        <ui:select label="text('portlet.projects.field.project.category.name')" name="'pcid'" list="projectCategories" listKey="'string('id')'" listValue="'string('name')'" template="selectmap.jsp">
            <ui:param name="'headerrow'" value="'None'" />
            <ui:param name="'headervalue'" value="-1" />
        </ui:select>
        <ui:component name="'pid'" template="hidden.jsp"/>
	</page:applyDecorator>
</ww:else>

</body>
</html>
