<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>

<html>
<head>
    <title><ww:text name="'portal.configure'"/></title>
    <meta name="decorator" content="general" />
    <content tag="section">home_link</content>
</head>
<body>
<script type="text/javascript">window.dhtmlHistory.create();</script>
<div class="item-header" id="content-top">
    <div class="operations-container">
        <ul class="operations">
            <li><a class="lnk no-icon" id="create_page" href="<ww:url page="AddPortalPage!default.jspa"></ww:url>"><ww:text name="'addportalpage.clone.blank'"/></a>
            <li><a class="lnk no-icon" id="restore_defaults" href="<ww:url page="RestoreDefaultDashboard!default.jspa"><ww:param name="'destination'" value="'manageportal'"/></ww:url>"><ww:text name="'restoredefaultdashboard.restoredefaults'"/></a>
        </ul>
    </div>
    <h1 class="item-name avatar">
        <img id="project-avatar" alt="" class="project-avatar-48" height="48" src="<%= request.getContextPath() %>/images/icons/dashboards_manage_48.png" width="48" />
        <span><ww:text name="'configureportalpages.title'"/></span>
    </h1>
    <h2 class="item-summary"><ww:text name="'configureportalpages.title'"/></h2>
</div>
<div id="main-content">
    <jsp:include page="configureportalpages-tabs.jsp" />
    <div class="active-area">
        <jsp:include page="configureportalpages-content.jsp" />
    </div>
</div>
</body>
</html>
