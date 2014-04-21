<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>

<ww:bean id="fieldVisibility" name="'com.atlassian.jira.web.bean.FieldVisibilityBean'" />
<html>
<head>
    <meta content="general" name="decorator" />
    <link rel="index" href="<ww:url page="/secure/IssueNavigator.jspa" atltoken="false" />" />
    <title>[#<ww:property value="/issueObject/key" />] <ww:property value="/issueObject/summary" /></title>
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
<body class="type-ab stalker sn">
<fieldset class="hidden parameters">
    <input type="hidden" id="viewMoreMsg" value="<ww:property value="text('viewissue.shorten.view.more')" />">
    <input type="hidden" id="hideMsg" value="<ww:property value="text('viewissue.shorten.hide')" />" >
    <input type="hidden" id="error-401" value="<ww:text name="'issue.operations.error.401'"/>" >
    <input type="hidden" id="issueOpTitleUnvote" value="<ww:text name="'issue.operations.simple.voting.alreadyvoted'"/>" >
    <input type="hidden" id="issueOpTitleVote" value="<ww:text name="'issue.operations.simple.voting.notvoted'"/>" >
    <input type="hidden" id="issueOpTitleWatch" value="<ww:text name="'issue.operations.simple.startwatching'"/>" >
    <input type="hidden" id="issueOpTitleUnwatch" value="<ww:text name="'issue.operations.simple.stopwatching'"/>" >
    <input type="hidden" id="issueOpUnvote" value="<ww:text name="'issue.operations.simple.unvote'"/>" >
    <input type="hidden" id="issueOpVote" value="<ww:text name="'issue.operations.simple.vote'"/>" >
    <input type="hidden" id="issueOpWatch" value="<ww:text name="'issue.operations.watch'"/>" >
    <input type="hidden" id="issueOpUnwatch" value="<ww:text name="'issue.operations.unwatch'"/>" >
    <input type="hidden" id="i18nVote" value="<ww:text name="'common.concepts.vote'"/>" >
    <input type="hidden" id="i18nVoted" value="<ww:text name="'common.concepts.voted'"/>" >
    <input type="hidden" id="i18nWatch" value="<ww:text name="'common.concepts.watch'"/>" >
    <input type="hidden" id="i18nWatching" value="<ww:text name="'common.concepts.watching'"/>" >
</fieldset>

<div id="stalker" <ww:if test="/enableStalkerBar() == true"> class="stalker"</ww:if>>
    <div class="item-header">
    <ww:property value="issue">
        <jsp:include page="/includes/panels/issue_headertable.jsp" />
    </ww:property>
    </div>
    <jsp:include page="/includes/panels/issue/viewissue-opsbar.jsp"/>
    <div class="btm"></div>
</div>
<div id="main-content">
    <div class="active-area">
        <div class="column" id="primary">
            <div class="content">
                <%-- Make the Issue object referenceable via '@issue' (eg. from issue_descriptiontable.jsp) --%>
                <ww:property value="/issueObject" id="issue" />
                <%-- Put the issue GV on the stack --%>
                <ww:property value="issue">

                    <div id="details-module" class="module toggle-wrap">
                        <div class="mod-header">
                            <h3 class="toggle-title"><ww:text name="'viewissue.subheading.issuedetails'"/></h3>
                        </div>
                        <div class="mod-content">
                            <ww:property value="/summaryHtml" escape="false"/>

                            <ww:if test="/fieldScreenRenderTabs/empty == false">
                                <jsp:include page="/includes/panels/issue/view_customfields.jsp" />
                            </ww:if>
                        </div>
                    </div>

                    <%@ include file="/includes/panels/issue_descriptiontable.jsp" %>

                    <ww:property value="./long('project')">
                        <jsp:include page="/includes/panels/issue/view_attachments.jsp" />
                    </ww:property>
                </ww:property>
                <ww:property value="/leftWebPanels">
                    <ww:property value="renderPanels(.)" escape="false"/>
                </ww:property>

            </div><%-- //.content --%>
        </div><%-- //.column#primary --%>

        <div class="column" id="secondary">
            <div class="content">
                <ww:property value="/rightWebPanels">
                    <ww:property value="renderPanels(.)" escape="false"/>
                </ww:property>

            </div><%--content--%>
        </div><%-- //.column#secondary --%>
        <ww:property value="infoWebPanels">
            <ww:if test=".">
                <ww:iterator value=".">
                    <ww:property value="renderHeadlessPanel(.)" escape="false"/>
                </ww:iterator>
            </ww:if>
        </ww:property>

    </div><%-- //.active-area --%>
</div><%-- //#main-content --%>
</body>
</html>
