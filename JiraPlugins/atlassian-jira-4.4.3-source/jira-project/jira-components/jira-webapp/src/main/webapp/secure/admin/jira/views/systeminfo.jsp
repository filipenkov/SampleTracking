<%@ page import="com.atlassian.jira.bc.JiraServiceContext" %>
<%@ page import="com.atlassian.jira.bc.JiraServiceContextImpl" %>
<%@ page import="com.atlassian.jira.util.system.ExtendedSystemInfoUtils" %>
<%@ page import="webwork.action.CoreActionContext" %>
<%@ page import="java.util.Set" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<ww:bean id="math" name="'com.atlassian.core.bean.MathBean'"/>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/troubleshooting_and_support"/>
    <meta name="admin.active.tab" content="system_info"/>
	<title><ww:text name="'admin.systeminfo.system.info'"/></title>
</head>

<body>

<ww:if test="/warningMessages/size() > 0">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'id'">environment_warnings</aui:param>
        <aui:param name="'titleText'"><ww:text name="'admin.systeminfo.environment.warnings'"/></aui:param>
        <aui:param name="'messageHtml'">
            <ul>
            <ww:iterator value="/warningMessages" status="'status'">
                <li><ww:property value="." escape="false" /></li>
            </ww:iterator>
            </ul>
        </aui:param>
    </aui:component>
</ww:if>

<%--
System info
--%>
<ww:component template="help.jsp" name="'system_information_help'" />
<h3 class="formtitle"><ww:text name="'admin.systeminfo.system.info'"/></h3>
<table class="aui aui-table-rowhover" id="system_info_table">
    <tbody>
        <tr>
            <td width="40%"><strong><ww:text name="'admin.generalconfiguration.base.url'"/></strong></td>
            <td width="60%"><ww:property value="/extendedSystemInfoUtils/baseUrl"/></td>
        </tr>
    <ww:iterator value="/extendedSystemInfoUtils/props(true)" status="'status'">
        <tr>
            <td><b><ww:property value="key" /></b></td>
            <td><ww:property value="value" /></td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<ww:if test="appliedPatches/size() > 0">
<%--
Patches: This is never shown unless we have an actual set of patches, which most of the time is not the case.
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.applied.patches'"/></h3>
<table class="aui aui-table-rowhover" id="applied_patches">
    <tbody>
    <ww:iterator value="appliedPatches" status="'status'">
        <tr>
            <td width="40%"><b><ww:property value="issueKey" /></b></td>
            <td width="60%"><ww:property value="description" /></td>
        </tr>
    </ww:iterator>
    </tbody>
</table>
</ww:if>

