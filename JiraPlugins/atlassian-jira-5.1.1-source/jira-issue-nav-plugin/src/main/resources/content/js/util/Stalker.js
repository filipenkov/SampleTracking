(function ($) {

    /**
     *
     * @param options
     */
    $.fn.stalker = function (options) {

        var $stalker = this,
            $placeholder,
            offset;

        if (!$stalker.length || $stalker.data("stalker-applied")) {
            return;
        }

        $stalker.data("stalker-applied", true);

        options = options || {};
        options.offsetTop = options.offsetTop || 0;

        function unstalk () {
            if ($placeholder) {
                $stalker.css({
                    position: "",
                    top: "",
                    left: "",
                    width: "",
                    height: ""
                }).removeClass("detached");
                $stalker.unbind("stalkerHeightUpdated", updatePlaceholderHeight);
                $placeholder.remove();
                $placeholder = null;
            }
        }

        function stalk() {
            if (!$placeholder) {
                $placeholder = jQuery("<div class='stalker-placeholder' />").css("visibility", "hidden")
                        .addClass($stalker[0].className)
                        .height($stalker.outerHeight())
                        .width($stalker.outerWidth())
                        .insertBefore($stalker);

                $stalker.bind("stalkerHeightUpdated", updatePlaceholderHeight);

                $stalker.css({
                    position: "fixed",
                    top: options.offsetTop,
                    left: offset.left,
                    width: options.width || $stalker.width()
                }).addClass("detached");

                if (typeof pollWhenStalking.timeout === 'undefined') pollWhenStalking();
            }
        }

        function updatePlaceholderHeight() {
            $placeholder.height($stalker.height());
        }

        function doStalk () {

            if (!$stalker.is(":visible")) {
                 return;
            }

            if (!offset) {
                offset = $stalker.offset(); // cache offset
            }

            // Is the window top intersecting the stalker
            if ($(window).scrollTop() + options.offsetTop  >= offset.top) {
                stalk();
            } else {
                unstalk();
            }
        }

        // IE9 won't trigger a scroll when removing elements, causes the page to jump - so sadly polling is needed.
        // Trying to minimize impact by polling only when stalker is stalking
        function pollWhenStalking () {
            if ($placeholder) {
                doStalk();
                pollWhenStalking.timeout = setTimeout(pollWhenStalking, 250);
            } else {
                delete pollWhenStalking.timeout;
            }
        }

        $(window).bind('resize scroll', doStalk);

        doStalk(); // Trigger straight away incase we have scrolled the page as the result of anchor

        return {
            unstalk: function () {
                unstalk();
                $(window).unbind("resize scroll", doStalk);
            }
        }
    };



})(AJS.$);



