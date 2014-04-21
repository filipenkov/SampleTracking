/**
 * Triggers event on instance
 *
 * @param {String} evt - Event Name
 * @param {Array} args - Args to pass to event handlers
 */
Class.prototype.triggerEvent = function (evtName, args) {

    jQuery(this).trigger(evtName, args);

    if (this.globalEventNamespaces) {
        jQuery.each(this.globalEventNamespaces, function (i, glbEvtName) {
            var newEvtName = evtName.replace(/.*(\..*)/, glbEvtName  + "$1");
            jQuery(AJS).trigger(newEvtName, args);
        });
    }

    return this;
};

/**
 * Bindes event on instance
 *
 * @param {String} evt - Event Name
 * @param {Array} args - Args to pass to event handlers
 */
Class.prototype.bind = function (evt, func) {
    jQuery(this).bind(evt, func);
    return this;
};

/**
 * Triggers event on AJS object
 *
 * @param {String} evt - Event Name
 * @param {Array} args - Args to pass to event handlers
 */
Class.prototype.triggerGlobal = function (evt, args) {
    jQuery(AJS).trigger(evt, args);
};


/**
 * Wraps a property as a function if it is not already one
 *
 * @param property
 * @return function
 */
JIRA.makePropertyFunc = function (property) {
    if (jQuery.isFunction(property)) {
        return property;
    } else {
        return function () {
            return property;
        }
    }
};

/**
 * Appends inline errors to form, focusing the first field with error
 *
 * @param {jQuery} form
 * @param {Object<fieldName>:<errorMessage>} errors
 */
JIRA.applyErrorsToForm = function (form, errors) {

    var $focusField;

    jQuery.each(errors, function (name, message) {
        var $group,
            $error,
            $field = form.find(":input[name='" + name + "']");

        if ($field.length === 1) {
            if (!$focusField) {
                $focusField = $field; // store first field with error so we can focus it at the end
            }
            $error = jQuery("<div class='error' />").text(message);
            $group = $field.closest(".field-group");
            $group.find(".error").remove(); // remove any pre-existing errors
            $group.append($error);
        }
    });

    if ($focusField) {
        $focusField.focus();
    }
};

/**
 * Prepends error message (aui style) to form body
 *
 * @param {jQuery} form
 * @param {String} error
 */
JIRA.applyMessageToForm = function (type, form, message, dismissable) {

    var $errorCtx = form.find(".aui-message-context");

    if (!$errorCtx.length) {
        $errorCtx = jQuery("<div class='aui-message-context' />");

        if (JIRA.useLegacyDecorator()) {
            $errorCtx.prependTo(form.find(".content-body"));
        } else {
            $errorCtx.prependTo(form.find(".form-body"));
        }
    }

    $errorCtx.empty();

    AJS.messages[type]($errorCtx, {
        shadowed: false,
        closeable: dismissable,
        body: message
    });
};

/**
 * Displays a message depicting which issue has been successfully updated on this issue navigator.
 */
JIRA.notifyNavigatorOfIssueModification = function () {

    var issueId,
        issueKey,
        sessionStorge = JIRA.SessionStorage;

    if (JIRA.IssueNavigator.isNavigator()) {
            issueId = JIRA.IssueNavigator.getSelectedIssueId();
            issueKey = JIRA.IssueNavigator.getSelectedIssueKey();
        if (issueId) {
            sessionStorge = JIRA.SessionStorage;
            sessionStorge.setItem('selectedIssueId', issueId);
            sessionStorge.setItem('selectedIssueKey', issueKey);
        }
    }
}

JIRA.useLegacyDecorator = function () {
    return !JIRA.Version.isGreaterThanOrEqualTo("5.0");
};

JIRA.applyErrorMessageToForm = function (form, error) {
    return JIRA.applyMessageToForm("error", form, error, false);
};

JIRA.applySuccessMessageToForm = function (form, message) {
    return JIRA.applyMessageToForm("success", form, message, true);
}
