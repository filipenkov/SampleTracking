<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'bulkworkflowtransition.title'"/></title>
    <script language="javascript">
        function check(id)
        {
            eval("document.jiraform." + id + ".checked = true");
        }
    </script>
</head>
<body>
    <!-- Step 3 - Bulk Operation: Operation Details -->
    <div class="content-container" id="stepped-process">
        <div class="content-related">
            <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator name="jirapanel">
                <page:param name="title"><ww:text name="'bulkedit.title'"/> <ww:text name="'bulkedit.step3'"/>: <ww:text name="'bulkedit.step3.title'"/></page:param>
                <page:param name="description">
                    <ww:text name="'bulkworkflowtransition.select.action'" />
                </page:param>
                <page:param name="width">100%</page:param>
            </page:applyDecorator>
            <form class="aui" name="jiraform" action="BulkWorkflowTransitionDetailsValidation.jspa" method="POST">
                <%@include file="bulkchooseaction_submit_buttons.jsp"%>
                <%@include file="/secure/views/bulkedit/includes/bulkworkflowtransition_transitionmapping.jsp"%>
                <%@include file="bulkchooseaction_submit_buttons.jsp"%>
            </form>
        </div>
    </div>
</body>
</html>
