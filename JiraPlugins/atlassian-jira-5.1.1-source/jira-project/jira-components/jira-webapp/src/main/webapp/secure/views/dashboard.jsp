<%@ page import="com.atlassian.jira.web.util.ExternalLinkUtilImpl" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%
    // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "atl.dashboard"
    final WebResourceManager wrm = ComponentManager.getInstance().getWebResourceManager();
    wrm.requireResourcesForContext("atl.dashboard");
    wrm.requireResourcesForContext("jira.dashboard");
%>
<html>
<head>
    <title><ww:property value="/dashboardTitle"/></title>
    <content tag="section">home_link</content>
    <script type="text/javascript">
        AJS.$(document).ready(function() {
            AJS.warnAboutFirebug(AJS.params.firebugWarning);

            if(AJS.params.showWhitelistUpgradeWarning) {
               var message = AJS.format(AJS.params.whitelistUpgradeText, '<a href="' + contextPath + '/secure/admin/ConfigureWhitelist!default.jspa?showUpgrade=true">', '</a>');
                var $warning = AJS.$("<div id='applinks-upgrade-warning' class='global-warning'><p>" + message + "</p></div>");
                    $warning.prependTo(AJS.$("body"));
                }
        });
    </script>
</head>
<body class="page-type-dashboard">
    <ww:if test="/warningMessage != null && /warningMessage/length != 0">
        <aui:component id="dashmsg" template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:property value="/warningMessage" escape="false"/></p>
            </aui:param>
        </aui:component>
    </ww:if>
    <ww:render value="/dashboardRenderable"/>
    <fieldset class="hidden parameters">
        <input type="hidden" id="firebugWarning" value="<ww:text name="'firebug.performance.warning'">
        <ww:param name="'value0'"><a href='<%=ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.firebug.warning")%>'></ww:param>
        <ww:param name="'value1'"></a></ww:param>
        </ww:text>">
        <ww:if test="/showWhitelistUpgradeWarning == true">
            <input type="hidden" title="showWhitelistUpgradeWarning" value="true"/>
            <input type="hidden" title="whitelistUpgradeText" value="<ww:text name="'gadget.dashboard.warning'"/>"/>
        </ww:if>
    </fieldset>
</body>
</html>
