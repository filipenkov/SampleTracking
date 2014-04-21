<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib prefix="webwork" uri="webwork" %>
<% ComponentManager.getInstance().getWebResourceManager().requireResource("jira.webresources:jira-global"); %>
<html>
<head>
    <ww:if test="/issueExists == true"><meta content="popup" name="decorator" /></ww:if>
    <ww:else><meta content="message" name="decorator" /></ww:else>
    <title><ww:text name="'attachscreenshot.title'"/></title>
</head>
<body>
<ww:if test="/issueExists==true">
    <h3><ww:text name="'attachscreenshot.title'"/></h3>
    <p>
        <ww:text name="'attachscreenshot.description.line1'"/><br>
        <ww:text name="'attachscreenshot.description.line2'"/>
    </p>

    <ww:if test="/hasErrorMessages == 'true'">
        <ul>
            <ww:iterator value="/flushedErrorMessages">
                <li><ww:property value="." /></li>
            </ww:iterator>
        </ul>
    </ww:if>

    <ul id="applet-params" style="display:none">
        <li id="user-agent"><%= TextUtils.htmlEncode(request.getHeader("User-Agent")) %></li>

        <ww:iterator value="/groupLevels" status="'paramStatus'">
            <li id="comment-group-name-<ww:property value="@paramStatus/index"/>"><ww:text name="." /></li>
        </ww:iterator>

        <ww:iterator value="/roleLevels" status="'paramStatus'">
        <li id="comment-role-<ww:property value="@paramStatus/index"/>"><ww:text name="./name" /></li>
        </ww:iterator>

    </ul>

    <script type="text/javascript" src="<%= request.getContextPath() %>/includes/deployJava.js"></script>
    <script type="text/javascript">
        var version = '1.6';
        var attributes = {
            codebase:"<%= request.getContextPath() %>/secure/",
            code:"com.atlassian.jira.screenshot.applet.ScreenshotApplet.class",
            archive:"applet/screenshot.jar",
            width:710,
            height:540
        };
        var parameters = {
            scriptable:"false",
            post:"AttachScreenshot.jspa?secureToken=<ww:property value="/newUserToken"/>",
            issue:<ww:property value="id" />,
            screenshotname:"<ww:property value="nextScreenshotName"/>",
            after:"<ww:property value="afterUrl" />",
            encoding:"<ww:property value="/applicationProperties/encoding" />",
            useragent: jQuery("#user-agent").text(),
            <ww:iterator value="/groupLevels" status="'paramStatus'">
            'comment.group.name.<ww:property value="@paramStatus/index"/>': jQuery("#comment-group-name-<ww:property value="@paramStatus/index"/>").text().replace(/"/g, '&quot;'),
            </ww:iterator>
            <ww:iterator value="/roleLevels" status="'paramStatus'">
            'comment.role.<ww:property value="@paramStatus/index"/>':"<ww:text name="./id/toString()" />|" + jQuery("#comment-role-<ww:property value="@paramStatus/index"/>").text().replace(/"/g, '&quot;'),
            </ww:iterator>
            'paste.text':"<ww:property value="/encode(/text('attachfile.paste.label'))" />",
            'filename.text':"<ww:property value="/encode(/text('attachfile.filename.label'))" />",
            'errormsg.filename.text':"<ww:property value="/encode(/text('attachfile.applet.filename.error'))" />",
            'comment.text':"<ww:property value="/encode(/text('attachfile.comment.update.label'))" />",
            'attach.text':"<ww:property value="/encode(/text('attachfile.submitname'))" />",
            'cancel.text':"<ww:property value="/encode(/text('common.words.cancel'))" />",
            'badconfiguration.text':"<ww:property value="/encode(/text('attachfile.applet.configuration.error'))" />",
            'comment.level.text':"<ww:property value="/encode(/text('comment.update.viewableby.label'))" />",
            'allusers.text':"<ww:property value="/encode(/text('comment.constants.allusers'))" />",
            'projectroles.text':"<ww:property value="/encode(/text('common.words.project.roles'))" />",
            'groups.text':"<ww:property value="/encode(/text('common.words.groups'))" />",
            'security.text':"<ww:property value="/encode(/text('attachfile.applet.security.problem'))" />"
        };

        deployJava.runApplet(attributes, parameters, version);
    </script>
    <input type="submit" accesskey="<ww:text name="'common.forms.cancel.accesskey'" />" onclick="window.close();" class="hiddenButton" name="randombutton" />
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'admin.errors.issues.current.issue.null'"/></p>
        </aui:param>
    </aui:component>
</ww:else>
</body>
</html>
