<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%
    final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
    fieldResourceIncluder.includeFieldResourcesForCurrentUser();
%>
<html>
<head>
    <title><ww:text name="'bulkworkflowtransition.title'"/></title>
    <script type="text/javascript" language="javascript">
        function check(field_id)
        {
            var cbox = document.getElementById("cb" + field_id);
            if (cbox)
            {
                cbox.checked = true;
            }
        }

        var tabClasses = new Array(<ww:property value="/fieldScreenRenderTabs/size" />);
        var j = 0;
        <ww:iterator value="/fieldScreenRenderTabs">
            <ww:if test="parameters['errortabs']/contains(.) == true">tabClasses[j++] = 'errorTabCell';</ww:if>
            <ww:else>tabClasses[j++] = 'unselectedTabCell';</ww:else>
        </ww:iterator>

        function showTab(tabId)
        {
            for(var i = 1; i <= <ww:property value="/fieldScreenRenderTabs/size" />; i++)
            {
                var tableObject = document.getElementById('tab' + i);
                var tableCellObject = document.getElementById('tabCell' + i);
                var tabCellTextSelected = document.getElementById('tabCellTextSelected' + i);
                var tabCellTextNotSelected = document.getElementById('tabCellTextNotSelected' + i);
                var selectedTableElement;
                if (i == tabId)
                {
                    tableObject.className = 'borderedTabBox';
                    tableCellObject.className='selectedTabCell';
                    tabCellTextSelected.style.display ='inline';
                    tabCellTextNotSelected.style.display ='none';
                    selectedTableElement = tableObject;
                }
                else
                {
                    tableObject.className = 'borderedTabBoxHidden';
                    tableCellObject.className = tabClasses[i - 1];
                    tabCellTextSelected.style.display ='none';
                    tabCellTextNotSelected.style.display ='inline';
                }
            }
            AJS.$(selectedTableElement || document).trigger("bulkTabSelect");
        }
    </script>
