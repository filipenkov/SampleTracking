
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="view_custom_fields"/>
	<title><ww:text name="'admin.issuefields.customfields.view.custom.fields'"/></title>
</head>

<body>

<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.issuefields.customfields.view.custom.fields'"/></page:param>
    <page:param name="helpURL">customfields</page:param>
    <page:param name="width">100%</page:param>
    <p>
        <ww:text name="'admin.issuefields.customfields.the.table.below'"/>
        <ww:if test="customFields/size > 0">
            <ww:text name="'admin.issuefields.customfields.number.available'">
                <ww:param name="'value0'"><ww:property value="customFields/size" /></ww:param>
            </ww:text>
        </ww:if>
    </p>

    <ww:if test="/customFieldTypesExist == true">
    <ul class="optionslist">
        <li><a id="add_custom_fields" href="<%= request.getContextPath() %>/secure/admin/CreateCustomField!default.jspa"><ww:text name="'admin.issuefields.customfields.add.custom.field'"/></a></li>
    </ul>
    </ww:if>
    <ww:else>
        <p><ww:text name="'admin.issuefields.customfields.no.plugins.configured'"/></p>
    </ww:else>
</page:applyDecorator>

<ww:if test="customFields/size > 0">
<table id="custom-fields" class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th>
                <ww:text name="'common.words.name'"/>
            </th>
            <th>
                <ww:text name="'admin.common.words.type'"/>
            </th>
            <th>
                <ww:text name="'admin.issuefields.available.contexts'"/>
            </th>
            <th>
                <ww:text name="'admin.issuefields.screens'"/>
            </th>
            <th>
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
        <ww:iterator value="customFields" status="'status'">
        <tr <ww:if test="@status/modulus(2) != 1">class="rowAlternate"</ww:if>>
            <td id="custom-fields-<ww:property value="./id" />-name">
            <strong><ww:property value="name"/></strong><br />
            <span class="subText"><ww:property value="description" escape="false"/></span>
            </td>
            <td id="custom-fields-<ww:property value="./id" />-type" class="noWrap">
              <ww:property value='customFieldType/name'/>
            </td>
            <td>
              <jsp:include page="contexts.jsp" flush="true"/>
            </td>
            <td class="noWrap">
                <ww:if test="/fieldScreenTabs(.)/empty == false">
                <ul>
                    <ww:iterator value="/fieldScreenTabs(.)" status="'tabStatus'">
                        <li>
                        <ww:if test="./fieldScreen/tabs/size > 1">
                            <a href="<%= request.getContextPath() %>/secure/admin/ConfigureFieldScreen.jspa?id=<ww:property value="./fieldScreen/id" />&tabPosition=<ww:property value="./position" />"><ww:property value="./fieldScreen/name" /></a> (<ww:property value="./name" />)
                        </ww:if>
                        <ww:else>
                            <a href="<%= request.getContextPath() %>/secure/admin/ConfigureFieldScreen.jspa?id=<ww:property value="./fieldScreen/id" />"><ww:property value="./fieldScreen/name" /></a>
                        </ww:else>
                        </li>
                    </ww:iterator>
                </ul>
                </ww:if>
                <ww:else>&nbsp;</ww:else>
            </td>
            <td>
                <ul class="operations-list">
                  <li><a id="config_<ww:property value="./id" />" href="ConfigureCustomField!default.jspa?customFieldId=<ww:property value="genericValue/long('id')"/>"><ww:text name="'admin.common.words.configure'"/></a></li>
                  <li><a id="edit_<ww:property value="./name" />" href="EditCustomField!default.jspa?id=<ww:property value="genericValue/long('id')"/>"><ww:text name="'common.words.edit'"/></a></li>
                  <li><a id="associate_<ww:property value="./id" />" href="AssociateFieldToScreens!default.jspa?fieldId=<ww:property value="id"/>&returnUrl=ViewCustomFields.jspa"><ww:text name="'admin.issuefields.screens'"/></a></li>
                  <li><a id="del_<ww:property value="./id" />" href="DeleteCustomField!default.jspa?id=<ww:property value="genericValue/long('id')"/>"><ww:text name="'common.words.delete'"/></a></li>
                </ul>
            </td>
        </tr>
        </ww:iterator>
    </tbody>
</table>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.customfields.no.custom.fields.defined'"/></aui:param>
    </aui:component>
</ww:else>
</body>
</html>
