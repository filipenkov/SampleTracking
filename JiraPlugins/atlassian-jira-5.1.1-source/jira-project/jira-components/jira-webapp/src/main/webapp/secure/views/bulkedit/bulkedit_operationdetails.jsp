<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%
    final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
    fieldResourceIncluder.includeFieldResourcesForCurrentUser();
%>
<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
</head>
<body>
    <!-- Step 3 - Bulk Operation: Operation Details -->
    <div class="content-container" id="stepped-process">
        <div class="content-related">
            <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator name="jirapanel">
                <page:param name="title"><ww:text name="'bulkedit.title'"/> <ww:text name="'bulkedit.step3'"/>: <ww:text name="'bulkedit.step3.title'"/></page:param>
                <page:param name="description">
                    <!-- check for EDIT_ISSUE permissions and show an appropriate error message if user does not have this permission -->
                    <ww:if test="hasAvailableActions == false">
                        <ww:if test="/bulkEditBean/multipleProjects == true">
                            <ww:text name="'bulkedit.step2.note.noactions.multiple'">
                                <ww:param name="'value0'"><b><ww:property value="/bulkEditBean/selectedIssues/size"/></b></ww:param>
                                <ww:param name="'value1'"><b><ww:property value="/bulkEditBean/projectIds/size"/></b></ww:param>
                            </ww:text>
                        </ww:if>
                        <ww:else>
                            <ww:text name="'bulkedit.step2.note.noactions.single'">
                                <ww:param name="'value0'"><b><ww:property value="/bulkEditBean/selectedIssues/size"/></b></ww:param>
                                <ww:param name="'value1'"><b><ww:property value="/bulkEditBean/project/string('name')"/></b></ww:param>
                            </ww:text>
                        </ww:else>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'bulkedit.step2.desc'">
                            <ww:param name="'value0'"><b><ww:property value="/bulkEditBean/selectedIssues/size"/></b></ww:param>
                        </ww:text>
                    </ww:else>
                </page:param>
                <page:param name="width">100%</page:param>
            </page:applyDecorator>
            <form class="aui top-label" name="jiraform" action="BulkEditDetailsValidation.jspa" method="POST">
                <ww:if test="visibleActions/empty == 'false'">
                    <table id="availableActionsTable">
                        <ww:iterator value="visibleActions">
                            <tr class="availableActionRow">
                                <td width="20%">
                                    <input class="checkbox" type="checkbox" id="cb<ww:property value="./field/id"/>" name="actions" value="<ww:property value="./field/id"/>" <ww:if test="/checked(./field/id) == true">checked="checked"</ww:if>>
                                    <label for="cb<ww:property value="./field/id"/>">
                                    <ww:text name="'bulkedit.actions.changefield'">
                                        <ww:param name="'value0'" value="/htmlEncode(./fieldName)"/>
                                    </ww:text>
                                    </label>
                                </td>
                                <ww:property value="/fieldHtml(./field/id)" escape="'false'" />
                            </tr>
                        </ww:iterator>
                    </table>
                </ww:if>
                <ww:if test="hiddenActions/empty == 'false'">
                    <div id="unavailableActionsTable" class="twixi-block collapsed">
                        <div class="twixi-trigger">
                            <h5><span class="icon icon-twixi"></span><ww:text name="'bulkedit.actions.show.unavailable'"/></h5>
                        </div>
                        <div class="twixi-content">
                            <ul>
                                <ww:iterator value="hiddenActions">
                                    <li>
                                        <ww:text name="'bulkedit.actions.changefield'">
                                            <ww:param name="'value0'"><ww:property value="./fieldName" /></ww:param>
                                        </ww:text>
                                        <div class="description">
                                            <ww:text name="unavailableMessage">
                                                <ww:param name="'value0'"><span class="highlight"></ww:param>
                                                <ww:param name="'value1'"></span></ww:param>
                                            </ww:text>
                                        </div>
                                    </li>
                                </ww:iterator>
                            </ul>
                        </div>
                    </div>
                </ww:if>
                <jsp:include page="/includes/bulkedit/bulkedit-sendnotifications.jsp"/>
                <%@include file="bulkchooseaction_submit_buttons.jsp"%>
                <!-- Hidden field placed here so as not affect the buttons -->
                <ww:if test="/canDisableMailNotifications() == false">
                    <ui:component name="'sendBulkNotification'" template="hidden.jsp" theme="'single'" value="'true'" />
                </ww:if>
            </form>
        </div>
    </div>
</body>
</html>
