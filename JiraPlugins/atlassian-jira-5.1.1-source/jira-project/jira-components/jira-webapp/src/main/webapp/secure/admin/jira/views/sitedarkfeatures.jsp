<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section"/>
    <meta name="admin.active.tab" content="dark_features"/>

    <title><ww:text name="'admin.darkfeatures.manage.heading'"/></title>
</head>
<body>

<div id="system-dark-features">
    <h4><ww:text name="'admin.darkfeatures.system.property'"/></h4>
    <ww:if test="/systemEnabledFeatures/size > 0">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.darkfeatures.system.property.warning'"/></p>
            </aui:param>
        </aui:component>

        <ul>
            <ww:iterator value="/systemEnabledFeatures" status="'status'">
                <li>
                    <ww:property value="." />
                </li>
            </ww:iterator>
        </ul>
    </ww:if>
    <ww:else>
        <li>
            <ww:text name="'admin.darkfeatures.no.system'"/>
        </li>
    </ww:else>
</div>
<p />
<div id="site-dark-features">
    <h4><ww:text name="'admin.darkfeatures.site.property'"/></h4>
    <ww:if test="/siteEnabledFeatures/size > 0">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.darkfeatures.site.property.warning'"/></p>
            </aui:param>
        </aui:component>
        <ul id="site-enabled-features">
            <ww:iterator value="/siteEnabledFeatures" status="'status'">
                <li>
                    <ww:property value="." /> (<a href="<ww:url page="SiteDarkFeatures!remove.jspa"><ww:param name="'featureKey'" value="." /></ww:url>"><ww:text name="'admin.common.words.disable'"/></a>)
                </li>
            </ww:iterator>
        </ul>
    </ww:if>
    <p />
    <ww:if test="/enabled('jira.site.darkfeature.admin') == false || /enabled('jira.user.darkfeature.admin') == false">
        <strong><ww:text name="'admin.darkfeatures.site.admin.add'"/></strong>
        <ul id="site-disabled-features">
            <ww:if test="/enabled('jira.site.darkfeature.admin') == false">
                <li>
                    <ww:property value="'jira.site.darkfeature.admin'" /> (<a href="<ww:url page="SiteDarkFeatures.jspa"><ww:param name="'featureKey'" value="'jira.site.darkfeature.admin'" /></ww:url>"><ww:text name="'admin.common.words.enable'"/></a>)
                </li>
            </ww:if>
            <ww:if test="/enabled('jira.user.darkfeature.admin') == false">
                <li>
                    <ww:property value="'jira.user.darkfeature.admin'" /> (<a href="<ww:url page="SiteDarkFeatures.jspa"><ww:param name="'featureKey'" value="'jira.user.darkfeature.admin'" /></ww:url>"><ww:text name="'admin.common.words.enable'"/></a>)
                </li>
            </ww:if>
        </ul>
    </ww:if>
</div>

<page:applyDecorator id="dark-features" name="auiform">
    <page:param name="action">SiteDarkFeatures.jspa</page:param>
    <page:param name="method">post</page:param>
    <page:param name="submitId">enable-dark-feature</page:param>
    <page:param name="submitButtonText"><ww:text name="'admin.common.words.submit'"/></page:param>

    <div class="aui-group">
        <div class="aui-item">
            <page:applyDecorator name="auifieldgroup">
                <aui:textfield label="text('admin.darkfeatures.enable')" maxlength="255" id="'featureKey'" name="'featureKey'" theme="'aui'">
                </aui:textfield>
            </page:applyDecorator>
        </div>
    </div>
</page:applyDecorator>

</body>
</html>