<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>

<body>
    <div class="content-container" id="stepped-process">
        <div class="content-related">
            <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator name="jirapanel">
                <page:param name="title"><decorator:getProperty property="title" /></page:param>
                <page:param name="description">
                    <ww:if test="/rootBulkEditBean/currentStep == 1">
                        <ww:text name="'bulkedit.step1'"/>
                    </ww:if>
                    <ww:if test="/rootBulkEditBean/currentStep == 2">
                        <ww:text name="'bulkedit.step2'"/>
                    </ww:if>
                    <ww:if test="/rootBulkEditBean/currentStep == 3">
                        <ww:text name="'bulkedit.step3'"/>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'bulkedit.step4'"/>
                    </ww:else>
                </page:param>
                <page:param name="width">100%</page:param>
                <page:param name="helpURL">bulkoperations</page:param>
                <page:param name="helpURLFragment">#Bulk+Move</page:param>

                <decorator:getProperty property="instructions" />
            </page:applyDecorator>

            <form class="aui top-label" name="jiraform" action="<decorator:getProperty property="action" />" method="POST" >

                <%@include file="/secure/views/bulkedit/bulkchooseaction_submit_buttons.jsp"%>

                <decorator:body />

                <%@include file="/secure/views/bulkedit/bulkchooseaction_submit_buttons.jsp"%>
                <!-- Hidden field placed here so as not affect the buttons -->
                <ww:if test="/canDisableMailNotifications() == false">
                    <ui:component name="'sendBulkNotification'" template="hidden.jsp" theme="'single'" value="'true'" />
                </ww:if>

            </form>
        </div>
    </div>
</body>
