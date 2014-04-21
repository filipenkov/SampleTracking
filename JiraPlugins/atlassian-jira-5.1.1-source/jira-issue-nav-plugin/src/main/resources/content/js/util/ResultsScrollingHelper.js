AJS.namespace("JIRA.Issues.ResultsScrollingHelper");

/**
 * When in view issue mode we stalk the results table. We also ensure that it's height is contained within the window.
 */
JIRA.Issues.ResultsScrollingHelper = function () {

    var $window = AJS.$(window),
            $resultsPanel,
            active = false,
            scrollTop,
            windowHeight,
            originalHeight,
            $navigatorContent,
            top;

    /**
     * Set the issue table height, no bigger than the bottom of the screen. Scrolling for navigator in view issue mode
     * is handled by custom scroll bars.
     *
     * @private
     */
    function initViewableArea () {
        $resultsPanel = AJS.$(".results-panel").css({
            position: "static",
            visibility: "hidden"
        });

        $navigatorContent = AJS.$(".navigator-content"); // avoid stale elements
        top = $resultsPanel.offset().top;

        windowHeight = AJS.$(window).height();
        originalHeight = windowHeight - $navigatorContent.offset().top;
        $resultsPanel.css({
            position: "fixed",
            visibility: ""
        }); // Needs to be done after measurements are taken

        originalHeight = originalHeight -  parseInt($resultsPanel.css("bottom"), 10);
        $navigatorContent.height(originalHeight);
    }

    function scrollTo() {
        AJS.$(window).scrollTop(0);
        $navigatorContent.scrollTop(scrollTop);
    }

    function setViewableArea() {
        var target = originalHeight + jQuery(window).scrollTop();
        if (target < windowHeight - top) {
            $navigatorContent.height(target);
        } else {
            $navigatorContent.height(windowHeight - top);
        }
    }

    function activate() {


        if (active) {
            return; // prevent double binding of behaviour
        }

        initViewableArea();
        setViewableArea();
        scrollTo();
        $window.resize(initViewableArea);
        $window.scroll(setViewableArea);
        active = true;
    }

    function disable () {
        if (active) {

            // store scrollTop to scroll navigator to
            var scrollTop = $navigatorContent.scrollTop();

            $window.unbind("resize", initViewableArea)
                    .unbind("scroll", setViewableArea);

            // reset positioning overrides
            $resultsPanel.css({
                position: "",
                visibility: ""
            });

            $navigatorContent.css({
                height: ""
            });


            AJS.$(window).scrollTop(scrollTop); // restore back to original scroll position
            active = false;
        }
    }

    return {
        setup: function (model) {
            model.bind("beforeIssueRequest", function () {
                scrollTop = AJS.$(window).scrollTop();
            })
            .bindIssueLoaded(activate)
            .bind("issueDismissed", disable);
        }

    };
}();