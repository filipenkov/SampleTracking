<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.jira.ManagerFactory"%>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties"%>
<%@ page import="com.atlassian.jira.license.LicenseJohnsonEventRaiser"%>
<%@ page import="com.atlassian.johnson.JohnsonEventContainer"%>
<%@ page import="com.atlassian.johnson.event.Event"%>
<%@ page import="com.atlassian.johnson.event.EventType"%>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%
    ApplicationProperties ap = ManagerFactory.getApplicationProperties();
    final WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
%>
<html>
<%
    JohnsonEventContainer appEventContainer = JohnsonEventContainer.get(pageContext.getServletContext());
    //if there are Events outstanding then display them in a table
    if (appEventContainer.hasEvents() ) {
%>
<body class="errorpage">
<meta http-equiv="Refresh" content="30;"/>
<h2><ww:text name="'system.error.access.constraints.title'"/></h2>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">error</aui:param>
    <aui:param name="'messageHtml'">
        <p><ww:text name="'system.error.access.constrinats.desc'"/></p>
    </aui:param>
</aui:component>
<table class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th width="40%">
                <ww:text name="'common.words.description'"/>
            </th>
            <th width="10%">
                <ww:text name="'common.words.time'"/>
            </th>
            <th>
                <ww:text name="'common.words.level'"/>
            </th>
            <th width="40%">
                <ww:text name="'common.words.exception'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <%
        com.atlassian.jira.web.util.ExternalLinkUtil externalLinkUtil = com.atlassian.jira.web.util.ExternalLinkUtilImpl.getInstance();
        Collection events  = appEventContainer.getEvents();
        for (Iterator iterator = events.iterator(); iterator.hasNext();)
        {
            Event event = (Event) iterator.next();
            %>
        <tr>
            <td>
            <% if (EventType.get("export-illegal-xml").equals(event.getKey())) { %>
                <ww:component template="help.jsp" name="'autoexport'"><ww:param name="'helpURLFragment'"/></ww:component><br/>
            <% } %>
            <%= event.getDesc() %><br/>
            <% if (event.hasProgress()) {%>
                <br/><ww:text name="'system.error.progress.completed'">
                    <ww:param name="value0"><%=event.getProgress()%></ww:param>
                </ww:text>
            <%}%>
            <% if (EventType.get(LicenseJohnsonEventRaiser.LICENSE_INVALID).equals(event.getKey()))
               { %>
               <br/><a href="<%= request.getContextPath() %>/secure/ConfirmInstallationWithLicense!default.jspa"><ww:text name="'system.error.edit.license'"/></a>
            <% }
               else if (EventType.get(LicenseJohnsonEventRaiser.LICENSE_TOO_OLD).equals(event.getKey()))
               { %>
               <br/><a href="<%= request.getContextPath() %>/secure/ConfirmNewInstallationWithOldLicense!default.jspa"><ww:text name="'system.error.edit.license.or.evaluate'"/></a>
            <% }
               else if (EventType.get("export-illegal-xml").equals(event.getKey()))
               { %>
               <br/><a href="<%= request.getContextPath() %>/secure/CleanData!default.jspa"><ww:text name="'system.error.clean.characters.from.database'"/></a><br/>
               <ww:text name="'system.error.disable.export.on.upgrade.desc'">
                   <ww:param name="value0"><b></ww:param>
                   <ww:param name="value1"></b></ww:param>
               </ww:text> &nbsp;
            <% }
               else if (EventType.get("index-lock-already-exists").equals(event.getKey()))
               { %>
                <p>
                    <ww:text name="'system.error.unexpected.index.lock.found.desc1'"/>
                    <br/>
                    <br/>
                    <%
                       Object lockFiles = event.getAttribute("lockfiles");
                       if (lockFiles != null)
                       {
                           out.println(lockFiles);
                       }
                    %>
                    <br/>
                    <br/>
                    <ww:text name="'system.error.unexpected.index.lock.found.desc2'"/>
                </p>
                <p>
                    <ww:text name="'system.error.unexpected.index.lock.found.desc3'">
                        <ww:param name="value0"><strong></ww:param>
                        <ww:param name="value1"></strong></ww:param>
                    </ww:text>
                </p>
            <% }
               else if (EventType.get("upgrade").equals(event.getKey()))
               {
                   String exportFilePath = ManagerFactory.getUpgradeManager().getExportFilePath();
                   if (TextUtils.stringSet(exportFilePath))
                   {
                   %>
                   <br/>
                    <ww:text name="'system.error.data.before.upgrade.exported.to'">
                        <ww:param name="value0"><%= exportFilePath %></ww:param>
                    </ww:text>
                <% } %>
            <% } %>
                <!-- (<ww:text name="'system.error.type'">
                        <ww:param name="value0"><%= event.getKey().getType() %></ww:param>
                    </ww:text>) -->

            </td>
            <td><%=event.getDate()%></td>
            <td><%=event.getLevel().getLevel()%> </td>
            <td><pre><%= event.getException() == null ? "" : event.getException() %></pre></td>
        </tr>
    <% } %>
    </tbody>
</table>
<% } else { %>
<body>
<meta http-equiv="Refresh" content="30;"/>
    <h2><ww:text name="'system.error.access.constraints.title'"/></h2>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">success</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'system.error.no.problems.accessing.jira'"/></p>
        </aui:param>
    </aui:component>
        <p><a href="<%=request.getContextPath()%>/secure/Dashboard.jspa"><ww:text name="'system.error.go.to.dashboard'"/></a></p>
 <% } %>

    </body>
</html>
