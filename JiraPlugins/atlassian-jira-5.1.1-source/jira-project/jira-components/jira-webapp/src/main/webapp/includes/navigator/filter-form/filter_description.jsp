<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>

<ww:if test="browsableProjects == null || browsableProjects/size <= 0">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <%@ include file="/includes/noprojects.jsp" %>
        </aui:param>
    </aui:component>
</ww:if>
<ww:else>
    <%-- Shows the details about the current filter --%>
    <ww:property value="searchRequest">
        <ww:if test=". && loaded == true && remoteUser && actionName == 'IssueNavigator'">
            <div class="favourite-me" id="filter-favourite">
                <ww:component name="'favourite'" template="favourite.jsp">
                    <ww:param name="'enabled'"><ww:property value="/filterFavourite" /></ww:param>
                    <ww:param name="'entityType'">SearchRequest</ww:param>
                    <ww:param name="'entityId'"><ww:property value="./id" /></ww:param>
                    <ww:param name="'tableId'">nav</ww:param>
                    <ww:param name="'entityName'"><ww:property value="./name"/></ww:param>
                    <ww:param name="'relatedDropdown'">find_link</ww:param>
                </ww:component>
            </div>
        </ww:if>
        <ww:if test=". && loaded == true">
        <ul class="filter-description item-details" id="filter-description">
            <li title="<ww:property value="name" />">
                <dl>
                    <dt><ww:text name="'navigator.filter'"/>:</dt>
                    <dd><ww:property value="name" /></dd>
                </dl>
            </li>
            <ww:if test="description && description/length > 0">
                <li>
                    <dl>
                        <dt><ww:text name="'common.words.description'"/>:</dt>
                        <dd class="filter-detail"><ww:property value="description" /></dd>
                    </dl>
                </li>
            </ww:if>
            <ww:if test="searchRequest/ownerUserName != remoteUser/name">
                <li>
                <ww:if test="modified == true">
                    <ww:if test="/mode != 'show'">
                        <p><span class="warning"><ww:text name="'navigator.filter.modifiedsinceloading'"/></span></p>
                    </ww:if>
                    <ww:else>
                        <p>
                        <ww:if test="/navigatorTypeAdvanced == true">
                            <span class="warning"><ww:text name="'navigator.filter.modifiedsinceloading'"/></span>
                        </ww:if>
                        <ww:else>
                            <ww:text name="'navigator.filter.modifiedsinceloading.view.hide.operations'">
                                <ww:param name="'value0'">
                                    <span class="warning"></ww:param><ww:param name="'value1'"></span>
                                </ww:param>
                            </ww:text>
                        </ww:else>
                        </p>
                    </ww:else>
                </ww:if>
                <ww:if test="/filterValid == true">
                    <ww:text name="'navigator.filter.createnewfromcurrent'">
                        <ww:param name="'value0'"><a id="copyasnewfilter" href="SaveAsFilter!default.jspa"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </ww:if>
                </li>
            </ww:if>
            <ww:elseIf test="modified == true && /filterValid == true">
                <li>
                <ww:if test="/mode != 'show'">
                    <p><span class="warning"><ww:text name="'navigator.filter.modifiedsinceloading'"/></span></p>
                </ww:if>
                <ww:else>
                    <p>
                    <ww:if test="/navigatorTypeAdvanced == true">
                        <span class="warning"><ww:text name="'navigator.filter.modifiedsinceloading'"/></span>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'navigator.filter.modifiedsinceloading.view.hide.operations'">
                            <ww:param name="'value0'">
                                <span class="warning"></ww:param><ww:param name="'value1'"></span>
                            </ww:param>
                        </ww:text>
                    </ww:else>
                    </p>
                </ww:else>
                </li>
            </ww:elseIf>
        </ul>
        </ww:if>
        <ww:elseIf test="!.">
            <ul class="filter-description item-details" id="filter-description">
                <li>
                    <p><ww:text name="'navigator.filter.nofilterselected'"/></p>
                    <ww:if test="$createNew != true && $refreshFilter">
                        <%-- Only project ahas been selected - do not show "Save Filter" link --%>
                    </ww:if>
                    <ww:elseIf test="$createNew != true && /mode != 'show'">
                        <p><ww:text name="'navigator.filter.createnewsearch'">
                            <ww:param name="'value0'"><a href="IssueNavigator.jspa?mode=show&createNew=true"></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text></p>
                    </ww:elseIf>
                </li>
            </ul>
        </ww:elseIf>
    </ww:property>

    <ww:if test="/searchRequest != null && /searchRequest/loaded == false && /filterValid == true">
        <ul class="filter-description item-details">
            <li>
                <p><ww:text name="'navigator.filter.newsearch'"/>
                <ww:if test="remoteUser && actionName != 'SaveAsFilter' && $refreshFilter != true">
                    <ww:text name="'navigator.hidden.operation.saveasfilter'">
                        <ww:param name="'value0'"><a id="filtersavenew" href="SaveAsFilter!default.jspa"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </ww:if>
                </p>
            </li>
        </ul>
    </ww:if>
</ww:else>