<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
	<title><ww:text name="'admin.schemes.workflows.add.workflow.to.scheme'"/></title>
</head>

<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">AddWorkflowSchemeEntity.jspa</page:param>
    <page:param name="submitId">add_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="cancelURI"><ww:url page="EditWorkflowSchemeEntities!default.jspa"><ww:param name="'schemeId'" value="schemeId"/></ww:url></page:param>
    <page:param name="title"><ww:text name="'admin.schemes.workflows.add.workflow.to.scheme'"/></page:param>
    <page:param name="helpURL">workflow_schemes</page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
    <p>
    <ww:text name="'admin.schemes.workflows.please.select.the.issue.type'"/>: <ww:property value="scheme/string('name')"/>.
    </p>
    </page:param>

    <ui:select label="text('common.concepts.issuetype')" name="'type'" list="issueTypes" listKey="'key'" listValue="'value'">
        <ui:param name="'description'">
            <ww:text name="'admin.schemes.workflows.issue.type.description'"/>
        </ui:param>
    </ui:select>

    <ui:select label="text('issue.field.workflow')" name="'workflow'" list="/workflows" listKey="'name'" listValue="'name'">
        <ui:param name="'description'">
            <ww:text name="'admin.schemes.workflows.workflow.description'"/>
        </ui:param>
    </ui:select>

    <ui:component name="'schemeId'" template="hidden.jsp"/>
    <ui:component name="'event'" template="hidden.jsp"/>
</page:applyDecorator>


</body>
</html>
