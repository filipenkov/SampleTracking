<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.supportrequest.no.support.error.title'"/></title>
</head>

<body>
<table width="100%" cellpadding="10" cellspacing="0" border="0">
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.supportrequest.no.support.error.title'"/></page:param>
        <ww:property value="/supportRequestMessage" escape="false"/>
    </page:applyDecorator>
</table>
</body>
</html>
