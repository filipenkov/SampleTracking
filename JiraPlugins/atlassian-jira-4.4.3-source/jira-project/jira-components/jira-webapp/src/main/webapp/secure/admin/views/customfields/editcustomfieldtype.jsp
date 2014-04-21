
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.issuefields.customfields.edit.custom.field.type.details'"/></title>
</head>

<body>

	<page:applyDecorator name="jiraform">
		<page:param name="title"><ww:text name="'admin.issuefields.customfields.edit.custom.field.type'"/></page:param>
		<page:param name="action">EditCustomFieldType.jspa</page:param>
        <page:param name="submitId">update_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
		<page:param name="width">100%</page:param>
		<page:param name="cancelURI"><ww:property value="cancelURI"/></page:param>

	    <%@ include file="/includes/admin/customfields/customfieldtypefields.jsp" %>
        <ui:component name="'id'" template="hidden.jsp" />
        <%-- record what page to redirect after success --%>
        <ui:component name="'redirectURI'" template="hidden.jsp" />
	</page:applyDecorator>

</body>
</html>
