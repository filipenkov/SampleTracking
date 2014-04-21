<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title>Func Master 4000</title>
    <%
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:global-static");
        webResourceManager.requireResource("jira.webresources:jira-global");
        webResourceManager.includeResources(out, UrlMode.AUTO);
    %>
    <script language="JavaScript" type="text/javascript" src="<%=request.getContextPath()%>/func/func.js"></script>
    <script language="JavaScript" type="text/javascript" src="<%=request.getContextPath()%>/func/obj.js"></script>
    <link type="text/css" rel="StyleSheet" href="<%= request.getContextPath() %>/func/func.css"/>
    <%-- Do not decorate the func master as having a decorator fails to update the command list --%>
    <meta name="decorator" content="none">
    <script language="JavaScript" type="text/javascript">
        <!--
        frameOnload = function()
        {
            loadUrl();

        <ww:iterator value="./allEvents" status="'status'">
        <ww:if test="./alternativeHandlerMethod">
            addEventsToListen('<ww:property value="./tagName" />', '<ww:property value="./eventType" />', <ww:property value="alternativeHandlerMethod" />);
        </ww:if>
        <ww:else>
            addEventsToListen('<ww:property value="./tagName" />', '<ww:property value="./eventType" />');
        </ww:else>
        </ww:iterator>

            addEventsToListen('textarea', 'blur', handleInput);
            addEventsToListen('input', 'click', handleSubmit);

            funcmaster_onload();
        }
            //-->
    </script>
</head>
<body onload="frameOnload()">

<form id="urlForm" method="post" action="#" onsubmit="return loadPage();">
    <input type="text" name="currentUrl" id="currentUrl"/>
</form>

<div id="commands">
    <div id="commandsHeader">
        <a class="deleteLink" href="#" onclick="removeAllCommands();return false">remove all</a>
        <a class="deleteLink" href="#" onclick="extractCommands();return false">extract</a>
        <h2>Commands Executed</h2>
    </div>
    <div id="commandsInner">
        <ol id="commandsList">
        </ol>
    </div>
    <textarea id="commandsText" style="display:none;"></textarea>
</div>

<div id="frameHolderHolder">
    <iframe id="frameHolder" name="frameHolder" src="<%=request.getContextPath()%>/secure/Dashboard.jspa">
    </iframe>
</div>

<div class="othercrap">
    <!-- Original Toolbar with text asserts and Navigator All Issues -->
    <div class="tools">
        <a href="#" onclick="assertPresent();return false;">Assert present</a> |
        <a href="#" onclick="assertCollapsedText();return false;">Assert collapsed text</a> |
        <a href="#" onclick="assertBefore();return false;" id="assertBefore">Assert before..</a> |
        <a href="#" onclick="displayAllIssues(); return false;">Navigator All Issues</a> |
        <a href="#" onclick="saveUrl(); return false;">Save current url</a>
    </div>
    <!-- Table operations Toolbar -->
    <div class="tools">
        <strong>Table:</strong>
        <select name="ddTable" id="ddTable" onchange="ddTableValueChanged();" onfocus="refreshTools();">
        </select>
        Action:
        <select name="ddTableAction" id="ddTableAction">
            <option value="AssertAllRowsInOrder">Assert all rows - Compact</option>
            <option value="AssertAllRowsVerbose">Assert all rows - Verbose</option>
            <option value="AssertAllRowsNoOrder">Assert all rows - No Order</option>
            <option value="GetWebTableObject">Get WebTable object</option>
        </select>
        <a href="#" onclick="doTableAction(); return false;">Do It</a>
    </div>
    <!-- Form operations Toolbar -->
    <div class="tools">
        <strong>Form:</strong>
        <select name="ddForm" id="ddForm" onchange="ddFormValueChanged();" onfocus="refreshTools();">
        </select>
        Component:
        <select name="ddFormComponent" id="ddFormComponent" onchange="ddFormComponentValueChanged();">
            <option value="ALL">All</option>
        </select>
        Action:
        <select name="ddFormAction" id="ddFormAction">
            <option value="AssertComponentValue">Assert Component Value</option>
            <option value="AssertComponentPresent">Assert Component Present</option>
        </select>
        <a href="#" onclick="doFormAction(); return false;">Do It</a>
    </div>

    <!--If you want to FV looger back then uncomment this include-->
    <%--<%@include file="/debug/jsdebug.jsp"%>--%>


    <iframe id="hiddenIFrame" name="hiddenIFrame" class="hidden">
    </iframe>
</div>
</body>
</html>
