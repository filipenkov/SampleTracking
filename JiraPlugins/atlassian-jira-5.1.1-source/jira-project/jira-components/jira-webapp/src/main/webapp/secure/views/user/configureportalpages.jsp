<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    <title><ww:text name="'portal.configure'"/></title>
    <content tag="section">home_link</content>
    <script type="text/javascript">window.dhtmlHistory.create();</script>
</head>
<body>
    <header>
        <div class="operations-container">
            <ul class="operations">
                <li><a class="lnk no-icon" id="create_page" href="<ww:url page="AddPortalPage!default.jspa"></ww:url>"><ww:text name="'addportalpage.clone.blank'"/></a>
                <li><a class="lnk no-icon" id="restore_defaults" href="<ww:url page="RestoreDefaultDashboard!default.jspa"><ww:param name="'destination'" value="'manageportal'"/></ww:url>"><ww:text name="'restoredefaultdashboard.restoredefaults'"/></a>
            </ul>
        </div>
        <div id="heading-avatar">
            <img alt="" height="48" src="<ww:url value="'/images/icons/dashboards_manage_48.png'" atltoken="false" />" width="48" />
        </div>
        <h1><ww:text name="'configureportalpages.title'"/></h1>
    </header>
    <div class="content-container">
        <div class="content-related">
            <jsp:include page="configureportalpages-tabs.jsp" />
        </div>
        <div class="content-body aui-panel">
            <jsp:include page="configureportalpages-content.jsp" />
        </div>
    </div>
</body>
</html>
