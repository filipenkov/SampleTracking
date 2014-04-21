<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties" %>
<%@ page import="com.atlassian.jira.user.preferences.PreferenceKeys" %>
<%@ page import="com.atlassian.jira.user.preferences.UserPreferencesManager" %>
<%@ page import="com.atlassian.jira.util.BrowserUtils" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.jira.security.JiraAuthenticationContext" %>
<%@ page import="com.atlassian.crowd.embedded.api.User" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
	<title><ww:if test="searchRequest/name">[<ww:property value="searchRequest/name"/>] </ww:if><ww:text name="'navigator.title'"/></title>
    <ww:if test="/hasAnyErrors == false && /searchResults/total > 0">
        <LINK REL="alternate" TITLE="<ww:property value="searchRequest/name"/>" HREF="<%= request.getContextPath() %>/secure/IssueNavigator.jspa?view=rss&<ww:property value="jqlQueryString" />&tempMax=100&reset=true&decorator=none" TYPE="application/rss+xml">
    </ww:if>
    <meta id="isNavigator" name="isNavigator" content="true" />
    <meta id="focusSearch" name="focusSearch" content="<ww:property value="/shouldFocusField()" />" />
    <content tag="section">find_link</content>

<%
    final JiraAuthenticationContext jiraAuthenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
    final WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    final ApplicationProperties applicationProperties = ComponentManager.getInstance().getApplicationProperties();
    final User remoteUser = jiraAuthenticationContext.getLoggedInUser();
    boolean userAutocompleteDisabled = false;
    if (remoteUser != null)
    {
        final UserPreferencesManager userPreferencesManager = ComponentManager.getInstance().getUserPreferencesManager();
        userAutocompleteDisabled = userPreferencesManager.getPreferences(remoteUser).getBoolean(PreferenceKeys.USER_JQL_AUTOCOMPLETE_DISABLED);
    }
    if (!applicationProperties.getOption("jira.jql.autocomplete.disabled") && !userAutocompleteDisabled)
    {
        webResourceManager.requireResource("jira.webresources:jqlautocomplete");
    }
    final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
    fieldResourceIncluder.includeFieldResourcesForCurrentUser();
    webResourceManager.requireResourcesForContext("jira.navigator.advanced");
