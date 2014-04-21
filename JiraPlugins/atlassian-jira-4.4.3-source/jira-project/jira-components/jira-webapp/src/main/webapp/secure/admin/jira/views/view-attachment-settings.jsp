<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.globalsettings.attachment.settings'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="attachments"/>
</head>

<body>
<%-- error messages --%>
<ww:if test="hasErrorMessages == 'true'">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'titleText'"><ww:text name="'admin.common.words.errors'"/></aui:param>
        <aui:param name="'messageHtml'">
            <ul>
                <ww:iterator value="errorMessages">
                    <li><ww:property /></li>
                </ww:iterator>
            </ul>
        </aui:param>
    </aui:component>
</ww:if>
    <page:applyDecorator name="jiratable">
        <page:param name="id">AttachmentSettings</page:param>
        <page:param name="title"><ww:text name="'admin.globalsettings.attachment.settings'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="titleColspan">2</page:param>
        <page:param name="helpURL">attachments</page:param>
        <page:param name="description">

            <ww:text name="'admin.attachmentsettings.instruction1'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"><ww:text name="'admin.permissions.CREATE_ATTACHMENT'"/></ww:param>
                <ww:param name="'value2'"></b></ww:param>
            </ww:text>
            <ul>
                <li><ww:text name="'admin.attachmentsettings.attachment.path.explanation'">
                  <ww:param name="'value0'"><b></ww:param>
                  <ww:param name="'value1'"></b></ww:param>
                  <ww:param name="'value2'"><br/></ww:param>
                </ww:text>
                </li>
                <li><ww:text name="'admin.attachmentsettings.attachment.size.explanation'">
                  <ww:param name="'value0'"><b></ww:param>
                  <ww:param name="'value1'"></b></ww:param>
                </ww:text>
                </li>
                <li><ww:text name="'admin.attachmentsettings.enable.thumbnails.explanation'">
                  <ww:param name="'value0'"><b></ww:param>
                  <ww:param name="'value1'"></b></ww:param>
                </ww:text>
                </li>
                <li><ww:text name="'admin.attachmentsettings.enable.zip.support.explanation'">
                  <ww:param name="'value0'"><strong></ww:param>
                  <ww:param name="'value1'"></strong></ww:param>
                </ww:text>
                </li>
            </ul>

            <p><ww:text name="'admin.common.phrases.more.information'"/></p>

            <ww:if test="/systemAdministrator == false && applicationProperties/option('jira.option.allowattachments') == false">
                <p>
                <ww:text name="'admin.attachmentsettings.disabled.contact.sys.admin'">
                  <%--Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved--%>
                  <%--<ww:param name="'value0'"><a href="<%=request.getContextPath()%>/secure/Administrators.jspa#sysadmins"></ww:param>--%>
                  <%--<ww:param name="'value1'"></a></ww:param>--%>
                  <ww:param name="'value0'"> </ww:param>
                  <ww:param name="'value1'"> </ww:param>
                </ww:text>
                </p>
            </ww:if>
        </page:param>

        <tr data-attachment-setting="allow-attachment">
            <td width="40%" data-cell-type="label">
                <b><ww:text name="'admin.attachmentsettings.allow.attachments'"/></b>
            </td>
            <td width="60%" data-cell-type="value">
                <ww:if test="applicationProperties/option('jira.option.allowattachments') == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
			    </ww:if>
			    <ww:else>
				    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
        <tr data-attachment-setting="attachment-path">
            <td data-cell-type="label">
                <b><ww:text name="'admin.attachmentsettings.attachment.path'"/></b>
            </td>
            <td data-cell-type="value">
                <ww:property value="attachmentPath" />
            </td>
        </tr>
        <tr data-attachment-setting="attachment-size">
            <td data-cell-type="label">
                <b><ww:text name="'admin.attachmentsettings.attachment.size'"/></b>
            </td>
            <td data-cell-type="value">
                <ww:property value="prettyAttachmentSize"/>
            </td>
        </tr>
        <tr data-attachment-setting="allow-thumbnails">
            <td data-cell-type="label">
                <b><ww:text name="'admin.attachmentsettings.enable.thumbnails'"/></b>
            </td>
            <td data-cell-type="value">
                <ww:if test="applicationProperties/option('jira.option.allowthumbnails') == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
			    </ww:if>
			    <ww:else>
				    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
        <tr data-attachment-setting="zipsupport">
            <td data-cell-type="label">
                <b><ww:text name="'admin.attachmentsettings.enable.zipsupport'"/></b>
            </td>
            <td data-cell-type="value">
                <ww:if test="/zipSupport == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
			    </ww:if>
			    <ww:else>
				    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
    </page:applyDecorator>
    <ww:if test="/systemAdministrator == true || applicationProperties/option('jira.option.allowattachments') == true">
        <div class="buttons-container aui-toolbar form-buttons noprint">
            <div class="toolbar-group">
                <span class="toolbar-item">
                    <a id="edit-attachments" class="toolbar-trigger" href="EditAttachmentSettings!default.jspa"><ww:text name="'admin.common.phrases.edit.configuration'"/></a>
                </span>
            </div>
        </div>
    </ww:if>

</body>
</html>
