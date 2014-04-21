<%@ taglib uri="webwork" prefix="ww" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<html>
<head>
    <title><ww:property value="/project/name" />: <ww:property value="/version/name" /></title>
    <meta name="decorator" content="general" />
    <content tag="section">browse_link</content>
    <%
        // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "browse.version"
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResourcesForContext("jira.browse");
        webResourceManager.requireResourcesForContext("jira.browse.version");
    %>
</head>
<body class="type-aa">
<script type="text/javascript">window.dhtmlHistory.create();</script>

<div class="item-header" id="content-top">

<div id="create-issue">
<ww:if test="/hasCreateIssuePermissionForProject == true">
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
                        <ul>
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
</ww:if>
<ww:else>
    <%--ie wont give an empty div any credit.  Sorry Sharpy--%>
    &nbsp;
</ww:else>
</div>

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
    <h2 class="item-summary"><ww:property value="/version/name" /></h2>

    <ul class="version-navigation">
    <ww:property value="/nextAndPreviousVersions">
        <ww:if test="./previous">
            <li class="previous" >
                <a title="<ww:text name="'browseversion.browse.previous'"/>" href="<%= request.getContextPath() %>/browse/<ww:property value="/project/key"/>/fixforversion/<ww:property value="./previous/id"/>"><img src="<%= request.getContextPath() %>/images/icons/arrow_left_faded.gif" alt="<ww:text name="'browseversion.browse.previous'"/>"><span><ww:property value="./previous/name"/></span></a>|
            </li>
        </ww:if>
        <li class="current">
            <span><ww:property value="/version/name"/></span>
        </li>
        <ww:if test="./next">
            <li class="next" >
                |<a title="<ww:text name="'browseversion.browse.next'"/>" href="<%= request.getContextPath() %>/browse/<ww:property value="/project/key"/>/fixforversion/<ww:property value="./next/id"/>"><span><ww:property value="./next/name"/></span><img src="<%= request.getContextPath() %>/images/icons/arrow_right_faded.gif" alt="<ww:text name="'browseversion.browse.next'"/>"></a>
            </li>
        </ww:if>
    </ww:property>
    </ul>
</div>

<div id="main-content">
    <ul class="vertical tabs">
        <ww:iterator value="/versionTabPanels" status="'status'">
            <li class="<ww:if test="/selected == completeKey">active</ww:if> <ww:if test="@status/first == true"> first</ww:if>">
                <a id="<ww:if test="./completeKey/startsWith('com.atlassian.jira.plugin.system.')"><ww:property value="./key"/></ww:if><ww:else><ww:property value="./completeKey"/></ww:else>-panel" class="browse-tab" href="<ww:url value="'/browse/' + /project/key + '/fixforversion/' + /version/id"><ww:param name="'selectedTab'" value="completeKey"/></ww:url>"><strong><ww:property value="label" /></strong></a>
            </li>
        </ww:iterator>
    </ul>
    <div class="active-area" id="project-tab">
        <ww:property value="/tabHtml" escape="false" />
    </div>
</div>

<fieldset class="hidden parameters">
    <input type="hidden" id="project-key" value="<ww:property value="/project/key"/>"/>
    <input type="hidden" id="version-id" value="<ww:property value="/version/id"/>"/>
</fieldset>

</body>
</html>
