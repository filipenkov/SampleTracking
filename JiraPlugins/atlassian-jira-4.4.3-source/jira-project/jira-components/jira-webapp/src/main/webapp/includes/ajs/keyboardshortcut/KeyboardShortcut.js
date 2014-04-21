/**
 * AJS.whenIType -- factory for creating new AJS.KeyboardShortcut instances
 *
 * Example usage:
 *
 * AJS.whenIType("gh").or("gd").goTo("/secure/Dashboard.jspa");
 * AJS.whenIType("c").click("#create_link");
 *
 * @param {string} shortcut
 * @returns {AJS.KeyboardShortcut}
 */
AJS.whenIType = function(shortcut) {
    return new AJS.KeyboardShortcut(shortcut);
};

/**
 * @constructor AJS.KeyboardShortcut -- metaprogramming object for keyboard shortcut actions
 * @param {string} shortcut
 */
AJS.KeyboardShortcut = function(shortcut) {
    this._executer = null;
    this.shortcuts = [shortcut];
    this._bindShortcut(shortcut);
};

AJS.KeyboardShortcut.prototype._bindShortcut = function(shortcut) {
    if (typeof shortcut !== "string") {
        throw new TypeError("AJS.KeyboardShortcut expects string; received " + typeof shortcut);
    }
    if (/^(?:ctrl|alt|shift|meta)+/i.test(shortcut)) {
        throw new SyntaxError('AJS.KeyboardShortcut cannot bind the shortcut "' + shortcut + '" because it uses a modifier');
    }
    var self = this;
    jQuery(document).bind("shortcut", shortcut, function(event) {
        if (self._executer && !AJS.popup.current && !AJS.dropDown.current && !JIRA.Dialog.current) {
            if (AJS.InlineDialog.current) {
                // If there's an inline dialog shown, hide it, since you'll loose focus on the inline
                // dialog anyway as soon as keys are pressed. @see JRADEV-2323
                AJS.InlineDialog.current.hide();
            }
            self._executer(event);
            event.preventDefault();
        }
    });
};

AJS.KeyboardShortcut.prototype._addShortcutTitle = function(selector) {
    var elem = jQuery(selector);
    var title = elem.attr("title") || "";
    var typeStr = AJS.I18n.getText("keyboard.shortcuts.type");
    var thenStr = AJS.I18n.getText("keyboard.shortcuts.then");
    var orStr   = AJS.I18n.getText("keyboard.shortcuts.or");
    var shortcuts = jQuery.map(this.shortcuts, function(shortcut) {
        return " '" + shortcut.split("").join("' " + thenStr + " '") + "'";
    });
    title += " ( " + typeStr + shortcuts.join(" " + orStr + " ") + " )";
    elem.attr("title", title);
};

/**
 * @method moveToNextItem -- Scrolls to and adds "focused" class to the next item in the jQuery collection
 * @param selector
 */
AJS.KeyboardShortcut.prototype.moveToNextItem = function(selector) {
    this._executer = function () {
        var index,
            items = jQuery(selector),
            focusedElem = jQuery(selector + ".focused");

        if (!this._executer.blurHandler) {
            jQuery(document).one("keypress", function (e) {
                if (e.keyCode === 27 && focusedElem) {
                    focusedElem.removeClass("focused");
                }
            });
        }

        if (focusedElem.length === 0) {
            focusedElem = jQuery(selector).eq(0);
        } else {
            focusedElem.removeClass("focused");
            index = jQuery.inArray(focusedElem.get(0), items);
            if (index < items.length-1) {
                index = index +1;
                focusedElem = items.eq(index);
            } else {
                focusedElem.removeClass("focused");
                focusedElem = jQuery(selector).eq(0);
            }
        }
        if (focusedElem && focusedElem.length > 0) {
            focusedElem.addClass("focused");
            focusedElem.scrollIntoView();
            focusedElem.find("a:first").focus();
        }
    };
};

/**
 * @method moveToPrevItem -- Scrolls to and adds "focused" class to the previous item in the jQuery collection
 * @param selector
 */
