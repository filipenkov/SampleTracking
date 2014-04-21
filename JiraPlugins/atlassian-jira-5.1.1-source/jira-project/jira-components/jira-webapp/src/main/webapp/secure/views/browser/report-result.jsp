<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'common.concepts.report'"/> - <ww:property value="report/name" /></title>
    <style>
        .excel {
            float:right;
            text-align: right;
            padding-top: .5em;
            padding-right: .5em;
            padding-bottom: .5em;
        }
    </style>
    <style media="print">
        .excel {
            display: none;
        }
    </style>
</head>
<body class="page-type-report">
    <header>
        <h1><ww:property value="report/label" /></h1>
    </header>
    <div class="content-container">
        <div class="content-body aui-panel">
            <ww:if test="report/module/excelViewSupported == true">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <p class="excel">
                            <a href="<%= request.getContextPath() %>/secure/ConfigureReport!excelView.jspa?<ww:property value="queryString"/>"><ww:text name="'excel.view'"/><img src="<%= request.getContextPath() %>/images/icons/attach/excel.gif" height="16" width="16" border="0" align="absmiddle" alt="<ww:text name="'excel.view'"/>"/></a>
                        </p>
                        <ww:if test="report/description">
                            <p>
                                <b><ww:text name="'common.concepts.description'"/>:</b><br/>
                                <ww:property value="report/description" escape="false" />
                            </p>
                        </ww:if>
                    </aui:param>
                </aui:component>
            </ww:if>
            <ww:property value="generatedReport" escape="false" />
        </div>
    </div>
</body>
</html>
