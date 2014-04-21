jQuery.namespace("AG.ajax.makeRequestToJQueryResponseHandler");

/**
 * Handles response the same way jQuery.ajax would
 *
 * @param settings
 * @param data
 * @param xhr
 */
AG.ajax.handleCallbacks = function (settings, data, xhr) {

    if (xhr && xhr.aborted) {
        return;
    }


    if (xhr.status >= 400) {

        if (settings.error) {
            settings.error(xhr, "error");
        }

        if (settings.complete) {
            settings.complete(xhr, "error");
        }

    } else {
        
        if (settings.complete) {
            settings.complete(xhr, "success");
        }

        if (settings.success) {
            settings.success(data, "success", xhr);
        }
    }
};