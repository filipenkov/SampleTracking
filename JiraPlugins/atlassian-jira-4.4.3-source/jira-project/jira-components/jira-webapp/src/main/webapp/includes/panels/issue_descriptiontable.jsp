<%--
This JSP include prints a table (like the middle of view issue) with the issue description.
It requires that the issue is the top item on the value stack, and
that various beans / actions are already set. Use it like so:

<%@ taglib uri="jiratags" prefix="jira" %>
<ww:property value="path/to/issueGenericValue">
    <%@ include this file %>
</ww:property>
--%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<ww:property value="./long('project')">
<ww:if test="@fieldVisibility/fieldHidden(., 'description', ../string('type')) == false && ../string('description') != null && ../string('description')/length > 0">
<div id="descriptionmodule" class="module toggle-wrap">
    <div class="mod-header">
        <h3 class="toggle-title"><ww:text name="'common.concepts.description'" /></h3>
    </div>
    <div id="issue-description" class="mod-content">
        <ww:property value="/renderedContent('description', ../string('description'), @issue)" escape="'false'" />
    </div>
</div>
</ww:if>
</ww:property>

