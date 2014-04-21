<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.jira.ComponentManager,
                 com.atlassian.plugin.webresource.WebResourceManager"%>
<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section"/>
    <meta name="admin.active.tab" content="general_configuration"/>       
	<title><ww:text name="'admin.editapplicationproperties.title'"/></title>
    <%
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:jira-fields");
    %>
</head>
<body>
<page:applyDecorator name="jiraform">
	<page:param name="action">EditApplicationProperties.jspa</page:param>
	<page:param name="submitId">edit_property</page:param>
	<page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
	<page:param name="cancelURI">ViewApplicationProperties.jspa</page:param>
	<page:param name="title"><ww:text name="'admin.common.words.settings'"/></page:param>
	<page:param name="width">100%</page:param>

	<ui:textfield label="text('admin.generalconfiguration.application.title')" name="'title'">
		<ui:param name="'size'">40</ui:param>
		<ui:param name="'description'">
			<ww:text name="'admin.generalconfiguration.application.title.description'"/>
		</ui:param>
	</ui:textfield>

	<ui:select label="text('admin.common.words.mode')" name="'mode'"
		list="allowedModes" listKey="'key'" listValue="'value'">
		<ui:param name="'description'">
			<ww:text name="'admin.generalconfiguration.mode.description'">
			    <ww:param name="'value0'"><br/></ww:param>
			    <ww:param name="'value1'"><br/></ww:param>
			</ww:text>
		</ui:param>
	</ui:select>

    <ui:textfield label="text('admin.generalconfiguration.maximum.authentication.attempts.allowed')" name="'maximumAuthenticationAttemptsAllowed'">
        <ui:param name="'size'">8</ui:param>
        <ui:param name="'description'">
            <ww:text name="'admin.generalconfiguration.maximum.authentication.attempts.allowed.description'"/>
        </ui:param>
    </ui:textfield>

    <tr>
        <td class="fieldLabelArea">
            <ww:text name="'admin.generalconfiguration.captcha.on.signup'"/>
        </td>
        <td class="fieldValueArea">
            <input id="captchaOn" class="radio" type="radio" value="true" name="captcha" <ww:if test="captcha == true">checked="checked"</ww:if>/><label for="captchaOn"><ww:text name="'admin.common.words.on'"/></label>
            &nbsp;
            <input id="captchaOff" class="radio" type="radio" value="false" name="captcha" <ww:if test="captcha == false">checked="checked"</ww:if>/><label for="captchaOff"><ww:text name="'admin.common.words.off'"/></label>
            <div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.captcha.on.signup.description'"/>
            </div>
        </td>
    </tr>

	<ui:textfield label="text('admin.generalconfiguration.base.url')" name="'baseURL'">
        <ui:param name="'size'">40</ui:param>
		<ui:param name="'description'">
			<ww:text name="'admin.generalconfiguration.base.url.description'"/>
		</ui:param>
	</ui:textfield>

	<ui:textfield label="text('admin.generalconfiguration.email.from.header')" name="'emailFromHeaderFormat'">
        <ui:param name="'size'">40</ui:param>
		<ui:param name="'description'">
			<ww:text name="'admin.generalconfiguration.email.from.header.description'">
                <ww:param name="value0">From:</ww:param>
            </ww:text>
        </ui:param>
	</ui:textfield>

    <ui:textarea label="text('admin.common.words.introduction')" name="'introduction'" rows="8" cols="60">
		<ui:param name="'description'">
			<ww:text name="'admin.generalconfiguration.introduction.description'">
			    <ww:param name="'value0'"><br/></ww:param>
			</ww:text>
		</ui:param>
	</ui:textarea>


    <tr>
		<td colspan="2">
            <h3 class="formtitle"><ww:text name="'admin.generalconfiguration.internationalisation'"/></h3>
        </td>
	</tr>

    <ui:select label="text('admin.generalconfiguration.indexing.language')" name="'language'"
		list="allowedLanguages" listKey="'key'" listValue="'value'">
		<ui:param name="'description'">
			<ww:text name="'admin.generalconfiguration.indexing.language.description'">
			    <ww:param name="'value0'"><br/></ww:param>
			    <ww:param name="'value1'"><br/></ww:param>
                <ww:param name="'value2'"><a href="IndexAdmin.jspa"></ww:param>
			    <ww:param name="'value3'"></a></ww:param>
                <ww:param name="'value4'"><br/></ww:param>
            </ww:text>
		</ui:param>
	</ui:select>

    <tr>
		<td class="fieldLabelArea">
			<ww:text name="'admin.generalconfiguration.installed.languages'"/>
        </td>
		<td class="fieldValueArea">
            <ww:iterator value="/localeManager/installedLocales" status="'status'">
                <ww:property value="/displayNameOfLocale(.)"/><ww:if test="@status/last == false">, </ww:if>
            </ww:iterator>
			<div class="fieldDescription">
				<ww:text name="'admin.generalconfiguration.installed.languages.description'"/>
                <ww:component template="help.jsp" name="'i18n'">
                    <ww:param name="'align'">middle</ww:param>
                </ww:component>
			</div>
		</td>
	</tr>


    <ui:select label="text('admin.generalconfiguration.default.language')" name="'defaultLocale'"
		list="/installedLocales" listKey="'key'" listValue="'value'">
		<ui:param name="'description'">
            <ww:text name="'admin.generalconfiguration.default.language.description'">
                <ww:param name="'value0'"><br/></ww:param>
            </ww:text>
		</ui:param>
	</ui:select>

    <tr>
        <td class="fieldLabelArea">
            <ww:text name="'admin.generalconfiguration.default.timezone'"/>
        </td>
        <td class="fieldValueArea aui-field-cascadingselect" >
            <label for="timeZoneRegion"><ww:text name="'admin.timezone.region'"/></label>
            <select class="select cascadingselect-parent" id="timeZoneRegion" name="timeZoneRegion">
               <ww:iterator value="/timeZoneRegions">
                   <option class="option-group-<ww:property value="./key"/>" value="<ww:property value="./key"/>" <ww:if test="/configuredTimeZoneRegion == ./key">selected="selected"</ww:if>  ><ww:property value="./displayName"/></option>
               </ww:iterator>
             </select>
            <label for="defaultTimeZoneId"><ww:text name="'admin.timezone.zone'"/></label>
            <select class="select cascadingselect-child" id="defaultTimeZoneId" name="defaultTimeZoneId">
                <ww:iterator value="/timeZoneInfos">
                      <option class="option-group-<ww:property value="./regionKey"/>" value="<ww:property value="./timeZoneId"/>" <ww:if test="/configuredTimeZoneId == ./timeZoneId">selected="selected"</ww:if>><ww:property value="./GMTOffset"/> <ww:property value="./city"/> </option>
                </ww:iterator>
            </select>
            <div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.timezone.description'"/>
            </div>
        </td>
    </tr>
	<tr>
		<td colspan="2">
            <h3 class="formtitle"><ww:text name="'admin.common.words.options'"/></h3>
        </td>
	</tr>

	<tr>
		<td class="fieldLabelArea">
			<ww:text name="'admin.generalconfiguration.voting'"/>
        </td>
		<td class="fieldValueArea">
			<input id="votingOn" type="radio" class="radio" value="true" name="voting" <ww:if test="voting == true">checked="checked"</ww:if>/><label for="votingOn"><ww:text name="'admin.common.words.on'"/></label>
			&nbsp;
			<input id="votingOff" type="radio" class="radio" value="false" name="voting" <ww:if test="voting == false">checked="checked"</ww:if>/><label for="votingOff"><ww:text name="'admin.common.words.off'"/></label>
			<div class="fieldDescription">
				<ww:text name="'admin.generalconfiguration.voting.description'"/>
			</div>
		</td>
	</tr>

	<tr>
		<td class="fieldLabelArea">
			<ww:text name="'admin.generalconfiguration.watching'"/>
        </td>
		<td class="fieldValueArea">
			<input id="watchingOn" type="radio" class="radio" value="true" name="watching" <ww:if test="watching == true">checked="checked"</ww:if>/><label for="watchingOn"><ww:text name="'admin.common.words.on'"/></label>
			&nbsp;
			<input id="watchingOff" type="radio" class="radio" value="false" name="watching" <ww:if test="watching == false">checked="checked"</ww:if>/><label for="watchingOff"><ww:text name="'admin.common.words.off'"/></label>
			<div class="fieldDescription">
				<ww:text name="'admin.generalconfiguration.watching.description'"/>
			</div>
		</td>
	</tr>

	<tr>
		<td class="fieldLabelArea">
			<ww:text name="'admin.generalconfiguration.allow.unassigned.issues'"/>
        </td>
		<td class="fieldValueArea">
            <ww:if test="allowUnassigned == false || canSwitchUnassignedOff == true">
                <input id="allowUnassignedOn" type="radio" class="radio" value="true" name="allowUnassigned" <ww:if test="allowUnassigned == true">checked="checked"</ww:if>/><label for="allowUnassignedOn"><ww:text name="'admin.common.words.on'"/></label>
                &nbsp;
                <input id="allowUnassignedOff" type="radio" class="radio" value="false" name="allowUnassigned" <ww:if test="allowUnassigned == false">checked="checked"</ww:if>/><label for="allowUnassignedOff"><ww:text name="'admin.common.words.off'"/></label>
                <div class="fieldDescription">
                    <ww:text name="'admin.generalconfiguration.allow.unassigned.issues.description'"/>
                </div>
            </ww:if>
            <ww:else>
                <ww:if test="unassignedIssueCount > 0">
                    <div class="fieldDescription">
                    <ww:text name="'admin.generalconfiguration.allow.unassigned.issues.error1'">
                        <ww:param name="'value0'"><ww:text name="'admin.generalconfiguration.allow.unassigned.issues'"/></ww:param>
                        <ww:param name="'value1'"><ww:property value="unassignedIssueCount"/> <a href="<ww:url page="/secure/IssueNavigator.jspa"><ww:param name="'assigneeSelect'" value="'unassigned'"/><ww:param name="'reset'" value="'true'"/></ww:url>"></ww:param>
                        <ww:param name="'value2'"></a></ww:param>
                        <ww:param name="'value3'"><br/></ww:param>
                        <ww:param name="'value4'"><br/></ww:param>
                    </ww:text>
                    </div>
                </ww:if>
                <ww:if test="projectsWithDefaultUnassignedCount > 0">
                    <div class="fieldDescription">
                    <ww:text name="'admin.generalconfiguration.allow.unassigned.issues.error2'">
                        <ww:param name="'value0'"><ww:text name="'admin.generalconfiguration.allow.unassigned.issues'"/></ww:param>
                        <ww:param name="'value1'"><ww:property value="projectsWithDefaultUnassignedCount"/> <a href="<ww:url page="ViewProjects.jspa"/>"></ww:param>
                        <ww:param name="'value2'"></a></ww:param>
                        <ww:param name="'value3'"><br/></ww:param>
                        <ww:param name="'value4'"><br/></ww:param>
                    </ww:text>
                    </div>
                </ww:if>
            </ww:else>
		</td>
	</tr>

	<tr>
		<td class="fieldLabelArea">
			<ww:text name="'admin.generalconfiguration.external.user.management'"/>
        </td>
		<td class="fieldValueArea">
			<input id="externalUMOn" type="radio" class="radio" value="true" name="externalUM" <ww:if test="externalUM == true">checked="checked"</ww:if>/><label for="externalUMOn"><ww:text name="'admin.common.words.on'"/></label>
			&nbsp;
			<input id="externalUMOff" type="radio" class="radio" value="false" name="externalUM" <ww:if test="externalUM == false">checked="checked"</ww:if>/><label for="externalUMOff"><ww:text name="'admin.common.words.off'"/></label>
			<div class="fieldDescription">
			<ww:text name="'admin.generalconfiguration.external.user.management.description'">
                <ww:param name="'value0'"><br/><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
                <ww:param name="'value2'"><b></ww:param>
                <ww:param name="'value3'"></b></ww:param>
            </ww:text>
			</div>
		</td>
	</tr>

    <tr>
		<td class="fieldLabelArea">
            <ww:text name="'admin.generalconfiguration.logout.confirmation'"/>
        </td>
		<td class="fieldValueArea">
            <input type="radio" class="radio" value="never" id="logout_never" name="logoutConfirm" <ww:if test="logoutConfirm == 'never'">checked="checked"</ww:if>/><label for="logout_never"><ww:text name="'admin.common.words.never'"/></label>
            &nbsp;
            <input type="radio" class="radio" value="cookie" id="logout_cookie" name="logoutConfirm" <ww:if test="logoutConfirm == 'cookie'">checked="checked"</ww:if>/><label for="logout_cookie"><ww:text name="'admin.common.words.cookie'"/></label>
            &nbsp;
		    <input type="radio" class="radio" value="always" id="logout_always" name="logoutConfirm" <ww:if test="logoutConfirm == 'always'">checked="checked"</ww:if>/><label for="logout_always"><ww:text name="'admin.common.words.always'"/></label>
			<div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.logout.confirmation.description'"/>
			</div>
		</td>
	</tr>

    <tr>
		<td class="fieldLabelArea">
			<ww:text name="'admin.generalconfiguration.use.gzip.compression'"/>
        </td>
		<td class="fieldValueArea">
			<input type="radio" class="radio" value="true" name="useGzip" id="useGzipOn" <ww:if test="useGzip == true">checked="checked"</ww:if>/><label for="useGzipOn"><ww:text name="'admin.common.words.on'"/></label>
			&nbsp;
			<input type="radio" class="radio" value="false" name="useGzip" id="useGzipOff" <ww:if test="useGzip == false">checked="checked"</ww:if>/><label for="useGzipOff"><ww:text name="'admin.common.words.off'"/></label>
			<div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.use.gzip.compression.confirmation'"/>
            </div>
		</td>
	</tr>

    <tr>
		<td class="fieldLabelArea">
			<ww:text name="'admin.generalconfiguration.accept.remote.api.calls'"/>
        </td>
		<td class="fieldValueArea">
			<input type="radio" class="radio" value="true" name="allowRpc" id="allowRpcOn" <ww:if test="allowRpc == true">checked="checked"</ww:if>/><label for="allowRpcOn"><ww:text name="'admin.common.words.on'"/></label>
			&nbsp;
			<input type="radio" class="radio" value="false" name="allowRpc" id="allowRpcOff" <ww:if test="allowRpc == false">checked="checked"</ww:if>/><label for="allowRpcOff"><ww:text name="'admin.common.words.off'"/></label>
			<div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.accept.remote.api.calls.description'"/>
            </div>
		</td>
	</tr>

    <tr>
        <td class="fieldLabelArea">
            <ww:text name="'admin.generalconfiguration.user.email.visibility'"/>
        </td>
        <td class="fieldValueArea">
            <input type="radio" class="radio" value="show" id="email_show" name="emailVisibility" <ww:if test="emailVisibility == 'show'">checked="checked"</ww:if>/><label for="email_show"><ww:text name="'admin.generalconfiguration.public'"/></label>
            &nbsp;
            <input type="radio" class="radio" value="hide" id="email_hide" name="emailVisibility" <ww:if test="emailVisibility == 'hide'">checked="checked"</ww:if>/><label for="email_hide"><ww:text name="'admin.generalconfiguration.hidden'"/></label>
            &nbsp;
            <input type="radio" class="radio" value="mask" id="email_mask" name="emailVisibility" <ww:if test="emailVisibility == 'mask'">checked="checked"</ww:if>/><label for="email_mask"><ww:text name="'admin.generalconfiguration.masked'"><ww:param name="value0">user at example dot com</ww:param></ww:text></label>
            &nbsp;
            <input type="radio" class="radio" value="user" id="email_user" name="emailVisibility" <ww:if test="emailVisibility == 'user'">checked="checked"</ww:if>/><label for="email_user"><ww:text name="'admin.generalconfiguration.logged.in.only'"/></label>
            <div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.user.email.visibility.description'"/>
            </div>
        </td>
    </tr>


    <tr>
        <td class="fieldLabelArea">
            <ww:text name="'admin.generalconfiguration.commentlevel.visibility'"/>
        </td>
        <td class="fieldValueArea">
            <input type="radio" class="radio" value="true" id="groupVisibilityOn" name="groupVisibility" <ww:if test="groupVisibility == true">checked="checked"</ww:if>/><label for="groupVisibilityOn"><ww:text name="'admin.generalconfiguration.commentlevel.visibility.both'"/></label>
            &nbsp;
            <input type="radio" class="radio" value="false" id="groupVisibilityOff" name="groupVisibility" <ww:if test="groupVisibility == false">checked="checked"</ww:if>/><label for="groupVisibilityOff"><ww:text name="'admin.generalconfiguration.commentlevel.visibility.rolesonly'"/></label> &nbsp;
            <div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.commentlevel.visibility.description'"/>
            </div>
        </td>
    </tr>

    <tr>
		<td class="fieldLabelArea">
			<ww:text name="'admin.generalconfiguration.enable.email.header'">
			    <ww:param name="'value0'"><br/>&nbsp;&nbsp;</ww:param>
			    <ww:param name="'value1'">Precedence: bulk</ww:param>
			</ww:text>
        </td>
		<td class="fieldValueArea">
			<input type="radio" class="radio" value="true" name="excludePrecedenceHeader" id="excludePrecedenceHeaderOn" <ww:if test="excludePrecedenceHeader == true">checked="checked"</ww:if>/><label for="excludePrecedenceHeaderOn"><ww:text name="'admin.common.words.on'"/></label>
			&nbsp;
			<input type="radio" class="radio" value="false" name="excludePrecedenceHeader" id="excludePrecedenceHeaderOff" <ww:if test="excludePrecedenceHeader == false">checked="checked"</ww:if>/><label for="excludePrecedenceHeaderOff"><ww:text name="'admin.common.words.off'"/></label>
			<div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.enable.email.header.description'">
                    <ww:param name="'value0'"><br/></ww:param>
                    <ww:param name="'value1'">Precedence: bulk</ww:param>
                </ww:text>
            </div>
		</td>
	</tr>

    <tr>
		<td class="fieldLabelArea">
			<ww:text name="'admin.generalconfiguration.enable.ajax.issue.picker'" />
        </td>
		<td class="fieldValueArea">
			<input type="radio" class="radio" value="true" name="ajaxIssuePicker" id="ajaxIssuePickerOn" <ww:if test="ajaxIssuePicker == true">checked="checked"</ww:if>/><label for="ajaxIssuePickerOn"><ww:text name="'admin.common.words.on'"/></label>
			&nbsp;
			<input type="radio" class="radio" value="false" name="ajaxIssuePicker" id="ajaxIssuePickerOff" <ww:if test="ajaxIssuePicker == false">checked="checked"</ww:if>/><label for="ajaxIssuePickerOff"><ww:text name="'admin.common.words.off'"/></label>
			<div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.enable.ajax.issue.picker.description'">
                    <ww:param name="'value0'"><br/></ww:param>
                </ww:text>
            </div>
		</td>
	</tr>
    <tr>
		<td class="fieldLabelArea">
			<ww:text name="'admin.generalconfiguration.enable.ajax.user.full.search'" />
        </td>
		<td class="fieldValueArea">
			<input type="radio" class="radio" value="true" name="ajaxUserPicker" id="ajaxUserPickerOn" <ww:if test="ajaxUserPicker == true">checked="checked"</ww:if>/><label for="ajaxUserPickerOn"><ww:text name="'admin.common.words.on'"/></label>
			&nbsp;
			<input type="radio" class="radio" value="false" name="ajaxUserPicker" id="ajaxUserPickerOff" <ww:if test="ajaxUserPicker == false">checked="checked"</ww:if>/><label for="ajaxUserPickerOff"><ww:text name="'admin.common.words.off'"/></label>
			<div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.enable.ajax.user.full.search.description'">
                    <ww:param name="'value0'"><br/></ww:param>
                </ww:text>
            </div>
		</td>
	</tr>
    <tr>
		<td class="fieldLabelArea">
			<ww:text name="'admin.generalconfiguration.enabled.jql.autocomplete'" />
        </td>
		<td class="fieldValueArea">
			<input type="radio" class="radio" value="false" name="jqlAutocompleteDisabled" id="jqlAutocompleteOn" <ww:if test="jqlAutocompleteDisabled == false">checked="checked"</ww:if>/><label for="jqlAutocompleteOn"><ww:text name="'admin.common.words.on'"/></label>
			&nbsp;
			<input type="radio" class="radio" value="true" name="jqlAutocompleteDisabled" id="jqlAutocompleteOff" <ww:if test="jqlAutocompleteDisabled == true">checked="checked"</ww:if>/><label for="jqlAutocompleteOff"><ww:text name="'admin.common.words.off'"/></label>
			<div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.enabled.jql.autocomplete.description'"/>
            </div>
		</td>
	</tr>

    <ui:select label="text('admin.generalconfiguration.ie.mime.sniffing')" name="'ieMimeSniffer'" template="radiomap.jsp"
        list="/validMimeSnifferOptions" listKey="'key'" listValue="'value'">
        <ui:param name="'description'"><ww:text name="'admin.generalconfiguration.ie.mime.sniffing.description'"/></ui:param>
    </ui:select>

    <tr>
        <td class="fieldLabelArea">
            <ww:text name="'admin.generalconfiguration.show.contact.administrators.form'" />
        </td>
        <td class="fieldValueArea">
            <input type="radio" class="radio" value="true" name="showContactAdministratorsForm" id="showContactAdministratorsFormOn" <ww:if test="/hasMailServer == false">disabled="disabled"</ww:if> <ww:if test="showContactAdministratorsForm == true">checked="checked"</ww:if>/><label for="showContactAdministratorsFormOn"><ww:text name="'admin.common.words.on'"/></label>
            &nbsp;
            <input type="radio" class="radio" value="false" name="showContactAdministratorsForm" id="showContactAdministratorsFormOff" <ww:if test="showContactAdministratorsForm == false">checked="checked"</ww:if>/><label for="showContactAdministratorsFormOff"><ww:text name="'admin.common.words.off'"/></label>
            <div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.show.contact.administrators.form.description'"/>
            </div>
            <ww:if test="/hasMailServer == false">
                <ww:if test="/systemAdministrator == true">
                    <ww:text name="'admin.email.to.configure.mail.server'">
                        <ww:param name="'value0'"><a id="configure_mail_server" href="ViewMailServers.jspa"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </ww:if>
                <ww:else>
                    <ww:text name="'admin.email.to.configure.mail.server.admin'">
                        <ww:param name="'value0'"> </ww:param>
                        <ww:param name="'value1'"> </ww:param>
                    </ww:text>
                </ww:else>
            </ww:if>
        </td>
    </tr>
    <tr>
        <td class="fieldLabelArea">
            <ww:text name="'admin.generalconfiguration.contact.administrators.message'" />
        </td>
        <td>
            <ww:property value="/contactAdministratorsMessageEditHtml" escape="false"/>
            <div class="fieldDescription">
                <ww:text name="'admin.generalconfiguration.contact.administrators.message.description'"/>
            </div>
        </td>
    </tr>

</page:applyDecorator>
</body>
</html>
