<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'common.concepts.managefilters'"/></title>
    <meta name="decorator" content="general" />
    <content tag="section">find_link</content>
</head>
<body>
<script type="text/javascript">window.dhtmlHistory.create();</script>
<div class="item-header" id="content-top">
    <h1 class="item-name avatar">
        <img id="project-avatar" alt="" class="project-avatar-48" height="48" src="<%= request.getContextPath() %>/images/icons/filter_48.png" width="48" />
        <span><ww:text name="'managefilters.title'"/></span>
    </h1>
    <h2 class="item-summary"><ww:text name="'managefilters.title'"/></h2>
</div>
<div id="main-content">
    <jsp:include page="managefilters-tabs.jsp" />
    <div class="active-area">
        <jsp:include page="managefilters-content.jsp" />
    </div>
</div>
</body>
</html>

