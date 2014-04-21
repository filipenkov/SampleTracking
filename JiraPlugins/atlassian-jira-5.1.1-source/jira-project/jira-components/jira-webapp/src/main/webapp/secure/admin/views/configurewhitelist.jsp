<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>
<html>
<head>
	<title><ww:text name="'whitelist.admin.action.title'"/></title>
    <jira:web-resource-require modules="jira.webresources:configure-whitelist" />
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/security_section"/>
    <meta name="admin.active.tab" content="whitelist"/>
</head>
<body>
<page:applyDecorator id="configure-whitelist" name="auiform">
    <page:param name="action">ConfigureWhitelist.jspa</page:param>
    <page:param name="submitButtonName">Save</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.words.save'"/></page:param>

    <ww:if test="/whitelistSaved == true">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">success</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'whitelist.admin.action.saved'"/></p>
            </aui:param>
        </aui:component>
    </ww:if>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'whitelist.admin.action.title'"/></aui:param>
    </aui:component>

    <ww:if test="/showUpgrade == true">
        <fieldset class="hidden parameters">
            <input type="hidden" title="confirmGadgetDelete" value="<ww:text name="'gadget.applinks.delete.confirm'"/>"/>
            <input type="hidden" title="errorDeletingGadget" value="<ww:text name="'gadget.applinks.delete.error'"/>"/>
        </fieldset>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'titleText'"><ww:text name="'whitelist.admin.action.upgrade.notice'"/></aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'whitelist.admin.action.upgrade.notice.line1'"/></p>
                <p><ww:text name="'whitelist.admin.action.upgrade.notice.line2'">
                    <ww:param name="'value0'"><strong></ww:param>
                    <ww:param name="'value1'"></strong></ww:param>
                </ww:text></p>
                <p><ww:text name="'whitelist.admin.action.upgrade.notice.line3'"/></p>
                <ww:if test="./groupedExternalGadgets/entrySet > 0">
                    <ul class="security-upgrades">
                    <ww:iterator value="/groupedExternalGadgets/entrySet">
                        <li><a href="<ww:property value="./key"/>"><ww:property value="./key"/></a>
                            <ww:if test="./value/size > 0">
                                <ul>
                                    <ww:iterator value="./value">
                                        <li id="external-spec-<ww:property value="./id"/>">
                                            <ww:property value="./specUri"/>
                                        </li>
                                    </ww:iterator>
                                </ul>
                            </ww:if>
                        </li>
                    </ww:iterator>
                    </ul>
                </ww:if>
            </aui:param>
        </aui:component>
    </ww:if>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'whitelist.admin.action.description.line1'"/></p>
            <p><ww:text name="'whitelist.admin.action.description.line2'"/></p>
            <p>
                <ww:text name="'whitelist.admin.action.description.line3'">
                    <ww:param name="'value0'"><a href="<ww:url page="/plugins/servlet/applinks/listApplicationLinks" atltoken="'false'"/>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldset">
        <page:param name="type">group</page:param>
        <div class="radio">
            <ww:if test="/disableWhitelist == true">
                <aui:radio id="'allow'" label="/text('whitelist.admin.action.radio.allow')" list="null" name="'disableWhitelist'" value="'true'" checked="true" theme="'aui'" />
                <aui:radio id="'restrict'" label="/text('whitelist.admin.action.radio.restrict')" list="null" name="'disableWhitelist'" value="'false'" theme="'aui'" />
            </ww:if>
            <ww:else>
                <aui:radio id="'allow'" label="/text('whitelist.admin.action.radio.allow')" list="null" name="'disableWhitelist'" value="'true'" theme="'aui'" />
                <aui:radio id="'restrict'" label="/text('whitelist.admin.action.radio.restrict')" list="null" name="'disableWhitelist'" value="'false'" checked="true" theme="'aui'" />
            </ww:else>
        </div>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <page:param name="cssClass">rules-container</page:param>
        <page:param name="description">
                <p><ww:text name="'whitelist.admin.action.rules.description'"/></p>
                <ul>
                    <li><ww:text name="'whitelist.admin.action.rules.description.line1'"/></li>
                    <li><ww:text name="'whitelist.admin.action.rules.description.line2'"/></li>
                    <li><ww:text name="'whitelist.admin.action.rules.description.line3'"/></li>
                </ul>
                <p><ww:text name="'whitelist.admin.action.rules.example'"/></p>
                <ul><li>http://www.atlassian.com/*</li></ul>
        </page:param>
        <aui:textarea id="'rules'" label="'Whitelisted URL Patterns'" mandatory="'false'" name="'rules'" rows="'20'" size="'long'" theme="'aui'"/>
    </page:applyDecorator>

</page:applyDecorator>
</body>
</html>
