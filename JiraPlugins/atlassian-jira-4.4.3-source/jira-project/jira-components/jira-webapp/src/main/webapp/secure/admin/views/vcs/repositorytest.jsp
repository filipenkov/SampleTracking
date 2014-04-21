<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.cvsmodules.test.cvs.repository'"/></title>
    <meta name="admin.active.section" content="admin_plugins_menu/source_control"/>
    <meta name="admin.active.tab" content="cvs_modules"/>
</head>
<body>

    <page:applyDecorator name="jiraform">
        <page:param name="action">ViewRepositories.jspa</page:param>
        <page:param name="submitId">ok_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.common.words.ok'"/></page:param>
        <page:param name="title"><ww:text name="'admin.cvsmodules.test.repository'"/></page:param>
        <page:param name="autoSelectFirst">false</page:param>
        <page:param name="description"><ww:text name="'admin.cvsmodules.test.page.description'"/></page:param>
        <page:param name="width">100%</page:param>

        <tr>
            <td colspan="2">
                <ww:if test="/message || /invalidInput == true">
                    <aui:component template="auimessage.jsp" theme="'aui'">
                        <aui:param name="'messageType'">error</aui:param>
                        <aui:param name="'iconText'"><ww:text name="'admin.common.words.error'"/></aui:param>
                        <aui:param name="'messageHtml'">
                            <pre><ww:property value="/message" /></pre>
                        </aui:param>
                    </aui:component>
                </ww:if>
                <ww:else>
                    <aui:component template="auimessage.jsp" theme="'aui'">
                        <aui:param name="'messageType'">success</aui:param>
                        <aui:param name="'iconText'"><ww:text name="'admin.common.words.success'"/></aui:param>
                        <aui:param name="'messageHtml'">
                            <p><ww:text name="'admin.cvsmodules.test.no.problems'"/></p>
                        </aui:param>
                    </aui:component>
                </ww:else>
            </td>
        </tr>

    </page:applyDecorator>

</body>
</html>
