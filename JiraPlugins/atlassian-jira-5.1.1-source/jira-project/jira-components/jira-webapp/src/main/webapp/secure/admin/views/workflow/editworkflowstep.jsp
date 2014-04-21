<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflowstep.update.workflow.step'"/></title>
</head>

<body>

    <p>
    <table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jiraform">
        <page:param name="action">EditWorkflowStep.jspa</page:param>
        <page:param name="submitId">update_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    	<page:param name="cancelURI"><ww:property value="/cancelUrl" /></page:param>
        <page:param name="title"><ww:text name="'admin.workflowstep.update.workflow.step'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <ww:text name="'admin.workflowstep.update.page.description'">
                <ww:param name="'value0'"><b><ww:property value="step/name" /></b></ww:param>
            </ww:text>
        </page:param>

        <ui:component name="'workflowStep'" value="step/id" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'workflowName'" value="workflow/name" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'workflowMode'" value="workflow/mode" template="hidden.jsp" />

        <ui:textfield label="text('admin.workflowstep.step.name')" name="'stepName'" size="'30'" />

        <ww:if test="/oldStepOnDraft(step) == true">
            <ui:select label="text('admin.workflowstep.linked.status')" name="'stepStatus'" list="/statuses" listKey="'string('id')'" listValue="'string('name')'" disabled="'true'">
                <ui:param name="'description'">
                    <ww:text name="'admin.workflowstep.cannot.change'"/>
                </ui:param>
            </ui:select>
        </ww:if>
        <ww:else>
            <ui:select label="text('admin.workflowstep.linked.status')" name="'stepStatus'" list="/statuses" listKey="'string('id')'" listValue="'string('name')'" />
        </ww:else>

        <ui:component name="'originatingUrl'" template="hidden.jsp" theme="'single'"  />

    </page:applyDecorator>
    </table>
    </p>

</body>
</html>
