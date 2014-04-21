<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'portal.addpage'"/></title>
    <content tag="section">home_link</content>
</head>
<body class="page-type-issuenav">
<!-- TODO: SEAN discuss removing the left hand panel of these pages so we could dialog them. -->
    <header>
        <h1><ww:text name="'portal.addpage'"/></h1>
    </header>
    <div id="issuenav" class="content-container<ww:if test="/conglomerateCookieValue('jira.toggleblocks.cong.cookie','lhc-state')/contains('#issuenav') == true"> lhc-collapsed</ww:if>">
        <div class="content-related">
            <a class="toggle-lhc" href="#" title="<ww:text name="'jira.issuenav.toggle.lhc'" />"><ww:text name="'jira.issuenav.toggle.lhc'" /></a>
            <jira:formatuser user="/user" type="'fullProfile'" id="'add_portal'"/>
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator name="auiform">
                <page:param name="action">AddPortalPage.jspa</page:param>
                <page:param name="cancelLinkURI">ConfigurePortalPages!default.jspa</page:param>
                <page:param name="submitButtonName">add_submit</page:param>
                <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>

                <aui:component template="formHeading.jsp" theme="'aui'">
                    <aui:param name="'text'"><ww:text name="'portal.addpage'"/></aui:param>
                    <aui:param name="helpURL">portlets.dashboard_pages</aui:param>
                    <aui:param name="helpURLFragment">#creating_dashboards</aui:param>
                </aui:component>

                <%--Used for warnings of dashboard share options--%>
                <div id="share_warning"></div>

                <page:applyDecorator name="auifieldgroup">
                    <aui:textfield label="text('common.words.name')" name="'portalPageName'" theme="'aui'">
                        <aui:param name="'mandatory'">true</aui:param>
                    </aui:textfield>
                </page:applyDecorator>

                <page:applyDecorator name="auifieldgroup">
                    <aui:textarea label="text('common.concepts.description')" name="'portalPageDescription'" rows="3" theme="'aui'" />
                </page:applyDecorator>

                <page:applyDecorator name="auifieldgroup">
                    <aui:select label="text('portal.startfrom')" name="'clonePageId'" list="cloneTargetDashboardPages" listKey="'id'" listValue="'name'" theme="'aui'">
                        <aui:param name="'defaultOptionText'"><ww:text name="'portal.blankpage'" /></aui:param>
                        <aui:param name="'defaultOptionValue'" value="''"/>
                    </aui:select>
                    <page:param name="description"><ww:text name="'portal.blankpage.description'" /></page:param>
                </page:applyDecorator>

                <page:applyDecorator name="auifieldgroup">
                    <ww:component template="formFieldLabel.jsp" label="text('common.favourites.favourite')" theme="'aui'"/>
                    <ww:component name="'favourite'" template="favourite-new.jsp" theme="'aui'">
                        <ww:param name="'enabled'"><ww:property value="./favourite" /></ww:param>
                        <ww:param name="'fieldId'">favourite</ww:param>
                        <ww:param name="'entityType'">PortalPage</ww:param>
                    </ww:component>
                </page:applyDecorator>

                <ww:if test="/showShares == true">
                    <ww:component name="'shares'" label="text('common.sharing.shares')" template="edit-share-types.jsp" theme="'aui'">
                        <ww:param name="'shareTypeList'" value="/shareTypes"/>
                        <ww:param name="'noJavaScriptMessage'">
                           <ww:text name="'common.sharing.no.share.javascript'"/>
                        </ww:param>
                        <ww:param name="'editEnabled'" value="/editEnabled"/>
                        <ww:param name="'dataString'" value="/jsonString"/>
                        <ww:param name="'submitButtonId'">add_submit</ww:param>
                    </ww:component>
                </ww:if>
            </page:applyDecorator>
        </div>
    </div>
</body>
</html>
