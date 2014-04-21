<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section"/>
    <meta name="admin.active.tab" content="general_configuration"/>
	<title><ww:text name="'admin.generalconfiguration.jira.configuration'"/></title>
</head>
<%-- Use data-property-id to identify calls containing property values for testing --%>
<body>
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
    <page:applyDecorator name="jiratable">
        <page:param name="title"><ww:text name="'admin.common.words.settings'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="cssClass">module</page:param>
        <page:param name="helpURLFragment">#settings</page:param>
        <page:param name="helpURL">configuration</page:param>
        <page:param name="titleColspan">2</page:param>
        <tr>
            <td width="40%"><b><ww:text name="'admin.common.words.title'"/></b></td>
            <td width="60%" data-property-id="title"><ww:property value="applicationProperties/string('jira.title')"/></td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.common.words.mode'"/></b></td>
            <td data-property-id="jira-mode"><ww:property value="/jiraMode"/></td>
        </tr>
        <tr id="maximumAuthenticationAttemptsAllowed">
            <td><b><ww:text name="'admin.generalconfiguration.maximum.authentication.attempts.allowed'"/></b></td>
            <ww:if test="applicationProperties/defaultBackedString('jira.maximum.authentication.attempts.allowed') != null && applicationProperties/defaultBackedString('jira.maximum.authentication.attempts.allowed')/length() > 0">
                <td data-property-id="maximumAuthenticationAttemptsAllowed"><ww:property value="applicationProperties/defaultBackedString('jira.maximum.authentication.attempts.allowed')" /></td>
            </ww:if>
            <ww:else>
                <td data-property-id="maximumAuthenticationAttemptsAllowed"><ww:text name="'common.words.unlimited'"/></td>
            </ww:else>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.captcha.on.signup'"/></b></td>
            <td>
                <ww:if test="applicationProperties/option('jira.option.captcha.on.signup') == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                </ww:if>
                <ww:else>
                    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.base.url'"/></b></td>
            <td><a href="<ww:property value="applicationProperties/string('jira.baseurl')"/>" target="_blank"><ww:property value="applicationProperties/string('jira.baseurl')"/></a></td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.email.from.header'"/></b></td>
            <td><ww:property value="applicationProperties/defaultBackedString('jira.email.fromheader.format')"/></td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.common.words.introduction'"/></b></td>
            <td><ww:property value="/introductionProperty/viewHtml" escape="false" /></td>
        </tr>
    </page:applyDecorator>

    <page:applyDecorator name="jiratable">
        <page:param name="id">language-info</page:param>
        <page:param name="title"><ww:text name="'admin.generalconfiguration.internationalisation'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="cssClass">module</page:param>
        <page:param name="helpURLFragment">#i18n</page:param>
        <page:param name="helpURL">configuration</page:param>
        <page:param name="titleColspan">2</page:param>
        <ww:if test="/showPluginHints == true">
        <page:param name="description">
            <ww:text name="'admin.generalconfiguration.internationalisation.description'" >
                <ww:param name="value0"><a href="<%= request.getContextPath() %>/plugins/servlet/upm"></ww:param>
                <ww:param name="value1"></a></ww:param>
                <ww:param name="value2"><a target='_blank' href='<ww:property value="/tacUrl()"/>'></ww:param>
                <ww:param name="value3"></a></ww:param>
            </ww:text>
        </page:param>
        </ww:if>
        <tr>
            <td width="40%"><b><ww:text name="'admin.generalconfiguration.indexing.language'"/></b></td>
            <td width="60%"><ww:property value="applicationProperties/string('jira.i18n.language.index')" /></td>
        </tr>
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
            <td><ww:property value="/displayNameOfLocale(/applicationProperties/defaultLocale)" /></td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.default.timezone'"/></b></td>
            <td><ww:if test="/useSystemTimeZone == true"><ww:text name="'admin.timezone.system.default'"/>: </ww:if><ww:property value="/defaultTimeZoneInfo/GMTOffset"/> <ww:property value="/defaultTimeZoneInfo/city"/></td>
        </tr>
    </page:applyDecorator>

    <page:applyDecorator name="jiratable">
        <page:param name="id">options_table</page:param>
        <page:param name="title"><ww:text name="'admin.common.words.options'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="cssClass">module</page:param>
        <page:param name="helpURLFragment">#options</page:param>
        <page:param name="helpURL">configuration</page:param>
        <page:param name="titleColspan">2</page:param>

        <tr>
            <td width="40%"><b><ww:text name="'admin.generalconfiguration.allow.users.to.vote'"/></b></td>
            <td width="60%">
                <ww:if test="applicationProperties/option('jira.option.voting') == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                </ww:if>
                <ww:else>
                    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.allow.users.to.watch'"/></b></td>
            <td>
                <ww:if test="applicationProperties/option('jira.option.watching') == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                </ww:if>
                <ww:else>
                    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.allow.unassigned.issues'"/></b></td>
            <td>
                <ww:if test="applicationProperties/option('jira.option.allowunassigned') == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                </ww:if>
                <ww:else>
                    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.external.user.management'"/></b></td>
            <td>
                <ww:if test="applicationProperties/option('jira.option.user.externalmanagement') == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                </ww:if>
                <ww:else>
                    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.logout.confirmation'"/></b></td>
            <td><strong>
                <ww:if test="applicationProperties/defaultBackedString('jira.option.logoutconfirm') == 'never'">
                    <ww:text name="'admin.common.words.never'"/>
                </ww:if>
                <ww:elseIf test="applicationProperties/defaultBackedString('jira.option.logoutconfirm') == 'cookie'">
                    <ww:text name="'admin.common.words.cookie'"/>
                </ww:elseIf>
                <ww:else>
                    <ww:text name="'admin.common.words.always'"/>
                </ww:else>
                </strong>
            </td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.use.gzip.compression'"/></b></td>
            <td>
                <ww:if test="/gzipCompression/enabled == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                </ww:if>
                <ww:else>
                    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.accept.remote.api.calls'"/></b></td>
            <td>
                <ww:if test="applicationProperties/option('jira.option.rpc.allow') == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                </ww:if>
                <ww:else>
                    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.user.email.visibility'"/></b></td>
            <td><strong>
                <ww:if test="applicationProperties/defaultBackedString('jira.option.emailvisible') == 'show'">
                    <ww:text name="'admin.generalconfiguration.public'"/>
                </ww:if>
                <ww:elseIf test="applicationProperties/defaultBackedString('jira.option.emailvisible') == 'hide'">
                    <ww:text name="'admin.generalconfiguration.hidden'"/>
                </ww:elseIf>
                <ww:elseIf test="applicationProperties/defaultBackedString('jira.option.emailvisible') == 'mask'">
                    <ww:text name="'admin.generalconfiguration.masked'"><ww:param name="value0">user at example dot com</ww:param></ww:text>
                </ww:elseIf>
                <ww:else>
                    <ww:text name="'admin.generalconfiguration.logged.in.only'"/>
                </ww:else>
                </strong>
            </td>
        </tr>

        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.commentlevel.visibility'"/></b></td>
            <td>
                <ww:if test="applicationProperties/option('jira.comment.level.visibility.groups') == true">
                    <strong><ww:text name="'admin.generalconfiguration.commentlevel.visibility.both'"/></strong>
                </ww:if>
                <ww:else>
                    <strong><ww:text name="'admin.generalconfiguration.commentlevel.visibility.rolesonly'"/></strong>
                </ww:else>
            </td>
        </tr>

        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.enable.email.header'">
                <ww:param name="'value0'"></b><br></ww:param>
                <ww:param name="'value1'">Precedence: bulk</ww:param>
            </ww:text></td>
            <td>
                <ww:if test="applicationProperties/option('jira.option.precedence.header.exclude') == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                </ww:if>
                <ww:else>
                    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
       <tr>
            <td><b><ww:text name="'admin.generalconfiguration.enable.ajax.issue.picker'" /></b></td>
            <td>
                <ww:if test="applicationProperties/option('jira.ajax.autocomplete.issuepicker.enabled') == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                </ww:if>
                <ww:else>
                    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
        <tr>
             <td><b><ww:text name="'admin.generalconfiguration.enabled.jql.autocomplete'" /></b></td>
             <td>
                 <ww:if test="applicationProperties/option('jira.jql.autocomplete.disabled') == false">
                     <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                 </ww:if>
                 <ww:else>
                     <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                 </ww:else>
             </td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.ie.mime.sniffing'"/></b></td>
            <td><strong>
                <ww:if test="applicationProperties/defaultBackedString('jira.attachment.download.mime.sniffing.workaround') == 'workaround'">
                    <ww:text name="'admin.generalconfiguration.ie.mime.sniffing.workaround'"/>
                </ww:if>
                <ww:elseIf test="applicationProperties/defaultBackedString('jira.attachment.download.mime.sniffing.workaround') == 'secure'">
                    <ww:text name="'admin.generalconfiguration.ie.mime.sniffing.paranoid'"/>
                </ww:elseIf>
                <ww:elseIf test="applicationProperties/defaultBackedString('jira.attachment.download.mime.sniffing.workaround') == 'insecure'">
                    <ww:text name="'admin.generalconfiguration.ie.mime.sniffing.owned'"><ww:param name="value0">user at example dot com</ww:param></ww:text>
                </ww:elseIf>
                <ww:else>
                    <ww:text name="'admin.generalconfiguration.logged.in.only'"/>
                </ww:else>
                </strong>
            </td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.show.contact.administrators.form'" /></b></td>
            <td data-property-id="contact-admin-form">
                <ww:if test="applicationProperties/option('jira.show.contact.administrators.form') == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                </ww:if>
                <ww:else>
                    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>
        <tr>
            <td><b><ww:text name="'admin.generalconfiguration.contact.administrators.message'" /></b></td>
            <td>
                <ww:property value="/contactAdministratorsMessage" escape="false"/>
            </td>
        </tr>

        <%-- - Use Gravatar --%>
        <tr>
            <td><b><ww:text name="'admin.useravatar.gravatar.option'"></ww:text></b></td>
            <td data-property-id="use-gravatar">
                <ww:if test="/useGravatar == true">
                    <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
            </ww:if>
                <ww:else>
                    <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                </ww:else>
            </td>
        </tr>

        <%-- Inline edit --%>
        <ww:if test="showDisableInlineEdit == 'true'">
            <tr data-property-id="disableInlineEdit">
                <td><b><ww:text name="'admin.generalconfiguration.inline.edit'"></ww:text></b></td>
                <td>
                    <ww:if test="/disableInlineEdit == false">
                        <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                    </ww:if>
                    <ww:else>
                        <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                    </ww:else>
                </td>
            </tr>
        </ww:if>

    </page:applyDecorator>
    <div class="buttons-container">
        <div class="buttons">
            <a class="aui-button" id="edit-app-properties" href="EditApplicationProperties!default.jspa"><ww:text name="'admin.common.phrases.edit.configuration'"/></a>
        <ww:if test="/systemAdministrator == true">
            <a class="aui-button" id="edit-advanced-properties" href="AdvancedApplicationProperties.jspa"><ww:text name="'admin.menu.advanced'"/></a>
        </ww:if>
        </div>
    </div>
</body>
</html>
