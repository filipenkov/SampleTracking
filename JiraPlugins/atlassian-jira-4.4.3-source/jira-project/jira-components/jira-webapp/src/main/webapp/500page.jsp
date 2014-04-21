<%@ page import="com.atlassian.core.logging.DatedLoggingEvent" %>
<%@ page import="com.atlassian.core.logging.ThreadLocalErrorCollection" %>
<%@ page import="com.atlassian.core.ofbiz.util.OFBizPropertyUtils" %>
<%@ page import="com.atlassian.core.util.collection.EasyList" %>
<%@ page import="com.atlassian.crowd.embedded.api.User" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.security.JiraAuthenticationContext" %>
<%@ page import="com.atlassian.jira.security.PermissionManager" %>
<%@ page import="com.atlassian.jira.security.Permissions" %>
<%@ page import="com.atlassian.jira.service.JiraServiceContainer" %>
<%@ page import="com.atlassian.jira.util.ExceptionInterpreterUtil" %>
<%@ page import="com.atlassian.jira.util.JiraContactHelper" %>
<%@ page import="com.atlassian.jira.util.I18nHelper" %>
<%@ page import="com.atlassian.jira.util.system.ExtendedSystemInfoUtils" %>
<%@ page import="com.atlassian.jira.util.system.ExtendedSystemInfoUtilsImpl" %>
<%@ page import="com.atlassian.jira.util.system.SystemInfoUtils" %>
<%@ page import="com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory" %>
<%@ page import="com.atlassian.jira.web.util.ExternalLinkUtil" %>
<%@ page import="com.atlassian.jira.web.util.ExternalLinkUtilImpl"%>
<%@ page import="com.atlassian.jira.web.util.JiraLocaleUtils"%>
<%@ page import="com.atlassian.plugin.Plugin"%>
<%@ page import="com.atlassian.plugin.PluginInformation"%>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager"%>
<%@ page import="com.atlassian.seraph.auth.DefaultAuthenticator" %>
<%@ page import="com.opensymphony.module.propertyset.PropertySet" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.apache.log4j.spi.LoggingEvent" %>
<%@ page import="org.ofbiz.core.entity.GenericValue" %>
<%@ page import="javax.servlet.ServletException" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page isErrorPage="true" %>
<%@ taglib uri="webwork" prefix="ww" %>

<%!
    private static final Logger log = Logger.getLogger("500ErrorPage.jsp");
%>
<%

    // Need to ensure request gets cached if it hasn't had the chance yet.
    DefaultVelocityRequestContextFactory.cacheVelocityRequestContext(request);
    WebResourceManager webResourceManager = null;
    try {
        webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "atl.error"
        webResourceManager.requireResourcesForContext("atl.error");
        webResourceManager.requireResourcesForContext("jira.error");
    }
    catch (Exception e)
    {
        // maybe plugins is not available?
        log.error("Can't get WebResourceManager resources: ", e);
    }
