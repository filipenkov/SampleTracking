<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'filtersubscription.title'"/></title>
    <content tag="section">find_link</content>
</head>
<body class="nl iss-nav">

    <div class="item-header">
        <ww:if test="(/hasAnyErrors == false && /searchResults) || /mode == 'hide'">
            <jsp:include page="/includes/navigator/table/header.jsp"/>
        </ww:if>
        <ww:else>
            <h1 class="item-summary">
                <ww:property value="text('navigator.title')"/><ww:if test="searchRequest/name"> &mdash; <ww:property value="searchRequest/name"/></ww:if>
            </h1>
        </ww:else>
    </div>
    <div id="iss-wrap" <ww:if test="/conglomerateCookieValue('jira.toggleblocks.cong.cookie','lhc-state')/contains('#iss-wrap') == true">class="lhc-collapsed"</ww:if>>
        <div id="main-content">
            <div class="column" id="primary" >
            <a class="toggle-lhc" href="#" title="<ww:text name="'jira.issuenav.toggle.lhc'" />"><ww:text name="'jira.issuenav.toggle.lhc'" /></a>
                <div class="content rounded">
                    <jsp:include page="/includes/navigator/summary-pane.jsp" />
                </div>
            </div>
            <div class="column iss-nav-form" id="secondary">
                <div class="content rounded">

                    <page:applyDecorator name="jiraform">
                        <page:param name="title"><ww:text name="'filtersubscription.title'"/></page:param>
                        <page:param name="action">FilterSubscription.jspa</page:param>
                        <page:param name="width">100%</page:param>
                        <ww:property value="subscribed" />
                        <page:param name="submitId">filter_subscription_submit</page:param>
                        <page:param name="submitName"><ww:property value="submitName" /></page:param>
                        <page:param name="cancelURI"><ww:property value="cancelStr"/></page:param>
                        <ww:if test="hasGroupPermission == true">
                            <ui:select label="text('filtersubscription.field.recipients')" name="'groupName'" list="groups" listKey="'.'" listValue="'.'" >
                                <ui:param name="'headerrow'" value="text('filtersubscription.personal.sub')" />
                                <ui:param name="'headervalue'" value="''" />
                            </ui:select>
                        </ww:if>

                        <ui:checkbox label="text('filtersubscription.field.emailZeroResults')" name="'emailOnEmpty'" fieldValue="true" >
                            <ui:param name="'description'" >
                                <ww:text name="'filtersubscription.emailEmptyResults'"/>
                            </ui:param>
                        </ui:checkbox>
                        <ww:if test="lastRunStr" >
                            <ui:component label="'Last sent'" value="lastRunStr" template="textlabel.jsp" />
                        </ww:if>
                        <ww:if test="nextRunStr" >
                            <ui:component label="'Next send'" value="nextRunStr" template="textlabel.jsp"/>
                        </ww:if>
                        <ui:component name="'lastRun'" template="hidden.jsp"/>
                        <ui:component name="'nextRun'" template="hidden.jsp"/>
                        <ui:component name="'subId'" template="hidden.jsp"/>
                        <ui:component name="'filterId'" template="hidden.jsp"/>

                        <ui:component name="'cron.editor.name'" label="text('filtersubscription.field.schedule')" template="croneditor.jsp">
                            <ui:param name="'cronEditorBean'" value="/cronEditorBean"/>
                            <ui:param name="'parameterPrefix'">filter.subscription.prefix</ui:param>
                        </ui:component>

                    </page:applyDecorator>

                </div>
            </div>
        </div>
    </div>
</body>
</html>

