

AJS.$(function() {

    var creator = new JIRA.Issues.IssueNavCreator().create(AJS.$(document));

    Backbone.history.start({
        pushState: true,
        root: contextPath  + "/issues/"
    });



    // Issues Public API
    AJS.namespace("JIRA.Issues.Api");

    /**
     * Moves to the next issue
     * @public
     */
    JIRA.Issues.Api.nextIssue = creator.searchPageModel.next;

    /**
     * Moves to the prev issue
     * @public
     */
    JIRA.Issues.Api.prevIssue = creator.searchPageModel.prev;

    /**
     * Switches to detailed view. Also known as selected mode.
     * @public
     */
    JIRA.Issues.Api.switchToDetailedView = creator.searchPageModel.switchToDetailedView;

    /**
     * Switches to detailed view. Also known as selected mode.
     * @public
     */
    JIRA.Issues.Api.switchToSearchView = creator.searchPageModel.backToSearch;

    JIRA.Issues.Api.hasSavesInProgress = function () {
        return creator.saveInProgressManager.hasSavesInProgress();
    };

    /**
     * Override JIRA dialogs
     */
    JIRA.Issues.overrideIssueDialogs(creator.viewIssueLoader);

    /**
     * Override JIRA JIRA.Issue api
     */
    JIRA.Issues.overrideIssueApi(creator.viewIssueLoader);
});
