<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.jira.web.util.HelpUtil" %>

<% HelpUtil helpUtil = new HelpUtil();
   HelpUtil.HelpPath internalHelpPath = helpUtil.getHelpPath("hsqldb");
   HelpUtil.HelpPath externalHelpPath = helpUtil.getHelpPath("dbconfig.index");

    // don't show ANYTHING to the user if they come here looking for trouble
    if (com.atlassian.jira.util.JiraUtils.isSetup())
    {
%>
<%--
Leave this as a raw HTML. Do not use response.getWriter() or response.getOutputStream() here as this will fail
on Orion. Let the application server figure out how it want to output this text.
--%>
JIRA has already been set up.
<%
}
else
{
%>
<html>
<head>
    <title><ww:text name="'setup.title'"/></title>
</head>

<body>

<ww:if test="databaseConnectionTestWorked == 'true'">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">success</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:property value="text('setup.database.connection.test.successful')"/></p>
        </aui:param>
    </aui:component>
</ww:if>

<page:applyDecorator id="jira-setupwizard" name="auiform">
    <page:param name="action">SetupDatabase.jspa</page:param>
    <page:param name="hideToken">true</page:param>
    <page:param name="useCustomButtons">true</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setupdb.title'"/></aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldset">
        <page:param name="type">group</page:param>
        <page:param name="legend"><ww:text name="'setupdb.option.label'" /></page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <page:param name="description">
                <ww:text name="'setupdb.internal.desc'">
                    <ww:param name="'value0'"><a href="<%=internalHelpPath.getUrl()%>" target="_blank"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </page:param>
            <aui:radio id="'database-internal'" label="text('setupdb.internal.label')" list="null" name="'databaseOption'" theme="'aui'">
                <aui:param name="'customValue'">INTERNAL</aui:param>
                <ww:if test="databaseOption == 'INTERNAL'">
                    <aui:param name="'checked'">true</aui:param>
                </ww:if>
            </aui:radio>

        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <page:param name="description">
                <ww:text name="'setupdb.external.desc'">
                    <ww:param name="'value0'"><a href="<%=externalHelpPath.getUrl()%>" target="_blank"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </page:param>
            <aui:radio id="'database-external'" label="text('setupdb.external.label')" list="null" name="'databaseOption'" theme="'aui'">
                <aui:param name="'customValue'">EXTERNAL</aui:param>
                <ww:if test="databaseOption == 'EXTERNAL'">
                    <aui:param name="'checked'">true</aui:param>
                </ww:if>
            </aui:radio>
        </page:applyDecorator>
    </page:applyDecorator>

    <script type="text/javascript">

        // Used to prefill fields on the page depending on the databaseType option selected
        var dbPrefills = { ports: {}, schemas: {} };

        dbPrefills.ports['postgres72'] = '5432';
        dbPrefills.schemas['postgres72'] = 'public';

        dbPrefills.ports['mysql'] = '3306';
        dbPrefills.schemas['mysql'] = '';

        dbPrefills.ports['oracle10g'] = '1521';
        dbPrefills.schemas['oracle10g'] = '';

        dbPrefills.ports['mssql'] = '1433';
        dbPrefills.schemas['mssql'] = 'dbo';

    </script>

    <div id="setup-db-external" class="hidden">

        <page:applyDecorator name="auifieldgroup">
            <aui:select label="text('setupdb.databasetype.label')" name="'databaseType'" list="externalDatabases" listKey="'key'" listValue="'value'" mandatory="'true'" theme="'aui'" />
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'setupdb.jdbcHostname.desc'" /></page:param>
            <aui:textfield label="text('setupdb.jdbcHostname')" name="'jdbcHostname'" mandatory="'true'" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'setupdb.jdbcPort.desc'" /></page:param>
            <aui:textfield label="text('setupdb.jdbcPort')" name="'jdbcPort'" mandatory="'true'" theme="'aui'" />
        </page:applyDecorator>
        <div class="db-option-mssql db-option-postgres72 db-option-mysql setup-fields">
            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'setupdb.jdbcDatabase.desc'" /></page:param>
                <aui:textfield label="text('setupdb.jdbcDatabase')" name="'jdbcDatabase'" mandatory="'true'" theme="'aui'" />
            </page:applyDecorator>
        </div>
        <div class="db-option-oracle10g setup-fields hidden">
            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'setupdb.jdbcSid.desc'" /></page:param>
                <aui:textfield label="text('setupdb.jdbcSid')" name="'jdbcSid'" mandatory="'true'" theme="'aui'" />
            </page:applyDecorator>
        </div>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'setupdb.jdbcUsername.desc'" /></page:param>
            <aui:textfield label="text('setupdb.jdbcUsername')" name="'jdbcUsername'" mandatory="'true'" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'setupdb.jdbcPassword.desc'" /></page:param>
            <aui:password label="text('setupdb.jdbcPassword')" name="'jdbcPassword'" theme="'aui'" value="jdbcPassword" />
        </page:applyDecorator>
        <div class="db-option-mssql db-option-postgres72 setup-fields hidden">
            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'setupdb.schemaName.desc'" /></page:param>
                <aui:textfield label="text('setupdb.schemaName')" name="'schemaName'" theme="'aui'" />
            </page:applyDecorator>
        </div>

    </div>

    <aui:component name="'testingConnection'" value="'false'" template="hidden.jsp" theme="'aui'" />

    <aui:component name="'language'" value="language" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'changingLanguage'" value="'false'" template="hidden.jsp" theme="'aui'" />

    <page:applyDecorator name="auifieldgroup">
        <page:param name="type">buttons-container</page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">buttons</page:param>
            <aui:component theme="'aui'" template="formSubmit.jsp">
                <aui:param name="'id'">jira-setupwizard-submit</aui:param>
                <aui:param name="'submitButtonName'">next</aui:param>
                <aui:param name="'submitButtonText'"><ww:text name="'common.words.next'"/></aui:param>
            </aui:component>
            <aui:component name="'testConnection'" template="formButton.jsp" theme="'aui'">
                <aui:param name="'cssClass'">hidden</aui:param>
                <aui:param name="'id'">test-connection</aui:param>
                <aui:param name="'text'"><ww:text name="'setupdb.testconnection'" /></aui:param>
            </aui:component>
            <div class="hidden throbber-message">
                <span id="submit-throbber" class="hidden"><ww:text name="'setupdb.database.being.setup'"/></span>
                <span id="test-connection-throbber" class="hidden"><ww:text name="'setupdb.database.test.connection'"/></span>
            </div>
        </page:applyDecorator>
    </page:applyDecorator>

</page:applyDecorator>

<div id="language-picker">
    <h3><ww:property value="text('setup.choose.language')"/></h3>
    <ww:if test="/installedLocales/size > 0">
        <ul>
        <ww:iterator value="/installedLocales" status="'status'">
            <ww:if test="./toString == /applicationProperties/defaultLocale/toString">
                <li class="language-default">
                    <span title="<ww:property value="./displayLanguage"/> (<ww:text name="'setup.language.default'"/>)"><img src="<%= request.getContextPath() %>/images/flags/<ww:property value="./toString"/>.gif" alt="<ww:property value="./displayLanguage"/>" /></span>
                </li>
            </ww:if>
        </ww:iterator>
        <ww:iterator value="/installedLocales" status="'status'">
            <ww:if test="./toString != /applicationProperties/defaultLocale/toString">
                <li>
                    <button data-value="<ww:property value="./toString"/>" title="<ww:property value="./displayName(.)"/> <ww:property value="./displayLanguage" />"><img border="0" src="<%= request.getContextPath() %>/images/flags/<ww:property value="./toString"/>.gif" alt="<ww:property value="./displayName(.)"/> <ww:property value="./displayLanguage" />" /></button>
                </li>
            </ww:if>
        </ww:iterator>
        </ul>
    </ww:if>
</div>

</body>
</html>
<% } %>
