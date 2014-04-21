<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%
    final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
    final WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();

    fieldResourceIncluder.includeFieldResourcesForCurrentUser();
    webResourceManager.requireResourcesForContext("jira.navigator.simple");
%>
<html>
<head>
    <content tag="section">find_link</content>
	<title><ww:if test="searchRequest/name">[<ww:property value="searchRequest/name"/>] </ww:if><ww:text name="'navigator.title'"/></title>
    <ww:if test="/hasAnyErrors == false &&  /searchResults/total > 0">
        <link rel="alternate" title="<ww:property value="searchRequest/name"/>" href="<%= request.getContextPath() %>/secure/IssueNavigator.jspa?view=rss&<ww:property value="jqlQueryString" />&tempMax=100&reset=true&decorator=none" type="application/rss+xml" />
    </ww:if>
    <meta id="isNavigator" name="isNavigator" />
    <meta id="focusSearch" name="focusSearch" content="<ww:property value="/shouldFocusField()" />" />
</head>
<body class="nl iss-nav">

    <div class="item-header">
        <ww:if test="(/hasAnyErrors == false && /searchResults) || /mode == 'hide'">
            <jsp:include page="/includes/navigator/table/header.jsp"/>
        </ww:if>
        <ww:else>
            <h1 class="item-summary">
                <ww:property value="text('navigator.title')"/><ww:if test="searchRequest/name"> &mdash; <ww:property value="searchRequest/name"/></ww:if>
            </h1>
        </ww:else>
    </div>
    <div id="iss-wrap" <ww:if test="/conglomerateCookieValue('jira.toggleblocks.cong.cookie','lhc-state')/contains('#iss-wrap') == true">class="lhc-collapsed"</ww:if>>
        <div id="main-content">
            <div class="column" id="primary" >
            <a class="toggle-lhc" href="#" title="<ww:text name="'jira.issuenav.toggle.lhc'" />"><ww:text name="'jira.issuenav.toggle.lhc'" /></a>
                <div class="content rounded">
                    <ww:if test="/mode == 'show'">
                        <jsp:include page="/includes/navigator/filter-form.jsp" flush="false" />
                    </ww:if>
                    <ww:else>
                        <jsp:include page="/includes/navigator/summary-pane.jsp" flush="false" />
                    </ww:else>
                </div>
            </div>
            <div class="column" id="secondary">
                <div class="content rounded">
                    <div class="results">
                    <ww:if test="valid == 'true'">
                        <jsp:include page="/includes/navigator/issuelinks.jsp" flush="false" />
                        <jsp:include page="/includes/navigator/table.jsp" flush="false" />
                    </ww:if>
                    <ww:else>
                        <jsp:include page="/includes/navigator/cantviewfilter.jsp" flush="false" />
                    </ww:else>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
