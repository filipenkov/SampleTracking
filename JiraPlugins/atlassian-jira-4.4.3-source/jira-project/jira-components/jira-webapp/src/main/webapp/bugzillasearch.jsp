<%@ page import="com.atlassian.core.ofbiz.CoreFactory,
                 com.atlassian.core.util.collection.EasyList,
                 com.atlassian.core.util.map.EasyMap,
                 org.ofbiz.core.entity.GenericEntityException,
                 org.ofbiz.core.entity.GenericValue"%><%!

private GenericValue findMovedIssue(final String origKey) throws GenericEntityException
{
	String key = origKey.toUpperCase();
	java.util.List changeItems = CoreFactory.getGenericDelegator().findByLike("ChangeItem", EasyMap.build("field", "Bugzilla Import Key", "oldstring", key), EasyList.build("group desc"));
	java.util.Iterator iter = changeItems.iterator();
	while (iter.hasNext())
	{

		GenericValue changeItem = (GenericValue) iter.next();
		if (changeItem == null) continue;
		GenericValue changeGroup = changeItem.getRelatedOne("ParentChangeGroup");
		if (changeGroup == null) continue;
		GenericValue issue = changeGroup.getRelatedOne("ParentIssue");
        // Seems it's possible for there to be changeitems for deleted issues; ignore them here
        if (issue == null) continue;
        else return issue;
	}
	return null;
}
%><%
String idParam = request.getParameter("id");
if (idParam == null)
{
	out.println("Please specify an id parameter");
}
else
{
	GenericValue issue = findMovedIssue(idParam);
	if (issue != null)
	{
		response.sendRedirect(request.getContextPath()+"/browse/"+issue.getString("key"));
	}
	else
	{
		String bugzillaUrl = request.getParameter("bugzilla_url") + "/show_bug.cgi?id=";
		response.sendRedirect(bugzillaUrl + idParam);
	}
}
%>
