<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'common.concepts.report'"/> - <ww:property value="report/name" /></title>
	<meta name="decorator" content="notitle">
    <style type="text/css">
    .jiraformbody
    {
        padding: 0 !important;
    }
    .excel
    {
        float:right;
        text-align: right;
        padding-top: .5em;
        padding-right: .5em;
        padding-bottom: .5em;
    }
    </style>
    <style type="text/css" media="print">
    .excel
    {
        display: none;
    }
    </style>
</head>
<body>
<page:applyDecorator name="jirapanel">
	<page:param name="title"><ww:text name="'common.concepts.report'"/>: <a href="<ww:url page="ConfigureReport!default.jspa"><ww:param name="'reportKey'" value="report/completeKey" /></ww:url>"><ww:property value="report/label" /></a></page:param>
	<page:param name="instructions">
        <ww:if test="report/module/excelViewSupported == true">
        <div class="excel">
            <a href="<%= request.getContextPath() %>/secure/ConfigureReport!excelView.jspa?<ww:property value="queryString"/>"><ww:text name="'excel.view'"/><img src="<%= request.getContextPath() %>/images/icons/attach/excel.gif" height="16" width="16" border="0" align="absmiddle" alt="<ww:text name="'excel.view'"/>"/></a>
        </div>
        <ww:if test="report/description"><b><ww:text name="'common.concepts.description'"/>:</b><br/><ww:property value="report/description" escape="false" /></ww:if>
        </ww:if>
    </page:param>
    <page:param name="width">100%</page:param>

    <ww:property value="generatedReport" escape="false" />
</page:applyDecorator>
</body>
</html>
