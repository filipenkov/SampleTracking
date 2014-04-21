<%@ taglib uri="webwork" prefix="ww" %>

<%-- Expects a WorkflowConditionFormatBean to be in context with id 'descriptorBean' --%>

<%-- Only print the 'grouping brace' if there is more than one condition --%>
<ww:if test="@descriptorBean/multipleDescriptors == true">
    <%-- NOTE: The HTML comment MUST be PRESENT in the DIV to ensure IE renders it as a flat DIV. Otherwise IE
             makes the div have a minimum height. --%>
    <div class="top-tick"><!-- --></div>
        <div class="condition-group">
</ww:if>

<ww:iterator value="@descriptorBean/descriptorCollection" status="'status'" >
    <ww:if test="@status/first == false">
        <div class="operator">
            <span style="color: green;">&mdash; <ww:text name="@descriptorBean/operatorTextKey"/></span>
            <ww:if test="workflow/editable == true">
                <%-- Only allow the user to change anything if the workflow is editable --%>
                <ww:if test="@status/index == 1">
                    &nbsp;&nbsp;
                    <ww:text name="'admin.workflowtransition.addcondition'">
                        <ww:param name="'value0'"><a href="<ww:url page="AddWorkflowTransitionCondition!default.jspa"><ww:param name="'workflowMode'" value="workflow/mode" /><ww:param name="'workflowName'" value="workflow/name" /><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBean/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                    |
                    <a href="<ww:url value="'ViewWorkflowTransition!changeLogicOperator.jspa'"><ww:param name="'workflowMode'" value="workflow/mode" /><ww:param name="'workflowName'" value="workflow/name" /><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBean/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>">
                        <ww:text name="'admin.workflowtransition.changeoperator'">
                            <ww:param name="'value0'"><ww:text name="@descriptorBean/otherOperatorTextKey"/></ww:param>
                        </ww:text>
                    </a>
                </ww:if>
            </ww:if>
        </div>
    </ww:if>

    <ww:if test="@descriptorBean/nestedDescriptor(.) == false">
        <%@ include file="/includes/admin/workflow/viewworkflowdescriptor.jsp" %>
    </ww:if>
    <ww:else>
        <%-- Loop into this JSP with a new descriptorBean that represents the nested ConditionsDescriptor --%>

        <%-- needed to get around the WW bug of looking up the stack - so use the id --%>
        <ww:property value="." id="conditionsDescriptor" />

        <ww:if test="@conditionsDescriptor/conditions && @conditionsDescriptor/conditions/empty == false">
            <ww:if test="@conditionsDescriptor/conditions/size > 1">
                <div class="condition-block">
            </ww:if>

            <%-- save the descriptor bean on top of the stack --%>
            <ww:property value="@descriptorBean">
                <%-- Hack to get around the WebWork EL bug --%>
                <ww:property value="./deleteAction" id="da" />
                <ww:property value="./editAction" id="ea" />
                <ww:property value="@descriptorBean/parentPrefix" id="pp" />
                <%-- create a new descriptor bean to represent the conditions element --%>
                <ww:bean id="descriptorBean" name="'com.atlassian.jira.web.bean.WorkflowConditionFormatBean'">
                    <ww:param name="'descriptor'" value="@conditionsDescriptor"/>
                    <ww:param name="'pluginType'" value="'workflow-condition'" />
                    <ww:param name="'parentPrefix'"><ww:property value="@pp" /><ww:property value="@status/count" /></ww:param>
                    <ww:param name="'delete'" value="/workflow/editable" />
                    <ww:param name="'edit'" value="workflow/editable" />
                    <ww:param name="'deleteAction'" value="@da" />
                    <ww:param name="'editAction'" value="@ea" />
                </ww:bean>

                <%-- recurse to this JSP to print out the ConditionsDescriptor which likely has more than one
                ConditionDescriptor inside --%>
                <jsp:include page="/includes/admin/workflow/viewworkflowconditions.jsp" />

                <%-- put the old descriptor format bean back --%>
                <ww:property id="descriptorBean" value="."/>

            </ww:property>

            <ww:if test="@conditionsDescriptor/conditions/size > 1">
                </div>
            </ww:if>
        </ww:if>
    </ww:else>
</ww:iterator>

<%-- Only close the 'grouping brace' if there is more than one condition --%>
<ww:if test="@descriptorBean/multipleDescriptors == true">
        <%-- Close 'condition-group' div --%>
        <%-- NOTE: The HTML comment MUST be PRESENT in the DIV to ensure IE renders it as a flat DIV. Otherwise IE
             makes the div have a minimum height. --%>
        </div><div class="bottom-tick"><!-- --></div>
</ww:if>
