<%@ taglib prefix="ww" uri="webwork" %>
<div class="hidden">
    <ww:iterator value="/issueOperations">
        <a id="<ww:property value="./id"/>" class="<ww:property value="./styleClass"/>" href="<ww:property value="./url"/>&returnUrl=/secure/IssueNavigator.jspa"></a>
    </ww:iterator>
</div>