jQuery.namespace("AG.ajax.mapToMakeRequestOptions");

/**
 * Converts jQuery ajax options to ones consumable by gadgets.io.makeRequest
 *
 * @param {object} options
 * @return {object}
 */
AG.mapToMakeRequestOptions = function (options) {

    var params = {},
        prefix;

    params.url = AG.getAbsoluteURL(options.url);

    params.OWNER_SIGNED = options.signOwner;
    params.VIEWER_SIGNED = options.signViewer;

    if (options.headers) {
        params[gadgets.io.RequestParameters.HEADERS] = options.headers;
    }

    if (options.authorization) {
        params[gadgets.io.RequestParameters.AUTHORIZATION] = gadgets.io.AuthorizationType[options.authorization.toUpperCase()];
    }

    if (options.useToken) {
        params[gadgets.io.RequestParameters.OAUTH_USE_TOKEN] = options.useToken;
    }

    if (options.oauthServiceName) {
        params[gadgets.io.RequestParameters.OAUTH_SERVICE_NAME] = options.oauthServiceName;
    }

    if (options.summaries) {
        params[gadgets.io.RequestParameters.GET_SUMMARIES] = options.summaries;
    }

    if (options.entries) {
        params[gadgets.io.RequestParameters.NUM_ENTRIES] = options.entries;
    }

    if (options.dataType) {
        if (options.dataType.toUpperCase() === "XML") {
            params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.DOM;
        } else {
            params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType[options.dataType.toUpperCase()];
        }
    }

    if (options.data) {

        if (!options.type || options.type.toLowerCase() == "get") {

            if (options.url.indexOf("?") >= 0) {
                if(options.url.lastIndexOf("&") == options.url.length - 1) {
                    prefix = "";
                } else {
                    prefix = "&"
                }
            } else {
                prefix = "?";
            }

            if (typeof options.data == "string") {
                params.url += prefix + options.data.replace(/^\?/,"");
            } else {
                params.url += prefix + gadgets.io.encodeValues(options.data).replace(/^\?/,"");
            }

        } else {
            params[gadgets.io.RequestParameters.POST_DATA] = options.data;
        }
    }

    if (options.type) {
        params[gadgets.io.RequestParameters.METHOD] = gadgets.io.MethodType[options.type];
    }

    return params;
};