%>
<html>
	<head>
	<title>Oops - an error has occurred</title>
    <script language="JavaScript" type="text/javascript" src="<%=request.getContextPath()%>/includes/js/cookieUtil.js"></script>
    <%
        if (webResourceManager != null) {
            webResourceManager.includeResources(out);
        }
    %>

    <style type="text/css">
		h1 { color: #003366; text-align: left; margin: 0 0 30px 10px; padding: 0; border-width: 0 0 1px 0; border-style: solid; border-color: #003366;}
        p { padding: 5px 10px 5px 10px;}
		p.message { padding: 10px 30px 10px 30px; font-weight: bold; font-size: 130%; border-width: 1px; border-style: dashed; border-color: #336699; }
		p.description { padding: 10px 30px 20px 30px; border-width: 0 0 1px 0; border-style: solid; border-color: #336699;}
		p.topped { padding: 5px 10px 5px 0; margin: 0 0 0 10px; border-width: 1px 0 0 0; border-style: solid; border-color: #003366; }
        span.switch { cursor: pointer; margin-left: 5px; text-decoration: underline; }
        pre {padding: 5px 10px 5px 10px;}
   </style>
   </head>

<%
    final ExternalLinkUtil externalLinkUtil = new ExternalLinkUtilImpl();
    final I18nHelper i18nBean = ComponentManager.getComponent(JiraAuthenticationContext.class).getI18nHelper();
    final PermissionManager permissionManager = ComponentManager.getComponent(PermissionManager.class);
    final ExtendedSystemInfoUtils extendedSystemInfoUtils = new ExtendedSystemInfoUtilsImpl(i18nBean);
    final SystemInfoUtils systemInfoUtils = extendedSystemInfoUtils.getSystemInfoUtils();
    // Get the contact administrators message
    JiraContactHelper jiraContactHelper = ComponentManager.getComponentInstanceOfType(JiraContactHelper.class);
    final String contactAdministratorLink = jiraContactHelper.getAdministratorContactLinkHtml(request.getContextPath(), i18nBean);

    // orion does not support the exception object.
    boolean isOrion = "Orion".equals(systemInfoUtils.getAppServer());

    // The first line of the Exception. It is HTML escaped.
    String line = "";

    // The full stack trace of the Exception. It is HTML escaped.
    String ex = null;

    if (isOrion)
    {
       ex = (String) request.getAttribute("javax.servlet.error.message");
       //get the first line of the error message for the "cause:"

       if (ex != null)
       {
           // this is a bit of a hack to get the message from the decorated exception
           if (ex.startsWith("<PRE>"))
           {
               line = ex.substring("<PRE>".length(), ex.length());
           }
           else
           {
               line = ex;
           }
           line = line.substring(0, line.indexOf("<br>"));
       }
       else
           line = "No exception";

    }
    else
    {
       if (exception != null)
       {
           Throwable cause = exception;
           if (exception instanceof ServletException)
           {
               Throwable rootCause = ((ServletException) exception).getRootCause();
               if (rootCause != null)
                   cause = rootCause;
           }
           //log exception to the log files, so that it gets captured somewhere.
           log.error("Exception caught in 500 page " + cause.getMessage(), cause);
           line = TextUtils.htmlEncode(cause.toString());
           StringWriter sw = new StringWriter();
           PrintWriter pw = new PrintWriter(sw);
           cause.printStackTrace(pw);
           ex = TextUtils.htmlEncode(sw.toString());
       }
    }

    String interpretedMsg = ExceptionInterpreterUtil.execute(extendedSystemInfoUtils, ex);

    // Check that the user has the global admin permission to see the services/listeners params (JRA-12081)
    //
    // The 500 page is run in Tomcat outside the original FilterChain and hence the auth context thread local
    // has been torn down at this stage.  So we have to go to the session to get the user information
    //
    HttpSession httpSession = request.getSession(false); //dont create the session if there isnt one
    User remoteUser = httpSession != null ? (User) httpSession.getAttribute(DefaultAuthenticator.LOGGED_IN_KEY) : null;
    boolean isGlobalAdmin = false;
    boolean isSystemAdmin = false;
    try {
        isGlobalAdmin = remoteUser != null && permissionManager.hasPermission(Permissions.ADMINISTER, remoteUser);
        isSystemAdmin = remoteUser != null && permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, remoteUser);
    } catch (Exception dontCare) {
       log.error(dontCare);
    }
    List sysadminOnlyProperties = EasyList.build(i18nBean.getText("admin.systeminfo.system.cwd"),
                                                 i18nBean.getText("admin.systeminfo.jvm.input.arguments"),
                                                 i18nBean.getText("admin.server.id"));
%>

<body class="lp errorpage">
<div id="content-top" class="item-header" style="min-height:0">
    <h2 class="item-summary">Oops - an error has occurred</h2>
</div>
<div id="main-content">
    <div class="active-area">
<% if(interpretedMsg != null && !"".equals(interpretedMsg.trim()))
   {
%>

<h1><ww:text name="'system.error.known.title'"/></h1>
<p><ww:text name="'system.error.known.description'"/>
<p>
<ww:text name="'system.error.known.resembles'"/>
<p>
<%= interpretedMsg %>
</p>
<p>
<ww:text name="'system.error.known.notifyadmin'"/>

<% }
    else
   {
%>
<h1><ww:text name="'system.error.unknown.title'"/></h1>
<p><ww:text name="'system.error.unknown.description1'"/></p>

    <% if (!isGlobalAdmin)
    { %>
    <p><ww:text name="'system.error.unknown.description2'">
        <ww:param name="'value0'"><%=contactAdministratorLink%></ww:param>
    </ww:text></p>
    <%
    }
 }
 %>

<p class="topped">
<% if (isGlobalAdmin) { %>
    <ww:text name="'system.error.unknown.description3'"><a href="<%= request.getContextPath() %>/secure/admin/jira/JiraSupportRequest!default.jspa" target="_blank">
    <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/jira/JiraSupportRequest!default.jspa" target="_blank"></ww:param>
    <ww:param name="'value1'"></a></ww:param>
    </ww:text>
    <br/>
<%}%>
<ww:text name="'system.error.unknown.description4'">
<ww:param name="'value0'"><b></ww:param>
<ww:param name="'value1'"></b></ww:param>
<ww:param name="'value2'">
    <a href="<%= externalLinkUtil.getProperty("external.link.jira.support.site") %>"><%= externalLinkUtil.getProperty("external.link.jira.support.site") %></a>
</ww:param>
</ww:text>
<ol>
<li><ww:text name="'system.error.step1'"/>
<li><ww:text name="'system.error.step2'"/>
<% if (isSystemAdmin) { %>
<li><ww:text name="'system.error.step3'"><ww:param name="'value0'"><% out.println(extendedSystemInfoUtils.getLogPath());%></ww:param></ww:text>
<%}%>
</ol>
<p class="topped">

        <p>
        <b><ww:text name="'system.error.cause'"/>: </b><br/>
        <%= line %>
        </p>
        <% if(ex != null)
        {
        %>
            <p>
            <b><ww:text name="'system.error.stack.trace'"/>: </b> <span class="switch" id="stacktrace-switch" onclick="CookieUtil.toggleVisibility('stacktrace', '[show]', '[hide]', true)">[hide]</span>
            <pre id="stacktrace"><%= ex %></pre>
        <% }
            else
           { %>
            <p><%= TextUtils.htmlEncode((String) request.getAttribute("javax.servlet.error.message")) %></p>
        <% } %>
        <p><ww:text name="'system.error.referer.url'"/>:
            <b><%= request.getHeader("Referer") != null ? TextUtils.htmlEncode(request.getHeader("Referer")) : "Unknown" %></b></p>
        <p>
        <b><ww:text name="'system.error.build.information'"/>: </b><br/>
        <%
            final Map buildstats = extendedSystemInfoUtils.getBuildStats();
            for (Iterator i = buildstats.entrySet().iterator(); i.hasNext();)
            {
                Map.Entry entry = (Map.Entry) i.next();
                %><%= entry.getKey() %> : <%
                if (!isGlobalAdmin && i18nBean.getText("admin.server.id").equals(entry.getKey()))
                {
                    %><i><ww:text name="'system.error.property.not.admin'"/></i><br /><%
                }
                else
                {
                %><%= TextUtils.htmlEncode(String.valueOf(entry.getValue())) %><br /><%
                }
            }
        %>
        </p>
        <p>
        <b><ww:text name="'system.error.server.information'"/>: </b><br/>
            <ww:text name="'system.error.application.server'"/>: <%= TextUtils.htmlEncode(application.getServerInfo()) %><br>
            <ww:text name="'system.error.servlet.version'"/>: <%= application.getMajorVersion() %>.<%= application.getMinorVersion() %><br>
        </p>
        <p>
        <b><ww:text name="'system.error.file.paths'"/>:</b><br/>
        <%
        if (isSystemAdmin)
        {%>
           <ww:text name="'system.error.location.of.entityengine'"/>: <%= TextUtils.htmlEncode(extendedSystemInfoUtils.getEntityEngineXmlPath()) %><br>
            <ww:text name="'system.error.location.of.log'"/>: <%= TextUtils.htmlEncode(extendedSystemInfoUtils.getLogPath())%><br/>
        <%}
        else
        {%>
            <i><ww:text name="'system.error.file.paths.not.sysadmin'"/></i>
        <%}%>
        </p>
        <p>
        <b><ww:text name="'system.error.memory.information'"/>:</b><br/>
        <%
            final Map jvmStats = extendedSystemInfoUtils.getJvmStats();
            for (Iterator i = jvmStats.entrySet().iterator(); i.hasNext();)
            {
                Map.Entry entry = (Map.Entry) i.next();
                %><%= TextUtils.htmlEncode(String.valueOf(entry.getKey())) %>: <%= TextUtils.htmlEncode(String.valueOf(entry.getValue())) %><br /><%
            }
        %>
        </p>
        <p>
        <b><ww:text name="'system.error.system.information'"/>:</b><br/>
        <%
            final Map sysinfo = extendedSystemInfoUtils.getProps();
            for (Iterator i = sysinfo.entrySet().iterator(); i.hasNext();)
            {
                Map.Entry entry = (Map.Entry) i.next();
                %><%= TextUtils.htmlEncode(String.valueOf(entry.getKey())) %> : <%
                if (!isSystemAdmin && sysadminOnlyProperties.contains(entry.getKey()))
                {
                    %><i><ww:text name="'system.error.property.not.sysadmin'"/></i><br /><%
                }
                else
                {
                %><%= TextUtils.htmlEncode(String.valueOf(entry.getValue())) %><br /><%
                }
            }
        %>
        </p>

        <p>
        <b><ww:text name="'admin.systeminfo.language.info'"/>:</b><br/>
        <table id="language-info">
            <tr>
                <td valign="top"><ww:text name="'admin.generalconfiguration.installed.languages'"/>:</td>
                <td>
                    <%
                        JiraLocaleUtils jiraLocaleUtils = new JiraLocaleUtils();
                        final List installedLocales = jiraLocaleUtils.getInstalledLocales();
                        for (Iterator localeIt = installedLocales.iterator(); localeIt.hasNext();)
                        {
                            Locale locale = (Locale) localeIt.next();
                            %><%= TextUtils.htmlEncode(locale.getDisplayName(i18nBean.getLocale())) %><% if (localeIt.hasNext()) { %><br/><% }
                        }
                    %>
                </td>
            </tr>
            <tr>
                <td><ww:text name="'admin.generalconfiguration.default.language'"/>:</td>
                <td><%= TextUtils.htmlEncode(extendedSystemInfoUtils.getDefaultLanguage()) %><% if (extendedSystemInfoUtils.isUsingSystemLocale()) { %> - <ww:text name="'admin.systeminfo.system.default.locale'"/><% } %></td>
            </tr>
        </table>
        </p>
        <p>

        <b><ww:text name="'system.error.request.information'"/>:</b><br/>
        <%
            try {
                String encodedQueryString = request.getQueryString() == null ? " " : TextUtils.htmlEncode(request.getQueryString());
        %>

            - <ww:text name="'system.error.request.url'"/>: <%= TextUtils.htmlEncode("" + request.getRequestURL()) %><br>
            - <ww:text name="'system.error.scheme'"/>: <%= TextUtils.htmlEncode("" + request.getScheme()) %><br>
            - <ww:text name="'system.error.server'"/>: <%= TextUtils.htmlEncode("" + request.getServerName()) %><br>
            - <ww:text name="'system.error.port'"/>: <%= TextUtils.htmlEncode("" + request.getServerPort()) %><br>
            - <ww:text name="'system.error.uri'"/>: <%= TextUtils.htmlEncode("" + request.getRequestURI()) %><br>
            - <ww:text name="'system.error.context.path'"/>: <%= TextUtils.htmlEncode("" + request.getContextPath()) %><br>
            - - <ww:text name="'system.error.servlet.path'"/>: <%= TextUtils.htmlEncode("" + request.getServletPath()) %><br>
            - - <ww:text name="'system.error.path.info'"/>: <%= TextUtils.htmlEncode("" + request.getPathInfo()) %><br>
            - - <ww:text name="'system.error.query.string'"/>: <%= encodedQueryString %><br><br>

                <b><ww:text name="'system.error.request.attributes'"/></b><br>
            <%
                Enumeration attributeNames = request.getAttributeNames();
                while (attributeNames.hasMoreElements())
                {
                    String name = (String) attributeNames.nextElement();
                    Object attribute = request.getAttribute(name);
                    %>
                    - <%= name %> : <%= TextUtils.htmlEncode(attribute == null ? "null" : attribute.toString()) %><br>
                    <%
                }
            %>
        <%
            }
            catch (Throwable t)
            {
                out.println("Error rendering logging information - uh oh.");
                t.printStackTrace(new PrintWriter(out));
            }
        %>
        </p>

        <p>
        <b><ww:text name="'system.error.request.logging'"/></b><br/>
        <%
            try {
            List list = ThreadLocalErrorCollection.getList();
            %>
                <b><%= list.size() %></b> log statements generated by this request:<br>
            <%
            for (Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                DatedLoggingEvent dle = (DatedLoggingEvent) iterator.next();
                LoggingEvent loggingEvent = dle.getEvent();
                Date date = dle.getDate();
                %>
<%-- In case of backward compatibility issues with logging functions see JRA-5405 --%>
                <font color="darkred">[<%= TextUtils.htmlEncode(String.valueOf(loggingEvent.getLevel())) %>]</font>
                <font color="darkblue"><%= TextUtils.htmlEncode(date.toString()) %></font>
                [<%= TextUtils.htmlEncode(loggingEvent.getLoggerName()) %>]
                <%= TextUtils.htmlEncode(loggingEvent.getRenderedMessage()) %>
                <br>
                <% if (loggingEvent.getThrowableInformation() != null) { %>
                    <div style="font-size: 10px; margin: 0 0 0 20px;">
                        <%
                            final String[] throwableStrRep = loggingEvent.getThrowableStrRep();
                            for (int i = 0; i < throwableStrRep.length && i < 20; i++)
                            {
                                String s = throwableStrRep[i];
                                out.println(TextUtils.htmlEncode(s) + "<br>");
                            }
                        %>
                    </div>
                <% } %>
                <%
            }
            }
            catch (Throwable t)
            {
                out.println("Error rendering logging information - uh oh.");
                t.printStackTrace(new PrintWriter(out));
            }
        %>
        </p>

<%-- LISTENERS - start --%>
<% try { %>
<p>
    <span style="font-weight:bold;"><ww:text name="'admin.systeminfo.listeners'"/></span><br/>
    <%
        final Collection listeners = extendedSystemInfoUtils.getListeners();
        for (Iterator i = listeners.iterator(); i.hasNext();)
        {
            GenericValue gv = (GenericValue) i.next();
    %>
    &nbsp;-&nbsp;<span style="font-weight:bold;"><%= TextUtils.htmlEncode(gv.getString("name")) %></span> (<%= TextUtils.htmlEncode(gv.getString("clazz")) %>)
    <%
        if (isGlobalAdmin)
        {
            final PropertySet propertySet = OFBizPropertyUtils.getPropertySet(gv);
            final Collection keys = propertySet.getKeys("", 5);
            if (keys != null && !keys.isEmpty())
            {
                for (Iterator j = keys.iterator(); j.hasNext();)
                {
                    String key = (String) j.next();
        %><br/>&nbsp;&nbsp;&nbsp;-&nbsp;<span style="font-weight:bold;"><%= TextUtils.htmlEncode(key) %>:</span> <%= TextUtils.htmlEncode(propertySet.getString(key)) %>
        <%
                }
            }
        }
    %>
    <br/>
    <%
        }
    %>
</p>
<%
}
catch (Exception e) {
    System.out.println("Error getting listeners");
    e.printStackTrace();
}
%>
<%-- LISTENERS - end --%>

<%-- SERVICES - start --%>
<%
    try { %>
<p>
    <span style="font-weight:bold;"><ww:text name="'admin.systeminfo.services'"/></span><br/>
    <%
        final Collection services = extendedSystemInfoUtils.getServices();
        for (Iterator i = services.iterator(); i.hasNext();)
        {
            JiraServiceContainer service = (JiraServiceContainer) i.next();
    %>
    &nbsp;-&nbsp;<span style="font-weight:bold;"><%= TextUtils.htmlEncode(service.getName()) %></span> (<%= TextUtils.htmlEncode(service.getServiceClass()) %>)<br/>
    &nbsp;&nbsp;&nbsp;-&nbsp;<span style="font-weight:bold;"><ww:text
        name="'admin.systeminfo.service.delay'"/>:</span> <%= extendedSystemInfoUtils.getMillisecondsToMinutes(service.getDelay()) %>
    <ww:text name="'core.dateutils.minutes'"/><br/>
    <%
            if (isGlobalAdmin)
            {
            final Map servicePropertyMap = extendedSystemInfoUtils.getServicePropertyMap(service);
            if (servicePropertyMap != null && !servicePropertyMap.isEmpty())
            {
                for (Iterator j = servicePropertyMap.entrySet().iterator(); j.hasNext();)
                {
                    Map.Entry entry = (Map.Entry) j.next();
        %>&nbsp;&nbsp;&nbsp;-&nbsp;<span
            style="font-weight:bold;"><%= TextUtils.htmlEncode(String.valueOf(entry.getKey())) %>:</span> <%= TextUtils.htmlEncode(i18nBean.getText((String) entry.getValue())) %><br/>
        <%
                    }
                }
            }
        }
    %>
</p>
<%
}
catch (Exception e) {
    System.out.println("Error getting services");
    e.printStackTrace();
}
%>
<%-- SERVICES - end --%>

<%-- PLUGINS - start --%>
<% try { %>
<p>
    <span style="font-weight:bold;"><ww:text name="'admin.systeminfo.plugins'"/></span><br/>
    <%
        final Collection plugins = extendedSystemInfoUtils.getPlugins();
        for (Iterator i = plugins.iterator(); i.hasNext();)
        {
            final Plugin plugin = (Plugin) i.next();
            final PluginInformation pluginInformation = plugin.getPluginInformation();
    %>
    &nbsp;-&nbsp;<span style="font-weight:bold;"><%= TextUtils.htmlEncode(plugin.getName()) %></span> <%= TextUtils.htmlEncode(pluginInformation.getVersion()) %> -
    <ww:text name="'admin.systeminfo.plugin.by'"/>&nbsp;<%= TextUtils.htmlEncode(pluginInformation.getVendorName()) %><br/>
    <%
        if (extendedSystemInfoUtils.isPluginEnabled(plugin))
        {
    %>
    &nbsp;&nbsp;&nbsp;-&nbsp;<ww:text name="'admin.systeminfo.plugin.enabled'"/><br/>
    <%
    }
    else
    {
    %>
    &nbsp;&nbsp;&nbsp;-&nbsp;<ww:text name="'admin.systeminfo.plugin.disabled'"/><br/>
    <%
        }
        final Map parameters = pluginInformation.getParameters();
        if (parameters != null && !parameters.isEmpty())
        {
            for (Iterator j = parameters.entrySet().iterator(); j.hasNext();)
            {
                Map.Entry entry = (Map.Entry) j.next();
    %>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;<span
        style="font-weight:bold;"><%= TextUtils.htmlEncode(String.valueOf(entry.getKey())) %>:</span> <%= TextUtils.htmlEncode(i18nBean.getText((String) entry.getValue())) %><br/>
    <%
                }
            }
        }
    %>
</p>
<%
}
catch (Exception e) {
    System.out.println("Error getting plugins");
    e.printStackTrace();
}
%>
<%-- PLUGINS - end --%>

    </div>
</div>

</body>
</html>

