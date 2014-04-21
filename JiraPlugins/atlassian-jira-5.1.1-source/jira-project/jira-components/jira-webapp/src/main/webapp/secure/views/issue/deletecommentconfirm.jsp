<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    <ww:if test="/issueValid == true">
        <title><ww:text name="'viewissue.commentdelete.title'"/></title>
        <meta name="decorator" content="issueaction" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/issueValid == true">
    <page:applyDecorator name="jiraform">
        <page:param name="title"><ww:text name="'viewissue.commentdelete.title'"/></page:param>
        <page:param name="description">
            <p><ww:text name="'viewissue.commentdelete.message'"/></p>
        </page:param>
        <page:param name="width">100%</page:param>
        <page:param name="action">DeleteComment.jspa</page:param>
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="cancelURI"><ww:url value="/issuePath" atltoken="false"/></page:param>
        <ww:if test="/errorMessages/size == 0">
            <tr class="fieldArea">
                <td class="fieldLabelArea"><ww:text name="'viewissue.comment.author'" />:</td>
                <td class="fieldValueArea"><jira:formatuser user="/commentObject/author" type="'profileLink'" id="'comment_summary'"/></td>
            </tr>
            <tr class="fieldArea">
                <td class="fieldLabelArea"><ww:text name="'viewissue.comment.created'" />:</td>
                <td class="fieldValueArea"><ww:property value="/dmyDateFormatter/format(/commentObject/created)"/></td>
            </tr>
            <ww:if test="/commentObject/created/equals(/commentObject/updated) == false">
                <tr class="fieldArea">
                    <td class="fieldLabelArea"><ww:text name="'viewissue.comment.update.author'" />:</td>
                    <td class="fieldValueArea"><jira:formatuser user="/commentObject/updateAuthor" type="'profileLink'" id="'comment_summary_updated'"/></td>
                </tr>
                <tr class="fieldArea">
                    <td class="fieldLabelArea"><ww:text name="'viewissue.comment.updated'" />:</td>
                    <td class="fieldValueArea"><ww:property value="/dmyDateFormatter/format(/commentObject/updated)"/></td>
                </tr>
            </ww:if>
            <ww:if test="/commentObject/level != null">
                <tr class="fieldArea">
                    <td class="fieldLabelArea"><ww:text name="'viewissue.comment.visibleby'"/>:</td>
                    <td class="fieldValueArea"><ww:property value="/commentObject/level" /></td>
                </tr>
            </ww:if>
            <tr class="fieldArea">
                <td class="fieldLabelArea"><ww:text name="'viewissue.comment.update.author'" />:</td>
                <td class="fieldValueArea"><jira:formatuser user="/commentObject/updateAuthor" type="'profileLink'" id="'comment_summary_updated'"/></td>
            </tr>
            <tr class="fieldArea">
                <td class="fieldLabelArea"><ww:text name="'viewissue.comment.label'" />:</td>
                <td class="fieldValueArea"><ww:property value="/renderedContent()" escape="'false'" /></td>
            </tr>
        </ww:if>
        <ui:component name="'id'" template="hidden.jsp" />
        <ui:component name="'commentId'" template="hidden.jsp" />
    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <%@ include file="/includes/issue/generic-errors.jsp" %>
    </div>
</ww:else>
</body>
</html>