</head>
<body>
    <!-- Step 3 - Bulk Operation: Operation Details -->
    <page:applyDecorator name="bulkpanel" >
        <page:param name="title"><ww:text name="'bulkworkflowtransition.title'"/>: <ww:text name="'bulkworkflowtransition.edit.fields'"/></page:param>
        <page:param name="action">BulkWorkflowTransitionEditValidation.jspa</page:param>
        <ww:property value="'true'" id="hideSubMenu" />
        <page:param name="instructions">
            <p>
            <ww:text name="'bulkworkflowtransition.fields.available.edit'"/>
            </p>
        </page:param>

            <ul class="item-details bulk-details">
                <li>
                    <dl>
                        <dt><ww:text name="'bulkworkflowtransition.issue.workflow'"/></dt>
                        <dd><ww:property value="/bulkEditBean/selectedWFTransitionKey/workflowName" /></dd>
                    </dl>
                </li>
                <li>
                    <dl>
                        <dt><ww:text name="'bulkworkflowtransition.selected.transition'"/></dt>
                        <dd><ww:property value="/bulkWorkflowTransitionOperation/actionDescriptor(/bulkEditBean/selectedWFTransitionKey)/name" /></dd>
                    </dl>
                </li>
                <li>
                    <dl>
                        <dt><ww:text name="'bulkworkflowtransition.status.transition'"/></dt>
                        <dd>
                            <ww:property value="/originStatus(/bulkEditBean/selectedWFTransitionKey)">
                                <ww:component name="'status'" template="constanticon.jsp">
                                    <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                    <ww:param name="'iconurl'" value="./string('iconurl')" />
                                    <ww:param name="'alt'"><ww:property value="/nameTranslation(.)" /></ww:param>
                                </ww:component>
                                <ww:property value="/nameTranslation(.)" />
                            </ww:property>
                            <img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" alt="" align=absmiddle>
                            <ww:property value="/destinationStatus(/bulkEditBean/selectedWFTransitionKey)">
                                <ww:component name="'status'" template="constanticon.jsp">
                                    <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                    <ww:param name="'iconurl'" value="./string('iconurl')" />
                                    <ww:param name="'alt'"><ww:property value="/nameTranslation(.)" /></ww:param>
                                </ww:component>
                                <ww:property value="/nameTranslation(.)" />
                            </ww:property>
                        </dd>
                    </dl>
                </li>
            </ul>
            <p class="bulk-affects">
                <ww:text name="'bulkworkflowtransition.number.affected.issues'">
                    <ww:param name="'value0'"><strong><ww:property value="/bulkEditBean/selectedIssues/size()" /></strong></ww:param>
                </ww:text>
            </p>


            <ww:if test="/fieldScreenRenderTabs/empty == true">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <ww:text name="'bulkworkflowtransition.nofields.onscreen'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"></strong></ww:param>
                        </ww:text>
                    </aui:param>
                </aui:component>
            </ww:if>
            <ww:else>
            <table class="aui" id="editfields">

                <ww:if test="/fieldScreenRenderTabs/size > 1">
                    <tr><td>


                        <%-- Show tab headings --%>
                        <table cellpadding="2" cellspacing="0" border="0" width="100%" align="center">
                            <tr>
                            <ww:iterator value="/fieldScreenRenderTabs" status="'status'">
                                <td id="tabCell<ww:property value="@status/count" />"
                                    <ww:if test="@status/count == /selectedTab">class="selectedTabCell"</ww:if>
                                    <ww:else>
                                        <%-- Test if the tab has any errors on it, and if so highlight it --%>
                                        <ww:if test="/errorTabs/contains(.) == true">class="errorTabCell"</ww:if>
                                        <%-- If not simply show normal header --%>
                                        <ww:else>class="unselectedTabCell"</ww:else>
                                    </ww:else>
                                    width=1% nowrap align=center onClick="showTab(<ww:property value="@status/count"/>);">

                                    <ww:if test="@status/count == /selectedTab">
                                        &nbsp;<span id="tabCellTextSelected<ww:property value="@status/count" />" class="selectedTabFont" style="display: inline;"><ww:property value="./name"/></span><span id="tabCellTextNotSelected<ww:property value="@status/count" />" class="unselectedTabFont" style="display: none;"><a href="javascript:void(0);"><ww:property value="./name" /></a></span>&nbsp;
                                    </ww:if>
                                    <ww:else>
                                        &nbsp;<span id="tabCellTextSelected<ww:property value="@status/count" />" class="selectedTabFont" style="display: none;"><ww:property value="./name"/></span><span id="tabCellTextNotSelected<ww:property value="@status/count" />" class="unselectedTabFont" style="display: inline;"><a href="javascript:void(0);"><ww:property value="./name" /></a></span>&nbsp;
                                    </ww:else>
                                </td>
                            </ww:iterator>
                            <td width="100%">&nbsp;</td>
                            </tr>
                        </table>
                </ww:if>

                <%-- Show the actual tabs with their fields --%>
                <ww:iterator value="/fieldScreenRenderTabs" status="'status'">
                    <ww:if test="/fieldScreenRenderTabs/size > 1">
                        <table id="tab<ww:property value="@status/count"/>" <ww:if test="@status/count == /selectedTab">class="borderedTabBox"</ww:if><ww:else>class="borderedTabBoxHidden"</ww:else> cellpadding="0" cellspacing="0" align="center">
                    </ww:if>

                        <%-- Show tab's fields --%>
                        <ww:iterator value="/editActions(./name)">
                            <tr>
                            <ww:if test="./available(/bulkEditBean) == true">
                                <td width="1%">
                                    <!-- Force Resolution requirability -->
                                    <!-- If a resolution is detected on a screen - force the user to make a selection -->
                                    <!-- Avoids transitioning the issue to a 'Resolved' status without setting the 'resolution' -->
                                    <ww:if test="/forceResolution(./field) == true">
                                        <input type="checkbox" disabled="true" id="cb<ww:property value="./field/id"/>" name="actions" value="<ww:property value="./field/id"/>" checked="true" />
                                        <input type="hidden" name="forcedResolution" value="<ww:property value="./field/id"/>">
                                    </ww:if>
                                    <ww:else>
                                        <input type="checkbox" id="cb<ww:property value="./field/id"/>" name="actions" value="<ww:property value="./field/id"/>" <ww:if test="/checked(./field/id) == true">checked</ww:if> />
                                    </ww:else>
                                </td>
                                <td class="fieldLabelArea">
                                    <label for="cb<ww:property value="./field/id"/>">
                                    <ww:text name="'bulkedit.actions.changefield'">
                                        <ww:param name="'value0'"><ww:text name="./fieldName"/></ww:param>
                                    </ww:text>
                                    </label>
                                </td>
                                <ww:property value="/fieldHtml(./field)" escape="'false'" />
                            </ww:if>
                            <ww:else>
                                <td width="1%"><ww:text name="'bulkedit.constants.na'"/></td>
                                <td class="fieldLabelArea">
                                    <ww:text name="'bulkedit.actions.changefield'">
                                        <ww:param name="'value0'"><ww:property value="./fieldName" /></ww:param>
                                    </ww:text>
                                </td>
                                <td>
                                    <ww:text name="unavailableMessage">
                                        <ww:param name="'value0'"><strong></ww:param>
                                        <ww:param name="'value1'"></strong></ww:param>
                                    </ww:text>
                                </td>
                            </ww:else>
                            </tr>
                        </ww:iterator>
                    <ww:if test="/fieldScreenRenderTabs/size > 1">
                        </table>
                    </ww:if>
                </ww:iterator>

                <ww:if test="/fieldScreenRenderTabs/size > 1">
                    </td></tr>
                </ww:if>

            <ww:if test="/commentBulkEditAction/available(/bulkEditBean) == true">
                <tr><td <ww:if test="/fieldScreenRenderTabs/size == 1">colspan="3"</ww:if>>
                        <b><ww:text name="'comment.update.title'"/></b> (<ww:text name="'comment.update.desc'"/>)
                </td></tr>
                <ww:if test="/fieldScreenRenderTabs/size > 1">
                    <tr><td <ww:if test="/fieldScreenRenderTabs/size == 1">colspan="3"</ww:if> >
                        <table width="100%" border="0" cellspacing="0" cellpadding="0">
                </ww:if>

                <tr>
                    <td width="1%">
                        <input type="checkbox" id="cb<ww:property value="/commentBulkEditAction/field/id"/>" name="commentaction" value="<ww:property value="/commentBulkEditAction/field/id"/>" <ww:if test="/checked(/commentBulkEditAction/field/id) == true">checked</ww:if> />
                    </td>
                    <td class="fieldLabelArea">
                        <label for="cb<ww:property value="/commentBulkEditAction/field/id"/>">
                        <ww:text name="'bulkedit.actions.changefield'">
                            <ww:param name="'value0'"><ww:text name="/commentBulkEditAction/fieldName"/></ww:param>
                        </ww:text>
                        </label>
                    </td>
                    <ww:property value="/commentHtml" escape="false" />
                </tr>

                <ww:if test="/fieldScreenRenderTabs/size > 1">
                        </table>
                    </td></tr>
                </ww:if>
            </ww:if>

            </table>
            <script language="JavaScript" type="text/javascript">
            <!--
                <ww:iterator value="/fieldScreenRenderTabs">
                    <ww:iterator value="/editActions(./name)">
                        <ww:if test="./available(/bulkEditBean) == true">
                            <ww:if test="/forceResolution(./field) == false">
                                var field_<ww:property value="./field/id"/> = document.getElementById('<ww:property value="./field/id"/>');
                                if (field_<ww:property value="./field/id"/>) {
                                    /* Check if the field has the onchange function already */
                                    if (field_<ww:property value="./field/id"/>.onchange)
                                    {
                                        /* If it does, then we need to call that function first. */
                                        old_on_change_<ww:property value="./field/id"/> = field_<ww:property value="./field/id"/>.onchange;
                                        field_<ww:property value="./field/id"/>.onchange = function() { old_on_change_<ww:property value="./field/id"/>(); check('<ww:property value="./field/id"/>'); };
                                    }
                                    else
                                    {
                                        /* Otherwise, just call the check function. */
                                        field_<ww:property value="./field/id"/>.onchange = function() { check('<ww:property value="./field/id"/>'); };
                                    }
                                }
                            </ww:if>
                        </ww:if>
                    </ww:iterator>
                </ww:iterator>
                var commentField = document.getElementById('<ww:property value="/commentBulkEditAction/field/id"/>');
                commentField.onchange = function() { check('<ww:property value="/commentBulkEditAction/field/id"/>'); };
            //-->
            </script>
            </ww:else>


            <jsp:include page="/includes/bulkedit/bulkedit-sendnotifications.jsp"/>

    </page:applyDecorator>
</body>
</html>
