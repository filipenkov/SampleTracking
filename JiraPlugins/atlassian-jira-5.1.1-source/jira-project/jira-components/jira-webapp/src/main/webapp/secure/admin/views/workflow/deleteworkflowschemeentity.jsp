<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.schemes.workflows.delete.entity'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
	<meta name="decorator" content="panel-admin"/>
</head>
<body>

    <page:applyDecorator id="delete-workflow-scheme-entity" name="auiform">
        <page:param name="action">DeleteWorkflowSchemeEntity.jspa</page:param>
        <page:param name="method">post</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="submitButtonName"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url page="EditWorkflowSchemeEntities!default.jspa"><ww:param name="'schemeId'" value="schemeId"/></ww:url></page:param>
        <aui:component name="'schemeId'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'id'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'confirmed'" value="true" template="hidden.jsp" theme="'aui'" />

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'">
                <ww:text name="'admin.schemes.workflows.delete.entity'"/>
            </aui:param>
        </aui:component>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <ww:if test="workflowSchemeEntity/string('issuetype') == '0'">
                    <ww:text name="'admin.schemes.workflows.delete.confirmation1'">
                        <ww:param name="'value0'"><strong><ww:property value="workflow/name" /></strong></ww:param>
                    </ww:text>
                </ww:if>
                <ww:else>
                    <ww:text name="'admin.schemes.workflows.delete.confirmation2'">
                        <ww:param name="'value0'"><strong><ww:property value="workflow/name" /></strong></ww:param>
                        <ww:param name="'value1'"><strong><ww:property value="/constantsManager/issueType(workflowSchemeEntity/string('issuetype'))/string('name')" /></strong></ww:param>
                    </ww:text>
                </ww:else>
            </aui:param>
        </aui:component>

    </page:applyDecorator>

</body>
</html>
