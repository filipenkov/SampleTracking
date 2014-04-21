<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.jira.config.properties.APKeys"%>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties" %>
<%@ page import="com.atlassian.jira.issue.Issue" %>
<%@ page import="com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory" %>
<%@ page import="webwork.action.CoreActionContext" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%
    final ApplicationProperties applicationProperties = ComponentManager.getInstance().getApplicationProperties();
    if (applicationProperties.getOption(APKeys.JIRA_OPTION_TRACKBACK_RECEIVE))
    {
        final String baseUrl = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext().getCanonicalBaseUrl();
        Issue issue = (Issue) CoreActionContext.getValueStack().findValue("issueObjectNoSecurityCheck");
        String issueUrl = baseUrl + "/browse/" + issue.getKey();
        String issueUrlViaId = baseUrl + "/secure/ViewIssue.jspa?id=" + issue.getId();
        String issueUrlViaKey = baseUrl + "/secure/ViewIssue.jspa?key=" + issue.getKey();
%>
<ww:bean name="'com.atlassian.core.util.StringUtils'" id="stringUtils" />
<!--
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:dc="http://purl.org/dc/elements/1.1/"
         xmlns:trackback="http://madskills.com/public/xml/rss/module/trackback/">
<rdf:Description rdf:about="<%= issueUrl %>"
    dc:identifier="<%= issueUrl %>"
    trackback:ping="<%= baseUrl %>/rpc/trackback/<ww:property value="./key"/>"/>

<rdf:Description rdf:about="<%= issueUrlViaId %>"
    dc:identifier="<%= issueUrlViaId %>"
    trackback:ping="<%= baseUrl %>/rpc/trackback/<ww:property value="./key"/>"/>

<rdf:Description rdf:about="<%= issueUrlViaKey %>"
    dc:identifier="<%= issueUrlViaKey %>"
    trackback:ping="<%= baseUrl %>/rpc/trackback/<ww:property value="./key"/>"/>
</rdf:RDF>-->
<% } %>
