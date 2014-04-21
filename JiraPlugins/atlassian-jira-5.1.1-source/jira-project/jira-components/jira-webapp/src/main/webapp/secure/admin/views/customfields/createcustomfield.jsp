<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="view_custom_fields"/>
	<title><ww:text name="'admin.issuefields.customfields.create.custom.field'"/></title>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.issuefields.customfields.create.custom.field'"/></page:param>
    <page:param name="instructions">
        <p>
            <ww:text name="'admin.issuefields.customfields.choose.the.field.type'">
                <ww:param name="'value0'"><ww:property value="fieldTypes/size" /></ww:param>
            </ww:text>
        </p>
        <p>
            <ww:text name="'admin.issuefields.customfields.extentions'">
                  <ww:param name="'value0'"><strong><a href="<ww:component name="'external.link.jira.extensions'" template="externallink.jsp" />"></ww:param>
                  <ww:param name="'value1'"></a></strong></ww:param>
            </ww:text>
        </p>
    </page:param>
    <page:param name="width">100%</page:param>
    <page:param name="action">CreateCustomField.jspa</page:param>
    <page:param name="cancelURI">ViewCustomFields.jspa</page:param>
    <page:param name="helpURL">addingcustomfields</page:param>
    <page:param name="wizard">true</page:param>
    <tr>
        <td>
        <ww:if test="fieldTypes/size > 0">
        <table class="aui custom-field-types">
            <ww:iterator value="fieldTypes" status="'status'">
                <ww:if test="@status/odd == true">
                    <tr>
                </ww:if>
                    <td id="cell<ww:property value="key"/>" onclick="selectCellRadioBox(this.id)">
                        <div class="field-group">
                            <input type="radio" name="fieldType" value="<ww:property value="key"/>" id="<ww:property value="key"/>_id">
                            <label for="<ww:property value="key"/>_id"><ww:property value="name"/></label>
                            <div class="description"><ww:property value="description"/></div>
                        </div>
                    </td>
                <ww:if test="@status/even == true">
                    </tr>
                </ww:if>
            </ww:iterator>
        </table>
        </ww:if>
        <ww:else>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.customfields.you.have.no.custom.field.types.available'"/></aui:param>
            </aui:component>
        </ww:else>
        </td>
    </tr>
    <ui:component template="multihidden.jsp" >
        <ui:param name="'fields'">fieldName,description,searcher,global</ui:param>
        <ui:param name="'multifields'">projects,issuetypes</ui:param>
    </ui:component>
</page:applyDecorator>


<script language="javascript" type="text/javascript">

    <ww:if test="/fieldTypeValid == 'true'">
        <!--
        selectCellRadioBox('cell<ww:property value="/fieldType" />');
        //-->
    </ww:if>

    var selected;
    function selectCellRadioBox(cellId)
    {
        var id = cellId.substring(4, cellId.length);
        document.forms['jiraform'].elements[id + '_id'].checked = true;
    }

</script>
</body>
</html>
