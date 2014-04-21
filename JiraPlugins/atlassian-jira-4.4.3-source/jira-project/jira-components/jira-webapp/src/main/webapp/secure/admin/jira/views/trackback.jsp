<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/issue_features"/>
    <meta name="admin.active.tab" content="trackbacks"/>
	<title><ww:text name="'admin.globalsettings.trackbacks.jira.trackbacks'"/></title>
</head>

<body>
<%-- error messages --%>
<ww:if test="hasErrorMessages == 'true'">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'titleText'"><ww:text name="'admin.common.words.errors'"/></aui:param>
        <aui:param name="'messageHtml'">
            <ul>
                <ww:iterator value="errorMessages">
                    <li><ww:property /></li>
                </ww:iterator>
            </ul>
        </aui:param>
    </aui:component>
</ww:if>
    <page:applyDecorator name="jiratable">
        <page:param name="title"><ww:text name="'admin.globalsettings.trackbacks.trackback.settings'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="titleColspan">2</page:param>
        <page:param name="helpURL">trackback</page:param>
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
          </ww:text></p></li>
          <li><ww:text name="'admin.globalsettings.trackbacks.description.outgoing'">
              <ww:param name="'value0'"><b></ww:param>
              <ww:param name="'value1'"></b></ww:param>
          </ww:text></p></li>
          <li><ww:text name="'admin.globalsettings.trackbacks.description.url.patterns.to.exclude'">
              <ww:param name="'value0'"><b></ww:param>
              <ww:param name="'value1'"></b></ww:param>
          </ww:text></li>
          </ul>
          <p><ww:text name="'admin.common.phrases.more.information'"/></p>

        </page:param>

        <tr>
            <td width="40%">
                <b><ww:text name="'admin.globalsettings.trackbacks.accept.incoming.trackback.pings'"/></b>
            </td>
            <td width=60%>
                <ww:if test="acceptPings == true">
                    <span class="status-active"><ww:text name="'admin.common.words.on'"/></span>
			    </ww:if>
			    <ww:else>
				    <span class="status-inactive"><ww:text name="'admin.common.words.off'"/></span>
                </ww:else>
            </td>
        </tr>
        <tr>
            <td>
                <b><ww:text name="'admin.globalsettings.trackbacks.send.outgoing.trackback.pings'"/></b>
            </td>
            <td>
                <ww:if test="sendPings == 'allIssues'">
                    <span class="status-active"><ww:text name="'admin.common.words.on'"/></span> (<ww:text name="'admin.globalsettings.trackbacks.for.all.issues'"/>)
			    </ww:if>
                <ww:elseIf test="sendPings == 'public'">
                    <span class="status-active"><ww:text name="'admin.common.words.on'"/></span> (<ww:text name="'admin.globalsettings.trackbacks.for.public.issues.only'"/>)
			    </ww:elseIf>
			    <ww:else>
				    <span class="status-inactive"><ww:text name="'admin.common.words.off'"/></span>
                </ww:else>
            </td>
        </tr>
        <tr>
            <td>
                <b><ww:text name="'admin.globalsettings.trackbacks.url.patterns.to.exclude'"/></b>
            </td>
            <td>
                <ww:property value="/urlExcludePattern" />
            </td>
        </tr>
    </page:applyDecorator>

<div class="buttons-container aui-toolbar form-buttons noprint">
    <div class="toolbar-group">
        <span class="toolbar-item">
        	<a class="toolbar-trigger" href="TrackbackAdmin!default.jspa"><ww:text name="'admin.common.phrases.edit.configuration'"/></a>
        </span>
    </div>
</div>

</body>
</html>
