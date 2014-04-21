jQuery.fn.overlabel = function (targField) {

    this.each(function ()
    {
        var label = AJS.$(this)
                .removeClass("overlabel")
                .addClass("overlabel-apply show")
                .click(function(e)
        {
            field.focus();
            e.preventDefault();
        });

        var field = targField || AJS.$("#" + label.attr("for"));

        field.focus(function()
        {
            label.removeClass("show").hide();
        }).blur(function()
        {
            if (AJS.$(this).val() === "")
            {
                label.addClass("show").show();
            }
        });
        if (field.val() && field.val() !== "")
        {
            label.removeClass("show").hide();
        }
    });
    return this;
};
