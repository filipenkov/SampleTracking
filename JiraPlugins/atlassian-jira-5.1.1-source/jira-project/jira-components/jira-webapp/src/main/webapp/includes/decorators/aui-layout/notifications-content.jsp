<%@ page import="com.atlassian.jira.admin.AnnouncementBanner" %>
<%
    AnnouncementBanner banner = ComponentAccessor.getComponentOfType(AnnouncementBanner.class);
    if (banner.isDisplay())
    {
%>
<div id="announcement-banner" class="alertHeader">
    <%= banner.getViewHtml() %>
</div>
<%
    }
%>
