<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<ww:bean id="fieldVisibility" name="'com.atlassian.jira.web.bean.FieldVisibilityBean'"/>
<html>
<head>
    <link rel="index" href="<ww:url page="/secure/IssueNavigator.jspa" atltoken="false" />"/>
    <title>[#<ww:property value="/issueObject/key"/>] <ww:property value="/issueObject/summary"/></title>
    <content tag="section">find_link</content>
    <%
        // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "issue.view"
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResourcesForContext("jira.view.issue");
        final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
        fieldResourceIncluder.includeFieldResourcesForCurrentUser();

        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issueaction);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
</head>
<body class="<ww:if test="/useKickAss() == true">ka page-type-navigator navigator-collapsed navigator-issue-only</ww:if><ww:else>page-type-viewissue</ww:else>">
<%-- We need this while kickass is a dark feature --%>
<ww:if test="/useKickAss() == true">
<div class="content-container">
    <div class="content-body">
        <div class="result-panel">
            <div id="issue-content">
</ww:if>
                <header id="stalker" <ww:if test="/enableStalkerBar() == true"> class="issue-header js-stalker"</ww:if>>
                    <div class="issue-header-content">
                        <ww:property value="issue">
                            <jsp:include page="/includes/panels/issue_headertable.jsp"/>
                        </ww:property>
                        <jsp:include page="/includes/panels/issue/viewissue-opsbar.jsp"/>
                    </div>
                </header>
                <div class="content-container issue-body-content navigator-collapsed">
                    <div class="content-body aui-panel">
                        <div class="aui-group">
                            <div class="aui-item issue-main-column">
                                <ww:property value="/renderUtil/leftWebPanels">
                                    <ww:property value="/renderUtil/renderPanels(.)" escape="false"/>
                                </ww:property>
                            </div>
                            <div id="viewissuesidebar" class="aui-item issue-side-column">
                                <ww:property value="/renderUtil/rightWebPanels">
                                    <ww:property value="/renderUtil/renderPanels(.)" escape="false"/>
                                </ww:property>
                            </div>
                        </div>
                        <div class="issue-body">
                            <ww:property value="/renderUtil/infoWebPanels">
                                <ww:if test=".">
                                    <ww:iterator value=".">
                                        <ww:property value="/renderUtil/renderHeadlessPanel(.)" escape="false"/>
                                    </ww:iterator>
                                </ww:if>
                            </ww:property>
                        </div>
                    </div>
                </div>
                <fieldset class="hidden parameters">
                    <input type="hidden" id="viewMoreMsg" value="<ww:property value="text('viewissue.shorten.view.more')" />">
                    <input type="hidden" id="hideMsg" value="<ww:property value="text('viewissue.shorten.hide')" />">
                    <input type="hidden" id="error-401" value="<ww:text name="'issue.operations.error.401'"/>">
                    <input type="hidden" id="issueOpTitleUnvote" value="<ww:text name="'issue.operations.simple.voting.alreadyvoted'"/>">
                    <input type="hidden" id="issueOpTitleVote" value="<ww:text name="'issue.operations.simple.voting.notvoted'"/>">
                    <input type="hidden" id="issueOpTitleWatch" value="<ww:text name="'issue.operations.simple.startwatching'"/>">
                    <input type="hidden" id="issueOpTitleUnwatch" value="<ww:text name="'issue.operations.simple.stopwatching'"/>">
                    <input type="hidden" id="issueOpUnvote" value="<ww:text name="'issue.operations.simple.unvote'"/>">
                    <input type="hidden" id="issueOpVote" value="<ww:text name="'issue.operations.simple.vote'"/>">
                    <input type="hidden" id="issueOpWatch" value="<ww:text name="'issue.operations.watch'"/>">
                    <input type="hidden" id="issueOpUnwatch" value="<ww:text name="'issue.operations.unwatch'"/>">
                    <input type="hidden" id="i18nVote" value="<ww:text name="'common.concepts.vote'"/>">
                    <input type="hidden" id="i18nVoted" value="<ww:text name="'common.concepts.voted'"/>">
                    <input type="hidden" id="i18nWatch" value="<ww:text name="'common.concepts.watch'"/>">
                    <input type="hidden" id="i18nWatching" value="<ww:text name="'common.concepts.watching'"/>">
                </fieldset>

<%-- We need this while kickass is a dark feature --%>
<ww:if test="/useKickAss() == true">
            </div>
        </div>
    </div>
</div>
</ww:if>
</body>
</html>
