<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'filtersubscription.title'"/></title>
    <content tag="section">find_link</content>
</head>
<body class="page-type-issuenav">
    <header>
        <ww:if test="(/hasAnyErrors == false && /searchResults) || /mode == 'hide'">
            <jsp:include page="/includes/navigator/table/header.jsp"/>
        </ww:if>
        <ww:else>
            <h1>
                <ww:property value="text('navigator.title')"/><ww:if test="searchRequest/name"> &mdash; <ww:property value="searchRequest/name"/></ww:if>
            </h1>
        </ww:else>
    </header>
    <div id="issuenav" class="content-container<ww:if test="/conglomerateCookieValue('jira.toggleblocks.cong.cookie','lhc-state')/contains('#issuenav') == true"> lhc-collapsed</ww:if>">
        <div class="content-related">
            <a class="toggle-lhc" href="#" title="<ww:text name="'jira.issuenav.toggle.lhc'" />"><ww:text name="'jira.issuenav.toggle.lhc'" /></a>
            <jsp:include page="/includes/navigator/summary-pane.jsp" />
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator id="filter-subscription" name="auiform">
                <page:param name="action">FilterSubscription.jspa</page:param>
                <page:param name="method">post</page:param>
                <page:param name="submitButtonText"><ww:property value="submitName" /></page:param>
                <page:param name="submitButtonName"><ww:property value="submitName" /></page:param>
                <page:param name="cancelLinkURI"><ww:property value="cancelStr"/></page:param>

                <aui:component template="formHeading.jsp" theme="'aui'">
                    <aui:param name="'text'"><ww:text name="'filtersubscription.title'"/></aui:param>
                </aui:component>

                <ww:if test="hasGroupPermission == true">
                    <page:applyDecorator name="auifieldgroup">
                        <aui:select label="text('filtersubscription.field.recipients')" name="'groupName'" list="groups" listKey="'.'" listValue="'.'" theme="'aui'">
                            <aui:param name="'defaultOptionText'">
                                <ww:text name="'filtersubscription.personal.sub'" />
                            </aui:param>
                            <aui:param name="'defaultOptionValue'" value="''"/>
                        </aui:select>
                    </page:applyDecorator>
                </ww:if>

                <page:applyDecorator name="auifieldset">
                    <page:param name="type">group</page:param>

                    <page:applyDecorator name="auifieldgroup">
                        <page:param name="type">checkbox</page:param>
                        <aui:checkbox name="'emailOnEmpty'" label="text('filtersubscription.emailEmptyResults')" fieldValue="true" theme="'aui'"/>
                    </page:applyDecorator>
                </page:applyDecorator>

                <ww:if test="lastRunStr">
                    <page:applyDecorator name="auifieldgroup">
                        <aui:component label="text('subscriptions.lastSent')" value="lastRunStr" template="formFieldValue.jsp" theme="'aui'" />
                    </page:applyDecorator>
                </ww:if>

                <ww:if test="nextRunStr">
                    <page:applyDecorator name="auifieldgroup">
                        <aui:component label="text('subscriptions.nextSend')" value="nextRunStr" template="formFieldValue.jsp" theme="'aui'" />
                    </page:applyDecorator>
                </ww:if>

                <ui:component name="'cron.editor.name'" label="text('filtersubscription.field.schedule')" template="croneditor.jsp">
                    <ui:param name="'cronEditorBean'" value="/cronEditorBean"/>
                    <ui:param name="'parameterPrefix'">filter.subscription.prefix</ui:param>
                </ui:component>

                <aui:component name="'lastRun'" template="hidden.jsp" theme="'aui'" />
                <aui:component name="'nextRun'" template="hidden.jsp" theme="'aui'"/>
                <aui:component name="'subId'" template="hidden.jsp" theme="'aui'"/>
                <aui:component name="'filterId'" template="hidden.jsp" theme="'aui'"/>
            </page:applyDecorator>

        </div>
    </div>
</body>
</html>

