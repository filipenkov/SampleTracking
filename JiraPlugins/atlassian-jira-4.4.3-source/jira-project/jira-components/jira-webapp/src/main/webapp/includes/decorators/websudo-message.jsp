<%@ page import="com.atlassian.jira.security.websudo.InternalWebSudoManager" %>
<%@ page import="com.atlassian.crowd.embedded.api.User" %>
<%
    final User loggedInUser = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
    if(loggedInUser != null) {

        final InternalWebSudoManager websudoManager = ComponentManager.getComponentInstanceOfType(InternalWebSudoManager.class);

        if (websudoManager.isEnabled() && websudoManager.hasValidSession(session))
        {
%>
<div class="global-warning" id="websudo-banner">
<%
        if (websudoManager.isWebSudoRequest(request))
        {
%>
            <ww:text name="'websudo.enabled.message'">
                <ww:param name="'value0'"><a id="websudo-drop-from-protected-page"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
                <ww:param name="'value2'"><a target="_blank" href="<ww:property value="@helpUtil/helpPath('websudo')/url"/>"></ww:param>
                <ww:param name="'value3'"></a></ww:param>
            </ww:text>
<%
        }
        else
        {
%>
            <ww:text name="'websudo.enabled.message'">
                <ww:param name="'value0'"><a id="websudo-drop-from-normal-page"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
                <ww:param name="'value2'"><a target="_blank" href="<ww:property value="@helpUtil/helpPath('websudo')/url"/>"></ww:param>
                <ww:param name="'value3'"></a></ww:param>
            </ww:text>
<%
        }
%>
</div>
<%
        }
    }
%>