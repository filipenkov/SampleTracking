<%@ taglib uri="webwork" prefix="ww" %>

<ww:if test="searchRequest/query/whereClause == null">
    <div class="searcherValue">
        <ww:text name="'navigator.hidden.allissues'"/>
    </div>
</ww:if>
<ww:elseIf test="searchRequestFitsNavigator == true">
    <ww:iterator value="/searcherGroups" >
        <ww:iterator value="./searchers" >
            <ww:property value="/searcherViewHtml(.)" escape="false" />
        </ww:iterator>
    </ww:iterator>
</ww:elseIf>
<ww:else>
    <div class="searcherValue">
        <label class="fieldLabel" for="fieldJqlQuery"><ww:text name="'jira.jql.query'"/>:</label> 
        <span id="fieldJqlQuery" class="fieldValue"><ww:property value="/searchRequestJqlString"/></span>
    </div>
</ww:else>

<ww:if test="/searchSorts != null && /searchSorts/size != 0" >
    <div class="searcherValue">
        <label class="fieldLabel"><ww:text name="'navigator.hidden.sortedby'"/>:</label>
        <span class="fieldValue"><ww:iterator value="/searchSortDescriptions(searchRequest)"><ww:property value="."/> </ww:iterator></span>
    </div>
</ww:if>
