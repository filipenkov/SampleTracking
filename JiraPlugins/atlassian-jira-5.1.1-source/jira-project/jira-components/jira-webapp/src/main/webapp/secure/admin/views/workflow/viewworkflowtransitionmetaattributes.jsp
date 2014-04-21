<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflowtransition.view.workflow.transition.properties'"/> - <ww:property value="transition/name" /></title>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.workflowtransition.view.workflow.transition.properties'"/>: <ww:property value="transition/name" /></page:param>
    <page:param name="width">100%</page:param>
    <p>
        <ww:text name="'admin.workflowtransition.page.description'">
            <ww:param name="'value0'"><b><ww:property value="transition/name" /></b></ww:param>
        </ww:text>
    </p>
    <ul>
        <li><ww:text name="'admin.workflowtransition.propertyconsistsof'"/></li>
        <li><ww:text name="'admin.workflowtransition.arbitraryproperties'"/></li>
    </ul>
    <ul class="optionslist">
        <li><ww:text name="'admin.workflowtransition.view.workflow.transition.specific'">
            <ww:param name="'value0'"><a href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
            <ww:param name="'value2'"><b><ww:property value="transition/name"/></b></ww:param>
        </ww:text></li>
    </ul>
</page:applyDecorator>

<%@ include file="/includes/admin/workflow/metaattributes.jsp" %>

<ww:if test="workflow/editable == true">
    <page:applyDecorator name="jiraform">
        <page:param name="action">AddWorkflowTransitionMetaAttribute.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="title"><ww:text name="'admin.workflowtransition.addnewproperty'"/></page:param>
        <page:param name="width">100%</page:param>

        <ui:component name="'workflowName'" value="workflow/name"  template="hidden.jsp"/>
        <ui:component name="'workflowMode'" value="workflow/mode" template="hidden.jsp" />
        <ui:component name="'workflowStep'" value="step/id"  template="hidden.jsp"/>
        <ui:component name="'workflowTransition'" value="transition/id"  template="hidden.jsp"/>

        <ui:textfield label="text('admin.workflowtransition.propertykey')" name="'attributeKey'" size="'30'" />

        <ui:textfield label="text('admin.workflowtransition.propertyvalue')" name="'attributeValue'" size="'30'" />
    </page:applyDecorator>
</ww:if>
</body>
</html>
