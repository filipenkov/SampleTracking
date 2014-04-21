/**
 * Registers a "Vote" action against any feed items with an "issue" type.
 * 
 * Creates a link which adds the current user as a vote.
 */
(function() {
    
    /**
     * Adds the current user as a vote.
     * 
     * @method addIssueVote
     * @param {Event} e Event object
     */
    function addIssueVote(e) {
        var target = AJS.$(e.target),
            activityItem = target.closest('div.activity-item'),
            url,
            feedItem = e.data && e.data.feedItem
    
        if (feedItem) {
            url = feedItem.links['http://streams.atlassian.com/syndication/issue-vote'];
        } else {
            ActivityStreams.InlineActions.statusMessage(activityItem, ActivityStreams.i18n.get('streams.jira.action.issue.vote.failure.general'), 'error');
        }
        
        e.preventDefault();
        hideLink(activityItem);

        AJS.$.ajax({
            type : 'POST',
            url : ActivityStreams.InlineActions.proxy(url, feedItem),
            global: false,
            beforeSend: function() {
                target.trigger('beginInlineAction');
            },
            complete: function() {
                target.trigger('completeInlineAction');
            },
            success : function() {
                ActivityStreams.InlineActions.statusMessage(activityItem, ActivityStreams.i18n.get('streams.jira.action.issue.vote.success'), 'info');
            },
            error : function(request) {
                var msg;
                //check both request.status and request.rc for backwards compatibility
                if (request.status == 401 || request.rc == 401) {
                    msg = ActivityStreams.i18n.get('streams.jira.action.issue.vote.failure.authentication');
                } else if (request.status == 412 || request.rc == 412){
                    msg = ActivityStreams.i18n.get('streams.jira.action.issue.vote.failure.precondition.failed');
                } else if (request.status == 409 || request.rc == 409){
                    msg = ActivityStreams.i18n.get('streams.jira.action.issue.vote.failure.previously.voted');
                } else {
                    msg = ActivityStreams.i18n.get('streams.jira.action.issue.vote.failure.general');
                }
                ActivityStreams.InlineActions.statusMessage(activityItem, msg, 'error');
            }
        });
    }

    /**
     * Hide the action link, showing the non-hyperlinked label instead.
     * 
     * @method hideLink
     * @param {Object} activityItem the .activity-item div
     */
    function hideLink(activityItem) {
        activityItem.find('a.activity-item-issue-vote-link').addClass('hidden');
        activityItem.find('span.activity-item-issue-vote-label').removeClass('hidden');
    }

    /**
     * Builds a link to trigger the action.
     * 
     * @method buildLink
     * @param {Object} feedItem Object representing the activity item
     * @return {HTMLElement}
     */
    function buildLink(feedItem) {
        //if no issue-vote link exists in the feed item, do not bind the entry to a trigger handler
        if (!feedItem.links['http://streams.atlassian.com/syndication/issue-vote']) {
            return;
        } 
        
        var link = AJS.$('<a href="#" class="activity-item-issue-vote-link"></a>')
                .text(ActivityStreams.i18n.get('streams.jira.action.issue.vote.title'))
                .bind('click', {feedItem: feedItem}, addIssueVote),
            label = AJS.$('<span class="activity-item-issue-vote-label hidden"></span>')
                .text(ActivityStreams.i18n.get('streams.jira.action.issue.vote.title'));
        
        return link.add(label);
    }

    // Registers the action for any issues in the feed
    ActivityStreams.registerAction('issue file', buildLink, 9);
})();
