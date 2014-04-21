<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.deleteuser.delete.user'"/>: <ww:property value="name" /></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>

<body>
<ww:if test="user">
    <ww:if test="deleteable == 'true'">
        <page:applyDecorator name="jiraform">
            <page:param name="description">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'admin.deleteuser.users.may.only.be.delete'"/></p>
                        <p><ww:text name="'admin.deleteuser.components.lead.desc'"/></p>
                    </aui:param>
                </aui:component>
            </page:param>
            <page:param name="jiraformId">delete_user_confirm</page:param>
            <page:param name="title"><ww:text name="'admin.deleteuser.delete.user'"/>: <ww:property value="name" /></page:param>
            <page:param name="labelWidth">50%</page:param>
            <page:param name="width">100%</page:param>
            <page:param name="action">DeleteUser.jspa</page:param>
            <page:param name="submitId">delete_submit</page:param>
            <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
            <page:param name="cancelURI">UserBrowser.jspa</page:param>
            <page:param name="autoSelectFirst">false</page:param>
            <ui:component name="'name'" template="hidden.jsp" theme="'single'"  />
            <ui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'single'"  />
            <jsp:include page="/secure/admin/user/views/assignedreported.jsp"/>
        </page:applyDecorator>
    </ww:if>
    <ww:else>
        <page:applyDecorator name="jiraform">
            <page:param name="title"><ww:text name="'admin.deleteuser.delete.user'"/>: <ww:property value="name" /></page:param>
            <page:param name="jiraformId">delete_user_confirm</page:param>
            <page:param name="width">100%</page:param>
            <page:param name="description">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'admin.deleteuser.cannot.be.deleted'"/></p>
                        <ww:if test="/nonSysAdminAttemptingToDeleteSysAdmin == true">
                            <p><ww:text name="'admin.errors.users.cannot.delete.due.to.sysadmin'"/></p>
                        </ww:if>
                        <p><ww:text name="'admin.deleteuser.components.lead.desc'"/></p>
                    </aui:param>
                </aui:component>
            </page:param>
            <jsp:include page="/secure/admin/user/views/assignedreported.jsp"/>
        </page:applyDecorator>
    </ww:else>
</ww:if>
<ww:else>
	<page:applyDecorator name="jiraform">
		<page:param name="title"><ww:text name="'admin.deleteuser.delete.user'"/>: <ww:property value="name" /></page:param>
		<page:param name="width">100%</page:param>
		<page:param name="description">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.deleteuser.user.does.not.exist'">
                        <ww:param name="'value0'"><b><ww:property value="name" /></b></ww:param>
                        <ww:param name="'value1'"><a href="<ww:url page="UserBrowser.jspa"/>"></ww:param>
                        <ww:param name="'value2'"></a></ww:param>
                    </ww:text></p>
                </aui:param>
            </aui:component>
		</page:param>
	</page:applyDecorator>
</ww:else>
</body>
</html>
