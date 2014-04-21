<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.services.services'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="services"/>
</head>

<body>

<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.services.services'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">services</page:param>
</page:applyDecorator>


<table class="aui aui-table-rowhover" id="tbl_services">
    <thead>
        <tr>
            <th width="30%">
                <ww:text name="'admin.common.phrases.name.class'"/>
            </th>
            <th width="50%">
                <ww:text name="'admin.common.words.properties'"/>
            </th>
            <th width="10%">
                <ww:text name="'admin.services.delay.mins'"/>
            </th>
            <th width="10%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
	<tbody>
	<ww:iterator value="services" status="'status'">
        <tr id="service-<ww:property value="./id"/>" <ww:if test="./usable == false">class="disabled"</ww:if> >
            <td>
                <span id="service-name-<ww:property value="./id"/>">
                    <strong><ww:property value="./name"/></strong>
                </span>
                <div id="service-class-<ww:property value="./id"/>" class="description">
                    <ww:property value="./serviceClass"/>
                </div>
            </td>
            <td>
            <%-- get the property set for this service, and then get all the keys where the propertyset is of type String ('5') --%>
                <ul>
                    <ww:iterator value="/propertyMap(.)/keySet">
                        <li><strong><ww:property value="." />:</strong> <ww:property value="/text(/propertyMap(..)/(.))" /></li>
                    </ww:iterator>
                </ul>
            </td>
            <td><ww:property value="delayInMins(.)"/></td>
            <td <ww:if test="./usable == false">class="disabled"</ww:if>>
                <ul class="operations-list">
                    <ww:if test="./usable == true">
                    <li><a id="edit_<ww:property value="id"/>" href="<ww:url page="EditService!default.jspa"><ww:param name="'id'" value="id"/></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                    <ww:if test="./internal == false">
                        <li><a id="del_<ww:property value="id"/>" href="<ww:url page="ViewServices.jspa"><ww:param name="'delete'" value="id"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                    </ww:if>
                    </ww:if>
                    <ww:else>
                        <li><a id="del_<ww:property value="id"/>" href="<ww:url page="ViewServices.jspa"><ww:param name="'delete'" value="id"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                    </ww:else>
                </ul>
            </td>
        </tr>
	</ww:iterator>
    </tbody>
</table>


<script type="text/javascript">
  function showServices() {
        var servicesDiv = document.getElementById("builtinServices");
        var servicesArrow = document.getElementById("builtinServicesArrow");
        if (servicesDiv.style.display == 'none') {
          servicesDiv.style.display = '';
          servicesArrow.src='<%= request.getContextPath() %>/images/icons/navigate_down.gif';
        } else {
          servicesDiv.style.display='none';
          servicesArrow.src='<%= request.getContextPath() %>/images/icons/navigate_right.gif';
        }
  }
  function setService(clazz) {
        var classField = document.getElementById("serviceClass");
        var nameField = document.getElementById("serviceName");
        classField.value = clazz;
        nameField.focus();
  }
</script>

<page:applyDecorator name="jiraform">
	<page:param name="action">ViewServices.jspa</page:param>
	<page:param name="submitId">addservice_submit</page:param>
	<page:param name="submitName"><ww:text name="'admin.services.add.service'"/></page:param>
	<page:param name="title"><ww:text name="'admin.services.add.service'"/></page:param>
	<page:param name="width">100%</page:param>
    <page:param name="helpURL">services</page:param>
    <page:param name="helpURLFragment">#Registering+a+Service</page:param>
<%--	<page:param name="helpDescription">with Services</page:param>--%>
	<page:param name="description"><ww:text name="'admin.services.add.service.instruction'"/></page:param>

	<ui:textfield label="text('common.words.name')" name="'name'" size="'30'">
        <ui:param name="'cssId'">serviceName</ui:param>
    </ui:textfield>

	<ui:textfield label="text('admin.services.class')" name="'clazz'" size="'60'">
        <ui:param name="'cssId'">serviceClass</ui:param>
        <ui:param name="'description'">

        <img id="builtinServicesArrow" src="<%= request.getContextPath() %>/images/icons/navigate_right.gif" width=8 height=8 border=0>
        <a href="#" onclick="showServices(); return false;"><ww:text name="'admin.services.built.in.services'"/></a>

              <div id="builtinServices" style="display: none">
                  <ul>
                      <ww:iterator value="inBuiltServiceTypes">
                          <li><a href="#" onclick="setService('<ww:property value="./type/name"/>');"><ww:text name="./i18nKey"/></a></li>
                      </ww:iterator>
                  </ul>
              </div>
        </ui:param>
    </ui:textfield>
	<ui:textfield label="text('admin.services.delay')" name="'delay'" size="'30'">
        <ui:param name="'description'"><ww:text name="'admin.services.delay.description'"/></ui:param>
    </ui:textfield>
</page:applyDecorator>


</body>
</html>
