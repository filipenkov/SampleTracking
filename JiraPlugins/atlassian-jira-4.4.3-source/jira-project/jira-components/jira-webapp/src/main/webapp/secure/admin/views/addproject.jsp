<%@ page import="com.atlassian.jira.ComponentManager,
                 com.atlassian.plugin.webresource.WebResourceManager"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.addproject.create.new.project'"/></title>
    <%
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:avatarpicker");
    %>
    <meta name="admin.active.section" content="admin_project_menu/project_section"/>
    <meta name="admin.active.tab" content="view_projects"/>
</head>
<body>

<div class="content">

    <page:applyDecorator id="add-project" name="auiform">
        <page:param name="action">AddProject.jspa</page:param>
        <page:param name="submitButtonName">Add</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url value="'/secure/project/ViewProjects.jspa'" atltoken="false" /></page:param>
        <page:param name="showHint"><ww:property value="/showImportHint" /></page:param>
        <page:param name="hint">
            <ww:text name="'admin.addproject.project.import.link'">
                <ww:param name="'value0'"><a href="<ww:url atltoken="false" value="'/secure/admin/views/ExternalImport1.jspa'"/>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </page:param>
        <page:param name="hintTooltip"><ww:text name="'admin.addproject.project.import.link.tip'"/></page:param>
        <page:param name="hideHintLabel">true</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.addproject.add.a.new.project'"/></aui:param>
        </aui:component>

        <page:applyDecorator name="auifieldset">
            <page:applyDecorator name="auifieldgroup">
                <aui:textfield label="text('common.words.name')" name="'name'" maxlength="150" theme="'aui'">
                    <aui:param name="'mandatory'" value="true" />
                </aui:textfield>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description">
                <ww:text name="'admin.addproject.key.description'">
                        <ww:param name="'value0'"><br></ww:param>
                    </ww:text><br>
                    <ww:property value="projectKeyDescription" escape="false" />
                </page:param>
                <aui:textfield label="text('common.concepts.key')" name="'key'" maxlength="255" theme="'aui'">
                    <aui:param name="'mandatory'" value="true" />
                </aui:textfield>
            </page:applyDecorator>

            <ww:if test="/shouldShowLead == true">
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="id">lead-picker</page:param>
                    <page:param name="description"><ww:text name="'admin.addproject.project.lead.description'"/></page:param>
                    <aui:component label="text('common.concepts.projectlead')" name="'lead'" id="'lead'" template="singleSelectUserPicker.jsp" theme="'aui'">
                        <aui:param name="'inputText'" value="/leadError" />
                        <aui:param name="'userName'" value="/lead"/>
                        <aui:param name="'userFullName'" value="/leadUserObj/displayName"/>
                        <aui:param name="'userAvatar'" value="/leadUserAvatarUrl"/>
                        <aui:param name="'mandatory'" value="'true'" />
                        <aui:param name="'disabled'" value="/userPickerDisabled" />
                    </aui:component>
                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <aui:component label="'lead'" name="'lead'" template="hidden.jsp" theme="'aui'">
                    <aui:param name="'divId'" value="'lead-picker'" />
                </aui:component>
            </ww:else>

            <aui:component label="'permissionScheme'" name="'permissionScheme'" template="hidden.jsp" theme="'aui'"/>
            <aui:component label="'assigneeType'" name="'assigneeType'" template="hidden.jsp" theme="'aui'"/>

            <ww:if test="/inlineDialogMode == false && /showImportHint == true">
                <ww:text name="'admin.addproject.project.import.link'">
                    <ww:param name="'value0'"><a href="<ww:url atltoken="false" value="'/secure/admin/views/ExternalImport1.jspa'"/>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </ww:if>

        </page:applyDecorator>
    </page:applyDecorator>
</div>

</body>
</html>
