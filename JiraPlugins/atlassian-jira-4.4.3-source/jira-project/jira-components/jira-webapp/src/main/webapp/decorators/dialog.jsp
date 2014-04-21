<%@ taglib uri="webwork" prefix="ww"%>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib prefix="decorator" uri="sitemesh-decorator" %>
<fieldset class="hidden parameters">
    <input type="hidden" id="closelink" value="<ww:text name="'admin.common.words.close'"/>"/>
</fieldset>
<div class="aui-dialog-content">
    <decorator:body />
</div>