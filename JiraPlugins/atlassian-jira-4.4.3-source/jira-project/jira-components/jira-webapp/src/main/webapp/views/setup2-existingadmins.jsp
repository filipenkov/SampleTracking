
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
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
	<title>JIRA Setup</title>
</head>

<body>

<page:applyDecorator name="jiraform">
	<page:param name="title"><ww:text name="'setup2.adminexists'"/></page:param>
	<page:param name="description">
        <p>
        <ww:text name="'setup2.adminexists.desc'"/>
        </p>
        <ww:if test="hasErrorMessages == 'true'">
            <div class="formErrors warningBox">
                <ul>
                <ww:iterator value="flushedErrorMessages">
                    <li><ww:property /></li>
                </ww:iterator>
                </ul>
            </div>
        </ww:if>
    </page:param>
	<page:param name="action">SetupExisting.jspa</page:param>
	<page:param name="submitId">next_submit</page:param>
	<page:param name="submitName"><ww:property value="text('common.forms.next')"/>&gt;&gt;</page:param>
    <page:param name="enableFormErrors">false</page:param>
    <page:param name="autoSelectFirst">false</page:param>

	<ui:textfield label="text('common.words.username')" name="'username'">
		<ui:param name="'size'">12</ui:param>
	</ui:textfield>

	<ui:password label="text('common.words.password')" name="'password'">
		<ww:if test="/showForgotLogin == true">
            <ui:param name="'description'"><ww:text name="'setup2.forgotpassword.desc'"/>&nbsp;<a href="ForgotLoginDetails.jspa"><ww:text name="'common.words.here'"/></a>.)</ui:param>
        </ww:if>
	</ui:password>

</page:applyDecorator>

</body>
</html>
<% } %>
