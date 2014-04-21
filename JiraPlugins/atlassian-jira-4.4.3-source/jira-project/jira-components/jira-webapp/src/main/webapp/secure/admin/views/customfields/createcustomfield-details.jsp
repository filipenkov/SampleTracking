<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="view_custom_fields"/>
	<title><ww:text name="'admin.createcustomfield.details.title'"/></title>
</head>

<body>
<%-- JRA-4345 - need to statically include the javascript to address IE6 refresh issue --%>

	<page:applyDecorator name="jiraform">
		<page:param name="title"><ww:text name="'admin.createcustomfield.details.title'"/></page:param>
		<page:param name="instructions"><ww:text name="'admin.createcustomfield.details.instructions'"/></page:param>
		<page:param name="action">CreateCustomField.jspa</page:param>
		<page:param name="width">100%</page:param>
    	<page:param name="cancelURI">ViewCustomFields.jspa</page:param>
        <page:param name="helpURL">addingcustomfields</page:param>
        <page:param name="wizard">true</page:param>

        <ui:component template="multihidden.jsp" >
            <ui:param name="'fields'">fieldType</ui:param>
        </ui:component>

        <ui:component label="text('admin.issuefields.customfields.field.type')" value="/customFieldType/name" template="textlabel.jsp" />

        <ui:textfield label="text('admin.issuefields.field.name')" name="'fieldName'" >
            <ui:param name="'description'"><ww:text name="'admin.createcustomfield.details.name.for.custom.field'"/></ui:param>
            <ui:param name="'mandatory'">true</ui:param>
        </ui:textfield>
        <ui:textarea label="text('common.concepts.description')" name="'description'" rows="5" cols="40">
            <ui:param name="'description'">
                <ww:text name="'admin.createcustomfield.details.description.desc'"/>
            </ui:param>
        </ui:textarea>

        <ui:component label="text('admin.createcustomfield.details.choose.search.template')" template="sectionbreak.jsp">
        </ui:component>

        <ww:if test="/searchers != null && /searchers/empty == false">
            <ui:select label="text('admin.createcustomfield.details.search.template')" name="'searcher'" list="/searchers" listKey="'descriptor/completeKey'" listValue="'descriptor/name'"  value="/searcher">
                <ui:param name="'summary'" value="'./description'"/>
                <ui:param name="'description'">
                    <ww:text name="'admin.createcustomfield.details.searcher.desc'"/>
                </ui:param>
                <ui:param name="'headerrow'" value="'None'" />
                <ui:param name="'headervalue'" value="'-1'" />
            </ui:select>
        </ww:if>
        <ww:else>
            <ui:component label="text('admin.createcustomfield.details.search.template')" value="text('admin.createcustomfield.details.searcher.template.msg')" template="textlabel.jsp" />
        </ww:else>

        <jsp:include page="addcontext.jsp" />

	</page:applyDecorator>

</body>
</html>
