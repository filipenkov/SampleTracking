<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<table class="blank">
<ww:if test="./enabled != true">
    <title><ww:text name="'admin.issuefields.customfields.contexts'"/></title>
    <tr><td colspan="2"><ww:text name="'admin.issuefields.customfields.not.configured.context'"/></td></tr>
</ww:if>
<ww:else>
    <title><ww:text name="'admin.issuefields.customfields.contexts'"/></title>

    <ww:if test="./global == true">
    <tr><td colspan="2"><ww:text name="'admin.issuefields.customfields.global.all.issues'"/></td></tr>
    </ww:if>

    <ww:if test="./allIssueTypes == false">
    <tr>
        <td><ww:text name="'admin.issuefields.customfields.issue.types'"/>:</td>
        <td>
        <ww:iterator value="./associatedIssueTypes" status="'status'">
            <ww:property value="/constantsManager/issueTypeObject(./string('id'))">
                <a style="padding-right: 0.3em;" href="EditIssueType!default.jspa?returnUrl=ViewCustomFields.jspa&id=<ww:property value="./id" />">
<ww:component name="'customfieldicon'" template="constanticon.jsp">
  <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
  <ww:param name="'iconurl'" value="iconUrl" />                             -
  <ww:param name="'alt'"><ww:property value="nameTranslation" /></ww:param>
  <ww:param name="'title'"><ww:property value="nameTranslation"/> - <ww:property value="descTranslation"/></ww:param>
</ww:component>
                </a>
            </ww:property>
        </ww:iterator>
        </td>
    </tr>
    </ww:if>

    <ww:if test="./associatedProjectCategories != null && ./associatedProjectCategories/empty == false">
    <tr>
        <td><ww:text name="'admin.issuefields.customfields.project.categories'"/>:</td>
        <td><ww:iterator value="./associatedProjectCategories" status="'status'">
           <a title="<ww:property value="string('name')" /><ww:property value="string('description')"><ww:if test=". && !./equals('')"> - <ww:property value="." /></ww:if></ww:property>" href="EditProjectCategory!default.jspa?returnUrl=ViewCustomFields.jspa&id=<ww:property value="long('id')" />"><ww:property value="string('name')" /></a><br />
         </ww:iterator></td>
    </tr>
    </ww:if>
    <ww:if test="./associatedProjects != null && ./associatedProjects/empty == false">
    <tr>
        <td><ww:text name="'admin.issuefields.customfields.projects'"/>:</td>
        <td><ww:iterator value="./associatedProjects" status="'status'">
           <a title="<ww:property value="string('name')" /><ww:property value="string('description')"><ww:if test=". && !./equals('')"> - <ww:property value="." /></ww:if></ww:property>" href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="string('name')" /></a><br />
         </ww:iterator></td>
    </tr>
    </ww:if>
</ww:else>
</table>

