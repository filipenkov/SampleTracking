<%@ page import="com.atlassian.jira.web.component.cron.CronEditorWebComponent" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%--
    Renders the cron editor within a jira form. The cronEditorBean is a reference to a CronEditorBean
    that the component will use the renderer the editor.
    <ww:component name="'cron.editor.name'" template="croneditor.jsp" >
        <ww:param name="'cronEditorBean'">/cronEditorBean</ww:param>
        <ww:param name="'parameterPrefix'">filter.subscriptions.prefix</ww:param>
    </ww:component>">
--%>

<%@ include file="/template/standard/controlheader.jsp" %>

<%
    CronEditorWebComponent cronEditorWebComponent = new CronEditorWebComponent();
    request.setAttribute("cronEditorWebComponent", cronEditorWebComponent);
%>
<ww:if test="parameters['cronEditorBean'] != true">
    <ww:if test="parameters['parameterPrefix'] != true">
        <ww:property
                value="@cronEditorWebComponent/html(parameters['cronEditorBean'], parameters['parameterPrefix'])"
                escape="false"/>
    </ww:if>
    <ww:else>
        <ww:property value="@cronEditorWebComponent/html(parameters['cronEditorBean'], null)" escape="false"/>
    </ww:else>
</ww:if>
<ww:else>
    You can not display the cron editor without the cron editor bean.
</ww:else>

<%@ include file="/template/standard/controlfooter.jsp" %>