<%--
Java VM Memory stats
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.java.vm.memory.statistics'"/></h3>
<table class="aui aui-table-rowhover">
    <tbody>
    <ww:iterator value="/extendedSystemInfoUtils/jvmStats" status="'status'">
    <tr>
		<td width="40%"><b><ww:property value="key" /></b></td>
		<td width="60%"><ww:property value="value" /></td>
	</tr>
    </ww:iterator>
    <tr>
		<td><b><ww:text name="'admin.systeminfo.memory.graph'"/></b></td>
		<td width="60%">
            <table  border="0" cellpadding="0" cellspacing="0" style="float:left;" width="40%">
                <tr>
                <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)">
                    <td class="bar-status-bad" width="<ww:property value="."/>%">
                        <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                            <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                 alt=""
                                 height="15"
                                 width="100%"
                                 border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                             <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                         </ww:text>">
                        </a>
                    </td>
                </ww:property>

                <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)">
                    <td class="bar-status-good" width="<ww:property value="."/>%">
                        <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                            <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                 alt=""
                                 height="15"
                                 width="100%"
                                 border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                                 <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                             </ww:text>">
                        </a>
                    </td>
                </tr>
            </table>
            <b>&nbsp;&nbsp;<ww:text name="'admin.systeminfo.memory.percent.free'">
                <ww:param name="'value0'"><ww:property value="."/> </ww:param>
            </ww:text> </b>
            (<ww:text name="'common.words.total'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/totalMemory"/> MB)
            &nbsp;&nbsp;<span>(<a href="ViewSystemInfo!garbageCollection.jspa"><ww:text name="'admin.systeminfo.force.garbage.collection'"><ww:param name="value0">garbage collection</ww:param></ww:text></a>)</span>
            </ww:property>
        </td>
    </tr>
    <ww:if test="/extendedSystemInfoUtils/jvmJava5OrGreater == true">
        <ww:if test="/extendedSystemInfoUtils/systemInfoUtils/usedPermGenMemory != 0">
        <tr>
            <td width="40%"><b><ww:text name="'admin.systeminfo.perm.gen.memory.graph'"/></b></td>
            <td width="60%">
                <table  border="0" cellpadding="0" cellspacing="0" style="float:left;" width="40%">
                    <tr>
                    <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedPermGenMemory, /extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory)">
                        <td bgcolor="#CC3333" width="<ww:property value="."/>%">
                            <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                                <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                     alt=""
                                     height="15"
                                     width="100%"
                                     border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                                 <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                             </ww:text>">
                            </a>
                        </td>
                    </ww:property>

                    <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freePermGenMemory, /extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory)">
                        <td bgcolor="#00CC00" width="<ww:property value="."/>%">
                            <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                                <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                     alt=""
                                     height="15"
                                     width="100%"
                                     border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                                     <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                                 </ww:text>">
                            </a>
                        </td>
                    </tr>
                </table>
                <b>&nbsp;&nbsp;<ww:text name="'admin.systeminfo.memory.percent.free'">
                    <ww:param name="'value0'"><ww:property value="."/> </ww:param>
                </ww:text> </b>
                (<ww:text name="'common.words.total'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory"/> MB)
                </ww:property>
            </td>
        </tr>
        </ww:if>
        <ww:if test="/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory != 0">
        <tr>
            <td width="40%"><b><ww:text name="'admin.systeminfo.nonheap.memory.graph'"/></b></td>
            <td width="60%">
                <table  border="0" cellpadding="0" cellspacing="0" style="float:left;" width="40%">
                    <tr>
                    <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)">
                        <td bgcolor="#CC3333" width="<ww:property value="."/>%">
                            <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                                <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                     alt=""
                                     height="15"
                                     width="100%"
                                     border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                                 <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                             </ww:text>">
                            </a>
                        </td>
                    </ww:property>

                    <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)">
                        <td bgcolor="#00CC00" width="<ww:property value="."/>%">
                            <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                                <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                     alt=""
                                     height="15"
                                     width="100%"
                                     border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                                     <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                                 </ww:text>">
                            </a>
                        </td>
                    </tr>
                </table>
                <b>&nbsp;&nbsp;<ww:text name="'admin.systeminfo.memory.percent.free'">
                    <ww:param name="'value0'"><ww:property value="."/> </ww:param>
                </ww:text> </b>
                (<ww:text name="'common.words.total'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory"/> MB)
                </ww:property>
            </td>
        </tr>
        </ww:if>
        <tr>
            <td width="40%">&nbsp;</td>
            <td width="60%">
                <ww:text name="'admin.systeminfo.java.vm.memory.statistics.more.url'">
                    <ww:param name="'value0'"><a href="ViewMemoryInfo.jspa"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text> <ww:text name="'admin.systeminfo.java.vm.memory.statistics.more.info'"/>
            </td>
        </tr>
    </ww:if>
    </tbody>
</table>

