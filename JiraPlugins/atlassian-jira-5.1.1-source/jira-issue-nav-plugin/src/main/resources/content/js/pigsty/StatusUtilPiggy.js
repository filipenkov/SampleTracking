
AJS.namespace("JIRA.Issues.StatusUtil");

JIRA.Issues.StatusUtil = {
    /**
     * @return {string} the css class representing the status for this issue.
     */
    getClass: function(statusObject) {
        // TODO css styles will have to be generated from ids from colours users must now define instead of icons
        return "status-" + statusObject.name.toLowerCase().replace(/\s/g, "");
    }
};
