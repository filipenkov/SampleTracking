<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ page import="com.atlassian.jira.issue.search.util.DefaultQueryCreator" %>
<%--  If we have used a 'smart search' then inform the user of this fact, and allow them to query again without smart search --%>
<% if ("true".equalsIgnoreCase(request.getParameter("usedQuickSearch"))) { %>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">warning</aui:param>
    <aui:param name="'helpKey'">quicksearch</aui:param>
    <aui:param name="'messageHtml'">
        <p><ww:text name="'navigator.quicksearch.activated'"/> <a href="<%= DefaultQueryCreator.QUERY_PREFIX%>&query=<ww:property value="@origQuery" />"><ww:text name="'navigator.quicksearch.runwithout'"/></a>.</p>
    </aui:param>
</aui:component>
<% } %>
