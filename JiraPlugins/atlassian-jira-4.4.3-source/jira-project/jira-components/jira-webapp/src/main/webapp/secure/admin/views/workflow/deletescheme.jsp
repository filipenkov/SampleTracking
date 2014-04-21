
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
	<title><ww:text name="'admin.schemes.workflow.delete.workflow.scheme'"/></title>
</head>

<body>

    <p>
    <table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jiraform">
        <page:param name="action">DeleteWorkflowScheme.jspa</page:param>
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    	<page:param name="cancelURI">ViewWorkflowSchemes.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.schemes.workflow.delete.workflow.scheme'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="autoSelectFirst">false</page:param>
	    <page:param name="description">
        <input type="hidden" name="schemeId" value="<ww:property value="schemeId" />">
        <input type="hidden" name="confirmed" value="true">
        <p>
        <ww:text name="'admin.schemes.delete.confirmation'">
            <ww:param name="'value0'"><u><ww:property value="name" /></u></ww:param>
        </ww:text><br>
        <ww:if test="description" >
            "<ww:property value="description" />"
        </ww:if>
        </p>

        <ww:if test="projects(scheme)/size > 0" >
            <p><ww:text name="'admin.schemes.delete.warning'">
                <ww:param name="'value0'"><ww:property value="name" /></ww:param>
                <ww:param name="'value1'"><font color=#cc0000></ww:param>
            </ww:text>
            <ww:iterator value="projects(scheme)" status="'liststatus'">
                <a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary">
                <ww:property value="string('name')" /></a><ww:if test="@liststatus/last == false">, </ww:if><ww:else>.</ww:else>
            </ww:iterator><br>
            <ww:text name="'admin.schemes.workflow.you.may.wish.to.select.another'"/></font></p>
        </ww:if>

        </page:param>
    </page:applyDecorator>
    </table>
    </p>

</body>
</html>
