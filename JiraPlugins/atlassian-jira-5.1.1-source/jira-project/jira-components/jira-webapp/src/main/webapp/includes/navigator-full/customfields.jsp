<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<ww:iterator value="/validCustomFields(.)">
    <ww:if test="./value(@issue) != null">
        <tr>
            <td bgcolor="#f0f0f0" width="20%" valign="top"><b><ww:property value="./name" />:</b></td>
            <td bgcolor="#ffffff" width="80%">
                <ww:property value="/customFieldHtml(., @issueGv)" escape="false" />
            </td>
        </tr>
    </ww:if>
</ww:iterator>
