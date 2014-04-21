
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.import.restore.jira.data'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/import_export_section"/>
    <meta name="admin.active.tab" content="restore_data"/>
</head>

<body>

<page:applyDecorator name="jiraform">
	<page:param name="action">XmlRestore.jspa</page:param>
	<page:param name="title"><ww:text name="'admin.import.restore.jira.data.from.backup'"/></page:param>
	<page:param name="width">100%</page:param>
	<page:param name="instructions">
		<p><ww:text name="'admin.import.instruction'"/></p>

		<aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.import.note1'"/></p>
                <p><ww:text name="'admin.import.note2'"/></p>
            </aui:param>
        </aui:component>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'admin.import.warning'">
                        <ww:param name="'value0'"><a href="XmlBackup!default.jspa"><b></ww:param>
                        <ww:param name="'value1'"></b></a></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>

        <ww:if test="/hasSpecificErrors == true && /specificErrors/errorMessages/empty == false">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'messageHtml'">
                    <ww:iterator value="specificErrors/errorMessages">
                        <p><ww:property escape="false"/></p>
                    </ww:iterator>
                </aui:param>
            </aui:component>
        </ww:if>
        <%--This is here for our functests so we can do a quick import where we just clear the cache--%>
        <input id="quickImport" type="checkbox" name="quickImport" value="true" style="display:none;" />

	</page:param>
	<page:param name="submitId">restore_submit</page:param>
	<page:param name="submitName"><ww:text name="'admin.import.restore'"/></page:param>
	<page:param name="cancelURI">default.jsp</page:param>
    <page:param name="helpURL">restore_data</page:param>

    <ui:textfield label="text('admin.export.file.name')" name="'filename'">
		<ui:param name="'size'">100</ui:param>
		<ui:param name="'description'"><ww:text name="'admin.import.file.name.description'"/> <span id="default-import-path"><ww:property value="/defaultImportPath"/></span></ui:param>
	</ui:textfield>

    <ui:component template="paths/radio-indexing-restore.jsp" label="text('setup.indexpath.label')"/>

    <ui:textarea label="text('admin.import.license.if.required')" name="'license'" cols="50" rows="5">
        <ui:param name="'description'">
			<ww:text name="'admin.import.enter.a.license'"/>
		</ui:param>
    </ui:textarea>

    <ui:component name="'saxParser'" template="hidden.jsp" theme="'single'"  />
</page:applyDecorator>
<script type="text/javascript">
   jQuery(function() {
        jQuery("#reimport").click(function(e) {
            e.preventDefault();
            //set the form to import with default paths
            jQuery("#restore_submit,#cancelButton").attr("disabled", "true");
            jQuery(this).closest("form").append("<input type='hidden' name='useDefaultPaths' value='true' />").submit();
        });
    });
</script>
</body>
</html>
