<%@ taglib uri="webwork" prefix="ww"  %>
<%@ taglib uri="webwork" prefix="aui"  %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<html>
<head>
	<title><ww:text name="'admin.loggingandprofiling.logging.and.profiling'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/troubleshooting_and_support"/>
    <meta name="admin.active.tab" content="logging_profiling"/>
</head>

<body>

<%--
   General Logging Section

   Part of JRA-14513
--%>
<%-- error messages --%>
<ww:if test="hasErrorMessages == 'true'">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'titleText'"><ww:text name="'admin.common.words.errors'"/></aui:param>
        <aui:param name="'messageHtml'">
            <ul>
                <ww:iterator value="errorMessages">
                    <li><ww:property /></li>
                </ww:iterator>
            </ul>
        </aui:param>
    </aui:component>
</ww:if>

<%--
  Log Marking And Rollover
--%>
<div class="logging-container">
    <a name="marklogs"></a>
    <h3 class="formtitle"><ww:text name="'admin.loggingandprofiling.marklogs'"/></h3>
    <p><ww:text name="'admin.loggingandprofiling.marklogs.description'"/></p>

    <page:applyDecorator id="login-form" name="auiform">
        <page:param name="action">ViewLogging!markLogs.jspa</page:param>
        <page:param name="method">post</page:param>
        <page:param name="submitButtonName">mark</page:param>
        <page:param name="submitButtonText"><ww:text name="'admin.loggingandprofiling.marklogs.mark'"/></page:param>


        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.loggingandprofiling.marklogs.markmessage.description'"/></page:param>

            <aui:textfield id="'markMessage'" label="text('admin.loggingandprofiling.marklogs.markmessage')" mandatory="false" name="'markMessage'" theme="'aui'" value="/markMessage"/>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.loggingandprofiling.marklogs.rollover.description'"/></page:param>
            <aui:checkbox label="text('admin.loggingandprofiling.marklogs.rollover')" name="'rollOver'" fieldValue="'true'" theme="'aui'">
                <ww:if test="/rollOver == true">
                    <aui:param name="'checked'" value="'checked'"/>
                </ww:if>
            </aui:checkbox>
        </page:applyDecorator>


    </page:applyDecorator>
</div>

<%--
   HTTP Access Log Section
--%>
<div class="logging-container">
    <a name="http"></a>
    <h3 class="formtitle"><ww:text name="'admin.loggingandprofiling.httpaccesslog'"/></h3>
    <p><ww:text name="'admin.loggingandprofiling.httpaccesslog.description'"/></p>

    <ww:if test="/httpAccessLogEnabled == true">
        <p>
        <ww:text name="'admin.loggingandprofiling.httpaccesslog.status'">
            <ww:param name="'value0'"><span class="status-active"><ww:text name="'admin.common.words.on'"/></span></ww:param>
        </ww:text>
        </p>
        <ul class="optionslist">
            <li>
                <ww:text name="'admin.loggingandprofiling.disable.httpaccesslog'">
                    <ww:param name="'value0'"><a id="disable_http_access" href="ViewLogging!disableHttpAccessLog.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
            </li>
        </ul>
        <div style="padding-left:2em">

        <ww:if test="/httpAccessLogIncludeImagesEnabled == true">
            <p>
            <ww:text name="'admin.loggingandprofiling.httpaccesslog.includeimages.status'">
                <ww:param name="'value0'"><span class="status-active"><ww:text name="'admin.common.words.on'"/></span></ww:param>
            </ww:text>
            </p>
            <ul class="optionslist">
                <li>
                <ww:text name="'admin.loggingandprofiling.disable.httpaccesslog.includeimages'">
                    <ww:param name="'value0'"><a id="disable_http_access_includeimages" href="ViewLogging!disableHttpAccessLogIncludeImages.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
                </li>
            </ul>
        </ww:if>
        <ww:else>
            <p>
            <ww:text name="'admin.loggingandprofiling.httpaccesslog.includeimages.status'">
                <ww:param name="'value0'"><span class="status-inactive"><ww:text name="'admin.common.words.off'"/></span></ww:param>
            </ww:text>
            </p>
            <ul class="optionslist">
                <li>
                <ww:text name="'admin.loggingandprofiling.enable.httpaccesslog.includeimages'">
                    <ww:param name="'value0'"><a id="enable_http_access_includeimages" href="ViewLogging!enableHttpAccessLogIncludeImages.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
                </li>
            </ul>
        </ww:else>


        <ww:if test="/httpDumpLogEnabled == true">
            <p>
            <ww:text name="'admin.loggingandprofiling.httpdumplog.status'">
                <ww:param name="'value0'"><span class="status-active"><ww:text name="'admin.common.words.on'"/></span></ww:param>
            </ww:text>
            </p>
            <ul class="optionslist">
                <li>
                <ww:text name="'admin.loggingandprofiling.disable.httpdumplog'">
                    <ww:param name="'value0'"><a id="disable_http_dump" href="ViewLogging!disableHttpDumpLog.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
                </li>
            </ul>
        </ww:if>
        <ww:else>
            <p>
            <ww:text name="'admin.loggingandprofiling.httpdumplog.status'">
                <ww:param name="'value0'"><span class="status-inactive"><ww:text name="'admin.common.words.off'"/></span></ww:param>
            </ww:text>
            </p>
            <ul class="optionslist">
                <li>
                <ww:text name="'admin.loggingandprofiling.enable.httpdumplog'">
                    <ww:param name="'value0'"><a id="enable_http_dump" href="ViewLogging!enableHttpDumpLog.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
                </li>
            </ul>
        </ww:else>
        </div>

    </ww:if>
    <ww:else>
        <p>
        <ww:text name="'admin.loggingandprofiling.httpaccesslog.status'">
            <ww:param name="'value0'"><span class="status-inactive"><ww:text name="'admin.common.words.off'"/></span></ww:param>
        </ww:text>
        </p>
        <ul class="optionslist">
            <li>
                <ww:text name="'admin.loggingandprofiling.enable.httpaccesslog'">
                    <ww:param name="'value0'"><a id="enable_http_access" href="ViewLogging!enableHttpAccessLog.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
            </li>
        </ul>
    </ww:else>
