<%@ page import="com.atlassian.jira.workflow.JiraWorkflow"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflowtransitions.update.title'"/></title>
</head>

<body>

    <p>
    <table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jiraform">
        <page:param name="action">EditWorkflowTransition.jspa</page:param>
        <page:param name="submitId">update_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    	<page:param name="cancelURI"><ww:property value="/cancelUrl" /></page:param>
        <page:param name="title"><ww:text name="'admin.workflowtransitions.update.title'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="instructions">
            <p><ww:text name="'admin.workflowtransitions.update.page.description'">
                <ww:param name="'value0'"><strong><ww:property value="/transition/name" /></strong></ww:param>
            </ww:text></p>
            <ww:if test="/nameI8n">
                <div class="infoBox">
                    <ww:text name="'admin.workflowtransitions.update.information'">
                        <ww:param name="'value0'"><code><%=JiraWorkflow.JIRA_META_ATTRIBUTE_I18N%></code></ww:param>
                        <ww:param name="'value1'"><code><ww:property value="nameI8n" /></code></ww:param>
                        <ww:param name="'value2'"><a href="<ww:url page="ViewWorkflowTransitionMetaAttributes.jspa"><ww:param name="'workflowMode'" value="workflow/mode" /><ww:param name="'workflowName'" value="workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>"></ww:param>
                        <ww:param name="'value3'"></a></ww:param>
                    </ww:text>
                </div>
            </ww:if>
        </page:param>

        <ui:component name="'workflowStep'" value="step/id" template="hidden.jsp" />
        <ui:component name="'workflowName'" value="workflow/name" template="hidden.jsp" />
        <ui:component name="'workflowMode'" value="workflow/mode" template="hidden.jsp" />
        <ui:component name="'workflowTransition'" value="transition/id" template="hidden.jsp" />

        <ui:textfield label="text('admin.workflowtransitions.transition.name')" name="'transitionName'" size="'30'" />
        <ui:textfield label="text('common.words.description')" name="'description'" >
            <ui:param name="'class'">textfield</ui:param>
        </ui:textfield>
        <ui:select label="text('admin.workflowtransition.destinationstep')" name="'destinationStep'" list="/transitionSteps" listKey="'id'" listValue="'name'" />

        <ww:if test="/setView == true">
            <ui:select label="text('admin.workflowtransition.transitionview')" name="'view'" list="/fieldScreens" listKey="'id'" listValue="'name'">
                <ui:param name="'headerrow'"><ww:text name="'admin.workflowtransitions.no.view.for.transition'"/></ui:param>
                <ui:param name="'headervalue'" value="''" />
                <ui:param name="'description'"><ww:text name="'admin.workflowtransitions.the.screen.that.appears.for.this.transition'"/></ui:param>
            </ui:select>
        </ww:if>

        <ui:component name="'originatingUrl'" template="hidden.jsp" />

    </page:applyDecorator>
    </table>
    </p>

</body>
</html>
