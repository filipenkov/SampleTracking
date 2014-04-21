<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.jira.ComponentManager" %> 
<html>
<head>
    <title><ww:text name="'browseprojects.title'" /></title>
    <ww:if test="/categories/size > 0"><meta name="decorator" content="general" /></ww:if>
    <ww:else><meta name="decorator" content="message" /></ww:else>
    <content tag="section">browse_link</content>
    <%
        // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "browse.projects"
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResourcesForContext("jira.browse");
        webResourceManager.requireResourcesForContext("jira.browse.projects");
    %>
</head>
<body class="type-a">
<ww:if test="/categories/size > 0">
<script type="text/javascript">window.dhtmlHistory.create();</script>
    <div class="item-header" id="content-top">
        <h1 class="item-summary"><ww:text name="'browseprojects.title'"/></h1>
    </div>
    <div id="main-content" class="bp">
    <ww:if test="/showTabs() == true">
        <ul class="vertical tabs">
            <ww:iterator value="tabs" status="'status'">
                <li id="<ww:property value="./id" />-panel-tab" class="<ww:if test="@status/first == true">first </ww:if><ww:if test="/selectedCategory == ./id">active</ww:if>">
                   <a id="<ww:property value="./id" />-panel-tab-lnk" rel="<ww:property value="./id" />" title="<ww:property value="./description" />" href="<%= request.getContextPath() %>/secure/BrowseProjects.jspa?selectedCategory=<ww:property value="./id" />"><strong><ww:property value="./name" /></strong></a>
                </li>
            </ww:iterator>
            <li id="all-panel-tab" class="<ww:if test="/selectedCategory == 'all'">active</ww:if>">
               <a id="all-panel-tab-lnk" rel="all" title="<ww:text name="'browse.projects.all.desc'"/>" href="<%= request.getContextPath() %>/secure/BrowseProjects.jspa?selectedCategory=all"><strong><ww:text name="'browse.projects.all'"/></strong></a>
            </li>
        </ul>
    </ww:if>
        <div class="active-area category-list" id="project-tab">
            <div id="primary" class="column">
                <div class="content">
                <ww:iterator value="categories" status="'status'">
                    <div class="module<ww:if test="./all == true"> inall</ww:if> <ww:if test="/selectedCategory == ./id || (/selectedCategory == 'all' && ./all == true)">active</ww:if><ww:else>hidden</ww:else>" id="<ww:property value="./id" />-panel">
                        <div class="mod-header plain">
                            <h3><ww:property value="./name" /></h3>
                        </div>
                        <div class="mod-content">
                        <ww:property value="./projects">
                            <%@ include file="/includes/project/projectstable.jsp" %>
                        </ww:property>
                        </div>
                    </div>
                </ww:iterator>
                </div>
            </div>
        </div>
    </div>
</ww:if>

<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <%@ include file="/includes/noprojects.jsp" %>
        </aui:param>
    </aui:component>
</ww:else>

</body>
</html>
