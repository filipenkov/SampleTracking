AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {
        if (window.opener && window.opener.oauthCallback) {
            if ($('.error').length == 0) {
                window.opener.oauthCallback.success();
            } else {
                $('#continue-link').click(function() {
                    window.opener.oauthCallback.failure();
                });
            }
        }
    })(AJS.$)
});
