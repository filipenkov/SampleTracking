<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%
    // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "navigator"
    ComponentManager.getInstance().getWebResourceManager().requireResourcesForContext("jira.navigator");

    KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
    keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issueaction);
    keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
%>

<%@ include file="/includes/decorators/header-deprecated.jsp" %>

<decorator:body />

<%@ include file="/includes/decorators/footer.jsp" %>
