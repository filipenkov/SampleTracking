<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflowtransitions.delete.workflow.transitions'"/></title>
</head>

<body>

    <p>
    <table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jiraform">
        <page:param name="action">DeleteWorkflowTransitions.jspa</page:param>
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    	<page:param name="cancelURI"><ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="/selectedTransitions/iterator/next/id" /></ww:url></page:param>
        <page:param name="title"><ww:text name="'admin.workflowtransitions.delete.workflow.transitions'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <p>
                <ww:text name="'admin.workflowtransitions.delete.confirm.deletion'">
                    <ww:param name="'value0'"><ww:iterator value="/selectedTransitions" status="'status'">
                    <ww:if test="@status/first == false">, </ww:if><b><ww:property value="name" /></b>
                </ww:iterator></ww:param>
                </ww:text>
            </p>
        </page:param>

        <ui:component name="'workflowStep'" value="step/id" template="hidden.jsp" />
        <ui:component name="'workflowName'" value="workflow/name" template="hidden.jsp" />
        <ui:component name="'workflowMode'" value="workflow/mode" template="hidden.jsp" />

        <ww:iterator value="/selectedTransitions">
            <ui:component name="'transitionIds'" value="./id" template="hidden.jsp" />
        </ww:iterator>
    </page:applyDecorator>
    </table>
    </p>

</body>
</html>
