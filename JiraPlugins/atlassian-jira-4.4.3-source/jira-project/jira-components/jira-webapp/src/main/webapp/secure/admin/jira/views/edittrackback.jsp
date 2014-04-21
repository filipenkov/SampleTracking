<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/issue_features"/>
    <meta name="admin.active.tab" content="trackbacks"/>
	<title><ww:text name="'admin.globalsettings.trackbacks.jira.trackbacks'"/></title>
</head>

<body>
<page:applyDecorator name="jiraform">
    <page:param name="helpURL">trackback</page:param>
    <page:param name="action">TrackbackAdmin.jspa</page:param>
    <page:param name="submitId">update_trackback</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="title"><ww:text name="'admin.globalsettings.trackbacks.trackback.settings'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <page:param name="cancelURI">TrackbackAdmin!initial.jspa</page:param>
    <page:param name="description">
        <ww:text name="'admin.globalsettings.trackbacks.description'">
          <ww:param name="'value0'"><em></ww:param>
          <ww:param name="'value1'"></em></ww:param>
          <ww:param name="'value2'"><a href="http://www.movabletype.org/docs/mttrackback.html"></ww:param>
          <ww:param name="'value3'"></a></ww:param>
          <ww:param name="'value4'"><br></ww:param>
        </ww:text>
        <ul>
            <li><ww:text name="'admin.globalsettings.trackbacks.description.incoming'">
              <ww:param name="'value0'"><b></ww:param>
              <ww:param name="'value1'"></b></ww:param>
              <ww:param name="'value2'"><a href="<ww:component name="'external.link.confluence.product.site'" template="externallink.jsp" />"></ww:param>
              <ww:param name="'value3'"></a></ww:param>
            </ww:text></li>
            <li>
            <ww:text name="'admin.globalsettings.trackbacks.description.outgoing'">
              <ww:param name="'value0'"><b></ww:param>
              <ww:param name="'value1'"></b></ww:param>
              <ww:param name="'value2'"><em></ww:param>
              <ww:param name="'value3'"></em></ww:param>
              <ww:param name="'value4'"><em></ww:param>
              <ww:param name="'value5'"></em></ww:param>
            </ww:text>
            </li>
            <li><ww:text name="'admin.globalsettings.trackbacks.description.url.patterns.to.exclude'">
              <ww:param name="'value0'"><b></ww:param>
              <ww:param name="'value1'"></b></ww:param>
            </ww:text>
                <ul>
                    <li><ww:text name="'admin.globalsettings.trackbacks.perl.instructions'"/></li>
                    <li><ww:text name="'admin.globalsettings.trackbacks.base.url.is.excluded'">
                        <ww:param name="'value0'"><i><ww:property value="applicationProperties/string('jira.baseurl')" />.*</i></ww:param>
                    </ww:text></li>
                    <li><ww:text name="'admin.globalsettings.trackbacks.one.expression.per.line'"/></li>
                    <li><ww:text name="'admin.globalsettings.example'"/>:<br>
                    .*server.domain.com.*<br>
                    .*server2.domain.com.*<br>
                    .*domain2.com.*
                    </li>
                    <li><ww:text name="'admin.globalsettings.trackbacks.for.more.information'">
                        <ww:param name="'value0'"><a href="http://perldoc.perl.org/perlre.html" TARGET="_blank"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                    </li>
                </ul>
            </li>

          <!--<li><strong>Allowed URL Patterns:</strong> A list of Perl style regular expressions that URLs will have to match before an outgoing trackback ping is sent. If this field is left blank then all URLs will be sent trackback pings.-->

        </ul>

        <p><ww:text name="'admin.common.phrases.more.information'"/></p>

    </page:param>


    <ui:component label="text('admin.globalsettings.trackbacks.incoming.trackbacks')" template="sectionbreak.jsp">
        <ui:param name="'nobreak'">true</ui:param>
    </ui:component>


    <tr>
		<td class="fieldLabelArea">
            <ww:text name="'admin.globalsettings.trackbacks.accept.incoming.trackback.pings'"/>
        </td>
		<td class="fieldValueArea">
			<input class="radio" type="radio" value="true" name="acceptPings" id="acceptPingsTrue" <ww:if test="acceptPings == true">checked="checked"</ww:if> /> <label for="acceptPingsTrue"><ww:text name="'admin.common.words.on'"/></label>
			&nbsp;
			<input class="radio" type="radio" value="false" name="acceptPings" id="acceptPingsFalse" <ww:if test="acceptPings == false">checked="checked"</ww:if> /> <label for="acceptPingsFalse"><ww:text name="'admin.common.words.off'"/></label>
            <div class="description">
                <ww:text name="'admin.globalsettings.trackbacks.description2.incoming'"/>
            </div>
		</td>
	</tr>

    <ui:component label="text('admin.globalsettings.trackbacks.outgoing.trackbacks')" template="sectionbreak.jsp" />

    <tr>
		<td class="fieldLabelArea">
            <ww:text name="'admin.globalsettings.trackbacks.send.outgoing.trackback.pings'"/>
        </td>
		<td class="fieldValueArea">
			<input class="radio" type="radio" value="allIssues" name="sendPings" id="sendPingsTrue" <ww:if test="sendPings == 'allIssues'">checked="checked"</ww:if> /> <label for="sendPingsTrue"><ww:text name="'admin.globalsettings.trackbacks.on.for.all'"/></label>
			&nbsp;
			<input class="radio" type="radio" value="public" name="sendPings" id="sendPingsPublic" <ww:if test="sendPings == 'public'">checked="checked"</ww:if> /> <label for="sendPingsPublic"><ww:text name="'admin.globalsettings.trackbacks.on.for.public'"/></label>
			&nbsp;
			<input class="radio" type="radio" value="false" name="sendPings" id="sendPingsFalse" <ww:if test="sendPings == 'false'">checked="checked"</ww:if> /> <label for="sendPingsFalse"><ww:text name="'admin.common.words.off'"/></label>
			<div class="description">
                <ww:text name="'admin.globalsettings.trackbacks.description2.outgoing'"/>
            </div>
		</td>
	</tr>
    <ui:textarea label="text('admin.globalsettings.trackbacks.url.patterns.to.exclude2')" name="'urlExcludePattern'" rows="6" >
        <ui:param name="'description'">
        <ww:text name="'admin.globalsettings.trackbacks.description2.url.patterns'"/>
        </ui:param>
    </ui:textarea>
    <%--<ui:textarea label="'Allowed URL Patterns'" name="'urlAllowedPattern'" rows="6" >--%>
        <%--<ui:param name="'description'">--%>
        <!--List of Perl regular expressions (one per line), of URL patternss you can send trackbacks to.-->
        <%--</ui:param>--%>
    <%--</ui:textarea>--%>

</page:applyDecorator>

</body>
</html>
