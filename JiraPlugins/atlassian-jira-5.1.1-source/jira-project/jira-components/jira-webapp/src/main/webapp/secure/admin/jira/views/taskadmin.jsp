<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <ww:if test="hasLiveTasks == true">
        <meta http-equiv="refresh" content="20">
    </ww:if>
    <title>
         <ww:text name="'admin.task.taskadmin.title'"/>
    </title>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="columns">1</page:param>
    <tr>
        <td>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.task.taskadmin.refresh.warning'"><ww:param name="'value0'">20</ww:param></ww:text>
                    </p>
                </aui:param>
            </aui:component>
        </td>
    </tr>
    <tr><td>
        <ww:iterator value="allTasks" status="''">
            <ui:component template="taskdescriptor.jsp" name="'.'">
                <ww:if test="finished == true && userWhoStartedTask == true">
                    <ui:param name="'acknowledgementURL'"><ww:property value="/acknowledgementURL(.)" escape="false" /></ui:param>
                </ww:if>
            </ui:component>
        </ww:iterator>
    </td></tr>
    <page:param name="method">get</page:param>
    <page:param name="action">TaskAdmin.jspa</page:param>
    <page:param name="submitId">refresh_submit</page:param>
    <page:param name="submitName"><ww:text name="'admin.common.words.refresh'"/></page:param>
</page:applyDecorator>
</body>
</html>
