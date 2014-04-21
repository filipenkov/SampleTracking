<%@ page import="com.atlassian.jira.config.ReindexMessageManager" %>
<%@ page import="com.atlassian.jira.security.JiraAuthenticationContext" %>
<%@ page import="com.atlassian.jira.user.util.UserUtil" %>
<%@ page import="com.atlassian.jira.util.ComponentFactory" %>
<%@ page import="com.atlassian.jira.web.sitemesh.AdminDecoratorHelper" %>
<%@ page import="com.atlassian.jira.security.PermissionManager" %>
<%@ page import="com.atlassian.jira.security.Permissions" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="ww"%>
<%@ taglib uri="webwork" prefix="aui"%>
<decorator:usePage id="configPage"/>
<%
    {
        final ComponentFactory factory = ComponentManager.getComponentInstanceOfType(ComponentFactory.class);
        final AdminDecoratorHelper helper = factory.createObject(AdminDecoratorHelper.class);

        helper.setCurrentSection(configPage.getProperty("meta.admin.active.section"));
        helper.setCurrentTab(configPage.getProperty("meta.admin.active.tab"));
        helper.setProject(configPage.getProperty("meta.projectKey"));

        request.setAttribute("adminHelper", helper);
        request.setAttribute("jira.admin.mode",true);
        request.setAttribute("jira.selected.section", helper.getSelectedMenuSection()); // Determine what tab should be active

        // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "atl.admin"
        final WebResourceManager adminWebResourceManager = ComponentManager.getInstance().getWebResourceManager();
        adminWebResourceManager.requireResourcesForContext("atl.admin");
        adminWebResourceManager.requireResourcesForContext("jira.admin");

        final KeyboardShortcutManager adminKeyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        adminKeyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.admin);
    }
%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="/includes/decorators/aui-layout/head-common.jsp" %>
    <%@ include file="/includes/decorators/aui-layout/head-resources.jsp" %>
    <decorator:head/>
</head>
<body id="jira" class="aui-layout aui-theme-default page-type-admin <decorator:getProperty property="body.class" />">
<div id="page">
    <header id="header" role="banner">
        <%@ include file="/includes/decorators/aui-layout/notifications-header.jsp" %>
        <%@ include file="/includes/decorators/unsupported-browsers.jsp" %>
        <%@ include file="/includes/decorators/aui-layout/header.jsp" %>
    </header>
    <%@ include file="/includes/decorators/aui-layout/notifications-content.jsp" %>
    <section id="content" role="main">
        <%
            ReindexMessageManager reindexMessageManager = ComponentManager.getComponentInstanceOfType(ReindexMessageManager.class);
            JiraAuthenticationContext authenticationContext = ComponentManager.getComponentInstanceOfType(JiraAuthenticationContext.class);
            final boolean isAdmin = ComponentManager.getComponentInstanceOfType(PermissionManager.class).hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser());
            final String message = reindexMessageManager.getMessage(authenticationContext.getLoggedInUser());
            if (isAdmin && !StringUtils.isBlank(message))
            {
        %>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'helpKey'">reindex_after_configuration_changes</aui:param>
            <aui:param name="'messageHtml'"><%= message %></aui:param>
        </aui:component>
        <%
            }

            UserUtil userUtil = ComponentManager.getComponentInstanceOfType(UserUtil.class);
            if (isAdmin && userUtil.hasExceededUserLimit())
            {
        %>
        <aui:component id="'adminMessages2'" template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <ww:text name="'admin.globalpermissions.user.limit.warning'">
                    <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/ViewLicense!default.jspa"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </aui:param>
        </aui:component>
        <%
            }
        %>
        <div class="content-container">
            <div class="content-body aui-panel">
                <decorator:body />
            </div>
        </div>
    </section>
    <footer id="footer" role="contentinfo">
        <%--<%@ include file="/includes/decorators/aui-layout/notifications-footer.jsp" %>--%>
        <%@ include file="/includes/decorators/aui-layout/footer.jsp" %>
    </footer>
</div>
</body>
</html>
