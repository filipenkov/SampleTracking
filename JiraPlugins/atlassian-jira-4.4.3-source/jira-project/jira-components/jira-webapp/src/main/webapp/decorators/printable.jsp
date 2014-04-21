<!DOCTYPE html>
<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.jira.ManagerFactory"%>
<%@ page import="com.atlassian.jira.config.properties.APKeys"%>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties"%>
<%@ page import="com.atlassian.jira.config.properties.LookAndFeelBean"%>
<%@ page import="com.atlassian.plugin.webresource.UrlMode"%>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%-- a printable decorator --%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>

<%-- get the decorator page object, for use within the decorator --%>
<decorator:usePage id="decoratorPage" />
<%
    ApplicationProperties ap = ManagerFactory.getApplicationProperties();

    final LookAndFeelBean lAndF = LookAndFeelBean.getInstance(ap);
    String linkColour = lAndF.getTextLinkColour();
    String linkAColour = lAndF.getTextActiveLinkColour();

    WebResourceManager webResourceManagerPrintable = ComponentManager.getInstance().getWebResourceManager();
    final KeyboardShortcutManager shortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
%>
<html>
<head>
	<title><%= TextUtils.htmlEncode(ap.getDefaultBackedString(APKeys.JIRA_TITLE)) %> - <decorator:title default="New Generation Issue Tracking" /></title>
<%
    webResourceManagerPrintable.requireResource("jira.webresources:global-static");
    webResourceManagerPrintable.requireResource("jira.webresources:jira-global");
    webResourceManagerPrintable.includeResources(out, UrlMode.RELATIVE);
%>
    <%-- Ensure that the printable style sheet is last so it overrides the previous styles --%>
    <link rel="stylesheet" type="text/css" media="print,screen" href="<%= webResourceManagerPrintable.getStaticResourcePrefix(String.valueOf(lAndF.getVersion()), UrlMode.RELATIVE) %>/styles/printable.css">
    <% if (shortcutManager.isKeyboardShortcutsEnabled()) { %>
        <script type="text/javascript" src="<%=request.getContextPath() + TextUtils.htmlEncode(shortcutManager.includeShortcuts())%>"></script>
    <% } %>
<style type="text/css">
.linkback
{
    background-color:#fff;
    width:100%;
    border:0;
    font-weight:bold;
}
</style>
<style type="text/css" media="print">
.linkback
{
    display: none;
}
</style>
</head>

<body bgcolor="#ffffff" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" link="<%= linkColour %>" vlink="<%= linkColour %>" alink="<%= linkAColour %>">
<table class="linkback" cellpadding="2" cellspacing="0">
    <tr><td>&nbsp;<a href="#" onClick="javascript:history.go(-1); ">&lt;&lt; <ww:text name="'common.concepts.back.to.previous.view'"/></a></td></tr>
	<tr><td bgcolor="#003366"><img src="<%= request.getContextPath() %>/images/border/spacer.gif" height="1" width="1" alt=""/></td></tr>
</table>
<div id="printableBody">
    <decorator:body />
</div>
<script language="JavaScript" type="text/javascript">
<!--
    <%-- Javascript to prevent links from working under this view. The bestest of solution would be to filter this through a servelt that strips all the anchor tags.. If it's *that* important --%>
    var printableBody = document.getElementById("printableBody");
    var anch = printableBody.getElementsByTagName("A");
    for(i=0;i<anch.length;i++)
    {
        anch[i].href = '';
        anch[i].onclick = '';
        anch[i].removeAttribute('href');
    }

    var tds = printableBody.getElementsByTagName("TD");
    for(i=0;i<tds.length;i++)
    {
        if(tds[i].onclick)
        {
            tds[i].onclick = '';
        }
    }
//-->
</script>

</body>
</html>
