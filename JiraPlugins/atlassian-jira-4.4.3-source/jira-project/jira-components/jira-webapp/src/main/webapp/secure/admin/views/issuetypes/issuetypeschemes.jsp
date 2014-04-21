<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>



<table id="issuetypeschemes" class="aui">
    <thead>
        <tr>
            <th>
                <ww:text name="'common.words.name'"/>
            </th>
            <th>
                <ww:text name="'common.words.description'"/>
            </th>
            <th>
                <ww:text name="'admin.common.words.options'"/>
            </th>
            <th>
                <ww:text name="'common.concepts.projects'"/>
            </th>
            <th>
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="/schemes" status="'status'">
    <tr <ww:if test="$actionedSchemeId == ./id">class="rowHighlighted"</ww:if><ww:elseIf test="@status/modulus(2) == 0">class="rowAlternate"</ww:elseIf>>
        <td><ww:property value="name" /></td>
        <td><ww:property value="description"/></td>
        <td>
        <ww:property value="/options(.)" >
            <ww:if test=". && ./size() > 0">
                <ul class="imagebacked">
                <ww:iterator value="." status="'status2'">
                    <li style="background-image: url('<ww:url value="./imagePath" />')"><ww:property value="./name" /><ww:if test="/default(./id, ../..) == true"> <span class="smallgrey">(<ww:text name="'admin.common.words.default'"/>)</span></ww:if></li>
                </ww:iterator>
                </ul>
            </ww:if>
            <ww:else>
                <span class="errorText"><ww:text name="'admin.issuesettings.no.issue.types.associated'"/></span>
            </ww:else>
        </ww:property>
        </td>

        <td>
        <ww:if test="./global == true">
            <ww:text name="'admin.issuesettings.global'"/>
        </ww:if>
        <ww:elseIf test="./associatedProjects && ./associatedProjects/size() > 0">
            <ul>
            <ww:iterator value="./associatedProjects" status="'status2'">
                <li><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="./string('name')" /></a></li>
            </ww:iterator>
            </ul>
        </ww:elseIf>
        <ww:else>
            <span class="errorText"><ww:text name="'admin.issuesettings.no.project'"/></span>
        </ww:else>
        </td>

        <td>
            <ul class="operations-list">
                <li><a id="edit_<ww:property value="id"/>" href="ConfigureOptionSchemes!default.jspa?fieldId=<ww:property value="fieldId" />&schemeId=<ww:property value="id"/>"><ww:text name="'common.words.edit'"/></a></li>
            <ww:if test="/default(.) != true">
                <li><a id="associate_<ww:property value="id"/>" href="AssociateIssueTypeSchemes!default.jspa?fieldId=<ww:property value="fieldId" />&schemeId=<ww:property value="id"/>"><ww:text name="'admin.projects.schemes.associate'"/></a></li>
            </ww:if>
            <ww:else>
                <li><a id="associate_<ww:property value="id"/>" href="AssociateIssueTypeSchemesWithDefault!default.jspa?fieldId=<ww:property value="fieldId" />&schemeId=<ww:property value="id"/>"><ww:text name="'admin.projects.schemes.associate'"/></a></li>
            </ww:else>
                <li><a id="copy_<ww:property value="id"/>" href="ConfigureOptionSchemes!copy.jspa?fieldId=<ww:property value="fieldId" />&schemeId=<ww:property value="id"/>"><ww:text name="'common.words.copy'"/></a></li>
            <ww:if test="/default(.) != true">
                <li><a id="delete_<ww:property value="id"/>" href="DeleteOptionScheme!default.jspa?fieldId=<ww:property value="fieldId" />&schemeId=<ww:property value="id"/>"><ww:text name="'common.words.delete'"/></a></li>
            </ww:if>
            </ul>
        </td>
    </tr>
    </ww:iterator>
    </tbody>
</table>

<page:applyDecorator name="jiraform">
    <page:param name="action"><ww:url page="ConfigureOptionSchemes!default.jspa"><ww:param name="'fieldId'" value="fieldId" /></ww:url></page:param>
    <page:param name="submitName"><ww:property value="text('common.forms.add')"/></page:param>
    <page:param name="submitId">submitAdd</page:param>
    <page:param name="title"><ww:text name="'admin.issuesettings.add.new.scheme'">
        <ww:param name="'value0'"><ww:property value="titleSingle" /></ww:param>
    </ww:text></page:param>

    <ui:textfield label="text('common.words.name')" name="'name'"  >
        <ui:param name="'mandatory'">true</ui:param>
        <ui:param name="'class'">standardField</ui:param>
    </ui:textfield>

    <ui:textfield label="text('common.words.description')" name="'description'" >
        <ui:param name="'class'">standardField</ui:param>
    </ui:textfield>

</page:applyDecorator>
