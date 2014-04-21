<%@ page import="com.atlassian.jira.util.BrowserUtils,
                 com.atlassian.jira.web.action.issue.bulkedit.BulkDelete,
                 com.atlassian.jira.web.component.IssueTableWebComponent"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
</head>
<body>
    <!-- Step 4 - Bulk Operation: Confirmation for DELETE -->
    <div class="content-container" id="stepped-process">
        <div class="content-related">
            <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator name="jirapanel">
                <page:param name="title"><ww:text name="'bulkedit.title'"/> <ww:text name="'bulkedit.step4'"/>: <ww:text name="'bulkedit.step4.title'"/></page:param>
                <page:param name="width">100%</page:param>
                <table border="0" cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td colspan="2">
                            <ww:text name="'bulk.delete.confirmation.line1'">
                                <ww:param name="'value0'"><b><ww:property value="/bulkEditBean/selectedIssuesIncludingSubTasks/size"/></b></ww:param>
                            </ww:text>
                            <br>
                            <ww:text name="'bulk.delete.confirmation.line2'"/>
                            <br>
                            <ww:text name="'bulk.delete.confirmation.line3'"/>
                        </td>
                    </tr>
                </table>
            </page:applyDecorator>

            <!-- Send Mail confirmation -->
            <ww:if test="/canDisableMailNotifications() == true && /bulkEditBean/hasMailServer == true">
                <jsp:include page="/includes/bulkedit/bulkedit-sendnotifications-confirmation.jsp"/>
            </ww:if>

            <form class="aui" name="bulkdelete" method="POST" action="<%= request.getContextPath() %>/secure/BulkDeletePerform.jspa">
                <ww:component name="'atl_token'" value="/xsrfToken" template="hidden.jsp"/>

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

                <%-- Set this so that it can be used further down --%>
                <ww:property value="/" id="bulkEdit" />
                <%
                    BulkDelete bulkEdit = (BulkDelete) request.getAttribute("bulkEdit");
                    List issues = bulkEdit.getBulkEditBean().getSelectedIssuesIncludingSubTasks();
                %>
                <%= new IssueTableWebComponent().getHtml(bulkEdit.getIssueTableLayoutBean(), issues, null) %>

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
