<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'common.concepts.managefilters'"/></title>
    <content tag="section">find_link</content>
    <script type="text/javascript">window.dhtmlHistory.create();</script>
</head>
<body>
    <header>
        <div id="heading-avatar">
            <img alt="" height="48" src="<ww:url value="'/images/icons/filter_48.png'" atltoken="false" />" width="48" />
        </div>
        <h1><ww:text name="'managefilters.title'"/></h1>
    </header>
    <div class="content-container">
        <div class="content-related">
            <jsp:include page="managefilters-tabs.jsp" />
        </div>
        <div class="content-body aui-panel">
            <jsp:include page="managefilters-content.jsp" />
        </div>
    </div>
</body>
</html>
