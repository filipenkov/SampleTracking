<%@ taglib uri="webwork" prefix="ww" %>
<ww:bean name="'com.atlassian.core.util.StringUtils'" id="stringUtils" /> <%-- this is used by issuedisplayer --%>
<%@ include file="/includes/js/multipickerutils.jsp" %>
<table>
<ww:if test="/singleSelectOnly == 'false'">
    <ww:if test="/selectMode == 'multiple'">
        <tr><td colspan=5><strong><a href="#" onClick="populateFormMultiple(this)"><ww:text name="'issuedisplayer.select.issues'" /></a></strong></td></tr>
    </ww:if>
    <ww:else>
        <tr><td colspan=5><strong><a href="<ww:url>
            <ww:param name="'selectMode'" value="'multiple'" />
            <ww:param name="'mode'" value="/mode" />
            <ww:param name="'callbackMode'" value="/callbackMode" />
            <ww:param name="'fieldId'" value="/fieldId" />
            <ww:param name="'currentIssue'" value="/currentIssue"/>
            <ww:param name="'singleSelectOnly'" value="/singleSelectOnly"/>
            <ww:param name="'showSubTasks'" value="/showSubTasks"/>
            <ww:param name="'showSubTasksParent'" value="/showSubTasksParent"/>
            <ww:param name="'searchRequestId'" value="/searchRequestId" />
            <ww:if test="/selectedProjectId">
                <ww:param name="'selectedProjectId'" value="/selectedProjectId"/>
            </ww:if>

            </ww:url>" ><ww:text name="'issuedisplayer.select.multiple.issues'" /></a></strong></td></tr>
    </ww:else>
</ww:if>
<ww:subset count="50" >
    <ww:iterator value="." >
        <ww:if test="/selectMode == 'multiple'">
            <tr onmouseover="rowHover(this)" class="issue-picker-row"><td width="1%"><input type="checkbox" name="issuekey" value="<ww:property value="key" />" onclick="processCBClick(event, this);"></td>
        </ww:if>
        <ww:else>
            <tr onmouseover="rowHover(this)"  onClick="populateForm('<ww:property value="key" />')" class="issue-picker-row">
        </ww:else>
            <td width="1%" nowrap><a data-label="<ww:property value="key" /> - <ww:property value="summary" />" rel="<ww:property value="key" />" href="#" title="<ww:property value="summary" />" onClick="populateForm('<ww:property value="key" />')"><ww:property value="key" /></a> &nbsp;</td>
            <td><a href="#" title="<ww:property value="summary" />" onClick="populateForm('<ww:property value="key" />')"><ww:property value="@stringUtils/crop(summary, 80, ' ...')" /> </td>
            <td nowrap width=1%><%@ include file="/includes/icons/priority.jsp" %></td>
            <td nowrap width=1%><%@ include file="/includes/icons/status.jsp" %></td>
        </tr>
    </ww:iterator>
</ww:subset>
<ww:if test="/singleSelectOnly == 'false'">
    <ww:if test="/selectMode == 'multiple'">
        <tr><td colspan=5><strong><a href="#" onClick="populateFormMultiple(this)"><ww:text name="'issuedisplayer.select.issues'" /></a></strong></td></tr>
    </ww:if>
</ww:if>
</table>
