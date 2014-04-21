<%@ taglib uri="webwork" prefix="ww" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<html>
<head>
    <title><ww:property value="/project/name" /></title>
    <meta name="decorator" content="general" />
    <content tag="section">browse_link</content>
    <%
        // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "browse.project"
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResourcesForContext("jira.browse");
        webResourceManager.requireResourcesForContext("jira.browse.project");
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
                    <a class="lnk" title="<ww:property value="./descTranslation"/>" href="<%= request.getContextPath() %>/secure/CreateIssue.jspa?pid=<ww:property value="/project/id" />&issuetype=<ww:property value="./id" />"><img src="<%= request.getContextPath() %><ww:property value="./iconUrl"/>" alt=""/><ww:property value="./nameTranslation"/></a>
                </li>
            </ww:iterator>
            <ww:if test="/otherIssueTypes/empty == false">
                <li class="aui-dd-parent">
                    <a id="more" class="lnk aui-dd-link standard no-icon" href="#" hidefocus title="<ww:text name="'browseproject.create.other.issue.type'" />"><span><ww:text name="'common.words.other.no.dots'" /></span></a>
                    <div class="aui-list hidden">
                        <ul id="more-dropdown">
                            <ww:iterator value="/otherIssueTypes">
                                <li class="aui-list-item">
                                    <a class="aui-list-item-link aui-iconised-link" style="background-image:url(<%= request.getContextPath() %><ww:property value="./iconUrl"/>)" title="<ww:property value="./descTranslation"/>" href="<%= request.getContextPath() %>/secure/CreateIssue.jspa?pid=<ww:property value="/project/id" />&issuetype=<ww:property value="./id" />"><ww:property value="./nameTranslation"/></a>
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
    <ww:if test="/project/avatar != null">
        <img id="project-avatar" alt="" class="project-avatar-48" height="48" src="<%= request.getContextPath() %>/secure/projectavatar?pid=<ww:property value="/project/id"/>&avatarId=<ww:property value="/project/avatar/id"/>&size=large" width="48" />
    </ww:if>
        <span><ww:property value="/project/name" /></span>
    </h1>
    <h2 class="item-summary"><ww:property value="/project/name" /></h2>
</div>

<div id="main-content">
    <ul class="vertical tabs">
        <ww:iterator value="/projectTabPanels" status="'status'">
            <li class="<ww:if test="/selected == completeKey">active</ww:if> <ww:if test="@status/first == true"> first</ww:if>">
                <a class="browse-tab" id="<ww:if test="./completeKey/startsWith('com.atlassian.jira.plugin.system.')"><ww:property value="./key"/></ww:if><ww:else><ww:property value="./completeKey"/></ww:else>-panel" href="<ww:url value="'/browse/' + /project/key" atltoken="false"><ww:param name="'selectedTab'" value="completeKey"/></ww:url>" hidefocus><strong><ww:property value="label" /></strong></a>
            </li>
        </ww:iterator>
    </ul>
    <div class="active-area" id="project-tab" data-project-key="<ww:property value="/project/key" />">
        <ww:property value="/tabHtml" escape="false" />
    </div>
</div>

</body>
</html>
