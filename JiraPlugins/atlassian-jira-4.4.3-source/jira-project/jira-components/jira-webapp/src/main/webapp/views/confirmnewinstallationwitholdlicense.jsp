<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<body>

<ww:if test="licenseUpdated == true">
    <page:applyDecorator name="jirapanel">
	    <page:param name="width">100%</page:param>
	    <page:param name="title"><ww:text name="'admin.license.update.license'"/></page:param>
        <page:param name="description">
            <font color="#cc0000"><b><ww:text name="'system.error.license.updated.please.restart'"/>
            <p>
            <ww:text name="'system.error.restart.for.changes'"/></b></font>
            <p><ww:text name="'system.error.click.here.after.restart'">
                   <ww:param name="value0"><a href="<%= request.getContextPath() %>/"></ww:param>
                   <ww:param name="value0"></a></ww:param>
               </ww:text>
        </page:param>
    </page:applyDecorator>
</ww:if>
<ww:elseIf test="installationConfirmed == true">
    <page:applyDecorator name="jirapanel">
	    <page:param name="width">100%</page:param>
	    <page:param name="title"><ww:text name="'admin.license.update.license'"/></page:param>
        <page:param name="description">
            <font color="#cc0000"><b><ww:text name="'system.error.installation.complete.please.restart'"/>
            <p>
            <ww:text name="'system.error.restart.for.changes'"/></b></font>
            <p><ww:text name="'system.error.click.here.after.restart'">
                   <ww:param name="value0"><a href="<%= request.getContextPath() %>/"></ww:param>
                   <ww:param name="value0"></a></ww:param>
               </ww:text>
        </page:param>
    </page:applyDecorator>
</ww:elseIf>
<ww:else>
    <page:applyDecorator name="jiraform">
        <page:param name="action">ConfirmNewInstallationWithOldLicense.jspa</page:param>
        <page:param name="submitId">proceed_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.proceed'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'system.error.confirm.new.installation'"/></page:param>
        <page:param name="description">
            <p><ww:text name="'system.error.this.jira.was.created.on'">
                    <ww:param name="value0"><ww:property value="/buildUtilsInfo/version"/></ww:param>
                    <ww:param name="value1"><font size="1"></ww:param>
                    <ww:param name="value2"><ww:property value="/buildUtilsInfo/currentBuildNumber"/></ww:param>
                    <ww:param name="value3"></font></ww:param>
                    <ww:param name="value4"><ww:property value="/currentBuildDate"/></ww:param>
                </ww:text></p>
            <p><ww:property value="licenseStatusMessage" escape="false"/></p>
            <p style="color:#f00;"><ww:text name="'system.error.or'"/></p>
            <p><ww:text name="'system.error.proceed.under.evaluation.terms'">
                    <ww:param name="value0"><b></ww:param>
                    <ww:param name="value1"></b></ww:param>
                </ww:text></p>
        </page:param>

        <ui:component label="text('system.error.admin.username')" name="'userName'" template="userselect.jsp">
            <ui:param name="'formname'" value="'jiraform'" />
            <ui:param name="'imageName'" value="'userImage'"/>
            <ui:param name="'size'" value="40"/>
        </ui:component>

        <ui:component label="text('common.words.password')" name="'password'" template="password.jsp">
            <ui:param name="'size'">40</ui:param>
            <ui:param name="'description'"><ww:text name="'system.error.admin.username.password.desc'"/></ui:param>
        </ui:component>


        <tr>
            <td style="background-color:#fffff0;">&nbsp;</td>
            <td style="background-color:#fff;">&nbsp;</td>
        </tr>

        <tr>
            <td style="background-color:#fffff0;">&nbsp;</td>
            <td style="background-color:#fff; color:#f00; font-weight:bold;">
                <ww:text name="'system.error.either.enter.license.key'"/>
            </td>
        </tr>

        <page:param name="license"><ww:text name="'system.error.copy.and.paste.license.key.below'"/></page:param>

        <ui:component template="textlabel.jsp" label="text('admin.server.id')" value="/serverId"/>

        <ui:textarea label="text('admin.license')" name="'license'" cols="50" rows="10" >
            <ui:param name="'description'">
                <ww:text name="'system.error.license.line1.desc'" /><br>
                <ww:text name="'system.error.license.line2.desc'">
                    <ww:param name="'value0'"><a target="_blank" href="<ww:component name="'external.link.jira.license.view'" template="externallink.jsp" >
                        <ww:param name="'value0'"><ww:property value="/buildUtilsInfo/version"/></ww:param>
                        <ww:param name="'value1'"><ww:property value="/buildUtilsInfo/currentBuildNumber"/></ww:param>
                        <ww:param name="'value2'">Enterprise</ww:param>
                        <ww:param name="'value3'"><ww:property value="/serverId"/></ww:param>
                        </ww:component>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </ui:param>
        </ui:textarea>


        <tr>
            <td style="background-color:#fffff0;">&nbsp;</td>
            <td style="background-color:#fff; color:#f00; font-weight:bold;">
                <ww:text name="'system.error.or.confirm.evaluation.period'"/>
            </td>
        </tr>

        <ui:checkbox label="text('admin.evaluation')" name="'confirm'" fieldValue="'confirmed'" >
            <ui:param name="'description'"><ww:text name="'system.error.check.box.to.agree.evaluation.terms'"/></ui:param>
        </ui:checkbox>

    </page:applyDecorator>
</ww:else>

</body>
</html>
