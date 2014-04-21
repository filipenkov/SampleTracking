jQuery.namespace("AG.ajax.jQuery");

/**
 * Makes request using jQuery.ajax. We use this when the baseURL of the gadget is the same as the containers baseURL.
 *
 * @param {object} options - jQuery ajax options
 */
AG.ajax.jQuery = function ($ajax){

    // closure to store reference to jQuery.ajax as we override it

    var atlassian = window.atlassian || {};

    return function (options) {

        var params,
            xhr = new AG.MockXHR(); // use a standard xhr object for both jQuery and makeRequest

        options.url = AG.getAbsoluteURL(options.url);
        options.global = false; // we handle all jQuery global methods in our overriden jQuery.ajax

        params = AG.copyObject(options);

        // we override these handlers
        delete params.success;
        delete params.error;
        delete params.complete;

        params.success = function (data, status, realXHR) {
            xhr.updateFromJQueryXHR(realXHR);
            AG.ajax.handleCallbacks(options, data, xhr);
        };

        params.error = function (realXHR) {
            xhr.updateFromJQueryXHR(realXHR);
            AG.ajax.handleCallbacks(options, null, xhr);
        };

        if (typeof atlassian.util === "undefined" || atlassian.util.getRendererBaseUrl() !== options.baseUrl) {
            options.unauthorized(options, xhr);
        } else {
            options.send();
            $ajax(params);
        }

        return xhr;
    };

}(jQuery.ajax);
