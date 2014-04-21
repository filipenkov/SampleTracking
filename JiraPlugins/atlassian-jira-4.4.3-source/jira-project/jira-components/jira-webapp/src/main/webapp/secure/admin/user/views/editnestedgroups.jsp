<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.editnestedgroups.title'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="group_browser"/>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">EditNestedGroups.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.editnestedgroups.title'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
        <ww:text name="'admin.editnestedgroups.description'"/>
        <p>
            <ww:text name="'admin.editnestedgroups.description2'"/>
        </p>
        <ul>
            <li>
                <ww:text name="'admin.editnestedgroups.admin.group.not.supported'">
                <ww:param name="'value0'"><strong></ww:param>
                <ww:param name="'value1'"></strong></ww:param>
                </ww:text>
            </li>
            <li>
                <ww:text name="'admin.editnestedgroups.memberslist.description'"/>
            </li>
            <li>
                <ww:text name="'admin.editnestedgroups.removing.description'">
                <ww:param name="'value0'"><strong></ww:param>
                <ww:param name="'value1'"></strong></ww:param>
                </ww:text>
            </li>
            <li>
                <ww:text name="'admin.editnestedgroups.adding.description'">
                <ww:param name="'value0'"><strong></ww:param>
                <ww:param name="'value1'"></strong></ww:param>
                </ww:text>
            </li>
        </ul>
        <p>
        <ww:text name="'admin.editnestedgroups.step.one'">
            <ww:param name="'value0'"><strong></ww:param>
            <ww:param name="'value1'"></strong></ww:param>
        </ww:text><br>
        <ww:text name="'admin.editnestedgroups.step.two'">
            <ww:param name="'value0'"><strong></ww:param>
            <ww:param name="'value1'"></strong></ww:param>
        </ww:text>
        </p>
    </page:param>
    <ww:if test="prunedChildrenToAssign != null">
        <tr>
            <td>
                <fieldset class="hidden parameters">
                    <input type="hidden" title="prunedChildrenToAssign" value="<ww:property value="prunedChildrenToAssign"/>"/>
                </fieldset>
                <div id="prunePanel" class="infoBox" style="display: none; cursor: pointer;" onclick="pruneErroneousNames();">
                    <ww:text name="'admin.editnestedgroups.prune.erroneous.names'">
                        <ww:param name="'value0'"><a id="prune" href="#" onclick="pruneErroneousNames();return false;"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </div>
            </td>
        </tr>
    </ww:if>
    <tr>
    <td>
        <table cellpadding="0" cellspacing="0" width="100%" border="0">
        <tr>
            <td bgcolor="#fffff0" align="center">
                <b><ww:text name="'admin.editnestedgroups.selected.x.of.y.groups'">
                    <ww:param name="'value0'"><ww:property value="selectedGroups/size"/></ww:param>
                    <ww:param name="'value1'"><ww:property value="allVisibleGroups/size"/></ww:param>
                </ww:text></b>
            </td>
            <td bgcolor="#fffff0" align="center">
                <b>
                <ww:if test="membersList/size == 1">
                    <ww:text name="'admin.editnestedgroups.n.group.members'">
                        <ww:param name="'value0'"><ww:property value="assignedChildrenCount"/></ww:param>
                    </ww:text>
                </ww:if>
                <ww:else>
                    <ww:text name="'admin.editnestedgroups.group.members'"/>
                </ww:else>
                </b>
            </td>
            <td bgcolor="#fffff0" align="center">
                <b><ww:text name="'admin.editnestedgroups.add.group.members'"/></b>
            </td>
        </tr>
        <tr>
            <td bgcolor="#ffffff" align="center">
                &nbsp;
            </td>
            <td bgcolor="#ffffff" align="center">
                <input name="unassign" type="submit" value="<ww:text name="'admin.editnestedgroups.leave'"/> &gt;&gt;">
            </td>
            <td bgcolor="#ffffff" align="center">
                <input name="assign" type="submit" value="&lt;&lt; <ww:text name="'admin.editnestedgroups.join'"/>">
            </td>
        </tr>
        <tr>
            <ww:if test="allVisibleGroups != null && allVisibleGroups/size > 0">
                <td bgcolor="#ffffff" align="center" valign="top" width="33%">
                    <select id="selectedGroupsStr" name="selectedGroupsStr" multiple size="<ww:property value="listSize(allVisibleGroups/size)" />">
                        <ww:iterator value="allVisibleGroups">
                            <option <ww:if test="isGroupSelected(.) == 'true'">selected</ww:if> value="<ww:property value="." />"><ww:property value="." /></option>
                        </ww:iterator>
                    </select>
                    <p>

                    <div id="groupRefreshPanel" name="groupRefreshPanel" class="infoBox" style="display:none;" onclick="refreshDependentFields();">
                        <ww:text name="'admin.editnestedgroups.please.refresh'">
                            <ww:param name="'value0'"><a id="refresh-dependant-fields" href="#" onclick="refreshDependentFields();return false;"></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </div>
                    <noscript>
                        <aui:component template="auimessage.jsp" theme="'aui'">
                            <aui:param name="'messageType'">warning</aui:param>
                            <aui:param name="'messageHtml'">
                                <p>
                                    <ww:text name="'admin.editnestedgroups.please.refresh.no.javascript'">
                                        <ww:param name="'value0'"><input name="refresh" type="submit" value="<ww:text name="'admin.editnestedgroups.click.here'"/>"></ww:param>
                                    </ww:text>
                                </p>
                            </aui:param>
                        </aui:component>
                    </noscript>
                    </p>
                </td>
                <td bgcolor="#ffffff" align=center valign=top width="33%">
                    <ww:if test="assignedChildrenCount > 0">
                        <select id="childrenToUnassign" name="childrenToUnassign" multiple size="<ww:property value="assignedChildrenListSize" />">
                            <ww:iterator value="membersList">
                                <optgroup label="<ww:property value="./name"/>">
                                    <ww:iterator value="./childOptions">
                                        <option value="<ww:property value="optionValue(.)"/>">
                                            <ww:property value="./name"/>
                                        </option>
                                    </ww:iterator>
                                </optgroup>
                            </ww:iterator>
                        </select>
                        <ww:if test="tooManyGroupsListed == 'true'">
                            <aui:component template="auimessage.jsp" theme="'aui'">
                                <aui:param name="'messageType'">warning</aui:param>
                                <aui:param name="'messageHtml'">
                                    <p>
                                        <ww:text name="'admin.editnestedgroups.warn.too.many.groups.for.groups'">
                                            <ww:param name="'value0'"><ww:property value="prettyPrintOverloadedGroups"/></ww:param>
                                            <ww:param name="'value1'"><ww:property value="maxChildrenDisplayedPerGroup"/></ww:param>
                                        </ww:text>
                                    </p>
                                </aui:param>
                            </aui:component>
                        </ww:if>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.editnestedgroups.no.groups.in.selection'"/>
                    </ww:else>
                </td>
                <td bgcolor="#ffffff" align=center valign=top width="33%">
                    <select id="childrenToAssignStr" name="childrenToAssignStr" multiple size="<ww:property value="listSize(allVisibleGroups/size)" />">
                        <ww:iterator value="allVisibleGroups">
                            <option value="<ww:property value="." />"><ww:property value="." /></option>
                        </ww:iterator>
                    </select>
                </td>
            </ww:if>
            <ww:else>
                <td colspan="3">
                    <ww:text name="'admin.editnestedgroups.no.groups'"/>
                </td>
            </ww:else>
        </tr>
        </table>
    </td>
    </tr>

