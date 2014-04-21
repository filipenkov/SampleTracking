/**
 * Registers a "Watch issue" action against any feed items with an "issue" type.
 * 
 * Creates a link which adds the current user as an issue watcher.
 */
(function() {
    
    /**
     * Adds the current user as an issue watcher.
     * 
     * @method addIssueWatcher
     * @param {Event} e Event object
     */
    function addIssueWatcher(e) {
        var target = AJS.$(e.target),
            activityItem = target.closest('div.activity-item'),
            url,
            feedItem = e.data && e.data.feedItem;
    
        if (feedItem) {
            url = feedItem.links['http://streams.atlassian.com/syndication/watch'];
        } else {
            ActivityStreams.InlineActions.statusMessage(activityItem, ActivityStreams.i18n.get('streams.jira.action.issue.watch.failure.general'), 'error');
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
                ActivityStreams.InlineActions.statusMessage(activityItem, ActivityStreams.i18n.get('streams.jira.action.issue.watch.success'), 'info');
            },
            error : function(request) {
                var msg;
                //check both request.status and request.rc for backwards compatibility
                if (request.status == 401 || request.rc == 401) {
                    msg = ActivityStreams.i18n.get('streams.jira.action.issue.watch.failure.authentication');
                } else if (request.status == 412 || request.rc == 412){
                    msg = ActivityStreams.i18n.get('streams.jira.action.issue.watch.failure.precondition.failed');
                } else if (request.status == 409 || request.rc == 409){
                    msg = ActivityStreams.i18n.get('streams.jira.action.issue.watch.failure.previously.watched');
                } else {
                    msg = ActivityStreams.i18n.get('streams.jira.action.issue.watch.failure.general');
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
        activityItem.find('a.activity-item-issue-watch-link').addClass('hidden');
        activityItem.find('span.activity-item-issue-watch-label').removeClass('hidden');
    }

    /**
     * Builds a link to trigger the action.
     * 
     * @method buildLink
     * @param {Object} feedItem Object representing the activity item
     * @return {HTMLElement}
     */
    function buildLink(feedItem) {
        //if no issue-watch link exists in the feed item, do not bind the entry to a trigger handler
        if (!feedItem.links['http://streams.atlassian.com/syndication/watch']) {
            return;
        } 
        
        var link = AJS.$('<a href="#" class="activity-item-issue-watch-link"></a>')
                .text(ActivityStreams.i18n.get('streams.jira.action.issue.watch.title'))
                .bind('click', {feedItem: feedItem}, addIssueWatcher),
            label = AJS.$('<span class="activity-item-issue-watch-label hidden"></span>')
                .text(ActivityStreams.i18n.get('streams.jira.action.issue.watch.title'));
        
        return link.add(label);
    }

    // Registers the action for any issues in the feed
    ActivityStreams.registerAction('issue comment file', buildLink, 10);
})();
