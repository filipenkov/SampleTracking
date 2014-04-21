<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<!-- Step 4 - Bulk Operation: Confirmation for DELETE -->

<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
</head>
<body class="nl">
<div id="stepped-process">
    <div class="steps-wrap">
        <div class="steps-container">
            <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
        </div>

        <div class="current-step">
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
</div>
</body>
</html>
