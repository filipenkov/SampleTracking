<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <ww:if test="/issueValid == true">
        <title><ww:text name="'worklog.delete.title'"/></title>
        <meta name="decorator" content="issueaction" />
        <%
             KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
             keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
         %>
        <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/issueValid == true">
    <script language="JavaScript">
        function selectNewEstimate()
        {
            document.forms['jiraform'].elements['new_estimate_id'].checked = true;
        }

        function selectManualAdjustEstimate()
        {
            document.forms['jiraform'].elements['manual_adjust_estimate_id'].checked = true;
        }

        function resetSelect(form, selectId)
        {
            if (form[selectId])
            {
                form[selectId].selectedIndex = 0;
            }
        }
    </script>
    <page:applyDecorator name="jiraform">
        <page:param name="action"><ww:property value="/actionName"/>.jspa</page:param>
        <page:param name="cancelURI"><ww:url value="/issuePath" atltoken="false"/></page:param>
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'worklog.delete.title'"/></page:param>
        <page:param name="description">
            <ww:text name="'worklog.delete.desc'"/>
        </page:param>

        <ww:if test="/issueExists == true">
            <ui:component name="'worklogId'" template="hidden.jsp" theme="'single'" value="/worklogId" />

            <ww:if test="errors['adjustmentAmount']">
                <tr>
                    <td class="fieldLabelArea formErrors">&nbsp;</td>
                    <td class="fieldValueArea formErrors">
                        <span class="errMsg"><ww:property value="errors['adjustmentAmount']"/></span>
                    </td>
                </tr>
            </ww:if>
            <ww:if test="errors['newEstimate']">
                <tr>
                    <td class="fieldLabelArea formErrors">&nbsp;</td>
                    <td class="fieldValueArea formErrors">
                        <span class="errMsg"><ww:property value="errors['newEstimate']"/></span>
                    </td>
                </tr>
            </ww:if>
            <tr>
                <ww:if test="errors['newEstimate'] || errors['adjustmentAmount']">
                    <td class="fieldLabelArea formErrors">
                </ww:if>
                <ww:else>
                    <td class="fieldLabelArea">
                </ww:else>
                 <ww:text name="'logwork.adjustestimate'"/>:
                </td>
            <ww:if test="errors['newEstimate'] || errors['adjustmentAmount']">
                <td class="fieldValueArea formErrors">
            </ww:if>
            <ww:else>
                <td class="fieldValueArea">
            </ww:else>
                    <input type="radio" name="adjustEstimate" id="auto_adjust_estimate_id" align="middle"
                    value="auto" <ww:if test="adjustEstimate == 'auto'"> checked</ww:if>>
                    <label for="auto_adjust_estimate_id">
                        <ww:text name="'logwork.bullet1.autoadjust'"/><br>
                        &nbsp; &nbsp; &nbsp; &nbsp; <span class="subText">(<ww:text name="'logwork.bullet1.autoadjust.desc'"/>
                        )</span><br>
                    </label>

                    <input type="radio" name="adjustEstimate" id="leave_estimate_id" align="middle"
                    value="leave" <ww:if test="adjustEstimate == 'leave'"> checked</ww:if>>
                    <label for="leave_estimate_id">
                        <ww:if test="estimate==null">
                            <ww:text name="'logwork.bullet2.estimateunknown'"/><br>
                        </ww:if>
                        <ww:else>
                            <ww:text name="'logwork.bullet2.leaveestimate'">
                                <ww:param name="'value0'"><b><ww:property value="estimate"/></b></ww:param>
                            </ww:text><br>
                        </ww:else>
                    </label>

                    <input type="radio" name="adjustEstimate" id="new_estimate_id" align="middle"
                    value="new" <ww:if test="adjustEstimate == 'new'"> checked</ww:if>>
                    <label for="new_estimate_id">
                        <ww:text name="'logwork.bullet3.setnewestimate'"/>
                    </label>
                    <input type="text" name="newEstimate" align="middle" size="5" onChange="selectNewEstimate();"
                           value="<ww:property value="/newEstimate"/>">
                    <br/>

                    <input type="radio" name="adjustEstimate" id="manual_adjust_estimate_id" align="middle"
                           value="manual" <ww:if test="adjustEstimate == 'manual'"> checked</ww:if>>
                    <label for="manual_adjust_estimate_id">
                        <ww:text name="'logwork.bullet4.increaseestimate'"/>
                    </label>
                    <input type="text" name="adjustmentAmount" align="middle" size="5" onChange="selectManualAdjustEstimate();"
                           value="<ww:property value="/adjustmentAmount"/>">
                    <br>

                </td>
            </tr>
            <tr id="newEstimateTableRow" >
                <ww:if test="errors['newEstimate']">
                    <td class="fieldLabelArea formErrors">
                </ww:if>
                <ww:else>
                    <td class="fieldLabelArea">
                </ww:else>

                <ww:if test="errors['newEstimate']">
                    <td class="fieldValueArea formErrors">
                </ww:if>
                <ww:else>
                    <td class="fieldValueArea">
                </ww:else>
                    <span class="subText">
                        <ww:text name="'worklog.delete.adjust.desc'">
                            <ww:param name="'value0'"><ww:property value="daysPerWeek"/>d</ww:param>
                            <ww:param name="'value1'"><ww:property value="hoursPerDay"/>h</ww:param>
                            <ww:param name="'value2'">*w *d *h *m</ww:param>
                            <ww:param name="'value3'">4d, 5h 30m, 60m</ww:param>
                            <ww:param name="'value4'">3w</ww:param>
                            <ww:param name="'value5'">1w</ww:param>
                            <ww:param name="'value6'">1d</ww:param>
                        </ww:text>
                    </span>
                </td>
            </tr>
            <ui:component name="'id'" template="hidden.jsp" theme="'single'"/>
        </ww:if>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <%@ include file="/includes/issue/generic-errors.jsp" %>
    </div>
</ww:else>
</body>
</html>
