<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<decorator:usePage id="decoratorPage"/>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="/includes/decorators/aui-layout/head-common.jsp" %>
    <%@ include file="/includes/decorators/aui-layout/head-resources.jsp" %>
    <decorator:head/>
</head>
<body id="jira" class="aui-layout aui-theme-default <decorator:getProperty property="body.class" />">
<div id="page">
    <section id="content" role="main">
        <div class="content-container">
            <div class="content-body aui-panel">
                <decorator:body />
            </div>
        </div>
    </section>
</div>
</body>
</html>
