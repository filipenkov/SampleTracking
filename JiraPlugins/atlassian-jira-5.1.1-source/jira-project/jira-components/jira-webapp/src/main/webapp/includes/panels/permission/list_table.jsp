<%@ taglib uri="webwork" prefix="ww" %>
<table id="global_perms" class="aui aui-table-rowhover">
    <thead>
    <ww:if test="key == 23">
        <ww:if test="allowGlobalPerms == true">
        <tr>
            <th>
                <ww:text name="'admin.globalpermissions.title'"/>
            </th>
            <th style="width: 240px;">
                <ww:text name="'admin.common.words.users.groups'"/>
            </th>
        </tr>
        </ww:if>
    </ww:if>
    <ww:else>
    <tr>
        <th>
            <ww:property value="project/string('name')" /> <ww:text name="'admin.common.words.permissions'"/>
        </th>
        <th style="width: 240px;">
            <ww:text name="'admin.common.words.users.groups'"/>
        </th>
    </tr>
    </ww:else>
    </thead>
    <tbody>
    <ww:iterator value="permTypes">
        <tr>
            <td>
                <h5><ww:property value="text(value)"/></h5>
                <ww:property value="/description(key)" escape="false"/>
                <%-- special case for 'USE' pemission --%>
                <ww:if test="key == 1"><div class="description"><ww:text name="'admin.globalpermissions.use.note'">
                    <ww:param name="'value0'"><strong></ww:param>
                    <ww:param name="'value3'"></strong></ww:param>
                </ww:text></div></ww:if>
                <%-- special case for 'ADMIN' or 'SYS_ADMIN' permissions --%>
                <ww:if test="key == 0 || key == 44"><div class="description"><ww:text name="'admin.globalpermissions.admins.note'">
                   <ww:param name="'value0'"><strong></ww:param>
                   <ww:param name="'value3'"></strong></ww:param>
                </ww:text></div></ww:if>
            </td>
            <td>
                <ww:property value="/permissionGroups(key)">
                    <ww:if test=". != null && size > 0">
                        <ww:iterator value=".">
                            <ul class="operations-list">
                                <ww:if test="group">
                                    <li><ww:property value="group" /></li>
                                </ww:if>
                                <ww:else>
                                    <li><ww:text name="'admin.common.words.anyone'"/></li>
                                </ww:else>
                                <li><a href="<%= request.getContextPath() %>/secure/admin/user/UserBrowser.jspa?group=<ww:property value="group" />"><ww:text name="'admin.globalpermissions.view.users'"/></a></li>
                                <li><a id="del_<ww:property value="../key" />_<ww:property value="group" />" href="<ww:url page="GlobalPermissions.jspa">
                                    <ww:param name="'action'">confirm</ww:param>
                                    <ww:param name="'permType'" value="../key" />
                                    <ww:param name="'groupName'" value="group"/><%-- if no group - then don't show it --%>
                                    <ww:param name="'pid'" value="pid"/>
                                </ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                            </ul>
                        </ww:iterator>
                    </ww:if>
                    <ww:else>
                        &nbsp;
                    </ww:else>
                </ww:property>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>
