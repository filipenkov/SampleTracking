AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {
        AppLinks.OAuthCallback = function() {

        };

        AppLinks.OAuthCallback.prototype.success = function() {
            this.aouthWindow.close();
            this.onSuccess();
        };

        AppLinks.OAuthCallback.prototype.failure = function() {
            this.aouthWindow.close();
            this.onFailure();
        };

        AppLinks.OAuthCallback.prototype.show = function(url, onSuccess, onFailure) {
            this.onSuccess = onSuccess;
            this.onFailure = onFailure;
            this.aouthWindow = window.open(url, "com_atlassian_applinks_authentication");
        };

        oauthCallback = new AppLinks.OAuthCallback();

        AppLinks.authenticateRemoteCredentials = function(url, onSuccess, onFailure) {
            $('.applinks-error').remove();
            oauthCallback.show(url, onSuccess, onFailure);
        };
    })(AJS.$)
});
