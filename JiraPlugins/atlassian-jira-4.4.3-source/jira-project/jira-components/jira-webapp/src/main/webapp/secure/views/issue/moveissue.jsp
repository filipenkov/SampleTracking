<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'moveissue.title'"/>: <ww:if test="/issueValid == 'true'"><ww:property value="issue/string('key')" /></ww:if></title>
    <ww:if test="/issueValid == true && allowedProjects/empty == false"><meta content="navigator" name="decorator" /></ww:if>
    <ww:else><meta content="message" name="decorator" /></ww:else>
    <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
</head>

<body class="nl">
<div id="stepped-process">
    <div class="steps-wrap">
    <ww:if test="/issueValid == true && allowedProjects/empty == false">
        <div class="steps-container">
            <jsp:include page="/secure/views/issue/moveissuepane.jsp" flush="false" />
        </div>
        <div class="current-step">
            <ww:if test="subTask == false">
                <page:applyDecorator name="jiraform">
                    <page:param name="title"><ww:text name="'moveissue.title'"/>: <ww:property value="issue/string('key')" /></page:param>
                    <page:param name="description">
                        <ww:text name="'moveissue.chooseproject.desc.ent'"/>
                    </page:param>
                    <page:param name="columns">1</page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="action">MoveIssue.jspa</page:param>
                    <page:param name="autoSelectFirst">false</page:param>
                    <page:param name="cancelURI"><ww:url value="/issuePath" atltoken="false" /></page:param>
                    <page:param name="submitId">next_submit</page:param>
                    <page:param name="submitName"><ww:text name="'common.forms.next'"/> &gt;&gt;</page:param>
                    <tr>
                        <td>
                            <table class="aui">
                                <tbody>
                                    <tr class="totals">
                                        <td colspan="5"><ww:text name="'moveissue.selectproject'"/></td>
                                    </tr>
                                    <tr>
                                        <%-- Select Project --%>
                                        <td width="20%" class="noWrap"><b><ww:text name="'moveissue.currentproject'"/></b>:</td>
                                        <td class="noWrap"><ww:property value="project/string('name')"/></td>
                                        <td width="1%" class="noWrap"><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" /></td>
                                        <%-- Target Project --%>
                                        <td width="20%" class="noWrap"><b><ww:text name="'moveissue.newproject'"/></b>:</td>
                                        <ww:property value="/fieldHtml('project')" escape="'false'" />
                                    </tr>
                                    <tr class="totals">
                                        <td colspan="5"><ww:text name="'moveissue.selectissuetype'"/></td>
                                    </tr>
                                    <tr>
                                        <%-- Select Issue Type --%>
                                        <td width="20%" class="noWrap"><b><ww:text name="'moveissue.currentissuetype'"/></b>:</td>
                                        <td class="noWrap"><ww:property value="./constantsManager/issueType(currentIssueType)/string('name')"/></td>
                                        <td align="absmiddle" nowrap><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" /></td>
                                        <%-- Target Issue Type --%>
                                        <td width="20%" class="noWrap"><b><ww:text name="'moveissue.newissuetype'"/></b>:</td>
                                        <ww:property value="/fieldHtml('issuetype')" escape="'false'" />
                                    </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <ui:component name="'id'" template="hidden.jsp"  theme="'single'" />
                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <%-- Sub Tasks cannot be moved - must move parent issue. This will only happen if someone intentionally crafts
                a URL to try to do this --%>
                <page:applyDecorator name="jiraform">
                    <page:param name="title"><ww:text name="'moveissue.title'"/></page:param>
                    <page:param name="description">
                        <span class="warning"><ww:text name="'moveissue.subtask.cannot.move'"/></span>
                    </page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="cancelURI"><ww:url value="/issuePath" atltoken="false" /></page:param>

                    <ui:component name="'id'" template="hidden.jsp"  theme="'single'" />
                </page:applyDecorator>
            </ww:else>
        </div>
    </ww:if>
    <ww:else>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <ww:iterator value="flushedErrorMessages">
                    <p><ww:property /></p>
                </ww:iterator>
                <%@ include file="/includes/noprojects.jsp" %>
            </aui:param>
        </aui:component>
    </ww:else>
    </div>
</div>
</body>
</html>
