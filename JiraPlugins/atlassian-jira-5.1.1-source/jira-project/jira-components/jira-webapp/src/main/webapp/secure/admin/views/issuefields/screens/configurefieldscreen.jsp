<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.screens.configure.screen'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screens"/>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.issuefields.screens.configure.screen'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">fieldscreens</page:param>
    <page:param name="postTitle">
        <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
            <ui:param name="'projects'" value="/usedIn"/>
        </ui:component>
    </page:param>
    <p>
        <ww:text name="'admin.issuefields.screens.configure.main.page.description'">
            <ww:param name="'value0'"><b id="screenName"><ww:property value="/fieldScreen/name" /></b></ww:param>
        </ww:text>
    </p>

    <p>
        <ww:if test="/tabsAllowed == true">
            <ww:text name="'admin.issuefields.screens.configure.page.description.a'"/>
        </ww:if>
        <ww:else>
            <ww:text name="'admin.issuefields.screens.configure.page.description.b'"/>
        </ww:else>
    </p>
    <p>
        <ww:text name="'admin.issuefields.screens.configure.note'">
            <ww:param name="'value0'"><span class="warning"></ww:param>
            <ww:param name="'value1'"></span></ww:param>
        </ww:text>
    </p>
    <ul class="optionslist">
        <li><ww:text name="'admin.issuefields.screens.configure.view.all'">
            <ww:param name="'value0'"><b><a id="view_screen" href="ViewFieldScreens.jspa"></ww:param>
            <ww:param name="'value1'"></a></b></ww:param>
        </ww:text></li>
    </ul>
</page:applyDecorator>



