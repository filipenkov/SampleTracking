<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%--

Basic Usage:
<aui:component template="auimessage.jsp" theme="'aui'">

</aui:component>

Note: Use the parameter <aui:param name="'hideIcon'" value="true" /> if you do not want an icon
Note: If the message has a help link use the parameter <aui:param name="'helpKey'">JRAfookey</aui:param>
--%>

<ww:if test="parameters['nameValue']">
    <ww:property id="avatarValue" value="parameters['nameValue']" />
</ww:if>
<ww:elseIf test="parameters['hiddenvalue']">
    <ww:property id="avatarValue" value="parameters['hiddenvalue']" />
</ww:elseIf>
<jsp:include page="/template/aui/formFieldLabel.jsp" />
<div class="hidden"><input type="text" name="<ww:property value="parameters['name']"/>" id="<ww:property value="parameters['hiddenid']"/>" value="<ww:property value="@avatarValue" />" /></div>
<a
    <ww:if test="parameters['linkid']">id="<ww:property value="parameters['linkid']"/>"</ww:if>
    <ww:if test="parameters['linkclass']">class="<ww:property value="parameters['linkclass']"/>"</ww:if>
    href="<ww:property value="parameters['url']" escape="false"/>"
    <ww:if test="parameters['onclick']">onclick="<ww:property value="parameters['onclick']" escape="false"/>;return false;"</ww:if>
>
    <img
        <ww:if test="parameters['src']">
            title="<ww:property value="parameters['title']"/>"
            alt="<ww:property value="parameters['title']"/>"
            src="<ww:property value="parameters['src']" escape="false"/>"
        </ww:if>
        <ww:else>
            src="<%= request.getContextPath()%>/images/border/spacer.gif"
        </ww:else>
        name="<ww:property value="parameters['imagename']"/>"
        id="<ww:property value="parameters['id']"/>"
        <ww:if test="parameters['width']">
            width="<ww:property value="parameters['width']"/>"
        </ww:if>
        <ww:if test="parameters['height']">
            height="<ww:property value="parameters['height']"/>"
        </ww:if>
        <ww:if test="parameters['class']">class="<ww:property value="parameters['class']"/>"</ww:if>
        border="0"
    />
</a>
<span id="<ww:property value="parameters['textid']"/>">
    <ww:if test="!parameters['src']">
        <a href="#" onclick="<ww:property value="parameters['onclick']" escape="false"/>;return false;"><ww:text name="'admin.text.image.select.image'"/></a>
    </ww:if>
</span>
