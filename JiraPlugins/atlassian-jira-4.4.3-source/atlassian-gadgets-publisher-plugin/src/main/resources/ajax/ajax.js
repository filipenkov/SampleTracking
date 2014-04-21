/**
 * Handles all the remote requests. Provides a jQuery.ajax-like interface for requests. See http://docs.jquery.com.
 *
 * When making ajax requests an 'authorisation' process will be used to determine
 * the method and settings required to make and authorised remote requests. The process is:
 *
 * jQuery - If the request url is part of the same web-app as the gadget
 * trustedApps - If the request url is not part of the same web-app
 * OAuth - If the request url is not part of the same web-app and anonymous access is not allowed.
 * anonymous - If the request url is not part of the same web-app and anonymous access is allowed
 *
 * @param {function(Object)} ajaxOptions - a restricted set of jQuery's ajaxOptions
 */
jQuery.ajax = function (options, queue) {

    var paramWhiteList = [
            /* We do not support all the jQuery ajax options as they need to be available in both jQuery and makeRequest */
            "success", "error", "complete", "send", "url", "data", "dataType", "headers", "type", "global", "oauthApprovalUrl"
        ],

        requestParams = AG.copyObject(jQuery.ajaxSettings),
        userParams = AG.copyObject(options, true),
        authQueue = queue || [AG.ajax.jQuery, AG.ajax.trustedApps, AG.ajax.OAuth, AG.ajax.anonymous],

        /* Make a request using first request mechanism from authQueue */
        request = authQueue.splice(0,1)[0];

    // remove JSONP settings to avoid jQuery 1.5+ treating all requests as JSONP (JRADEV-5180)
    delete requestParams.jsonpCallback;
    delete requestParams.jsonp;


    // trigger ajax start for the first request
    if (!queue && options.global !== false) {
        jQuery.event.trigger("ajaxStart", [{}, options]);
    }

    jQuery.extend(requestParams, userParams, {

        send: function (xhr) {

            if (options.send) {
                options.send.apply(this, arguments);
            }
        },

        complete: function (xhr) {

            if (options.global !== false) {
                jQuery.event.trigger("ajaxStop", [xhr, options]);
            }

            if (options.complete) {
                options.complete.apply(this, arguments);
            }
        },

        error: function (xhr) {

            if (options.error) {
                options.error.apply(this, arguments);
            }

            if (options.global !== false) {
                jQuery.event.trigger("ajaxError", [xhr, options]);
            }
        },

        success: function (data, textStatus, xhr) {

            if (options.success) {
                options.success.apply(this, arguments);
            }

            if (options.global !== false) {
                jQuery.event.trigger("ajaxSuccess", [xhr, options]);
            }
        },

        unauthorized: function (ops, xhr, approvalUrl) {
            options.oauthApprovalUrl = approvalUrl;
            jQuery.ajax(options, authQueue);
        }
    });

    jQuery.each(paramWhiteList, function (i, name) {
        if (jQuery.inArray(name, paramWhiteList) === -1) {
            console.warn("jQuery.ajax: param [" + name + "] is invalid. Ignoring...");
        }
    });

    return request(requestParams);
};