</page:applyDecorator>

<script language="JavaScript" type="text/javascript">
    <!--
    var groupsSelectList = document.getElementById("selectedGroupsStr");
    var originalGroupValue = getMultiSelectValues(groupsSelectList);

    function refreshDependentFields()
    {
        document.jiraform.submit();
    }

    function toggleRefresh()
    {
        var groupRefreshPanel = document.getElementById("groupRefreshPanel");
        if (originalGroupValue == getMultiSelectValues(groupsSelectList) && groupRefreshPanel != null)
        {
            groupRefreshPanel.style.display = 'none';
        }
        else
        {
            groupRefreshPanel.style.display = '';
        }
    }

    <ww:if test="prunedChildrenToAssign != null">
        function pruneErroneousNames()
        {
            var childrenToAssignElement = document.getElementById("childrenToAssignStr");
            childrenToAssignElement.value = AJS.params.prunedChildrenToAssign;
            var prunePanel = document.getElementById("prunePanel");
            if (prunePanel != null)
            {
                prunePanel.style.display = 'none';
            }
        }

        function showPrunePanel()
        {
            var prunePanel = document.getElementById("prunePanel");
            if (prunePanel != null)
            {
                prunePanel.style.display = '';
            }
        }

        showPrunePanel();
    </ww:if>

    groupsSelectList.onchange = toggleRefresh;

    toggleRefresh();
//-->
</script>

</body>
</html>
