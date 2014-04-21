<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="ui" uri="webwork" %>
<%@ taglib prefix="decorator" uri="sitemesh-decorator" %>
<%
    // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "atl.error"
    final WebResourceManager wrm = ComponentManager.getInstance().getWebResourceManager();
    wrm.requireResourcesForContext("atl.general");
    wrm.requireResourcesForContext("jira.general");
%>
<%@ include file="/includes/decorators/header.jsp" %>
<decorator:body />
<%@ include file="/includes/decorators/footer.jsp" %>