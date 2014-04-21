<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%--  // --%>
<%--  // SEARCH RESULTS SECTION HERE--%>
<%-- // --%>
<ww:if test="filterView == 'search' && tabShowing('search') == true">
    <ww:if test="searchContentOnly == false">
        <h2><ww:text name="'managefilters.search.desc'"/></h2>
        <ww:component template="help.jsp" name="'issue_filters'" >
            <ww:param name="'helpURLFragment'">#managing_filters</ww:param>
        </ww:component>
        <p><ww:text name="'managefilters.search.long.desc'"/></p>
        <%-- TODO: SEAN convert to proper aui form - nastiness at the moment with the aui class, but tabular form --%>
        <form id="filterSearchForm" class="aui" action="ManageFilters.jspa" method="get" name="filterSearchForm">
            <input type="hidden" name="filterView" value="<ww:property value="/filterView"/>"/>
            <table class="filterSearchInput" cellpadding="0" cellspacing="0">
                <tr>
                    <td class="filterSearchInputRightAligned fieldLabelArea"><ww:text name="'common.concepts.search'"/>:</td>
                    <ui:textfield label="text('common.concepts.search')" name="'searchName'" theme="'single'">
                        <ui:param name="'formname'" value="'filterSearchForm'"/>
                        <ui:param name="'mandatory'" value="false"/>
                        <ui:param name="'size'" value="50"/>
                        <ui:param name="'maxlength'" value="50"/>
                        <ui:param name="'description'" value="text('filters.search.text.desc')"/>
                    </ui:textfield>
                    <td class="fieldLabelArea" width="10%"><ww:text name="'admin.common.words.owner'"/>:</td>
                    <%--Already has a TD--%>
                    <ui:component label="text('admin.common.words.owner')" name="'searchOwnerUserName'" template="userselect.jsp" theme="'single'">
                        <ui:param name="'formname'" value="'filterSearchForm'"/>
                        <ui:param name="'mandatory'" value="false"/>
                    </ui:component>
                </tr>
                <%-- component includes its own row --%>
                <ww:if test="/userLoggedIn == true">
                    <ww:component name="'shares'" label="text('common.concepts.shared.with')" template="select-share-types.jsp" >
                        <ww:param name="'class'" value="'filterSearchInputRightAligned fieldLabelArea'"/>
                        <ww:param name="'valueColSpan'" value="3"/>
                        <ww:param name="'noJavaScriptMessage'">
                            <ww:text name="'common.sharing.no.share.javascript'"/>
                        </ww:param>
                        <ww:param name="'shareTypeList'" value="/filtersViewHelper/shareTypeRendererBeans"/>
                        <ww:param name="'dataString'" value="/filtersViewHelper/searchShareTypeJSON"/>
                        <ww:param name="'anyDescription'"><ww:text name="'common.sharing.search.template.any.desc.SearchRequest'"/></ww:param>
                    </ww:component>
                </ww:if>
                <ww:else>
                    <ui:component template="multihidden.jsp" >
                        <ui:param name="'fields'">searchShareType,groupShare,projectShare,roleShare</ui:param> <%-- TODO: why not use the back end ShareType* shit to get these? --%>
                    </ui:component>
                </ww:else>
                <tr class="buttons">
                    <td>&nbsp;</td>
                    <td colspan="3">
                        <input class="aui-button" name="Search" type="submit" value="<ww:text name="'common.concepts.search'"/>"/>
                    </td>
                </tr>
            </table>
        </form>
        <div id="filter_search_results">
    </ww:if>
    <ww:if test="/searchRequested == true && /filters/size > 0">
        <ww:component name="text('common.concepts.search')" template="filter-list.jsp">
            <ww:param name="'id'" value="'mf_browse'"/>
            <ww:param name="'filterList'" value="/filters"/>
            <ww:param name="'operations'">false</ww:param>
            <ww:param name="'shares'" value="true"/>
            <ww:param name="'favourite'" value="/canShowFavourite"/>

            <ww:param name="'sort'" value="true"/>
            <ww:param name="'sortColumn'" value="/sortColumn"/>
            <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
            <ww:param name="'linkRenderer'" value="/filterLinkRenderer"/>

            <ww:param name="'paging'" value="true"/>
            <ww:param name="'pagingMessage'">
                <ww:text name="'common.sharing.searching.results.message'">
                    <ww:param name="'value0'"><ww:property value="/startPosition"/></ww:param>
                    <ww:param name="'value1'"><ww:property value="/endPosition"/></ww:param>
                    <ww:param name="'value2'"><ww:property value="/totalResultCount"/></ww:param>
                </ww:text>
            </ww:param>
            <ww:param name="'pagingPrevUrl'" value="/previousUrl"/>
            <ww:param name="'pagingNextUrl'" value="/nextUrl"/>
            <ww:param name="'emptyMessage'"><ww:text name="/searchEmptyMessageKey"/></ww:param>
        </ww:component>
    </ww:if>
    <ww:else>
        <ww:if test="/searchRequested == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="/searchEmptyMessageKey"/></p>
                </aui:param>
            </aui:component>
        </ww:if>
    </ww:else>
    <ww:if test="searchContentOnly == false">
        </div>
    </ww:if>
