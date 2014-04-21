<!DOCTYPE html>
<%@ page import="com.atlassian.jira.ManagerFactory,
                 com.atlassian.jira.config.properties.APKeys,
                 com.atlassian.jira.config.properties.ApplicationProperties" %>
<%@ page import="com.atlassian.jira.config.properties.LookAndFeelBean" %>
<%@ page import="com.atlassian.jira.util.BrowserUtils" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.opensymphony.user.User" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>

<%
    ApplicationProperties ap = ManagerFactory.getApplicationProperties();
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    final LookAndFeelBean lAndF = LookAndFeelBean.getInstance(ap);
%>

<%-- get the decorator page object, for use within the decorator --%>
<decorator:usePage id="decoratorPage"/>
<html>
<head>
    <title><decorator:title default="JIRA"/></title>
    <meta http-equiv="Content-Type" content="<%= ap.getContentType() %>"/>
    <meta http-equiv="Pragma" content="no-cache"/>
    <meta http-equiv="Expires" content="-1"/>

    <script type="text/javascript">var contextPath = "<%=request.getContextPath()%>"</script>
    <%@ include file="/includes/decorators/xsrftoken.jsp" %>
    <%
        webResourceManager.requireResource("jira.webresources:global-static");
        webResourceManager.requireResource("jira.webresources:jira-global");
        // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "atl.popup"
        webResourceManager.requireResourcesForContext("atl.popup");
        webResourceManager.requireResourcesForContext("jira.popup");
        webResourceManager.requireResource("jira.webresources:set-focus");

        webResourceManager.includeResources(out, UrlMode.AUTO);
    %>
</head>
<%
    String topBgColour = lAndF.getTopBackgroundColour();
    String topSepBgColour = lAndF.getTopSeparatorBackgroundColor();
    String menuBgColour = lAndF.getMenuBackgroundColour();
    String linkColour = lAndF.getTextLinkColour();
    String linkAColour = lAndF.getTextActiveLinkColour();
    String applicationID = lAndF.getApplicationID();

    String jiraLogo = lAndF.getLogoUrl();
    if (jiraLogo != null && !jiraLogo.startsWith("http://") && !jiraLogo.startsWith("https://"))
    {
        jiraLogo = request.getContextPath() + jiraLogo;
    }

    String userAgent = TextUtils.noNull(request.getHeader("USER-AGENT"));
    boolean logoNeedsOpacityFix = jiraLogo != null && jiraLogo.endsWith(".png") && BrowserUtils.isFilterBasedPngOpacity(userAgent);
    final User user = ComponentManager.getInstance().getJiraAuthenticationContext().getUser();
    request.setAttribute("username", user == null ? null : user.getName());
%>
<body id="<%= applicationID %>" <decorator:getProperty property="body.class" writeEntireProperty="true"/>>
<div id="header-top">
    <table border="0" cellpadding="0" cellspacing="0" width="100%" bgcolor="#ffffff">
        <tr>
            <td bgcolor="<%= topBgColour %>">
                <ww:component name="'default'" template="logoWithOpacity.jsp">
                    <ww:param name="'needsOpacityFix'"><%= logoNeedsOpacityFix %></ww:param>
                    <ww:param name="'logoTitle'"><%= TextUtils.htmlEncode(ap.getDefaultBackedString(APKeys.JIRA_TITLE)) %></ww:param>
                    <ww:param name="'logoUrl'"><%= jiraLogo %></ww:param>
                    <ww:param name="'logoWidth'"><%= lAndF.getLogoWidth() %></ww:param>
                    <ww:param name="'logoHeight'"><%= lAndF.getLogoHeight() %></ww:param>
                </ww:component>
            </td>
            <td bgcolor="<%= topBgColour %>" align="right">
                <ww:if test="@username != null">
                    <span id="popup-profile-user"><jira:formatuser user="@username" type="'fullName'" id="'popup-user-link'"/></span>
                </ww:if>
            </td>
        </tr>
        <tr>
            <td colspan="2" bgcolor="<%= menuBgColour %>">
                <img src="<%= request.getContextPath() %>/images/border/spacer.gif" height="5" width="1" alt=""></td>
        </tr>
    </table>
    <table border="0" cellpadding="10" cellspacing="0" width="100%" bgcolor="#ffffff">
        <tr>
            <td>
                <decorator:body/>
            </td>
        </tr>
    </table>
</div>
</body>
</html>
