<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <meta content="message" name="decorator" />
    <title><ww:text name="'contact.administrator.title'"/></title>
</head>
<body class="type-a">
<ww:if test="/sendEmail == true">
    <div class="content intform">
        <page:applyDecorator id="contact-administrators" name="auiform">
            <page:param name="action">ContactAdministrators.jspa</page:param>
            <page:param name="submitButtonName">Send</page:param>
            <page:param name="label"><ww:text name="'contact.administrator.title'"/></page:param>
            <page:param name="showHint">true</page:param>
            <page:param name="submitButtonText"><ww:text name="'admin.email.send'" /></page:param>
            <page:param name="cancelLinkURI"><ww:url value="'/secure/Dashboard.jspa'" atltoken="false"/></page:param>

            <page:applyDecorator name="auifieldset">
                <ww:property value="/renderedMessage" escape="false"/>
            </page:applyDecorator>
            <page:applyDecorator name="auifieldset">
                <page:applyDecorator name="auifieldgroup">
                    <aui:textfield id="'to'" label="text('admin.email.to')" disabled="true" mandatory="false" name="'to'" theme="'aui'" value="to"/>
                </page:applyDecorator>

                <page:applyDecorator name="auifieldgroup">
                    <aui:textfield id="'from'" label="text('admin.email.from')" mandatory="true" name="'from'" theme="'aui'" value="from"/>
                </page:applyDecorator>

                <page:applyDecorator name="auifieldgroup">
                    <aui:textfield id="'subject'" label="text('admin.email.subject')" mandatory="true" name="'subject'" theme="'aui'" value="subject"/>
                </page:applyDecorator>

                <page:applyDecorator name="auifieldgroup">
                    <aui:textarea id="'details'" label="text('contact.administrator.details')" mandatory="true" name="'details'" theme="'aui'" value="details" rows="10" cols="100"/>
                </page:applyDecorator>

            </page:applyDecorator>
        </page:applyDecorator>
    </div>
</ww:if>
<ww:else>
    <div class="content intform">
        <page:applyDecorator id="contact-administrators" name="auiform">
            <page:param name="action">Dashboard.jspa</page:param>
            <page:param name="submitButtonName">ok</page:param>
            <page:param name="label"><ww:text name="'contact.administrator.title'"/></page:param>
            <page:param name="submitButtonText"><ww:text name="'admin.common.words.ok'" /></page:param>

            <ww:property value="/renderedMessage" escape="false"/>
        </page:applyDecorator>
    </div>
</ww:else>
</body>
</html>
