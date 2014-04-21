<%@ page import="java.util.*"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<html>
<head>
    <meta content="issuesummary" name="decorator" />
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
        <h2><ww:text name="'trackback.manage.title'"/></h2>
        <div class="content">
            <div class="module" id="issue-attachments-table">
                <div class="mod-content">
                    <p><ww:text name="'trackback.manage.desc'"/></p>
                    <ww:if test="applicationProperties/option('jira.option.trackback.receive') == true && trackbacks/size > 0">
                        <jsp:include page="managetrackbackstable.jsp" />
                    </ww:if>
                    <ww:else>
                        <aui:component template="auimessage.jsp" theme="'aui'">
                            <aui:param name="'messageType'">warning</aui:param>
                            <aui:param name="'messageHtml'">
                                <p><ww:text name="'trackback.no.links'"/></p>
                            </aui:param>
                        </aui:component>
                    </ww:else>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
