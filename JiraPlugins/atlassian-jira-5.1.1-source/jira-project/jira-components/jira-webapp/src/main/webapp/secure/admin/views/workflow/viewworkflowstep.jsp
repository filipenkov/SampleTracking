<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflowstep.view.workflow.step'"/> - <ww:property value="step/name" /></title>
</head>
<body>

<table width="100%" cellpadding="4" cellspacing="0"><tr><td valign="top" width="50%">
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.workflowstep.view.workflow.step'"/> &mdash; <ww:property value="step/name" /></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">workflow</page:param>
    <%@ include file="/includes/admin/workflow/workflowinfobox.jsp" %>
    <p>
        <ww:text name="'admin.workflowstep.thispageshows'">
            <ww:param name="'value0'"><b><ww:property value="step/name" /></b></ww:param>
        </ww:text>
        <ww:if test="/oldStepOnDraft(/step) == true">
             <ww:text name="'admin.workflowstep.step.exists.on.active'"/>
        </ww:if><br>
    </p>
    <p>
        <ww:property value="/step">
            <ww:if test="metaAttributes/('jira.status.id')">
                <ww:property value="metaAttributes/('jira.status.id')">
                    <ww:property value="/status(.)">
                        <ww:text name="'admin.workflowstep.linkedtostatus'"/>:

                        <ww:component name="'notUsed'" template="constanticon.jsp">
                            <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                            <ww:param name="'iconurl'" value="./string('iconurl')" />
                            <ww:param name="'alt'"><ww:property value="./string('name')" /></ww:param>
                            <ww:param name="'title'"><ww:property value="./string('name')" /> - <ww:property value="./string('description')" /></ww:param>
                        </ww:component>
                        <ww:property value="./string('name')" /><br>
                    </ww:property>
                </ww:property>
            </ww:if>
            <ww:else>
                <ww:text name="'admin.workflowstep.no.linked.status'">
                    <ww:param name="'value0'"><b></ww:param>
                    <ww:param name="'value1'"></b></ww:param>
                </ww:text>
            </ww:else>
        </ww:property>
    </p>
    <ul class="square">
        <li><ww:text name="'admin.workflows.viewallsteps'">
            <ww:param name="'value0'"><a href="<ww:url page="ViewWorkflowSteps.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /></ww:url>"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
            <ww:param name="'value2'"><b><ww:property value="/workflowDisplayName" /></b></ww:param>
        </ww:text></li>
        <ww:if test="/workflow/editable == true">
            <ww:if test="/stepWithoutTransitionsOnDraft(/step/id) == false">
                <li><ww:text name="'admin.workflowstep.add.outgoing.transition'">
                    <ww:param name="'value0'"><a id="add_transition" href="<ww:url page="AddWorkflowTransition!default.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'originatingUrl'" value="'viewWorkflowStep'" /></ww:url>"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text></li>
            </ww:if>
            <ww:if test="/step/actions/empty == false">
                <li><ww:text name="'admin.workflowstep.delete.outgoing.transitions'">
                    <ww:param name="'value0'"><a id="del_transition" href="<ww:url page="DeleteWorkflowTransitions!default.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'originatingUrl'" value="'viewWorkflowStep'" /></ww:url>"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text></li>
            </ww:if>
            <li><ww:text name="'admin.workflowstep.edit.step'">
                <ww:param name="'value0'"><a id="edit_step" href="<ww:url page="EditWorkflowStep!default.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'originatingUrl'" value="'viewWorkflowStep'" /></ww:url>"><b></ww:param>
                <ww:param name="'value1'"></b></a></ww:param>
            </ww:text></li>
            <ww:if test="canDeleteStep(/step) == true">
                <li><ww:text name="'admin.workflowstep.delete.step'">
                    <ww:param name="'value0'"><a id="del_step" href="<ww:url page="DeleteWorkflowStep!default.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'originatingUrl'" value="'viewWorkflowStep'" /></ww:url>"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text></li>
            </ww:if>
        </ww:if>
        <li><ww:text name="'admin.workflowstep.viewproperties'">
            <ww:param name="'value0'"><a id="view_properties_<ww:property value="/step/id"/>" href="<ww:url page="ViewWorkflowStepMetaAttributes.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /></ww:url>"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
        </ww:text></li>
    </ul>
