<%@ page import="com.atlassian.jira.web.action.issue.IssueNavigator,
                 java.util.List,
                 com.atlassian.jira.web.component.IssuePager,
                 com.atlassian.jira.web.component.IssueTableLayoutBean,
                 com.atlassian.jira.web.component.IssueTableWebComponent,
                 com.atlassian.jira.web.action.user.ViewUserIssueColumns,
                 com.atlassian.jira.web.component.AllIssuesIssuePager,
                 com.atlassian.jira.web.action.AbstractViewIssueColumns"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>



<ww:if test="sampleIssues/empty == false">
    <%-- if standard add form --%>
    <ww:if test="/filterId">
        <page:applyDecorator id="addcolumnform" name="auiform">
             <%--<%-*****-<form name="addcolumnform" method="post" action="<ww:property value="actionName"/>.jspa">-******-%> --%>
            <page:param name="action"><%= request.getContextPath()%>/secure/views/enterprise/<ww:property value="actionName"/>.jspa</page:param>
            <page:param name="useCustomButtons">true</page:param>
            <page:param name="helpURL">issuecolumn_hiding_and_reordering</page:param>

            <aui:select id="'column-select'" label="text('issue.columns.add.new.columns')" list="addableColumns" listKey="'id'" listValue="'/text(nameKey)'" name="'fieldId'" theme="'aui'">
                <aui:param name="'defaultOptionText'"><ww:text name="'issue.columns.select.column'"/></aui:param>
            </aui:select>
            <aui:component name="'operation'" value="'1'" template="hidden.jsp" theme="'aui'" />
            <aui:component name="'filterId'" value="/filterId" template="hidden.jsp" theme="'aui'" />

            <aui:component template="formSubmit.jsp" theme="'aui'">
                <aui:param name="'submitButtonName'">add</aui:param>
                <aui:param name="'submitButtonText'"><ww:text name="'issue.columns.add.button'" /></aui:param>
            </aui:component>
            <ww:if test="/usingDefaultColumns == false">
                <a href="<ww:property value="actionName"/>.jspa?operation=5&filterId=<ww:property value="/filterId"/>"><ww:text name="'issue.columns.remove.layout'"/></a>
            </ww:if>
        </page:applyDecorator>
    </ww:if>
    <ww:else>
        <page:applyDecorator id="issue-nav-add-columns" name="auiform">
            <page:param name="action"><ww:property value="actionName"/>.jspa</page:param>
            <page:param name="useCustomButtons">true</page:param>

            <aui:select id="'column-select'" label="text('issue.columns.add.new.columns')" list="addableColumns" listKey="'id'" listValue="'/text(nameKey)'" name="'fieldId'" theme="'aui'">
                <aui:param name="'defaultOptionText'"><ww:text name="'issue.columns.select.column'"/></aui:param>
            </aui:select>
            <aui:component name="'operation'" value="'1'" template="hidden.jsp" theme="'aui'" />

            <aui:component template="formSubmit.jsp" theme="'aui'">
                <aui:param name="'submitButtonName'">add</aui:param>
                <aui:param name="'submitButtonText'"><ww:text name="'issue.columns.add.button'" /></aui:param>
            </aui:component>
            <ww:if test="/usingDefaultColumns == false">
                <a href="<ww:property value="actionName"/>.jspa?operation=5" title="<ww:text name="'issue.columns.defaults.are'"/> <ww:iterator value="/defaultColumns" status="'columnStatus'"><ww:property value="./navigableField"><ww:text name="./nameKey"/><ww:if test="@columnStatus/last == false"> |</ww:if> </ww:property></ww:iterator>"><ww:text name="'issue.columns.restore.defaults'"/></a>
            </ww:if>
        </page:applyDecorator>
    </ww:else>
</ww:if>

<div class="reorder-columns">
    <h3><ww:text name="'issue.columns.reorder.columns'"/></h3>
    
    <ww:property value="/actionsAndOperationsShowing" id="showActionsColumn"/>
    <ww:text name="'issue.columns.reorder.instructions.second.line'">
        <ww:param name="'value0'"><img src="<%= request.getContextPath()%>/images/icons/prev.gif" height=16 width=16 border=0></ww:param>
        <ww:param name="'value1'"><img src="<%= request.getContextPath()%>/images/icons/next.gif" height=16 width=16 border=0></ww:param>
        <ww:param name="'value2'"><img src="<%= request.getContextPath()%>/images/icons/trash_16.gif" height=16 width=16 border=0></ww:param>
    </ww:text>
</div>



<ww:if test="/sampleIssues && /sampleIssues/size > 0">
    <%-- Set this so that it can be used further down --%>
    <ww:property value="/" id="issueColumns" />

    <%
        Long filterId = (Long) request.getAttribute("filterId");
        String actionUrl = (String) request.getAttribute("actionUrl");
        Boolean showActionsColumn = (Boolean) request.getAttribute("showActionsColumn");

        AbstractViewIssueColumns issueColumns = (AbstractViewIssueColumns) request.getAttribute("issueColumns");

        List currentPage = issueColumns.getSampleIssues();
        List columns = issueColumns.getColumns();
        IssueTableLayoutBean layoutBean = new IssueTableLayoutBean(columns);
        layoutBean.setSortingEnabled(false);
        layoutBean.setShowHeaderDescription(true);
        layoutBean.addCellDisplayParam(IssueTableLayoutBean.CELL_NO_LINK, Boolean.TRUE);
        layoutBean.setAlternateRowColors(false);
        layoutBean.setShowTableEditHeader(true);
        layoutBean.setFilterId(filterId);
        layoutBean.setActionUrl(actionUrl);
        layoutBean.setShowActionColumn(showActionsColumn != null && showActionsColumn.booleanValue());

    %>
    <%= new IssueTableWebComponent().getHtml(layoutBean, currentPage, null) %>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'issue.columns.no.issues'"/></p>
        </aui:param>
    </aui:component>
</ww:else>