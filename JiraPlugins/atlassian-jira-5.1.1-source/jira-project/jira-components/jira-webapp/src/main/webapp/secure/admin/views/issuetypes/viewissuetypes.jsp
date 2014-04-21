<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%-- The page is used for the manageable option object --%>
<ww:property value="/manageableOption" >
<html>
<head>
	<title><ww:text name="'admin.issuesettings.issuetypes.view.title'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="issue_types"/>
</head>
<body>
    <header>
        <nav class="aui-toolbar">
            <div class="toolbar-split toolbar-split-right">
                <ul class="toolbar-group">
                    <li class="toolbar-item">
                        <a id="add-issue-type" class="toolbar-trigger trigger-dialog" href="AddNewIssueType.jspa">
                            <span class="icon icon-add16"></span>
                            <ww:text name="'admin.issuesettings.issuetypes.add.new.button.label'"/>
                        </a>
                    </li>
                    <li class="toolbar-item">
                        <aui:component name="'manageIssueTypes'" template="toolbarHelp.jsp" theme="'aui'">
                            <aui:param name="'cssClass'">toolbar-trigger</aui:param>
                        </aui:component>
                    </li>
                </ul>
            </div>
        </nav>
        <h2><ww:text name="'admin.issuesettings.issuetypes.view.title'"/></h2>
    </header>
    <jsp:include page="/secure/admin/views/issuetypes/issuetypes.jsp" />
</body>
</html>
</ww:property>
