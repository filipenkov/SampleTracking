<%@ page import="com.atlassian.jira.util.JiraUtils"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<tr>
<td bgcolor=#ffffff colspan=2>
<ww:if test="remoteUser == null">
	<p>
	 <ww:text name="'showconstantshelp.notloggedin'"/>
	</p>

    <p>
    <ww:text name="'showconstantshelp.mustfirstlogin'">
        <ww:param name="'value0'"><jira:loginlink><ww:text name="'common.words.login'"/></jira:loginlink></ww:param>
        <ww:param name="'value1'"></ww:param>
    </ww:text>
    <ww:if test="extUserManagement != true">
		<% if (JiraUtils.isPublicMode()) { %>
            <ww:text name="'noprojects.signup'">
                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/Signup!default.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        <% } %>
    </ww:if>
    </p>
</ww:if>
<ww:else>
	<p><ww:text name="'showconstantshelp.error.permission'"/></p>
</ww:else>
</td>
</tr>
