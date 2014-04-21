<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>

<script type="text/javascript">
function toggle(mode, elementId)
{
    var hideElement;
    var showElement;

    if (mode == "hide")
    {
        hideElement = document.getElementById('long_' + elementId);
        showElement = document.getElementById('short_' + elementId);
    }
    else
    {
        hideElement = document.getElementById('short_' + elementId);
        showElement = document.getElementById('long_' + elementId);
    }

    if (hideElement && showElement)
    {
        hideElement.style.display = 'none';
        showElement.style.display = '';
    }
}
</script>

<ui:component label="text('admin.deleteuser.filters.created')" template="textlabel.jsp">
    
        <ww:if test="numberOfFilters > 0">
            <ww:param name="'texthtml'"><span id="numberOfFilters"><a href="<ww:url page="../filters/ViewSharedFilters.jspa"><ww:param name="'searchOwnerUserName'" value="name"/></ww:url>"><ww:property value="numberOfFilters"/></a></span></ww:param>
        </ww:if>
        <ww:else>
            <ww:param name="'texthtml'"><span id="numberOfFilters"><ww:property value="numberOfFilters"/></span></ww:param>
        </ww:else>
</ui:component>
<ui:component label="text('admin.deleteuser.filters.favourited')" template="textlabel.jsp">
    <ww:param name="'texthtml'"><span id="numberOfOtherFavouritedFilters"><ww:property value="numberOfOtherFavouritedFilters"/></span></ww:param>
    <ww:param name="'description'">
        (<ww:text name="'admin.deleteuser.filters.favourited.desc'"/>)
    </ww:param>
</ui:component>
<ui:component label="text('admin.deleteuser.portalpages.created')" template="textlabel.jsp">
    <ww:if test="numberOfNonPrivatePortalPages > 0">
        <ww:param name="'texthtml'"><span id="numberOfNonPrivatePortalPages"><a href="<ww:url page="../dashboards/ViewSharedDashboards.jspa"><ww:param name="'searchOwnerUserName'" value="name"/></ww:url>"><ww:property value="numberOfNonPrivatePortalPages"/></a></span></ww:param>
    </ww:if>
    <ww:else>
        <ww:param name="'texthtml'"><span id="numberOfNonPrivatePortalPages"><ww:property value="numberOfNonPrivatePortalPages"/></span></ww:param>
    </ww:else>
</ui:component>
<ui:component label="text('admin.deleteuser.portalpages.favourited')" template="textlabel.jsp">
    <ww:param name="'texthtml'"><span id="numberOfOtherFavouritedPortalPages"><ww:property value="numberOfOtherFavouritedPortalPages"/></span></ww:param>
    <ww:param name="'description'">
        (<ww:text name="'admin.deleteuser.portalpages.favourited.desc'"/>)
    </ww:param>
</ui:component>
<ui:component label="text('admin.deleteuser.assigned.issues')" template="textlabel.jsp">
    <ww:if test="numberOfAssignedIssues > 0">
        <ww:param name="'texthtml'"><a href="<ww:url page="/secure/IssueNavigator.jspa"><ww:param name="'reset'" value="'true'"/><ww:param name="'mode'" value="'hide'"/><ww:param name="'sorter/order'" value="'ASC'"/><ww:param name="'sorter/field'" value="'priority'"/><ww:param name="'pid'" value=".././long('id')"/><ww:param name="'assigneeSelect'" value="'specificuser'"/><ww:param name="'assignee'" value="name" /></ww:url>"><ww:property value="numberOfAssignedIssues"/></a></ww:param>
    </ww:if>
    <ww:else>
        <ww:param name="'texthtml'"><ww:property value="numberOfAssignedIssues"/></ww:param>
    </ww:else>
