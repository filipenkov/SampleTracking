<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>

<style>
.condition-block { margin: 0 0 0 20px; padding: 0; }
.operator { padding: 10px 0 10px 0; }
.highlighted-leaf { background: #cee7ff; margin: 0 0 0 20px; padding: 4px; }
.leaf { background: #f0f0f0; margin: 0 0 0 20px; padding: 4px; }
.single-leaf { background: #f0f0f0; margin: 0; padding: 4px; }

/* Used for drawing a 'grouping brace' around grouped conditions, i.e. conditions in the same ConditionsDescriptor */
.condition-group { border-left: 1px solid green; }
.top-tick { border-bottom: 1px solid green; height: 1px; width: 12px; }
.bottom-tick { border-top: 1px solid green; height: 1px; width: 12px; }
</style>

<table id="workflow-transition-tab" class="gridBox" cellpadding="3" cellspacing="1" width="100%" align="center"><tr bgcolor="#ffffff"><td>

<ww:if test="/initial != true">
<ww:if test="/descriptorTab == 'all' || /descriptorTab == 'conditions'">
    <ww:if test="/descriptorTab == 'all'"><h3><ww:text name="'admin.workflowtransition.conditions'"/></h3></ww:if>
    <ww:if test="workflow/editable == true">
        <img src="<%= request.getContextPath() %>/images/icons/bullet_creme.gif" height=8 width=8 border=0 align=absmiddle>
        <ww:text name="'admin.workflowtransition.addnewcondition'">
            <ww:param name="'value0'"><a id="add_new_condition" href="<ww:url page="AddWorkflowTransitionCondition!default.jspa"><ww:param name="'workflowMode'" value="workflow/mode" /><ww:param name="'workflowName'" value="workflow/name" /><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
        </ww:text>
    </ww:if>

    <ww:if test="transition/restriction && transition/restriction/conditionsDescriptor && transition/restriction/conditionsDescriptor/conditions">
        <p>

        <ww:property value="transition/restriction/conditionsDescriptor" >
            <ww:if test="./conditions">
                <ww:property value="." id="conditionDescriptor" />

                <ww:bean id="descriptorBean" name="'com.atlassian.jira.web.bean.WorkflowConditionFormatBean'">
                    <ww:param name="'descriptor'" value="@conditionDescriptor"/>
                    <ww:param name="'delete'" value="workflow/editable" />
                    <ww:param name="'edit'" value="workflow/editable" />
                    <ww:param name="'deleteAction'">DeleteWorkflowTransitionCondition.jspa</ww:param>
                    <ww:param name="'editAction'">EditWorkflowTransitionConditionParams!default.jspa</ww:param>
                    <ww:param name="'pluginType'">workflow-condition</ww:param>
                </ww:bean>

                <%@ include file="/includes/admin/workflow/viewworkflowconditions.jsp" %>
            </ww:if>
            <ww:else>
                <ww:text name="'admin.workflowtransition.available.to.everybody'"/>
            </ww:else>
        </ww:property>
        </p>
    </ww:if>
    <ww:else>
        <p><ww:text name="'admin.workflowtransition.no.conditions'"/></p>
    </ww:else>
</ww:if>
</ww:if>

<jsp:include page="/secure/admin/views/workflow/workflow-validators.jsp" flush="false" />


<ww:if test="/descriptorTab == 'all' || /descriptorTab == 'other'">

<jsp:include page="/secure/admin/views/workflow/workflow-conditionals.jsp" flush="false" />

<jsp:include page="/secure/admin/views/workflow/workflow-unconditionals.jsp" flush="false" />

<jsp:include page="/secure/admin/views/workflow/workflow-uncond-prefunctions.jsp" flush="false" />

<jsp:include page="/secure/admin/views/workflow/workflow-global-prefuncs.jsp" flush="false" />

<jsp:include page="/secure/admin/views/workflow/workflow-global-postfuncs.jsp" flush="false" />

</ww:if>

<ww:if test="/descriptorTab == 'all' || /descriptorTab == 'postfunctions'">
    <ww:if test="/descriptorTab == 'all'"><h3><ww:text name="'admin.workflowtransition.post.functions'"/></h3></ww:if>

    <ww:if test="transition/unconditionalResult">
        <ww:if test="workflow/editable == true">
        <img src="<%= request.getContextPath() %>/images/icons/bullet_creme.gif" height=8 width=8 border=0 align=absmiddle>
        <ww:text name="'admin.workflowtransition.add.new.post.function'">
            <ww:param name="'value0'"><a id="add_post_func" href="<ww:url page="AddWorkflowTransitionPostFunction!default.jspa"><ww:param name="'workflowMode'" value="workflow/mode" /><ww:param name="'workflowName'" value="workflow/name" /><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
        </ww:text>
    </ww:if>
    <p>

    <ww:if test="/transition/unconditionalResult/postFunctions/empty == false">

        <ww:if test="/transition/unconditionalResult/postFunctions/size > 1">
            <div class="top-tick"><!-- --></div>
            <div class="condition-group">
        </ww:if>


        <ww:bean id="descriptorBean" name="'com.atlassian.jira.web.bean.WorkflowDescriptorFormatBean'">
            <ww:param name="'descriptorCollection'" value="transition/unconditionalResult/postFunctions"/>
            <ww:param name="'delete'" value="workflow/editable" />
            <ww:param name="'edit'" value="workflow/editable" />
            <ww:param name="'deleteAction'">DeleteWorkflowTransitionPostFunction.jspa</ww:param>
            <ww:param name="'editAction'">EditWorkflowTransitionPostFunctionParams!default.jspa</ww:param>
            <ww:param name="'pluginType'">workflow-function</ww:param>
            <ww:param name="'orderable'" value="workflow/editable" />
            <ww:param name="'operatorTextKey'">admin.workflowtransition.operator.then</ww:param>
        </ww:bean>

        <%@ include file="/includes/admin/workflow/viewworkflowdescriptors.jsp" %>

        <ww:if test="/transition/unconditionalResult/postFunctions/size > 1">
            </div>
            <div class="bottom-tick"><!-- --></div>
        </ww:if>
    </ww:if>
    <ww:else>
        <ww:text name="'admin.workflowtransition.no.post.functions'"/>
    </ww:else>
    </p>
    </ww:if>
</ww:if>

</td></tr></table>
