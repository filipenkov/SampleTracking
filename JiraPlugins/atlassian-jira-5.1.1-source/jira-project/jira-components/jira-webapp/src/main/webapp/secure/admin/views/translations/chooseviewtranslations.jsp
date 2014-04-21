<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title>
        <ww:text name="'admin.issuesettings.translations'">
            <ww:param name="'value0'"><ww:property value="/issueConstantName" /></ww:param>
        </ww:text>
    </title>
</head>
<body>
    <header>
        <h2>
            <ww:text name="'admin.issuesettings.translations'">
                <ww:param name="'value0'"><ww:property value="/issueConstantName" /></ww:param>
            </ww:text>
        </h2>
    </header>
    <%@include file="viewtranslations.jsp"%>
</body>
</html>

