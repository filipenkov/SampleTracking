<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta content="issuesummary" name="decorator" />
	<title><ww:text name="'trackback.delete.title'"/></title>
</head>
<body class="type-a">
<div class="item-header">
    <ww:property value="issue">
        <jsp:include page="/includes/panels/issue_headertable.jsp" />
    </ww:property>
</div>
<div id="main-content">
    <div class="active-area">
        <div id="primary" class="column">
            <div class="content intform">
                <page:applyDecorator name="jiraform">
                    <page:param name="title"><ww:text name="'trackback.delete.title'"/>: <ww:property value="/trackback(trackbackId)/title" /></page:param>
                    <page:param name="autoSelectFirst">false</page:param>
                    <page:param name="description">
                        <p><ww:text name="'trackback.delete.desc'"/></p>
                    </page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="action"><ww:url page="DeleteTrackback.jspa"><ww:param name="'id'" value="/id"/><ww:param name="'trackbackId'" value="trackbackId"/></ww:url></page:param>
                    <page:param name="submitId">delete_submit</page:param>
                    <page:param name="submitName">Delete</page:param>
                    <page:param name="cancelURI"><ww:url page="ManageTrackbacks.jspa"><ww:param name="'id'" value="/id" /></ww:url></page:param>
                    <input type="hidden" name="confirm" value="true">
                </page:applyDecorator>
            </div>
        </div>
    </div>
</div>
</body>
</html>
