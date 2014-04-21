<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<ww:if test="/showOpsBar() == true">
    <div class="command-bar">
        <div class="ops-cont">
            <div class="ops-menus aui-toolbar">
                <div class="toolbar-split toolbar-split-left">
                    <ww:property value="/editOrLoginLink">
                        <ww:if test=". != null">
                            <ul class="toolbar-group">
                                <li class="toolbar-item">
                                    <a id="<ww:property value="./id"/>"
                                       title="<ww:property value="./title"/>"
                                       class="toolbar-trigger <ww:property value="./styleClass"/>"
                                       href="<ww:property value="./url"/>"><ww:if test="./id == 'edit-issue'"><span class="icon butt-edit"></span></ww:if><span class="trigger-text"><ww:property value="./label"/></span></a>
                                </li>
                            </ul>
                        </ww:if>
                    </ww:property>
                    <ww:iterator value="/opsBarUtil/groups" id="group" status="'groupStatus'">
                        <ww:property value="/opsBarUtil/promotedLinks(.)">
                            <ww:if test="./empty == false">
                                <ul id="opsbar-<ww:property value="@group/id"/>" class="toolbar-group <ww:if test="@groupStatus/first = true && /showEdit() == false && /showLogin == false()"> first</ww:if>">
                                    <ww:iterator value="." status="'status'" id="curLink">
                                        <li class="toolbar-item">
                                            <a id="<ww:property value="./id"/>" title="<ww:property value="/opsBarUtil/titleForLink(.)"/>" class="<ww:if test="./id == 'comment-issue'">inline-comment </ww:if>toolbar-trigger <ww:property value="./styleClass"/>" href="<ww:property value="./url"/>"><ww:property value="/opsBarUtil/labelForLink(.)"/></a>
                                        </li>
                                    </ww:iterator>
                                    <ww:property value="/opsBarUtil/nonEmptySectionsForGroup(@group)">
                                        <ww:if test="./empty == false">
                                            <li class="toolbar-item toolbar-dropdown">
                                                <a id="<ww:property value="@group/id"/>_more" class="toolbar-trigger js-default-dropdown" href="#"><span class="dropdown-text"><ww:if test="@group/label != null"><ww:property value="@group/label"/></ww:if><ww:else><ww:text name="'common.concepts.more'"/></ww:else></span><span class="icon drop-menu"></span></a>
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
                                                <li class="toolbar-item toolbar-dropdown disabled">
                                                    <span id="<ww:property value="@group/id"/>_more" class="toolbar-trigger">
                                                        <span class="dropdown-text"><ww:property value="@group/label"/></span>
                                                        <span class="icon drop-menu"></span>
                                                    </span>
                                                </li>
                                            </ww:if>
                                        </ww:else>
                                    </ww:property>
                                </ul>
                            </ww:if>
                        </ww:property>
                    </ww:iterator>
                </div>
                <div class="toolbar-split toolbar-split-right">
                    <ul class="toolbar-group pluggable-ops">
                    <ww:property value="/toolLinks">
                        <ww:if test=".">
                        <ww:iterator value=".">
                            <li class="toolbar-item">
                                <a id="<ww:property value="./id"/>" title="<ww:property value="/opsBarUtil/titleForLink(.)"/>" class="toolbar-trigger" rel="nofollow" href="#"><span class="icon <ww:property value="./styleClass"/>"></span><ww:property value="./label"/></a>
                            </li>
                        </ww:iterator>
                        </ww:if>
                    </ww:property>
                    <ww:property value="/issueViews">
                        <ww:if test=".">
                            <li class="toolbar-item toolbar-dropdown">
                                <a href="#" title="<ww:text name="'admin.issue.views.plugin.tooltip'"/>" class="toolbar-trigger js-default-dropdown"><span class="dropdown-text"><span class="icon icon-view"></span><ww:text name="'common.concepts.views'"/></span><span class="icon drop-menu"></span></a>

                                <div class="aui-list hidden">
                                    <ul class="aui-first aui-last aui-list-section">
                                        <ww:iterator value=".">
                                            <li class="aui-list-item">
                                                <a class="aui-list-item-link" rel="nofollow" href="<%=request.getContextPath()%><ww:property value="/urlForIssueView(.)"/>"><ww:property value="./name"/></a>
                                            </li>
                                        </ww:iterator>
                                    </ul>
                                </div>
                            </li>
                        </ww:if>
                    </ww:property>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</ww:if>
