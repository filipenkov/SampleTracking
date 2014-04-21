<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<html>
<head>
	<title><ww:text name="'admin.projects.edit.project'"/>: <ww:property value="project/string('name')" /></title>

    <%
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:autocomplete");
        webResourceManager.requireResource("jira.webresources:avatarpicker");
    %>
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
                <aui:param name="'linkid'">project_avatar_link</aui:param>
                <aui:param name="'linkclass'">avatar_link</aui:param>
                <aui:param name="'id'">project_avatar_image</aui:param>
                <aui:param name="'class'">avatar-image</aui:param>
                <aui:param name="'hiddenid'">project_avatar_id</aui:param>
                <aui:param name="'url'"><%= request.getContextPath() %>/secure/project/AvatarPicker!default.jspa?ownerId=<ww:property value="/pid"/>&avatarField=project_avatar_id&avatarType=project&updateUrl=<%= request.getContextPath() %>/rest/api/1.0/project/<ww:property value="/project/string('key')"/>/avatar</aui:param>
                <aui:param name="'src'"><ww:property value="/avatarUrl"/></aui:param>
                <aui:param name="'width'">48</aui:param>
                <aui:param name="'height'">48</aui:param>
                <aui:param name="'textid'">remove</aui:param>
                <aui:param name="'title'"><ww:text name="'admin.projects.edit.avatar.click.to.edit'"/></aui:param>
                <aui:param name="'mandatory'">true</aui:param>
                <aui:param name="'description'"><ww:text name="'admin.projects.edit.avatar'"/></aui:param>
            </aui:component>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="description">
                <ww:text name="'admin.addproject.description.description'"/>
            </page:param>
            <aui:textarea label="text('common.words.description')" name="'description'" rows="5" theme="'aui'"/>
        </page:applyDecorator>

        <aui:component name="'pid'" template="hidden.jsp" theme="'aui'"/>
        <aui:component name="'avatarId'" template="hidden.jsp" theme="'aui'"/>

    </ww:else>

</page:applyDecorator>
</body>
</html>