<%--
JIRA info
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.jira.info'"/></h3>
<table class="aui aui-table-rowhover" id="jirainfo">
    <tbody>
    <ww:iterator value="/extendedSystemInfoUtils/buildStats" status="'status'">
        <tr>
            <td width="40%"><b><ww:property value="key" /></b></td>
            <td width="60%"><ww:property value="value" /></td>
        </tr>
        </ww:iterator>
    <ww:if test="/extendedSystemInfoUtils/upgradeHistory/empty == false">
        <tr>
            <td width="40%">&nbsp;</td>
            <td width="60%">
                <ww:text name="'admin.systeminfo.upgrade.history.more.url'">
                    <ww:param name="'value0'"><a id="view_upgrade_history" href="ViewUpgradeHistory.jspa"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text> <ww:text name="'admin.systeminfo.upgrade.history.more.info'"/></td>
        </tr>
    </ww:if>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.installed.languages'"/></b></td>
            <td>
            <ww:iterator value="/localeManager/installedLocales" status="'status'">
                <ww:property value="/displayNameOfLocale(.)"/><ww:if test="@status/last == false"><br></ww:if>
            </ww:iterator>
            </td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.default.language'"/></b></td>
            <td><ww:property value="/extendedSystemInfoUtils/defaultLanguage" /><ww:if test="/extendedSystemInfoUtils/usingSystemLocale == true"> - <ww:text name="'admin.systeminfo.system.default.locale'"/></ww:if></td>
        </tr>
    </tbody>
</table>

<%--
License info
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.license.info'"/></h3>
<table class="aui aui-table-rowhover" id="license_info">
    <tbody>
    <ww:iterator value="/extendedSystemInfoUtils/licenseInfo" status="'status'">
        <tr>
            <td width="40%"><b><ww:property value="key" /></b></td>
            <td width="60%"><ww:property value="value" /></td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<%--
Config info
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.common.config.info'"/></h3>
<table class="aui aui-table-rowhover" id="common_config_info">
    <tbody>
    <ww:iterator value="/extendedSystemInfoUtils/commonConfigProperties" status="'status'">
        <tr>
            <td width="40%"><b><ww:property value="key" /></b></td>
            <td width="60%"><ww:property value="value" /></td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<%--
DB Stats
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.database.statistics'"/></h3>
<table class="aui aui-table-rowhover">
    <tbody>
    <ww:iterator value="/extendedSystemInfoUtils/usageStats" status="'status'">
        <tr>
            <td width="40%"><b><ww:property value="key" /></b></td>
            <td width="60%"><ww:property value="value" /></td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<%--
File Paths
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.file.paths'"/></h3>
<table class="aui aui-table-rowhover" id="file_paths">
    <tbody>
        <tr>
            <td width="40%"><b><ww:text name="'admin.systeminfo.location.of.jira.home'"/></b></td>
            <td width="60%" id="file_paths_jirahome"><ww:property value="/extendedSystemInfoUtils/jiraHomeLocation" /></td>
        </tr>
        <tr>
            <td width="40%"><b><ww:text name="'admin.systeminfo.location.of.entity.engine'"/></b></td>
            <td width="60%"><ww:property value="/extendedSystemInfoUtils/entityEngineXmlPath" /></td>
        </tr>
        <tr>
            <td width="40%"><b><ww:text name="'admin.systeminfo.location.of.atlassian.jira.log'"/></b></td>
            <td width="60%"><ww:property value="/extendedSystemInfoUtils/logPath" /></td>
        </tr>
        <tr>
            <td width="40%"><b><ww:text name="'admin.systeminfo.location.of.indexes'"/></b></td>
            <td width="60%"><ww:property value="/extendedSystemInfoUtils/indexLocation" /></td>
        </tr>
        <tr>
            <td width="40%"><b><ww:text name="'admin.systeminfo.location.of.attachments'"/></b></td>
            <td width="60%"><ww:property value="/extendedSystemInfoUtils/attachmentsLocation" /></td>
        </tr>
        <tr>
            <td width="40%"><b><ww:text name="'admin.systeminfo.location.of.backups'"/></b></td>
            <td width="60%"><ww:property value="/extendedSystemInfoUtils/backupLocation" /></td>
        </tr>
    </tbody>
</table>

<%--
LISTENERS
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.listeners'"/></h3>
<table class="aui aui-table-rowhover">
    <tbody>
    <ww:iterator value="/extendedSystemInfoUtils/listeners" status="'status'">
        <tr>
            <td width="40%">
                <b><ww:property value="string('name')" /></b>
                <div class="description"><ww:property value="string('clazz')" /></div>
            </td>
            <td width="60%">
                <ww:property value="propertySet(.)/keys('',5)">
                <table cellpadding="2" cellspacing="0" border="0">
                    <ww:if test=". != null">
                        <ww:iterator value=".">
                            <tr>
                                <td><b><ww:property value="." />:</b></td>
                                <td><ww:property value="propertySet(../..)/string(.)"/></td>
                            </tr>
                        </ww:iterator>
                    </ww:if>
                </table>
                </ww:property>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<%--
