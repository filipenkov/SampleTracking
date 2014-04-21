<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<%-- The page is used for the manageable option object --%>
<ww:property value="/manageableOption" >
<html>
<head>
	<title><ww:text name="'admin.manage.title'">
	    <ww:param name="'value0'"><ww:property value="title" /></ww:param>
	</ww:text></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="issue_types"/>
</head>
<body>

<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.manage.title'"><ww:param name="'value0'"><ww:property value="title" /></ww:param></ww:text></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">manageIssueTypes</page:param>
    <div class="tabwrap tabs2">
        <ul class="tabs horizontal">
            <li <ww:if test="/actionType == 'view'">class="active"</ww:if>>
                <a href="ViewIssueTypes.jspa">
                    <strong><ww:text name="'admin.issuesettings.issuetypes.global.issue.types'"><ww:param name="'value0'"><ww:property value="title" /></ww:param></ww:text></strong>
                </a>
            </li>
            <li <ww:if test="/actionType == 'scheme'">class="active"</ww:if>>
                <a href="ManageIssueTypeSchemes!default.jspa" >
                    <strong><ww:text name="'admin.issuesettings.issuetypes.issue.type.scheme'"><ww:param name="'value0'"><ww:property value="title" /></ww:param></ww:text></strong>
                </a>
            </li>
        <ww:if test="/translatable == true">
            <li <ww:if test="/actionType == 'translate'">class="active"</ww:if>>
                <a href="ViewTranslations!default.jspa?issueConstantType=<ww:property value="fieldId" />" id="translate_link"  >
                    <strong><ww:text name="'admin.issuesettings.issuetypes.translate'"/></strong>
                </a>
            </li>
        </ww:if>
        </ul>
    </div>
    <ww:if test="/actionType == 'scheme'">
        <p><ww:text name="'admin.issuesettings.issuetypes.issue.type.schemes.determines'"/></p>
    </ww:if>
    <ww:if test="/actionType == 'view'">
        <p><ww:text name="'admin.issuesettings.issuetypes.alphabetical.order'">
            <ww:param name="'value0'"><strong></ww:param>
            <ww:param name="'value1'"></strong></ww:param>
        </ww:text></p>
    </ww:if>
</page:applyDecorator>


    <ww:if test="/actionType == 'scheme'">
        <jsp:include page="/secure/admin/views/issuetypes/issuetypeschemes.jsp" />
    </ww:if>
    <ww:elseIf test="/actionType == 'translate'">
        <jsp:include page="/secure/admin/views/translations/viewtranslations.jsp" />
    </ww:elseIf>
    <ww:else>
        <jsp:include page="/secure/admin/views/issuetypes/issuetypes.jsp" />
    </ww:else>
</body>
</html>
</ww:property>
