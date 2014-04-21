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
            <p class="issuenav-nomatches"><ww:text name="'navigator.results.nomatchingissues'"/></p>
        </aui:param>
    </aui:component>
</ww:if>

<jsp:include page="/includes/navigator/quick-search-reverse.jsp"/>

<ww:if test="(/hasErrorMessages == false && /hasErrors == false) || /mode == 'hide'" >
    <div class="results">
        <div class="results-wrap">
            <jsp:include page="/includes/navigator/results.jsp"/>
        </div>
    </div>
</ww:if>
