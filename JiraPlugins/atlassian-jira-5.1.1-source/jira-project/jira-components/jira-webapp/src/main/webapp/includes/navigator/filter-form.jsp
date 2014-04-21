<%@ page import="com.atlassian.util.profiling.UtilTimerStack"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:bean id="fieldVisibility" name="'com.atlassian.jira.web.bean.FieldVisibilityBean'" />
<jsp:include page="/includes/navigator/filter-form/tabs.jsp" />
<jsp:include page="/includes/navigator/filter-form/navigator_type_links.jsp" />
<jsp:include page="/includes/navigator/filter-form/filter_description.jsp" />

<page:applyDecorator id="issue-filter" name="auiform">
    <page:param name="action">IssueNavigator.jspa?</page:param>
    <page:param name="method">post</page:param>
    <page:param name="cssClass">top-label</page:param>
    <page:param name="useCustomButtons">true</page:param>

    <aui:component id="'refreshFilter'" name="'refreshFilter'" template="hidden.jsp" theme="'aui'" value="'false'" />
    <aui:component id="'reset'" name="'reset'" template="hidden.jsp" theme="'aui'" value="'update'" />
    <aui:component id="'usercreated'" name="'usercreated'" template="hidden.jsp" theme="'aui'" value="'true'" />

    <ww:if test="browsableProjects != null && browsableProjects/size > 0">
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">buttons-container</page:param>
            <page:applyDecorator name="auifieldgroup">
                <page:param name="type">buttons</page:param>
                <aui:component theme="'aui'" template="formSubmit.jsp">
                    <aui:param name="'submitButtonName'">show</aui:param>
                    <aui:param name="'submitButtonText'"><ww:text name="'common.concepts.search'"/></aui:param>
                </aui:component>
            </page:applyDecorator>
        </page:applyDecorator>

        <ww:iterator value="/searcherGroups" status="'status'">
            <%
                UtilTimerStack.push("filter-form.jsp - searchergroup");
            %>
            <ww:if test="/shown(.) == true">
                <page:applyDecorator name="auifieldset">
                    <ww:if test="printHeader == true">
                        <page:param name="id"><ww:property value="/convertToId(./titleKey)" />-group</page:param>
                        <page:param name="cssClass">toggle-wrap collapsed</page:param>
                        <page:param name="legendCssClass">toggle-trigger</page:param>
                        <page:param name="legendSpanCssClass">toggle-title</page:param>
                        <page:param name="legend"><ww:text name="titleKey"/></page:param>
                    </ww:if>
                    <page:applyDecorator name="auifieldgroup"> <!-- opening .field-group1 #<ww:property value="titleKey" /> -->
                        <page:param name="type">toggle-target</page:param>
                        <page:param name="id"><ww:property value="/convertToId(./titleKey)" /></page:param>
                        <ww:iterator value="./searchers" status="'status'">
                            <ww:property value="./name" id="searcherName" />
                            <%
                                final String searcherName = UtilTimerStack.isActive() ? "filter-form.jsp - searcher - " + request.getAttribute("searcherName") : "";
                                UtilTimerStack.push(searcherName);
                            %>
                            <ww:property value="/searcherEditHtml(.)" escape="false" />
                            <%
                                UtilTimerStack.pop(searcherName);
                            %>
                        </ww:iterator>
                    </page:applyDecorator><!-- closing .field-group1 -->  
                </page:applyDecorator>
            </ww:if>
            <%
                UtilTimerStack.pop("filter-form.jsp - searchergroup");
            %>
        </ww:iterator>
       
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">buttons-container</page:param>
            <page:param name="cssClass">buttons-bottom</page:param>
            <page:applyDecorator name="auifieldgroup">
                <page:param name="type">buttons</page:param>
                <aui:component theme="'aui'" template="formSubmit.jsp">
                    <aui:param name="'id'">issue-filter-submit-base</aui:param>
                    <aui:param name="'submitButtonName'">show</aui:param>
                    <aui:param name="'submitButtonText'"><ww:text name="'common.concepts.search'"/></aui:param>
                    <aui:param name="'submitButtonHideAccessKey'">true</aui:param>
                </aui:component>
            </page:applyDecorator>
        </page:applyDecorator>
    </ww:if>
</page:applyDecorator>
