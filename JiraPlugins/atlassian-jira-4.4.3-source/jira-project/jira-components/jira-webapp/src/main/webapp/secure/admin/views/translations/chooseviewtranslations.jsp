<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%-- This file is a partial hack until we merge the other constants to deal in the same way issue types are dealt with --%>
<ww:if test="/issueConstantType == 'issuetype'">
    <%@include file="/secure/admin/views/issuetypes/viewissuetypes.jsp"%>
</ww:if>
<ww:else>
<html>
<head>
    <title><ww:text name="'admin.issuesettings.translations'">
        <ww:param name="'value0'"><ww:property value="/issueConstantName" /></ww:param>
    </ww:text></title>
</head>
<body>
    <%@include file="viewtranslations.jsp"%>
</body>
</html>
</ww:else>

