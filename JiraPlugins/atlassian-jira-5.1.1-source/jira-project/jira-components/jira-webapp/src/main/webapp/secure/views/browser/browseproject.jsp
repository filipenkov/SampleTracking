<%@ taglib uri="webwork" prefix="ww" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<html>
<head>
    <title><ww:property value="/project/name" /></title>
    <content tag="section">browse_link</content>
    <%
        // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "browse.project"
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResourcesForContext("jira.browse");
        webResourceManager.requireResourcesForContext("jira.browse.project");
    %>
    <script type="text/javascript">window.dhtmlHistory.create();</script>
</head>
<body>
    <header>
        <jsp:include page="browseheader-createissue-links.jsp"/>
        <ww:if test="/project/avatar != null">
            <div id="heading-avatar">
                <img id="project-avatar" alt="<ww:property value="/project/name"/>" class="project-avatar-48" height="48" src="<ww:url value="'/secure/projectavatar'" atltoken="false"><ww:param name="'pid'" value="/project/id" /><ww:param name="'avatarId'" value="/project/avatar/id" /><ww:param name="'size'" value="'large'" /></ww:url>" width="48" />
            </div>
        </ww:if>
        <h1><ww:property value="/project/name" /></h1>
    </header>
    <div class="content-container">
        <div class="content-related">
            <ul class="vertical tabs">
                <ww:iterator value="/projectTabPanels" status="'status'">
                    <li class="<ww:if test="/selected == completeKey">active</ww:if> <ww:if test="@status/first == true"> first</ww:if>">
                        <a class="browse-tab" id="<ww:if test="./completeKey/startsWith('com.atlassian.jira.plugin.system.')"><ww:property value="./key"/></ww:if><ww:else><ww:property value="./completeKey"/></ww:else>-panel" href="<ww:url value="'/browse/' + /project/key" atltoken="false"><ww:param name="'selectedTab'" value="completeKey"/></ww:url>" hidefocus><strong><ww:property value="label" /></strong></a>
                    </li>
                </ww:iterator>
            </ul>
        </div>
        <div id="project-tab" class="content-body aui-panel" data-project-key="<ww:property value="/project/key" />">
            <ww:property value="/tabHtml" escape="false" />
        </div>
    </div>
</body>
</html>