<ww:if test="/fieldScreen && /fieldScreen/tabs/empty == false">
    <ww:if test="/tabsAllowed == true">
    <%-- Show tab headings --%>
        <div class="tabwrap tabs2" id="configureScreenTabs">
            <ul class="tabs horizontal">
            <ww:iterator value="/fieldScreen/tabs" status="'status'">
                <ww:if test="@status/index == /tabPosition">
                    <li class="active">
                        <strong>
                        <ww:if test="@status/first == false">
                            <a class="icon" style="background-image:url('<%= request.getContextPath()%>/images/icons/arrow_left_small.gif');float:left;margin-right:4px;" id="move_tableft" href="MoveFieldScreenTabLeft.jspa?id=<ww:property value="/id" />&tabPosition=<ww:property value="/tabPosition" />" title="<ww:text name="'admin.issuefields.screens.configure.move.tab.left'"/>">
                                <%--<img src="<%= request.getContextPath()%>/images/icons/arrow_left_small.gif" height="16" width="16" border="0" >--%>

                                </a>
                        </ww:if>

                        <ww:if test="@status/last == false">
                            <a class="icon" style="background-image:url('<%= request.getContextPath()%>/images/icons/arrow_right_small.gif');float:right;margin-left:4px;" id="move_tabright" href="MoveFieldScreenTabRight.jspa?id=<ww:property value="/id" />&tabPosition=<ww:property value="/tabPosition" />" title="<ww:text name="'admin.issuefields.screens.configure.move.tab.right'"/>">
                                <%--<img src="<%= request.getContextPath()%>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" >--%>

                                </a>
                        </ww:if>
                            <ww:property value="./name"/>
                        </strong>
                    </li>
                    </ww:if>
                <ww:else>
                    <li>
                        <a href="ConfigureFieldScreen.jspa?id=<ww:property value="/id" />&tabPosition=<ww:property value="@status/index" />"><strong><ww:property value="./name" /></strong></a>
                    </li>
                </ww:else>
            </ww:iterator>
            </ul>
        </div>
    </ww:if>

    <%-- Show the tab --%>
    <ww:property value="/tab">
        <ww:if test="/tabsAllowed == true">
        <div id="field_tab_table">
            <%-- Show the tab's operations --%>
            <form id="renameTabForm" name="ranameTabForm" action="RenameFieldScreenTab.jspa" method="post" class="aui">
                <ww:component name="'atl_token'" value="/xsrfToken" template="hidden.jsp"/>
                <ul class="operations-list">
                    <li><ww:text name="'admin.issuefields.screens.configure.delete.tab'">
                        <ww:param name="'value0'"><b><a id="delete_fieldscreentab" href="ViewDeleteFieldScreenTab.jspa?id=<ww:property value="/id" />&tabPosition=<ww:property value="/tabPosition" />"></ww:param>
                        <ww:param name="'value1'"></a></b></ww:param>
                        <ww:param name="'value2'">
                            <ww:property value="/tabName" />
                        </ww:param>
                    </ww:text></li>
                    <li>
                        <ww:text name="'admin.issuefields.screens.configure.rename.tab'">
                            <ww:param name="'value0'">
                                <ww:property value="/tabName" />
                            </ww:param>
                            <ww:param name="'value1'"><input type="text" id="tabName" name="tabName" value="<ww:property value="/tabName" />">
                                <input type="submit" id="rename_tab_submit" value="<ww:text name="'admin.common.words.ok'"/>" class="subtleButton" />
                                <ww:if test="/errors/('tabName')"> - <span class="warning"><ww:property value="/errors/('tabName')" /></span></ww:if>
                            </ww:param>
                        </ww:text>
                    </li>
                </ul>
                <input type="hidden" name="id" value="<ww:property value="/id" />">
                <input type="hidden" name="tabPosition" value="<ww:property value="/tabPosition" />">
            </form>
        </ww:if>

        <ww:if test="./fieldScreenLayoutItems/empty == false">
        <form id="tabForm" name="tabFormOperations" action="ConfigureFieldScreenTab.jspa" method="post">
            <ww:component name="'atl_token'" value="/xsrfToken" template="hidden.jsp"/>
            <table id="field_table" class="aui aui-table-rowhover">
                <thead>
                    <tr>
                        <th width="1%">
                            <ww:text name="'admin.issuefields.screens.configure.position'"/>
                        </th>
                        <th>
                            <ww:text name="'common.words.name'"/>
                        </th>
                        <ww:if test="/tab/fieldScreenLayoutItems/size > 1">
                            <th width="1%">
                                <ww:text name="'admin.issuefields.screens.configure.order'"/>
                            </th>
                            <th width="5%">
                                <ww:text name="'admin.issuefields.screens.configure.move.to.position'"/>
                            </th>
                        </ww:if>
                        <ww:if test="/fieldScreen/tabs/size > 1 && /tab/fieldScreenLayoutItems/empty == false">
                            <th width="5%">
                                <ww:text name="'admin.issuefields.screens.configure.move.to.tab'"/>
                            </th>
                        </ww:if>
                        <ww:if test="/tab/fieldScreenLayoutItems/empty == false">
                            <th width="1%">
                                <ww:text name="'admin.common.words.remove'"/>
                            </th>
                        </ww:if>
                    </tr>
                </thead>
                <tbody>

                <ww:iterator value="./fieldScreenLayoutItems" status="'status'">
                    <tr class="<ww:if test="/hlFields/contains(./orderableField/id) == true">rowHighlighted</ww:if><ww:elseIf test="@status/odd == true">rowNormal</ww:elseIf><ww:else>rowAlternate</ww:else>">
                        <td>
                            <ww:property value="@status/count" />.
                        </td>
                        <td>
                            <ww:if test="./orderableField">
                                <b><ww:property value="./orderableField/name"/></b>
                            </ww:if>
                            <ww:else>
                                <span class="warning"><ww:text name="'admin.issuefields.screens.configure.invalid.or.disabled.field'">
                                    <ww:param name="'value0'"><ww:property value="./fieldId" /></ww:param>
                                </ww:text></span>
                            </ww:else>
                        </td>
                        <ww:if test="../fieldScreenLayoutItems/size > 1">
                            <td nowrap>
                                <ww:if test="@status/first == false">
                                <a id="moveToFirst_<ww:property value="./orderableField/id" />" href="FieldScreenLayoutItemFirst.jspa?id=<ww:property value="/id" />&tabPosition=<ww:property value="/tabPosition"/>&fieldPosition=<ww:property value="@status/index"/>"><img src="<%= request.getContextPath() %>/images/icons/arrow_first.gif" border="0" width="16" height="16" title="<ww:text name="'admin.issuefields.screens.configure.move.field.to.first'"/>"></a>
                                <a id="moveUp_<ww:property value="./orderableField/id" />" href="FieldScreenLayoutItemUp.jspa?id=<ww:property value="/id" />&tabPosition=<ww:property value="/tabPosition"/>&fieldPosition=<ww:property value="@status/index"/>"><img src="<%= request.getContextPath() %>/images/icons/arrow_up_blue.gif" border="0" width="16" height="16" title="<ww:text name="'admin.issuefields.screens.configure.move.field.up'"/>"></a></ww:if>
                                <ww:else><image src="<%= request.getContextPath() %>/images/border/spacer.gif" border=0 width=13 height=14><image src="<%= request.getContextPath() %>/images/border/spacer.gif" border="0" width="20" height="16"></ww:else>
                                <ww:if test="@status/last != true">
                                <a id="moveDown_<ww:property value="./orderableField/id" />" href="FieldScreenLayoutItemDown.jspa?id=<ww:property value="/id" />&tabPosition=<ww:property value="/tabPosition"/>&fieldPosition=<ww:property value="@status/index"/>"><img src="<%= request.getContextPath() %>/images/icons/arrow_down_blue.gif" border="0" width="16" height="16" title="<ww:text name="'admin.issuefields.screens.configure.move.field.down'"/>"></a>
                                <a id="moveToLast_<ww:property value="./orderableField/id" />" href="FieldScreenLayoutItemLast.jspa?id=<ww:property value="/id" />&tabPosition=<ww:property value="/tabPosition"/>&fieldPosition=<ww:property value="@status/index"/>"><img src="<%= request.getContextPath() %>/images/icons/arrow_last.gif" border="0" width="16" height="16" title="<ww:text name="'admin.issuefields.screens.configure.move.field.to.last'"/>"></a></ww:if>
                                <ww:else><image src="<%= request.getContextPath() %>/images/border/spacer.gif" border=0 width=13 height=14><image src="<%= request.getContextPath() %>/images/border/spacer.gif" border="0" width="20" height="16"></ww:else>
                            </td>

                            <ui:textfield name="/newPositionTextBoxName(@status/index)" label="text('admin.issuefields.screens.configure.new.field.position')" theme="'single'" value="/newPositionValue(@status/index)" size="'4'">
                                <ui:param name="'class'" value="'fullyCentered'" />
                            </ui:textfield>

                        </ww:if>
                        <ww:if test="/fieldScreen/tabs/size > 1">
                            <td align="center">
                                <select name="<ww:property value="/destinationTabBoxName(@status/index)" />">
                                    <option value="" selected ><ww:text name="'admin.issuefields.screens.configure.select.tab'"/></option>
                                    <ww:iterator value="/destinationTabs">
                                        <option value="<ww:property value="./position" />"><ww:property value="./name" /></option>
                                    </ww:iterator>
                                </select>
                            </td>
                        </ww:if>
                        <td style="text-align:center;">
                            <input id="removebox_<ww:property value="./orderableField/id"/>" type="checkbox" name="<ww:property value="/removeFieldBoxName(@status/index)" />">
                        </td>
                    </tr>
                </ww:iterator>
                    <tr class="totals">
                        <td colspan="<ww:property value="/buttonRowSize" />">&nbsp;<input type="hidden" name="tabPosition" value="<ww:property value="/tabPosition" />"><input type="hidden" name="id" value="<ww:property value="/id" />"></td>
                        <ww:if test="./fieldScreenLayoutItems/size > 1"><td><input class="aui-button" type="submit" name="moveFieldsToPosition" value="<ww:text name="'common.forms.move'"/>"></td></ww:if>
                        <ww:if test="/fieldScreen/tabs/size > 1"><td><input class="aui-button" type="submit" name="moveFieldsToTab" value="<ww:text name="'common.forms.move'"/>"></td></ww:if>
                        <ww:if test="./fieldScreenLayoutItems/empry == false"></ww:if><td><input class="aui-button" type="submit" name="deleteFieldsFromTab" value="<ww:text name="'admin.common.words.remove'"/>"></td>
                    </tr>
                </tbody>
            </table>
        </form>
        </ww:if>
        <ww:else>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.screens.configure.no.fields'"/></aui:param>
            </aui:component>
        </ww:else>
    </ww:property>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.screens.configure.no.screens'"/></aui:param>
    </aui:component>
