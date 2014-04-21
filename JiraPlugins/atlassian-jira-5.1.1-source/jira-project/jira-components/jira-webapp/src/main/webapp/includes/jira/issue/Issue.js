/**
 * @namespace JIRA.Issue
 */
AJS.namespace("JIRA.Issue");

(function () {

    /**
     * Represents the View Issue page.  This class should be used to get the current issue key
     * and any other issue centric information!
     */
    var $keyVal;

    //private function to cache the key value.
    function getKeyVal() {
        if(!$keyVal) {
            $keyVal = jQuery("#key-val");
        }
        return $keyVal;
    }

    /**
     * @return {jQuery}
     */
    JIRA.Issue.getStalker = function () {
        return jQuery("#stalker");
    };

    /**
     * Gets subtask contents
     * @param jQuery
     */
    JIRA.Issue.getSubtaskContent = function () {
        return JIRA.Issue.getSubtaskModule().find(".mod-content");
    };

    /**
     * Gets subtask module
     */
    JIRA.Issue.getSubtaskModule = function () {
        return jQuery("#view-subtasks");
    };

    /**
     * Reloads View Issue screen
     */
    JIRA.Issue.reload = function () {
        window.location.reload();
    };

    /**
     * Goes back to the server to get updates content, if there is any.
     *
     * @return jQuery.promise
     */
    JIRA.Issue.refreshSubtasks = function () {

        var deferred = jQuery.Deferred(),
                $subtasks = JIRA.Issue.getSubtaskContent();

        if ($subtasks.length === 0) {
            AJS.reloadViaWindowLocation(window.location.href + "#view-subtasks");
            return deferred.promise();
        } else {
            return jQuery.ajax({
                url: contextPath + "/secure/ViewSubtasks.jspa?id=" + JIRA.Issue.getIssueId(),
                success: function (html) {
                    $subtasks.replaceWith(html);
                    JIRA.trigger("Issue.subtasksRefreshed", [JIRA.Issue.getSubtaskContent()]);
                }
            });
        }
    };

    /**
     * Highlights specified issues
     *
     * @param issues
     */
    JIRA.Issue.highlightSubtasks = function (issues) {
        jQuery.each(issues, function (i, issue) {
            jQuery(".issuerow[data-issuekey='" + issue.issueKey + "']").fadeInBackground();
        });
    };

    /**
     * Returns the issue id of the current issue being viewed.
     *
     * @method getIssueId
     * @return {String} the issue id or undefined if none can be found.
     */
    JIRA.Issue.getIssueId = function() {
        var $keyVal = getKeyVal();
        if($keyVal.length !== 0) {
            return $keyVal.attr("rel");
        }
        return undefined;
    };

    /**
     * Returns the issue key of the current issue being viewed.
     *
     * @method getIssueKey
     * @return {String} the issue key or undefined if none can be found.
     */
    JIRA.Issue.getIssueKey = function() {
        var $keyVal = getKeyVal();
        if($keyVal.length !== 0) {
            return $keyVal.text();
        }
        return undefined;
    };

    /**
     * Gets I18N message for a created issue.
     *
     * @method issueCreatedMessage
     * @param issue issue to get message for
     * @param isSubtask whether the created issue is a subtask
     * @return {String} the issue created message according to the given data
     */
    JIRA.Issue.issueCreatedMessage = function(issue, isSubtask) {
        var issueText = isSubtask ? AJS.I18n.getText("common.words.subtask") : AJS.I18n.getText("common.words.issue");
        var link = '<a class="issue-created-key" href="' + AJS.contextPath() + '/browse/' + issue.issueKey + '">'
            + issue.issueKey + ' - ' + AJS.escapeHTML(issue.summary) + '</a>';
        return AJS.I18n.getText('createissue.issuecreated', issueText, link);
    }
})(AJS.$);

/** Preserve legacy namespace
 @deprecated jira.app.issue */
AJS.namespace("jira.app.issue", null, JIRA.Issue);
