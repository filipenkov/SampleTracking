/**
 * Refresh the config iframe if you switch click on a tab.
 */

/** AJS.$(document).ready() won't be called on iframes unfortunately. At least not in Firefox.
 * Had to transform it into a 'ready' function which is called at the end of the page.
 */

var atlassianAuthContainerReadyHandler = function() {
    AJS.tabs.setup();
    (function ($) {
    var $iframes = $('.auth-container').find('iframe');
        $('.menu-item a').each(function (index) {
            var $tab = $(this),
                $iframe = $iframes.eq(index),
                $loadIcon = $('<div class="loading loading-tabs"/>').insertAfter($iframe);
            $iframe.load(function() {
                    $loadIcon.hide();
                });
            $tab.bind('tabSelect', function (a, tab) {
                $loadIcon.show();
                $iframe.attr('src', $iframe.attr('src'));
            });
        });
    })(AJS.$);
};
