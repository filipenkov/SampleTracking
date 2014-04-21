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
        //determine what tab should be active!
        request.setAttribute("jira.selected.section", helper.getSelectedMenuSection());

        final WebResourceManager wrm = ComponentManager.getInstance().getWebResourceManager();

        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.admin);

        wrm.requireResourcesForContext("atl.admin");
        wrm.requireResourcesForContext("jira.admin");
        request.setAttribute("jira.web.resource.context", "atl.admin");
    }
%>

<%@ include file="/includes/decorators/header.jsp" %>
<ww:property value="@adminHelper">

<div id="admin-config">
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
    <div id="admin-headers">
        <ww:iterator value="headers">
            <ww:property value="html" escape="false"/>
        </ww:iterator>
    </div>
    <div id="admin-config-content" class="<ww:if test="numberOfTabs > 1">aui-tabs vertical-tabs</ww:if><ww:else>no-tabs</ww:else>">
        <div class="admin-active-wrap">
            <ww:property value="tabHtml" escape="false"/>
            <div class="admin-active-area">
                <decorator:body/>
            </div>
        </div>
    </div>
</div>
</ww:property>
<%@ include file="/includes/decorators/footer.jsp" %>
