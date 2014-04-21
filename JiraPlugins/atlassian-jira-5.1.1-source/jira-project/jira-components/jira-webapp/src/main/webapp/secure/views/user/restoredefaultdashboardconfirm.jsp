<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
	<title><ww:text name="'restoredefaultdashboard.title'"/></title>
    <content tag="section">home_link</content>
</head>
<body class="page-type-issuenav">
<!-- TODO: SEAN discuss removing the left hand panel of these pages so we could dialog them. -->
    <header>
        <h1><ww:text name="'restoredefaultdashboard.title'"/></h1>
    </header>
    <div id="issuenav" class="content-container<ww:if test="/conglomerateCookieValue('jira.toggleblocks.cong.cookie','lhc-state')/contains('#issuenav') == true"> lhc-collapsed</ww:if>">
        <div class="content-related">
            <a class="toggle-lhc" href="#" title="<ww:text name="'jira.issuenav.toggle.lhc'" />"><ww:text name="'jira.issuenav.toggle.lhc'" /></a>
            <jira:formatuser user="/user" type="'fullProfile'" id="'restore_default_dash'"/>
        </div>
        <div class="content-body aui-panel">
            <page:applyDecorator name="jiraform">
                <page:param name="title"><ww:text name="'restoredefaultdashboard.title'"/></page:param>
                <page:param name="description"><ww:text name="'restoredefaultdashboard.desc'"/></page:param>
                <page:param name="width">100%</page:param>
                <page:param name="action">RestoreDefaultDashboard.jspa</page:param>
                <page:param name="submitId">restore_submit</page:param>
                <page:param name="submitName"><ww:text name="'restoredefaultdashboard.submit.restore'"/></page:param>
                <page:param name="cancelURI"><ww:property value="/cancelUrl"/></page:param>
                <page:param name="autoSelectFirst">false</page:param>
                <input type="hidden" name="confirm" value="true" />
                <ww:if test="/portalPagesFavouritedByOthersWithUserCount/empty == false">
                    <ui:component label="text('restoredefaultdashboard.others.favourites')" template="textlabel.jsp">
                        <ww:param name="'texthtml'"><span id="othersFavouritedPortalPages">
                            <ww:iterator value="/portalPagesFavouritedByOthersWithUserCount" status="'status'">
                                <ww:property escape="true" value="key/name"/>
                                <ww:property value="value">
                                    <span class="small">(<ww:property value="."/> <ww:if test=". == 1"><ww:text name="'common.words.user'"/>)</ww:if><ww:else><ww:text name="'admin.common.words.users'"/>)</ww:else></span><ww:if test="@status/last == false">,</ww:if>
                                </ww:property>
                            </ww:iterator>
                        </span></ww:param>
                    </ui:component>
                </ww:if>
            </page:applyDecorator>
        </div>
    </div>
</body>
</html>
