<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ include file="/template/standard/controlheader.jsp" %>

<div class="formOne">
    <ww:if test="/customAttachmentPath != null">
        <fieldset class="hidden parameters">
            <input type="hidden" id="admin.attachmentsettings.custompath.migration.confirmation" value="<ww:text name="'admin.attachmentsettings.custompath.migration.confirmation'"/>">
        </fieldset>
        <script type="text/javascript">
            jQuery(function()
            {
                var promptMsg = function(e)
                {
                    var msg = AJS.params['admin.attachmentsettings.custompath.migration.confirmation'];
                    if (! confirm(msg))
                    {
                        e.preventDefault();
                        e.stopPropagation();
                    }
                    else
                    {
                        jQuery('#attachmentPathOption_DEFAULT').unbind('click', arguments.callee);
                        jQuery('#attachmentPathOption_DISABLED').unbind('click', arguments.callee);
                    }
                };
                jQuery('#attachmentPathOption_DEFAULT').bind('click', promptMsg);
                jQuery('#attachmentPathOption_DISABLED').bind('click', promptMsg);
            });
        </script>

        <div>
            <input class="radio" type="radio" id="attachmentPathOption_CUSTOM" name="attachmentPathOption" value="CUSTOM" checked="CHECKED" disabled="disabled"/>
            <label for="attachmentPathOption_CUSTOM"><ww:text name="'admin.attachmentsettings.usecustompath'"/></label>

            <div class="description">
                <p><ww:text name="'setup.attachments.path.msg'"/> :
                    <em><ww:property value="/customAttachmentPath"/></em></p>
            </div>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'helpKey'">JRA21004</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.attachmentsettings.custompath.migration.msg'"/></p>
                </aui:param>
            </aui:component>
        </div>
    </ww:if>
    <div>
        <input class="radio" type="radio" id="attachmentPathOption_DEFAULT" name="attachmentPathOption" value="DEFAULT"
               <ww:if test="attachmentPathOption == 'DEFAULT'">checked="checked"</ww:if> />
        <label for="attachmentPathOption_DEFAULT"><ww:text name="'admin.attachmentsettings.usedefaultpath'"/></label>

        <div class="description">
            <ww:text name="'setup.attachments.path.msg'"/> : <em><ww:property value="/defaultAttachmentPath"/></em>
        </div>
    </div>
    <div>
        <input class="radio" type="radio" id="attachmentPathOption_DISABLED" name="attachmentPathOption" value="DISABLED"
               <ww:if test="attachmentPathOption == 'DISABLED'">checked="checked"</ww:if>" />
        <label for="attachmentPathOption_DISABLED"><ww:text name="'admin.attachmentsettings.disableattachments'"/></label>
    </div>
</div>

<%@ include file="/template/standard/controlfooter.jsp" %>
