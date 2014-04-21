<%@ page import="com.atlassian.jira.web.util.ExternalLinkUtilImpl" %>
<%@ page import="com.atlassian.jira.web.util.ExternalLinkUtil" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.supportrequest.error.title'"/></title>
</head>

<body>
<%
    ExternalLinkUtil externalLinkUtil = new ExternalLinkUtilImpl();
%>
<table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.supportrequest.error.title'"/></page:param>
        <ww:if test="/invalidAddresses">
            <p>
                <ww:text name="'admin.supportrequest.bad.addresses'">
                    <ww:param name="'value0'"><ww:property value="/invalidAddresses"/></ww:param>
                </ww:text>
            </p>
        </ww:if>
        <ww:else>
            <p>
                <ww:text name="'admin.supportrequest.error.desc'" />
            </p>
        </ww:else>
        <p><ww:text name="'admin.supportrequest.bad.addresses2'" /></p>
        <p> <ww:text name="'admin.supportrequest.error.manual.create'" >
            <ww:param name="'value0'"><a href="<%= externalLinkUtil.getProperty("external.link.jira.support.site") %>"><%= externalLinkUtil.getProperty("external.link.jira.support.site") %></a></ww:param>
            </ww:text>
        </p>

        <p>- <ww:text name="'admin.supportrequest.atlassian.team'"/></p>
    </page:applyDecorator>
</table>

</body>
</html>
