<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<div class="module message">
    <p><ww:text name="'avatarpicker.cropper.instructions'"/></p>
    <form id="avataror" action="<%= request.getContextPath() %>/secure/project/AvatarPicker.jspa">
        <input type="hidden" value="<ww:property value="/ownerId"/>" name="ownerId"/>
        <input type="hidden" value="<ww:property value="/avatarType"/>" name="avatarType"/>
        <input type="hidden" value="true" name="close"/>
        <input type="hidden" value="<ww:property value="/avatarField"/>" name="avatarField"/>
        <input type="hidden" value="<ww:property value="/remove"/>" name="remove"/>

        <%-- crop instructions --%>
        <input type="hidden" name="offsetX" id="avatar-offsetX" value="<ww:property value="/offsetX"/>"/>
        <input type="hidden" name="offsetY" id="avatar-offsetY"value="<ww:property value="/offsetY"/>"/>
        <input type="hidden" name="width" id="avatar-width" value="<ww:property value="/width"/>"/>


        <input type="submit" value="<ww:text name="'avatarpicker.choose.avatar'"/>" />
        <input type="button" value="<ww:text name="'common.forms.cancel'"/>" />
    </form>
</div>

<div class="avataror">
    <img src="<%= request.getContextPath() %>/secure/temporaryavatar" alt="" title="" /> <%-- TODO width and height --%>
</div>


