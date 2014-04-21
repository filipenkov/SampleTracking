<%@ taglib uri="webwork" prefix="ww" %>
<ww:property value="/attachmentManager/attachments(@issueGv)">
    <ww:if test=". != null && size > 0">
        <tr>
            <td bgcolor="#f0f0f0" width="20%" valign="top" style="font-weight:bold;">
                <ww:text name="'common.concepts.attachments.files'"/>:
            </td>
            <td bgcolor="#ffffff" valign="top">
                <%-- Iterate over the attachments of the issue stored in . --%>
                <ww:iterator value="." status="'attachmentstatus'">
                    <ww:fragment template="attachment-icon.jsp">
                        <ww:param name="'filename'" value="filename"/>
                        <ww:param name="'mimetype'" value="mimetype"/>
                    </ww:fragment>
                    <ww:property value="string('filename')"/> &nbsp;&nbsp;&nbsp;
                </ww:iterator>
            </td>
        </tr>
    </ww:if>
</ww:property>
