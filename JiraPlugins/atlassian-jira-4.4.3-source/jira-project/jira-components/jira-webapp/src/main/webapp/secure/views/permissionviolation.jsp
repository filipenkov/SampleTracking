<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>

<ww:if test="/contentOnly == true">
    <% response.setContentType("application/json"); %>
    {"permissionsError": true}
</ww:if>
<ww:else>
<html>
<head>
    <meta name="decorator" content="message">
    <title><ww:text name="'perm.violation.title'"/></title>
</head>
<body>
    <h1><ww:text name="'perm.violation.title'"/></h1>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'perm.violation.desc'"/></p>
            <p>
                <ww:text name="'contact.admin.for.perm'">
                    <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>
<ww:if test="issueObjectNoSecurityCheck">
    <ww:property value="issueObjectNoSecurityCheck">
        <%@ include file="/includes/trackback_rdf_min.jsp"%>
    </ww:property>
</ww:if>
</body>
</html>
</ww:else>
