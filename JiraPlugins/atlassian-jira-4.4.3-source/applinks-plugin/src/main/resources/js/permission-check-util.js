AJS.$(document).bind(AppLinks.Event.PREREADY, function() {
    (function($) {
        AppLinks = AJS.$.extend(window.AppLinks || {}, {
            checkPermission: function(permissionFn, link, authRequired, noLink, errorFn) {
                var permSettings = {
                    noPermission:               function() {
                        noLink(AppLinks.I18n.getText(AppLinks.I18n.getText('applinks.dialog.no.permission.to.delete.remote')));
                    },
                    missing:                    noLink,
                    noAuthenticationConfigured: noLink,
                    noAuthentication:           noLink,
                    authenticationFailed:       noLink,
                    credentialsRequired:        authRequired,
                    noConnection: function() {
                        noLink(AppLinks.I18n.getText(AppLinks.I18n.getText('applinks.dialog.delete.link.no.connection')));
                    },
                    allowed: link,
                    unrecognisedCode: function(code) {
                        noLink(AppLinks.I18n.getText("applinks.dialog.invalid.permission.code", code))
                    }
                };
                permissionFn(AppLinks.SPI.processPermissionCode(permSettings), errorFn);
            }
        })
    })(AJS.$)
});
