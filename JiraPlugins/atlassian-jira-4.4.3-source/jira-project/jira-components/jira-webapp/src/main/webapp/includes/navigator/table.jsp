<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<ww:if test="(/hasAnyErrors == false && /searchResults) || /mode == 'hide'">
    <ww:if test="!/searchResults">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'navigator.results.searchnotstarted'"/></p>
            </aui:param>
        </aui:component>
    </ww:if>
</ww:if>
<ww:else>

    <p><ww:text name="'navigator.desc1'"/></p>
    <p><ww:text name="'navigator.desc2'"/></p>

    <ww:if test="/hasErrors == true">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'navigator.warning'"/></p>
            </aui:param>
        </aui:component>
    </ww:if>
</ww:else>

<ww:if test="/searchResults/total == 0">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'navigator.results.nomatchingissues'"/></p>
        </aui:param>
    </aui:component>
</ww:if>

<jsp:include page="/includes/navigator/quick-search-reverse.jsp"/>

<ww:if test="(/hasErrorMessages == false && /hasErrors == false) || /mode == 'hide'" >
    <div class="results-wrap">
        <ww:if test="/searchResults/total > 0">
            <div class="results-count">
            <ww:text name="'navigator.results.displayissues'">
                <ww:param name="'value0'"><span id="results-count-start"><ww:property value="/searchResults/niceStart" /></span></ww:param>
                <ww:param name="'value1'"><ww:property value="/searchResults/end" /></ww:param>
                <ww:param name="'value2'"><strong id="results-count-total"><ww:property value="/searchResults/total" /></strong></ww:param>
            </ww:text>
            </div>
        </ww:if>
        <jsp:include page="/includes/navigator/results.jsp"/>
        <ww:if test="/searchResults/total > 0">
            <div class="results-count">
            <ww:text name="'navigator.results.displayissues'">
                <ww:param name="'value0'"><span id="results-count-start"><ww:property value="/searchResults/niceStart" /></span></ww:param>
                <ww:param name="'value1'"><ww:property value="/searchResults/end" /></ww:param>
                <ww:param name="'value2'"><strong id="results-count-total"><ww:property value="/searchResults/total" /></strong></ww:param>
            </ww:text>
            </div>
        </ww:if>
    </div>
</ww:if>
