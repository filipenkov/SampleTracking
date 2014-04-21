<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.customfields.config.for.customfield'">
	    <ww:param name="'value0'"><ww:property value="title" /></ww:param>
	    <ww:param name="'value1'"><ww:property value="customField/name" /></ww:param>
	</ww:text></title>
</head>

<body>
	<page:applyDecorator name="jiraform">
		<page:param name="title"><ww:text name="'admin.issuefields.customfields.config.for.customfield'">
	    <ww:param name="'value0'"><ww:property value="title" /></ww:param>
	    <ww:param name="'value1'"><ww:property value="customField/name" /></ww:param>
	</ww:text></page:param>
		<page:param name="instructions"><ww:property value="instructions" /></page:param>
		<page:param name="action">EditBasicConfig.jspa</page:param>
		<page:param name="submitId">words_submit</page:param>
		<page:param name="submitName"><ww:text name="'admin.common.words.ok'"/></page:param>
        <page:param name="cancelURI">ViewCustomFields.jspa</page:param>
        <page:param name="helpURL">customfields</page:param>

        <ui:component name="'className'" template="hidden.jsp" theme="'single'" />
        <ui:component name="'fieldConfigId'" template="hidden.jsp" theme="'single'"  />

    <ww:iterator value="/configFields" status="'status'">
        <ui:textfield label="./name" name="./key" value="/fieldValue(./key)" >
            <ui:param name="'description'"><ww:property value="./description" /> <ww:property value="/fieldValue(./key)" /></ui:param>
        </ui:textfield>
    </ww:iterator>
    </page:applyDecorator>
</body>
</html>
