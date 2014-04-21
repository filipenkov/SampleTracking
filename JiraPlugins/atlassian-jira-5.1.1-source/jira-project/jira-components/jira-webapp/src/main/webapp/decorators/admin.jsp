<%@ page import="com.atlassian.jira.util.ComponentFactory" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.web.sitemesh.AdminDecoratorHelper" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
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
        <%@ include file="/includes/admin/admin-info-notifications.jsp" %>
        <ww:property value="@adminHelper">
            <ww:if test="hasHeader == true">
                <header>
                    <ww:property value="headerHtml" escape="false" />
                </header>
            </ww:if>
            <div class="content-container">
                <ww:property value="tabHtml" escape="false"/>
                <div class="content-body aui-panel">
                    <decorator:body />
                </div>
            </div>
        </ww:property>
    </section>
    <footer id="footer" role="contentinfo">
        <%--<%@ include file="/includes/decorators/aui-layout/notifications-footer.jsp" %>--%>
        <%@ include file="/includes/decorators/aui-layout/footer.jsp" %>
    </footer>
</div>
</body>
</html>
