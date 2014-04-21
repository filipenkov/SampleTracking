<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
    <meta content="message" name="decorator" />
    <title><ww:text name="'illegal.workflow.title'"/></title>
</head>
<body>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'titleText'"><ww:text name="'illegal.workflow.title'" /></aui:param>
        <aui:param name="'messageHtml'">
            <ww:iterator value="flushedErrorMessages"><p><ww:property /></p></ww:iterator>
            <p><ww:text name="'illegal.workflow.desc'"/></p>
            <p>
                <ww:text name="'contact.admin.for.perm'">
                    <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>
</body>
</html>