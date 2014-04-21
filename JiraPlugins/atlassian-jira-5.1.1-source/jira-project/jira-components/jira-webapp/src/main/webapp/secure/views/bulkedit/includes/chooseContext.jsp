<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<div id="<ww:property value="./value/key" />">
    <ww:property value="./value" >
        <table class="aui aui-table-rowhover">
            <thead>
                <tr>
                    <th width="20%"><ww:text name="'bulk.move.move'"/></th>
                    <th width="5%">&nbsp;</th>
                    <th><ww:text name="'bulk.move.to'"/></th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>
                        <strong><ww:property value="../key/project/string('name')" /></strong>
                    </td>
                    <td>
                        <img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif"  alt="<ww:text name="'bulk.move.targetproject'"/>" />
                    </td>
                    <ww:if test="/allowProjectEdit(.) == true">
                        <ww:property value="/fieldHtml('project', .)" escape="'false'" />
                    </ww:if>
                    <ww:else>
                        <td>
                            <ww:property value="targetProjectGV/string('name')" />
                            <ui:component name="/projectFieldName(.)" value="targetProjectGV/string('id')" template="hidden.jsp" >
                                <ui:param name="'id'"><ww:property value="./key" />project</ui:param>
                                <ui:param name="'cssClass'">project-field-readonly</ui:param>
                            </ui:component>
                        </td>
                    </ww:else>
                </tr>
                <tr>
                    <td>
                        <ww:component name="'issuetype'" template="constanticon.jsp">
                          <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                          <ww:param name="'iconurl'" value="../key/issueTypeObject/iconUrl" />
                          <ww:param name="'alt'"><ww:property value="../key/issueTypeObject/name" /></ww:param>
                        </ww:component>
                        <strong><ww:property value="../key/issueTypeObject/nameTranslation" /></strong>
                    </td>
                    <td>
                        <img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif"  alt="<ww:text name="'bulk.move.targetissuetype'"/>" />
                    </td>
                    <ww:property value="fieldHtml('issuetype', .)" escape="'false'" />
                </tr>
            </tbody>
        </table>
    </ww:property>
    <p class="bulk-affects">
        <ww:text name="'bulk.migrate.confirm.subheading'">
            <ww:param name="'value0'"><strong><ww:property value="./value/selectedIssues/size()" /></strong></ww:param>
            <ww:param name="'value1'"><strong><ww:property value="./key/issueTypeObject/nameTranslation" /></strong></ww:param>
            <ww:param name="'value2'"><strong><ww:property value="./key/project/string('name')" /></strong></ww:param>
        </ww:text>
    </p>
    <ww:if test="./value/subTaskCollection == false && !@showSameAsBulkEditBean">
        <ww:property value="true" id="showSameAsBulkEditBean"/>
        <div class="use-for-all">
            <input type="checkbox" name="sameAsBulkEditBean" id="sameAsBulkEditBean" value="<ww:property value="./value/key" />" onclick="toggle('<ww:property value="./value/key" />')" />
            <label for="sameAsBulkEditBean">
                <ww:text name="'bulk.migrate.choosecontext.same.all'" />
            </label>
        </div>
    </ww:if>
    <a href="#top" class="backToTop"><ww:text name="'common.concepts.back.to.top'"/></a>
</div>