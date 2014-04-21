AJS.$(function(){
    AJS.$.fn.isDirty = function(){}; // disable dirty form check

    // Handle the fetch license link which sends them off to my.atlassian.com
    AJS.$("#fetchLicense").click(function(){
        var formValues = AJS.$("#bonfire-setupwizard").serializeArray();
        var url = AJS.$(this).attr("data-url");
        AJS.$.post(url, formValues, function(){
            return false;
        });
    });
});
