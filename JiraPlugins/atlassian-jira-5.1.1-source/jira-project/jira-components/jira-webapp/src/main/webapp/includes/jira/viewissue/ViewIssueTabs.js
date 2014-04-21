/**
 * @namespace JIRA.ViewIssueTabs
 *
 * @requires AJS.$
 * @requires JIRA.Dialog
 *
 * This module encapsulates issue tab-related functionality
 */
JIRA.ViewIssueTabs = (function() {
    /**
     * The CSS class used to mark issue tab panel links that can be loaded using AJAX.
     */
    var AJAX_LOAD_CLASS = 'ajax-activity-content';

    /**
     * The selector for links that should be loaded using AJAX.
     */
    var AJAX_LINK_SELECTOR = AJS.format('a.{0}', AJAX_LOAD_CLASS);

    /**
     *
     * This array holds the functions that will be called after a issue tab is loaded.
     */
    var issueTabLoadedListeners = [];

    /**
     * These are used to display progress and the tab contents.
     */
    var $tabContents,
        $tabProgress;

    /**
     * The XHR that's currently in progress and hasn't been aborted, or null.
     */
    var xhrInProgress;

    /**
     * Dispatches the "issue tab loaded" event to the registered listeners.
     *
     * @param container the DOM node that was loaded (may be a tab or the whole document)
     */
    function dispatchIssueTabLoadedEvent(container) {
        container = container || document;
        AJS.$.each(issueTabLoadedListeners, function (i, fn) {
            fn(container);
        });
    }

    /**
     * Binds this class's $tabContents, $tabErrors, $tabProgress vars to the loaded tab.
     *
     * @param container the tab
     */
    function bindToTabDivs(container) {
        // these are the DOM elements we'll manipulate
        $tabContents = AJS.$(container).find('#issue_actions_container');
        $tabProgress = AJS.$(container).find('div.issuePanelProgress');
    }

    /**
     * Dispatches the "issue tab error" event to the registered listeners.
     */
    function dispatchIssueTabErrorEvent(smartAjaxResult, activeTabKey) {
        var errorPopup = new JIRA.FormDialog({
            id: 'issue-tab-error-dialog',
            widthClass: 'small',
            content: JIRA.SmartAjax.buildDialogErrorContent(smartAjaxResult, false)
        });

        // restore the previously-active tab before showing the pop-up
        setActiveTab(activeTabKey);
        $tabContents.show();

        errorPopup.show();
    }

    function setActiveTab(activeTabKey) {
        AJS.$('#issue-tabs li').each(function() {
            var $li = AJS.$(this);

            // activate the right tab
            var tabKey = $li.data('key');
            var labelHtml = AJS.format('<strong>{0}</strong>', $li.data('label'));
            if (tabKey == activeTabKey)
            {
                $li.addClass('active');
                $li.html(labelHtml);
            }
            else
            {
                $li.removeClass('active');
                var id = $li.data('id');
                var href = $li.data('href');
                $li.html(AJS.format('<a id="{0}" href="{1}" class="{2}">{3}</a>', id, href, AJAX_LOAD_CLASS, labelHtml));
            }
        });

        enablePjaxOnLinks(AJS.$('#issue-tabs'));
    }

    /**
     * Puts a tab in the loading state: marks the tab title as active, hides the previous tab's content, and shows a
     * "loading" image.
     */
    function putTabInLoadingState(activeTabKey) {
        $tabContents.hide();
        setActiveTab(activeTabKey);
    }

    /**
     * Make all activitymodule links PJAX-enabled.
     */
    function enablePjaxOnLinks(context) {
        var activeTabKey = AJS.$(context).find('li.active').data('key');
        AJS.$(context).find(AJAX_LINK_SELECTOR).click(function (event) {
            if (event.metaKey) {
                // allow people to meta-click to open link in a new tab or window
                return;
            }

            event.preventDefault();

            // cancel any pending requests
            if (xhrInProgress) {
                xhrInProgress.abort();
            }

            var $a = AJS.$(this);
            var containerID = '#activitymodule div.mod-content';

            // hide the contents, and activate the other tab
            var loadingTabKey = $a.parent().data('key');
            putTabInLoadingState(loadingTabKey);

            var xhr = JIRA.SmartAjax.makeRequest({
                jqueryAjaxFn: AJS.$.pjax,
                container: containerID,
                url: $a.attr('href'),
                timeout: null,
                complete: function (xhr, status, smartAjaxResult) {
                    if (status != 'abort') {
                        xhrInProgress = null;

                        if (!smartAjaxResult.successful)
                        {
                            // don't display error when we're going to redirect anyway
                            if (smartAjaxResult.status < 300 || smartAjaxResult.status >= 400)
                            {
                                dispatchIssueTabErrorEvent(smartAjaxResult, activeTabKey);
                            }

                            return;
                        }

                        dispatchIssueTabLoadedEvent(AJS.$(containerID));
                    }
                }
            });
            jQuery(xhr).throbber({target: $tabProgress});
            xhrInProgress = xhr;
        })
    }

    /**
     * Appends "#issue-tabs" to each activity module link, in order to
     * maintain the legacy behaviour.
     */
    function appendHashCodeToLinks(context) {
        AJS.$(context).find(AJAX_LINK_SELECTOR).each(function () {
            var $a = AJS.$(this);

            $a.attr('href', $a.attr('href') + '#issue-tabs');
        })
    }

    /**
     * Either appends "#issue-tabs" to each link or PJAXifies them, depending
     * on whether the browser supports the pushState API.
     */
    function processActivityModuleLinks(context) {
        if (AJS.$.support.pjax) {
            enablePjaxOnLinks(context);
        } else {
            appendHashCodeToLinks(context);
        }
    }

    function setupMouseoverBehaviour(context) {
        if (jQuery.browser.msie && parseInt(jQuery.browser.version, 10) === 7) {
            jQuery("a.twixi", context).bind("focus", function (e) {
                e.preventDefault();
            });
        } else {
            jQuery(context).bind("moveToFinished", function (event, target) {
                jQuery("a.twixi:visible", target).focus();
            });
        }
    }

    function onTabReady(listener) {
        // Prevent duplicate listeners.
        if (jQuery.inArray(listener, issueTabLoadedListeners) < 0) {
            issueTabLoadedListeners.push(listener);
        }
    }

    // sprinkle AJAX magic all over the tab links after they are loaded
    onTabReady(bindToTabDivs);
    onTabReady(processActivityModuleLinks);
    onTabReady(setupMouseoverBehaviour);
    onTabReady(JIRA.userhover);

    // PUBLIC methods
    return {
        /**
         * Adds the given listener to the list of listeners that get called after an issue tab has been loaded.
         *
         * @param listener a function(contents)
         */
        onTabReady: onTabReady,

        /**
         * Sets up the loaded issue tab after the DOM is ready.
         */
        domReady: dispatchIssueTabLoadedEvent
    };
})();

AJS.$(function () {
    // Remembering focused activity after we refresh panel
    if (JIRA.Events.PANEL_REFRESHED) {
        // kickass
        JIRA.bind(JIRA.Events.PANEL_REFRESHED, function (e, panel, $new, $existing) {
            if (panel === "activitymodule") {
                var $focusedTab = $existing.find("#issue_actions_container > .issue-data-block.focused");
                //assume only one focused tab
                if ($focusedTab.length === 1) {
                    $new.find("#" + $focusedTab.attr("id")).addClass("focused");
                }
            }
        });
    }
});

JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function(event, $el) {
    JIRA.ViewIssueTabs.domReady($el);
});
