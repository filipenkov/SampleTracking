<%@ page import="com.atlassian.jira.web.util.HelpUtil" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%-- Provides help on a topic. Sample usage:

    <ww:component name="'navigatorviews'" template="help.jsp" theme="aui>
    </ww:component>

Code use:
    com.atlassian.jira.web.util.HelpUtil helpUtil = new com.atlassian.jira.web.util.HelpUtil(request.getRemoteUser(), request.getContextPath());
    request.setAttribute("helpUtil", helpUtil);
--%>
<%
    // pico tries to find a satisfiable constructor for HelpUtil, whilst none exists.  This is quite slow for performance reasons
    HelpUtil helpUtil = new HelpUtil();
    request.setAttribute("helpUtil", helpUtil);
%>
<ww:declare id="varCssClass" value="parameters['cssClass']">
    <ww:if test="."> <ww:property value="."/></ww:if>
</ww:declare>
<ww:property value="@helpUtil/helpPath(parameters['name'])">
    <ww:if test="local">
        <a class="toolbar-trigger<ww:property value="@varCssClass" />" title="<ww:text name="'local.help'"/> <ww:text name="title" />" href="<ww:url value="url"/>" onclick="var child = window.open('<ww:url value="url"/>', 'jiraLocalHelp', 'width=600, height=500, resizable, scrollbars=yes'); child.focus(); return false;">
    </ww:if>
    <ww:else>
        <a class="toolbar-trigger<ww:property value="@varCssClass" />" title="<ww:text name="'online.help'"/> <ww:text name="title" />" href="<ww:property value="url"/><ww:property value="parameters['helpURLFragment']" />" target="_jirahelp">
    </ww:else>
        <span class="icon icon-help">
            <span>
            <ww:property value="parameters['linktext']">
                <ww:if test=".">
                   <ww:property value="."/>
                </ww:if>
                <ww:else>
                    <ww:property value="alt" />
                </ww:else>
            </ww:property>
            </span>
        </span>
    </a>
</ww:property>
<ww:property id="varCssClass" value="''"/>
