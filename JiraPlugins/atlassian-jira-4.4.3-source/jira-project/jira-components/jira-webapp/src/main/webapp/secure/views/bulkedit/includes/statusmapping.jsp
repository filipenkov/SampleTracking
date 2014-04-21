<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<ui:component name="'subTaskPhase'" template="hidden.jsp"  />

<ww:if test="/bulkEditBean/invalidStatuses != null && /bulkEditBean/invalidStatuses/empty == false">
    <table class="aui aui-table-rowhover" >
        <thead>
            <tr>
                <th width="20%"><ww:text name="'bulk.move.issue.status'"/></th>
                <th colspan="2">
                    <ww:text name="'bulk.move.affects.issues'">
                        <ww:param name="'value0'"><strong><ww:property value="/bulkEditBean/selectedIssues/size"/></strong></ww:param>
                    </ww:text>
                </th>
                <th width="5%"><ww:text name="'bulk.move.to'" /></th>
                <th><ww:text name="'bulk.move.targetworkflow'"/>&nbsp;(<ww:property value="/bulkEditBean/targetWorkflow/name"/>)</th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="/bulkEditBean/invalidStatuses">
            <tr>
                <%-- Invalid Issue Status --%>
                <td><strong><ww:text name="'bulk.move.currentstatus'"/></strong></td>
                <td>
                    <ww:component name="'status'" template="constanticon.jsp">
                        <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                        <ww:param name="'iconurl'" value="string('iconurl')" />
                        <ww:param name="'alt'"><ww:property value="/nameTranslation(.)" /></ww:param>
                        <ww:param name="'title'"><ww:property value="/nameTranslation(.)" /> - <ww:property value="/descTranslation(.)" /></ww:param>
                    </ww:component>
                    <ww:property value="/nameTranslation(.)"/>
                </td>
                <td>
                    <img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0"/>
                </td>
                <%-- Target Status --%>
                <td><strong><ww:text name="'bulk.move.targetstatus'"/></strong></td>
                <td><%-- Selection is given name with status id in order to retrieve it from params later --%>
                    <select class="imagebacked" name="<ww:property value="./string('id')"/>" >
                        <ww:iterator value="targetWorkflowStatuses(/bulkEditBean/targetIssueTypeId)">
                            <option value="<ww:property value="./string('id')" />" style="background-image: url('<ww:url value="./string('iconurl')" />');">
                                <ww:property value="/nameTranslation(.)" />
                            </option>
                        </ww:iterator>
                    </select>
                </td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
</ww:if>
