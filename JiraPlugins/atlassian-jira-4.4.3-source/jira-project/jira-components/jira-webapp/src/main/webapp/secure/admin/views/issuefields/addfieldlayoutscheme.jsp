<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.addfieldlayoutscheme.title'"/></title>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">AddFieldLayoutScheme.jspa</page:param>
    <page:param name="submitId">add_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="cancelURI">ViewFieldLayoutSchemes.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.addfieldlayoutscheme.title'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">view_field_layout_schemes</page:param>

    <ui:textfield label="text('common.words.name')" name="'name'" size="'30'">
        <ui:param name="'mandatory'" value="true"/>
    </ui:textfield>
    <ui:textarea label="text('common.words.description')" name="'description'" cols="'30'" rows="'3'" />
</page:applyDecorator>
</body>
</html>
