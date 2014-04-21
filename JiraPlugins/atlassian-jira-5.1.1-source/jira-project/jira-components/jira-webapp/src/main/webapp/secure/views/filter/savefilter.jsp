<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'savefilter.title'"/></title>
    <content tag="section">find_link</content>
</head>
<body class="page-type-issuenav">
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
            <jsp:include page="/includes/navigator/summary-pane.jsp" />
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator name="jiraform">
                <page:param name="action">SaveFilter.jspa</page:param>
                <page:param name="cancelURI">IssueNavigator.jspa</page:param>
                <page:param name="submitId">save_submit</page:param>
                <page:param name="submitName"><ww:text name="'savefilter.save'"/></page:param>
                <page:param name="width">100%</page:param>
                <page:param name="title"><ww:text name="'savefilter.title'"/></page:param>
                <tr>
                <td colspan="2">
                    <table class="aui">
                        <thead>
                            <tr>
                                <th width="50%"><ww:text name="'savefilter.old.request'"/></th>
                                <th width="50%"><ww:text name="'savefilter.updated.request'"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                <ww:if test="/advancedQuery == true">
                                    <div class="searcherValue"><label class="fieldLabel" for="dbJqlQuery"><ww:text name="'jira.jql.query'"/>:</label> <span id="dbJqlQuery" class="fieldValue"><ww:property value="/diffedDbSearchRequestJql" escape="false"/></span></div>
                                </ww:if>
                                <ww:else>
                                    <ww:iterator value="/searcherGroups" >
                                    <ww:iterator value="./searchers" >
                                        <ww:property value="/oldSearcherViewHtml(.)" escape="false" />
                                    </ww:iterator>
                                    </ww:iterator>
                                </ww:else>
                                <ww:if test="/searchSorts(/dbSearchRequest)/size != 0" >
                                    <div class="searcherValue">
                                        <label class="fieldLabel"><ww:text name="'savefilter.sorted.by'"/></label>
                                        <ww:iterator value="/searchSortDescriptions(dbSearchRequest)" status="'status'">
                                            <span class="fieldValue<ww:if test="/searchSortsEqual == false"> old-value</ww:if>"><ww:property value="."/></span>
                                        </ww:iterator>
                                    </div>
                                </ww:if>
                                </td>
                                <td>
                                <ww:if test="/advancedQuery == true">
                                    <div class="searcherValue"><label class="fieldLabel" for="currentJqlQuery"><ww:text name="'jira.jql.query'"/>:</label> <span id="currentJqlQuery" class="fieldValue"><ww:property value="/diffedCurrentSearchRequestJql" escape="false"/></span></div>
                                </ww:if>
                                <ww:else>
                                    <ww:iterator value="/searcherGroups" >
                                    <ww:iterator value="./searchers" >
                                        <ww:property value="/newSearcherViewHtml(.)" escape="false" />
                                    </ww:iterator>
                                    </ww:iterator>
                                </ww:else>
                                <ww:if test="/searchSorts(/searchRequest)/size != 0" >
                                    <div class="searcherValue">
                                        <label class="fieldLabel"><ww:text name="'savefilter.sorted.by'"/></label>
                                        <ww:iterator value="/searchSortDescriptions(searchRequest)" status="'status'">
                                            <span class="fieldValue<ww:if test="/searchSortsEqual == false"> new-value</ww:if>"><ww:property value="."/></span>
                                        </ww:iterator>
                                    </div>
                                </ww:if>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </td>
                </tr>
            </page:applyDecorator>
        </div>
    </div>
</body>
</html>
