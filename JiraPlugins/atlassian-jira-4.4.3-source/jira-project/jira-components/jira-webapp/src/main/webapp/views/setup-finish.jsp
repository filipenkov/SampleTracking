<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'setup.title'"/></title>
    <meta content="message" name="decorator" />
</head>

<body>

    <h1><ww:text name="'setup.complete'"/></h1>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">success</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'setup.complete.msg'"/></p>
            <p>
                <ww:text name="'setup.complete.youcanlogin'">
                    <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>

</body>
</html>
