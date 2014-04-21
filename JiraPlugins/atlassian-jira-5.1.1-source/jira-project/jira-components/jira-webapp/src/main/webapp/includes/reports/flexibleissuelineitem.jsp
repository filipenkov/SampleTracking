<%-- This is called by issuelineitem.jsp & also by searchresults.jsp --%>
<%-- issuelineitem.jsp just adds <tr> </tr> to the end of the lines --%>

    <td width=5%><%@ include file="/includes/icons/type.jsp" %></td>
    <td width=5% nowrap><a href="<%= request.getContextPath() %>/browse/<ww:property value="string('key')"/>"><ww:property value="string('key')" /></a></td>
    <td width=5% nowrap>
        <font size=1>
        <ww:if test="./string('resolution') == null">
            <em><ww:text name="'common.status.unresolved'" /></em>
        </ww:if>
        <ww:else>
            <ww:property value="/upperCase(/constantsManager/resolution(string('resolution'))/string('name'))" />
        </ww:else>
        </font>
    </td>
    <td width=80%>
        <ww:if test="@issueBean/subTask(.) == true">
            <span class="smallgrey"><a href="<%=request.getContextPath()%>/browse/<ww:property value="@issueBean/parentIssueKey(.)"/>" style="text-decoration: none; "><ww:property value="@issueBean/parentIssueKey(.)"/></a></span><br>
            <img src="<%= request.getContextPath() %>/images/icons/link_out_bot.gif" width=16 height=16 border=0 align=absmiddle>
        </ww:if>
        <a href="<%= request.getContextPath() %>/browse/<ww:property value="string('key')"/>"><ww:property value="string('summary')" /></a>
    </td>
    <td nowrap width=1%>
	<%@ include file="/includes/icons/priority.jsp" %>
    </td>
    <td nowrap width=1%>
	<%@ include file="/includes/icons/status.jsp" %>
    </td>
