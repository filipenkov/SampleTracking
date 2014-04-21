<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>

<ww:if test="id == 'components'">
    <ww:if test="/projectManager/components(project)/size > 0">
        <ui:select label="text(./nameKey)" name="id" template="selectmultiple.jsp"
            list="projectManager/components(project)" listKey="'long('id')'" listValue="'string('name')'">
            <ui:param name="'size'" value="'5'" />
            <ui:param name="'headerrow'" value="text('common.words.unknown')" />
            <ui:param name="'headervalue'" value="'-1'" />
            <ui:param name="'description'"><ww:property value="fieldDescription" escape="false" /></ui:param>
            <ui:param name="'mandatory'" value="../required"/>
        </ui:select>
    </ww:if>
    <ww:else>
        <tr>
            <td class="fieldLabelArea"><ww:text name="'issue.field.components'"/>:</td>
            <td class="fieldValueArea">
            <ww:text name="'common.words.unknown'"/>
            </td>
        </tr>
    </ww:else>
</ww:if>
