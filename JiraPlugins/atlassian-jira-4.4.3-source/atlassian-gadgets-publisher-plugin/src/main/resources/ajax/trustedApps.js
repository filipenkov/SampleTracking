jQuery.namespace("AG.ajax.trustedApps");

/**
 * Makes a request using gadgets.io.makeRequest but with jQuery.ajax formatted paramaters
 *
 * @param {object} options - jQuery ajax options 
 */
AG.ajax.trustedApps = function (options){

    var params,
        xhr = new AG.MockXHR();


    // Trusted apps is Atlassian ONLY

    if (!AG.isAtlassianContainer()) {

        if (options.unauthorized) {
            options.unauthorized(options, xhr);
        }

    } else {

        params = AG.mapToMakeRequestOptions(options);

        options.send();

        gadgets.io.makeRequest(params.url, function (response) {
            xhr.updateFromMakeRequestResp(response);
            AG.ajax.trustedApps.handleResponse(options, response, xhr);
        }, params);

    }

    return xhr;
};

/**
 * Handles makeRequest response
 *
 * @param options - ajax settings
 * @param {object} response - makeRequest response  object
 */
AG.ajax.trustedApps.handleResponse = function (options, response, xhr) {

    /* This header was only passed through to the client in AG.3.0. We use this header to determine if the response
        has accessed authenticated data using Trusted Apps or it has just returned anonymous data. If the header does not
        exist we cannot tell if trusted apps exist so throw an authError anyway. */
    var trustedAppsHeader = response.headers["x-seraph-trusted-app-status"];

    if (trustedAppsHeader && trustedAppsHeader[0] === "OK") {
        AG.ajax.handleCallbacks(options, response.data, xhr);
    } else if (options.unauthorized) {
        options.unauthorized(options, response, xhr);
    }
};