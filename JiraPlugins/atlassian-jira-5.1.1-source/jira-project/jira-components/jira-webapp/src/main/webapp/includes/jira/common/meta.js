/**
 * JIRA.Meta represents meta-state about the current JIRA page - logged in user, etc.
 *
 * Add getters in here instead of duplicating a jQuery-attr get on some random element!
 */
(function($) {
    AJS.namespace('JIRA.Meta');

    /**
     * @return the currently logged-in user
     */
    JIRA.Meta.getLoggedInUser = function () {
        return {
            name: AJS.Meta.get('remote-user'),
            fullName: AJS.Meta.get('remote-user-fullname')
        };
    };

    /**
     * @return the Project key for the currently-viewed issue. Blank if the current page isn't for an issue or project.
     */
    JIRA.Meta.getProject = function () {
        return AJS.params.projectKey;
    };

    /**
     * @return the key of the currently-viewed issue. Blank if the current page isn't for a viewed issue.
     */
    JIRA.Meta.getIssueKey = function () {
        return AJS.Meta.get('issue-key');
    };

}(AJS.$));
