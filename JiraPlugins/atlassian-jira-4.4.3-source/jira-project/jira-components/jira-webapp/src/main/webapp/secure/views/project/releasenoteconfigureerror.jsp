<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'releasenotes.configure'" /></title>
    <meta name="decorator" content="genericaction">
</head>
<body>
<div class="content release-notes">
    <page:applyDecorator name="jiraform">
        <page:param name="title"><ww:text name="'common.concepts.releasenotes'"/></page:param>
        <page:param name="description">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:if test="versions/size <= 0 && styleNames/size <=0">
                            <ww:text name="'releasenotes.generate.note'"/>
                        </ww:if>
                        <ww:elseIf test="versions/size <= 0">
                            <ww:text name="'releasenotes.generate.versions'"/>
                        </ww:elseIf>
                        <ww:elseIf test="styleNames/size <= 0">
                            <ww:text name="'releasenotes.generate.styles'"/>
                        </ww:elseIf>
                    </p>
                </aui:param>
            </aui:component>
        </page:param>
        <page:param name="submitId">cancel_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.cancel'"/></page:param>
        <page:param name="action">Dashboard.jspa</page:param>
    </page:applyDecorator>
</div>
</body>
</html>