</div>

<%--
   SOAP Access Log Section
--%>
<div class="logging-container">
    <a name="soap"></a>
    <h3 class="formtitle"><ww:text name="'admin.loggingandprofiling.soapaccesslog'"/></h3>
    <p><ww:text name="'admin.loggingandprofiling.soapaccesslog.description'"/></p>
    <ww:if test="/soapAccessLogEnabled == true">
        <p>
        <ww:text name="'admin.loggingandprofiling.soapaccesslog.status'">
            <ww:param name="'value0'"><span class="status-active"><ww:text name="'admin.common.words.on'"/></span></ww:param>
        </ww:text>
        </p>
        <ul class="optionslist">
            <li>
                <ww:text name="'admin.loggingandprofiling.disable.soapaccesslog'">
                    <ww:param name="'value0'"><a id="disable_soap_access" href="ViewLogging!disableSoapAccessLog.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
            </li>
        </ul>
        <div style="padding-left:2em">
        <ww:if test="/soapDumpLogEnabled == true">
            <p>
            <ww:text name="'admin.loggingandprofiling.soapdumplog.status'">
                <ww:param name="'value0'"><span class="status-active"><ww:text name="'admin.common.words.on'"/></span></ww:param>
            </ww:text>
            </p>
            <ul class="optionslist">
                <li>
                    <ww:text name="'admin.loggingandprofiling.disable.soapdumplog'">
                        <ww:param name="'value0'"><a id="disable_soap_dump" href="ViewLogging!disableSoapDumpLog.jspa"><b></ww:param>
                        <ww:param name="'value1'"></b></a></ww:param>
                    </ww:text>
                </li>
            </ul>
        </ww:if>
        <ww:else>
            <p>
            <ww:text name="'admin.loggingandprofiling.soapdumplog.status'">
                <ww:param name="'value0'"><span class="status-inactive"><ww:text name="'admin.common.words.off'"/></span></ww:param>
            </ww:text>
            </p>
            <ul class="optionslist">
                <li>
                    <ww:text name="'admin.loggingandprofiling.enable.soapdumplog'">
                       <ww:param name="'value0'"><a id="enable_soap_dump" href="ViewLogging!enableSoapDumpLog.jspa"><b></ww:param>
                       <ww:param name="'value1'"></b></a></ww:param>
                    </ww:text>
                </li>
            </ul>
        </ww:else>
        </div>
    </ww:if>
    <ww:else>
        <p>
        <ww:text name="'admin.loggingandprofiling.soapaccesslog.status'">
            <ww:param name="'value0'"><span class="status-inactive"><ww:text name="'admin.common.words.off'"/></span></ww:param>
        </ww:text>
        </p>
        <ul class="optionslist">
            <li>
                <ww:text name="'admin.loggingandprofiling.enable.soapaccesslog'">
                    <ww:param name="'value0'"><a id="enable_soap_access" href="ViewLogging!enableSoapAccessLog.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
            </li>
        </ul>
    </ww:else>
</div>

<%--
   SQL Log Section