%>
</head>
<body class="nl iss-nav">
    <div id="jqlFieldz" style="display:none;"><ww:property value="/visibleFieldNamesJson"/></div>
    <div id="jqlFunctionNamez" style="display:none;"><ww:property value="/visibleFunctionNamesJson"/></div>
    <div id="jqlReservedWordz" style="display:none;"><ww:property value="/jqlReservedWordsJson"/></div>
    <div class="item-header">
        <ww:if test="/hasAnyErrors == false && /searchResults">
            <jsp:include page="/includes/navigator/table/header.jsp"/>
        </ww:if>
        <ww:else>
            <h1 class="item-summary">
                <ww:property value="text('navigator.title')"/>
            </h1>
        </ww:else>
    </div>
    <div id="iss-wrap" <ww:if test="/conglomerateCookieValue('jira.toggleblocks.cong.cookie','lhc-state')/contains('#iss-wrap') == true">class="lhc-collapsed"</ww:if>>
        <div id="main-content">
            <div class="column" id="primary" >
                <a class="toggle-lhc" href="#" title="<ww:text name="'jira.issuenav.toggle.lhc'" />"><ww:text name="'jira.issuenav.toggle.lhc'" /></a>
                <div class="content rounded">
                    <jsp:include page="/includes/navigator/filter-form/tabs.jsp" />
                    <jsp:include page="/includes/navigator/filter-form/navigator_type_links.jsp" />
                    <jsp:include page="/includes/navigator/filter-form/filter_description.jsp" />
                    <ww:if test="browseableProjects != null && browseableProjects/size > 0">
                        <jsp:include page="/includes/navigator/filter-operations.jsp" flush="false" />
                        <div id="queryBoxTable" class="module toggle-wrap">
                            <div class="mod-header toggle-trigger">
                                <h3 class="toggle-title" title="<ww:text name="'filter.title.prefix'"/> <ww:text name="'navigator.advanced.helper.history'"/>" >
                                     <ww:text name="'navigator.advanced.helper.history'"/>
                                </h3>
                            </div>
                            <div class="mod-content toggle-target" id="jqlHistory">
                                <div class="queryBox" id="jqlHistoryFieldArea">
                                    <ul class="queryBox" id="queryBox">
                                    <ww:iterator value="/savedJqlQueryHistoryItems" status="'status'">
                                        <li <ww:if test="@status/first == true">class="first"</ww:if>><a id="historyItem<ww:property value="@status/index"/>" class="jqlQuickLink" title="<ww:property value="."/>" href="<ww:url page="/secure/IssueNavigator!executeAdvanced.jspa" atltoken="false"><ww:param name="'jqlQuery'" value="."/><ww:param name="'runQuery'" value="'true'"/><ww:param name="'clear'" value="'true'"/></ww:url>"><ww:property value="."/></a></li>
                                    </ww:iterator>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </ww:if>
                </div>
            </div>

            <div class="column" id="secondary">
                <div id="jqlcomponent" class="content rounded">
                    <div class="results">

                        <ww:if test="/hasErrorMessages == true">
                            <aui:component id="jqlerror" template="auimessage.jsp" theme="'aui'">
                                <aui:param name="'messageType'">error</aui:param>
                                <aui:param name="'messageHtml'">
                                    <ww:iterator value="/errorMessages" status="'warningStatus'">
                                        <p><ww:property value="."/></p>
                                    </ww:iterator>
                                </aui:param>
                            </aui:component>
                        </ww:if>
                        <ww:if test="/warningMessages != null && /warningMessages/empty == false">
                            <aui:component id="jqlwarning" template="auimessage.jsp" theme="'aui'">
                                <aui:param name="'messageType'">warning</aui:param>
                                <aui:param name="'titleText'" ><ww:text name="'navigator.advanced.warning.heading'"/></aui:param>
                                <aui:param name="'messageHtml'">
                                    <ul>
                                        <ww:iterator value="/warningMessages" status="'warningStatus'">
                                            <li><ww:property value="."/></li>
                                        </ww:iterator>
                                    </ul>
                                </aui:param>
                            </aui:component>
                        </ww:if>

                        <jsp:include page="/includes/navigator/quick-search-reverse.jsp"/>

                        <form id="jqlform" action="IssueNavigator!executeAdvanced.jspa" method="post">
                            <div class="page-help">
                                <ww:component name="'advanced_search'" template="help.jsp">
                                    <ww:param name="'align'" value="'absmiddle'"/>
                                    <ww:param name="'linktext'"><ww:text name="'navigator.advanced.query.syntax'"/></ww:param>
                                </ww:component>
                            </div>
                            <label for="jqltext">
                                <ww:if test="/autocompleteEnabledForThisRequest == true">
                                    <span id="jqlerrormsg" class="icon jqlgood"><span>parse</span></span> <ww:text name="'navigator.advanced.query.label'"/> <span id="jqlcolrowcount"><ww:text name="'navigator.advanced.query.line'"/>:<span id="jqlrownum">0</span><ww:text name="'navigator.advanced.query.character'"/>:<span id="jqlcolumnnum">0</span></span>
                                </ww:if>
                            </label>
                        <ww:if test="/autocompleteEnabledForThisRequest == true">
                            <div class="atlassian-autocomplete" >
                        </ww:if>
                        <ww:else>
                            <div class="autocomplete-off">
                        </ww:else>
                                <textarea id="jqltext" rows="3" cols="80" name="jqlQuery" class="focus-select-end <ww:if test="/focusJql == true">focused</ww:if>"><ww:property value="/jqlQuery"/></textarea>
                            </div>
                            <div class="jqlundertext">
                                <input class="button" id="jqlrunquery" type="submit" value="<ww:text name="'navigator.advanced.execute.query'"/>" accesskey="<ww:text name="'common.forms.submit.accesskey'"/>"
                                   title="<ww:text name="'common.forms.submit.tooltip'">
                                   <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
                                   <ww:param name="'value1'"><%=BrowserUtils.getModifierKey()%></ww:param>
                                   </ww:text>" />
                                <input type="hidden" name="runQuery" value="true"/>
                                <ww:if test="/remoteUser != null && /autocompleteDisabled == false">
                                    <span class="jql-hints">
                                        <input type="checkbox" id="autocomplete" name="autocomplete" <ww:if test="/autocompleteDisabledForUser == false">checked</ww:if> onclick="AJS.reloadViaWindowLocation('IssueNavigator!toggleAutocompletePref.jspa?autocomplete=<ww:if test="/autocompleteDisabledForUser == true">false</ww:if><ww:else>true</ww:else><ww:if test="$createNew == true">&createNew=true</ww:if>')"/>
                                        <label for="autocomplete"><ww:text name="'navigator.advanced.autocomplete'"/></label>
                                    </span>
                                </ww:if>
                                <ww:if test="/showPluginHints == true">
                                     <span class="jql-plugin-hints">
                                         <a href="<ww:property value="/pluginHintsUrl"/>" target="_blank"><ww:text name="'get.plugin.hint.jql'"/></a>
                                     </span>
                                 </ww:if>
                            </div>
                        </form>

                        <ww:if test="/hasAnyErrors == false && /searchResults/total == 0">
                            <div class="jqlerror-container">
                                <aui:component template="auimessage.jsp" theme="'aui'">
                                    <aui:param name="'messageType'">info</aui:param>
                                    <aui:param name="'messageHtml'"><p><ww:text name="'navigator.results.nomatchingissues'"/></p></aui:param>
                                </aui:component>
                            </div>
                        </ww:if>
                        <ww:if test="/hasAnyErrors == false && /searchResults">
                            <div class="results-wrap">
                                <jsp:include page="/includes/navigator/issuelinks.jsp" flush="false" />
                                <ww:if test="/searchResults/total > 0">
                                    <div class="results-count">
                                    <ww:text name="'navigator.results.displayissues'">
                                        <ww:param name="'value0'"><span id="results-count-start"><ww:property value="/searchResults/niceStart" /></span></ww:param>
                                        <ww:param name="'value1'"><ww:property value="/searchResults/end" /></ww:param>
                                        <ww:param name="'value2'"><strong id="results-count-total"><ww:property value="/searchResults/total" /></strong></ww:param>
                                    </ww:text>
                                    </div>
                                </ww:if>
                                <jsp:include page="/includes/navigator/results.jsp"/>
                                <ww:if test="/searchResults/total > 0">
                                    <div class="results-count">
                                    <ww:text name="'navigator.results.displayissues'">
                                        <ww:param name="'value0'"><span id="results-count-start"><ww:property value="/searchResults/niceStart" /></span></ww:param>
                                        <ww:param name="'value1'"><ww:property value="/searchResults/end" /></ww:param>
                                        <ww:param name="'value2'"><strong id="results-count-total"><ww:property value="/searchResults/total" /></strong></ww:param>
                                    </ww:text>
                                    </div>
                                </ww:if>
                            </div>
                        </ww:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
<fieldset class="jql-autocomplete-params hidden" >
</fieldset>
</body>
</html>
