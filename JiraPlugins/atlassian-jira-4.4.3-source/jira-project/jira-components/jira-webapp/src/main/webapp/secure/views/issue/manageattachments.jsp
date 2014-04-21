<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.web.util.CookieUtils" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:bean name="'com.atlassian.core.user.UserUtils'" id="userUtils" />
<% request.setAttribute("contextPath", request.getContextPath()); %>
<html>
<head>
    <meta content="issuesummary" name="decorator" />
    <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
    <title>[#<ww:property value="/issueObject/key" />] <ww:property value="/issueObject/summary" /></title>
</head>
<body>
<div class="item-header">
    <ww:property value="issue">
        <jsp:include page="/includes/panels/issue_headertable.jsp" />
    </ww:property>
</div>
<div id="main-content">
    <div class="active-area">
        <ww:if test="/attachable == true || /screenshotAttachable == true || /zipSupport == true">
            <div class="command-bar">
                <div class="ops-cont">
                    <ul class="ops">
                        <li id="back-lnk-section" class="last">
                            <a id="back-lnk" class="button first last" href="<%= request.getContextPath() %>/browse/<ww:property value="/issueObject/key" />"><span class="icon icon-back"><span><ww:text name="'opsbar.back.to.issue'"/></span></span><ww:text name="'opsbar.back.to.issue'"/></a>
                        </li>
                    </ul>
                    <ul class="ops">
                        <ww:if test="/attachable == true">
                            <li><a id="attach-more-files-link" class="button first<ww:if test="/screenshotAttachable == false && /zipSupport == false"> last</ww:if>" href="<ww:url page="AttachFile!default.jspa"><ww:param name="'id'" value="/issueObject/id" /><ww:param name="'returnUrl'" value="'ManageAttachments.jspa?id=' + /issueObject/id" /></ww:url>"><ww:text name="'manageattachments.attach.more.files'"/></a></li>
                        </ww:if>
                        <ww:if test="/zipSupport == true">
                            <li><a id="aszipbutton" class="button<ww:if test="/attachable == false"> first</ww:if><ww:if test="/screenshotAttachable == false"> last</ww:if>" href="<ww:property value="@contextPath"/>/secure/attachmentzip/<ww:property value="/issueObject/id"/>.zip" title="<ww:text name="'common.concepts.attachments.as.a.zip'"/>"><ww:text name="'common.concepts.attachments.as.a.zip.short'"/></a></li>
                        </ww:if>

                        <ww:if test="/screenshotAttachable == true">
                            <li>
                                <a class="button<ww:if test="/attachable == false && /zipSupport == false"> first</ww:if> last" href="<ww:url value="'AttachScreenshot!default.jspa'" >
                                    <ww:param name="'id'" value="/issueObject/id" />
                                    </ww:url>" onclick="jira.app.attachments.screenshot.openWindow(this.href); return false;">
                                    <ww:text name="'manageattachments.attach.another.screenshot'"/>
                                </a>
                            </li>
                        </ww:if>
                    </ul>
                </div>
            </div>
        </ww:if>
        <h2 id="manage-attachments-title"><ww:text name="'manageattachments.title'"/></h2>
        <div class="content">
            <div class="module" id="issue-attachments-table">
                <div class="mod-content">
                    <p><ww:text name="'manageattachments.description'"/></p>
                    <ww:property value="/issueObject">
                        <ww:if test="attachments != null && attachments/empty == false">
                            <table class="aui" border="0" cellpadding="0" cellspacing="0">
                                <thead>
                                    <tr>
                                        <th>
                                            &nbsp;
                                        </th>
                                        <th>
                                            &nbsp;
                                        </th>
                                        <th>
                                            <ww:text name="'manageattachments.file.name'"/>
                                        </th>
                                        <th>
                                            <ww:text name="'manageattachments.size'"/>
                                        </th>
                                        <th>
                                            <ww:text name="'manageattachments.mime.type'"/>
                                        </th>
                                        <th>
                                            <ww:text name="'manageattachments.date.attached'"/>
                                        </th>
                                        <th>
                                            <ww:text name="'manageattachments.author'"/>
                                        </th>
                                        <th>
                                            &nbsp;
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <ww:bean name="'com.atlassian.core.util.FileSize'" id="sizeFormatter" />
                                    <ww:iterator value="attachments" status="'status'">
                                    <tr<ww:if test="@status/odd == false"> class="alt"</ww:if>>
                                        <td><ww:property value="@status/count"/></td>
                                        <td>
                                            <ww:fragment template="attachment-icon.jsp">
                                                <ww:param name="'filename'" value="filename"/>
                                                <ww:param name="'mimetype'" value="mimetype"/>
                                            </ww:fragment>
                                        </td>
                                        <td><a href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="id" />/<ww:property value="urlEncoded(filename)" />"><ww:property value="filename" /></a></td>
                                        <td><ww:property value="@sizeFormatter/format(filesize)"/></td>
                                        <td><ww:property value="mimetype"/></td>
                                        <td class="attachment-date"><ww:property value="/outlookDate/formatDMYHMS(created)"/></td>
                                        <td><ww:if test="@userUtils/existsUser(author) == true"><ww:property value="@userUtils/user(author)/fullName"/></ww:if><ww:else><span title="<ww:text name="'admin.viewuser.user.does.not.exist.title'" />"><ww:property value="author"/></span></ww:else></td>
                                        <td class="icon">
                                            <ww:if test="/hasDeleteAttachmentPermission(./id) == true"> <a title="Delete this attachment" href="<ww:url page="DeleteAttachment!default.jspa"><ww:param name="'id'" value="/issueObject/id" /><ww:param name="'deleteAttachmentId'" value="./id" /></ww:url>" id="del_<ww:property value="./id" />" class="icon icon-delete"><span><ww:text name="'common.words.delete'"/></span></a></ww:if>
                                        </td>
                                    </tr>
                                    </ww:iterator>
                                </tbody>
                            </table>
                        </ww:if>
                        <ww:else>
                            <aui:component template="auimessage.jsp" theme="'aui'">
                                <aui:param name="'messageType'">info</aui:param>
                                <aui:param name="'messageHtml'">
                                    <p><ww:text name="'manageattachments.no.attachments.notification'"/></p>
                                </aui:param>
                            </aui:component>
                        </ww:else>
                    </ww:property>
                </div>
            </div>
        </div>
    </div>
</div>


</body>
</html>