SERVICES
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.services'"/></h3>
<table class="aui aui-table-rowhover">
    <tbody>
    <ww:iterator value="/extendedSystemInfoUtils/services" status="'status'">
        <tr>
            <td width="40%">
                <b><ww:property value="./name" /></b>
                <div class="description"><ww:property value="./serviceClass" /></div>
            </td>
            <td width="60%">
                <table cellpadding="2" cellspacing="0" border="0">
                    <tr>
                        <td><b><ww:text name="'admin.systeminfo.service.delay'"/>:</b></td>
                        <td><ww:property value="/extendedSystemInfoUtils/millisecondsToMinutes(./delay)"/>&nbsp;<ww:text name="'core.dateutils.minutes'"/></td>
                    </tr>
                    <ww:iterator value="/extendedSystemInfoUtils/servicePropertyMap(.)/entrySet">
                    <tr>
                        <td><b><ww:property value="./key" />:</b></td>
                        <td><ww:property value="/text(./value)" /></td>
                    </tr>
                    </ww:iterator>
                </table>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<%--
user PLUGINS
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.user.plugins'"/></h3>
<ww:if test="/pluginInfoProvider/userPlugins/empty == true">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'"><p><ww:text name="'admin.systeminfo.no.user.plugins.installed'"/></p></aui:param>
    </aui:component>
</ww:if>
<ww:else>
    <table class="aui aui-table-rowhover">
        <tbody>
        <ww:iterator value="/pluginInfoProvider/userPlugins" status="'status'">
        <tr>
            <td width="40%">
                <b><ww:property value="./name" /></b> - <ww:property value="./pluginInformation/version" />
                <div class="description"><ww:text name="'admin.systeminfo.plugin.by'"/>&nbsp;<ww:property value="./pluginInformation/vendorName" /></div>
            </td>
            <td width="60%">
                <table cellpadding="2" cellspacing="0" border="0">
                    <tr>
                        <td colspan="2">
                            <ww:if test="./enabled == true">
                                <ww:text name="'admin.systeminfo.plugin.enabled'"/>
                            </ww:if>
                            <ww:else>
                                <ww:text name="'admin.systeminfo.plugin.disabled'"/>
                            </ww:else>
                        </td>
                    </tr>
                    <ww:iterator value="./pluginInformation/parameters/entrySet">
                    <tr>
                        <td><b><ww:property value="./key" />:</b></td>
                        <td><ww:property value="/text(./value)" /></td>
                    </tr>
                    </ww:iterator>
                </table>
            </td>
        </tr>
        </ww:iterator>
        </tbody>
    </table>
</ww:else>

<%--
System Plugins
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.system.plugins'"/></h3>
<table class="aui aui-table-rowhover">
    <tbody>
    <ww:iterator value="/pluginInfoProvider/systemPlugins" status="'status'">
        <tr>
            <td width="40%">
                <b><ww:property value="./name" /></b> - <ww:property value="./pluginInformation/version" />
                <div class="description"><ww:text name="'admin.systeminfo.plugin.by'"/>&nbsp;<ww:property value="./pluginInformation/vendorName" /></div>
            </td>
            <td width="60%">
                <table cellpadding="2" cellspacing="0" border="0">
                    <tr>
                        <td colspan="2">
                            <ww:if test="./enabled == true">
                                <ww:text name="'admin.systeminfo.plugin.enabled'"/>
                            </ww:if>
                            <ww:else>
                                <ww:text name="'admin.systeminfo.plugin.disabled'"/>
                            </ww:else>
                        </td>
                    </tr>
                    <ww:iterator value="./pluginInformation/parameters/entrySet">
                    <tr>
                        <td><b><ww:property value="./key" />:</b></td>
                        <td><ww:property value="/text(./value)" /></td>
                    </tr>
                    </ww:iterator>
                </table>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>


