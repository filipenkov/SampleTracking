<%@ page import="com.atlassian.jira.issue.Issue" %>
<%@ page import="com.atlassian.jira.web.action.issue.ViewIssue" %>
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
                        <a id="previous-issue" href="<%= request.getContextPath() %>/browse/<ww:property value="./previousKey" />" title="<ww:text name="'navigator.previous.title'"/> '<ww:property value="./previousKey" />'">
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
                                <a id="return-to-search" href="<%=request.getContextPath()%>/secure/IssueNavigator.jspa" title="<ww:text name="'navigator.return.search'"/>"><ww:text name="'navigator.return.search'"/></a>
                            </ww:param>
                        </ww:text>
                    </li>
                <ww:if test="/nextPreviousPager/nextKey != null">
                    <li class="next">
                        <a id="next-issue" href="<%= request.getContextPath() %>/browse/<ww:property value="./nextKey" />" title="<ww:text name="'navigator.next.title'"/> '<ww:property value="./nextKey" />'">
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
<h1 class="item-name avatar" <ww:if test="./avatar != null">style="background-image:url(<%= request.getContextPath() %>/secure/projectavatar?pid=<ww:property value="./id"/>&amp;avatarId=<ww:property value="./avatar/id"/>&amp;size=large)"</ww:if>>
        <ww:if test="./avatar != null">
            <img id="project-avatar" alt="" class="project-avatar-48" height="48" src="<%= request.getContextPath() %>/secure/projectavatar?pid=<ww:property value="./id"/>&amp;avatarId=<ww:property value="./avatar/id"/>&amp;size=large" width="48" />
        </ww:if>
        <span><ww:property value="./name"/></span>
</h1>
</ww:property>

<ul class="breadcrumbs">
    <li><a id="project-name-val" href="<%= request.getContextPath() %>/browse/<ww:property value="/project/string('key')" />"><ww:property value="/project/string('name')"/></a></li>
    <ww:if test="/subTask == true">
        <ww:property value="/parentIssueObject" id="parentIssueObject">
            <ww:if test="@permissionCheck/issueVisible(.) == true">
                <li><a title="<ww:property value="./summary" escape="true"/>" id="parent_issue_summary" href="<%= request.getContextPath() %>/browse/<ww:property value="./key"/>"><ww:property value="./key"/> <%= TextUtil.escapeHTML(StringUtils.abbreviate(((Issue)pageContext.getAttribute("parentIssueObject")).getSummary(), 40)) %></a></li>
            </ww:if>
            <ww:else>
                <li><ww:property value="./key"/></li>
            </ww:else>
        </ww:property>
    </ww:if>
    <li><a id="key-val" rel="<ww:property value="string('id')" />" href="<%= request.getContextPath() %>/browse/<ww:property value="string('key')"/>"><ww:property value="string('key')"/></a></li>
</ul>

<h2 id="issue_header_summary" class="item-summary"><a href="<%= request.getContextPath() %>/browse/<ww:property value="string('key')"/>"><ww:property value="string('summary')"/></a></h2>
