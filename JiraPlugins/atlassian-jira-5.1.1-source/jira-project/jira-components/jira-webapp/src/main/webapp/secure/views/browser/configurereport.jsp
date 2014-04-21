<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.common.words.configure'"/> - <ww:property value="report/name" /></title>
</head>
<body>
    <header>
        <h1><ww:text name="'admin.common.words.configure'"/> - <ww:property value="report/name" /></h1>
    </header>
    <div class="content-container">
        <div class="content-body aui-panel">
            <page:applyDecorator name="jiraform" >
                <page:param name="method">GET</page:param>
                <page:param name="action">ConfigureReport.jspa</page:param>
                <page:param name="suppressAtlToken">true</page:param>
                <page:param name="cancelURI"><%= request.getContextPath() %>/secure/BrowseProject.jspa</page:param>
                <page:param name="width">100%</page:param>
                <page:param name="title"><ww:text name="'common.concepts.report'"/>: <ww:property value="report/label" /></page:param>
                <page:param name="description">
                    <ww:if test="report/description"><b><ww:text name="'common.concepts.description'"/>:</b><br/><ww:property value="report/description" escape="false" /></ww:if>
                </page:param>

                <ww:iterator value="objectConfiguration/fieldKeys" >
                <%@ include file="/includes/panels/objectconfiguration_form.jsp"  %>
                </ww:iterator>

                <ui:component name="'selectedProjectId'" value="selectedProjectId" template="hidden.jsp" />
                <ui:component name="'reportKey'" value="reportKey" template="hidden.jsp" />
                    <% if (! Boolean.FALSE.equals(request.getAttribute("jira.portletform.showsavebutton")))
                       { // if there is a problem with one of the parameters not being satisfied above, then we should not show the save button
                         // Fixes JRA-5279
                    %>
                    <page:param name="submitId">next_submit</page:param>
                    <page:param name="submitName"><ww:text name="'common.forms.next'"/></page:param>
                    <% }
                       else
                       { // reset the submit button to empty since in tomcat the state seems to be cached and we run into JRA-5042
                    %>
                    <page:param name="submitName"><ww:text name="''"/></page:param>
                    <%
                       }
                    %>

            </page:applyDecorator>
        </div>
    </div>
</body>
</html>
