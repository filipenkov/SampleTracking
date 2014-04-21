<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/user_interface"/>
    <meta name="admin.active.tab" content="lookandfeel"/>
	<title><ww:text name="'admin.globalsettings.lookandfeel.look.and.feel.configuration'"/></title>
</head>
<body>
<ww:if test="/refreshResourcesPerformed == true">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'admin.globalsettings.lookandfeel.refresh.resources.performed'"/></p>
        </aui:param>
    </aui:component>
</ww:if>

<div class="module">
    <ui:component name="'lookandfeel.logoandcolours'" template="help.jsp">
        <ui:param name="'helpURLFragment'" value = "'#logo'"/>
    </ui:component>
    <h3 class="formtitle"><ww:text name="'admin.globalsettings.lookandfeel.logo'"/></h3>
    <p><ww:text name="'admin.globalsettings.lookandfeel.logo.desc'"/></p>
    <table id="lookAndFeelLogo" class="aui aui-table-rowhover">
        <tbody>
            <%-- NOTE: Keep these property keys the same as APKeys - can't use statics in WW --%>
            <tr>
                <td width="30%"><b><ww:text name="'admin.globalsettings.lookandfeel.preview'"/></b></td>
                <td width="70%">
                    <ww:if test="/logoUrlWithContext() != null" >
                       <img class="logo-preview" src="<ww:property value="/logoUrlWithContext()"/>" width="<ww:property value="/logoWidth()"/>" height="<ww:property value="/logoHeight()"/>" />
                    </ww:if>
                </td>
            </tr>
            <tr>
                <td width="30%"><b><ww:text name="'admin.globalsettings.lookandfeel.faviconpreview'"/></b></td>
                <td width="70%">
                    <ww:if test="/faviconHiResUrlWithContext() != null" >
                       <img class="logo-preview" src="<ww:property value="/faviconHiResUrlWithContext()"/>" width="<ww:property value="/faviconHiResWidth()"/>" height="<ww:property value="/faviconHiResHeight()"/>" />
                    </ww:if>
                </td>
            </tr>
        </tbody>
    </table>
</div>

<div class="module">
    <ui:component name="'lookandfeel.logoandcolours'" template="help.jsp">
        <ui:param name="'helpURLFragment'" value = "'#colours'"/>
    </ui:component>
    <h3 class="formtitle"><ww:text name="'admin.globalsettings.lookandfeel.colours'"/></h3>
    <p><ww:text name="'admin.globalsettings.lookandfeel.colours.desc'"/></p>
    <table id="lookAndFeelColors" class="aui aui-table-rowhover">
        <tbody>
            <tr>
                <td width=30%><b><ww:text name="'admin.globalsettings.lookandfeel.top.bar.colour'"/></b></td>
                <td width=70%><ww:property value="/color('jira.lf.top.bgcolour')" escape="false"/></td>
            </tr>
            <tr>
                <td width=30%><b><ww:text name="'admin.globalsettings.lookandfeel.top.bar.hilightcolour'"/></b></td>
                <td width=70%><ww:property value="/color('jira.lf.top.hilightcolour')" escape="false"/></td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.globalsettings.lookandfeel.top.text.colour'"/></b></td>
                <td><ww:property value="/color('jira.lf.top.textcolour')" escape="false"/>
                    <ww:if test="/showInvisibleWarningForTopText == true">
                        &nbsp;&nbsp;<span class="warning"><ww:text name="'admin.globalsettings.lookandfeel.toptext.samecolour'"/></span>
                    </ww:if>
                </td>
            </tr>
            <tr>
                <td width=30%><b><ww:text name="'admin.globalsettings.lookandfeel.top.text.hilightcolour'"/></b></td>
                <td width=70%><ww:property value="/color('jira.lf.top.texthilightcolour')" escape="false"/></td>
            </tr>
            <tr>
                <td width=30%><b><ww:text name="'admin.globalsettings.lookandfeel.top.separatorcolor'"/></b></td>
                <td width=70%><ww:property value="/color('jira.lf.top.separator.bgcolor')" escape="false"/></td>
            </tr>
            <!-- =========== MENU BAR =================== -->
            <tr>
                <td><b><ww:text name="'admin.globalsettings.lookandfeel.menu.bar.colour'"/></b></td>
                <td><ww:property value="/color('jira.lf.menu.bgcolour')" escape="false"/></td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.globalsettings.lookandfeel.menu.bar.text.colour'"/></b></td>
                <td><ww:property value="/color('jira.lf.menu.textcolour')" escape="false"/></td>
            </tr>

            <tr>
                <td><b><ww:text name="'admin.globalsettings.lookandfeel.menu.bar.separator.colour'"/></b></td>
                <td><ww:property value="/color('jira.lf.menu.separator')" escape="false"/></td>
            </tr>
            <!-- =========== TEXT / LINK / HEADING =================== -->
            <tr>
                <td><b><ww:text name="'admin.globalsettings.lookandfeel.link.colour'"/></b></td>
                <td><ww:property value="/color('jira.lf.text.linkcolour')" escape="false"/></td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.globalsettings.lookandfeel.link.active.colour'"/></b></td>
                <td><ww:property value="/color('jira.lf.text.activelinkcolour')" escape="false"/></td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.globalsettings.lookandfeel.heading.colour'"/></b></td>
                <td><ww:property value="/color('jira.lf.text.headingcolour')" escape="false"/></td>
            </tr>
        </tbody>
    </table>
</div>