<%--
APPLICATION PROPERTIES
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.application.properties'"/></h3>
<table class="aui aui-table-rowhover" id="application_properties">
    <tbody>
    <ww:iterator value="applicationPropertiesHTML" status="'status'">
        <tr>
            <td width="40%"><b><ww:property value="key" /></b></td>
            <td width="60%"><ww:property value="value" escape="true"/></td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<%--
SYSTEM PROPERTIES
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.system.properties'"/></h3>
<table class="aui aui-table-rowhover">
    <tbody>
    <ww:iterator value="systemPropertiesHTML" status="'status'">
        <tr>
            <td width="40%"><b><ww:property value="key" /></b></td>
            <td width="60%"><ww:property value="value" escape="false"/></td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<%--
TRUSTED APPLICATIONS
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.trustedapps'"/></h3>
<%
    JiraServiceContext jiraServiceContext = (JiraServiceContext) CoreActionContext.getValueStack().findValue("/jiraServiceContext");
    JiraServiceContext trustedAppContext = new JiraServiceContextImpl(jiraServiceContext.getUser());
    ExtendedSystemInfoUtils sysInfo = (ExtendedSystemInfoUtils) CoreActionContext.getValueStack().findValue("/extendedSystemInfoUtils");
    Set trustedApps = sysInfo.getTrustedApplications(trustedAppContext);
    if(trustedAppContext.getErrorCollection().hasAnyErrors()) {
%>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">warning</aui:param>
    <aui:param name="'messageHtml'"><p><ww:text name="'admin.errors.trustedapps.no.permission'"/></p></aui:param>
</aui:component>
<% } else if(trustedApps.isEmpty()) { %>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">info</aui:param>
    <aui:param name="'messageHtml'"><p><ww:text name="'admin.trustedapps.no.apps.configured'"/></p></aui:param>
</aui:component>
<% } else { %>
<table class="aui aui-table-rowhover">
    <tbody>
    <ww:iterator value="/extendedSystemInfoUtils/trustedApplications(/jiraServiceContext)" status="'status'">
        <tr>
            <td width="40%"><b><ww:property value="./name" /></b></td>
            <td width="60%">
                <table cellpadding="2" cellspacing="0" border="0">
                    <tr>
                        <td><b><ww:text name="'admin.trustedapps.field.application.id'"/>:</b></td>
                        <td><ww:property value="./ID" /></td>
                    </tr>
                    <tr>
                        <td><b><ww:text name="'admin.trustedapps.field.timeout'"/>:</b></td>
                        <td><ww:property value="./timeout" /></td>
                    </tr>
                    <tr>
                        <td><b><ww:text name="'admin.trustedapps.field.ip.matches'"/>:</b></td>
                        <td><ww:iterator value="/extendedSystemInfoUtils/IPMatches(.)">
                                <ww:property value="." /><br/>
                            </ww:iterator></td>
                    </tr>
                    <tr>
                        <td><b><ww:text name="'admin.trustedapps.field.url.matches'"/>:</b></td>
                        <td><ww:iterator value="/extendedSystemInfoUtils/urlMatches(.)">
                                <ww:property value="." /><br/>
                            </ww:iterator></td>
                    </tr>
                </table>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>
<% } %>

<%--
client info. useful to check client's IP address
--%>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.client.info'"/></h3>
<table class="aui aui-table-rowhover">
    <tbody>
        <tr>
            <td width="40%"><b><ww:text name="'admin.systeminfo.remote.address'"/></b></td>
            <td width="60%"><%=request.getRemoteAddr()%></td>
        </tr>
        <tr>
            <td width="40%"><b><ww:text name="'admin.systeminfo.remote.host'"/></b></td>
            <td width="60%"><%=request.getRemoteHost()%></td>
        </tr>
        <tr>
            <td width="40%"><b><ww:text name="'admin.systeminfo.remote.port'"/></b></td>
            <td width="60%"><%=request.getRemotePort()%></td>
        </tr>
    </tbody>
</table>
</body>
</html>
