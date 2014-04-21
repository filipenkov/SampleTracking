<%@ page import="com.atlassian.jira.util.BrowserUtils,
                 com.atlassian.jira.web.action.issue.bulkedit.BulkEdit,
                 com.atlassian.jira.web.component.IssueTableWebComponent"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
</head>
<body>
    <!-- Step 4 - Bulk Operation: Confirmation for EDIT -->
    <div class="content-container" id="stepped-process">
        <div class="content-related">
            <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator name="jirapanel">
                <page:param name="title"><ww:text name="'bulkedit.title'"/> <ww:text name="'bulkedit.step4'"/>: <ww:text name="'bulkedit.step4.title'"/></page:param>
                <page:param name="description">
                    <ww:text name="'bulkedit.step3.desc'"/>
                </page:param>
                <page:param name="width">100%</page:param>
            </page:applyDecorator>
            <!-- Send Mail confirmation -->
            <ww:if test="/canDisableMailNotifications() == true && /bulkEditBean/hasMailServer == true">
                <jsp:include page="/includes/bulkedit/bulkedit-sendnotifications-confirmation.jsp"/>
            </ww:if>
            <form class="aui" name="bulkedit_confirmation" action="BulkEditPerform.jspa" method="POST">
                <h5><ww:text name="'bulkedit.confirm.updatedfields'"/></h5>
                <table class="aui aui-table-rowhover" id="updatedfields" width="70%">
                    <ww:iterator value="/bulkEditBean/actions/values">
                        <tr>
                            <td width="20%"><ww:property value="./fieldName"/></td>
                            <td><ww:property value="/fieldViewHtml(./field)" escape="false" /></td>
                        </tr>
                    </ww:iterator>
                </table>
                <p><ww:text name="'bulkedit.confirm.warning.about.blanks'"/></p>
                <p>
                    <ww:text name="'bulkedit.confirm.msg'">
                        <ww:param name="'value0'"><strong><ww:property value="/bulkEditBean/selectedIssues/size"/></strong></ww:param>
                    </ww:text>
                </p>
                <div class="buttons-container aui-toolbar form-buttons noprint">
                    <div class="toolbar-group">
                        <span class="toolbar-item">
                        <input class="toolbar-trigger" type="submit" name="<ww:text name="'common.forms.confirm'"/>" value="<ww:text name="'common.forms.confirm'"/>"
                           accessKey="<ww:text name="'common.forms.submit.accesskey'"/>"
                           title="<ww:text name="'common.forms.submit.tooltip'">
                           <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
                           <ww:param name="'value1'"><%=BrowserUtils.getModifierKey()%></ww:param>
                           </ww:text>"
                        />
                        </span>
                    </div>
                    <div class="toolbar-group">
                        <span class="toolbar-item toolbar-item-link">
                            <input class="toolbar-trigger" type="button"  id="<ww:text name="'common.forms.cancel'"/>" name="<ww:text name="'common.forms.cancel'"/>" value="<ww:text name="'common.forms.cancel'"/>" onclick="location.href='BulkCancelWizard.jspa'"/>
                        </span>
                    </div>
                </div>
                <div id="updatedIssueTable">
                    <%-- Set this so that it can be used further down --%>
                    <ww:property value="/" id="bulkEdit" />
                    <%
                        BulkEdit bulkEdit = (BulkEdit) request.getAttribute("bulkEdit");
                        List issues = bulkEdit.getBulkEditBean().getSelectedIssues();
                    %>
                    <%= new IssueTableWebComponent().getHtml(bulkEdit.getIssueTableLayoutBean(), issues, null) %>
                </div>
                <div class="buttons-container aui-toolbar form-buttons noprint">
                    <div class="toolbar-group">
                        <span class="toolbar-item">
                        <input class="toolbar-trigger" type="submit" name="<ww:text name="'common.forms.confirm'"/>" value="<ww:text name="'common.forms.confirm'"/>"
                           accessKey="<ww:text name="'common.forms.submit.accesskey'"/>"
                           title="<ww:text name="'common.forms.submit.tooltip'">
                           <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
                           <ww:param name="'value1'"><%=BrowserUtils.getModifierKey()%></ww:param>
                           </ww:text>"
                        />
                        </span>
                    </div>
                    <div class="toolbar-group">
                        <span class="toolbar-item toolbar-item-link">
                            <input class="toolbar-trigger" type="button"  id="<ww:text name="'common.forms.cancel'"/>" name="<ww:text name="'common.forms.cancel'"/>" value="<ww:text name="'common.forms.cancel'"/>" onclick="location.href='BulkCancelWizard.jspa'"/>
                        </span>
                    </div>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
