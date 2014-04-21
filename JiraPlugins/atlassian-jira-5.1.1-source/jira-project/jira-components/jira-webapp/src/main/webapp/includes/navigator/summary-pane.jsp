<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<div id="issue-filter">
<jsp:include page="/includes/navigator/filter-form/tabs.jsp" />
<jsp:include page="/includes/navigator/filter-form/filter_description.jsp" />

<ww:if test="searchRequest">
    <div class="module">
        <div class="mod-header">
            <h3><ww:text name="'navigator.hidden.summary'"/></h3>
        </div>
        <div class="mod-content" id="filter-summary">
            <jsp:include page="/includes/navigator/search-request_description.jsp" />
        </div>
    </div>
    <jsp:include page="/includes/navigator/filter-operations.jsp" />
</ww:if>

</div>
