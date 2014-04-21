<%@ taglib uri="webwork" prefix="ww" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<html>
<head>
    <title><ww:property value="/project/name" />: <ww:property value="/component/name" /></title>
    <meta name="decorator" content="general" />
    <content tag="section">browse_link</content>
    <%
        // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "browse.component"
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResourcesForContext("jira.browse");
        webResourceManager.requireResourcesForContext("jira.browse.component");
    %>
</head>
<body class="type-aa">
<script type="text/javascript">window.dhtmlHistory.create();</script>

<div class="item-header" id="content-top">

<ww:if test="/hasCreateIssuePermissionForProject == true">
    <div id="create-issue">
        <h2><ww:text name="'common.words.create'" />:</h2>
        <ul class="operations">
        <ww:iterator value="/popularIssueTypes">
            <li>
                <a class="lnk" title="<ww:property value="./descTranslation"/>" href="<%= request.getContextPath() %>/secure/CreateIssue.jspa?pid=<ww:property value="/project/id" />&issuetype=<ww:property value="./id" />"><img src="<%= request.getContextPath() %><ww:property value="./iconUrl"/>" alt=""/><span><ww:property value="./nameTranslation"/></span></a>
            </li>
        </ww:iterator>
        <ww:if test="/otherIssueTypes/empty == false">
            <li class="aui-dd-parent">
                <a id="more" class="lnk aui-dd-link standard no-icon" href="#" title="<ww:text name="'browseproject.create.other.issue.type'" />"><span><ww:text name="'common.words.other.no.dots'" /></span></a>
                <div class="aui-list hidden">
                    <ul id="more-dropdown" class="aui-section last">
                        <ww:iterator value="/otherIssueTypes">
                            <li class="aui-list-item">
                                <a class="aui-list-item-link aui-iconised-link" style="background-image:url(<%= request.getContextPath() %><ww:property value="./iconUrl"/>)" title="<ww:property value="./descTranslation"/>" href="<%= request.getContextPath() %>/secure/CreateIssue.jspa?pid=<ww:property value="/project/id" />&issuetype=<ww:property value="./id" />"><span><ww:property value="./nameTranslation"/></span></a>
                            </li>
                        </ww:iterator>
                    </ul>
                </div>
            </li>
        </ww:if>
        </ul>
    </div>
</ww:if>
    
    <h1 class="item-name avatar">
        <a href="<%= request.getContextPath() %>/browse/<ww:property value="/project/key"/>#selectedTab=com.atlassian.jira.plugin.system.project:summary-panel" title="<ww:property value="text('browsecomponent.back.to.desc', /browseProjectTabLabel, /project/name)" />">
            <ww:if test="/project/avatar != null">
                <img id="project-avatar" alt="" class="project-avatar-48" height="48" src="<%= request.getContextPath() %>/secure/projectavatar?pid=<ww:property value="/project/id"/>&avatarId=<ww:property value="/project/avatar/id"/>&size=large" width="48" />
            </ww:if>
            <span><ww:property value="text('browsecomponent.back.to.desc', /browseProjectTabLabel, /project/name)" /></span>
        </a>
    </h1>
    <ul class="breadcrumbs">
        <li>
            <a href="<%= request.getContextPath() %>/browse/<ww:property value="/project/key"/>#selectedTab=com.atlassian.jira.plugin.system.project:summary-panel" title="<ww:property value="text('browsecomponent.back.to.desc', /browseProjectTabLabel, /project/name)" />"><ww:property value="/project/name" />:</a>
        </li>
    </ul>
    <h2 class="item-summary"><ww:property value="/component/name" /></h2>
</div>

<div id="main-content">
    <ul class="vertical tabs">
        <ww:iterator value="/componentTabPanels" status="'status'">
            <li class="<ww:if test="/selected == completeKey">active</ww:if> <ww:if test="@status/first == true"> first</ww:if>">
                <a class="browse-tab" id="<ww:if test="./completeKey/startsWith('com.atlassian.jira.plugin.system.')"><ww:property value="./key"/></ww:if><ww:else><ww:property value="./completeKey"/></ww:else>-panel" href="<ww:url value="'/browse/' + /project/key + '/component/' + /component/id"><ww:param name="'selectedTab'" value="completeKey"/></ww:url>"><strong><ww:property value="label" /></strong></a>
            </li>
        </ww:iterator>
    </ul>
    <div class="active-area" id="project-tab">
        <ww:property value="/tabHtml" escape="false" />
    </div>
</div>

</body>
</html>
