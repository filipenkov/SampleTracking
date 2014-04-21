<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<!-- Step 3 - Bulk Operation: Operation Details -->

<html>
<head>
	<title><ww:text name="'bulkworkflowtransition.title'"/> </title>
</head>
<body class="nl">
<script language="javascript">
    function check(id)
    {
        eval("document.jiraform." + id + ".checked = true");
    }
</script>
<div id="stepped-process">
    <div class="steps-wrap">
        <div class="steps-container">
            <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
        </div>

        <div class="current-step">
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
</div>
</body>
</html>
