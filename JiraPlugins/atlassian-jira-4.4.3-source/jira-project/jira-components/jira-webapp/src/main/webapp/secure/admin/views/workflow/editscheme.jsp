
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
	<title><ww:text name="'admin.workflows.edit.scheme.title'"/></title>
</head>

<body>

    <p>
    <table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jiraform">
        <page:param name="action">EditWorkflowScheme.jspa</page:param>
        <page:param name="submitId">update_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    	<page:param name="cancelURI">ViewWorkflowSchemes.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.workflows.edit.scheme'">
            <ww:param name="'value0'"><ww:property value="scheme/string('name')" /></ww:param>
        </ww:text></page:param>
        <page:param name="width">100%</page:param>

        <ui:textfield label="text('common.words.name')" name="'name'" size="'30'" />
        <ui:textarea label="text('common.words.description')" name="'description'" cols="'30'" rows="'3'" />

        <ui:component name="'schemeId'" template="hidden.jsp" />
    </page:applyDecorator>
    </table>
    </p>

</body>
</html>
