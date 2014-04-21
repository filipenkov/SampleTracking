<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'popups.daterange.title'"/></title>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">null</page:param>
    <page:param name="onsubmit">return false;</page:param>
    <page:param name="title">
        <ww:text name="'popups.daterange.title'"/>
    </page:param>
    <page:param name="description">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'popups.daterange.error'"/></p>
            </aui:param>
        </aui:component>
    </page:param>
    <page:param name="width">100%</page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <page:param name="buttons">
        <input class="aui-button" type="button" value="<ww:text name="'admin.common.words.close'"/>" onclick="window.close();">
    </page:param>
</page:applyDecorator>
</body>
</html>
