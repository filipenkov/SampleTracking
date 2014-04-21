<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'movesubtask.title'"/>: <ww:property value="issue/string('key')" /></title>
</head>

<body class="nl">
<div id="stepped-process">
    <div class="steps-wrap">
        <div class="steps-container">
            <jsp:include page="/secure/views/issue/movetaskpane.jsp" flush="false" />
        </div>
        <div class="current-step">

        <ww:if test="subTaskTypes/size > 1">
            <page:applyDecorator name="jiraform">
                <page:param name="title"><ww:text name="'movesubtask.title'"/>: <ww:property value="issue/string('key')" /></page:param>
                <page:param name="description">
                    <ww:text name="'movesubtask.step1.desc'"/>
                </page:param>
                <page:param name="width">100%</page:param>
                <page:param name="action">MoveSubTaskType.jspa</page:param>
                <page:param name="autoSelectFirst">false</page:param>
                <page:param name="cancelURI"><ww:url value="/issuePath" atltoken="false"/></page:param>
                <page:param name="submitId">next_submit</page:param>
                <page:param name="submitName"><ww:text name="'common.forms.next'"/> &gt;&gt;</page:param>

                <ww:property value="/fieldHtml('issuetype')" escape="'false'" />

                <ui:component name="'id'" template="hidden.jsp" />
            </page:applyDecorator>
        </ww:if>
<%-- This will only be displayed if there are no other possible sub-task issue types available for this project. This
 should not happen because in the previous screen we check for that and disable the option. But still people could hand
 craft the URL or an Administrator may remove an issue type from the scheme while someone is changing the sub-tasks
 issue type. --%>
        <ww:else>
            <page:applyDecorator name="jiraform">
                <page:param name="title"><ww:text name="'movesubtask.title'"/></page:param>
                <page:param name="description"><span class="warning"><ww:text name="'common.words.note'"/></span>: <ww:text name="'movesubtask.nosubtasktypes'"/></page:param>
                <page:param name="cancelURI"><ww:url value="/issuePath" /></page:param>
            </page:applyDecorator>
        </ww:else>

        </div>
    </div>
</div>
</body>
</html>
