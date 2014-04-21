<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<div class="module message">
    <img src="<ww:property value="/avatarUrl"/>" alt="<ww:text name="'admin.project.avatar.delete.confirmation'"/>" width="48" height="48"/>
    <p><ww:text name="'admin.project.avatar.delete.confirmation'"/></p>
    <form action="<%= request.getContextPath() %>/secure/project/DeleteAvatar.jspa">
        <input type="hidden" name="avatarId" value="<ww:property value="/avatarId"/>" />
        <input type="hidden" name="avatarType" value="<ww:property value="/avatarType"/>" />
        <input type="hidden" name="ownerId" value="<ww:property value="/ownerId"/>" />
        <input type="hidden" name="confirm" value="true" />
        <input type="hidden" value="true" name="close"/>
        <input type="submit" value="<ww:text name="'common.words.delete'"/>" />
        <input type="button" class="cancel" value="<ww:text name="'common.forms.cancel'"/>" />
    </form>
</div>

