AJS.namespace("JIRA.Issues.IssueViewModel");

/**
 * Model for the view issue screen
 */
JIRA.Issues.IssueViewModel = JIRA.Issues.BaseModel.extend({

    namedEvents: [
        /**
         * Fired when a user hits the close icon on the view issue page */
        "returnToSearch",
        /**
         * Triggered when the view issue model is updated with data from server
         */
        "updated"
    ],

    /**
     * entity: issue json retrieved from server
     * panels: JIRA.Issues.IssuePanelsModel representing web panels for this issue
     */
    properties: [
        /**
         * Issue id
         * @type Number
         */
        "id",
        /**
         * Contains information about the issue, including summary and issue operations
         * @type Object
         */
        "entity",
        /**
         * Issue panels
         * @type JIRA.Issues.IssuePanelsModel
         */
        "panels",
        /**
         * @type JIRA.Issues.IssueEventBus
         */
        "issueEventBus",
        /**
         * @type {Boolean}
         */
        "statusColorSupport",
        /**
         * @type {Boolean}
         */
        "standalone"
    ],

    /**
     * @constructor
     */
    initialize: function() {
        _.bindAll(this);
        this.set({panels: new JIRA.Issues.IssuePanelsModel()});
        if (this.getStatusColorSupport()) {
            this.on("change", _.bind(function () {
                this.getIssueEventBus().triggerUpdateStatusColor();
            }, this));
        }
    },

    /**
     * Is standalone view issue page
     * @return {Boolean}
     */
    isStandalone: function () {
        return jQuery("body").hasClass("navigator-issue-only");
    },

    /**
     * Updates entity and panels with new data
     *
     * @param {Object} data
     * @param {Object} props
     * ... {Array<String>} fieldsSaved - The update may come as the result of a save. This array includes the ids of any fields that may have been saved before hand.
     * ... {Array<String>} fieldsInProgress - Array of fields that are still in edit mode or still saving.
     * ... {Boolean}initialize - parameter indicating if it is the first time the update has been called.

     */
    update:function(data, props) {
        this.setEntity(data.issue);
        this.getPanels().update(data.panels, props);
        this.triggerUpdated(props);
    },

    /**
     * Returns to search
     */
    returnToSearch: function () {
        this.triggerReturnToSearch();
    },

    /**
     * Gets the css class representing the status for this issue.
     * @return {string}
     */
    getStatusClass: function() {
        return JIRA.Issues.StatusUtil.getClass(this.getEntity().status);
    },

    /**
     * Let everyone know we are leaving the issue. This results in fields being edited, saved.
     */
    dismiss: function() {
        this.getIssueEventBus().triggerDismiss();
    }
});
