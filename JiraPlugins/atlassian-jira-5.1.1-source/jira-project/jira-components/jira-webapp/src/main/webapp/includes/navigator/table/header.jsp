<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager"%>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="ww" %>
<page:param name="title" />
<ww:if test="/navigatorTypeAdvanced == true">
   <page:param name="helpURL">advanced_search</page:param>
</ww:if>
<page:param name="description" />
<page:param name="width">100%</page:param>

<%-- Only show the views if there are issues that match the search criteria--%>

<div id="navigator-options">
    <ul class="operations">

        <%-- Pluggable buttons --%>
        <ww:property value="./pluggableItems" id="pluggableItems" />
        <ww:if test="@pluggableItems/empty == false">
            <ww:iterator value="@pluggableItems">
                <li class="pluggable-ops">
                    <a id="<ww:property value="./id"/>" class="lnk" title="<ww:property value="./title"/>" rel="nofollow" href="#">
                        <span class="icon <ww:property value="./styleClass"/>"></span>
                        <ww:property value="./label"/></a>
                </li>
            </ww:iterator>
        </ww:if>

    <ww:if test="/searchResults/total > 0">
        <%-- View Options dropdown --%>
        <li class="aui-dd-parent">
            <a href="#" id="viewOptions" class="lnk aui-dd-link standard icon-views"><span><ww:text name="'common.concepts.views'"/></span></a>
            <div class="aui-list hidden">
                <ul id="viewOptions-dropdown" class="aui-list-section aui-first">
                    <ww:if test="'bulk' != $view">
                        <%-- is this needed <li class="aui-list-item"><ww:text name="'navigator.results.currentview.browser'"/></li>--%>
                    </ww:if>
                    <ww:else>
                        <li class="aui-list-item"><a class="aui-list-item-link" rel="nofollow" href="<ww:url><ww:param name="'view'" value="''" /><ww:param name="'tempMax'" value="'-1'" /><ww:param name="'decorator'" value="''" /></ww:url>"><ww:text name="'navigator.results.currentview.browser'"/></a></li>
                    </ww:else>
                    <%-- all views for the issue navigator --%>

                    <ww:if test="/printable != null">
                        <li class="aui-list-item"><a class="aui-list-item-link" id="printable" rel="nofollow" href="<%= request.getContextPath() %><ww:property value="/restricted(/printable/URLWithoutContextPath(/searchRequest))"/>"><ww:text name="'navigator.results.currentview.browser.printable'"/></a></li>
                    </ww:if>

                    <ww:if test="/fullContent != null">
                        <li class="aui-list-item"><a class="aui-list-item-link" id="fullContent" rel="nofollow" href="<%= request.getContextPath() %><ww:property value="/restricted(/fullContent/URLWithoutContextPath(/searchRequest))"/>"><ww:text name="'navigator.results.currentview.browser.full'"/></a></li>
                    </ww:if>

                    <ww:if test="/xml != null">
                        <li class="aui-list-item"><a class="aui-list-item-link" id="xml" rel="nofollow" href="<%= request.getContextPath() %><ww:property value="/restricted(/xml/URLWithoutContextPath(/searchRequest))"/>"><ww:text name="'navigator.results.currentview.xml'"/></a></li>
                    </ww:if>
                </ul>
                <ww:if test="/rssIssues != null || /rssComments !=null">
                    <ul class="aui-list-section">
                    <ww:if test="/rssIssues != null">
                        <li class="aui-list-item"><a class="aui-list-item-link" id="rssIssues" rel="nofollow" href="<%= request.getContextPath() %><ww:property value="/restricted(/rssIssues/URLWithoutContextPath(/searchRequest))"/>"><ww:text name="'navigator.results.currentview.rss'"/> (<ww:text name="'navigator.results.currentview.rss.issues'"/>)</a></li>
                    </ww:if>

                    <ww:if test="/rssComments != null">
                        <li class="aui-list-item"><a class="aui-list-item-link" id="rssComments" rel="nofollow" href="<%= request.getContextPath() %><ww:property value="/restricted(/rssComments/URLWithoutContextPath(/searchRequest))"/>"><ww:text name="'navigator.results.currentview.rss'"/> (<ww:text name="'navigator.results.currentview.rss.comments'"/>)</a></li>
                    </ww:if>
                    </ul>
                </ww:if>

                <ww:if test="/word !=null">
                    <ul class="aui-list-section">
                        <li class="aui-list-item"><a class="aui-list-item-link" id="word" rel="nofollow" href="<%= request.getContextPath() %><ww:property value="/restricted(/word/URLWithoutContextPath(/searchRequest))"/>"><ww:text name="'navigator.results.currentview.word'"/></a></li>
                    </ul>
                </ww:if>

                <ww:if test="/allExcelFields !=null || /currentExcelFields !=null">
                    <ul <ww:if test="/chart == null || /remoteUser == null">class="aui-list-section aui-last"</ww:if>>
                    <ww:if test="/allExcelFields != null">
                        <li class="aui-list-item"><a class="aui-list-item-link" id="allExcelFields" rel="nofollow" href="<%= request.getContextPath() %><ww:property value="/restricted(/allExcelFields/URLWithoutContextPath(/searchRequest))"/>"><ww:text name="'navigator.results.currentview.excel'"/> (<ww:text name="'navigator.results.currentview.excel.full'"/>)</a></li>
                    </ww:if>

                    <ww:if test="/currentExcelFields != null">
                        <li class="aui-list-item"><a class="aui-list-item-link" id="currentExcelFields" rel="nofollow" href="<%= request.getContextPath() %><ww:property value="/restricted(/currentExcelFields/URLWithoutContextPath(/searchRequest))" />"><ww:text name="'navigator.results.currentview.excel'"/> (<ww:text name="'navigator.results.currentview.excel.current'"/>)</a></li>
                    </ww:if>
                    </ul>
                </ww:if>
                <ww:property value="/nonSystemSearchRequestViews">
                    <!-- We don't want to show this link if you are not logged in since the charting plugin portlets do not show to non-->
                    <ww:if test="/chart != null && /remoteUser != null" >
                        <div class="hidden">
                            <%
                                // The links for search request views are pulled from an XML file, so the charting plugin does not have access
                                // to call this method.  We should fix this...
                                WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
                                webResourceManager.requireResource("com.atlassian.jira.gadgets:searchrequestview-charts");
                            %>
                            <fieldset class="hidden parameters">
                                <ww:if test="/searchRequest/name != null && /searchRequest/modified == false" >
                                    <input type="hidden" id="filterId" value="<ww:property value="/searchRequest/id"/>">
                                </ww:if>
                                <ww:else>
                                    <input type="hidden" id="jql" value="<ww:property value="/searchRequestJqlString"/>">
                                </ww:else>
                            </fieldset>
                        </div>
                        <ul class="aui-list-section">
                            <li class="aui-list-item"><a class="aui-list-item-link" id="charts" rel="nofollow" class="lbOn" href="#"><ww:text name="'navigator.results.currentview.charts'"/></a></li>
                        </ul>
                        <ul <ww:if test="./empty == true">class="aui-list-section aui-last"</ww:if>>
                            <li class="aui-list-item"><a class="aui-list-item-link" id="onDashboard" rel="nofollow" class="lbOn" href="#"><ww:text name="'navigator.results.currentview.on.dashboard'"/></a></li>
                        </ul>
                    </ww:if>

                    <ww:if test="./empty == false">
                        <ul class="aui-list-section aui-last">
                        <ww:iterator value="." >
                           <li class="aui-list-item"><a class="aui-list-item-link" rel="nofollow" href="<%= request.getContextPath() %><ww:property value="URLWithoutContextPath(/searchRequest)" />" ><ww:property value="name" /></a></li>
                        </ww:iterator>
                        </ul>
                    </ww:if>
                </ww:property>
            </div>
        </li>

        <%-- Tool Options dropdown --%>
        <ww:property value="./toolOptions" id="toolOptions" />
        <ww:if test="@toolOptions/empty == false">
            <li class="aui-dd-parent">
                <a href="#" id="toolOptions" class="lnk aui-dd-link standard icon-tools"><span><ww:text name="'common.concepts.tools'"/></span></a>
                <div id="toolOptions-dropdown" class="aui-list hidden">
                    <ww:property value="@toolOptions/size - 1" id="lastIndex"/>
                    <ww:iterator value="@toolOptions">
                        <ww:if test="./label != null">
                            <h5><ww:property value="./label"/></h5>
                        </ww:if>
                        <ul
                            <ww:if test="@toolOptions/indexOf(.) == 0">
                                class="aui-list-section aui-first"
                            </ww:if>
                            <ww:if test="/maxIndex(@toolOptions) == @toolOptions/indexOf(.)">
                                class="aui-list-section aui-last"
                            </ww:if>
                        >
                            <ww:iterator value="./items" >
                            <li class="aui-list-item">
                                <a class="aui-list-item-link" id="<ww:property value="./id"/>" href="<%= request.getContextPath() %><ww:property value="./link"/>" title="<ww:property value="./title"/>"><ww:property value="./label"/></a>
                            </li>
                            </ww:iterator>
                        </ul>
                    </ww:iterator>
                </div>
            </li>
        </ww:if>
    </ul>

    </ww:if>