</ui:component>
<ui:component label="text('admin.deleteuser.reported.issues')" template="textlabel.jsp">
    <ww:if test="numberOfReportedIssues > 0">
        <ww:param name="'texthtml'"><a href="<ww:url page="/secure/IssueNavigator.jspa"><ww:param name="'reset'" value="'true'"/><ww:param name="'mode'" value="'hide'"/><ww:param name="'sorter/order'" value="'ASC'"/><ww:param name="'sorter/field'" value="'priority'"/><ww:param name="'pid'" value=".././long('id')"/><ww:param name="'reporterSelect'" value="'specificuser'"/><ww:param name="'reporter'" value="name" /></ww:url>"><ww:property value="numberOfReportedIssues"/></a></ww:param>
    </ww:if>
    <ww:else>
        <ww:param name="'texthtml'"><ww:property value="numberOfReportedIssues"/></ww:param>
    </ww:else>
</ui:component>

<ww:if test="numberOfProjectsUserLeads > 0">
<ui:component label="text('admin.deleteuser.project.lead')" template="textlabel.jsp">
    <ww:param name="'texthtml'">
        <div id="short_projects" onclick="toggle('expand', 'projects');">
            <ww:property value="projectsUserLeads(5)">
                <ww:iterator value="." status="'status'">
                    <a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./key"/>/summary"><ww:property value="./name"/></a>
                    <ww:if test="@status/last == false">,&nbsp;</ww:if>
                </ww:iterator>
                <ww:if test="/numberOfProjectsUserLeads > 5"><span style="cursor:pointer;" class="smallgrey" >...&nbsp(<ww:text name="'admin.deleteuser.projects.lead'"><ww:param  name="'value0'"><ww:property value="numberOfProjectsUserLeads" /></ww:param></ww:text>)</span></ww:if>
            </ww:property>
        </div>
        <ww:if test="numberOfProjectsUserLeads > 5">
            <div style="display:none; cursor:pointer;" id="long_projects" onclick="toggle('hide', 'projects');">
                <ww:property value="projectsUserLeads()">
                    <ww:iterator value="." status="'status'">
                        <a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./key"/>/summary"><ww:property value="./name"/></a>
                        <ww:if test="@status/last == false">,&nbsp;</ww:if>
                    </ww:iterator>
                </ww:property>
                <span class="smallgrey">[<ww:text name="'admin.deleteuser.hide'" />]</span>
            </div>
        </ww:if>
    </ww:param>
</ui:component>
</ww:if>
<ww:if test="numberOfComponentsUserLeads > 0">
<ui:component label="text('admin.deleteuser.component.lead')" template="textlabel.jsp">
    <ww:param name="'texthtml'">
        <div id="short_comps" onclick="toggle('expand', 'comps');">
            <ww:property value="/componentsUserLeads(5)">
                <ww:iterator value="." status="'status'">
                    <a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/projectKey(.)"/>/components"><ww:property value="./name"/></a><ww:if test="@status/last == false">,&nbsp;</ww:if>
                </ww:iterator>
                <ww:if test="/numberOfComponentsUserLeads > 5"><span style="cursor:pointer;" class="smallgrey" >...&nbsp(<ww:text name="'admin.deleteuser.components.lead'"><ww:param  name="'value0'"><ww:property value="numberOfComponentsUserLeads" /></ww:param></ww:text>)</span></ww:if>
            </ww:property>
        </div>
        <ww:if test="numberOfComponentsUserLeads > 5">
        <div style="display:none; cursor:pointer;" id="long_comps" onclick="toggle('hide', 'comps');">
            <ww:property value="/componentsUserLeads()">
                <ww:iterator value="." status="'status'">
                    <a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/projectKey(.)"/>/components"><ww:property value="./name"/></a><ww:if test="@status/last == false">,&nbsp;</ww:if>
                </ww:iterator>
            </ww:property>
            <span class="smallgrey">[<ww:text name="'admin.deleteuser.hide'" />]</span>
        </div>
        </ww:if>
    </ww:param>
</ui:component>
</ww:if>