<div class="module">
    <ui:component name="'lookandfeel.logoandcolours'" template="help.jsp">
        <ui:param name="'helpURLFragment'" value = "'#gadgetchromecolours'"/>
    </ui:component>
    <h3 class="formtitle"><ww:text name="'admin.globalsettings.lookandfeel.gadget.chrome.colors'"/></h3>
    <p><ww:text name="'admin.globalsettings.lookandfeel.gadget.chrome.colors.desc'"/></p>
    <table id="lookAndFeelGadgetChromeColours" class="aui aui-table-rowhover">
        <tbody>
        <ww:iterator value="/gadgetColors" status="'status'">
            <tr bgcolor="<ww:if test="@status/odd == true">#ffffff</ww:if><ww:else>#fffff0</ww:else>">
                <td width=30%><b><ww:text name="'admin.globalsettings.lookandfeel.gadget.colour.' + ."/></b></td>
                <td width=70%><ww:property value="/gadgetColor(.)" escape="false"/></td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
</div>

<div class="module">
    <ui:component name="'lookandfeel.logoandcolours'" template="help.jsp">
        <ui:param name="'helpURLFragment'" value = "'#timeformats'"/>
    </ui:component>
    <h3 class="formtitle"><ww:text name="'admin.globalsettings.lookandfeel.date.time.formats'"/></h3>
    <p>
        <ww:text name="'admin.globalsettings.lookandfeel.date.time.formats.desc'">
            <ww:param><a href="http://download.oracle.com/javase/6/docs/api/index.html?java/text/SimpleDateFormat.html"></ww:param>
            <ww:param></a></ww:param>
        </ww:text>
    </p>
    <table id="lookAndFeelFormats" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th width=30%>&nbsp;</th>
                <th width=30%><ww:text name="'admin.globalsettings.lookandfeel.format'"/></th>
                <th width=40%><ww:text name="'admin.globalsettings.lookandfeel.example'"/></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td><b><ww:text name="'admin.globalsettings.lookandfeel.time.format'"/></b></td>
                <td><ww:property value="applicationProperties/defaultBackedString('jira.lf.date.time')"  escape="false" /></td>
                <td><ww:property value="/outlookDate/formatTime(currentTimestamp)" /></td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.globalsettings.lookandfeel.day.format'"/></b></td>
                <td><ww:property value="applicationProperties/defaultBackedString('jira.lf.date.day')"  escape="false" /></td>
                <td><ww:property value="/outlookDate/formatDay(currentTimestamp)" /></td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.globalsettings.lookandfeel.complete.date.time.format'"/></b></td>
                <td><ww:property value="applicationProperties/defaultBackedString('jira.lf.date.complete')"  escape="false" /></td>
                <td><ww:property value="/outlookDate/formatDMYHMS(currentTimestamp)" /></td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.globalsettings.lookandfeel.day.month.year.format'"/></b></td>
                <td><ww:property value="applicationProperties/defaultBackedString('jira.lf.date.dmy')"  escape="false" /></td>
                <td><ww:property value="/outlookDate/formatDMY(currentTimestamp)" /></td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.globalsettings.lookandfeel.date.time.picker.useISO8601'"/></b></td>
                <td colspan="2">
                    <ww:if test="/useISO8601 == true">
                        <span class="status-active"><ww:text name="'admin.common.words.on'"/></span>
                    </ww:if>
                    <ww:else>
                        <span class="status-inactive"><ww:text name="'admin.common.words.off'"/></span>
                    </ww:else>
                </td>
            </tr>
        </tbody>
    </table>
</div>

<div class="module">
    <ww:if test="/hasUserFormatsToEdit == true">
        <div id="userFormats">
            <ui:component name="'lookandfeel.logoandcolours'" template="help.jsp">
                <ui:param name="'helpURLFragment'" value = "'#userformats'"/>
            </ui:component>
            <h3 class="formtitle"><ww:text name="'admin.globalsettings.lookandfeel.user.formats'"/></h3>
            <table id="userformat" class="aui aui-table-rowhover">
                <thead>
                    <tr>
                        <th width=30%><ww:text name="'admin.globalsettings.lookandfeel.type'"/></th>
                        <th width=30%><ww:text name="'admin.globalsettings.lookandfeel.format'"/></th>
                        <th width=40%><ww:text name="'admin.globalsettings.lookandfeel.example'"/></th>
                    </tr>
                </thead>
                <tbody>
                <ww:iterator value="/userFormatTypes" status="'status'">
                    <tr>
                        <td><b><ww:property value="/userFormatTypeName(.)" /></b></td>
                        <td><ww:property value="/userFormatName(.)"/>
                            <ww:if test="/userFormatTypeDesc(.) != null">
                                <div class="description">(<ww:property value="/userFormatTypeDesc(.)"/>)</div>
                            </ww:if>
                        </td>
                        <td><ww:property value="/sampleUserFormat(.)" escape="false"/></td>
                    </tr>
                </ww:iterator>
                </tbody>
            </table>
        </div>
    </ww:if>
</div>

<div class="buttons-container">
    <a class="aui-button" href="EditLookAndFeel!default.jspa" id="editlookandfeel" accesskey="E"><ww:text name="'admin.common.phrases.edit.configuration'"/></a>
</div>

<div class="module twixi-block collapsed">
    <div class="twixi-trigger">
        <h5><span class="icon icon-twixi"></span><ww:text name="'admin.globalsettings.lookandfeel.refresh.resources'"/></h5>
    </div>
    <div class="twixi-content">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.globalsettings.lookandfeel.refresh.resources.reason.1'"/></p>
                <p><ww:text name="'admin.globalsettings.lookandfeel.refresh.resources.reason.2'">
                    <ww:param name="'value0'"><a href="<ww:url page="EditLookAndFeel!refreshResources.jspa"/>" id="refreshlookandfeel"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text></p>
            </aui:param>
        </aui:component>
    </div>
</div>
</body>
</html>
