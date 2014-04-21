<%@ taglib uri="webwork" prefix="ww" %>
<%@ include file="/includes/js/multipickerutils.jsp" %>
<p align=center>
    <ww:if test="filter/start > 0">
        <a href="javascript:moveToPage(<ww:property value="filter/previousStart" />)">&lt&lt; <ww:text name="'common.words.previous'" /></a>
    </ww:if>
    <ww:else>
        &lt&lt; <ww:text name="'common.words.previous'" />
    </ww:else>
    <ww:property value = "pager/pages(/browsableItems)">
    <ww:if test="size > 1">
        <ww:iterator value="." status="'pagerStatus'">
            <ww:if test="currentPage == true"><b><ww:property value="pageNumber" /></b></ww:if>
            <ww:else>
                <a href="javascript:moveToPage(<ww:property value="start" />)"><ww:property value="pageNumber" /></a>
            </ww:else>
            <ww:if test="@pagerStatus/last == false"> | </ww:if>
        </ww:iterator>
    </ww:if>
    </ww:property>
    <ww:if test="filter/end < /browsableItems/size">
        <a href="javascript:moveToPage(<ww:property value="filter/nextStart" />)"><ww:text name="'common.words.next'" /> &gt;&gt;</a>
    </ww:if>
    <ww:else>
        <ww:text name="'common.words.next'" /> &gt;&gt;
    </ww:else>
</p>
