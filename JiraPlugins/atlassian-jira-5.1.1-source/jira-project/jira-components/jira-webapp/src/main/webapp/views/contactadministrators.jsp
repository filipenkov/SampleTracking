<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <title><ww:text name="'contact.administrator.title'"/></title>
    <ww:if test="/shouldDisplayForm == true">
        <meta name="decorator" content="panel-general" />
    </ww:if>
    <ww:else>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/shouldDisplayForm == true">
    <page:applyDecorator id="contact-administrators" name="auiform">
        <page:param name="action">ContactAdministrators.jspa</page:param>
        <page:param name="submitButtonName">Send</page:param>
        <page:param name="showHint">true</page:param>
        <page:param name="submitButtonText"><ww:text name="'admin.email.send'" /></page:param>
        <page:param name="cancelLinkURI"><ww:url value="'/secure/MyJiraHome.jspa'" atltoken="false"/></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'contact.administrator.title'"/></aui:param>
        </aui:component>

        <ww:if test="/hasCustomMessage == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <ww:property value="/renderedMessage" escape="false"/>
                </aui:param>
            </aui:component>
        </ww:if>

        <page:applyDecorator name="auifieldset">
            <page:applyDecorator name="auifieldgroup">
                <aui:textfield id="'to'" label="text('admin.email.to')" disabled="true" mandatory="false" name="'to'" value="to" theme="'aui'" />
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <aui:textfield id="'from'" label="text('admin.email.from')" mandatory="true" name="'from'" value="from"  theme="'aui'"/>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <aui:textfield id="'subject'" label="text('admin.email.subject')" mandatory="true" name="'subject'" value="subject" size="'long'" theme="'aui'" />
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <aui:textarea id="'details'" label="text('contact.administrator.details')" mandatory="true" name="'details'" value="details" rows="10" size="'long'" theme="'aui'" />
            </page:applyDecorator>

        </page:applyDecorator>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'contact.administrator.title'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <ww:property value="/renderedMessage" escape="false"/>
            </aui:param>
        </aui:component>
    </div>
</ww:else>
</body>
</html>
