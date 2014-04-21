<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'navigator.index.errors.title'" /></title>
</head>

<body>
    <table cellpadding=1 cellspacing=0 border=0 align=center width=80% bgcolor=#cc0000><tr><td>
	<table cellpadding=4 cellspacing=0 border=0 width=100% bgcolor=#ffffff><tr><td>
		<font color="#cc0000"><b><ww:text name="'navigator.index.errors.title'"/></b></font>
		<p>
        <ww:if test="systemAdministrator == true">
            <ww:text name="'navigator.index.errors.message.admin'">
                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/jira/IndexAdmin.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </ww:if>
        <ww:else>            
            <ww:text name="'navigator.index.errors.message.user'">
                <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
            </ww:text>
        </ww:else>        
		</p>
	</td></tr></table>
	</td></tr></table>


</body>
</html>
