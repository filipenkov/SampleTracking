<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
</head>
<body>
    <!-- Step 4 - Bulk Operation: Confirmation for DELETE -->
    <div class="content-container" id="stepped-process">
        <div class="content-related">
            <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator name="jirapanel">
                <page:param name="title"><ww:text name="'bulkedit.title'"/> <ww:text name="'bulkedit.step3'"/>: <ww:text name="'bulkedit.step3.title'"/></page:param>
                <page:param name="width">100%</page:param>
            </page:applyDecorator>
            <form class="aui top-label" name="jiraform" method="POST" action="<%= request.getContextPath() %>/secure/BulkDeleteDetailsValidation.jspa">
                <jsp:include page="/includes/bulkedit/bulkedit-sendnotifications.jsp"/>
                <%@include file="bulkchooseaction_submit_buttons.jsp"%>
                <!-- Hidden field placed here so as not affect the buttons -->
                <ww:if test="/canDisableMailNotifications() == false">
                    <ui:component name="'sendBulkNotification'" template="hidden.jsp" theme="'single'" value="'true'" />
                </ww:if>
            </form>
        </div>
    </div>
</body>
</html>
