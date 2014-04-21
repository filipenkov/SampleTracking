<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<ww:generator id="hiddenFields" val="parameters['fields']" separator="','"/>
<ww:iterator value="@hiddenFields">
    <ui:component name="." template="hidden.jsp" theme="'single'" />
<%--    <ui:textfield label="." name="." />--%>
</ww:iterator>
<ww:generator id="hiddenFields" val="parameters['multifields']" separator="','"/>
<ww:iterator value="@hiddenFields">
    <ui:component name="." template="arrayhidden.jsp" />
</ww:iterator>

