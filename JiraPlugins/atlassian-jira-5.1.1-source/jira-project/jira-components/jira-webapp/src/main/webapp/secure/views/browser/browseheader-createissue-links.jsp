<%@ taglib uri="webwork" prefix="ww" %>

<ww:if test="/hasCreateIssuePermissionForProject == true">
    <div id="create-issue">
        <h2><ww:text name="'common.words.create'" />:</h2>
        <ul class="operations">
            <ww:iterator value="/popularIssueTypes">
                <li>
                    <a class="create-issue-type lnk" data-pid="<ww:property value="/project/id" />" data-issue-type="<ww:property value="./id" />"  title="<ww:property value="./descTranslation"/>" href="<ww:url value="'/secure/CreateIssue.jspa'" atltoken="false"><ww:param name="'pid'" value="/project/id" /><ww:param name="'issuetype'" value="./id" /></ww:url>"><img src="<ww:url value="./iconUrl" atltoken="false" />" alt="" width="16" height="16" /><ww:property value="./nameTranslation"/></a>
                </li>
            </ww:iterator>
            <ww:if test="/otherIssueTypes/empty == false">
                <li class="aui-dd-parent">
                    <a id="more" class="lnk aui-dd-link standard no-icon" href="#" title="<ww:text name="'browseproject.create.other.issue.type'" />"><span><ww:text name="'common.words.other.no.dots'" /></span></a>
                    <div class="aui-list hidden">
                        <ul id="more-dropdown">
                             <ww:iterator value="/otherIssueTypes">
                                <li class="aui-list-item">
                                    <a class="aui-list-item-link create-issue-type" data-pid="<ww:property value="/project/id" />" data-issue-type="<ww:property value="./id" />" title="<ww:property value="./descTranslation"/>" href="<ww:url value="'/secure/CreateIssue.jspa'" atltoken="false"><ww:param name="'pid'" value="/project/id" /><ww:param name="'issuetype'" value="./id" /></ww:url>"><img src="<ww:url value="./iconUrl" atltoken="false" />" alt="" width="16" height="16" /><ww:property value="./nameTranslation"/></a>
                                </li>
                            </ww:iterator>
                        </ul>
                    </div>
                </li>
            </ww:if>
        </ul>
    </div>
</ww:if>
