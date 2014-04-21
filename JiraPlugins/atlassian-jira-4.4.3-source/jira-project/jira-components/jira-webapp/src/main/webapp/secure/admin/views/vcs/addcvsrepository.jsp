<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.cvsmodules.add.cvs.module'"/></title>
    <meta name="admin.active.section" content="admin_plugins_menu/source_control"/>
    <meta name="admin.active.tab" content="cvs_modules"/>
</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">AddRepository.jspa</page:param>
    <page:param name="submitId">add_submit</page:param>
    <page:param name="submitName"> <ww:text name="'common.forms.add'"/> </page:param>
    <page:param name="cancelURI">ViewRepositories.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.cvsmodules.add.cvs.module'"/></page:param>
    <page:param name="helpURL">cvs_integration</page:param>
    <page:param name="description">
        <ww:text name="'admin.cvsmodules.add.cvs.module.page.description'"/>
        <p><ww:text name="'admin.cvsmodules.add.instructions'"/></p>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.cvsmodules.add.note'" /></p>
            </aui:param>
        </aui:component>

     </page:param>
    <page:param name="width">100%</page:param>

    <%@ include file="/includes/admin/vcs/cvsfields.jsp" %>

</page:applyDecorator>

</body>
</html>
