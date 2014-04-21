<%@ taglib uri="jiratags" prefix="jira" %>
<div class="p-list">
    <table class="aui">
        <thead>
            <tr>
                <th width="25%"><ww:text name="'common.concepts.project'" /></th>
                <th width="10%"><ww:text name="'common.concepts.key'" /></th>
                <th width="25%"><ww:text name="'common.concepts.projectlead'" /></th>
                <th width="40%"><ww:text name="'common.concepts.url'" /></th>
            </tr>
        </thead>
        <tbody class="projects-list">
        <ww:iterator>
            <tr>
                <td>
                    <ww:if test="./long('avatar') != null">
                       <img alt="" class="project-avatar-16" height="16" src="<%= request.getContextPath() %>/secure/projectavatar?size=small&pid=<ww:property value="./long('id')"/>&avatarId=<ww:property value="./long('avatar')"/>" width="16" />
                   </ww:if>
                   <ww:else>
                       <img alt="" class="project-avatar-16" height="16" src="<%= request.getContextPath() %>/images/icons/favicon.png" width="16">
                    </ww:else>
                    <a href="<%= request.getContextPath() %>/browse/<ww:property value="string('key')" />"><ww:property value="string('name')" /></a>
                </td>
                <td>
                    <ww:property value="./string('key')" />
                </td>
                <td>
                    <ww:if test="string('lead')">
                        <jira:formatuser user="./string('lead')" type="'profileLink'" id="'project_' + ./string('key') + '_table'"/>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'browse.projects.no.lead'" />.
                    </ww:else>
                </td>
                <td class="cell-type-url">
                    <ww:if test="/stringSet(.,'url') == false">
                        <ww:text name="'browse.projects.no.url'" />
                    </ww:if>
                    <ww:else>
                        <a href="<ww:property value="./string('url')" />"><ww:property value="./string('url')" /></a>
                    </ww:else>
                </td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
</div>
