<%@ taglib uri="webwork" prefix="ww" %>
<div class="tabwrap filter-menu tabs2">
<ul id="filterFormHeader"  class="tabs horizontal">
    <%--<li class="tab-title">--%>
        <%--<ww:text name="'navigator.tabs.filter'"/>:--%>
    <%--</li>--%>
    <ww:if test="actionName == 'IssueNavigator' && mode == 'hide'">
        <li class="active">
            <strong><ww:text name="'common.concepts.summary'"/></strong>
        </li>
    </ww:if>
    <ww:else>
        <li>
            <a href="IssueNavigator.jspa?mode=hide" id="viewfilter" class="item" title="<ww:text name="'navigator.tabs.view.linktitle'"/>" accesskey="V"><strong><ww:text name="'common.concepts.summary'"/></strong></a>
        </li>
    </ww:else>

    <ww:if test="searchRequest">
        <ww:if test="actionName == 'IssueNavigator' && mode == 'show' && $createNew != true">
            <li class="active">
                <strong><ww:text name="'navigator.tabs.edit'"/></strong>
            </li>
        </ww:if>
        <ww:else>
            <li>
                <a href="IssueNavigator.jspa?mode=show" id="editfilter" class="item" title="<ww:text name="'navigator.tabs.edit.linktitle'"/>" accesskey="E"><strong><ww:text name="'navigator.tabs.edit'"/></strong></a>
            </li>
        </ww:else>
    </ww:if>

    <ww:if test="actionName == 'IssueNavigator' && mode == 'show' && searchRequest == null">
        <li class="active">
            <strong><ww:text name="'navigator.tabs.new'"/></strong>
        </li>
    </ww:if>
    <ww:else>
        <li>
            <a id="new_filter" href="IssueNavigator.jspa?mode=show&createNew=true" class="item" title="<ww:text name="'navigator.tabs.new.linktitle'"/>" accesskey="N"><strong><ww:text name="'navigator.tabs.new'"/></strong></a>
        </li>
    </ww:else>

    <%-- note: everyone has access to Manage Filters - even anonymous users --%>
    <ww:if test="actionName == 'ManageFilters'">
        <li class="active">
            <strong><ww:text name="'navigator.tabs.manage'"/></strong>
        </li>
    </ww:if>
    <ww:else>
        <li>
            <a id="managefilters" href="ManageFilters.jspa" class="item" title="<ww:text name="'navigator.tabs.manage.linktitle'"/>" accesskey="M"><strong><ww:text name="'navigator.tabs.manage'"/></strong></a>
        </li>
    </ww:else>
</ul>
</div>
