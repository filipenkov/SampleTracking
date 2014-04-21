<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
    <script language="javascript">
        function selectCellRadioBox(cell)
        {
            var id = cell.id.substring(4, cell.id.length);
            document.forms['bulkedit_chooseoperation'].elements[id + '_id'].checked = true;
        }
    </script>
</head>
<body>
    <!-- Step 2 - Bulk Operation: Choose Operation -->
    <div class="content-container" id="stepped-process">
        <div class="content-related">
            <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator name="jirapanel">
                <page:param name="title"><ww:text name="'bulkedit.title'"/> <ww:text name="'bulkedit.step2'"/>: <ww:text name="'bulkedit.chooseoperation.title'"/></page:param>
                <page:param name="description">
                    <ww:if test="/hasAvailableOperations == true">
                        <ww:text name="'bulkedit.chooseoperation.desc'">
                            <ww:param name="'value0'"><strong><ww:property value="/bulkEditBean/selectedIssues/size"/></strong></ww:param>
                        </ww:text>
                    </ww:if>
                    <ww:else>
                           <ww:text name="'bulkedit.chooseoperation.unavailable'">
                                <ww:param name="'value0'"><strong><ww:property value="/bulkEditBean/selectedIssues/size"/></strong></ww:param>
                           </ww:text>
                    </ww:else>
                </page:param>
                <page:param name="width">100%</page:param>
            </page:applyDecorator>

            <form class="aui" name="bulkedit_chooseoperation" action="BulkChooseOperation.jspa" method="POST">
                <table class="aui aui-table-rowhover">
                <ww:iterator value="bulkOperations" status="'status'">
                    <tr onclick="selectCellRadioBox(this)" id="cell<ww:property value="./nameKey"/>">
                        <%-- if the operation is available draw the radio button --%>
                        <ww:if test="/canPerform(.) == true">
                            <td width="2%">
                                <input type="radio" name="operation" id="<ww:property value="./nameKey"/>_id" value="<ww:property value="./nameKey"/>">
                            </td>
                            <td>
                                <label for="<ww:property value="./nameKey"/>_id"><ww:text name="./nameKey"/></label>
                            </td>
                            <td>
                                <label for="<ww:property value="./nameKey"/>_id"><ww:text name="./descriptionKey"/></label>
                            </td>
                        </ww:if>
                        <ww:else>
                            <td width="2%">
                                <ww:text name="'bulkedit.constants.na'"/>
                            </td>
                            <td>
                                <ww:text name="./nameKey"/>
                            </td>
                            <td>
                                <ww:text name="./cannotPerformMessageKey">
                                    <ww:param name="'value0'"><strong></ww:param>
                                    <ww:param name="'value1'"></strong></ww:param>
                                    <ww:param name="'value2'"><ww:property value="/bulkEditBean/selectedIssues/size"/></ww:param>
                                </ww:text>
                            </td>
                        </ww:else>
                    </tr>
                </ww:iterator>
                </table>
                <%@include file="bulkchooseoperation_submit_buttons.jsp"%>
            </form>
        </div>
    </div>
</body>
</html>
