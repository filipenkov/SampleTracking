<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/user_interface"/>
    <meta name="admin.active.tab" content="issue_field_columns"/>
	<title><ww:text name="'issue.columns.admin.title'"/></title>
</head>
<body>
<div class="column">
    <div class="content">
        <div class="command-bar">
            <div class="ops-cont">
                <ul class="ops">
                    <li class="last">
                        <a id="back-lnk" class="button first last" href="<%= request.getContextPath() %>/secure/IssueNavigator.jspa"><span class="icon icon-back"><span><ww:text name="'navigator.title'"/></span></span><ww:text name="'navigator.title'"/></a>
                    </li>
                    <li class="last">
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
        <h3><ww:text name="'issue.columns.admin.title'"/></h3>
        <p><ww:text name="'issue.columns.admin.description.first.line'">
                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/IssueNavigator.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
            <ww:text name="'issue.columns.description.second.line'"/></p>

        <ww:property value="'admin/ViewIssueColumns.jspa'" id="actionUrl"/>
        <%@ include file="/includes/panels/issuecolumns.jsp" %>
    </div>
</div>
</body>
</html>
