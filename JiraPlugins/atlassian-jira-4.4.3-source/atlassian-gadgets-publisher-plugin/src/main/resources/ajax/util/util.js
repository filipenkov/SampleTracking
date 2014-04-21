jQuery.namespace("AG");


AG.prefs = new gadgets.Prefs();


/**
 * Copies objects without references
 *
 * @param {object} object - to copy
 * @param {boolean} deep - weather to copy objects within object
 * @return {object}
 */
AG.copyObject = function (object, deep) {

    var copiedObject = {};

        AJS.$.each(object, function(name, property) {
            if (jQuery.isArray(property) || typeof property !== "object" || property === null || property instanceof AJS.$) {
                copiedObject[name] = property;
            } else if (deep !== false) {
                copiedObject[name] = AG.copyObject(property, deep);
            }
        });

    return copiedObject;
};


/**
 * Adds ouath approval tag and binds request options to it. This is used when creating oauth approval popup.
 *
 * @param approvalUrl
 * @param requestOptions
 */
AG.addApprovalTag = function (approvalUrl, requestOptions) {
    jQuery("<meta />").attr({name: "approvalUrl", content: approvalUrl})
            .data("requestOptions", requestOptions).appendTo("head");
};

/**
 * Gets oauth approval tag if there is one
 */
AG.getApprovalTag = function () {

    var $approvalTag = jQuery("meta[name=approvalUrl]");

    if ($approvalTag.length === 1) {
        return $approvalTag;
    }
};

/**
 * Returns if the gadget container is atlassian or not
 *
 * @return {boolean}
 */
AG.isAtlassianContainer = function () {
    return window._args
            && _args().container === "atlassian"
            && typeof atlassian !== "undefined";
};

/**
 * Checks if the url is relative
 *
 * @param {string} url
 */
AG.isRelativeURL = function (url) {
    return !/^(http|https):\/\//.test(url);
};


/**
 *
 * Gets text for i18n key. If key doesn't exist will return key instead.
 *
 * @param {String} key
 */
AG.getText = function (key) {
    if (AG.prefs.getMsg(key)) {
        arguments[0] = AG.prefs.getMsg(key);
        return AJS.format.apply(AJS, arguments)
    } else {
        return key;
    }
};

AG.shrinkText = function (text, length) {
    if (text.length > length + 3) {
        return [
            text.substring(0, Math.floor(length / 2)),
            text.substring(Math.floor(text.length - length / 2), text.length)
        ].join("...");
    } else {
        return text;
    }
}

/**
 * If the url is relative will convert to an absolute URL
 *
 * @param {string} url
 * @return {string}
 */
AG.getAbsoluteURL = function (url) {
    if (AG.isRelativeURL(url)) {
        // this currenly needs to be set in every gadget. We should be injecting this via publisher.
        return jQuery.ajaxSettings.baseUrl + url;
    } else {
        return url;
    }
};