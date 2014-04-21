
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
	<title><ww:text name="'admin.schemes.workflows.delete.entity'"/></title>
</head>

<body>

    <p>
    <table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jiraform">
        <page:param name="action">DeleteWorkflowSchemeEntity.jspa</page:param>
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    	<page:param name="cancelURI"><ww:url page="EditWorkflowSchemeEntities!default.jspa"><ww:param name="'schemeId'" value="schemeId"/></ww:url></page:param>
        <page:param name="title"><ww:text name="'admin.schemes.workflows.delete.entity'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="autoSelectFirst">false</page:param>
	    <page:param name="description">
        <input type="hidden" name="schemeId" value="<ww:property value="schemeId" />">
        <input type="hidden" name="id" value="<ww:property value="id" />">
        <input type="hidden" name="confirmed" value="true">
        <ww:if test="workflowSchemeEntity/string('issuetype') == '0'">
            <ww:text name="'admin.schemes.workflows.delete.confirmation1'">
                <ww:param name="'value0'"><b><ww:property value="workflow/name" /></b></ww:param>
            </ww:text>
        </ww:if>
        <ww:else>
            <ww:text name="'admin.schemes.workflows.delete.confirmation2'">
                <ww:param name="'value0'"><b><ww:property value="workflow/name" /></b></ww:param>
                <ww:param name="'value1'"><b><ww:property value="/constantsManager/issueType(workflowSchemeEntity/string('issuetype'))/string('name')" /></b></ww:param>
            </ww:text>
        </ww:else>
        </page:param>
    </page:applyDecorator>
    </table>
    </p>

</body>
</html>
