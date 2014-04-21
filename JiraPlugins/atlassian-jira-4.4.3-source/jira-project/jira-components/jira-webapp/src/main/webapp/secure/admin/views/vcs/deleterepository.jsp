<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.cvsmodules.delete.version.control.module'"/></title>
    <meta name="admin.active.section" content="admin_plugins_menu/source_control"/>
    <meta name="admin.active.tab" content="cvs_modules"/>
</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">DeleteRepository.jspa</page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <page:param name="submitId">delete_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    <page:param name="cancelURI">ViewRepositories.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.cvsmodules.delete.version.control.module'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
    <input type="hidden" name="id" value="<ww:property value="id" />">
    <input type="hidden" name="confirmed" value="true">

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'admin.cvsmodules.delete.confirmation'">
                    <ww:param name="'value0'"><b><ww:property value="name" /></b></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>

    </page:param>
</page:applyDecorator>

</body>
</html>
