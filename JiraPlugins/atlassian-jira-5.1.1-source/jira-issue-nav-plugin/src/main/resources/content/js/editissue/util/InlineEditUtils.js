
JIRA.Events.INLINE_EDIT_STARTED = "inlineEditStarted";
JIRA.Events.BEFORE_INLINE_EDIT_CANCEL= "inlineEditCancelled";
JIRA.Events.INLINE_EDIT_BLURRED = "inlineEditBlurred";
JIRA.Events.INLINE_EDIT_FOCUSED = "inlineEditFocused";
JIRA.Events.INLINE_EDIT_REQUESTED = "inlineEditRequested";
JIRA.Events.PANEL_REFRESHED = "panelRefreshed";
JIRA.Events.LOCK_PANEL_REFRESHING = "lockPanelRefreshing";
JIRA.Events.UNLOCK_PANEL_REFRESHING = "unlockPanelRefreshing";
JIRA.Events.REFRESH_ISSUE_PAGE = "refreshIssuePage";

// Reasons for cancelling edit
JIRA.Issues.CANCEL_REASON = {
    escPressed: "escPressed"
};

AJS.$(function() {

    AJS.namespace("JIRA.Issues.InlineEdit");

    /**
     * The time required to wait between blur and focus
     */
    JIRA.Issues.InlineEdit.BLUR_FOCUS_TIMEOUT = 50;

    var BlurTriggers = JIRA.Issues.InlineEdit.BlurTriggers = {
        Default: function(fieldId, $container) {

            // Include save buttons
            $container = $container.nextAll(".save-options").andSelf();

            var focusables = ':input, a[href], [tabindex]';
            var timeout;
            var containerHasFocus = hasFocus($container);
            var eventsMap = {
                focus: function() {
                    if (!containerHasFocus) {
                        containerHasFocus = true;
                        JIRA.trigger(JIRA.Events.INLINE_EDIT_FOCUSED, [fieldId]);
                    }
                },
                blur: function() {
                    if (timeout) clearTimeout(timeout);
                    timeout = setTimeout(triggerIfBlurred, JIRA.Issues.InlineEdit.BLUR_FOCUS_TIMEOUT);
                }
            };

            // Make container focusable
            $container.attr('tabindex', 1)
            // Bind to container
                .bind(eventsMap)
            // Bind to focusable elements in container
                .delegate(focusables, eventsMap);

            function triggerIfBlurred() {
                if (!hasFocus($container)) {
                    containerHasFocus = false;
                    JIRA.trigger(JIRA.Events.INLINE_EDIT_BLURRED, [fieldId]);
                }
            }
        }
    };

    function hasFocus($container) {
        var activeElement = document.activeElement;
        return $container.find(activeElement).length > 0 || $container.filter(activeElement).length > 0;
    }

    JIRA.Issues.InlineEdit.BlurTriggerMapping = {
        system: {
            "summary": BlurTriggers.Default,
            "priority": BlurTriggers.Default,
            "issuetype": BlurTriggers.Default,
            "components": BlurTriggers.Default,
            "versions": BlurTriggers.Default,
            "fixVersions": BlurTriggers.Default,
            "assignee": BlurTriggers.Default,
            "reporter": BlurTriggers.Default,
            "environment": BlurTriggers.Default,
            "description": BlurTriggers.Default,
            "labels": BlurTriggers.Default,
            "duedate": BlurTriggers.Default
        },
        custom: {
            "cascadingselect": BlurTriggers.Default,
            "datepicker": BlurTriggers.Default,
            "datetime": BlurTriggers.Default,
            "float": BlurTriggers.Default,
            "grouppicker": BlurTriggers.Default,
            "labels": BlurTriggers.Default,
            "multicheckboxes": BlurTriggers.Default,
            "multigrouppicker": BlurTriggers.Default,
            "multiselect": BlurTriggers.Default,
            "multiuserpicker": BlurTriggers.Default,
            "multiversion": BlurTriggers.Default,
            "project": BlurTriggers.Default,
            "select": BlurTriggers.Default,
            "radiobuttons": BlurTriggers.Default,
            "textarea": BlurTriggers.Default,
            "textfield": BlurTriggers.Default,
            "url": BlurTriggers.Default,
            "userpicker": BlurTriggers.Default,
            "version": BlurTriggers.Default
        }
    };

    JIRA.bind(JIRA.Events.INLINE_EDIT_STARTED, function(e, fieldId, fieldType, $container) {
        var blurTrigger;
        if (fieldType) {
            blurTrigger = JIRA.Issues.InlineEdit.BlurTriggerMapping.custom[fieldType];
        } else {
            blurTrigger = JIRA.Issues.InlineEdit.BlurTriggerMapping.system[fieldId];
        }

        if (blurTrigger) {
            blurTrigger(fieldId, $container);
        }
    });
});
