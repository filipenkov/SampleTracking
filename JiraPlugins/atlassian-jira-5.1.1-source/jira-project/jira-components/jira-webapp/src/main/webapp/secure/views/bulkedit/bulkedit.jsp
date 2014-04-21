<%@ page import="com.atlassian.jira.ComponentManager,
                 com.atlassian.jira.util.BrowserUtils,
                 com.atlassian.jira.web.action.issue.bulkedit.BulkEdit1"%>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.jira.web.component.IssueTableWebComponent" %>
<%@ page import="java.util.List" %>
<%
    final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
    fieldResourceIncluder.includeFieldResourcesForCurrentUser();
%>
<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
    <script language="javascript">
        function setCheckboxes()
        {
            var value = document.bulkedit.all.checked;
            var numelements = document.bulkedit.elements.length;
            var item;
            for (var i=0 ; i<numelements ; i++)
            {
                item = document.bulkedit.elements[i];
                item.checked = value;
            }
        }
    </script>
</head>
<body>
    <!-- STEP 1 - Bulk Operation: Choose Issues-->
    <div class="content-container" id="stepped-process">
        <div class="content-related">
            <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator name="jirapanel">
                <page:param name="title"><ww:text name="'bulkedit.title'"/> <ww:text name="'bulkedit.step1'"/>: <ww:text name="'bulkedit.step1.title'"/></page:param>
                <ww:if test="/bulkLimited == true">
                    <ww:text name="'bulk.edit.limited'"><ww:param name="'value0'" value="/tempMax"/></ww:text>
                </ww:if>
            </page:applyDecorator>

            <form class="aui" name="bulkedit" method="POST" action="BulkEdit1.jspa">
                <input type="hidden" name="tempMax" value="<ww:property value="/tempMax"/>"/>

                <div class="buttons-container aui-toolbar form-buttons noprint">
                    <div class="toolbar-group">
                        <span class="toolbar-item">
                        <input class="toolbar-trigger" type="submit" name="<ww:text name="'common.forms.next'"/>" id="<ww:text name="'common.forms.next'"/>" value="<ww:text name="'common.forms.next'"/> >>"
                           title="<ww:text name="'common.forms.submit.tooltip'">
                           <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
                           <ww:param name="'value1'"><%=BrowserUtils.getModifierKey()%></ww:param>
                           </ww:text>"
                        />
                        </span>
                    </div>
                    <div class="toolbar-group">
                        <span class="toolbar-item toolbar-item-link">
                            <input class="toolbar-trigger" type="button" id="<ww:text name="'common.forms.cancel'"/>" name="<ww:text name="'common.forms.cancel'"/>" value="<ww:text name="'common.forms.cancel'"/>" onclick="location.href='BulkCancelWizard.jspa'">
                        </span>
                    </div>
                </div>

                <%-- Set this so that it can be used further down --%>
                <ww:property value="/" id="bulkEdit" />
                <%
                    BulkEdit1 bulkEdit1 = (BulkEdit1) request.getAttribute("bulkEdit");
                    List issues = bulkEdit1.getBulkEditBean().getIssuesFromSearchRequest();
                %>
                <%= new IssueTableWebComponent().getHtml(bulkEdit1.getIssueTableLayoutBean(), issues, null) %>

                <div class="buttons-container aui-toolbar form-buttons noprint">
                    <div class="toolbar-group">
                        <span class="toolbar-item">
                        <input class="toolbar-trigger" type="submit" name="<ww:text name="'common.forms.next'"/>" id="<ww:text name="'common.forms.next'"/>" value="<ww:text name="'common.forms.next'"/> >>"
                           title="<ww:text name="'common.forms.submit.tooltip'">
                           <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
                           <ww:param name="'value1'"><%=BrowserUtils.getModifierKey()%></ww:param>
                           </ww:text>"
                        />
                        </span>
                    </div>
                    <div class="toolbar-group">
                        <span class="toolbar-item toolbar-item-link">
                            <input class="toolbar-trigger" type="button" id="<ww:text name="'common.forms.cancel'"/>" name="<ww:text name="'common.forms.cancel'"/>" value="<ww:text name="'common.forms.cancel'"/>" onclick="location.href='BulkCancelWizard.jspa'">
                        </span>
                    </div>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
