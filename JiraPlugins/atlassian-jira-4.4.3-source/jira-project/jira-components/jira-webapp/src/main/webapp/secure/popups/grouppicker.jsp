<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'grouppicker.title'" /></title>
</head>
<body>
<h3><ww:text name="'grouppicker.title'" /></h3>
<%@ include file="/includes/js/multipickerutils.jsp" %>

<ul id="params" style="display:none">
    <li id="openElement"><ww:property value="$element" /></li>
</ul>

<%--d = <ww:property value="element"/>--%>
<ww:if test="permission==true">
    <page:applyDecorator name="jiraform">
		<%--	<page:param name="title">UserBrowser</page:param>--%>
		<page:param name="description">
            <ww:text name="'grouppicker.displayinggroups'" value0="niceStart" value1="niceEnd" value2="groups/size" />
		</page:param>
		<page:param name="width">100%</page:param>
		<page:param name="action">GroupPickerBrowser.jspa</page:param>
		<page:param name="columns">6</page:param>
		<page:param name="autoSelectFirst">false</page:param>
		<script type="text/javascript">
			<%= "<!" + "--" %>
			function select(value)
			{
				opener.AJS.$('#'+AJS.$.trim(AJS.$("#openElement").text())).val(value);
				window.close();
			}
			<%= "//--" + ">" %>
		</script>
		<ww:property value="filter">
		<tr bgcolor="#ffffff">
			<td bgcolor="#fffff0" align="right"><b><ww:text name="'grouppicker.groupsperpage'" />:</b></td>
                <ui:select label="'Groups Per Page'" name="'max'" theme="'single'" list="/maxValues" listKey="'.'" listValue="'.'" >
                    <ui:param name="'headerrow'" value="''" />
                </ui:select>
			<td bgcolor="#fffff0" align="right"><b><ww:text name="'grouppicker.namecontains'" />:</b></td>
			    <ui:textfield label="'Name Contains'" name="'nameFilter'" size="15" theme="'single'" />
			<td>&nbsp;</td>
			<td>
				<input type="submit" value="<ww:text name="'userpicker.filter'" />">
				<ui:component name="'element'" template="hidden.jsp" />
                <ui:component name="'multiSelect'" template="hidden.jsp" />
                <ui:component name="'start'" template="hidden.jsp" />
                <ui:component name="'previouslySelected'" template="hidden.jsp" />
			</td>
		</tr>
		</ww:property>
    </page:applyDecorator>
    <br>
    <ww:if test="/multiSelect == true">
        <table width="100%">
            <tr><td align="center">
                <input type="submit" value="<ww:text name="'common.words.select'"/>" onclick="selectUsers(AJS.$.trim(AJS.$('#openElement').text()))">
            </td></tr>
        <form name="selectorform">
        <tr><td>
    </ww:if>
    <table bgcolor="#bbbbbb" border="0" cellpadding="0" cellspacing="0" width=100%><tr><td>
    <table  border="0" cellpadding="1" cellspacing="1" width=100%>
        <%-- table header --%>
        <tr bgcolor="#f0f0f0">
            <ww:if test="multiSelect == true">
                <td width="1%"><input type="checkbox" name="all" onClick="setCheckboxes()"></td>
            </ww:if>
            <td class="colHeaderLink" style="text-align: left;"><font size=2><b><ww:text name="'grouppicker.groupname'" /></b></font></td>
        </tr>
        <%-- table body --%>
        <ww:iterator value="currentPage" status="'status'">

        <div id="groupname_<ww:property value="@status/index"/>" value="<ww:property value="name"/>" style="visibility: hidden"/>

        <tr <ww:if test="@status/even == true">class="rowNormal"</ww:if><ww:else>class="rowAlternate"</ww:else>
                onmouseover="rowHover(this)" <ww:if test="/multiSelect == false">onclick="select(getElementById('groupname_<ww:property value="@status/index"/>').getAttribute('value'));"</ww:if>
                title="<ww:text name="'picker.click.to.select'"><ww:param name="'value0'"><ww:property value="name"/></ww:param></ww:text>"
        >
            <ww:if test="/multiSelect == true">
                <td valign="top"><input <ww:if test="wasPreviouslySelected(.) == true"> checked="checked"</ww:if> type=checkbox name="userchecks" value="<ww:property value="."/>" id="group_<ww:property value="@status/index"/>" onclick="processCBClick(event, this);"></td>
                <td valign="top" style="text-align: left;" onclick="toggleCheckBox(event, 'group_<ww:property value="@status/index"/>')"><font size="2"><ww:property value="name"/></font></td>
            </ww:if>
            <ww:else>
                <td valign="top" style="text-align: left;"><font size="2"><ww:property value="name"/> </font></td>
            </ww:else>
        </tr>
        </ww:iterator>
    </table>
    </td></tr></table>
    <ww:if test="/multiSelect == true">
        </form>
        <tr><td align="center"><input type="submit" value="<ww:text name="'common.words.select'"/>" onclick="selectUsers(AJS.$.trim(AJS.$('#openElement').text()))"></td></tr>
        </table>
    </ww:if>

    <jsp:include page="userpicker_navigation.jsp"/>
</ww:if>
<ww:else>
    <ww:text name="'userpicker.nopermissions'" />
</ww:else>
</body>
</html>
