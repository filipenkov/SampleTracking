<%@ page import="com.atlassian.jira.util.BrowserUtils"%>
<%@ taglib uri="webwork" prefix="ww" %>

<div class="buttons-container aui-toolbar form-buttons noprint">
<ww:if test="/hasAvailableOperations == true">
    <div class="toolbar-group">
        <span class="toolbar-item">
        <input class="toolbar-trigger" type="submit" id="<ww:text name="'common.forms.next'"/>" name="<ww:text name="'common.forms.next'"/>" value="<ww:text name="'common.forms.next'"/> >>"
           accessKey="<ww:text name="'common.forms.submit.accesskey'"/>"
           title="<ww:text name="'common.forms.submit.tooltip'">
           <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
            <ww:param name="'value1'"><%=BrowserUtils.getModifierKey()%></ww:param>
           </ww:text>"
        />
        </span>
    </div>
</ww:if>
    <div class="toolbar-group">
        <span class="toolbar-item toolbar-item-link">
            <input class="toolbar-trigger" type="button"  id="<ww:text name="'common.forms.cancel'"/>" name="<ww:text name="'common.forms.cancel'"/>" value="<ww:text name="'common.forms.cancel'"/>" onclick="location.href='BulkCancelWizard.jspa'"/>
        </span>
    </div>
</div>