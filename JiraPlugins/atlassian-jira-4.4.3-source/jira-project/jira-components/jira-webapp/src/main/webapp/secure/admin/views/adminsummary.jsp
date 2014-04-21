<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <meta content="admin" name="decorator" />
    <title><ww:text name="'menu.admin.header.title'"/></title>
    <meta name="admin.active.section" content="system.admin.top.navigation.bar"/>
    <meta name="admin.active.tab" content="admin_summary"/>
</head>
<body class="type-a">


<%-- 
     This form layout with 2 panels at the top spanning the full width of the page and 
     has 2 columns for the remainder of the content.
--%>      

<div id="admin-summary-panel-summary" class="admin-summary-panel">
    <div class="admin-summary-webpanel-fullwidth">
        <ww:iterator value="/topPanels">
        <div class="admin-summary-webpanel-column-content">
            <div id="admin-summary-webpanel-<ww:property value='panelKey'/>" class="module toggle-wrap admin-summary-webpanel">
                    <%-- Start webpanel content --%>
                    <ww:property value='contentHtml' escape="false"/>
                    <%-- End webpanel content  --%>
            </div>
        </div>
        </ww:iterator>
    </div>
    <div class="admin-summary-webpanel-column-wrap">
        <div class="admin-summary-webpanel-column">
            <ww:iterator value="/leftPanels">
            <div class="admin-summary-webpanel-column-content">
                <div id="admin-summary-webpanel-<ww:property value='panelKey'/>" class="module toggle-wrap admin-summary-webpanel">
                    <%-- Start webpanel content --%>
                    <ww:property value='contentHtml' escape="false"/>
                    <%-- End webpanel content  --%>
                </div>
            </div>
            </ww:iterator>
        </div>
        <div class="admin-summary-webpanel-column">
            <ww:iterator value="/rightPanels">
            <div class="admin-summary-webpanel-column-content">
                <div id="admin-summary-webpanel-<ww:property value='panelKey'/>" class="module toggle-wrap admin-summary-webpanel">
                    <%-- Start webpanel content --%>
                    <ww:property value='contentHtml' escape="false"/>
                    <%-- End webpanel content  --%>
                </div>
            </div>
            </ww:iterator>
        </div>
    </div>
</div>



</body>
</html>
