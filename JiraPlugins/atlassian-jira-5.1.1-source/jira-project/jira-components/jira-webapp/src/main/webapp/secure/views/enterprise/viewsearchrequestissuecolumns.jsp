<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'issue.columns.user.title'"/></title>
    <content tag="section">find_link</content>
</head>
<body class="page-type-issuenav">
    <header>
        <ww:if test="(/hasAnyErrors == false && /searchResults) || /mode == 'hide'">
            <jsp:include page="/includes/navigator/table/header.jsp"/>
        </ww:if>
        <ww:else>
            <h1>
               <ww:text name="'issue.columns.user.title'"/>
            </h1>
        </ww:else>
    </header>
    <div id="issuenav" class="content-container<ww:if test="/conglomerateCookieValue('jira.toggleblocks.cong.cookie','lhc-state')/contains('#issuenav') == true"> lhc-collapsed</ww:if>">
        <div class="content-related">
            <a class="toggle-lhc" href="#" title="<ww:text name="'jira.issuenav.toggle.lhc'" />"><ww:text name="'jira.issuenav.toggle.lhc'" /></a>
            <jsp:include page="/includes/navigator/summary-pane.jsp" flush="false" />
        </div>
        <div class="content-body aui-panel">
            <div class="command-bar">
                <div class="ops-cont">
                    <ul class="ops">
                        <li id="back-lnk-section" class="last">
                            <a id="back-lnk" class="button first last" href="<%= request.getContextPath() %>/secure/IssueNavigator.jspa?mode=hide&requestId=<ww:property value='/filterId'/>"><span class="icon icon-back"><span><ww:text name="'navigator.title'"/></span></span><ww:text name="'navigator.title'"/></a>
                        </li>
                    </ul>
                </div>
            </div>
            <h3><ww:text name="'issue.columns.user.title'"/></h3>
            <p>
            <ww:if test="/usingDefaultColumns == true">
                <ww:text name="'issue.columns.searchrequest.description.first.line.nocolumns'">
                    <ww:param name="'value0'"><ww:property value="/searchRequest/name"/></ww:param>
                    <ww:param name="'value1'"><a href="<%= request.getContextPath() %>/secure/IssueNavigator.jspa?mode=hide&requestId=<ww:property value='/filterId'/>"></ww:param>
                    <ww:param name="'value2'"></a></ww:param>
                </ww:text>
            </ww:if>
            <ww:else>
                <ww:text name="'issue.columns.searchrequest.description.first.line.columns'">
                    <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/IssueNavigator.jspa?mode=hide&requestId=<ww:property value='/filterId'/>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                    <ww:param name="'value2'"><ww:property value="/searchRequest/name"/></ww:param>
                </ww:text>
            </ww:else>
             <ww:text name="'issue.columns.description.second.line'"/>
            </p>
            <ww:property value="/filterId" id="filterId" />
            <ww:property value="'ViewSearchRequestIssueColumns.jspa'" id="actionUrl" />
            <%@ include file="/includes/panels/issuecolumns.jsp" %>
        </div>
    </div>
</body>
</html>
