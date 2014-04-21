<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<table class="aui">
    <thead>
        <tr>
            <th>
                <ww:text name="'common.words.name'"/>
            </th>
            <th>
                <ww:text name="'common.words.description'"/>
            </th>
            <ww:if test="typeEnabled == true">
                <th>
                    <ww:text name="'admin.common.words.type'"/>
                </th>
            </ww:if>
            <ww:if test="/schemes/size() > 1">
                <th>
                    <ww:text name="'admin.issuesettings.issuetypes.related.schemes'"/>
                </th>
            </ww:if>
            <th>
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="allOptions" status="'status'">
    <tr <ww:if test="@status/modulus(2) == 0">class="rowAlternate"</ww:if>>
        <td>
            <ww:if test="../iconEnabled == true">
                <ww:component name="../fieldId" template="constanticon.jsp">
                  <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                  <ww:param name="'iconurl'" value="iconUrl" />
                  <ww:param name="'alt'"><ww:property value="name" /></ww:param>
                </ww:component>
            </ww:if>
            <strong><ww:property value="name"/></strong>
        </td>
        <td><ww:property value="description"/></td>
        <ww:if test="../typeEnabled == true">
        <td>
            <ww:property value="type" />
        </td>
        </ww:if>
        <ww:if test="/schemes/size() > 1">
        <td>
            <ww:property value="/allRelatedSchemes(id)">
                <ww:if test="./size() > 0">
                    <ul>
                    <ww:iterator value="." status="'status'">
                        <li><a href="<ww:url value="'ManageIssueTypeSchemes!default.jspa'" ><ww:param name="'actionedSchemeId'" value="./id" /></ww:url>"><ww:property value="./name" /></a></li>
                    </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    <ww:text name="'admin.issuesettings.issuetypes.no.associated.schemes'"/>
                </ww:else>
            </ww:property>

        </td>
        </ww:if>
        <td>
            <ul class="operations-list">
                <li><a href="Edit<ww:property value="../actionPrefix" />!default.jspa?id=<ww:property value="id"/>"><ww:text name="'common.words.edit'"/></a></li>
            <%-- At least one constant  must exist - check that there is more than one constant --%>
            <ww:if test="../allOptions/size > 1">
                <li><a href="Delete<ww:property value="../actionPrefix" />!default.jspa?id=<ww:property value="id"/>"><ww:text name="'common.words.delete'"/></a></li>
            </ww:if>
            </ul>
        </td>
    </tr>
    </ww:iterator>
    </tbody>
</table>

<fieldset class="hidden parameters">
    <input type="hidden" title="fieldId" value="<ww:property value="fieldId"/>"/>
</fieldset>
<script type="text/javascript">
    function openWindow()
    {
        var vWinUsers = window.open('<%= request.getContextPath() %>/secure/popups/IconPicker.jspa?fieldType=' + AJS.params.fieldId + '&formName=jiraform','IconPicker', 'status=no,resizable=yes,top=100,left=200,width=580,height=650,scrollbars=yes');
        vWinUsers.opener = self;
	    vWinUsers.focus();
    }
</script>

<page:applyDecorator name="jiraform">
    <page:param name="action">Add<ww:property value="actionPrefix" />.jspa</page:param>
    <page:param name="submitId">add_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="title"><ww:text name="'admin.issuesettings.add.new'">
        <ww:param name="'value0'"><ww:property value="titleSingle" /></ww:param>
    </ww:text></page:param>
    <page:param name="instructions"><ww:text name="'admin.issuesettings.issuetypes.add.description'">
        <ww:param name="'value0'"><ww:property value="titleSingle" /></ww:param>
    </ww:text></page:param>

    <ui:textfield label="text('common.words.name')" name="'name'" maxlength="60" >
        <ui:param name="'mandatory'">true</ui:param>
        <ui:param name="'class'">standardField</ui:param>
    </ui:textfield>

    <ui:textfield label="text('common.words.description')" name="'description'">
        <ui:param name="'class'">standardField</ui:param>
    </ui:textfield>

    <%@ include file="/secure/admin/views/issuetypes/typeicon.jsp"%>

</page:applyDecorator>

