<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflowtransitions.add.workflow.transition'"/></title>
</head>

<body>

    <p>
    <table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jiraform">
        <page:param name="action">AddWorkflowTransition.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    	<page:param name="cancelURI"><ww:property value="/cancelUrl" /></page:param>
        <page:param name="title"><ww:text name="'admin.workflowtransitions.add.workflow.transition'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <ww:text name="'admin.workflowtransitions.create.a.transition.from'">
                <ww:param name="'value0'"><b><ww:property value="/workflow/descriptor/step(step/id)/name" /></b></ww:param>
            </ww:text>
        </page:param>

        <ui:component name="'workflowStep'" value="step/id" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'workflowName'" value="workflow/name" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'workflowMode'" value="workflow/mode" template="hidden.jsp" />

        <ui:textfield label="text('admin.workflowtransitions.transition.name')" name="'transitionName'" size="'30'" />
        <ui:textfield label="text('common.words.description')" name="'description'" >
            <ui:param name="'class'">textfield</ui:param>
        </ui:textfield>
        <ui:select label="text('admin.workflowtransitions.destination.step')" name="'destinationStep'" list="workflow/descriptor/steps" listKey="'id'" listValue="'name'" />

<%--        <ww:bean id="workflowActions" name="'com.atlassian.jira.web.action.admin.workflow.WorkflowActionsBean'" />--%>

        <ui:select label="text('admin.workflowtransitions.transition.view')" name="'view'" list="/fieldScreens" listKey="'id'" listValue="'name'">
            <ui:param name="'headerrow'"><ww:text name="'admin.workflowtransitions.no.view.for.transition'"/></ui:param>
            <ui:param name="'headervalue'" value="''" />
            <ui:param name="'description'"><ww:text name="'admin.workflowtransitions.the.screen.that.appears.for.this.transition'"/></ui:param>
        </ui:select>

        <ui:component name="'originatingUrl'" template="hidden.jsp" theme="'single'"  />

    </page:applyDecorator>
    </table>
    </p>

</body>
</html>
