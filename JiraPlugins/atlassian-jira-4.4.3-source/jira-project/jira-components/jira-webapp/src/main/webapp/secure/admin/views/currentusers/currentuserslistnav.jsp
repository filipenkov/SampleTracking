<%@ taglib prefix="ww" uri="webwork" %>
<div class="results-details">
    <p class="pagination">
        <ww:if test="/pager/startPageURL">
            <a href="<ww:property value="/pager/startPageURL"/>" id="gotoStart"><img src="<%= request.getContextPath() %>/images/icons/arrow_first.gif" title="<ww:text name="'common.words.start'"/>"/></a>
        </ww:if>
        <ww:else>
            <img src="<%= request.getContextPath() %>/images/icons/first_faded.gif"/>
        </ww:else>

        <ww:if test="/pager/prevPageURL">
            <a href="<ww:property value="/pager/prevPageURL"/>"  id="gotoPrev"><img src="<%= request.getContextPath() %>/images/icons/arrow_left_small.gif" title="<ww:text name="'common.forms.previous'"/>"/></a>
        </ww:if>
        <ww:else>
            <img src="<%= request.getContextPath() %>/images/icons/arrow_left_faded.gif"/>
        </ww:else>

        <ww:if test="/pager/nextPageURL">
            <a href="<ww:property value="/pager/nextPageURL"/>"  id="gotoNext"><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" title="<ww:text name="'common.forms.next'"/>"/></a>
        </ww:if>
        <ww:else>
            <img src="<%= request.getContextPath() %>/images/icons/arrow_right_faded.gif"/>
        </ww:else>

        <ww:if test="/pager/endPageURL">
            <a href="<ww:property value="/pager/endPageURL"/>"  id="gotoEnd"><img src="<%= request.getContextPath() %>/images/icons/arrow_last.gif" title="<ww:text name="'common.words.end'"/>"/></a>
        </ww:if>
        <ww:else>
            <img src="<%= request.getContextPath() %>/images/icons/last_faded.gif"/>
        </ww:else>
    </p>
    <p class="results-count">
        <ww:text name="'admin.currentusers.data.results'">
            <ww:param name="'value0'"><ww:property value="/fromIndex"/></ww:param>
            <ww:param name="'value1'"><ww:property value="/toIndex"/></ww:param>
            <ww:param name="'value2'"><ww:property value="/pager/fullListSize"/></ww:param>
        </ww:text>
    </p>
</div>
