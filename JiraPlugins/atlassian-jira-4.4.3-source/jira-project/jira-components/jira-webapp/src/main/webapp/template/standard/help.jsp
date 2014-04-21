<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.jira.web.util.HelpUtil" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%-- Provides help on a topic. Sample usage:

    <ww:component name="'navigatorviews'" template="help.jsp">
        <ww:param name="'align'" value="'middle'"/>
    </ww:component>

Code use:
    com.atlassian.jira.web.util.HelpUtil helpUtil = new com.atlassian.jira.web.util.HelpUtil(request.getRemoteUser(), request.getContextPath());
    request.setAttribute("helpUtil", helpUtil);
--%>

<%--<ww:bean name="'com.atlassian.jira.web.util.HelpUtil'" id="helpUtil" />--%>
<%
    // pico tries to find a satisfiable constructor for HelpUtil, whilst none exists.  This is quite slow for performance reasons
    HelpUtil helpUtil = new HelpUtil();
    request.setAttribute("helpUtil", helpUtil);
%>

<ww:property value="@helpUtil/helpPath(parameters['name'])">
    <ww:if test="local"><%-- Open local help in a popup window --%>
        <a class="localHelp help-lnk" href="<ww:url value="url"/>" onclick="var child = window.open('<ww:url value="url"/>', 'jiraLocalHelp', 'width=600, height=500, resizable, scrollbars=yes'); child.focus(); return false;">
    </ww:if>
    <ww:else><%-- Open remote help (docs) in a new window --%>
            <a class="help-lnk" href="<ww:property value="url"/><ww:property value="parameters['helpURLFragment']" />" target="_jirahelp">
    </ww:else>
            <ww:property value="parameters['linktext']">
                <ww:if test=".">
                       <span class="link-text"><ww:property value="."/></span>
                </ww:if>
            </ww:property>
            <img src="<%= ComponentManager.getInstance().getWebResourceManager().getStaticResourcePrefix() %>/images/icons/ico_help.png" width="16" height="16"
            <%--if this is from a component then dont align right as it should be tight to the component--%>
            <ww:if test="parameters['noalign'] != true">
                <ww:property value="parameters['align']">
                    <ww:if test=".">
                        align=<ww:property value="."/>
                    </ww:if>
                    <ww:else>
                        align=right
                    </ww:else>
                </ww:property>
            </ww:if>
            border="0" alt="<ww:property value="alt" />"
            title="<ww:if test="local"><ww:text name="'local.help'"/></ww:if><ww:else><ww:text name="'online.help'"/></ww:else> <ww:text name="title" />"/></a>

</ww:property>
