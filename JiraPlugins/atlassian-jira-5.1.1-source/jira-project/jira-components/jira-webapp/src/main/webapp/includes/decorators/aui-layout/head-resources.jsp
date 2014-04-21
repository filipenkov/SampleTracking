<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.plugin.web.WebInterfaceManager" %>
<%@ page import="com.atlassian.plugin.web.model.WebPanel" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.atlassian.plugin.web.model.WebPanel" %>
<!--[if IE]><![endif]--><%-- Leave this here - it stops IE blocking resource downloads - see http://www.phpied.com/conditional-comments-block-downloads/ --%>
<script type="text/javascript">var contextPath = '<%=request.getContextPath()%>';</script>
<%
    final WebResourceManager commonWebResourceManager = ComponentManager.getComponent(WebResourceManager.class);
    commonWebResourceManager.requireResource("jira.webresources:global-static");
    commonWebResourceManager.requireResource("jira.webresources:jira-global");
    commonWebResourceManager.requireResource("jira.webresources:key-commands");
    commonWebResourceManager.requireResource("jira.webresources:header");
    commonWebResourceManager.requireResource("jira.webresources:set-focus");
    commonWebResourceManager.requireResourcesForContext("atl.global");
    commonWebResourceManager.requireResourcesForContext("jira.global");
    commonWebResourceManager.includeResources(out, UrlMode.RELATIVE);

    final KeyboardShortcutManager commonShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
%>
    <script type="text/javascript" src="<%=request.getContextPath() + TextUtils.htmlEncode(commonShortcutManager.includeShortcuts())%>"></script>
<%

    final WebInterfaceManager headResourcesWebInterfaceManager = ComponentManager.getComponentInstanceOfType(WebInterfaceManager.class);

    Map<String, Object> headResourcesContext = Collections.emptyMap();
    List<WebPanel> headResourcesDisplayableWebPanels = headResourcesWebInterfaceManager.getDisplayableWebPanels("atl.header.after.scripts", headResourcesContext);
    for (WebPanel webPanel : headResourcesDisplayableWebPanels) {%>
        <%=webPanel.getHtml(headResourcesContext)%>
    <%}
%>
