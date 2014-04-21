jQuery.namespace("AG.ajax.anonymous");

/**
 * Makes a request using gadgets.io.makeRequest but with jQuery.ajax formatted paramaters
 *
 * @param {object} options - jQuery ajax options
 */
AG.ajax.anonymous = function (options){

    var xhr = new AG.MockXHR(),
        params = AG.mapToMakeRequestOptions(options);

    options.send();

    gadgets.io.makeRequest(params.url, function (response) {
        xhr.updateFromMakeRequestResp(response);
        AG.ajax.anonymous.handleResponse(options, response, xhr);
    }, params);

    return xhr;
};

/**
 * Handles makeRequest response
 *
 * @param options - ajax settings
 * @param {object} response - makeRequest response  object
 */
AG.ajax.anonymous.handleResponse = function (options, response, xhr) {

    if (response.rc === 401 || response.rc === 403) {

        jQuery(document).trigger("ajax.anonymousAccessDenied", [xhr, options]);

    } else {

        AG.ajax.handleCallbacks(options, response.data, xhr);
        jQuery(document).trigger("ajax.anonymousAccess", [xhr, options]);

    }
};
