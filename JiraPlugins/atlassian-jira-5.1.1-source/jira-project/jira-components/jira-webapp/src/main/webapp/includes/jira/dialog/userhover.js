/**
 * Inline dialogs for user infomation:
 *
 * @addon JIRA.userhover
 *   -- To reduce unneeded HTTP requests (JRADEV-2207) we need to circumvent AJS.InlineDialog's default
 *      behaviour. Thus, we use the noBind = true option, and manually control when to show/hide the
 *      dialog popups.
 *
 * @param {HTMLElement} context (optional)
 *   -- The scope in which to look for "a.user-hover" elements
 */
jQuery(document).delegate(".user-hover", {
    "mousemove": function() {
        JIRA.userhover.show(this);
    },
    "mouseleave": function() {
        JIRA.userhover.hide(this);
    },
    "click": function() {
        JIRA.userhover.hide(this, -1);
    }
});

/**
 * @deprecated -- This function is no longer needed since jQuery live events are bound at the document :root.
 */
JIRA.userhover = function() {};

JIRA.userhover.INLINE_DIALOG_OPTIONS = {
    urlPrefix: contextPath + "/secure/ViewUserHover!default.jspa?decorator=none&username=",
    showDelay: 400,
    closeOthers: false,
    noBind: true,
    hideCallback: function () {
        this.popup.remove(); // clean up so we don't have thousands of popups around in the dom
    }
};

JIRA.userhover.show = function(trigger) {
    clearTimeout(AJS.$.data(trigger, "AJS.InlineDialog.delayId") || 0);
    AJS.$.data(trigger, "AJS.InlineDialog.hasUserAttention", true);
    if (AJS.$.data(trigger, "AJS.InlineDialog") || JIRA.userhover._locked) {
        // This or another user hover dialog is already visible.
        return;
    }
    AJS.$.data(trigger, "AJS.InlineDialog.delayId", setTimeout(function() {
        // Don't show the dialog if the trigger has been detached or removed.
        if (AJS.$(trigger).closest("html").length === 0) {
            JIRA.userhover.hide(trigger);
            return;
        }

        AJS.$.data(trigger, "AJS.InlineDialog", AJS.InlineDialog(
            AJS.$(trigger),
            "user-hover-dialog-" + new Date().getTime(),
            function($contents, _, showPopup) {
                // Call the InlineDialog's url function with its expected arguments.
                JIRA.userhover._fetchDialogContents($contents, trigger, showPopup);
            },
            JIRA.userhover.INLINE_DIALOG_OPTIONS
        )).show();
    }, JIRA.userhover.INLINE_DIALOG_OPTIONS.showDelay));
};

JIRA.userhover.hide = function(trigger, showDelay) {
    clearTimeout(AJS.$.data(trigger, "AJS.InlineDialog.delayId") || 0);
    AJS.$.data(trigger, "AJS.InlineDialog.hasUserAttention", false);
    var dialog = AJS.$.data(trigger, "AJS.InlineDialog");
    if (dialog && !JIRA.userhover._locked) {
        if (typeof showDelay !== "number") {
            showDelay = JIRA.userhover.INLINE_DIALOG_OPTIONS.showDelay;
        }
        if (showDelay >= 0) {
            // Hide the dialog after the given delay period.
            AJS.$.data(trigger, "AJS.InlineDialog.delayId", setTimeout(function() {
                dialog.hide();
                AJS.$.data(trigger, "AJS.InlineDialog", null);
            }, showDelay));
        } else {
            // Hide the dialog immediately.
            dialog.hide();
            AJS.$.data(trigger, "AJS.InlineDialog", null);
        }
    }
};

JIRA.userhover._locked = false;

JIRA.userhover._fetchDialogContents = function($contents, trigger, showPopup) {
    AJS.$.get(JIRA.userhover.INLINE_DIALOG_OPTIONS.urlPrefix + trigger.getAttribute("rel"), function(html) {
        if (AJS.$.data(trigger, "AJS.InlineDialog.hasUserAttention")) {
            $contents.html(html);
            $contents.css("overflow", "visible");
            AJS.$(AJS.Dropdown.create({
                trigger: $contents.find(".aui-dd-link"),
                content: $contents.find(".aui-list")
            })).bind({
                "showLayer": function() {
                    JIRA.userhover._locked = true;
                },
                "hideLayer": function() {
                    JIRA.userhover._locked = false;
                    if (!AJS.$.data(trigger, "AJS.InlineDialog.hasUserAttention")) {
                        JIRA.userhover.hide(trigger);
                    }
                }
            });
            showPopup();
            // Wait for the popup's show animation to complete before binding event handlers
            // on $contents. This ensures the popup doesn't get in the way when the mouse
            // moves over it quickly.
            AJS.$.data(trigger, "AJS.InlineDialog.delayId", setTimeout(function() {
                $contents.bind({
                    "mousemove": function() {
                        JIRA.userhover.show(trigger);
                    },
                    "mouseleave": function() {
                        JIRA.userhover.hide(trigger);
                    }
                });
            }, JIRA.userhover.INLINE_DIALOG_OPTIONS.showDelay));
        }
    });
};

/** Preserve legacy namespace
    @deprecated jira.app.userhover */
AJS.namespace("jira.app.userhover", null, JIRA.userhover);
