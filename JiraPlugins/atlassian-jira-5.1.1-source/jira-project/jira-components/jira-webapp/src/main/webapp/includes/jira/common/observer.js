/**
 * A common place to aggregate events
 */

(function ($) {

    // If the events namespace is not defined, define it.
    JIRA.Events = JIRA.Events || {};

    JIRA.CONTENT_ADDED_REASON = {
        pageLoad: "pageLoad",
        inlineEditStarted: "inlineEditStarted",
        panelRefreshed: "panelRefreshed"
    };


    /**
     * Binds to many events to publish a single "newContentAdded" event. We use this to bind javascript to dynamically
     * inserted content
     */
    (function () {

        // Export the event name so listeners don't have to
        JIRA.Events.NEW_CONTENT_ADDED = "newContentAdded";
        JIRA.Events.NEW_PAGE_ADDED = "newPageAdded";


         // On dom ready
        $(function() {
            JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [$(document), JIRA.CONTENT_ADDED_REASON.pageLoad]);
            JIRA.trigger(JIRA.Events.NEW_PAGE_ADDED, [$(document)]);
        });

        // When dialog content refreshed
        $(document).bind("dialogContentReady", function(e, dialog) {
            JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [dialog.get$popupContent()]);
            JIRA.trigger(JIRA.Events.NEW_PAGE_ADDED, [dialog.get$popupContent()]);
        });

        // When arbitary fragment has been refreshed
        JIRA.bind("contentRefreshed", function(e, context) {
            JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [$(context)]);
        });

    })();


})(AJS.$);