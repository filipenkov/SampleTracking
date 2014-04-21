<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title>
        <ww:text name="'admin.project.import.select.backup.title'"/>
    </title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/import_export_section"/>
    <meta name="admin.active.tab" content="project_import"/>
</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">ProjectImportSelectBackup.jspa</page:param>
    <page:param name="submitId">next_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.next'"/></page:param>
    <page:param name="autoSelectFirst">true</page:param>
    <page:param name="title">
        <ww:text name="'admin.project.import.select.backup.title'"/>
    </page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">restore_project</page:param>
    <page:param name="description">
        <ww:text name="'admin.project.import.select.backup.desc'">
            <ww:param name="'value0'"><p/></ww:param>
            <ww:param name="'value1'"><a href="<ww:property value="/docsLink"/>"></ww:param>
            <ww:param name="'value2'"></a></ww:param>
            <ww:param name="'value3'"><ww:property value="/version"/></ww:param>
            <ww:param name="'value4'"><span class="note"></ww:param>
            <ww:param name="'value5'"></span></ww:param>
            <ww:param name="'value6'"><a href="<%=request.getContextPath()%>/secure/admin/XmlBackup!default.jspa"/></ww:param>
        </ww:text>
        <ww:if test="/showResumeLinkStep2 == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.project.import.select.backup.resume.step2'">
                            <ww:param name="'value0'"><a href='<%=request.getContextPath()%>/secure/admin/ProjectImportSelectProject!default.jspa'></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>
        <ww:if test="/showResumeLinkStep3 == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.project.import.select.backup.resume.step3'">
                            <ww:param name="'value0'"><ww:property value="/selectedProjectName"/></ww:param>
                            <ww:param name="'value1'"><a href='<%=request.getContextPath()%>/secure/admin/ProjectImportSummary!reMapAndValidate.jspa'></ww:param>
                            <ww:param name="'value2'"></a></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>
    </page:param>

    <ui:textfield label="text('admin.project.import.select.backup.filename.label')" name="'backupXmlPath'">
        <ui:param name="'size'">50</ui:param>
        <ui:param name="'description'">
            <ww:text name="'admin.project.import.select.backup.filename.desc'"/> <ww:property value="/defaultImportPath"/>
        </ui:param>
        <ui:param name="'mandatory'">true</ui:param>
    </ui:textfield>

    <ww:if test="/defaultImportAttachmentsPath != null">
        <ui:component template="textlabel.jsp" label="text('admin.project.import.select.backup.attachment.label')" name="'defaultImportAttachmentsPath'">
            <ui:param name="'description'">
                <ww:text name="'admin.project.import.select.backup.attachment.desc'"/>
            </ui:param>
        </ui:component>
    </ww:if>

</page:applyDecorator>


</body>
</html>
