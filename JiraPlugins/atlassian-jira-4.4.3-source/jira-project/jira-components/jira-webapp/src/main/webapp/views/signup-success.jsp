<%@ page import="java.util.*"%>
<%@ taglib uri="webwork" prefix="ww" %>

<html>
<head>
	<title><ww:text name="'signup.title'"/></title>
</head>

<body>
	<h2><ww:text name="'signup.heading'"/></h2>
	<p><ww:text name="'signup.success'"/></p>
	<p><a href="<%= request.getContextPath() %>/secure"><ww:text name="'login.click'"/></a>.</p>
    <p>
    <ww:text name="'signup.stay.in.touch'">
        <ww:param name="'value0'"><a href='<ww:property value="/externalLinkUtils/property('external.link.atlassian.news')"/>'></ww:param>
        <ww:param name="'value1'"></a></ww:param>
    </ww:text>
    </p>
</body>
</html>
