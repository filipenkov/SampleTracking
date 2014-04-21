<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.schemes.workflows.assign.workflow.to.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
	<meta name="decorator" content="panel-admin"/>
</head>
<body>

    <page:applyDecorator id="assign-workflow-scheme" name="auiform">
        <page:param name="action">AddWorkflowSchemeEntity.jspa</page:param>
        <page:param name="method">post</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.assign'"/></page:param>
        <page:param name="submitButtonName"><ww:text name="'common.forms.assign'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url page="EditWorkflowSchemeEntities!default.jspa"><ww:param name="'schemeId'" value="schemeId"/></ww:url></page:param>
        <aui:component name="'schemeId'" template="hidden.jsp" theme="'aui'" />

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'escape'">false</aui:param>
            <aui:param name="'text'">
                <ww:text name="'admin.schemes.workflows.assign.workflow.to.scheme'"/>: <ww:property value="scheme/string('name')"/>
            </aui:param>
        </aui:component>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.schemes.workflows.issue.type.description'"/></page:param>
            <aui:select label="text('common.concepts.issuetype')" name="'type'" list="issueTypes" listKey="'key'" listValue="'value'" theme="'aui'" />
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.schemes.workflows.workflow.description'"/></page:param>
            <aui:select label="text('issue.field.workflow')" name="'workflow'" list="/workflows" listKey="'name'" listValue="'name'" theme="'aui'" />
        </page:applyDecorator>

    </page:applyDecorator>

</body>
</html>
