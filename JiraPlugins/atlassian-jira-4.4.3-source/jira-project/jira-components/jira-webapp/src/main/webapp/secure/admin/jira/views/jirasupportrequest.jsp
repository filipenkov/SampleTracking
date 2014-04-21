<%@ page import="com.atlassian.jira.bc.JiraServiceContext" %>
<%@ page import="com.atlassian.jira.bc.JiraServiceContextImpl" %>
<%@ page import="com.atlassian.jira.util.system.ExtendedSystemInfoUtils" %>
<%@ page import="webwork.action.CoreActionContext" %>
<%@ page import="java.util.Set" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.supportrequest.support.request'"/></title>
</head>

<body>

<table width="100%" cellpadding="10" cellspacing="0" border="0">
    <tr>
        <td>
            <ww:if test="/hasMailServer == true">
                <page:applyDecorator name="jiraform">
                    <page:param name="title"><ww:text name="'admin.supportrequest.support.request'"/></page:param>
                    <page:param name="description"><ww:text name="'admin.supportrequest.instructions'"/>
                        <p><ww:text name="'admin.supportrequest.note'">
                            <ww:param name="'value0'"><b></ww:param>
                            <ww:param name="'value1'"></b></ww:param>
                        </ww:text></p>
                        <ww:if test="/mailSendingDisabled == true">
                            <p><ww:text name="'admin.mailqueue.sending.mail.is.disabled'">
                                <ww:param name="'value0'"><span class="warning"></ww:param>
                                <ww:param name="'value1'"></span></ww:param>
                            </ww:text></p>
                        </ww:if>
                    </page:param>
                    <page:param name="action">JiraSupportRequest.jspa</page:param>
                    <page:param name="submitId">support_submit</page:param>
                    <page:param name="submitName"><ww:text name="'admin.common.words.submit'"/></page:param>

                    <ui:textfield label="text('admin.supportrequest.to')" name="'to'" size="60" maxlength="255">
                        <ui:param name="'description'"><ww:text name="'admin.supportrequest.email.description'"/></ui:param>
                        <ui:param name="'mandatory'" value="true" />
                    </ui:textfield>

                    <ui:textfield label="text('admin.supportrequest.cc')" name="'cc'" size="60" maxlength="255">
                        <ui:param name="'description'"><ww:text name="'admin.supportrequest.email.description'"/></ui:param>
                    </ui:textfield>

                    <ui:textfield label="text('admin.supportrequest.subject')" name="'subject'" size="60" maxlength="255">
                        <ui:param name="'description'"><ww:text name="'admin.supportrequest.subject.description'"/></ui:param>
                        <ui:param name="'mandatory'" value="true" />
                    </ui:textfield>

                    <ui:textarea label="text('common.words.description')" name="'description'" cols="'70'" rows="'20'" >
                        <ui:param name="'description'" value="''" />
                        <ui:param name="'mandatory'" value="true" />
                    </ui:textarea>

                    <ui:textfield label="text('admin.supportrequest.supportIssueKey')" name="'supportIssueKey'" size="60" maxlength="20">
                        <ui:param name="'description'">
                            <ww:text name="'admin.supportrequest.supportIssueKey.description'">
                                <ww:param name="'value0'"><br /></ww:param>
                                <ww:param name="'value1'"><u></ww:param>
                                <ww:param name="'value2'"></u></ww:param>
                            </ww:text>
                        </ui:param>
                    </ui:textfield>

                    <ui:textfield label="text('admin.supportrequest.contact.name')" name="'name'" size="60" maxlength="255">
                        <ui:param name="'mandatory'" value="true" />
                    </ui:textfield>

                    <ui:textfield label="text('admin.supportrequest.contact.email')" name="'email'" size="60" maxlength="255">
                        <ui:param name="'description'"><ww:text name="'admin.supportrequest.email.description'" /></ui:param>
                        <ui:param name="'mandatory'" value="true" />
                    </ui:textfield>

                    <ui:textfield label="text('admin.supportrequest.contact.number')" name="'phone'" size="60" maxlength="255" />

                    <tr>
                        <td bgcolor="#fffff0" align="right" valign="top"><ww:text name="'admin.supportrequest.environment'"/>:</td>
                        <td bgcolor="#ffffff">
                            <table id="support_req_env" class="borderedBox" bgcolor="#f0f0f0" width="100%" cellpadding="1">

                                <ww:if test="/warningMessages/size() > 0">
                                <tr>
                                    <td nowrap width="1%"><b><ww:text name="'admin.systeminfo.environment.warnings'"/></b></td>
                                    <td>
                                        <ww:iterator value="/warningMessages">
                                            <ww:property value="."/><br/>
                                        </ww:iterator>
                                    </td>
                                </tr>
                                </ww:if>

                                <ww:if test="/appliedPatches/size() > 0">
                                <tr>
                                    <td nowrap width="1%"><b><ww:text name="'admin.systeminfo.applied.patches'"/></b></td>
                                    <td>
                                        <ww:iterator value="/appliedPatches">
                                            <ww:property value="./issueKey"/> : <ww:property value="./description"/><br/>
                                        </ww:iterator>
                                    </td>
                                </tr>
                                </ww:if>

                                <tr>
                                    <td nowrap width="1%"><b><ww:text name="'admin.generalconfiguration.base.url'"/></b></td>
                                    <td ><ww:property value="/extendedSystemInfoUtils/baseUrl"/></td>
                                </tr>

                                <ww:iterator value="/extendedSystemInfoUtils/props(true)" status="'status'">
                                <tr>
                                    <td nowrap width="1%"><b><ww:property value="key" /></b></td>
                                    <td ><ww:property value="value" /></td>
                                </tr>
                                </ww:iterator>

                                <ww:iterator value="/extendedSystemInfoUtils/licenseInfo" status="'status'">
                                <tr>
                                    <td nowrap width="1%"><b><ww:property value="key" /></b></td>
                                    <td ><ww:property value="value" /></td>
                                </tr>
                                </ww:iterator>

                                <ww:iterator value="/extendedSystemInfoUtils/jvmStats" status="'status'">
                                <tr>
                                    <td nowrap width="1%"><b><ww:property value="key" /></b></td>
                                    <td ><ww:property value="value" /></td>
                                </tr>
                                </ww:iterator>

                                <ww:iterator value="/extendedSystemInfoUtils/buildStats" status="'status'">
                                <tr>
                                    <td nowrap width="1%"><b><ww:property value="key" /></b></td>
                                    <td ><ww:property value="value" /></td>
                                </tr>
                                </ww:iterator>

                                <ww:iterator value="/extendedSystemInfoUtils/commonConfigProperties" status="'status'">
                                <tr>
                                    <td nowrap width="1%"><b><ww:property value="key" /></b></td>
                                    <td ><ww:property value="value" /></td>
                                </tr>
                                </ww:iterator>

                                <tr>
                                    <td valign="top"><b><ww:text name="'admin.generalconfiguration.installed.languages'"/></b></td>
                                    <td valign="top">
                                    <ww:iterator value="jiraLocaleUtils/installedLocales" status="'status'">
                                        <ww:property value="./displayName"/><ww:if test="@status/last == false"><br></ww:if>
                                    </ww:iterator>
                                    </td>
                                </tr>

                                <tr>
                                    <td valign="top"><b><ww:text name="'admin.generalconfiguration.default.language'"/></b></td>
                                    <td valign="top"><ww:property value="/extendedSystemInfoUtils/defaultLanguage" /><ww:if test="/extendedSystemInfoUtils/usingSystemLocale == true"> - <ww:text name="'admin.systeminfo.system.default.locale'"/></ww:if></td>
                                </tr>

                                <ww:if test="/extendedSystemInfoUtils/usageStats != null && /extendedSystemInfoUtils/usageStats/empty == false">
                                    <tr>
                                        <td valign="top" colspan="2"><b><ww:text name="'admin.systeminfo.database.statistics'"/></b></td>
                                    </tr>
                                    <ww:iterator value="/extendedSystemInfoUtils/usageStats" status="'status'">
                                    <tr>
                                        <td valign="top" width="40%"><ww:property value="key" /></td>
                                        <td valign="top" width="60%"><ww:property value="value" /></td>
                                    </tr>
                                    </ww:iterator>
                                </ww:if>

                                <tr>
                                    <td valign="top" colspan="2"><b><ww:text name="'admin.systeminfo.trustedapps'"/></b><ww:property value="trustedAppContext"/></td>
                                </tr>
                                <%
                                    JiraServiceContext jiraServiceContext = (JiraServiceContext) CoreActionContext.getValueStack().findValue("/jiraServiceContext");
                                    JiraServiceContext trustedAppContext = new JiraServiceContextImpl(jiraServiceContext.getUser());
                                    ExtendedSystemInfoUtils sysInfo = (ExtendedSystemInfoUtils) CoreActionContext.getValueStack().findValue("/extendedSystemInfoUtils");
                                    Set trustedApps = sysInfo.getTrustedApplications(trustedAppContext);
                                    if(trustedAppContext.getErrorCollection().hasAnyErrors()) {
                                %>
                                    <tr>
                                        <td width="40%" valign="top"></td>
                                        <td valign="top" width="60%" class="small">
                                            [<ww:text name="'admin.errors.trustedapps.no.permission'"/>]
                                        </td>
                                    </tr>
                                <% } else if(trustedApps.isEmpty()) { %>
                                    <tr>
                                        <td width="40%" valign="top"></td>
                                        <td valign="top" width="60%" class="small">
                                            [<ww:text name="'admin.trustedapps.no.apps.configured'"/>]
                                        </td>
                                    </tr>
                                <% } else { %>
                                    <ww:iterator value="/extendedSystemInfoUtils/trustedApplications(/jiraServiceContext)" status="'status'">
                                    <tr>
                                        <td width="40%" valign="top"><ww:property value="./name" /></td>
                                        <td width="60%" valign="top">
                                            <table cellpadding="0" cellspacing="0" border="0">
                                                <tr>
                                                    <td valign="top"><ww:text name="'admin.trustedapps.field.application.id'"/>:</td>
                                                    <td valign="top" class="small"><ww:property value="./ID" /></td>
                                                </tr>
                                                <tr>
                                                    <td valign="top"><ww:text name="'admin.trustedapps.field.timeout'"/>:</td>
                                                    <td valign="top" class="small"><ww:property value="./timeout" /></td>
                                                </tr>
                                                <tr>
                                                    <td valign="top"><ww:text name="'admin.trustedapps.field.ip.matches'"/>:</td>
                                                    <td valign="top" class="small"><ww:iterator value="/extendedSystemInfoUtils/IPMatches(.)">
                                                            <ww:property value="." /><br/>
                                                        </ww:iterator></td>
                                                </tr>
                                                <tr>
                                                    <td valign="top"><ww:text name="'admin.trustedapps.field.url.matches'"/>:</td>
                                                    <td valign="top" class="small"><ww:iterator value="/extendedSystemInfoUtils/urlMatches(.)">
                                                            <ww:property value="." /><br/>
                                                        </ww:iterator></td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    </ww:iterator>
                                <% } %>
                            </table>
                        </td>
                    </tr>
                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <page:applyDecorator name="jiraform">
                    <page:param name="action">ViewProjects.jspa</page:param>
                    <page:param name="submitId">cancel_submit</page:param>
                    <page:param name="submitName"><ww:text name="'common.forms.cancel'"/></page:param>
                    <page:param name="autoSelectFirst">false</page:param>
                    <page:param name="title"><ww:text name="'admin.supportrequest.support.request'"/></page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="description">
                        <p><ww:text name="'admin.supportrequest.page.description'"/></p>
                        <p>
                            <ww:if test="/systemAdministrator == true">
                                <ww:text name="'admin.supportrequest.note.configure.mail.server'">
                                    <ww:param name="'value0'"><span class="note"></ww:param>
                                    <ww:param name="'value1'"></span></ww:param>
                                    <ww:param name="'value2'"><a id="configure_mail_server" href="ViewMailServers.jspa"></ww:param>
                                    <ww:param name="'value3'"></a></ww:param>
                                </ww:text>
                            </ww:if>
                            <ww:else>
                                <ww:text name="'admin.supportrequest.note.configure.mail.server.admin'">
                                    <ww:param name="'value0'"><span class="note"></ww:param>
                                    <ww:param name="'value1'"></span></ww:param>
                                    <%--Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved--%>
                                    <%--<ww:param name="'value2'"><a href="<%=request.getContextPath()%>/secure/Administrators.jspa#sysadmins"></ww:param>--%>
                                    <%--<ww:param name="'value3'"></a></ww:param>--%>
                                    <ww:param name="'value2'"> </ww:param>
                                    <ww:param name="'value3'"> </ww:param>
                                </ww:text>
                            </ww:else>
                        </p>
                    </page:param>
                </page:applyDecorator>
            </ww:else>
        </td>
    </tr>
</table>

</body>
</html>
