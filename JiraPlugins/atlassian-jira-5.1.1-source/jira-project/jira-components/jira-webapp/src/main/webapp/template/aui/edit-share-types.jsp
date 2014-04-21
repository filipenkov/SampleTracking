<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:share-types");
%>
<page:applyDecorator name="auifieldgroup">
<jsp:include page="/template/aui/formFieldLabel.jsp" />
    <div id="share_display_div"></div>
<jsp:include page="/template/aui/formFieldError.jsp" />
</page:applyDecorator>

<page:applyDecorator name="auifieldgroup">
    <ww:component template="formFieldLabel.jsp" label="text('common.sharing.new.shares')" theme="'aui'"/>
    <ww:property value="parameters['shareTypeList']">
        <div id="share_div" style="display: none;">
            <ww:if test="parameters['editEnabled'] == false"><div style="display:none"></ww:if>
                <div id="share_display_component"><ww:if test=". != null && ./size > 0">
                    <div>
                    <select id="share_type_selector">
                        <ww:iterator value=".">
                            <option value="<ww:property value="./shareType"/>"><ww:property value="./shareTypeLabel"/></option>
                        </ww:iterator>
                    </select>
                    <ww:iterator value="." status="'typeStatus'">
                        <span id="share_<ww:property value="./shareType"/>" <ww:if test="@typeStatus/first == false">style="display:none"</ww:if>>
                            <ww:property value="./shareTypeEditor" escape="false"/>
                            <ww:if test="./addButtonNeeded == true">
                                <span class="addShare" id="share_add_<ww:property value="./shareType"/>"><img src="<%= request.getContextPath() %>/images/icons/16add_blue.png" />
                                <ww:text name="'common.sharing.add.share'"/></span>
                            </ww:if>
                        </span>
                    </ww:iterator>
                    <div class="fieldDescription" id="share_type_description"></div>
                </ww:if></div></div>

            <ww:if test="parameters['editEnabled'] == false"></div></ww:if>
        </div>
        <span id="shares_data" style="display:none;"><ww:property value="parameters['dataString']"/></span>
        <input id="share_type_hidden" name="shareValues" type="hidden"/>
        <img id="share_trash" height="16px" width="16px" style="display: none" class="shareTrash" src="<%= request.getContextPath() %>/images/icons/trash_16.gif"
            title="<ww:text name="'common.sharing.delete.share'"/>" alt="<ww:text name="'common.sharing.delete.share'"/>"/>
        <img id="share_icon" height="16px" width="16px" style="display: none;" class="shareIcon" src="<%= request.getContextPath() %>/images/icons/filter_public.gif"
             alt="<ww:text name="'common.sharing.share'"/>"/>
        <div class="shareItem" id="empty_share" style="display:none">
            <div title="<ww:text name="'common.sharing.shared.template.private.desc'"/>">
                <img class="shareIcon" src="<%= request.getContextPath() %>/images/icons/filter_private.gif" height="16px" width="16px" border="0"
                     alt="<ww:text name="'common.sharing.private'"/>"/><ww:text name="'common.sharing.not.shared'"/>
            </div>
        </div>

        <fieldset class="hidden parameters">
            <input type="hidden" title="paramSubmitButtonId" value="<ww:property value="parameters['submitButtonId']"/>"/>
        </fieldset>
        <script type="text/javascript">
            AJS.$(function() {
                <ww:iterator value=".">
                    <ww:iterator value="./translatedTemplates">
                        atlassian.jira.share.i18n["<ww:property value="key"/>"] = "<ww:property value="value" escape="false"/>";
                    </ww:iterator>
                </ww:iterator>
                atlassian.jira.share.i18n["common.sharing.remove.shares"] = "<ww:text name="'common.sharing.remove.shares'"/>";
                atlassian.jira.share.i18n["common.sharing.dirty.warning"] = "<ww:text name="'common.sharing.dirty.warning'"/>";
                atlassian.jira.share.registerEditShareTypes(AJS.params.paramSubmitButtonId);
            });
        </script>
    </ww:property>
</page:applyDecorator>

