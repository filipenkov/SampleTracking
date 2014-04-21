/**
 * Triggers event on instance
 *
 * @param {String} evt - Event Name
 * @param {Array} args - Args to pass to event handlers
 */
Class.prototype.triggerEvent = function (evtName, args, global) {

    jQuery(this).trigger(evtName, args);

    if (global && this.globalEventNamespaces) {
        jQuery.each(this.globalEventNamespaces, function (i, glbEvtName) {
            jQuery(AJS).trigger(glbEvtName + "." + evtName, args);
        });
    }
    return this;
};

/**
 * Bindes event on instance
 * 
 * NOTE: can remove after support for 4.4.x is no longer required
 *
 * @param {String} evt - Event Name
 * @param {Array} args - Args to pass to event handlers
 */
Class.prototype.bind = function (evt, func) {
    jQuery(this).bind(evt, func);
    return this;
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
 * Gets a specific attribute from the trigger that opened the current dialog
 */
JIRA.Dialog.getAttrFromActiveTrigger = function (attr) {
    var currentDialog = JIRA.Dialog.current,
        $activeTrigger = currentDialog.$activeTrigger;

    if ($activeTrigger) {
        return $activeTrigger.attr(attr);
    }
}

/**
 * Builds a title for an issue dialog based on if there is a data-issuekey specified on the link or it is launched
 * from the issue navigator. If there is no issue key, it just returns the action.
 *
 * @param {String} action - Prefix to title e.g Edit Issue
 * @param {Boolean} hasIssueLink - Whether to add a link to the issue if it exists in the title
 * @return {String}
 */
JIRA.Dialog.getIssueActionTitle = function (action, hasIssueLink) {
    var issueKey = JIRA.Dialog.getActionIssueKey();

    if (!issueKey) {
        return action
    }

    if(hasIssueLink) {
        return action + " : " + "<a class='header-issue-key' href='" + contextPath + "/browse/" + issueKey + "'>" + issueKey + "</a>";
    } else {
        return action + " : " + issueKey;
    }
};

JIRA.Dialog.getActionIssueKey = function () {
    var issueKey = JIRA.Dialog.getAttrFromActiveTrigger("data-issueKey");
    if (!issueKey && JIRA.Issue) {
        issueKey = JIRA.Issue.getIssueKey()
    }
    if (!issueKey && JIRA.IssueNavigator) {
        issueKey = JIRA.IssueNavigator.getSelectedIssueKey();
    }
    return issueKey;
};

/**
 * If the child element is within an aui tab, will make that tab active
 * @param {jQuery} child
 */
JIRA.activateTabWithChild = function (child) {

    var tabId,
        $tabContainer,
        $tabTrigger,
        $tab = child.closest(".tabs-pane");

    if ($tab.length) {
        $tabContainer = $tab.closest(".aui-tabs")
        tabId = $tab.attr("id")
        $tabTrigger = $tabContainer.find("a[href='#" + tabId + "']");
        $tabTrigger.click();
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
            $field = form.find(":input[name='" + name + "']").last();

        if ($field.length === 1) {
            if (!$focusField) {
                $focusField = $field; // store first field with error so we can focus it at the end
            }
            $error = jQuery("<div class='error' />").attr("data-field", name).text(message);
            $group = $field.closest(".field-group, .group");
            $group.find(".error").remove(); // remove any pre-existing errors
            $group.append($error);
        }
    });

    JIRA.activateTabWithChild(form.find(".error:first"));

    if ($focusField) {
        $focusField.focus();
    }
};

/**
 * Extracts script tags from html fragments.
 *
 * jQuery will remove any script tags in the supplied html and
 * append them separately to the result (var $html). It does this to
 * allow ajax responses to contain script elements which are evaluated
 * when appended.
 *
 * Since we want to run the scripts in our own time we'll strip them
 * out and return them in their own object.
 *
 * @param html
 */
JIRA.extractScripts = function (html) {
    var $html = jQuery(html);

    return {
        html: $html.not("script"),
        scripts: $html.filter("script")
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
 * We have different class names and html pre 5.0. So we need to determine which markup/css to apply.
 * @return {Boolean}
 */
JIRA.useLegacyDecorator = function () {
    return !JIRA.Version.isGreaterThanOrEqualTo("5.0");
};

/**
 * Adds an error message to the form
 *
 * @param {jQuery} form
 * @param {String} error
 */
JIRA.applyErrorMessageToForm = function (form, error) {
    return JIRA.applyMessageToForm("error", form, error, false);
};

/**
 * Adds a success mesage to the form
 * @param {jQuery} form
 * @param {String} message
 */
JIRA.applySuccessMessageToForm = function (form, message) {
    return JIRA.applyMessageToForm("success", form, message, true);
};
