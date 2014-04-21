<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.schemes.workflow.delete.workflow.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
	<meta name="decorator" content="panel-admin"/>
</head>
<body>

    <page:applyDecorator id="delete-workflow-scheme" name="auiform">
        <page:param name="action">DeleteWorkflowScheme.jspa</page:param>
        <page:param name="method">post</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="submitButtonName"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="cancelLinkURI">ViewWorkflowSchemes.jspa</page:param>
        <aui:component name="'schemeId'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'confirmed'" value="true" template="hidden.jsp" theme="'aui'" />

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'">
                <ww:text name="'admin.schemes.workflow.delete.workflow.scheme'"/>
            </aui:param>
        </aui:component>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <ww:text name="'admin.schemes.delete.confirmation'">
                    <ww:param name="'value0'"><strong><ww:property value="name" /></strong></ww:param>
                </ww:text>
                <ww:if test="description" >
                    <div class="secondary-text">"<ww:property value="description" />"</div>
                </ww:if>
            </aui:param>
        </aui:component>

        <ww:if test="projects(scheme)/size > 0" >
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <ww:text name="'admin.schemes.delete.warning'"/>
                    <ul>
                    <ww:iterator value="projects(scheme)" status="'liststatus'">
                        <li><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="string('name')" /></a></li>
                    </ww:iterator>
                    </ul>
                    <ww:text name="'admin.schemes.workflow.you.may.wish.to.select.another'"/>
                </aui:param>
            </aui:component>
        </ww:if>

    </page:applyDecorator>

</body>
</html>
