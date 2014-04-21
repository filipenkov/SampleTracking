<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
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
	<title><ww:text name="'setup.title'" /></title>
</head>
<body>

<page:applyDecorator id="jira-setupwizard" name="auiform">
    <page:param name="action">Setup.jspa</page:param>
    <page:param name="useCustomButtons">true</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setup.step1'"/></aui:param>
    </aui:component>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'setup.existingimport'">
                    <ww:param name="'value0'"><strong></ww:param>
                    <ww:param name="'value1'"></strong></ww:param>
                    <ww:param name="'value2'"><a href="SetupImport!default.jspa"></ww:param>
                    <ww:param name="'value3'"></a></ww:param>
                </ww:text>
            </p>
            <p><ww:text name="'setup.home.path.msg'"/>:<br /> <em><ww:property value="/homePath" /></em></p>
        </aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('setup.applicationtitle.label')" name="'title'" mandatory="'true'" theme="'aui'">
            <aui:param name="'size'">long</aui:param>
        </aui:textfield>
        <page:param name="description">
            <ww:text name="'setup.applicationtitle.desc'"/>
        </page:param>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:select label="text('setup.mode.label')" name="'mode'" list="allowedModes" listKey="'key'" listValue="'value'" mandatory="'true'" theme="'aui'" />
        <page:param name="description">
            <ww:text name="'setup.mode.line1.desc'" />:<br />
            <ww:text name="'setup.mode.line2.desc'" /><br />
            <ww:text name="'setup.mode.line3.desc'" />
        </page:param>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('setup.baseurl.label')" name="'baseURL'" mandatory="'true'" theme="'aui'">
            <aui:param name="'size'">long</aui:param>
        </aui:textfield>
        <page:param name="description">
            <ww:text name="'setup.baseurl.desc'" />
        </page:param>
    </page:applyDecorator>

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
                <ww:param name="'value0'"><a class="cancel" id="fetchLicense" data-url="Setup!fetchLicense.jspa" href="<ww:property value="/requestLicenseURL"/>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
            <br />
            <ww:text name="'setup.license.description.retrieve'">
                <ww:param name="'value0'"><a target="_blank" href="<ww:component name="'external.link.jira.licenses'" template="externallink.jsp"/>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </page:param>
    </page:applyDecorator>

    <aui:component name="'nextStep'" value="null" template="hidden.jsp" theme="'aui'" />

    <page:applyDecorator name="auifieldgroup">
        <page:param name="type">buttons-container</page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">buttons</page:param>
            <aui:component theme="'aui'" template="formSubmit.jsp">
                <aui:param name="'id'">jira-setupwizard-submit</aui:param>
                <aui:param name="'submitButtonName'">next</aui:param>
                <aui:param name="'submitButtonText'"><ww:text name="'common.words.next'"/></aui:param>
            </aui:component>
            <div class="hidden throbber-message">
                <ww:text name="'setup.spinner.message'" />
            </div>
        </page:applyDecorator>
    </page:applyDecorator>

</page:applyDecorator>

</body>
</html>
<% } %>
