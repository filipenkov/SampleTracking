<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%--

Basic Usage:
<aui:component template="auimessage.jsp" theme="'aui'">

</aui:component>

Note: Use the parameter <aui:param name="'hideIcon'" value="true" /> if you do not want an icon
Note: If the message has a help link use the parameter <aui:param name="'helpKey'">JRAfookey</aui:param>
--%>

<jsp:include page="/template/aui/formFieldLabel.jsp" />
<span id="attach-max-size" class="hidden">130000000</span>
<span id="default-avatar-id" class="hidden"><ww:property value="parameters['defaultId']" /></span>
<span id="avatar-owner-id" class="hidden"><ww:property value="parameters['avatarOwnerId']" /></span>
<span id="avatar-owner-key" class="hidden"><ww:property value="parameters['avatarOwnerKey']" /></span>
<img class="jira-avatar-picker-trigger"
     title="<ww:property value="parameters['title']" />"
     alt="<ww:property value="parameters['title']" />"
     id="<ww:property value="parameters['id']" />"
     src="<ww:property value="parameters['src']" />"
     width="<ww:property value="parameters['width']" />"
     height="<ww:property value="parameters['height']" />" />
