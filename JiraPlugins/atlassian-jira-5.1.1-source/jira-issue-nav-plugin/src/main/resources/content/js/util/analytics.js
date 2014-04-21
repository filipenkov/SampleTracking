(function() {

    AJS.namespace("JIRA.Issues.Analytics");

    JIRA.Issues.Analytics = _.clone(Backbone.Events);

    JIRA.Issues.Analytics.convertToLogEvent = function(name, parameters) {
        var logMsg = "***** Analytics log [" + name + "]";
        if(parameters) {
            logMsg += "[" + JSON.stringify(parameters) + "]";
        }
        AJS.log(logMsg);
        if (AJS.EventQueue) {
            // Register an analytics object for this event.
            AJS.EventQueue.push({
                name: name,
                properties: parameters || {}
            });
        }
    };

    //track if the user runs a search in the browser with Ctrl+f/Cmd+f.
    AJS.$(document).keydown(function(e) {
        if(e.keyCode === 70 && (e.metaKey || e.ctrlKey)) {
            JIRA.Issues.Analytics.trigger("kickass.inbrowsersearch");
        }
    });

    var logEvent = function(eventName) {
        JIRA.Issues.Analytics.on(eventName, function(props) {JIRA.Issues.Analytics.convertToLogEvent(eventName, props)});
    };

    /*
     * ##Events
     *
     * *IMPORTANT*: Only have one level of dots under kickass or they wont show up in graphite!
     */

    /* Any client side search triggerst this (including details of the search */
    logEvent("kickass.search");
    /* Triggered when the user switches back to keyword search */
    logEvent("kickass.switchtokeyword");
    /* Triggered when the user moves to JQL search */
    logEvent("kickass.switchtoadvanced");
    /* Triggered whenever infinity scroll has to fetch more issues */
    logEvent("kickass.scroll");
    /* Triggered when the user hits return to search */
    logEvent("kickass.returntosearch");
    /* Records a saved search got executed */
    logEvent("kickass.searchWithFilter");
    /* Records if an inline edit action took place. Also includes which fields were edited */
    logEvent("kickass.editFields");
    /* If more than one field was edited on a view issue page (Note: the count resets only when a new issue is loaded, *not*
    * when the user clicked 'Save') */
    logEvent("kickass.editMultipleFields");
    /* Records the duration the user spends in edit mode until they hit save */
    logEvent("kickass.editClientDuration");
    /* Records the duration the user spends in edit mode until they hit cancel */
    logEvent("kickass.editClientCancelledDuration");
    /* Time it takes from hitting save to the view issue page having rerenderd on the client */
    logEvent("kickass.editSaveonserverDuration");
    /* Triggered if a user uses in browser search to find something (CTRL + F) */
    logEvent("kickass.inbrowsersearch");
    /* Time it takes to load the view issue page */
    logEvent("kickass.issueLoadDuration");
    /* Time it takes save an issue, from user click to full panel reblat */
    logEvent("kickass.issueTotalSaveDuration");
    /* When the user opens the focus shifter. */
    logEvent("kickass.focusshifteropened");
})();
