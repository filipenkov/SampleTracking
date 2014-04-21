<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.bulkeditgroups.title'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="group_browser"/>
</head>
<body>

<p><a href="<ww:url page="/secure/admin/user/GroupBrowser.jspa" atltoken="false"/>"><ww:text name="'admin.groupbrowser.return'" /></a></p>

<page:applyDecorator name="jiraform">
    <page:param name="action">BulkEditUserGroups.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.bulkeditgroups.title'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
        <ww:text name="'admin.bulkeditgroups.description'"/>
        <p>
            <ww:text name="'admin.bulkeditgroups.description2'"/>
        </p>
        <ul>
            <li>
                <ww:text name="'admin.bulkeditgroups.memberslist.description'"/>
            </li>
            <li>
                <ww:text name="'admin.bulkeditgroups.removing.description'">
                <ww:param name="'value0'"><strong></ww:param>
                <ww:param name="'value1'"></strong></ww:param>
                </ww:text>
            </li>
            <li>
                <ww:text name="'admin.bulkeditgroups.adding.description'">
                <ww:param name="'value0'"><strong></ww:param>
                <ww:param name="'value1'"></strong></ww:param>
                </ww:text>
            </li>
        </ul>
        <p>
        <ww:text name="'admin.bulkeditgroups.step.one'">
            <ww:param name="'value0'"><strong></ww:param>
            <ww:param name="'value1'"></strong></ww:param>
        </ww:text><br>
        <ww:text name="'admin.bulkeditgroups.step.two'">
            <ww:param name="'value0'"><strong></ww:param>
            <ww:param name="'value1'"></strong></ww:param>
        </ww:text>
        </p>
    </page:param>
    <ww:if test="prunedUsersToAssign != null">
        <tr>
            <td>
                <fieldset class="hidden parameters">
                    <input type="hidden" title="prunedUsersToAssign" value="<ww:property value="prunedUsersToAssign"/>"/>
                </fieldset>
                <div id="prunePanel" class="aui-message info" style="display: none; cursor: pointer;" onclick="pruneErroneousNames();">
                    <span class="aui-icon icon-info"></span>
                    <p>
                    <ww:text name="'admin.bulkeditgroups.prune.erroneous.names'">
                        <ww:param name="'value0'"><a id="prune" href="#" onclick="pruneErroneousNames();return false;"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                    </p>
                </div>
            </td>
        </tr>
    </ww:if>
    <tr>
    <td>
        <ww:if test="allVisibleGroups != null && allVisibleGroups/size > 0">
        <table class="aui bulk-edit-user-groups">
            <thead>
                <tr>
                    <th width="33%">
                        <ww:text name="'admin.bulkeditgroups.selected.x.of.y.groups'">
                            <ww:param name="'value0'"><ww:property value="selectedGroupsUserHasPermToSee/size"/></ww:param>
                            <ww:param name="'value1'"><ww:property value="allVisibleGroups/size"/></ww:param>
                        </ww:text>
                    </th>
                    <th width="33%">
                    <ww:if test="membersList/size == 1">
                        <ww:text name="'admin.bulkeditgroups.n.group.members'">
                            <ww:param name="'value0'"><ww:property value="assignedUsersCount"/></ww:param>
                        </ww:text>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.bulkeditgroups.group.members'"/>
                    </ww:else>
                    </th>
                    <th width="33%">
                        <ww:text name="'admin.bulkeditgroups.add.group.members'"/>
                    </th>
                </tr>
            </thead>
            <tbody>
                <tr class="butt-row">
                    <td>
                        &nbsp;
                    </td>
                    <td>
                        <input class="aui-button" name="unassign" type="submit" value="<ww:text name="'admin.editusergroups.leave'"/> &gt;&gt;">
                    </td>
                    <td>
                        <input class="aui-button" name="assign" type="submit" value="&lt;&lt; <ww:text name="'admin.editusergroups.join'"/>">
                    </td>
                </tr>
                <tr class="totals">
                    <td>
                        <div class="field-group">
                            <select id="selectedGroupsStr" name="selectedGroupsStr" multiple size="<ww:property value="listSize(allVisibleGroups/size)" />">
                                <ww:iterator value="allVisibleGroups">
                                    <option <ww:if test="isGroupSelected(.) == 'true'">selected</ww:if> value="<ww:property value="name" />"><ww:property value="name" /></option>
                                </ww:iterator>
                            </select>
                            <div id="groupRefreshPanel" name="groupRefreshPanel" class="aui-message info" style="display:none;" onclick="refreshDependentFields();">
                                <span class="aui-icon icon-info"></span>
                                <p>
                                <ww:text name="'admin.bulkeditgroups.please.refresh'">
                                    <ww:param name="'value0'"><a id="refresh-dependant-fields" href="#" onclick="refreshDependentFields();return false;"></ww:param>
                                    <ww:param name="'value1'"></a></ww:param>
                                </ww:text>
                                </p>
                            </div>
                        </div>
                    </td>
                    <td>
                        <div class="field-group">
                        <ww:if test="assignedUsersCount > 0">
                            <select id="usersToUnassign" name="usersToUnassign" multiple size="<ww:property value="assignedUsersListSize" />">
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
                            <ww:if test="tooManyUsersListed == 'true'">
                                <aui:component template="auimessage.jsp" theme="'aui'">
                                    <aui:param name="'messageType'">warning</aui:param>
                                    <aui:param name="'messageHtml'">
                                        <p>
                                            <ww:text name="'admin.bulkeditgroups.warn.too.many.users.for.groups'">
                                                <ww:param name="'value0'"><ww:property value="prettyPrintOverloadedGroups"/></ww:param>
                                                <ww:param name="'value1'"><ww:property value="maxUsersDisplayedPerGroup"/></ww:param>
                                            </ww:text>
                                        </p>
                                    </aui:param>
                                </aui:component>
                            </ww:if>
                        </ww:if>
                        <ww:else>
                            <aui:component template="auimessage.jsp" theme="'aui'">
                                <aui:param name="'messageType'">info</aui:param>
                                <aui:param name="'messageHtml'">
                                    <p><ww:text name="'admin.bulkeditgroups.no.users.in.selection'"/></p>
                                </aui:param>
                            </aui:component>
                        </ww:else>
                        </div>
                    </td>
                    <ui:component name="'usersToAssignStr'" value="usersToAssignStr" template="multiuserpicker.jsp">
                        <ui:param name="'nolabel'" value="'true'"/>
                        <ui:param name="'style'" value="''"/>
                    </ui:component>
                </tr>
            </tbody>
        </table>
        </ww:if>
        <ww:else>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.bulkeditgroups.no.groups'"/></p>
                </aui:param>
            </aui:component>
        </ww:else>
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

    <ww:if test="prunedUsersToAssign != null">
        function pruneErroneousNames()
        {
            var usersToAssignElement = document.getElementById("usersToAssignStr");
            usersToAssignElement.value = AJS.params.prunedUsersToAssign;
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
