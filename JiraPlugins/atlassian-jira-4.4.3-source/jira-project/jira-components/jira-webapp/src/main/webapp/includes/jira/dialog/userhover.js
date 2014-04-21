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
JIRA.userhover = function(context) {
    AJS.$(".user-hover", context).bind({
        "mouseenter": function() {
            JIRA.userhover.show(this);
        },
        "mouseleave": function() {
            JIRA.userhover.hide(this);
        },
        "click": function() {
            JIRA.userhover.hide(this, -1);
        }
    });
};

JIRA.userhover.INLINE_DIALOG_OPTIONS = {
    urlPrefix: contextPath + "/secure/ViewUserHover!default.jspa?decorator=none&username=",
    showDelay: 400,
    closeOthers: false,
    noBind: true
};

JIRA.userhover.show = function(trigger) {
    clearTimeout(AJS.$.data(trigger, "AJS.InlineDialog.delayId") || 0);
    AJS.$.data(trigger, "AJS.InlineDialog.hasUserAttention", true);
    if (AJS.$.data(trigger, "AJS.InlineDialog") || JIRA.userhover._locked) {
        // This or another user hover dialog is already visible.
        return;
    }
    AJS.$.data(trigger, "AJS.InlineDialog.delayId", setTimeout(function() {
        AJS.$.data(trigger, "AJS.InlineDialog", AJS.InlineDialog(
            AJS.$(trigger),
            "user-hover-dialog-" + new Date().getTime(),
            function($contents, _, showPopup) {
                // Call the InlineDialog's url function with its expected arguments.
                JIRA.userhover._fetchDialogContents($contents, trigger, showPopup);
                // Handle mouse events on popup similarly to trigger.
                $contents.bind({
                    "mouseenter": function() {
                        JIRA.userhover.show(trigger);
                    },
                    "mouseleave": function() {
                        JIRA.userhover.hide(trigger);
                    }
                });
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
        }
    });
};

AJS.$(function() {
    // TODO: We can easily refactor this to use live events, and therefore remove
    // TODO: the JIRA.userhover function completely. Is this a worthwhile change?
    // TODO: Will it break legacy code and/or plugins?
    JIRA.userhover(document.body);
});

/** Preserve legacy namespace
    @deprecated jira.app.userhover */
AJS.namespace("jira.app.userhover", null, JIRA.userhover);
