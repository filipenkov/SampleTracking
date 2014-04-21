var upm = upm || {};

(function() {
    var notifications,
        upmContentType = 'application/vnd.atl.plugins+json',
        notificationsContainer,
        minMsBeforeFadeIn = 1000,
        hasOperationMenu = false,
        hasActionButtons = false,
        hasAdminPageHeadings = false,
        hasProjectConfigHeading = false;

        // These vars are populated by NotificationWebResourceTransformer when this web resource is loaded
        upm.productId;
        upm.productVersion;
        upm.pluginNotificationsTitle;
        upm.noNotificationsText;
        upm.notificationsUrl;

        // Save some info about our product for other upm files to use
        upm.productVersion = parseFloat(upm.productVersion);
        upm.isJira = (upm.productId == 'jira');
        upm.isBamboo = (upm.productId == 'bamboo');
        upm.isConfluence = (upm.productId == 'confluence');
        upm.isFecru = (upm.productId == 'fisheye' || upm.productId == 'crucible');

    /**
     * Dismisses the plugin update notification and makes call to REST resource with dismissed banner JSON
     * @method dismiss
     */
    function dismiss() {
        var notification,
            notificationGroup,
            postUrl,
            notificationElements;

        if (notificationsContainer.hasClass('new-notifications')) {
            notificationElements = AJS.$('div.upm-notification', '#upm-notifications');

            for (var i = 0, len = notificationElements.length; i < len; i++) {
                notification = AJS.$(notificationElements[i]);
                // Dismiss each undismissed notification (requires one call per group for now)
                if (notification.hasClass('upm-notification-new')) {
                    notificationGroup = notifications.notificationGroups[i];
                    notificationGroup.dismissed = true;

                    // If no post-notification link, this message isn't dismissable
                    postUrl = notificationGroup.links['post-notifications'];
                    if (postUrl) {
                        AJS.$.ajax({
                            type: 'POST',
                            url: postUrl,
                            dataType: 'json',
                            data: upm.json.stringify(notificationGroup),
                            contentType: upmContentType
                        });
                    }
                }
            }

            notificationsContainer.removeClass('new-notifications');
        }
    }

    /**
     * Finds and returns an element to append notifications html to, based on the host application
     * @method getMessageContainer
     * @return {HTMLElement} Element to append notifications to, if any
     */
    function getMessageContainer() {
        var $container;

        if (upm.isJira) {
            if (upm.productVersion < 4.4) {
                $container = AJS.$('#header-bottom');
            } else {
                // UPM-1803 look for the return to project button, and put it next to the button if found
                if (($container = AJS.$('#proj-config-return-link')).length) {
                    hasActionButtons = true;

                // Then try the operations menu page heading (like when viewing a project as admin, there will be action
                // links to avoid)
                } else if (($container = AJS.$('#project-config-header ul.operation-menu')).length) {
                    hasOperationMenu = true;

                // or try project config header without action links
                } else if (($container = AJS.$('#project-config-header')).length) {
                    hasProjectConfigHeading = true;

                // Or just use the normal page heading
                } else if (($container = AJS.$('#admin-page-heading').parent()).length) {
                    hasAdminPageHeadings = true;

                // If the title is gone for some reason, put it in the upm container
                } else {
                    $container = AJS.$('#upm-container').find('h2:first');
                }
            }
        } else if (upm.isConfluence) {
            // confluence has a page for just such an occasion, so only show the banner there and on the upm page
            if (window.location.href.indexOf('console.action') != -1
                    || window.location.href.indexOf('editconsolemessages.action') != -1
                    || upm.isUpm) {
                $container = AJS.$('<div></div>').prependTo('td.pagebody');
            }
        } else if (upm.isBamboo) {
            if (upm.productVersion < 3.4) {
                $container = AJS.$('#menu');
            // UPM-1738 Only show notifications on UPM pages - other 3.4 admin pages have no good top right area to jam it in
            } else if (upm.isUpm) {
                $container = upm.$container.find('h2:first');
            }
        } else if (upm.isFecru) {
            $container = AJS.$('#header-admin');
        }

        return $container;
    }

    /**
     * Creates and returns an element containing the notification text
     * @method buildNotificationMessage
     * @param {String} message Message text
     * @param {String} target (optional) Url to link off to
     * @param {Integer} count the count of notifications represented in this message
     * @return {HTMLElement} Notification message html
     */
    function buildNotificationMessage(message, target) {
        var span = AJS.$('<span class="upm-notification-text"></span>'),
            anchor;
        if (!target) {
            span.text(message);
        } else {
            span.html(message);
            span.find('a').attr('href', target);
        }
        return span;
    }

    /**
     * Creates and returns an element containing the notification count
     * @method buildNotificationsTrigger
     * @param {Number} count Number of displayed notifications
     * @return {HTMLElement} Notification count element
     */
    function buildNotificationsTrigger(count) {
        var trigger = AJS.$('<a id="upm-notifications-trigger" class="aui-dd-trigger" href="#"><span id="upm-notifications-icon"></span></a>')
                        .attr('title', upm.pluginNotificationsTitle);

        trigger.bind('click', dismiss);

        count = parseInt(count, 10);
        if (count === NaN) {
            count = 0;
        }

        AJS.$('<span id="upm-notifications-count"></span>')
            .text(count)
            .appendTo(trigger);

        return trigger;
    }

    /**
     * Creates and returns an element containing the specifics for one notification group
     * @method buildNotificationElement
     * @param {Object} notificationGroup Details of the notification group
     * @return {HTMLElement} Notification element
     */
    function buildNotificationElement(notificationGroup) {
        var div = AJS.$('<div class="upm-notification"></div>').attr('id', 'upm-notification-type-' + notificationGroup.notificationType),
            icon = AJS.$('<span class="upm-notification-icon"></span>'),
            dismissLink = notificationGroup.links['post-notifications'],
            plugin, notification, msg, target;

        if (notificationGroup.notifications.length === 1) {
            notification = notificationGroup.notifications[0];
            plugin = notification.plugin;
            msg = notification.message;
            target = notification.links.target;
            if (plugin.links && plugin.links['plugin-icon']) {
                AJS.$('<img src="" alt="">')
                    .attr('src', plugin.links['plugin-icon'])
                    .attr('title', plugin.name)
                    .appendTo(icon);
            } else {
                icon.addClass('empty');
            }
        } else {
            msg = notificationGroup.message;
            target = notificationGroup.links.target;
            AJS.$('<img src="" alt="">')
                .attr('src', notificationGroup.links['default-icon'])
                .appendTo(icon);
        }

        icon.appendTo(div);
        buildNotificationMessage(msg, target).appendTo(div);
        if (!notificationGroup.dismissed) {
            div.addClass('upm-notification-new');
        }

        if (dismissLink) {
            div.attr('data-dismiss-link', dismissLink);
        }

        return div;
    }

    /**
     * Creates and returns an element containing the title for the notifications
     * @method buildNotificationsHeader
     * @return {HTMLElement} Title element
     */
    function buildNotificationsHeader() {
        return AJS.$('<h3></h3>').text(upm.pluginNotificationsTitle);
    }

    /**
     * Creates and returns an element containing the text if there are no notifications available
     * @method buildNoNotificationsMessage
     * @return {HTMLElement} No notifications element
     */
    function buildNoNotificationsMessage() {
        return AJS.$('<p id="upm-no-notifications"></p>').text(upm.noNotificationsText);
    }

    /**
     * Creates and returns an element for containing the notifications
     * @method buildNotificationsContainer
     * @return {HTMLElement} Notifications
     */
    function buildNotificationsContainer() {
        notificationsContainer = AJS.$('<div id="upm-notifications" class="aui-dd-parent"></div>')
            .bind('refreshNotifications', refreshNotificationsDropdown)
            // Product / placement specific classes
            .toggleClass('upm-admin-headers', hasAdminPageHeadings)
            .toggleClass('upm-project-config', hasProjectConfigHeading)
            .toggleClass('upm-page-headers', upm.isJira && upm.productVersion == 4.4 && !hasOperationMenu && !hasActionButtons)
            .toggleClass('upm-operations-headers', hasOperationMenu)
            .toggleClass('upm-action-buttons', hasActionButtons)
            .toggleClass('upm-inverse', upm.productId == 'bamboo' && upm.productVersion < 3.4)
            .toggleClass('upm-h1', upm.productId == 'bamboo' && upm.productVersion >= 3.4);
        return notificationsContainer;
    }

    /**
     * Refresh the contents of the notifications dropdown
     * @method refreshNotificationsDropdown
     */
    function refreshNotificationsDropdown() {
        AJS.$.ajax({
            url: upm.notificationsUrl,
            type: 'get',
            dataType: 'json',
            contentType: upmContentType,
            success: function(response) {
                notifications = response;
                var groups = notifications && notifications.notificationGroups;

                // Populate the dropdown with the notifications
                populateNotificationsContainer(groups);
                notificationsContainer.removeClass('hidden').addClass('loaded');
            },
            error: function(request) {
                // We get a 401 if we are on demand and/or do not show notifications
                notificationsContainer.addClass('loaded');
           }
        });
    }

    /**
     * Populates the notificationsContainer element with notifications returned from the server
     * @method populateNotificationsContainer
     * @param {Object} notifications Notifications details
     */
    function populateNotificationsContainer(notifications) {
        var notificationsDropdown = AJS.$('#upm-notifications-dropdown'),
            notificationCount = 0;

        if (notifications) {
            notificationCount = notifications.length;
        }

        //clear notifications that were added on the previous refresh
        notificationsDropdown.find('div.upm-notification').remove();
        notificationsDropdown.find('#upm-no-notifications').remove();

        if (notificationCount) {
            for (var i = 0; i < notificationCount; i++) {
                buildNotificationElement(notifications[i]).appendTo(notificationsDropdown);
            }
            var newNotificationCount = notificationsDropdown.find('.upm-notification-new').length;
            if (newNotificationCount) {
                notificationsContainer.addClass('new-notifications');
                AJS.$('#upm-notifications-count', notificationsContainer).text(newNotificationCount);
            }
        } else {
            buildNoNotificationsMessage().appendTo(notificationsDropdown);
        }
        notificationsDropdown.appendTo(notificationsContainer);
        return notificationsContainer;
    }

    function init() {
        AJS.$(window).unbind('upmready', init);

        var container = getMessageContainer(),
            start = new Date().getTime(),
            notificationsDropdown = AJS.$('<div id="upm-notifications-dropdown" class="upm-notifications-dropdown aui-dropdown"></div>'),
            $built;

        // if the container element doesn't exist, we've got nowhere to put the notifications
        if (!container || !container.length) {
            return;
        }

        // Build the element to hold the dropdown

        $built = buildNotificationsContainer();

        // Stick before project config (only jira >= 4.4)
        if (hasProjectConfigHeading) {
            container.before($built);
        // Bamboo 3.4 doesn't give us much to work with
        } else if ((upm.productId == 'bamboo' && upm.productVersion >= 3.4)
                // Put it after in jira only if it isn't an admin page heading
                || ((upm.productId == 'jira' && upm.productVersion >= 4.4) && !hasAdminPageHeadings)) {
            container.after($built);
        } else {
            container.append($built);
        }

        if (upm.isJira && upm.productVersion == 4.4) {
            $built.addClass('jira44');
        }

        notificationsContainer.addClass('hidden upm-notifications-' + upm.productId);

        buildNotificationsTrigger(0).appendTo(notificationsContainer);
        buildNotificationsHeader().appendTo(notificationsDropdown);
        notificationsDropdown.appendTo(notificationsContainer);

        // The icon popping in after a second or so is jarring, it doesn't look like part of the
        // normal page load, so fade it in for that smooth jazz feel
        if (new Date().getTime() - start > minMsBeforeFadeIn) {
            notificationsContainer.fadeIn();
        }

        notificationsContainer.dropDown('Standard');

        // PLUG-819 - We could put the "notifications" to the web transformer and remove this AJAX call, but caching
        //            is broken right now.
        refreshNotificationsDropdown();
    }

    // If we are on the upm page, wait for upm loaded to trigger to at the very least get the upm container
    AJS.$(window).bind('upmready', init);

    AJS.toInit(function() {
        if (!upm.isUpm) {
            init();
        }
    });
})();
