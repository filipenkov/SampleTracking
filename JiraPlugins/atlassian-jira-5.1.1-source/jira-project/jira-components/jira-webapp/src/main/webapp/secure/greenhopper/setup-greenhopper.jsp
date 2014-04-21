<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'setup.greenhopper.title'" /></title>
    <meta content="setupgh" name="decorator"/>
</head>
<body>

<page:applyDecorator id="gh-setupwizard" name="auiform">
    <page:param name="action">SetupGreenHopper.jspa</page:param>
    <page:param name="submitButtonName">finish</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.finish'" /></page:param>
    <page:param name="cancelLinkURI"><ww:url value="'/secure/MyJiraHome.jspa'" atltoken="false"/></page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setup.greenhopper.step1'"/></aui:param>
    </aui:component>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'setup.greenhopper.info'"/></p>
        </aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <aui:component label="text('admin.server.id')" template="formFieldValue.jsp" theme="'aui'">
            <aui:param name="'texthtml'"><ww:property value="/serverId"/></aui:param>
        </aui:component>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textarea label="text('setup.license.label')" name="'license'" cols="50" rows="6" mandatory="'true'" theme="'aui'">
            <aui:param name="'size'">long</aui:param>
        </aui:textarea>
        <page:param name="description">
            <ww:text name="'setup.license.description.generate.eval'">
                <ww:param name="'value0'"><a id="fetchLicense" data-url="Setup!fetchLicense.jspa" href="<ww:property value="/requestLicenseURL"/>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
            <br />
            <ww:text name="'setup.license.description.retrieve'">
                <ww:param name="'value0'"><a target="_blank" href="<ww:component name="'external.link.jira.licenses'" template="externallink.jsp"/>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </page:param>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <ww:text name="'setup.greenhopper.proceed.instruction'">
            <ww:param name="'value0'"><a target="_blank" href="<ww:component name="'external.link.jira.licenses'" template="externallink.jsp"/>"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text>
    </page:applyDecorator>

</page:applyDecorator>

</body>
</html>
