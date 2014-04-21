/**
 * Registers a "Comment" action against any feed items with a "replyTo" link.
 *
 * Creates a link which in turn toggles a comment form that can be submitted to comment on the specified entry.
 */
(function() {

    /**
     * "Slides" the comment form in and out of view
     * 
     * @method toggleCommentForm
     * @param {Event} e Event object
     */
    function toggleCommentForm(e) {
        var activityItem = AJS.$(e.target).closest('div.activity-item'),
            form = activityItem.find('form.activity-item-comment-form');

        e.preventDefault();

        if (!form.length) {
            form = buildCommentForm(e, activityItem);
            form.appendTo(activityItem);
        }
        if (form.is(':visible')) {
            form.slideUp(function() {
                form.trigger('contentResize.streams').removeClass('ready');
            });
        } else {
            form.slideDown(function() {
                form.find('textarea').focus();
                // remove 'display: block' set by slideDown fn so that we can hide the form with css
                form.css({display: ''});
                form.trigger('contentResize.streams').addClass('ready');
            });
        }
    }

    /**
     * Returns html for the inline add comment form
     * 
     * @method buildCommentForm
     * @param {Event} e Event object
     * @param {Object} activityItem the .activity-item div
     * @return {HTMLElement}
     */
    function buildCommentForm(e, activityItem) {
        var form,
            fieldset,
            submit;
        
        if (!e.data || !e.data.feedItem) {
            ActivityStreams.InlineActions.statusMessage(activityItem, ActivityStreams.i18n.get('streams.comment.action.error.invalid.comment'), 'error');
            return null;
        }
        
        form = AJS.$('<form class="activity-item-comment-form" method="post" action=""></form>').css({display: 'none'});

        fieldset = AJS.$('<fieldset></fieldset>')
            .appendTo(form);
        AJS.$('<input type="hidden" name="replyTo">')
            .val(e.data.feedItem.links['http://streams.atlassian.com/syndication/reply-to'])
            .appendTo(fieldset);

        // The name of this hidden XSRF token field must correspond with the value of CROSS_PRODUCT_TOKEN_PARAM
        // in the com.atlassian.streams.internal.servlet.XsrfAwareRequest class (in the Streams Aggregator plugin)
        AJS.$('<input type="hidden" name="xsrfToken">')
                .val(window.top.AJS.$("#atlassian-token").attr("content"))
                .appendTo(fieldset);

        AJS.$('<textarea cols="40" rows="6" name="comment"></textarea>')
            .appendTo(fieldset);

        submit = AJS.$('<div class="submit"></div>')
            .appendTo(form);
        AJS.$('<button name="submit" type="submit"></button>')
            .text(ActivityStreams.i18n.get('streams.comment.action.add'))
            .appendTo(submit);
        AJS.$('<a href="#" class="streams-cancel"></a>')
            .text(ActivityStreams.i18n.get('streams.comment.action.cancel'))
            .click(toggleCommentForm)
            .appendTo(submit);

        form.submit(function(e) {
            e.preventDefault();
            var form = AJS.$(e.target),
                commentBody = AJS.$.trim(form.find("textarea").val());
            if (commentBody.length === 0) {
                ActivityStreams.InlineActions.statusMessage(activityItem, ActivityStreams.i18n.get('streams.comment.action.error.add.comment'), 'error');
                return;
            }
            form.find("button").attr("disabled", "true");

            AJS.$.ajax({
                type : 'POST',
                url : ActivityStreams.getBaseUrl() + '/plugins/servlet/streamscomments',
                data : form.serialize(),
                dataType : 'json',
                global: false,
                beforeSend: function() {
                    form.trigger('beginInlineAction');
                },
                complete: function() {
                    form.trigger('completeInlineAction');
                },
                success : function(data, textStatus, xhr) {
                    toggleCommentForm(e);
                    form.find("button").removeAttr("disabled");
                    form.find("textarea").val("");

                    //302 is not considered an error, but JIRA is using it to return blocked url errors https://sdog.jira.com/browse/JSTDEV-670 and https://studio.atlassian.com/browse/STRM-1982
                    if(xhr.status == 302) {
                        ActivityStreams.InlineActions.statusMessage(activityItem, ActivityStreams.i18n.get('streams.comment.action.error.invalid.comment'), 'error');
                    }
                    else {
                        ActivityStreams.InlineActions.statusMessage(activityItem, ActivityStreams.i18n.get('streams.comment.action.success.add.comment'), 'info');
                    }
                },
                error : function(response) {
                    var data = (response && response.data) || response,
                        subcode = (data && data.responseText && AJS.json.parse(data.responseText).subCode) || 'streams.comment.action.error.invalid.comment';
                    toggleCommentForm(e);
                    form.find('button').removeAttr('disabled');
                    ActivityStreams.InlineActions.statusMessage(activityItem, ActivityStreams.i18n.get(subcode), 'error');
                }
            });
        });

        return form;
    }

    /**
     * Builds an anchor element that toggles the comment form if feedItem has a replyTo link
     * 
     * @method buildTrigger
     * @param {String} label The display text for the link
     * @param {Object} feedItem Object representing the activity item
     * @return {HTMLElement}
     */
    function buildTrigger(label, feedItem) {
        //if no reply to link exists in the feed item, do not bind the entry to a comment handler
        if (!feedItem.links['http://streams.atlassian.com/syndication/reply-to']) {
            return null;
        } 

        return AJS.$('<a href="#" class="activity-item-comment-link"></a>')
            .text(label)
            .bind('click', {feedItem: feedItem}, toggleCommentForm);
    }

    /**
     * Builds a "Comment" link that toggles the comment form
     *
     * @method buildCommentLink
     * @param {Object} feedItem Object representing the activity item
     * @return {HTMLElement}
     */
    function buildCommentLink(feedItem) {
        var label = ActivityStreams.i18n.get('streams.comment.action.comment');
        if (feedItem.application !== 'com.atlassian.jira' && feedItem.type === 'comment') {
            label = ActivityStreams.i18n.get('streams.comment.action.reply');
        }
        return buildTrigger(label, feedItem);
    }

    // Registers the comment action for various types in the feed
    ActivityStreams.registerAction('article comment page issue file job', buildCommentLink, 1);
})();
