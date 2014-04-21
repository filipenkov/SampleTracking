<% if (p.isPropertySet("localHelpAction")) {
    String action = p.getProperty("localHelpAction");
 %>
 <a class="help-lnk" href=<%=action%>> <img src="<%= request.getContextPath() %>/images/icons/ico_help.png"
    width=16 height=16 align=right border=0 alt="Test"
    title="Get local help"></a>
<%
}
if (p.isPropertySet("helpURL")) {
    String helpUrl = "'" + p.getProperty("helpURL") + "'";
    String helpURLFragment = "";
    if (p.isPropertySet("helpURLFragment"))
        helpURLFragment = p.getProperty("helpURLFragment"); %>
    <ww:component template="help.jsp" name="<%= helpUrl %>" >
        <ww:param name="'helpURLFragment'"><%= helpURLFragment %></ww:param>
    </ww:component>
<% } %>
