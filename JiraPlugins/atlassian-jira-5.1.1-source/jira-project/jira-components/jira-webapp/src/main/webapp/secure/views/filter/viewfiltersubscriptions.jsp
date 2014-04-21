<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'common.concepts.managefilters'"/></title>
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
            <page:applyDecorator name="jirapanel">
                <page:param name="title">
                    <ww:text name="'subscriptions.title'"><ww:param name="'value0'"><ww:property value="filterName"/></ww:param></ww:text>
                </page:param>
                <page:param name="description">
                    <ww:text name="'subscriptions.list'"/>.<br>

                    <a href="<ww:url value="'FilterSubscription!default.jspa'"><ww:param name="'filterId'" value="filterId" /></ww:url>"><b><ww:text name="'subscriptions.add'"/></b></a><br>

                    <ww:text name="'subscriptions.viewAllFilters'">
                        <ww:param name="'value0'"><a href="<ww:url page="ManageFilters.jspa"/>"><b></ww:param>
                        <ww:param name="'value1'"></b></a></ww:param>
                    </ww:text>
                    <ww:if test="mailConfigured == false && subscriptionCount > 0">
                        <aui:component template="auimessage.jsp" theme="'aui'">
                            <aui:param name="'messageType'">warning</aui:param>
                            <aui:param name="'messageHtml'">
                                <p><ww:text name="'filters.no.mail.configured'"/></p>
                            </aui:param>
                        </aui:component>
                    </ww:if>
                </page:param>
                <page:param name="width">100%</page:param>
            </page:applyDecorator>
            <ww:if test="subscriptions != null && subscriptions/size > 0">
                <table class="aui aui-table-rowhover">
                    <thead>
                        <tr>
                            <th width=25%>
                                <ww:text name="'subscriptions.subscriber'"/>
                            </th>
                            <th width=25%>
                                <ww:text name="'subscriptions.subscribed'"/>
                            </th>
                            <th>
                                <ww:text name="'filtersubscription.field.schedule'"/>
                                <ww:component name="'issue_filters_subscribing'" template="help.jsp">
                                    <ww:param name="'noalign'" value="'true'"/>
                                </ww:component>
                            </th>
                            <th>
                                <ww:text name="'subscriptions.lastSent'"/>
                            </th>
                            <th>
                                <ww:text name="'subscriptions.nextSend'"/>
                            </th>
                            <th width=1%>
                                <ww:text name="'common.words.operations'"/>
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                    <ww:iterator value="subscriptions" status="'status'">
                        <tr>
                            <td>
                                <ww:property value="subscriber(.)"/>
                            </td>
                            <td>
                                <ww:if test="string('group') != null && string('group')/length > 0">
                                        <ww:if test="/groupValid(.) == false"><span class="warning" title="<ww:text name="'admin.projects.group.invalid'"/>"></ww:if>
                                        <ww:property value="string('group')"/>
                                        <ww:if test="/groupValid(.) == false"></span></ww:if>
                                </ww:if>
                                <ww:else>
                                        <ww:property value="subscriber(.)"/>
                                </ww:else>
                            </td>
                            <td class="nowrap"><span title="<ww:property value="/cronTooltip(.)"/>"><ww:property value="prettySchedule(.)" /></span></td>
                            <td class="nowrap"><ww:property value="lastSent(.)" /></td>
                            <td class="nowrap"><ww:property value="nextSend(.)" /></td>
                            <td class="nowrap">
                            <ww:if test="loggedInUserIsOwner(.) == true" >
                                <ul class="operations-list">
                                    <li><a id="edit_subscription" href="<ww:url value="'FilterSubscription!default.jspa'"><ww:param name="'subId'" value="long('id')" /><ww:param name="'filterId'" value="filterId" /></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                                    <li><a href="<ww:url value="'DeleteSubscription.jspa'"><ww:param name="'subId'" value="long('id')" /><ww:param name="'filterId'" value="filterId" /></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                                <ww:if test="mailConfigured == true">
                                    <li><a href="<ww:url value="'RunSubscription.jspa'"><ww:param name="'subId'" value="long('id')" /><ww:param name="'filterId'" value="filterId" /></ww:url>"><ww:text name="'common.forms.run.now'"/></a></li>
                                </ww:if>
                                </ul>
                            </ww:if>
                            </td>
                        </tr>
                    </ww:iterator>
                    </tbody>
                </table>
            </ww:if>
            <ww:else>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'subscriptions.nosubs'"/></p>
                    </aui:param>
                </aui:component>
            </ww:else>
        </div>
    </div>
</body>
</html>
