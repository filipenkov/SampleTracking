<%@ page import="com.atlassian.jira.issue.Issue" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="webwork.util.TextUtil" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>

<ww:bean id="fieldVisibility" name="'com.atlassian.jira.web.bean.FieldVisibilityBean'"/>
<ww:bean id="permissionCheck" name="'com.atlassian.jira.web.bean.PermissionCheckBean'"/>

<ww:if test="/searchRequest != null">
    <ww:property value="/nextPreviousPager">
        <ww:if test="/nextPreviousPager/hasCurrentKey == true">
            <ul class="ops page-navigation">
            <ww:if test="/nextPreviousPager/previousKey != null">
                <li class="previous">
                    <a id="previous-issue" href="<ww:url value="'/browse/' + ./previousKey" atltoken="false" />" title="<ww:text name="'navigator.previous.title'"/> '<ww:property value="./previousKey" />'">
                        <span class="icon icon-page-prev"><span><ww:text name="'navigator.previous.title'"/> '<ww:property value="./previousKey" />'</span></span>
                    </a>
                </li>
            </ww:if>
            <ww:else>
                <li class="previous">
                    <span class="icon icon-page-prev-deactivated" title="<ww:text name="'pager.results.firstpage'"/>"></span>
                </li>
            </ww:else>
                <li class="showing">
                    <ww:text name="'pager.results.displayissues.short'">
                        <ww:param name="'value0'"><ww:property value="./currentPosition"/></ww:param>
                        <ww:param name="'value1'"><ww:property value="./currentSize"/>
                            <a id="return-to-search" href="<ww:url value="'/secure/IssueNavigator.jspa'" atltoken="false" />" title="<ww:text name="'navigator.return.search'"/>"><ww:text name="'navigator.return.search'"/></a>
                        </ww:param>
                    </ww:text>
                </li>
            <ww:if test="/nextPreviousPager/nextKey != null">
                <li class="next">
                    <a id="next-issue" href="<ww:url value="'/browse/' + ./nextKey" atltoken="false" />" title="<ww:text name="'navigator.next.title'"/> '<ww:property value="./nextKey" />'">
                        <span class="icon icon-page-next"><span><ww:text name="'navigator.next.title'"/> '<ww:property value="./nextKey" />'</span></span>
                    </a>
                </li>
            </ww:if>
            <ww:else>
                <li class="next">
                    <span class="icon icon-page-next-deactivated" title="<ww:text name="'pager.results.lastpage'"/>"></span>
                </li>
            </ww:else>
            </ul>
        </ww:if>
    </ww:property>
</ww:if>
<ww:property value="/issueObject/projectObject">
    <ww:if test="./avatar != null">
        <div id="heading-avatar">
            <img id="project-avatar" alt="<ww:property value="/project/name"/>" class="project-avatar-48" height="48" src="<ww:url value="'/secure/projectavatar'" atltoken="false"><ww:param name="'pid'" value="./id" /><ww:param name="'avatarId'" value="./avatar/id" /><ww:param name="'size'" value="'large'" /></ww:url>" width="48" />
        </div>
    </ww:if>
</ww:property>
<ul class="breadcrumbs">
    <li><a id="project-name-val" href="<ww:url value="'/browse/' + /project/string('key')" atltoken="false" />"><ww:property value="/project/string('name')"/></a></li>
    <ww:if test="/subTask == true">
        <ww:property value="/parentIssueObject" id="parentIssueObject">
            <ww:if test="@permissionCheck/issueVisible(.) == true">
                <li><a title="<ww:property value="./summary" escape="true"/>" id="parent_issue_summary" href="<ww:url value="'/browse/' + ./key" atltoken="false" />"><ww:property value="./key"/> <%= TextUtil.escapeHTML(StringUtils.abbreviate(((Issue)pageContext.getAttribute("parentIssueObject")).getSummary(), 40)) %></a></li>
            </ww:if>
            <ww:else>
                <li><ww:property value="./key"/></li>
            </ww:else>
        </ww:property>
    </ww:if>
    <li><a id="key-val" rel="<ww:property value="string('id')" />" href="<ww:url value="'/browse/' + string('key')" atltoken="false" />"><ww:property value="string('key')"/></a></li>
</ul>
<h1 id="summary-val">
    <ww:if test="/useKickAss() == true"><ww:property value="string('summary')"/></ww:if>
    <ww:else><a href="<ww:url value="'/browse/' + string('key')" atltoken="false" />"><ww:property value="string('summary')"/></a></ww:else>
</h1>