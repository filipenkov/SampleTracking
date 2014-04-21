<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
</head>
<body>
    <header>
        <h1><ww:text name="'moveissue.title'"/></h1>
    </header>
    <div class="content-container">
        <div class="content-related">
            <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator name="jiraform">
                <page:param name="title"><ww:text name="'bulkworkflowtransition.error'"/></page:param>
                <page:param name="autoSelectFirst">false</page:param>
                <page:param name="action">IssueNavigator.jspa</page:param>
                <page:param name="width">100%</page:param>
                <page:param name="submitId">ok_submit</page:param>
                <page:param name="submitName">&nbsp;OK&nbsp;</page:param>
            </page:applyDecorator>
        </div>
    </div>
</body>
</html>
