<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%
// don't show ANYTHING to the user if they come here looking for trouble
if (com.atlassian.jira.util.JiraUtils.isSetup()) {
%>
<%--
Leave this as a raw HTML. Do not use response.getWriter() or response.getOutputStream() here as this will fail
on Orion. Let the application server figure out how it want to output this text.
--%>
JIRA has already been set up.
<%
} else {
%>
<html>
<head>
    <title><ww:text name="'setup.title'"/></title>
</head>

<body>

<page:applyDecorator id="jira-setupwizard" name="auiform">
    <page:param name="action">SetupImport.jspa</page:param>
    <page:param name="submitButtonName">import</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.import'" /></page:param>

    <ww:if test="/hasSpecificErrors == true && /specificErrors/errorMessages/empty == false">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <ww:iterator value="specificErrors/errorMessages">
                    <p><ww:property escape="false"/></p>
                </ww:iterator>
            </aui:param>
        </aui:component>
    </ww:if>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setup.import.title'"/></aui:param>
    </aui:component>

    <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'setup.import.desc.line1'"/>
                <ww:text name="'setup.import.desc.line2'">
                    <ww:param name="'value0'"><a href="Setup!default.jspa"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('setup.filename.label')" name="'filename'" mandatory="true" theme="'aui'">
            <aui:param name="'size'">long</aui:param>
        </aui:textfield>
        <page:param name="description">
            <ww:text name="'setup.filename.desc'"/>
        </page:param>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:component label="text('setup.indexpath.label')" template="formFieldValue.jsp" theme="'aui'">
            <aui:param name="'texthtml'"><ww:property value="/defaultIndexPath" /></aui:param>
        </aui:component>
        <page:param name="description">
            <ww:text name="'restore.index.path.msg'" />
        </page:param>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textarea label="text('admin.import.license.if.required')" name="'license'" cols="50" rows="6" theme="'aui'">
            <aui:param name="'size'">long</aui:param>
        </aui:textarea>
        <page:param name="description">
            <ww:text name="'admin.import.enter.a.license'"/>
            <br />
            <ww:text name="'setup.license.description.generate.eval'">
                <ww:param name="'value0'"><a id="fetchLicense" data-url="SetupImport!fetchLicense.jspa" href="<ww:property value="/requestLicenseURL"/>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
            <br />
            <ww:text name="'setup.license.description.retrieve'">
                <ww:param name="'value0'"><a target="_blank" href="<ww:component name="'external.link.jira.licenses'" template="externallink.jsp"/>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </page:param>
    </page:applyDecorator>

    <aui:component name="'useDefaultPaths'" value="'false'" template="hidden.jsp" theme="'aui'" />

</page:applyDecorator>

</body>
</html>
<% } %>
