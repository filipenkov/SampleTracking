<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    <%
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:jira-fields");
    %>
    <title><ww:text name="'user.profile'"/>: <ww:property value="user/displayName"/></title>
    <script type="text/javascript">window.dhtmlHistory.create();</script>
</head>
<body>
    <header>
        <jsp:include page="profile/viewprofile-tools.jsp" />
        <div id="heading-avatar">
            <img alt="<ww:property value="user/fullName" />" height="48" src="<ww:property value="/avatarUrl(user)"/>" width="48" />
        </div>
        <h1 id="up-user-title"><ww:text name="'common.concepts.profile'"/>: <span id="up-user-title-name"><ww:property value="user/displayName"/></span><ww:if test="user/active == false"> (<ww:text name="'admin.common.words.inactive'"/>)</ww:if></h1>
    </header>
    <div class="content-container">
        <ww:if test="/hasMoreThanOneProfileTabs == true">
            <div class="content-related">
                <jsp:include page="profile/viewprofile-tabs.jsp" />
            </div>
        </ww:if>
        <div class="content-body aui-panel">
            <ww:if test="/noTitle == false">
                <h2><ww:property value="/labelForSelectedTab"/></h2>
            </ww:if>
            <jsp:include page="profile/viewprofile-content.jsp" />
        </div>
    </div>
</body>
</html>