</page:applyDecorator>
</td><td valign="top" width="50%">
    <table cellpadding="4" cellspacing="0" border="0" align="center" width="100%"><tr><td style="border: 1px solid #bbbbb0; background: #fffff0;">
    <div class="formtitle"><ww:text name="'admin.workflowstep.workflow.browser'"/></div>
        <table id="workflow_browser" cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td align="absmiddle" width="33%" align="right">
                    <table id="inbound_trans" cellpadding="3" cellspacing="3" align="right">
                        <ww:if test="/inboundTransitions && inboundTransitions/empty == false">
                        <ww:iterator value="/inboundTransitions">
                            <tr>
                                <td style="border: 1px solid #bbb;" bgcolor="#f0f0f0" nowrap>
                                    <ww:if test="/global(.) == true">
                                        <em><a id="view_transition_<ww:property value="id" />" href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                               <ww:if test="metaAttributes/('jira.description')">title="<ww:property value="metaAttributes/('jira.description')"/>"</ww:if>><ww:property value="name" /></a></em>
                                    </ww:if>
                                    <ww:elseIf test="/initial(.) == true">
                                        <a id="view_transition_<ww:property value="id" />" href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                               <ww:if test="metaAttributes/('jira.description')">title="<ww:property value="metaAttributes/('jira.description')"/>"</ww:if>><ww:property value="name" /></a>
                                    </ww:elseIf>
                                    <ww:else>
                                        <ww:if test="/stepsForTransition(.)/empty == false">
                                            <ww:if test="/common(.) == true"><em></ww:if>
                                            <a id="view_transition_<ww:property value="id" />" href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/stepsForTransition(.)/iterator/next/id" /><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                               <ww:if test="metaAttributes/('jira.description')">title="<ww:property value="metaAttributes/('jira.description')"/>"</ww:if>><ww:property value="name" /></a>
                                            <ww:if test="/common(.) == true"></em></ww:if>
                                        </ww:if>
                                        <ww:else>
                                            <span class="warning"><ww:text name="'admin.workflowstep.error'"/></span>
                                        </ww:else>
                                    </ww:else>
                                    <span class="smallgrey">(<ww:property value="id" />)</span>
                                </td>
                                <td align="absmiddle" nowrap><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" align="absmiddle"></td>
                            </tr>
                        </ww:iterator>
                        </ww:if>
                        <ww:else>
                            <tr>
                                <td><span class="warning"><ww:text name="'admin.workflowstep.notransitions'"/></span></td>
                                <td align="absmiddle" nowrap><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" align="absmiddle"></td>
                            </tr>
                        </ww:else>
                    </table>
                </td>
                <td align="center" valign="middle" width="33%">
                    <table cellpadding="3" cellspacing="1" width="90%">
                        <tr>
                            <td align="center" style="border: 1px solid #bbb;" bgcolor="#ffffff" nowrap>
                                <ww:property value="/step/name" />
                            </td>
                        </tr>
                    </table>
                </td>
                <td align="absmiddle" width="33%">
                     <table id="outgoing_trans" cellpadding="3" cellspacing="3" border="0">
                        <ww:if test="/outboundTransitions && /outboundTransitions/empty == false">
                        <ww:iterator value="/outboundTransitions">
                            <tr>
                                <td align="absmiddle" nowrap><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" align="absmiddle"></td>
                                <td align="left" style="border: 1px solid #bbb;" bgcolor="#f0f0f0"  nowrap>
                                    <ww:if test="/global(.) == true">
                                        <em><a href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                               <ww:if test="metaAttributes/('jira.description')">title="<ww:property value="metaAttributes/('jira.description')"/>"</ww:if>><ww:property value="name" /></a></em>
                                    </ww:if>
                                    <ww:else>
                                        <ww:if test="/common(.) == true"><em></ww:if>
                                        <a href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="/workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                           <ww:if test="metaAttributes/('jira.description')">title="<ww:property value="metaAttributes/('jira.description')"/>"</ww:if>><ww:property value="name" /></a>
                                        <ww:if test="/common(.) == true"></em></ww:if>
                                    </ww:else>
                                    <span class="smallgrey">(<ww:property value="id" />)</span>
                                </td>
                            </tr>
                        </ww:iterator>
                        </ww:if>
                        <ww:else>
                            <tr>
                                <td align="absmiddle" nowrap><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" align="absmiddle"></td>
                                <td><span class="warning"><ww:text name="'admin.workflowstep.notransitions'"/></span></td>
                            </tr>
                        </ww:else>
                    </table>

                </td>
            </tr>
            <tr>
                <td align="center">(<ww:text name="'admin.workflowstep.incomingtransitions'"/>)</td>
                <td align="absmiddle"><b>&nbsp;</b></td>
                <td align="center">(<ww:text name="'admin.workflowstep.outgoingtransitions'"/>)</td>
            </tr>
        </table>
        </td></tr></table>
</td></tr></table>

    <p>
        <table class="gridBox" cellpadding="3" cellspacing="1" width="90%" align="center">
            <tr bgcolor="#ffffff"><td>
                <span class="warning"><ww:text name="'admin.common.words.note'"/></span>:
                <ul>
                    <li><ww:text name="'admin.workflowstep.transitions.appearing.in.italics'">
                        <ww:param name="'value0'"><em></ww:param>
                        <ww:param name="'value1'"></em></ww:param>
                    </ww:text></li>
                    <li><ww:text name="'admin.workflowstep.to.add.transition'"/></li>
                </ul>
            </td></tr>
        </table>

    </p>


