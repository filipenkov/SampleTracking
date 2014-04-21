<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    
	<title><ww:text name="'issue.columns.user.title'"/></title>
    <content tag="section">find_link</content>
</head>
<body class="nl iss-nav">
    <div class="item-header">
    <ww:if test="(/hasAnyErrors == false && /searchResults) || /mode == 'hide'">
        <jsp:include page="/includes/navigator/table/header.jsp"/>
    </ww:if>
    <ww:else>
        <h1 class="item-summary">
           <ww:text name="'issue.columns.user.title'"/>
        </h1>
    </ww:else>
    </div>
    <div id="iss-wrap" <ww:if test="/conglomerateCookieValue('jira.toggleblocks.cong.cookie','lhc-state')/contains('#iss-wrap') == true">class="lhc-collapsed"</ww:if>>
        <div id="main-content">
            <div class="column" id="primary" >
            <a class="toggle-lhc" href="#" title="<ww:text name="'jira.issuenav.toggle.lhc'" />"><ww:text name="'jira.issuenav.toggle.lhc'" /></a>
                <div class="content rounded">
                    <jira:formatuser user="/user" type="'fullProfile'" id="'view_user_issue_cols'"/>
                </div>
            </div>
            <div class="column" id="secondary">
                <div class="content rounded">
                    <div class="command-bar">
                        <div class="ops-cont">
                            <ul class="ops">
                                <li id="back-lnk-section" class="last">
                                    <a id="back-lnk" class="button first last" href="<%= request.getContextPath() %>/secure/IssueNavigator.jspa"><span class="icon icon-back"><span><ww:text name="'navigator.title'"/></span></span><ww:text name="'navigator.title'"/></a>
                                </li>
                            </ul>
                            <ul class="ops">
                                <li>
                                    <ww:if test="/actionsAndOperationsShowing/booleanValue() == true">
                                        <a class="button first last" href="<ww:property value="actionName"/>!hideActionsColumn.jspa"><ww:text name="'issue.actions.and.operations.column.hide'"/></a>
                                    </ww:if>
                                    <ww:else>
                                        <a class="button first last" href="<ww:property value="actionName"/>!showActionsColumn.jspa"><ww:text name="'issue.actions.and.operations.column.show'"/></a>
                                    </ww:else>
                                </li>
                            </ul>
                        </div>
                    </div>

                    <h3><ww:text name="'issue.columns.user.title'"/></h3>

                    <p><ww:text name="'issue.columns.user.description.first.line'">
                        <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/IssueNavigator.jspa"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text> <ww:text name="'issue.columns.description.second.line'"/></p>

                    <ww:property value="'ViewUserIssueColumns.jspa'" id="actionUrl"/>
                    <%@ include file="/includes/panels/issuecolumns.jsp" %>


                </div>
            </div>
        </div>
    </div>
</body>
</html>
