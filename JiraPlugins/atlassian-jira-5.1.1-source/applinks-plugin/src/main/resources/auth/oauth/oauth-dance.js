AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {
    	var success = ($('.error').length == 0) && (AJS.$("#applink-authorized").val() == "true");
    	
        if (window.opener && window.opener.oauthCallback) {
            if (success) {
                window.opener.oauthCallback.success();
            }
            else {
                $('#continue-link').click(function() {
                    window.opener.oauthCallback.failure();
                });
            }
        }
        
        //if (window.opener && window.opener.ApplinksUtils && window.opener.ApplinksUtils.onAuthFinished) {
        //	applinkId = $("meta[name='atlassian-applink-id']").attr("content");
        //	window.opener.ApplinksUtils.onAuthFinished(applinkId, success);
        //    window.close();
        //}
        
    })(AJS.$)
});
