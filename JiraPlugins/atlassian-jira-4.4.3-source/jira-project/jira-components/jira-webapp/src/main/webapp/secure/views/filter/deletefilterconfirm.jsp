<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <ww:if test="canDelete == true"><meta name="decorator" content="navigator" /></ww:if>
    <ww:else><meta name="decorator" content="message" /></ww:else>
	<title><ww:text name="'invalid.workflow.action.title'"/></title>
</head>
<body>
<ww:if test="canDelete == true">
    <div class="content intform">
        <page:applyDecorator id="delete-filter" name="auiform">
            <page:param name="action"><ww:property value="./actionName"/>.jspa</page:param>
            <page:param name="id">delete-filter-confirm-form-<ww:property value="filterId" /></page:param>
            <page:param name="submitButtonName">Delete</page:param>
            <page:param name="showHint">true</page:param>
            <ww:property value="/hint('delete_filter')">
                <ww:if test=". != null">
                    <page:param name="hint"><ww:property value="./text" escape="false" /></page:param>
                    <page:param name="hintTooltip"><ww:property value="./tooltip" escape="false" /></page:param>
                </ww:if>
            </ww:property>     
            <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
            <page:param name="cancelLinkURI"><ww:url value="'/secure/ManageFilters.jspa'" atltoken="false"/></page:param>
            <page:param name="returnUrl"><ww:property value="./returnUrl"/></page:param>
            <aui:component template="formHeading.jsp" theme="'aui'">
                <aui:param name="'text'"><ww:text name="'deletefilter.title'"><ww:param name="'value0'"><ww:property value="filterName" /></ww:param></ww:text></aui:param>
                <aui:param name="'escape'" value="'false'" />
            </aui:component>

            <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'deletefilter.confirm'"/></p>
                    <ul>
                        <ww:if test="otherFavouriteCount == 1">
                            <li><ww:text name="'deletefilter.other.favourite.filter.one'"/></li>
                        </ww:if>
                        <ww:elseIf test="otherFavouriteCount > 1">
                            <li><ww:text name="'deletefilter.other.favourite.filter.many'"><ww:param name="'value0'"><ww:property value="otherFavouriteCount"/></ww:param></ww:text></li>
                        </ww:elseIf>
                        <ww:if test="subscriptionCount == 1">
                            <li><ww:text name="'deletefilter.view.subscriptions.one'">
                                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/ViewSubscriptions.jspa?filterId=<ww:property value="filterId" />"></ww:param>
                                <ww:param name="'value1'"></a></ww:param>
                            </ww:text>
                            </li>
                        </ww:if>
                        <ww:elseIf test="subscriptionCount > 0">
                            <li>
                                <ww:text name="'deletefilter.view.subscriptions.many'">
                                    <ww:param name="'value0'"><ww:property value="subscriptionCount"/></ww:param>
                                    <ww:param name="'value1'"><a href="<%= request.getContextPath() %>/secure/ViewSubscriptions.jspa?filterId=<ww:property value="filterId" />"></ww:param>
                                    <ww:param name="'value2'"></a></ww:param>
                                </ww:text>
                            </li>
                        </ww:elseIf>
                        <ww:else>
                            <li><ww:text name="'deletefilter.noSubs'"/></li>
                        </ww:else>
                    </ul>
                </aui:param>
            </aui:component>
            <aui:component name="'searchName'" template="hidden.jsp" theme="'aui'" />
            <aui:component name="'searchOwnerUserName'" template="hidden.jsp" theme="'aui'" />
            <aui:component name="'sortColumn'" template="hidden.jsp" theme="'aui'" />
            <aui:component name="'sortAscending'" template="hidden.jsp" theme="'aui'" />
            <aui:component name="'pagingOffset'" template="hidden.jsp" theme="'aui'" />
            <aui:component name="'filterId'" template="hidden.jsp" theme="'aui'" />
            <aui:component name="'totalResultCount'" template="hidden.jsp" theme="'aui'" />
        </page:applyDecorator>
    </div>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'titleText'"><ww:text name="'invalid.workflow.action.title'" /></aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'deletefilter.can.not.delete'"/></p>
        </aui:param>
    </aui:component>
</ww:else>
</body>
</html>
