/**
 * @namespace JIRA.Issue
 */
AJS.namespace("JIRA.Issue");

(function() {
    /**
     * This will add a click vent to the the assigned to me link of the assignee field so that it selects
     * the current user in the assignee select box
     *
     * @param {jQuery | HTMLElement} context - the context used for selection
     */
    JIRA.Issue.wireAssignToMeLink = function(context) {
        jQuery('#assign-to-me-trigger', context).click(function (e) {
            e.preventDefault();
            var assigneeId = getHashedLinkTarget(jQuery(this).attr('href'));
            var currentUserOption = jQuery(assigneeId, context).find('.current-user');
            var val = currentUserOption.val();
            jQuery(assigneeId, context).val(val).change(); // cause a change event as well as set it
        });
    };

    /**
     * On IE7 it takes a href like '#assignee' and returns http:/x.x.x./x/x#assignee.  Why. why
     */
    function getHashedLinkTarget(url) {
        var hashIndex = url.indexOf('#');
        if (hashIndex != -1) {
            return url.substring(hashIndex);
        } else {
            return url;
        }
    }

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
})();

/** Preserve legacy namespace
    @deprecated jira.app.issue */
AJS.namespace("jira.app.issue", null, JIRA.Issue);
