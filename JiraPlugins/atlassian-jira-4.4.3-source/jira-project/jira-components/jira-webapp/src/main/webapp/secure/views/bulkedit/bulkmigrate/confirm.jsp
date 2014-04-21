<div class="jiraform" id="<ww:property value="./value/key" />">
<page:applyDecorator name="jirapanel">
    <page:param name="title">
        <strong><ww:property value="./key/project/string('name')" /></strong> -
        <ww:component name="'issuetype'" template="constanticon.jsp">
          <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
          <ww:param name="'iconurl'" value="./key/issueType/string('iconurl')" />
          <ww:param name="'alt'"><ww:property value="./key/issueType/string('name')" /></ww:param>
        </ww:component>
        <strong><ww:property value="./key/issueType/string('name')" /></strong>
        <a name="<ww:property value="./key/project/string('id')" /><ww:property value="./key/issueTypeObject/id" />" />
    </page:param>
    <page:param name="instructions">
        <ww:text name="'bulk.migrate.confirm.subheading'">
            <ww:param name="'value0'">
                <strong><ww:property value="./value/selectedIssues/size()" /></strong>
            </ww:param>
            <ww:param name="'value1'">
                 <ww:iterator value="./value/issueTypeObjects" status="'status'">
                     <ww:component name="'issuetype'" template="constanticon.jsp">
                       <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                       <ww:param name="'iconurl'" value="./iconUrl" />
                       <ww:param name="'alt'"><ww:property value="./name" /></ww:param>
                     </ww:component> <strong><ww:property value="./name" /></strong><ww:if test="@status/last == false">, </ww:if>
                 </ww:iterator>
             </ww:param>
             <ww:param name="'value2'">
                 <ww:iterator value="./value/projects" status="'status'">
                     <strong><ww:property value="./string('name')" /></strong><ww:if test="@status/last == false">, </ww:if>
                 </ww:iterator>
             </ww:param>
        </ww:text>
    </page:param>

    <ww:property value="./value">
        <%@include file="/secure/views/bulkedit/confirmationdetails.jsp"%>
    </ww:property>

    <a href="#top" class="backToTop"><ww:text name="'common.concepts.back.to.top'"/></a>
</page:applyDecorator>
</div>
