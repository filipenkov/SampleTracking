<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
</head>

<body>
    <page:applyDecorator name="jiraform">
        <page:param name="action">ConfirmInstallationWithLicense.jspa</page:param>
        <page:param name="submitId">proceed_submit</page:param>
        <page:param name="submitName">Proceed</page:param>
        <page:param name="width">100%</page:param>
        <page:param name="title">Confirm License Update</page:param>
        <page:param name="description">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'setup.error.invalidlicensekey.confirminstall.problem.title'"/><p>
                    <ww:property value="/licenseProblem" escape="false"/>
                </aui:param>
            </aui:component>
            <div>
                <strong><ww:text name="'setup.error.invalidlicensekey.confirminstall.current.details'"/> : </strong><br>
                <table class="borderedBox" bgcolor="#fffff0">
                    <ww:property value="/licenseDetails">
                        <tr><td><b><ww:text name="'admin.license.organisation'"/>:</b>&nbsp;</td><td><ww:property value="./organisation" /></td></tr>
                        <tr><td><b><ww:text name="'admin.license.type'"/>:</b>&nbsp;</td><td><ww:property value="./description" /></td></tr>
                        <ww:if test="./partnerName != null && ./partnerName != ''">
                            <tr><td><b><ww:text name="'setup.error.invalidlicensekey.confirminstall.partner.name'"/>:</b>&nbsp;</td><td><ww:property value="./partnerName" /></td></tr>
                        </ww:if>
                        <ww:if test="./supportEntitlementNumber != null && ./supportEntitlementNumber != ''">
                            <tr><td><b><ww:text name="'admin.license.sen'"/>:</b>&nbsp;</td><td><ww:property value="./supportEntitlementNumber" /></td></tr>
                        </ww:if>
                        <tr><td><b><ww:text name="'admin.license.date.purchased'"/>:</b>&nbsp;</td><td><ww:property value="/licensePurchaseDate" /></td></tr>
                    </ww:property>
                </table>
            </div>
        </page:param>
        <ww:if test="userInfoAvailable == true">
            <ui:component label="'Admin User Name'" name="'userName'" template="userselect.jsp">
                <ui:param name="'formname'" value="'jiraform'" />
                <ui:param name="'imageName'" value="'userImage'"/>
                <ui:param name="'size'" value="40"/>
            </ui:component>

            <ui:component label="'Password'" name="'password'" template="password.jsp">
                <ui:param name="'size'">40</ui:param>
                <ui:param name="'description'"><ww:text name="'setup.error.invalidlicensekey.confirminstall.valid.user.name'"/></ui:param>
            </ui:component>
        </ww:if>

        <ui:component template="textlabel.jsp" label="text('admin.server.id')" value="/serverId"/>

        <ui:textarea label="'License'" name="'license'" cols="50" rows="10" >
            <ui:param name="'description'">
                <ww:text name="'system.error.license.line1.desc'" /><br>
            </ui:param>
        </ui:textarea>

    </page:applyDecorator>
</body>
</html>