AJS.KeyboardShortcut.prototype.moveToPrevItem = function(selector) {
    this._executer = function () {
        var index,
            items = jQuery(selector),
            focusedElem = jQuery(selector + ".focused");

        if (!this._executer.blurHandler) {
            jQuery(document).one("keypress", function (e) {
                if (e.keyCode === 27 && focusedElem) {
                    focusedElem.removeClass("focused");
                }
            });
        }

        if (focusedElem.length === 0) {
            focusedElem = jQuery(selector + ":last");
        } else {

            focusedElem.removeClass("focused");
            index = jQuery.inArray(focusedElem.get(0), items);
            if (index > 0) {
                index = index -1;
                focusedElem = items.eq(index);
            } else {
                focusedElem.removeClass("focused");
                focusedElem = jQuery(selector + ":last");
            }
        }
        if (focusedElem && focusedElem.length > 0) {
            focusedElem.addClass("focused");
            focusedElem.scrollIntoView();
            focusedElem.find("a:first").focus();
        }
    };
};

/**
 * @method click -- Clicks the element matched by the selector
 * @param {string} selector -- jQuery selector for element
 */
AJS.KeyboardShortcut.prototype.click = function(selector) {
    this._addShortcutTitle(selector);

    this._executer = function () {
        jQuery(selector).click();
    };
};

/**
 * @method goTo -- Navigates to specified location
 * @param {string} location -- URL
 */
AJS.KeyboardShortcut.prototype.goTo = function(location) {
    this._executer = function () {
        window.location.href = contextPath + location;
    };
};

/**
 * @method followLink -- navigates browser window to link href
 * @param {string} selector - jQuery selector for element
 */
AJS.KeyboardShortcut.prototype.followLink = function(selector) {
    this._addShortcutTitle(selector);
    this._executer = function () {
        var elem = jQuery(selector);
        if (elem.length > 0 &&
                (elem.attr("nodeName").toLowerCase() === "a" || elem.attr("nodeName").toLowerCase() === "link")) {
            elem.click();
            window.location.href = elem.attr("href");
        }
    };
};

/**
 * @method moveToAndClick -- Scrolls to element if out of view, then clicks it.
 * @param {string} selector - jQuery selector for element
 */
AJS.KeyboardShortcut.prototype.moveToAndClick = function(selector) {
    this._addShortcutTitle(selector);
    this._executer = function () {
        var elem = jQuery(selector);
        if (elem.length > 0) {
            elem.click();
            elem.scrollIntoView();
        }
    };
};

/**
 * @method moveToAndFocus -- Scrolls to element if out of view, then focuses it
 * @param {string} selector - jQuery selector for element
 */
AJS.KeyboardShortcut.prototype.moveToAndFocus = function(selector) {
    this._addShortcutTitle(selector);
    this._executer = function (e) {
        var $elem = jQuery(selector);
        if ($elem.length > 0) {
            $elem.focus();
            $elem.scrollIntoView();
            if ($elem.is(':input')) {
                e.preventDefault();
            }
        }
    };
};

/**
 * @method evaluate -- Executes the javascript provided by the shortcut plugin point on page load
 * @param {function} command - the function provided by the shortcut key plugin point
 */
AJS.KeyboardShortcut.prototype.evaluate = function(command) {
    if (typeof command !== "function") {
        command = new Function(command);
    }
    command.call(this);
};

/**
 * @method execute -- Executes the javascript provided by the shortcut plugin point when the shortcut is invoked
 * @param {function} func
 */
AJS.KeyboardShortcut.prototype.execute = function(func) {
    var self = this;
    this._executer = function() {
        if (typeof func !== "function") {
            func = new Function(func);
        }
        func.call(self);
    };
};

/**
 * @method or -- Bind another shortcut sequence
 * @param {string} shortcut - keys to bind
 * @return {AJS.KeyboardShortcut}
 */
AJS.KeyboardShortcut.prototype.or = function(shortcut) {
    this.shortcuts.push(shortcut);
    this._bindShortcut(shortcut);
    return this;
};
