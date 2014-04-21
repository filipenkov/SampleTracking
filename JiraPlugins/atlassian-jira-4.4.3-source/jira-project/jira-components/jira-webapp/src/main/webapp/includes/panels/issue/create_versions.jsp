<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<ww:if test="id == 'versions'">
        <ww:property value="/field('versions')/createHtml(.., /)" escape="'false'" />
<%--    <ww:if test="/possibleVersions(project)/size > 0">--%>
<%--        <ui:select label="text(./nameKey)" name="id" template="selectmultiple.jsp"--%>
<%--            list="/possibleVersionsReleasedFirst(/project)" listKey="'key'" listValue="'value'">--%>
<%--            <ui:param name="'size'" value="'5'" />--%>
<%--            <ui:param name="'headerrow'" value="text('common.words.unknown')" />--%>
<%--            <ui:param name="'headervalue'" value="'-1'" />--%>
<%--            <ui:param name="'description'"><ww:property value="fieldDescription" escape="false" /></ui:param>--%>
<%--            <ui:param name="'mandatory'" value="../required"/>--%>
<%--        </ui:select>--%>
<%--    </ww:if>--%>
<%--    <ww:else>--%>
<%--        <tr>--%>
<%--            <td bgcolor=#fffff0 align=right><ww:text name="'issue.field.affectsversions'"/>:</td>--%>
<%--            <td bgcolor=#ffffff valign=top>--%>
<%--            <ww:text name="'common.words.unknown'"/>--%>
<%--            </td>--%>
<%--        </tr>--%>
<%--    </ww:else>--%>
</ww:if>

<ww:if test="id == 'fixVersions'">
    <ww:property value="/field('fixVersions')/createHtml(.., /)" escape="'false'" />

<%--    <ww:if test="id == 'fixVersions' && /hasProjectPermission('resolve', project) == true">--%>
<%--        <ww:if test = "possibleVersions(project)/size > 0">--%>
<%--            <ui:select label="text(./nameKey)" name="'fixVersions'" template="selectmultiple.jsp"--%>
<%--                list="possibleVersions(project)" listKey="'key'" listValue="'value'">--%>
<%--                <ui:param name="'size'" value="'5'" />--%>
<%--                <ui:param name="'headerrow'" value="text('common.words.unknown')" />--%>
<%--                <ui:param name="'headervalue'" value="'-1'" />--%>
<%--                <ui:param name="'description'"><ww:property value="fieldDescription" escape="false" /></ui:param>--%>
<%--                <ui:param name="'mandatory'" value="../required"/>--%>
<%--            </ui:select>--%>
<%--        </ww:if>--%>
<%--        <ww:else>--%>
<%--            <tr>--%>
<%--                <td bgcolor=#fffff0 align=right><ww:text name="'issue.field.fixversions'"/>:</td>--%>
<%--                <td bgcolor=#ffffff valign=top>--%>
<%--                <ww:text name="'common.words.unknown'"/>--%>
<%--                </td>--%>
<%--            </tr>--%>
<%--        </ww:else>--%>
<%--    </ww:if>--%>
</ww:if>
