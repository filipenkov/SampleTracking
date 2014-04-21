<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.cvsmodules.select.repository'"/></title>
    <ww:text name="'admin.iss.associate.security.scheme.to.project'"/>
</head>
<body>

<ww:if test="repositories == null || repositories/size == 0">
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.cvsmodules.select.none.available'"/></page:param>
        <page:param name="width">100%</page:param>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.cvsmodules.select.have.not.created.any.modules'"/></p>
            </aui:param>
        </aui:component>

        <ul class="optionslist">
            <li>
                <ww:text name="'admin.cvsmodules.add.new.module'">
                    <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/projectcategories/AddRepository!default.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
            </li>
        </ul>
    </page:applyDecorator>
</ww:if>
<ww:else>
	<page:applyDecorator name="jiraform">
		<page:param name="title"><ww:text name="'admin.cvsmodules.select.repository'"/></page:param>
        <page:param name="description">
            <p><ww:text name="'admin.cvsmodules.select.page.description'"/></p>

            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.cvsmodules.select.note'"/></p>
                </aui:param>
            </aui:component>

        </page:param>

        <page:param name="width">100%</page:param>
        <page:param name="cancelURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/summary</page:param>
        <page:param name="action">SelectProjectRepository.jspa</page:param>
        <page:param name="submitId">select_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.select'"/></page:param>

        <ui:select label="text('admin.cvsmodules.repository')" name="'repositoryIds'" list="repositories" listKey="'id'" listValue="'name'" template="selectmap.jsp">
            <ui:param name="'headerrow'" value="'None'" />
            <ui:param name="'headervalue'" value="-1" />
        </ui:select>
        <ui:component name="'projectId'" template="hidden.jsp"/>
	</page:applyDecorator>
</ww:else>

</body>
</html>