--%>
<div class="logging-container">
    <a name="sql"></a>
    <h3 class="formtitle"><ww:text name="'admin.loggingandprofiling.sqllog'"/></h3>
    <p><ww:text name="'admin.loggingandprofiling.sqllog.description'"/></p>
    <p><ww:text name="'admin.loggingandprofiling.sqllog.warning'"/></p>
    <ww:if test="/sqlLogEnabled == true">
        <p>
        <ww:text name="'admin.loggingandprofiling.sqllog.status'">
            <ww:param name="'value0'"><span class="status-active"><ww:text name="'admin.common.words.on'"/></span></ww:param>
        </ww:text>
        </p>
        <ul class="optionslist">
            <li>
                <ww:text name="'admin.loggingandprofiling.disable.sqllog'">
                    <ww:param name="'value0'"><a id="disable_sql_log" href="ViewLogging!disableSqlLog.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
            </li>
        </ul>
        <div style="padding-left:2em">
        <ww:if test="/sqlDumpLogEnabled == true">
            <p>
            <ww:text name="'admin.loggingandprofiling.sqldumplog.status'">
                <ww:param name="'value0'"><span class="status-active"><ww:text name="'admin.common.words.on'"/></span></ww:param>
            </ww:text>
            </p>
            <ul class="optionslist">
                <li>
                    <ww:text name="'admin.loggingandprofiling.disable.sqldumplog'">
                        <ww:param name="'value0'"><a id="disable_sql_dump" href="ViewLogging!disableSqlDumpLog.jspa"><b></ww:param>
                        <ww:param name="'value1'"></b></a></ww:param>
                    </ww:text>
                </li>
            </ul>
        </ww:if>
        <ww:else>
            <p>
            <ww:text name="'admin.loggingandprofiling.sqldumplog.status'">
                <ww:param name="'value0'"><span class="status-inactive"><ww:text name="'admin.common.words.off'"/></span></ww:param>
            </ww:text>
            </p>
            <ul class="optionslist">
                <li>
                    <ww:text name="'admin.loggingandprofiling.enable.sqldumplog'">
                       <ww:param name="'value0'"><a id="enable_sql_dump" href="ViewLogging!enableSqlDumpLog.jspa"><b></ww:param>
                       <ww:param name="'value1'"></b></a></ww:param>
                    </ww:text>
                </li>
            </ul>
        </ww:else>
        </div>
    </ww:if>
    <ww:else>
        <p>
        <ww:text name="'admin.loggingandprofiling.sqllog.status'">
            <ww:param name="'value0'"><span class="status-inactive"><ww:text name="'admin.common.words.off'"/></span></ww:param>
        </ww:text>
        </p>
        <ul class="optionslist">
            <li>
                <ww:text name="'admin.loggingandprofiling.enable.sqllog'">
                    <ww:param name="'value0'"><a id="enable_sql_access" href="ViewLogging!enableSqlLog.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
            </li>
        </ul>
    </ww:else>
</div>

<%--
Profiling Access Log Section
--%>
<div class="logging-container">
    <a name="profiling"></a>
    <h3 class="formtitle"><ww:text name="'admin.loggingandprofiling.profiling'"/></h3>
    <p><ww:text name="'admin.loggingandprofiling.profiling.description'"/></p>
    <ww:if test="/profilingEnabled == true">
        <p>
        <ww:text name="'admin.loggingandprofiling.profiling.status'">
            <ww:param name="'value0'"><span class="status-active"><ww:text name="'admin.common.words.on'"/></span></ww:param>
        </ww:text>
        </p>
        <ul class="optionslist">
            <li>
                <ww:text name="'admin.loggingandprofiling.disable.profiling'">
                    <ww:param name="'value0'"><a id="disable_profiling" href="ViewLogging!disableProfiling.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
            </li>
        </ul>
    </ww:if>
    <ww:else>
        <p>
        <ww:text name="'admin.loggingandprofiling.profiling.status'">
            <ww:param name="'value0'"><span class="status-inactive"><ww:text name="'admin.common.words.off'"/></span></ww:param>
        </ww:text>
        </p>
        <ul class="optionslist">
            <li>
                <ww:text name="'admin.loggingandprofiling.enable.profiling'">
                    <ww:param name="'value0'"><a id="enable_profiling" href="ViewLogging!enableProfiling.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
            </li>
        </ul>
    </ww:else>
</div>

<div class="logging-container">
    <h3 class="formtitle"><ww:text name="'admin.loggingandprofiling.logging'"/></h3>
    <%--<p><ww:text name="'admin.loggingandprofiling.description'"/></p>--%>
    <p>
    <ww:text name="'admin.loggingandprofiling.note'">
        <ww:param name="'value0'"><i></ww:param>
        <ww:param name="'value1'"></i></ww:param>
        <ww:param name="'value2'"><br></ww:param>
    </ww:text>
    </p>
    <p><ww:text name="'admin.loggingandprofiling.logging.will.go.to.the.console'"/></p>


    <table class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th><ww:text name="'admin.loggingandprofiling.package.name'"/></th>
                <th><ww:text name="'admin.loggingandprofiling.logging.level'"/></th>
                <th><ww:text name="'admin.loggingandprofiling.set.logging.level'"/></th>
            </tr>
        </thead>
        <tbody>
        <ww:property value="/rootLogger" >
            <tr>
                <td><i><ww:text name="'admin.common.words.default'"/></i></td>
                <td><strong><ww:property value="./level" /></strong></td>
                <td><jsp:include page="viewlogginglevels.jsp"/></td>
            </tr>
        </ww:property>
        <ww:iterator value="/loggers" status="'iteratorStatus'">
            <tr>
                <td><ww:property value="name" /></td>
                <td><strong><ww:property value="./level" /></strong></td>
                <td><jsp:include page="viewlogginglevels.jsp"/></td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
</div>
</body>
</html>