</div>

<%--These messages are there for issue operation dialogs so they can thank the user for their business--%>
<fieldset class="hidden parameters">
    <input type="hidden" id="thanks_issue_updated" value="<ww:text name="'navigator.results.thanks.updated'"/>">
    <input type="hidden" id="thanks_issue_transitioned" value="<ww:text name="'navigator.results.thanks.transitioned'"/>">
    <input type="hidden" id="thanks_issue_assigned" value="<ww:text name="'navigator.results.thanks.assigned'"/>">
    <input type="hidden" id="thanks_issue_commented" value="<ww:text name="'navigator.results.thanks.commented'"/>">
    <input type="hidden" id="thanks_issue_worklogged" value="<ww:text name="'navigator.results.thanks.worklogged'"/>">
    <input type="hidden" id="thanks_issue_voted" value="<ww:text name="'navigator.results.thanks.voted'"/>">
    <input type="hidden" id="thanks_issue_watched" value="<ww:text name="'navigator.results.thanks.watched'"/>">
    <input type="hidden" id="thanks_issue_moved" value="<ww:text name="'navigator.results.thanks.moved'"/>">
    <input type="hidden" id="thanks_issue_linked" value="<ww:text name="'navigator.results.thanks.linked'"/>">
    <input type="hidden" id="thanks_issue_cloned" value="<ww:text name="'navigator.results.thanks.cloned'"/>">
    <input type="hidden" id="thanks_issue_labelled" value="<ww:text name="'navigator.results.thanks.labelled'"/>">
    <input type="hidden" id="thanks_issue_deleted" value="<ww:text name="'navigator.results.thanks.deleted'"/>">
    <input type="hidden" id="thanks_issue_attached" value="<ww:text name="'navigator.results.thanks.attached'"/>">
</fieldset>


<div class="perm-link">
   <a id="permlink" class="icon-permalink" href="<%= request.getContextPath() %>/secure/IssueNavigator.jspa?reset=true<ww:property value="jqlQueryString" />" title="<ww:text name="'viewissue.permlink.title.navigator'"/>"><ww:text name="'viewissue.permlink'"/></a>
</div>

<div id="throbber-space">&nbsp;</div>

<h1>
    <ww:property value="text('navigator.title')"/><ww:if test="searchRequest/name"> &mdash; <span id="filter-name"><ww:property value="searchRequest/name"/></span></ww:if>
</h1>