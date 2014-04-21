<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_users_menu/shared_section"/>
    <meta name="admin.active.tab" content="shared_filters"/>
    <%@ include file="/includes/decorators/xsrftoken.jsp" %>
	<title><ww:text name="'admin.issues.filters.shared.title'"/></title>
</head>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:shared-filters-admin");
%>
<body>

<div id="main-content">
    <%--  // SEARCH SHARED FILTERS FORM--%>
    <%--  // JRADEV-6783 - we can't use aui forms here as we want a table --%>
    <form id="search-filters-form" class="aui" action="ViewSharedFilters.jspa" method="post" name="search-filters-form">
        <ww:if test="/returnUrl != null">
            <ww:component name="'returnUrl'" template="hidden.jsp" theme="'aui'"  />
        </ww:if>
        <ww:component name="'atl_token'" template="hidden.jsp" theme="'aui'" value="/xsrfToken" />
        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'">Search Shared Filters</aui:param>
        </aui:component>
        <p><ww:text name="'sharedfilters.search.long.desc'"/></p>
        <table class="filterSearchInput" cellpadding="0" cellspacing="0">
            <tr>
                <td class="filterSearchInputRightAligned fieldLabelArea"><ww:text name="'common.concepts.search'"/></td>
                <td>
                <ui:component label=" " name="'searchName'" template="text.jsp" theme="'aui'">
                    <ui:param name="'formname'" value="'search-filters-form'"/>
                    <ui:param name="'mandatory'" value="false"/>
                    <ui:param name="'size'" value="50"/>
                    <ui:param name="'maxlength'" value="50"/>
                    <ui:param name="'description'" value="text('filters.search.text.desc')"/>
                </ui:component>
                </td>
                <td class="fieldLabelArea" width="10%"><ww:text name="'admin.common.words.owner'"/></td>
                <td>
                <ui:component label=" "  id="'searchOwnerUserName'" name="'searchOwnerUserName'" template="userselect.jsp" theme="'aui'">
                    <ui:param name="'formname'" value="'search-filters-form'"/>
                    <ui:param name="'mandatory'" value="false"/>
                </ui:component>
                </td>
            </tr>
            <tr class="buttons">
                <td>&nbsp;</td>
                <td colspan="3">
                    <input name="Search" type="submit" value="<ww:text name="'common.concepts.search'"/>"/>
                </td>
            </tr>
        </table>
    </form>
    <div id="shared-filter-search-results">
         <jsp:include page="shared-filters-content.jsp" />
    </div>
</div>
</body>
</html>
