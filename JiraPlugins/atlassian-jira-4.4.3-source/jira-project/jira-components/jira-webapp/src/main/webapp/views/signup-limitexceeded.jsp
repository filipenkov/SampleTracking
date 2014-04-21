<%@ taglib uri="webwork" prefix="ww" %>

<html>
<head>
	<title><ww:text name="'signup.title'"/></title>
</head>

<body>
	<h2><ww:text name="'signup.heading'"/></h2>
    <p><ww:text name="'signup.limit.exceeded'"/></p>
    <p><ww:text name="'signup.contact.admin'">
        <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
    </ww:text></p>
    <p><ww:text name="'signup.return.to.dashboard'">
        <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/Dashboard.jspa"></ww:param>
        <ww:param name="'value1'"></a></ww:param>
    </ww:text></p>
</body>
</html>
