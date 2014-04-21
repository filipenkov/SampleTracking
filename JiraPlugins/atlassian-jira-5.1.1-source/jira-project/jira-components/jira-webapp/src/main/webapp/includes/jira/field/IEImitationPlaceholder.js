/**
 * Temporary functionality to allow HTML5 input placeholder text
 * functionality in IE.
 *
 * Please remove this file if/when IE supports placeholder.
 */
(function($) {
    $.fn.ieImitationPlaceholder = function () {
        if ($.browser.msie) {
            var form = this,
                fields = form.find("[placeholder]");

            fields.focus(clearTextIfUnchanged);
            fields.blur(setTextFromPlaceholder);
            form.submit(clearAllIfUnchanged);

            fields.each(setTextFromPlaceholder);

            function setTextFromPlaceholder() {
                var field = $(this);
                if (field.text() === "") {
                    field.text(field.attr("placeholder"));
                }
            }

            function clearTextIfUnchanged() {
                var field = $(this);
                if (field.text() === field.attr("placeholder")) {
                    field.text("");
                }
            }

            function clearAllIfUnchanged() {
                fields.each(clearTextIfUnchanged);
            }
        }
    };
})(AJS.$);