JIRA.Issues.IssueEventBus = JIRA.Issues.BaseModel.extend({

    properties: ["issueId"],

    namedEvents: [
        /*
         * Triggered when a user saves a field on the view issue screen. Should cause state to be saved
         * on the server.
         */
        "save",
        /*
         * Triggered when a save successfully returns from the server.
         */
        "saveSuccess",
        /**
         * Triggered when save request has been issued to server
         */
        "savingStarted",
        /*
         * Triggered when a save failed on the server.
         */
        "saveError",
        /*
         * Issue panels will fire this event when they've finished rendering after an update.
         */
        "panelRendered",
        /*
         * Fires when an issue view is closed or we go back to search.
         */
        "dismiss",
        /**
         * Triggered when the issue view needs to be refreshed.
         */
        "refreshIssue",
        /**
         * Triggered when the issue view has finished refreshing
         */
        "issueRefreshed",
        /**
         * Triggers views/models to update from a pre-existing DOM.
         */
        "updateFromDom",
        /**
         * Lets interested objects know to update status color
         */
        "updateStatusColor",
        /**
         * Triggered when a key is pressed whilst holding the tab key.
        */
        "quickEditKeyPressed",
        /**
         * Opens the focus shifter.
         */
        "openFocusShifter"
    ]

});
