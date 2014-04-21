<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>

<ww:if test="/filterOperationsBean/hasOperation == true">
    <div class="module">
        <div class="mod-header">
            <h3><ww:text name="'common.words.operations'"/></h3>
        </div>
        <div class="mod-content" id="filteroperations">
            <ul class="item-details">
            <ww:if test="/filterOperationsBean/showInvalid == true">
                <li><ww:text name="'navigator.hidden.operation.invalid'">
                    <ww:param name="'value0'"><a id="editinvalid" href="<ww:url value="'IssueNavigator.jspa?mode=show'"/>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text></li>
            </ww:if>
            <ww:if test="/filterOperationsBean/showEdit == true">
                <li><ww:text name="'navigator.hidden.operation.edit'">
                    <ww:param name="'value0'"><a id="filtereditshares" href="<ww:url value="'EditFilter!default.jspa'"><ww:param name="'returnUrl'" value="/returnUrl"/></ww:url>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text></li>
            </ww:if>
            <ww:if test="/filterOperationsBean/showSave == true">
                <li><ww:text name="'navigator.hidden.operation.save'">
                    <ww:param name="'value0'"><a id="filtersave" href="<ww:url value="'SaveFilter!default.jspa'"><ww:param name="'returnUrl'" value="/returnUrl"/></ww:url>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text></li>
            </ww:if>
            <ww:if test="/filterOperationsBean/showSaveAs == true">
                <li><ww:text name="'navigator.hidden.operation.saveasnew'">
                    <ww:param name="'value0'"><a id="filtersaveas" href="<ww:url value="'SaveAsFilter!default.jspa'"><ww:param name="'returnUrl'" value="/returnUrl"/></ww:url>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text></li>
            </ww:if>
            <ww:if test="/filterOperationsBean/showReload == true">
                <li><ww:text name="'navigator.filter.reloadfilter'">
                    <ww:param name="'value0'"><a id="reload" href="<ww:url value="'IssueNavigator.jspa?mode=show'"><ww:param name="'requestId'" value="searchRequest/id"/><ww:param name="'returnUrl'" value="/returnUrl"/></ww:url>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text></li>
            </ww:if>
            <ww:if test="/filterOperationsBean/showSaveNew == true">
                <li><ww:text name="'navigator.hidden.operation.saveasfilter'">
                    <ww:param name="'value0'"><a id="filtersavenew" href="<ww:url value="'SaveAsFilter!default.jspa'"><ww:param name="'returnUrl'" value="/returnUrl"/></ww:url>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text></li>
            </ww:if>
            <ww:if test="/filterOperationsBean/showViewSubscriptions == true">
                <li><ww:text name="'navigator.hidden.operation.subscription'">
                    <ww:param name="'value0'"><a id="filterviewsubscriptions" href="<ww:url value="'ViewSubscriptions.jspa'"><ww:param name="'filterId'" value="searchRequest/id" /><ww:param name="'returnUrl'" value="/returnUrl"/></ww:url>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text></li>
            </ww:if>
            </ul>
        </div>
    </div>
</ww:if>
