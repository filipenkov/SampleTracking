<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/issue_attributes"/>
    <meta name="admin.active.tab" content="statuses"/>
    <title><ww:text name="'admin.issuesettings.statuses.delete.title'"/></title>
</head>
<body>
    <p>
    <page:applyDecorator name="jiraform">
        <page:param name="action">DeleteStatus.jspa</page:param>
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="cancelURI">ViewStatuses.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.issuesettings.statuses.delete.status'">
            <ww:param name="'value0'"><ww:property value="constant/string('name')" /></ww:param>
        </ww:text></page:param>
        <page:param name="description"><ww:text name="'admin.issuesettings.statuses.delete.confirmation'"/></page:param>

        <ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'single'"  />

    </page:applyDecorator>
    </p>
</body>
</html>
