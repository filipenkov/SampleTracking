<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="/issueExists == true && /hasIssuePermission('link', /issue) == true ">
        <meta content="issueaction" name="decorator"/>
    </ww:if>
    <ww:else>
        <meta content="message" name="decorator" />
    </ww:else>
    <title><ww:text name="'linkissue.title'"/></title>
</head>
<body class="type-a">
<ww:if test="/issueExists == true && /hasIssuePermission('link', /issue) == true ">
    <div class="content intform">
        <page:applyDecorator id="issue-link" name="auiform">
            <page:param name="action">LinkExistingIssue.jspa</page:param>
            <page:param name="submitButtonName">Link</page:param>
            <page:param name="showHint">true</page:param>
            <ww:property value="/hint('link')">
                <ww:if test=". != null">
                    <page:param name="hint"><ww:property value="./text" escape="false" /></page:param>
                    <page:param name="hintTooltip"><ww:property value="./tooltip" escape="false" /></page:param>
                </ww:if>
            </ww:property>
            <page:param name="submitButtonText"><ww:text name="'linkissue.submitname'"/></page:param>
            <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false"/></page:param>

                <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
                    <aui:param name="'title'"><ww:text name="'linkissue.title'"/></aui:param>
                    <aui:param name="'subtaskTitle'"><ww:text name="'linkissue.title.subtask'"/></aui:param>
                    <aui:param name="'issueKey'"><ww:property value="/issueObject/key"  escape="false"/></aui:param>
                    <aui:param name="'issueSummary'"><ww:property value="/issueObject/summary"  escape="false"/></aui:param>
                    <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
                    <aui:param name="'cameFromParent'" value="/cameFromParent"/>
                </aui:component>

                <aui:component name="'id'" template="hidden.jsp" theme="'aui'" />

                <page:applyDecorator name="auifieldset">
                    <page:param name="legend"><ww:text name="'linkissue.issue.details'"/></page:param>

                    <page:applyDecorator name="auifieldgroup">
                        <aui:param name="'description'"><ww:text name="'linkissue.this.desc'"/></aui:param>
                        <aui:select id="'link-type'" label="text('linkissue.this')" list="linkDescs" listKey="'.'" listValue="'.'" name="'linkDesc'" theme="'aui'" />
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldgroup">
                        <aui:component label="text('common.concepts.issues')" name="'linkKey'" theme="'aui'" template="issuepicker.jsp">
                            <aui:param name="'size'" value="18"/>
                            <aui:param name="'formname'" value="'jiraform'"/>
                            <aui:param name="'currentIssue'" value="issue/string('key')" />
                            <aui:param name="'currentJQL'" value="/currentJQL" />
                            <aui:param name="'currentValue'" value="/currentValue" />
                        </aui:component>
                    </page:applyDecorator>

                </page:applyDecorator>

                <%@ include file="/includes/panels/updateissue_comment.jsp" %>
        </page:applyDecorator>
    </div>
</ww:if>
<ww:else>
    <page:applyDecorator name="auiissueerrorpanel">
        <page:param name="title"><ww:text name="'linkissue.title'"/></page:param>
    </page:applyDecorator>
</ww:else>
</body>
</html>