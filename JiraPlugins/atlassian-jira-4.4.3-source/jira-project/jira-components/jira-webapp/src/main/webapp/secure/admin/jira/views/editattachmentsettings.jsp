<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.attachmentsettings.edit.attachment.settings'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="attachments"/>
</head>
<body>

<page:applyDecorator name="jiraform">
	<page:param name="action">EditAttachmentSettings.jspa</page:param>
	<page:param name="submitId">edit_attachment</page:param>
	<page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
	<page:param name="cancelURI">ViewAttachmentSettings.jspa</page:param>
	<page:param name="title"><ww:text name="'admin.attachmentsettings.edit.attachment.settings'"/></page:param>
	<page:param name="width">100%</page:param>

    <ww:if test="/systemAdministrator == true">
        <script language="javascript" type="text/javascript">
            window.onload = function(){
                jQuery("#attachmentPathOption_CUSTOM").toggleField("#attachmentPath")
            }
        </script>

        <ui:component template="paths/radio-attachment-config.jsp" label="text('admin.attachmentsettings.attachment.path')" name="'attachmentPath'"/>
    </ww:if>
    <tr>
        <ui:textfield label="text('admin.attachmentsettings.attachment.size')" name="'attachmentSize'">
            <ui:param name="'size'">40</ui:param>
            <ui:param name="'description'">
                <ww:text name="'admin.attachmentsettings.attachment.size.description'">
                    <ww:param name="'value0'"><br/></ww:param>
                </ww:text>
            </ui:param>
        </ui:textfield>
    </tr>
    <tr>
		<td class="fieldLabelArea">
            <ww:text name="'admin.attachmentsettings.enable.thumbnails'"/>
        </td>
		<td class="fieldValueArea">
			<input class="radio" type="radio" value="true" name="thumbnailsEnabled" <ww:if test="thumbnailsEnabled == true">checked="checked"</ww:if>> <ww:text name="'admin.common.words.on'"/>
			&nbsp;
			<input class="radio" type="radio" value="false" name="thumbnailsEnabled" <ww:if test="thumbnailsEnabled == false">checked="checked"</ww:if>> <ww:text name="'admin.common.words.off'"/>
			<div class="description">
				<ww:text name="'admin.attachmentsettings.enable.thumbnails.description'"/>
			</div>
		</td>
	</tr>
    <tr>
		<td class="fieldLabelArea">
			<ww:text name="'admin.attachmentsettings.enable.zipsupport'"/>
        </td>
		<td class="fieldValueArea">
			<input class="radio" type="radio" value="true" name="zipSupport" <ww:if test="zipSupport == true">checked="checked"</ww:if>> <ww:text name="'admin.common.words.on'"/>
			&nbsp;
			<input class="radio" type="radio" value="false" name="zipSupport" <ww:if test="zipSupport == false">checked="checked"</ww:if>> <ww:text name="'admin.common.words.off'"/>
			<div class="description">
				<ww:text name="'admin.attachmentsettings.enable.zipsupport.description'"/>
			</div>
		</td>
	</tr>
</page:applyDecorator>
</body>
</html>
