<%@ taglib uri="webwork" prefix="webwork" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<html>
<head>
	<title><webwork:text name="'admin.issuefields.customfields.edit.option'">
	    <webwork:param name="'value0'"> <webwork:property value="/customField/name" /></webwork:param>
	</webwork:text></title>
</head>
<body>



<page:applyDecorator name="jiraform">
	<page:param name="title"><webwork:text name="'admin.issuefields.customfields.edit.option'">
	    <webwork:param name="'value0'"> <webwork:property value="/customField/name" /></webwork:param>
	</webwork:text>
    </page:param>
	<page:param name="autoSelectFirst">false</page:param>
	<page:param name="width">100%</page:param>
	<page:param name="description">
        <p>
        <ui:textfield label="text('admin.issuefields.customfields.edit.option.value')" name="'value'" value="value" mandatory="true"/>
        </p>

	</page:param>
	<page:param name="action">EditCustomFieldOptions!update.jspa</page:param>
	<page:param name="submitName"><webwork:text name="'common.words.update'"/></page:param>
	<page:param name="cancelURI"><webwork:property value="/urlWithParent('default')" /></page:param>

	<ui:component name="'fieldConfigId'" template="hidden.jsp" theme="'single'"  />
	<ui:component name="'selectedParentOptionId'" template="hidden.jsp" theme="'single'"  />
	<ui:component name="'selectedValue'" template="hidden.jsp" theme="'single'"  />

</page:applyDecorator>

</body>
</html>