</ww:else>

<ww:if test="/fieldScreen">
    <table border="0" width="100%">
        <tr>
            <ww:if test="/fieldScreen/tabs/empty == false">
            <%-- Add Fields to Tab Form --%>
            <td width="47%" valign="top">
                <ww:if test="/addableFields/empty == false">
                <page:applyDecorator name="jiraform">
                    <page:param name="action">AddFieldScreenLayoutItem.jspa</page:param>
                    <page:param name="submitId">add_field_submit</page:param>
                    <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
                    <page:param name="title"><ww:text name="'admin.issuefields.screens.configure.add.field'"/></page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="description">
                    <ww:if test="/tabsAllowed == true">
                        <ww:text name="'admin.issuefields.screens.configure.add.field.description.tab'">
                            <ww:param name="'value0'"><b><ww:property value="/tab/name" /></b></ww:param>
                        </ww:text>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.issuefields.screens.configure.add.field.description.screen'">
                            <ww:param name="'value0'"><b><ww:property value="/fieldScreen/name" /></b></ww:param>
                        </ww:text>
                    </ww:else>
                    </page:param>

                    <ui:select label="text('admin.issuefields.screens.configure.fields.to.add')" name="'fieldId'" list="/addableFields" listKey="'id'" listValue="'/fieldName(.)'" template="selectmultiple.jsp">
                        <ww:param name="'size'"><ww:if test="/addableFields/size < 2">2</ww:if><ww:elseIf test="/addableFields/size < 5"><ww:property value="/addableFields/size" /></ww:elseIf><ww:else>5</ww:else></ww:param>
                    </ui:select>

                    <ui:textfield label="text('admin.issuefields.screens.configure.position')" name="'fieldPosition'" >
                        <ui:param name="'description'"><ww:if test="/tabsAllowed == true"><ww:text name="'admin.issuefields.screens.configure.leave.blank.a'"/></ww:if>
                            <ww:else><ww:text name="'admin.issuefields.screens.configure.leave.blank.b'"/></ww:else></ui:param>
                    </ui:textfield>

                    <%-- Field Screen id --%>
                    <ui:component name="'id'" id="'screenId'" value="/fieldScreen/id" template="hidden.jsp" theme="'single'"/>

                    <%-- Field Screen Tab position --%>
                    <ui:component id="'screenId'" name="'tabPosition'" template="hidden.jsp" theme="'single'" />
                </page:applyDecorator>
                </ww:if>
                <ww:else>
                     <page:applyDecorator name="jirapanel">
                        <page:param name="title"><ww:text name="'admin.issuefields.screens.configure.add.field.to.tab'"/></page:param>
                        <page:param name="width">100%</page:param>
                        <p><ww:text name="'admin.issuefields.screens.configure.no.fields.to.add'"/></p>
                    </page:applyDecorator>
                </ww:else>
            </td>
            </ww:if>
            <ww:if test="/tabsAllowed == true">
            <td width="5%">&nbsp;</td>
            <%-- Add New tab Form --%>
            <td width="47%" valign="top">
                <page:applyDecorator name="jiraform">
                    <page:param name="action">AddFieldScreenTab.jspa</page:param>
                    <page:param name="submitId">add_tab_submit</page:param>
                    <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
                    <page:param name="title"><ww:text name="'admin.issuefields.screens.configure.add.new.tab'"/></page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="formName">jiraTabForm</page:param>
                    <page:param name="description">
                        <ww:text name="'admin.issuefields.screens.configure.add.new.tab.to.screen'">
                            <ww:param name="'value0'"><b><ww:property value="/fieldScreen/name" /></b></ww:param>
                        </ww:text>
                    </page:param>

                    <ui:textfield label="text('common.words.name')" name="'newTabName'" >
                        <ui:param name="'mandatory'" value="true" />
                    </ui:textfield>

                    <%-- Field Screen id --%>
                    <ui:component name="'id'" id="'screenId'" value="/fieldScreen/id" template="hidden.jsp" theme="'single'" />

                    <%-- Field Screen Tab position to return to incase of error--%>
                    <ui:component name="'tabPosition'" template="hidden.jsp" theme="'single'" />
                </page:applyDecorator>
            </td>
            </ww:if>
        </tr>
    </table>
</ww:if>
<ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
    <ui:param name="'projects'" value="/usedIn"/>
    <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.screen'"/></ui:param>
</ui:component>
</body>
</html>
