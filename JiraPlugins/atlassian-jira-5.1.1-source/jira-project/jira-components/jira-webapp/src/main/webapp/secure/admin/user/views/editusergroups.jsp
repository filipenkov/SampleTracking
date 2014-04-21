<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.editusergroups.edit.user.groups'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>

<p><a id="return_link" href="<ww:url page="ViewUser.jspa"><ww:param name="'name'" value="name"/></ww:url>">&lt;&lt;
<ww:text name="'admin.editusergroups.return.to.viewing.user'">
<ww:param name="'value0'">'<ww:property value="user/displayName"/>'</ww:param>
</ww:text></a>
</p>

<page:applyDecorator name="jiraform">
    <page:param name="action">EditUserGroups.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.editusergroups.edit.user.groups'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
        <ww:text name="'admin.editusergroups.page.description'"/>
    </page:param>
    <tr>
        <td colspan="2">
            <div id="userGroupPicker" class="ab-drag-wrap">
                <div class="ab-drag-container">
                    <h4>
                        <ww:text name="'admin.editusergroups.available.groups'"/>
                    </h4>
                    <div class="ab-items">
                        <ww:if test="/nonMemberGroups != null && /nonMemberGroups/size > 0">
                            <select name="groupsToJoin" multiple size="<ww:property value="/nonMemberGroups/size" />">
                                <ww:iterator value="/nonMemberGroups">
                                    <option value="<ww:property value="." />"><ww:property value="."/></option>
                                </ww:iterator>
                            </select>
                            <div class="buttons-container">
                                <input class="aui-button" name="join" type="submit" value="<ww:text name="'admin.editusergroups.join'"/> &gt;&gt;"/>
                            </div>
                        </ww:if>
                        <ww:else>
                            <aui:component template="auimessage.jsp" theme="'aui'">
                                <aui:param name="'messageType'">info</aui:param>
                                <aui:param name="'messageHtml'"><ww:text name="'admin.editusergroups.user.is.a.member.of.all'"/></aui:param>
                            </aui:component>
                        </ww:else>
                    </div>
                </div>
                <div class="ab-drag-container">
                    <h4>
                        <ww:text name="'common.words.groups'"/>
                    </h4>
                    <div class="ab-items">
                        <ww:if test="memberGroups != null && memberGroups/size > 0">
                        <select name="groupsToLeave" multiple size="<ww:property value="memberGroups/size" />">
                            <ww:iterator value="memberGroups">
                                <option value="<ww:property value="." />"><ww:property value="."/></option>
                            </ww:iterator>
                        </select>
                        <div class="buttons-container">
                            <input class="aui-button" name="leave" type="submit" value="&lt;&lt; <ww:text name="'admin.editusergroups.leave'"/>"/>
                        </div>
                        </ww:if>
                        <ww:else>
                            <aui:component template="auimessage.jsp" theme="'aui'">
                                <aui:param name="'messageType'">info</aui:param>
                                <aui:param name="'messageHtml'"><ww:text name="'admin.editusergroups.user.is.a.member.of.no.groups'"/></aui:param>
                            </aui:component>
                        </ww:else>
                    </div>
                </div>
           </div>
        </td>
    </tr>
    <ui:component name="'name'" template="hidden.jsp" theme="'single'"/>
</page:applyDecorator>
</body>
</html>
