<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>
<html>
<head>
	<title><ww:text name="'admin.projects.edit.project'"/>: <ww:property value="project/string('name')" /></title>
    <jira:web-resource-require modules="jira.webresources:autocomplete"/>
    <meta name="admin.active.section" content="admin_project_menu/project_section"/>
    <meta name="admin.active.tab" content="view_projects"/>
</head>
<body>

<fieldset class="hidden parameters">
    <input type="hidden" id="uploadImage" value="<ww:text name="'avatarpicker.upload.image'"/>">
</fieldset>

<page:applyDecorator id="project-edit" name="auiform">

    <page:param name="action">EditProject.jspa</page:param>

    <page:param name="cancelLinkURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/summary</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.projects.edit.project'"/>: <ww:text name="project/string('name')" /></aui:param>
    </aui:component>

    <ww:if test="/hasInvalidLead == 'true'">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                 <ww:text name="'admin.errors.not.a.valid.lead'"/>
            </aui:param>
        </aui:component>
    </ww:if>
    <ww:else>

        <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
        <page:param name="submitButtonName">Update</page:param>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="/text('common.words.name')" name="'name'" size="'50'" maxlength="255" mandatory="'true'" theme="'aui'"/>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="/text('common.concepts.url')" name="'url'" size="'50'" maxlength="255" theme="'aui'"/>
        </page:applyDecorator>


        <page:applyDecorator name="auifieldgroup">
            <aui:component label="/text('common.concepts.project.avatar')" name="'avatarId'" template="avatarpicker.jsp" theme="'aui'">
                <aui:param name="'id'">project_avatar_image</aui:param>
                <aui:param name="'defaultId'"><ww:property value="/defaultAvatar"/></aui:param>
                <aui:param name="'src'"><ww:property value="/avatarUrl"/></aui:param>
                <aui:param name="'width'">48</aui:param>
                <aui:param name="'height'">48</aui:param>
                <aui:param name="'title'"><ww:text name="'admin.projects.edit.avatar.click.to.edit'"/></aui:param>
                <aui:param name="'avatarOwnerId'"><ww:property value="/pid"/></aui:param>
                <aui:param name="'avatarOwnerKey'"><ww:property value="/key"/></aui:param>
                <aui:param name="'mandatory'">true</aui:param>
            </aui:component>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="description">
                <ww:text name="/projectDescriptionRenderer/descriptionI18nKey"/>
            </page:param>
            <label><ww:text name="'common.words.description'"/></label>
            <ww:property value="/projectDescriptionRenderer/editHtml(/projectObject)" escape="false"/>
        </page:applyDecorator>

        <aui:component name="'pid'" template="hidden.jsp" theme="'aui'"/>
        <aui:component name="'avatarId'" template="hidden.jsp" theme="'aui'"/>

    </ww:else>

</page:applyDecorator>
</body>
</html>
