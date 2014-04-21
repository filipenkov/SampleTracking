<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<html>
<head>
    <ww:if test="ableToCreateIssueInSelectedProject == 'true'"><meta content="genericaction" name="decorator" /></ww:if>
    <ww:else><meta content="message" name="decorator" /></ww:else>
    <title><ww:text name="'createissue.title'"/></title>
    <content tag="section">find_link</content>
    <%
        final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
        fieldResourceIncluder.includeFieldResourcesForCurrentUser();

        final WebResourceManager wrm = ComponentManager.getInstance().getWebResourceManager();
        wrm.requireResourcesForContext("jira.create.issue");
    %>
</head>
<body class="type-a">
<ww:if test="ableToCreateIssueInSelectedProject == 'true'">
    <div class="content intform">
        <page:applyDecorator id="issue-create" name="auiform">
            <page:param name="action">CreateIssueDetails.jspa</page:param>
            <page:param name="submitButtonName">Create</page:param>
            <page:param name="submitButtonText"><ww:property value="submitButtonName" escape="false" /></page:param>
            <page:param name="cancelLinkURI"><ww:url value="'default.jsp'" atltoken="false"/></page:param>
            <page:param name="isMultipart">true</page:param>

            <aui:component template="formHeading.jsp" theme="'aui'">
                <aui:param name="'text'"><ww:text name="'createissue.title'"/></aui:param>
            </aui:component>

            <aui:component name="'pid'" template="hidden.jsp" theme="'aui'" />
            <aui:component name="'issuetype'" template="hidden.jsp" theme="'aui'" />

            <page:applyDecorator name="auifieldgroup">
                <aui:component id="'project-name'" label="text('issue.field.project')" name="'project/string('name')'" template="formFieldValue.jsp" theme="'aui'" />
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <aui:component id="'issue-type'" label="text('issue.field.issuetype')" name="'issueType'" template="formIssueType.jsp" theme="'aui'">
                    <aui:param name="'issueType'" value="/constantsManager/issueType(issuetype)" />
                </aui:component>
            </page:applyDecorator>

            <ww:component template="issuefields.jsp" name="'createissue'">
                <ww:param name="'displayParams'" value="/displayParams"/>
                <ww:param name="'issue'" value="/issueObject"/>
                <ww:param name="'tabs'" value="/fieldScreenRenderTabs"/>
                <ww:param name="'errortabs'" value="/tabsWithErrors"/>
                <ww:param name="'selectedtab'" value="/selectedTab"/>
                <ww:param name="'ignorefields'" value="/ignoreFieldIds"/>
                <ww:param name="'create'" value="'true'"/>
            </ww:component>

            <jsp:include page="/includes/panels/updateissue_comment.jsp" />

        </page:applyDecorator>
    </div>
</ww:if>
<ww:else>
    <%-- Doesn't look like you can fall into this block anymore --%>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <%@ include file="/includes/createissue-notloggedin.jsp" %>
        </aui:param>
    </aui:component>
</ww:else>
</body>
</html>
