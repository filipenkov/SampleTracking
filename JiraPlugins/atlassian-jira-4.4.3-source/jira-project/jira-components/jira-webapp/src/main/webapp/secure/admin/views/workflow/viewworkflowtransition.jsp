<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflowtransition.view.workflow.transition'"/> - <ww:property value="transition/name" /></title>
</head>
<body>

<table width="100%" cellpadding="4" cellspacing="0"><tr><td valign="top" width="50%">
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.workflowtransition.transition'"/>: <ww:property value="transition/name" /></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">workflow</page:param>
    <%@ include file="/includes/admin/workflow/workflowinfobox.jsp" %>
    <ww:if test="/global == true">
        <p>
            <ww:text name="'admin.workflowtransition.availabletoall'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
            </ww:text>
        </p>
    </ww:if>
    <ww:elseIf test="/initial == true">
        <p>
            <ww:text name="'admin.workflowtransition.initial.transition'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
            </ww:text>
        </p>
    </ww:elseIf>

    <p>
    <b><ww:text name="'admin.workflowtransition.transitionview'"/></b>:
    <ww:if test="/initial == true">
       <ww:text name="'admin.workflowtransition.noinitialview'"/>
    </ww:if>
    <ww:elseIf test="/fieldScreen">
            <a id="configure_fieldscreen" href="<%= request.getContextPath() %>/secure/admin/ConfigureFieldScreen.jspa?id=<ww:property value="/fieldScreen/id" />"><ww:property value="/fieldScreen/name" /></a>
    </ww:elseIf>
    <ww:else>
        <ww:text name="'admin.workflowtransition.willhappeninstantly'"/>
    </ww:else>
    <ww:property value="/transition/metaAttributes/('jira.description')">
        <ww:if test=". && length >  0">
            <br>
            <b><ww:text name="'common.words.description'"/></b>: <ww:property value="." />
        </ww:if>
    </ww:property>
    </p>
    <ul class="square">
        <li><ww:text name="'admin.workflows.viewallsteps'">
            <ww:param name="'value0'"><a href="<ww:url page="ViewWorkflowSteps.jspa"><ww:param name="'workflowMode'" value="workflow/mode" /><ww:param name="'workflowName'" value="workflow/name" /></ww:url>"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
            <ww:param name="'value2'"><b><ww:property value="/workflowDisplayName" /></b></ww:param>
        </ww:text></li>
        <ww:if test="/workflow/editable == true">
            <li><ww:text name="'admin.workflowtransition.edittransition'">
                <ww:param name="'value0'"><a href="<ww:url page="EditWorkflowTransition!default.jspa"><ww:param name="'workflowMode'" value="workflow/mode" /><ww:param name="'workflowName'" value="workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>" id="edit_transition"><b></ww:param>
                <ww:param name="'value1'"></b></a></ww:param>
            </ww:text></li>
            <ww:if test="/global == false && /initial == false">
                <li><ww:text name="'admin.workflowtransition.deletetransition'">
                    <ww:param name="'value0'"><a id="delete_transition" href="<ww:url page="DeleteWorkflowTransitions!confirm.jspa"><ww:param name="'workflowMode'" value="workflow/mode" /><ww:param name="'workflowName'" value="workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'transitionIds'" value="transition/id" /></ww:url>"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text></li>
            </ww:if>
        </ww:if>
        <li><ww:text name="'admin.workflowtransition.viewproperties'">
            <ww:param name="'value0'"><a id="view_transition_properties" href="<ww:url page="ViewWorkflowTransitionMetaAttributes.jspa"><ww:param name="'workflowMode'" value="workflow/mode" /><ww:param name="'workflowName'" value="workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
        </ww:text></li>
    </ul>

</page:applyDecorator>
</td><td valign="top" width="50%">
<table cellpadding="4" cellspacing="0" border="0" align="center" width="100%"><tr><td style="border: 1px solid #bbbbb0; background: #fffff0;">
<div class="formtitle"><ww:text name="'admin.workflowstep.workflow.browser'"/></div>
    <table id="workflow_browser" cellpadding="3" cellspacing="0" border="0" width="100%">
        <tr>
            <td width="33%" align="right">
                <table id="orig_steps" cellpadding="3" cellspacing="3">
                    <ww:iterator value="/stepsForTransition">
                        <tr>
                            <td style="border: 1px solid #bbb;" bgcolor="#ffffff" align="left" nowrap>
                                <a href="<ww:url page="ViewWorkflowStep.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:property value="name" /></a>
                            </td>
                            <td align="absmiddle" nowrap><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height=16 width=16 border=0 align=absmiddle></td>
                        </tr>
                    </ww:iterator>
                </table>
            </td>
            <td align="center" valign="middle" width="33%">
                <table cellpadding="3" cellspacing="3" border="0">
                    <tr>
                        <td style="border: 1px solid #bbb;" bgcolor="#f0f0f0">
                            <ww:property value="/transition">
                                <ww:if test="/global == true || /common == true"><em></ww:if>
                                <ww:property value="transition/name" />
                                <ww:if test="/global == true || /common == true"></em></ww:if>
                                <span class="smallgrey">(<ww:property value="id" />)</span>
                            </ww:property>
                        </td>
                    </tr>
                </table>
            </td>
            <td align="left" width="33%">
                 <table id="dest_steps" cellpadding="3" cellspacing="3" border="0">
                    <ww:if test="/transitionWithoutStepChange == true">
                        <ww:iterator value="/stepsForTransition">
                            <tr>
                                <td align="absmiddle" nowrap><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height=16 width=16 border=0 align=absmiddle></td>
                                <td align="left" style="border: 1px solid #bbb;" bgcolor="#ffffff" nowrap>
                                    <a href="<ww:url page="ViewWorkflowStep.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:property value="name" /></a>
                                </td>
                            </tr>
                        </ww:iterator>
                    </ww:if>
                    <ww:else>
                        <tr>
                            <td align="absmiddle" nowrap><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height=16 width=16 border=0 align=absmiddle></td>
                            <td align="left" style="border: 1px solid #bbb;" bgcolor="#ffffff" nowrap>
                                <ww:property value="/workflow/descriptor/step(transition/unconditionalResult/step)">
                                    <a href="<ww:url page="ViewWorkflowStep.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:property value="name" /></a>
                                </ww:property>
                            </td>
                        </tr>
                    </ww:else>
                </table>
            </td>
        </tr>
        <tr>
            <td align="center">(<ww:text name="'admin.workflowtransition.originatingsteps'"/>)</td>
            <td align="absmiddle"><b>&nbsp;</b></td>
            <td align="center">(<ww:text name="'admin.workflowtransition.destinationstep'"/>)</td>
        </tr>
    </table>
    </td></tr></table>