</ww:if>
<%--//--%>
<%--// POPULAR RESULTS SECTION HERE--%>
<%--//--%>
<ww:elseIf test="filterView == 'popular' && tabShowing('popular') == true">
    <h2><ww:text name="'managefilters.popular.desc'"/></h2>
    <ww:component template="help.jsp" name="'issue_filters'" >
        <ww:param name="'helpURLFragment'">#managing_filters</ww:param>
    </ww:component>
    <p><ww:text name="'managefilters.popular.long.desc'"/></p>
    <ww:component name="text('common.concepts.popular')" template="filter-list.jsp">
        <ww:param name="'id'" value="'mf_popular'"/>
        <ww:param name="'filterList'" value="/filters"/>
        <ww:param name="'operations'">false</ww:param>
        <ww:param name="'shares'" value="true"/>
        <ww:param name="'favourite'" value="/canShowFavourite"/>

        <ww:param name="'sort'" value="false"/>
        <ww:param name="'sortColumn'" value="/sortColumn"/>
        <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
        <ww:param name="'linkRenderer'" value="/filterLinkRenderer"/>

        <ww:param name="'paging'" value="false"/>
        <ww:param name="'emptyMessage'"><ww:text name="'filters.no.popular'"/></ww:param>
        <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
    </ww:component>
</ww:elseIf>
<%--//--%>
<%--// MY RESULTS SECTION HERE--%>
<%--//--%>
<ww:elseIf test="filterView == 'my' && tabShowing('my') == true">
    <h2><ww:text name="'managefilters.my.desc'"/></h2>
    <ww:component template="help.jsp" name="'issue_filters'" >
        <ww:param name="'helpURLFragment'">#managing_filters</ww:param>
    </ww:component>
    <p><ww:text name="'managefilters.my.long.desc'"/></p>
    <ww:component name="text('managefilters.my')" template="filter-list.jsp">
        <ww:param name="'id'" value="'mf_owned'"/>
        <ww:param name="'filterList'" value="/filters"/>
        <ww:param name="'owner'">false</ww:param>
        <ww:param name="'favcount'">false</ww:param>
        <ww:param name="'favourite'" value="/canShowFavourite"/>
        <ww:param name="'shares'" value="true"/>
        <ww:param name="'emptyMessage'"><ww:text name="'filters.no.owned.filters'"/></ww:param>
        <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
        <ww:param name="'linkRenderer'" value="/filterLinkRenderer"/>
        <ww:param name="'operations'">true</ww:param>
        <ww:param name="'dropDownModelProvider'" value="/"/>
    </ww:component>
</ww:elseIf>
<%--// --%>
<%--// FAVOURITE RESULTS SECTION HERE--%>
<%--//--%>
<ww:elseIf test="filterView == 'favourites' && tabShowing('favourites') == true">
    <h2><ww:text name="'managefilters.favourite.desc'"/></h2>
    <ww:component template="help.jsp" name="'issue_filters'" >
        <ww:param name="'helpURLFragment'">#managing_filters</ww:param>
    </ww:component>
    <p><ww:text name="'managefilters.favourite.long.desc'"/></p>
    <aui:component id="'undo_div'" template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'cssClass'">hidden</aui:param>
    </aui:component>
    <ww:component name="text('common.favourites.favourite')" template="filter-list.jsp">
        <ww:param name="'id'" value="'mf_favourites'"/>
        <ww:param name="'filterList'" value="/filters"/>
        <ww:param name="'favcount'">false</ww:param>
        <ww:param name="'remove'">true</ww:param>
        <ww:param name="'shares'" value="true"/>
        <ww:param name="'favourite'" value="/canShowFavourite"/>
        <ww:param name="'emptyMessage'"><ww:text name="'filters.no.favourite'"/></ww:param>
        <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
        <ww:param name="'linkRenderer'" value="/filterLinkRenderer"/>
        <ww:param name="'operations'">true</ww:param>
        <ww:param name="'dropDownModelProvider'" value="/"/>
    </ww:component>
</ww:elseIf>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'filters.no.tab.permssion'"/></p>
        </aui:param>
    </aui:component>
</ww:else>
