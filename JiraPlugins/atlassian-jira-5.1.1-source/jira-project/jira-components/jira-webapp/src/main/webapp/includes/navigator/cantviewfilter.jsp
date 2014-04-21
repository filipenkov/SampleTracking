<%@ page import="com.atlassian.jira.util.JiraUtils" %>
<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'navigator.filter.title'"/></page:param>
    <tr>
    <td bgcolor="#ffffff" colspan="2">
        <ww:if test="requestPrivate == 'true'">
            <ww:text name="'navigator.filter.error.private'"/>
        </ww:if>
        <ww:else>
            <ww:text name="'navigator.filter.error.filter.dont.exist'">
                <ww:param name="'value0'"><ww:property value="requestId"/></ww:param>
            </ww:text>
        </ww:else>

        <ww:if test="remoteUser == null">
            <p>
                <ww:text name="'login.required.desc2'">
                    <ww:param name="'value0'"><jira:loginlink><ww:text name="'common.words.login'"/></jira:loginlink></ww:param>
                </ww:text>
                <% if (ComponentAccessor.getUserManager().hasPasswordWritableDirectory()) { %>
                    <ww:if test="extUserManagement != true">
                        <% if (JiraUtils.isPublicMode()) { %>
                        <ww:text name="'login.required.desc3'">
                            <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/Signup!default.jspa"></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                        <% } %>
                    </ww:if>
                <% } %>.
            </p>
        </ww:if>

        <p>
            <ww:text name="'contact.admin.for.perm'">
                <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
            </ww:text>
        </p>
    </td>
    </tr>
</page:applyDecorator>
