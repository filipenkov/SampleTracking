(function($) {

    var viewIssueController;

    JIRA.Issues.overrideIssueApi = function(_viewIssueController) {
        viewIssueController = _viewIssueController;
    };

    /**
     * The JIRA.Issue api no longer works with kickass since it assumes that only one issue will
     * ever be displayed on page load and so it will only ever return the id of the first issue in the
     * result set.
     *
     * Fixing this so that the methods below use the viewIssueController as their source of truth.
     */

    JIRA.Issue.getIssueId = function() {
        var model = viewIssueController.getSelectedIssueModel();
        if (model) {
            return model.getEntity().id;
        }
    };


    JIRA.Issue.getIssueKey = function() {
        var model = viewIssueController.getSelectedIssueModel();
        if (model) {
            return model.getEntity().key;
        }
    };

    AJS.$(function() {
        //TODO: we should eventually do this properly.  Have a new toggle model in backbone backed by localstorage
        // that's passed to the issuepanelview.  The panelview can then already render it collapsed.
        new JIRA.ToggleBlock({
            blockSelector: ".toggle-wrap",
            triggerSelector: ".mod-header h3",
            storageCollectionName: "block-states",
            originalTargetIgnoreSelector: "a"
        });
    });
})(AJS.$);