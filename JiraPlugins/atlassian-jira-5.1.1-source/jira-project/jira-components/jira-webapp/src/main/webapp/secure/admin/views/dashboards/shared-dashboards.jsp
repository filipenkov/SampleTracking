<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_users_menu/shared_section"/>
    <meta name="admin.active.tab" content="shared_dashboards"/>
    <%@ include file="/includes/decorators/xsrftoken.jsp" %>
	<title><ww:text name="'admin.issues.dashboards.shared.title'"/></title>
    <jira:web-resource-require modules="jira.webresources:shared-dashboards-admin" />
</head>

<body>
<div id="main-content">
    <%--  // SEARCH SHARED DASHBOARD FORM--%>
    <%--  // JRADEV-6783 - we can't use aui forms here as we want a table --%>
    <form id="search-dashboards-form" class="aui" action="ViewSharedDashboards.jspa" method="post" name="search-dashboards-form">
        <ww:if test="/returnUrl != null">
            <ww:component name="'returnUrl'" template="hidden.jsp" theme="'aui'"  />
        </ww:if>
        <ww:component name="'atl_token'" template="hidden.jsp" theme="'aui'" value="/xsrfToken" />
        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'shareddashboards.search.title'"/></aui:param>
            <aui:param name="'escape'" value="'false'" />
        </aui:component>
        <p><ww:text name="'shareddashboards.search.long.desc'"/></p>
        <table class="filterSearchInput" cellpadding="0" cellspacing="0">
            <tr>
                <td class="filterSearchInputRightAligned fieldLabelArea"><ww:text name="'common.concepts.search'"/></td>
                <td>
                <ui:component label=" " name="'searchName'" template="text.jsp" theme="'aui'">
                    <ui:param name="'formname'" value="'search-dashboards-form'"/>
                    <ui:param name="'mandatory'" value="false"/>
                    <ui:param name="'size'" value="50"/>
                    <ui:param name="'maxlength'" value="50"/>
                    <ui:param name="'description'" value="text('portalpage.search.text.desc')"/>
                </ui:component>
                </td>
                <td class="fieldLabelArea" width="10%"><ww:text name="'admin.common.words.owner'"/></td>
                <td>
                <ui:component label=" "  id="'searchOwnerUserName'" name="'searchOwnerUserName'" template="userselect.jsp" theme="'aui'">
                    <ui:param name="'formname'" value="'search-dashboards-form'"/>
                    <ui:param name="'mandatory'" value="false"/>
                </ui:component>
                </td>
            </tr>
            <tr class="buttons">
                <td>&nbsp;</td>
                <td colspan="3">
                    <input class="aui-button" name="Search" type="submit" value="<ww:text name="'common.concepts.search'"/>"/>
                </td>
            </tr>
        </table>
    </form>

    <div id="shared-dashboard-search-results">
         <jsp:include page="shared-dashboards-contents.jsp" />
    </div>
</div>
</body>
</html>
