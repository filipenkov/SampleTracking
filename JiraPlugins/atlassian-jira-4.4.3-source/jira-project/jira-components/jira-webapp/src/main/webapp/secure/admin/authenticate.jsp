<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>

<html>
<head>
    <meta content="frontpage" name="decorator"/>
</head>
<body>
<h2><ww:text name="'websudo.title'"/></h2>

<page:applyDecorator id="login-form" name="auiform">
    <page:param name="action"><%= request.getContextPath() %>/secure/admin/WebSudoAuthenticate.jspa</page:param>
    <page:param name="method">post</page:param>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'websudo.warning'"/></p>
        </aui:param>
    </aui:component>
    <p><ww:text name="'websudo.description'"/></p>

    <page:applyDecorator name="auifieldset">
        <legend class="assistive"><ww:text name="'websudo.warning'"/></legend>
        <legend class="assistive"><ww:text name="'websudo.description'"/></legend>

        <page:applyDecorator name="auifieldgroup">
            <label><ww:text name="'common.words.username'"/></label>
            <span id="login-form-username" class="field-value"><ww:property value="/username" /></span>
            <a id="login-notyou" href="<%= request.getContextPath() %>/logout"><ww:text name="'websudo.notyou'"/></a>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <aui:password label="text('common.words.password')" id="authenticatePassword" name="'webSudoPassword'" theme="'aui'"/>
        </page:applyDecorator>


    </page:applyDecorator>

    <div class="buttons-container content-footer">
    <div class="buttons">
        <input id="authenticateButton" class="button" name="authenticate" type="submit" value="<ww:text name="'websudo.button'" />" />
        <a id="login-cancel" href="<ww:property value="cancelUrl" />"><ww:text name="'common.forms.cancel'"/></a>
    </div>
    </div>

    <ww:iterator value="/requestParameters">
        <ww:iterator value="./value">
                <ww:component name="../key" value="." template="hidden.jsp" theme="'aui'"/>
        </ww:iterator>
    </ww:iterator>
</page:applyDecorator>

</body>
</html>
