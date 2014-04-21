AJS.$(function() {

    var $rulesContainer = AJS.$("#configure-whitelist .rules-container");
    var allowChecked = AJS.$("#configure-whitelist-allow").click(function() {
        $rulesContainer.hide();
    }).attr("checked");

    if(allowChecked) {
        $rulesContainer.hide();
    }

    AJS.$("#configure-whitelist-restrict").click(function() {
        $rulesContainer.show();
    });
});