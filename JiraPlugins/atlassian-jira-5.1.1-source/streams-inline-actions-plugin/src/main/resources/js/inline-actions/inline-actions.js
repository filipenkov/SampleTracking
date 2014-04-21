var ActivityStreams = ActivityStreams || {};

ActivityStreams.InlineActions = ActivityStreams.InlineActions || (function() {
    /**
     * Displays a status message.
     *
     * @method displayMessage
     * @param {Object} activityItem the .activity-item div
     * @param {String} message the message to display
     * @param {String} type the type of message to show
     * @param {Function} additionalEvents optional additional events to occur after delay
     */
    function displayMessage(activityItem, message, type, additionalEvents) {
        var container = activityItem.find('div.activity-item-action-status-container'),
            timeout = container.data('timeout');
        type = type || 'info';

        if (timeout) {
            clearTimeout(timeout);
        }
        container.bind('messageClose', function(e) {
            container.addClass('hidden').trigger('contentResize.streams');

            additionalEvents && additionalEvents();
        });
        AJS.messages[type](container, {
            body: message,
            closeable: false
        });
        container
            .removeClass('hidden')
            .trigger('contentResize.streams');

        timeout = setTimeout(function() {
            //remove the message and restore the gadget size
            container.find('div.aui-message').fadeOut(1000, function() {
                AJS.$(this).closeMessage();
            });
        }, 3000);
        container.data('timeout', timeout);
    }

    /**
     * Returns a proxied version of the url capable of being run on the current system.
     *
     * @method proxy
     * @param {String} url the url (absolute)
     * @param {String} feedItem the activity that the action is associated with (the original feed
     *   item parsed from the RSS data, not a DOM/JQuery object); this allows the function to
     *   determine whether the url needs to be proxied or can be used as-is; if omitted, the
     *   url will not be proxied
     * @return {String} the url in a form that the javascript can call
     */
    function proxy(url, feedItem) {
        if (feedItem && feedItem.generator && (feedItem.generator != ActivityStreamsGadgetBaseUrl)) {
            return ActivityStreamsGadgetBaseUrl + '/rest/activity-stream/1.0/url-proxy?url=' + url;
        }
        return url;
    }

    /**
     * Registers any necessary dom events
     * @method registerEvents
     */
    function registerEvents() {
        AJS.$('body')
            .bind('beginInlineAction', function(e) {
                var target = AJS.$(e.target),
                    actionsContainer = target.closest('div.activity-item').find('div.activity-item-actions'),
                    stopFn = ActivityStreams.showThrobber(actionsContainer);
                actionsContainer.data('stopFn', stopFn);
            })
            .bind('completeInlineAction', function(e) {
                var target = AJS.$(e.target),
                    actionsContainer = target.closest('div.activity-item').find('div.activity-item-actions'),
                    stopFn = actionsContainer.data('stopFn');
                if (stopFn) {
                    stopFn();
                } else {
                    actionsContainer.removeClass('loading');
                }
            });
    }

    registerEvents();

    return {
        // Expose displayMessage method for public use
        statusMessage: displayMessage,

        // Expose proxy method for public use
        proxy: proxy,

        // Expose registerEvents method for public use
        register: registerEvents
    };
})();