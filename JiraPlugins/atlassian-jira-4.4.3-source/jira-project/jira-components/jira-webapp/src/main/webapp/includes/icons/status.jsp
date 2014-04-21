<ww:property value="./statusObject">
<ww:if test=". != null"> <%-- handle both objects and genericvalues --%>
<ww:component name="'status'" template="constanticon.jsp">
  <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
  <ww:param name="'iconurl'" value="iconUrl" />                             -
  <ww:param name="'alt'"><ww:property value="nameTranslation" /></ww:param>
  <ww:param name="'title'"><ww:property value="nameTranslation"/> - <ww:property value="descTranslation"/></ww:param>
</ww:component>
</ww:if>
</ww:property>
<ww:else>
<ww:property value="/constantsManager/status(string('status'))">
<ww:component name="'status'" template="constanticon.jsp">
  <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
  <ww:param name="'iconurl'" value="./string('iconurl')" />
  <ww:param name="'alt'"><ww:property value="/nameTranslation(.)" /></ww:param>
  <ww:param name="'title'"><ww:property value="/nameTranslation(.)"/> - <ww:property value="/descTranslation(.)"/></ww:param>
</ww:component>
</ww:property>
</ww:else>
