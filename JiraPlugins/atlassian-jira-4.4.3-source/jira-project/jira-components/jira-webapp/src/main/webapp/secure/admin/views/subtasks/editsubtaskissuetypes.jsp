<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="subtasks"/>
    <title><ww:text name="'admin.subtasks.edit.subtask.issue.type'"/></title>
</head>
<body>
<script language="JavaScript">
    function openWindow()
    {
        var vWinUsers = window.open('<%= request.getContextPath() %>/secure/popups/IconPicker.jspa?fieldType=subtasks&formName=jiraform','IconPicker', 'status=no,resizable=yes,top=100,left=200,width=580,height=650,scrollbars=yes');
        vWinUsers.opener = self;
	    vWinUsers.focus();
    }
</script>
    <p>
    <page:applyDecorator name="jiraform">
        <page:param name="action">EditSubTaskIssueTypes.jspa</page:param>
        <page:param name="submitId">update_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="cancelURI">ManageSubTasks.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.subtasks.edit.subtask.issue.type'"/>: <ww:property value="/name" /></page:param>

        <ui:textfield label="text('common.words.name')" name="'name'" size="'30'" />

        <ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />

        <ui:component label="text('admin.common.phrases.icon.url')" name="'iconurl'" template="textimagedisabling.jsp">
    	    <ui:param name="'imagefunction'">openWindow()</ui:param>
    	    <ui:param name="'size'">60</ui:param>
    	    <ui:param name="'description'"><ww:text name="'admin.subtasks.relative.to.the.jira.applicaiton'"/></ui:param>
        </ui:component>

        <ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'sequence'" template="hidden.jsp" theme="'single'"  />
    </page:applyDecorator>
    </p>
</body>
</html>
