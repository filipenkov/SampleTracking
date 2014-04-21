jQuery.fn.overlabel = function (targField) {

    this.each(function () {
        var label = AJS.$(this),
            field = targField || AJS.$("#" + (label.data("target") || label.attr("for")));
        label
            .removeClass("overlabel")
            .addClass("overlabel-apply")
            .width(field.width())
            .click(function(e) {
                if (!field.attr('disabled')) {
                    field.focus();
                }
                e.preventDefault();
            });

        field.focus(function() {
            label.addClass("hidden");
        }).blur(function() {
            if (AJS.$(this).val() === "")
            {
                label.removeClass("hidden");
            }
        });

        if (field.val() && field.val() !== "") {
            label.addClass("hidden");
        }
    });
    return this;

};
