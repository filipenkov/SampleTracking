<%@ taglib uri="webwork" prefix="ww" %>
<html>
<head>
	<title><ww:text name="'common.words.login.caps'"/></title>
    <meta name="decorator" content="login" />
</head>
<body>
    <header>
        <h1><ww:text name="'login.welcome.to'"/> <%= TextUtils.htmlEncode(ComponentManager.getComponent(ApplicationProperties.class).getDefaultBackedString(APKeys.JIRA_TITLE))%></h1>
    </header>
    <%@ include file="/includes/loginform.jsp" %>
</body>
</html>