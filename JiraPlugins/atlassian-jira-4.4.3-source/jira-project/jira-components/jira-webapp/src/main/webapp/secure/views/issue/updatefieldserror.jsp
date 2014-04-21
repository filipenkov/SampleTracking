<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    <meta content="general" name="decorator" />
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
        <div class="content">
            <ww:bean id="fieldVisibility" name="'com.atlassian.jira.web.bean.FieldVisibilityBean'" />
            <ww:property value="/issueObject">
                <page:applyDecorator name="jirapanel">
                    <page:param name="title">Update fields for issue '<ww:property value="summary" />' failed</page:param>
                    <page:param name="cancelURI"><%=request.getContextPath()%>/secure/Dashboard.jspa</page:param>
                    <page:param name="instructions">
                    <p>Unable to update fields. Return to your form and fix up the problem.</p>
                    </page:param>
                    <ww:if test="/errors && /errors/size() > 0">
                    <div class="warningBox">
                            <span class="errMsg">Validation failed for the fields below:</span>
                            <ul class="square">
                            <ww:iterator value="/errors" status="'status'">
                                <li>For field <span class="errMsg"><ww:property value="/field(./key)/name" /></span>: <ww:property value="./value" /><li>
                            </ww:iterator>
                            </ul>
                    </div>
                    </ww:if>
                </page:applyDecorator>
            </ww:property>
        </div>
    </div>
</div>
</body>
</html>
