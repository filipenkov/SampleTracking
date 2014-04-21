
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.projects.cvsmodules.select.project.repository'"/></title>
    <meta name="admin.active.section" content="atl.jira.proj.config"/>
</head>
<body>

<ww:if test="repositories == null || repositories/size == 0">
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.projects.cvsmodules.no.version.control'"/></page:param>
        <page:param name="width">100%</page:param>
        <p>
        <ww:text name="'admin.projects.cvsmodules.no.version.control.description'"/>
        </p>
        <p>
        <img src="<%= request.getContextPath() %>/images/icons/bullet_creme.gif" height=8 width=8 border=0>
        <ww:text name="'admin.projects.cvsmodules.add.new.module'">
            <ww:param name="'value0'"><a id="cvs-no-modules" href="<%= request.getContextPath() %>/secure/admin/projectcategories/AddRepository!default.jspa"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
        </ww:text>
        </p>
    </page:applyDecorator>
</ww:if>
<ww:else>
	<page:applyDecorator name="jiraform">
		<page:param name="title"><ww:text name="'admin.projects.cvsmodules.select.version.control.modules'"/></page:param>
        <page:param name="description">
            <ww:text name="'admin.projects.cvsmodules.page.description'"/>
            <p><ww:text name="'admin.projects.cvsmodules.note'">
                <ww:param name="'value0'"><b><font color="#990000"></ww:param>
                <ww:param name="'value1'"></font></b></ww:param>
            </ww:text></p>
        </page:param>
        <page:param name="width">100%</page:param>
        <page:param name="cancelURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/summary</page:param>
        <page:param name="action">EnterpriseSelectProjectRepository.jspa</page:param>
        <page:param name="submitId">select_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.select'"/></page:param>

        <ui:select label="text('admin.cvsmodules.repositories')" name="'multipleRepositoryIds'" list="repositories" listKey="'id'" listValue="'name'" template="selectmultiple.jsp">
            <ui:param name="'headerrow'" value="'None'" />
            <ui:param name="'headervalue'" value="-1" />
        </ui:select>
        <ui:component name="'projectId'" template="hidden.jsp"/>
	</page:applyDecorator>
</ww:else>

</body>
</html>
