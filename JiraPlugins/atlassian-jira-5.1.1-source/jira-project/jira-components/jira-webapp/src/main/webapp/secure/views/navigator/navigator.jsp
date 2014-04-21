<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%
    final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
    final WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();

    fieldResourceIncluder.includeFieldResourcesForCurrentUser();
    webResourceManager.requireResourcesForContext("jira.navigator.simple");
%>
<html>
<head>
	<title><ww:if test="searchRequest/name">[<ww:property value="searchRequest/name"/>] </ww:if><ww:text name="'navigator.title'"/></title>
    <ww:if test="/hasAnyErrors == false &&  /searchResults/total > 0">
        <link rel="alternate" title="<ww:property value="searchRequest/name"/>" href="<%= request.getContextPath() %>/secure/IssueNavigator.jspa?view=rss&<ww:property value="jqlQueryString" />&tempMax=100&reset=true&decorator=none" type="application/rss+xml" />
    </ww:if>
    <meta name="decorator" content="navigator" />
    <meta id="isNavigator" name="isNavigator" />
    <meta id="focusSearch" name="focusSearch" content="<ww:property value="/shouldFocusField()" />" />
    <content tag="section">find_link</content>
</head>
<body>
    <header>
        <ww:if test="(/hasAnyErrors == false && /searchResults) || /mode == 'hide'">
            <jsp:include page="/includes/navigator/table/header.jsp"/>
        </ww:if>
        <ww:else>
            <h1>
                <ww:property value="text('navigator.title')"/><ww:if test="searchRequest/name"> &mdash; <ww:property value="searchRequest/name"/></ww:if>
            </h1>
        </ww:else>
    </header>
    <div id="issuenav" class="content-container<ww:if test="/conglomerateCookieValue('jira.toggleblocks.cong.cookie','lhc-state')/contains('#issuenav') == true"> lhc-collapsed</ww:if>">
        <div class="content-related">
            <a class="toggle-lhc" href="#" title="<ww:text name="'jira.issuenav.toggle.lhc'" />"><ww:text name="'jira.issuenav.toggle.lhc'" /></a>
            <ww:if test="/mode == 'show'">
                <jsp:include page="/includes/navigator/filter-form.jsp" flush="false" />
            </ww:if>
            <ww:else>
                <jsp:include page="/includes/navigator/summary-pane.jsp" flush="false" />
            </ww:else>
        </div>
        <div class="content-body aui-panel">
            <ww:if test="valid == 'true'">
                <jsp:include page="/includes/navigator/table.jsp" flush="false" />
                <jsp:include page="/includes/navigator/issuelinks.jsp" flush="false" />
            </ww:if>
            <ww:else>
                <jsp:include page="/includes/navigator/cantviewfilter.jsp" flush="false" />
            </ww:else>
        </div>
    </div>
</body>
</html>
