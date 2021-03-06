<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>

<html>
<head>
	<title><ww:text name="'signup.title'"/></title>
</head>

<body class="page-type-message">
    <div class="content-container">
        <div class="content-body aui-panel">
            <header>
                <h1><ww:text name="'signup.heading'"/></h1>
            </header>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'signup.limit.exceeded'"/></p>
                </aui:param>
            </aui:component>
            <p><ww:text name="'signup.contact.admin'">
                <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
            </ww:text></p>
            <p><ww:text name="'signup.return.to.dashboard'">
                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/Dashboard.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text></p>
        </div>
    </div>
</body>
</html>
