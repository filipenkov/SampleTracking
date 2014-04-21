<%@ page import="com.atlassian.seraph.util.RedirectUtils" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<ww:if test="/showOpsBar() == true">
    <div class="command-bar">
        <div class="ops-cont">
            <div class="ops-menus">
                <ww:if test="/showEdit() == true">
                    <ul id="opsbar-edit" class="first ops">
                        <li class="last">
                            <a id="editIssue" title="<ww:text name="'admin.issue.operations.plugin.edit.issue.name'"/>" class="issueaction-edit-issue button edit first last" href="<%=request.getContextPath()%>/secure/EditIssue!default.jspa?id=<ww:property value="/issueObject/id" />"><span class="icon butt-edit"><span><ww:text name="'common.forms.edit'"/> - <ww:property value="/issueObject/key" /></span></span><ww:text name="'common.forms.edit'"/></a>
                        </li>
                    </ul>
                </ww:if>
                <ww:elseIf test="/showLogin() == true">
                    <ul id="opsbar-login" class="first ops">
                        <li id="ops-login" class="last">
                            <a id="ops-login-lnk" class="button first last" href="<%= RedirectUtils.getLinkLoginURL(request)%>"><ww:text name="'common.concepts.login'"/></a>
                        </li>
                    </ul>
                </ww:elseIf>
                <ww:iterator value="/opsBarUtil/groups" id="group" status="'groupStatus'">
                    <ww:property value="/opsBarUtil/promotedLinks(.)">
                        <ww:if test="./empty == false">
                            <ul id="opsbar-<ww:property value="@group/id"/>" class="ops <ww:if test="@groupStatus/first = true && /showEdit() == false && /showLogin == false()"> first</ww:if>">
                                <ww:iterator value="." status="'status'" id="curLink" >
                                    <li <ww:if test="@status/last == true && /opsBarUtil/showMoreLinkforGroup(@group) == false">class="last"</ww:if>>
                                        <a id="<ww:property value="./id"/>" title="<ww:property value="/opsBarUtil/titleForLink(.)"/>" class="button <ww:property value="./styleClass"/> <ww:property value="@group/id"/><ww:if test="@status/first == true"> first</ww:if><ww:if test="@status/last == true && /opsBarUtil/showMoreLinkforGroup(@group) == false"> last</ww:if>" href="<ww:property value="./url"/>"><ww:property value="/opsBarUtil/labelForLink(.)"/></a>
                                    </li>
                                </ww:iterator>
                                <ww:property value="/opsBarUtil/nonEmptySectionsForGroup(@group)">
                                    <ww:if test="./empty == false">
                                        <li class="last">
                                                <a id="<ww:property value="@group/id"/>_more" class="button more last drop" href="#"><ww:if test="@group/label != null"><ww:property value="@group/label"/></ww:if><ww:else><ww:text name="'common.concepts.more'"/></ww:else><span class="icon drop-menu"><span><ww:text name="'common.concepts.more'"/></span></span></a>
                                                <div class="aui-list hidden">
                                                <ww:iterator value="." id="section" status="'sectionStatus'">
                                                    <ul class="aui-list-section <ww:if test="@sectionStatus/first == true">aui-first</ww:if><ww:if test="@sectionStatus/last == true"> aui-last</ww:if>">
                                                        <ww:iterator value="/opsBarUtil/nonPromotedLinksForSection(@group, @section)" id="curLink">
                                                            <li class="aui-list-item">
                                                                <a class="aui-list-item-link <ww:property value="./styleClass"/> <ww:property value="@group/id"/>" id="<ww:property value="./id"/>" title="<ww:property value="/opsBarUtil/titleForLink(.)"/>" href="<ww:property value="./url"/>"><ww:property value="/opsBarUtil/labelForLink(.)"/></a>
                                                            </li>
                                                        </ww:iterator>
                                                    </ul>
                                                </ww:iterator>
                                            </div>
                                        </li>
                                    </ww:if>
                                    <ww:else>
                                        <ww:if test="@group/label != null">
                                            <li class="last">
                                                <div>
                                                    <span id="<ww:property value="@group/id"/>_more" class="button disabled more last drop" ><ww:property value="@group/label"/><span class="icon drop-menu"><span><ww:text name="'common.concepts.more'"/></span></span></span>
                                                </div>
                                            </li>
                                        </ww:if>
                                    </ww:else>
                                </ww:property>
                            </ul>
                        </ww:if>
                    </ww:property>
                </ww:iterator>
                <ww:property value="/issueViews">
                    <ww:if test=".">
                        <ul class="ops" id="view-drop">
                            <li>
                                <div class="aui-dd-parent">
                                    <a href="#" title="<ww:text name="'admin.issue.views.plugin.tooltip'"/>" class="drop lnk aui-dd-link standard icon-views"><span><ww:text name="'common.concepts.views'"/></span></a>
                                        <div class="aui-list hidden">
                                        <ul class="aui-first aui-last aui-list-section">
                                            <ww:iterator value=".">
                                                <li class="aui-list-item">
                                                    <a class="aui-list-item-link" rel="nofollow" href="<%=request.getContextPath()%><ww:property value="/urlForIssueView(.)"/>"><ww:property value="./name"/></a>
                                                </li>
                                            </ww:iterator>
                                        </ul>
                                    </div>
                                </div>
                            </li>
                        </ul>
                    </ww:if>
                </ww:property>
            </div>
        </div>
    </div>          
</ww:if>
