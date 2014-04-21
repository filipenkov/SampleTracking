<%@ page import="java.util.*"%>
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
                <aui:param name="'messageType'">success</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'signup.success'"/></p>
                </aui:param>
            </aui:component>
            <p><a href="<%= request.getContextPath() %>/login.jsp"><ww:text name="'login.click'"/></a>.</p>
            <p>
            <ww:text name="'signup.stay.in.touch'">
                <ww:param name="'value0'"><a href='<ww:property value="/externalLinkUtils/property('external.link.atlassian.news')"/>'></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
            </p>
        </div>
    </div>
</body>
</html>
