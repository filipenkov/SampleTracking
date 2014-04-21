AJS.namespace("JIRA.Issues.IssueRowModel");

JIRA.Issues.IssueRowModel = JIRA.Issues.BaseModel.extend({

    namedEvents: ["selected"],

    /**
     * entity: issue json retrieved from server
     */
    properties: ["id","entity"],

    initialize: function() {
        _.bindAll(this);
    },

    /**
     * @return {string} the css class representing the status for this issue.
     */
    getStatusClass: function() {
        return JIRA.Issues.StatusUtil.getClass(this.getEntity().fields.status);
    }
});