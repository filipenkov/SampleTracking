<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section"/>
    <meta name="admin.active.tab" content="general_configuration"/>       
    
    <title><ww:text name="'admin.advancedproperties.settings.heading'"/></title>
</head>
<body>
<h3 class="formtitle">
    <ww:text name="'admin.advancedproperties.settings.heading'"/>
</h3>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">warning</aui:param>
    <aui:param name="'messageHtml'">
        <p><ww:text name="'admin.advancedproperties.settings.description'"/></p>
    </aui:param>
</aui:component>

<table id="settingsBlock" class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th width="60%">Key</th>
            <th>Value</th>
            <th></th>
        </tr>
    </thead>
    <tbody id="propsBody">

    </tbody>
</table>

<script type="text/javascript">

        jQuery(function() {

            var propsTable = jQuery("#propsBody");
            var editing = false;
            var propRest = contextPath + "/rest/api/2/application-properties";

            jQuery.ajax({
                url: propRest,
                success: function(data) {
                    jQuery.each(data, function(counter, element){
                        // for each element in the response, add a row with the corresponding keys/values/descirptions
                        var key = element.key;
                        var value = element.value;
                        var descHtml = "";
                        if ("desc" in element) {
                            descHtml = '<div class="description">'+ AJS.escapeHtml(element.desc) + '</div>';
                        }

                        propsTable.append('<tr><td class="key"><b>'
                                + AJS.escapeHtml(key) + '</b>'
                                + descHtml + '</td><td class="val" id="props_edit_'+counter+'"></td><td id="ops_'+counter+'">&nbsp;</td></tr>');
                        if ("defaultValue" in element) {
                            propsTable.append('<input id="props_default_' + counter + '"type="hidden" value="' + AJS.escapeHtml(element.defaultValue) + '"/>');
                        }
                        jQuery("#props_edit_" + counter).text(value);

                        // TODO if is a boolean type, make into a checkbox
                        var field = jQuery('<input type="text" size="'+ Math.max(20, value.length) + '"/>').val(value);

                        // Update the value by posting to the rest resource
                        // if successful, update the table and check if we need to display the 'revert' button
                        var updateValue = function(props_edit, newVal) {
                            jQuery.ajax({
                                url: propRest + "?key="+ key + "&value=" + encodeURIComponent(newVal),
                                type: "POST",
                                success: function(data) {
                                    jQuery(props_edit).text(data.value);
                                    value=data.value;
                                    field.val(value); // next time they view the input, it will be the val
                                    checkDefaultAfterUpdate(data);
                                },
                                error: function(xhr, status, error){
                                    jQuery("#props_edit_"+counter).click(); // go back to editing the same row
                                    field.before('<div class="errMsg">'+AJS.escapeHtml(xhr.responseText)+'</div>');
                                }
                            });
                        };

                        // When the particular row is clicked, add appropriate buttons and handle their click events
                        var editrow = jQuery("#props_edit_"+counter).click(function(){
                            var props_edit = this;
                            if (!editing)  {
                                editing = true;

                                jQuery(this).html(field);

                                var updatebutton = jQuery("<button>Update</button>").click(function(){
                                    jQuery("#ops_" + counter).html("");
                                    editing = false;
                                    // Only bother posting if the new value is actually different
                                    if (field.val() != value) {
                                        updateValue(props_edit, field.val());
                                    } else {
                                        jQuery(props_edit).text(field.val());
                                        checkDefault();
                                    }
                                });

                                var cancellink = jQuery("<a href='#'>Cancel</a>").click(function(){
                                    jQuery(props_edit).text(value);
                                    editing = false;
                                    jQuery("#ops_" + counter).html("");
                                    checkDefault();
                                });

                                jQuery("#ops_" + counter).html(updatebutton).append("&nbsp;&nbsp;").append(cancellink);
                            }
                        });

                        // Check and determine if we need to display/hide the 'revert' button
                        var checkDefault = function() {
                            var defaultVal = jQuery("#props_default_" + counter).attr("value");
                            if (defaultVal) {
                                var value = jQuery("#props_edit_" + counter).text();
                                renderRevertButton(defaultVal, value);
                            }
                        };

                        var checkDefaultAfterUpdate = function(element){
                            if ("defaultValue" in element) {
                                renderRevertButton(element.defaultValue, element.value);
                                defaultValue = '<input id="props_default_' + counter + '"type="hidden" value="' + AJS.escapeHtml(element.defaultValue) + '"/>';
                                propsTable.append(defaultValue);
                            }
                        };
                        
                        var renderRevertButton = function(defaultValue, currValue) {
                            var button = jQuery("<button>Revert</button>").click(function() {
                                    updateValue(editrow, defaultValue);
                                    // if we've just reverted, we won't need the rever button anymore
                                    jQuery("#ops_" + counter).html("");
                                }).css({"margin-left": "auto", "margin-right": "3px"});

                                if (defaultValue != currValue) {
                                    jQuery("#ops_" + counter).html(button);
                                } else {
                                    jQuery("#ops_" + counter).html("");
                                }
                        };

                        // Check if we want revert when the page loads
                        checkDefault();
                    });

                    jQuery("#propsBody td.val").hover(
                        function(){
                            jQuery(this).css('cursor', 'pointer');
                        },
                        function(){
                            jQuery(this).css('cursor', 'auto');
                        }
                    );

                    }});
        });
</script>

<div class="buttons-container">
    <div class="buttons">
        <a class="aui-button" href="ViewApplicationProperties.jspa"><ww:text name="'admin.menu.general.settings'"/></a>
    </div>
</div>

</body>
</html>