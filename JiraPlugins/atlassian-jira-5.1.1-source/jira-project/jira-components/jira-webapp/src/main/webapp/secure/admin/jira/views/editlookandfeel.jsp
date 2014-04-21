<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/user_interface"/>
    <meta name="admin.active.tab" content="lookandfeel"/>
	<title><ww:text name="'admin.globalsettings.lookandfeel.edit.look.and.feel.configuration'"/></title>
</head>

<body>
<dl style="display:none;" id="jira.webresource.flushcounter"><ww:property value="/lookAndFeelVersionNumber"/></dl>

<page:applyDecorator name="jiraform">
	<page:param name="action">EditLookAndFeel.jspa</page:param>
	<page:param name="submitId">edit_look_and_feel</page:param>
	<page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
	<page:param name="cancelURI">ViewLookAndFeel.jspa</page:param>
	<page:param name="title"><ww:text name="'admin.globalsettings.lookandfeel.logo'"/></page:param>
	<page:param name="buttons">
        <input class="aui-button" type="button" value="<ww:text name="'admin.common.words.reset.defaults'"/>" onclick="location.href='EditLookAndFeel!reset.jspa?atl_token=' + window.atl_token()" />
    </page:param>

	<ui:textfield label="text('admin.globalsettings.lookandfeel.logo.url')" name="'logoUrl'">
		<ui:param name="'size'">40</ui:param>
		<ui:param name="'description'">
			<ww:text name="'admin.globalsettings.lookandfeel.logo.description'"/>
		</ui:param>
	</ui:textfield>

	<ui:textfield label="text('admin.globalsettings.lookandfeel.logo.width')" name="'logoWidth'">
        <ui:param name="'size'">40</ui:param>
		<ui:param name="'description'">
			<ww:text name="'admin.globalsettings.lookandfeel.logo.width.description'"/>
		</ui:param>
	</ui:textfield>

	<ui:textfield label="text('admin.globalsettings.lookandfeel.logo.height')" name="'logoHeight'">
        <ui:param name="'size'">40</ui:param>
		<ui:param name="'description'">
			<ww:text name="'admin.globalsettings.lookandfeel.logo.height.description'"/>
		</ui:param>
	</ui:textfield>



	<tr>
		<td colspan="2"><h3 class="formtitle"><ww:text name="'admin.globalsettings.lookandfeel.colours'"/></h3></td>
	</tr>

    <!-- =========== MENU BAR =================== -->
	<ui:component label="text('admin.globalsettings.lookandfeel.top.bar.colour')" name="'topBgColour'" template="colorpicker.jsp">
        <ui:param name="'size'">40</ui:param>
    </ui:component>

    <ui:component label="text('admin.globalsettings.lookandfeel.top.bar.hilightcolour')" name="'topHighlightBgColour'" template="colorpicker.jsp">
        <ui:param name="'size'">40</ui:param>
    </ui:component>

    <ui:component label="text('admin.globalsettings.lookandfeel.top.text.colour')" name="'topTextColour'" template="colorpicker.jsp">
        <ui:param name="'size'">40</ui:param>
    </ui:component>

    <ui:component label="text('admin.globalsettings.lookandfeel.top.text.hilightcolour')" name="'topTextHighlightColour'" template="colorpicker.jsp">
        <ui:param name="'size'">40</ui:param>
    </ui:component>

    <ui:component label="text('admin.globalsettings.lookandfeel.top.separatorcolor')" name="'topSeparatorColor'" template="colorpicker.jsp">
        <ui:param name="'size'">40</ui:param>
    </ui:component>

    <!-- =========== MENU BAR =================== -->
	<ui:component label="text('admin.globalsettings.lookandfeel.menu.bar.colour')" name="'menuBgColour'" template="colorpicker.jsp">
        <ui:param name="'size'">40</ui:param>
    </ui:component>

	<ui:component label="text('admin.globalsettings.lookandfeel.menu.bar.text.colour')" name="'menuTextColour'" template="colorpicker.jsp">
        <ui:param name="'size'">40</ui:param>
    </ui:component>

	<ui:component label="text('admin.globalsettings.lookandfeel.menu.bar.separator.colour')" name="'menuSeparatorColour'" template="colorpicker.jsp">
        <ui:param name="'size'">40</ui:param>
    </ui:component>

    <!-- =========== TEXT / LINK / HEADINGS =================== -->
    <ui:component label="text('admin.globalsettings.lookandfeel.link.colour')" name="'linkColour'" template="colorpicker.jsp">
        <ui:param name="'size'">40</ui:param>
    </ui:component>

    <ui:component label="text('admin.globalsettings.lookandfeel.link.active.colour')" name="'linkAColour'" template="colorpicker.jsp">
        <ui:param name="'size'">40</ui:param>
    </ui:component>

    <ui:component label="text('admin.globalsettings.lookandfeel.heading.colour')" name="'headingColour'" template="colorpicker.jsp">
        <ui:param name="'size'">40</ui:param>
    </ui:component>

    <tr>
		<td colspan="2"><h3 class="formtitle"><ww:text name="'admin.globalsettings.lookandfeel.gadget.chrome.colors'"/></h3></td>
	</tr>

    <ww:iterator value="/gadgetColors" status="'status'">
        <ui:component label="text('admin.globalsettings.lookandfeel.gadget.colour.' + .)" name="'gadgetChromeColor' + ." value="/gadgetColor(.)" template="colorpicker.jsp">
            <ui:param name="'size'">40</ui:param>
        </ui:component>
    </ww:iterator>

	<tr>
		<td colspan="2"><h3 class="formtitle"><ww:text name="'admin.globalsettings.lookandfeel.date.time.formats'"/></h3>
        <ww:text name="'admin.globalsettings.lookandfeel.documentation.for.date.time'">
            <ww:param name="'value0'"><a href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text>.
		</td>
	</tr>
	<ui:textfield label="text('admin.globalsettings.lookandfeel.time.format')" name="'formatTime'">
        <ui:param name="'size'">40</ui:param>
        <ui:param name="'description'">
            <ww:text name="'admin.globalsettings.lookandfeel.eg'">
                <ww:param name="value0">hh:mm a (<ww:property value="/outlookDate/formatTime(exampleDate)" />)</ww:param>
            </ww:text>
        </ui:param>
	</ui:textfield>

	<ui:textfield label="text('admin.globalsettings.lookandfeel.day.format')" name="'formatDay'">
        <ui:param name="'size'">40</ui:param>
        <ui:param name="'description'">
            <ww:text name="'admin.globalsettings.lookandfeel.eg'">
                <ww:param name="value0">EEEE hh:mm a (<ww:property value="/outlookDate/formatDay(exampleDate)" />)</ww:param>
            </ww:text>
        </ui:param>
	</ui:textfield>

	<ui:textfield label="text('admin.globalsettings.lookandfeel.complete.date.time.format')" name="'formatComplete'">
        <ui:param name="'size'">40</ui:param>
        <ui:param name="'description'">
            <ww:text name="'admin.globalsettings.lookandfeel.eg'">
                <ww:param name="value0">dd/MMM/yy hh:mm a (<ww:property value="/outlookDate/formatDMYHMS(exampleDate)" />)</ww:param>
            </ww:text>
        </ui:param>
	</ui:textfield>

	<ui:textfield label="text('admin.globalsettings.lookandfeel.day.month.year.format')" name="'formatDMY'">
        <ui:param name="'size'">40</ui:param>
        <ui:param name="'description'">
            <ww:text name="'admin.globalsettings.lookandfeel.eg'">
                <ww:param name="value0">dd/MMM/yy (<ww:property value="/outlookDate/formatDMY(exampleDate)" />)</ww:param>
            </ww:text>
        </ui:param>
	</ui:textfield>

    <ui:component name="'preview'" value="'false'" template="hidden.jsp" theme="'single'"  />

    <ui:checkbox label="text('admin.globalsettings.lookandfeel.date.time.picker.useISO8601')" name="'useISO8601'" fieldValue="'true'">
        <ui:param name="'description'">
            <ww:text name="'admin.globalsettings.lookandfeel.date.time.picker.useISO8601.description'"/>
        </ui:param>
    </ui:checkbox>

    <ww:if test="/hasUserFormatsToEdit == true">
        <tr>
            <td colspan="2"><h3 class="formtitle"><ww:text name="'admin.globalsettings.lookandfeel.user.formats'"/></h3></td>
        </tr>
        <ww:iterator value="/userFormatTypes" status="'status'">
            <ww:if test="/userFormatModuleDescriptorsForType(.)/size > 1">
                <ui:select label="/userFormatTypeName(.)" name="'user_format_for_' + ." list="/userFormatModuleDescriptorsForType(.)" listKey="'completeKey'" listValue="'/userFormatName(.)'"
                        value="/currentUserFormatForType(.)">
                </ui:select>
            </ww:if>
        </ww:iterator>
    </ww:if>

    <tr>
		<td colspan="2">
            <p class="landf-restore">
            <ww:text name="'admin.globalsettings.lookandfeel.set.selection.defaults'"/><br />
            <ww:text name="'admin.globalsettings.lookandfeel.set.all.defaults'"/>
            </p>
		</td>
	</tr>

</page:applyDecorator>

</body>
</html>
