AJS.$(function() {
    function dropWebSudo (successCallback) {
        AJS.$.ajax({
            type: "DELETE",
            url: contextPath + "/rest/auth/1/websudo",
            contentType: "application/json",
            success: successCallback
        });
    }

    AJS.$("#websudo-drop-from-protected-page").click(function(event) {
        dropWebSudo(function() {
            window.location = contextPath + "/secure/MyJiraHome.jspa";
        });
        event.preventDefault();
    });

    AJS.$("#websudo-drop-from-normal-page").click(function(event) {
        dropWebSudo(function() {
            AJS.$("#websudo-banner").slideUp();
            AJS.$("#websudo-banner").addClass("dropped");
        });
        event.preventDefault();
    });
});
