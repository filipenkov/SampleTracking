/**
 * The idea here is that people can override the default handling of events
 */


(function () {

    var events = {

        "ajax.anonymousAccessDenied" : function (e, xhr, options) {
            if (options.oauthApprovalUrl) {
                AG.sysMsg.addOauthApprovalMsg(options);
            } else {

                if (xhr.responseText && xhr.responseText.length) {
                    AG.sysMsg.addError({
                        message: xhr.responseText.replace(/(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig,
                            "<a href='$1' target='_blank'>$1</a>")
                    });
                } else {
                    AG.sysMsg.addError(AG.getText("gadget.common.authrequired"));
                }

            }
        },

        "ajax.anonymousAccess" : function (e, xhr, options) {

            if (options.oauthApprovalUrl) {
                AG.sysMsg.addOauthApprovalMsg(options);
            }
        },

        "ajax.oAuthAvailable" : function (e, approvalUrl, requestOptions) {}
    };

    // bind all the events. Note the names of these events need to be a 1 -1 match to the event fired.
    jQuery.each(events, function (name, handler) {

        jQuery(document).bind(name, function () {

            var that = this,
                args = arguments,
                originalHandler = events[name];

                args[0].doDefault = function () {
                    return originalHandler.apply(that, args)
                };

            if (AG.events && AG.events[name]) {
                AG.events[name].apply(this, args)
            } else {
                events[name].apply(this, arguments);
            }
        });
    });

})();

