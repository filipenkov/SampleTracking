(function ($) {

    // Don't close InlineLayers if we click a calender inside it
    $(document).bind("InlineLayer.beforeHide", function (e, layer, reason) {
        var $target = $(e.target);
        if ($target.closest(".calendar").length) {
            e.preventDefault();
        }
    });

})(AJS.$);