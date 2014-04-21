<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.cvsmodules.update.cvs.module'"/></title>
    <meta name="admin.active.section" content="admin_plugins_menu/source_control"/>
    <meta name="admin.active.tab" content="cvs_modules"/>
</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">UpdateRepository.jspa</page:param>
    <page:param name="submitId">update_submit</page:param>
    <page:param name="submitName"> <ww:text name="'common.forms.update'"/> </page:param>
    <page:param name="cancelURI">ViewRepositories.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.cvsmodules.update.cvs.module'"/></page:param>
    <page:param name="helpURL">cvs_integration</page:param>
    <page:param name="description">
        <p><ww:text name="'admin.cvsmodules.update.page.description'"/></p>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.cvsmodules.update.note'"/></p>
            </aui:param>
        </aui:component>

    </page:param>
    <page:param name="width">100%</page:param>

    <%@ include file="/includes/admin/vcs/cvsfields.jsp" %>

    <ui:component name="'id'" template="hidden.jsp"/>
    <ui:component name="'type'" template="hidden.jsp"/>

</page:applyDecorator>

</body>
</html>
