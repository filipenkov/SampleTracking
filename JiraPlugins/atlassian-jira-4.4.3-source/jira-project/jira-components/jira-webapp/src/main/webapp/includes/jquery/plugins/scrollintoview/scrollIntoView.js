(function () {

    function getNewScrollTop(options) {

        options              = options              || {};
        options.marginTop    = options.marginTop    || options.margin || 0;
        options.marginBottom = options.marginBottom || options.margin || 0;

        var $window      = window.top.jQuery(window.top);
        var $stalker     = window.top.jQuery("#stalker");
        var scrollTop    = $window.scrollTop();
        var scrollHeight = $window.height();
        var offsetTop    = Math.max(0, getPageY(this[0]) - options.marginTop);
        var offsetHeight = options.marginTop + this.outerHeight() + options.marginBottom;
        var newScrollTop = scrollTop;

        // Fit this element's baseline inside window.
        if (newScrollTop + scrollHeight < offsetTop + offsetHeight) {
            newScrollTop = offsetTop + offsetHeight - scrollHeight;
        }

        // Accommodate stalker if it exists.
        if ($stalker.length > 0) {
            offsetTop -= $stalker.outerHeight() + 35;
        }

        // Fit this element's top edge inside the window.
        if (newScrollTop > offsetTop) {
            newScrollTop = offsetTop;
        }

        return newScrollTop;
    }

    function getPageY(element) {

        var currElement = element,
            offsetTop = 0;

        do {
            offsetTop += currElement.offsetTop;
        } while (currElement = currElement.offsetParent);

        currElement = element;

        do {
            if (currElement.scrollTop) {
                offsetTop -= currElement.scrollTop;
            }
            currElement = currElement.parentNode
            
        } while (currElement && currElement != document.body);

        return offsetTop;
    }

    /**
     * scrollIntoView jQuery plugin
     *
     * Scroll the window if necessary so that the first element of a jQuery collection
     * is visible and best fit into the space available.
     *
     * @method scrollIntoView
     * @param {object} options -- has the following keys:
     *    duration ....... The duration of the scroll animation.
     *    marginTop ...... The margin between target element and the top window edge.
     *    marginBottom ... The margin between target element and the bottom window edge.
     *    callback ....... A function to be called when the animation is complete.
     * @return {jQuery}
     */
    jQuery.fn.scrollIntoView = function(options) {

        if (this.length > 0 && !this.hasFixedParent()) {

            options = options || {};

            // If the item is not visible we callback but do not scroll to item
            if (!this.is(":visible") && options.callback) {
                options.callback();
                return this;
            }


            var scrollTop = window.top.jQuery(window.top).scrollTop();
            var newScrollTop = getNewScrollTop.call(this, options);
            var $stalker     = window.top.jQuery("#stalker");

            if (newScrollTop !== scrollTop) {

                var $target   = this;
                var $document = window.top.jQuery(window.top.document);

                $document.trigger("moveToStarted", $target);

                if (options.duration) {
                    $document.find("body, html").stop(true).animate(
                        {
                            scrollTop: newScrollTop
                        },
                        options.duration,
                        "swing",
                        function() {
                            if (options.callback) {
                                options.callback();
                            }
                            $document.trigger("moveToFinished", $target);
                            $stalker.trigger("positionChanged");
                        }
                    );
                } else {
                    $document.find("body, html").attr("scrollTop", newScrollTop);
                }
            } else if (options.callback) {
                options.callback();
            }
        }

        return this;


    };

    /**
     * isInView jQuery plugin
     *
     * Determins if the element is in the viewport
     *
     * @method scrollIntoView
     * @param {object} options -- has the following keys:
     *    marginTop ...... The margin between target element and the top window edge.
     *    marginBottom ... The margin between target element and the bottom window edge.
     * @return {jQuery}
     */
    jQuery.fn.isInView = function (options) {

        if (this.length > 0 && !this.hasFixedParent()) {

            options = options || {};

            var scrollTop = window.top.jQuery(window.top).scrollTop();
            var newScrollTop = getNewScrollTop.call(this, options);

            return newScrollTop === scrollTop;
        }

        return this;
    };

})();