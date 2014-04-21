<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflowstep.delete'"/>: <ww:property value="step/name" /></title>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.workflowstep.delete'"/>: <ww:property value="step/name" /></page:param>
	<page:param name="description">
		<p><ww:text name="'admin.workflowstep.delete.confirmation'"/></p>
	</page:param>
	<page:param name="width">100%</page:param>
	<page:param name="action">DeleteWorkflowStep.jspa</page:param>
	<page:param name="submitId">delete_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
	<page:param name="cancelURI"><ww:property value="/cancelUrl" /></page:param>

	<ui:component name="'workflowStep'" value="step/id" template="hidden.jsp" theme="'single'"  />
	<ui:component name="'workflowName'" value="workflow/name" template="hidden.jsp" theme="'single'"  />
    <ui:component name="'workflowMode'" value="workflow/mode" template="hidden.jsp" />

    <ui:component name="'originatingUrl'" template="hidden.jsp" theme="'single'"  />
</page:applyDecorator>

</body>
</html>