</td></tr></table>

<p>
<table cellpadding="2" cellspacing="0" border="0" width="100%" align="center">
<tr>
	<ww:if test="/descriptorTab == 'all'">
		<td bgcolor="#bbbbbb" width="1%" nowrap align="center">
			&nbsp;<font color="#ffffff"><b><ww:text name="'admin.workflowtransition.all'"/></b></font>&nbsp;
		</td>
	</ww:if>
	<ww:else>
		<td width="1%" nowrap align="center">
			&nbsp;<b><a id="view_all_trans" href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="/transition/id" /><ww:param name="'descriptorTab'" value="'all'" /></ww:url>"><ww:text name="'admin.workflowtransition.all'"/></a></b>&nbsp;
		</td>
	</ww:else>

    <ww:if test="/initial != true">
    <ww:if test="/descriptorTab == 'conditions'">
		<td bgcolor="#bbbbbb" width="1%" nowrap align="center">
			&nbsp;<font color=#ffffff><b><ww:text name="'admin.workflowtransition.conditions'"/></b> <span class="small">(<ww:property value="/numberConditions" />)</span></font>&nbsp;
		</td>
	</ww:if>
	<ww:else>
		<td width="1%" nowrap align="center">
			&nbsp;<b><a id="view_conditions" href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="/transition/id" /><ww:param name="'descriptorTab'" value="'conditions'" /></ww:url>"><ww:text name="'admin.workflowtransition.conditions'"/></b></a><span class="small"> (<ww:property value="/numberConditions" />)</span>&nbsp;
		</td>
	</ww:else>
    </ww:if>

    <ww:if test="/descriptorTab == 'validators'">
		<td bgcolor="#bbbbbb" width="1%" nowrap align="center">
			&nbsp;<font color="#ffffff"><b><ww:text name="'admin.workflowtransition.validators'"/></b> <span class="small">(<ww:if test="/transition/validators"><ww:property value="/transition/validators/size" /></ww:if><ww:else>0</ww:else>)</span></font>&nbsp;
		</td>
	</ww:if>
	<ww:else>
		<td width="1%" nowrap align="center">
			&nbsp;<b><a id="view_validators" href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="/transition/id" /><ww:param name="'descriptorTab'" value="'validators'" /></ww:url>"><ww:text name="'admin.workflowtransition.validators'"/></a></b> <span class="small">(<ww:if test="/transition/validators"><ww:property value="/transition/validators/size" /></ww:if><ww:else>0</ww:else>)</span>&nbsp;
		</td>
	</ww:else>

    <ww:if test="/descriptorTab == 'postfunctions'">
		<td bgcolor="#bbbbbb" width="1%" nowrap align="center">
			&nbsp;<font color="#ffffff"><b><ww:text name="'admin.workflowtransition.post.functions'"/></b> <span class="small">(<ww:if test="transition/unconditionalResult && transition/unconditionalResult/postFunctions"><ww:property value="transition/unconditionalResult/postFunctions/size" /></ww:if><ww:else>0</ww:else>)</span></font>&nbsp;
		</td>
	</ww:if>
	<ww:else>
		<td width="1%" nowrap align="center">
			&nbsp;<b><a id="view_post_functions" href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="/transition/id" /><ww:param name="'descriptorTab'" value="'postfunctions'" /></ww:url>"><ww:text name="'admin.workflowtransition.post.functions'"/></a></b> <span class="small">(<ww:if test="transition/unconditionalResult && transition/unconditionalResult/postFunctions"><ww:property value="transition/unconditionalResult/postFunctions/size" /></ww:if><ww:else>0</ww:else>)</span>&nbsp;
		</td>
	</ww:else>

    <ww:if test="/descriptorTab == 'other'">
		<td bgcolor="#bbbbbb" width="1%" nowrap align="center">
			&nbsp;<font color="#ffffff"><b><ww:text name="'admin.workflowtransition.other'"/></b></font>&nbsp;
		</td>
	</ww:if>
	<ww:elseIf test="/showOtherTab == true">
		<td width="1%" nowrap align="center">
			&nbsp;<b><a id="view_other" href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="/transition/id" /><ww:param name="'descriptorTab'" value="'other'" /></ww:url>"><ww:text name="'admin.workflowtransition.other'"/></a></b>&nbsp;
		</td>
	</ww:elseIf>

    <td width="100%">&nbsp;</td>
</tr>
</table>

<jsp:include page="/secure/admin/views/workflow/workflow-conditions-validators-results.jsp" />

</p>
</body>
</html>
