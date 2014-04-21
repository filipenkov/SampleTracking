<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.screens.view.screens'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screens"/>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.issuefields.screens.view.screens'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">fieldscreens</page:param>
    <p>
        <ww:text name="'admin.issuefields.screens.description'"/>
        <ww:text name="'admin.issuefields.screens.the.table.below'"/>
    </p>
    <ul>
        <li>
            <ww:text name="'admin.issuefields.screens.to.choose.screens'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
                <ww:param name="'value2'"><b></ww:param>
                <ww:param name="'value3'"></b></ww:param>
                <ww:param name="'value4'"><a href="ViewFieldScreenSchemes.jspa"></ww:param>
                <ww:param name="'value5'"></a></ww:param>
            </ww:text>
        </li>
        <li>
            <ww:text name="'admin.issuefields.screens.to.select.screens'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
                <ww:param name="'value2'"><a href="ListWorkflows.jspa"></ww:param>
                <ww:param name="'value3'"></a></ww:param>
            </ww:text>
        </li>
    </ul>
    <p>
        <ww:text name="'admin.issuefields.screens.note1'">
            <ww:param name="'value0'"><span class="note"></ww:param>
            <ww:param name="'value1'"></span></ww:param>
        </ww:text>
    </p>
</page:applyDecorator>
    <ww:if test="/fieldScreens/empty == false">
    <table class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th width="15%">
                    <ww:text name="'common.words.name'"/>
                </th>
                <th width="30%">
                    <ww:text name="'common.words.description'"/>
                </th>
                <th width="20%">
                    <ww:text name="'admin.menu.issuefields.screen.schemes'"/>
                </th>
                <th width="20%">
                    <ww:text name="'admin.menu.globalsettings.workflows'"/>
                </th>
                <th width="15%">
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
            <ww:iterator value="/fieldScreens" status="'status'">
            <tr>
                <td>
                    <ww:property value="./name" />
                </td>
                <td>
                    <ww:property value="./description" />
                </td>
                <td>
                    <ww:if test="/fieldScreenSchemes(.) != null && /fieldScreenSchemes(.)/empty != true ">
                        <ul>
                        <ww:iterator value="/fieldScreenSchemes(.)" status="'schemeStatus'">
                            <li><a href="<%= request.getContextPath() %>/secure/admin/ConfigureFieldScreenScheme.jspa?id=<ww:property value="./id" />"><ww:property value="./name" /></a></li>
                        </ww:iterator>
                        </ul>
                    </ww:if>
                    <ww:else>
                        &nbsp;
                    </ww:else>
                </td>
                <td>
                    <ww:property value="/workflowTransitionViews(.)">
                        <ww:if test=". && ./empty == false">
                            <ul>
                            <ww:iterator value="." status="'workflowTransitionStatus'">
                                <li>
                                <ww:if test="./hasSteps == true || ./globalAction == true">
                                    <%-- If the action is global then no need to pass the step id in teh link --%>
                                    <ww:if test="./globalAction == true">
                                        <a href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="./workflowMode" /><ww:param name="'workflowName'" value="./workflowName" /><ww:param name="'workflowTransition'" value="./transitionId" /></ww:url>"><ww:property value="./workflowName" /></a>
                                    </ww:if>
                                    <ww:else>
                                        <ww:property value="./firstStep" id="step" />
                                        <a href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="./workflowMode" /><ww:param name="'workflowName'" value="./workflowName" /><ww:param name="'workflowStep'" value="@step/id" /><ww:param name="'workflowTransition'" value="./transitionId" /></ww:url>"><ww:property value="./workflowName" /></a>
                                    </ww:else>
                                    (<ww:property value="./transitionName" />)
                                </ww:if>
                                </li>
                            </ww:iterator>
                            </ul>
                        </ww:if>
                        <ww:else>
                            &nbsp;
                        </ww:else>
                    </ww:property>
                </td>
                <td>
                    <ul class="operations-list">
                        <li>
                            <a id="configure_fieldscreen_<ww:property value="./name" />" rel="<ww:property value="./id" />" class="configure-fieldscreen" href="ConfigureFieldScreen.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screens.configure.tabs.and.fields'">
                            <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                            </ww:text>"><ww:text name="'admin.common.words.configure'"/></a>
                        </li>
                        <li>
                            <a id="edit_fieldscreen_<ww:property value="./name" />" rel="<ww:property value="./id" />" class="edit-fieldscreen" href="EditFieldScreen!default.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screens.edit.value'">
                            <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                            </ww:text>"><ww:text name="'common.words.edit'"/></a>
                        </li>
                        <li>
                            <a id="copy_fieldscreen_<ww:property value="./name" />" rel="<ww:property value="./id" />" class="copy-fieldscreen" href="ViewCopyFieldScreen.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screens.copy.value'">
                            <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                            </ww:text>"><ww:text name="'common.words.copy'"/></a>
                        </li>
                        <ww:if test="/deletable(.) == true">
                        <li>
                            <a id="delete_fieldscreen_<ww:property value="./name" />" rel="<ww:property value="./id" />" class="delete-fieldscreen" href="ViewDeleteFieldScreen.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screens.delete.value'">
                            <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                            </ww:text>"><ww:text name="'common.words.delete'"/></a>
                        </li>
                        </ww:if>
                    </ul>
                </td>
            </tr>
            </ww:iterator>
            </tbody>
    </table>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.screens.no.screens.configured'"/></aui:param>
    </aui:component>
</ww:else>


<page:applyDecorator name="jiraform">
    <page:param name="action">AddFieldScreen.jspa</page:param>
    <page:param name="submitId">add_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.screens.add.screen'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
        <ww:text name="'admin.issuefields.screens.add.screen.instruction'">
            <ww:param name="'value0'"><b><ww:text name="'common.forms.add'"/></b></ww:param>
        </ww:text>
    </page:param>

    <ui:textfield label="text('common.words.name')" name="'fieldScreenName'" size="'30'">
        <ui:param name="'mandatory'">true</ui:param>
    </ui:textfield>

    <ui:textfield label="text('common.words.description')" name="'fieldScreenDescription'" size="'60'" />
</page:applyDecorator>

</body>
</html>
