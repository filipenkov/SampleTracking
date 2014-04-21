
/**
 * InlineLayer that takes a backbone view to render itself.
 */
JIRA.ViewInlineLayer = AJS.InlineLayer.extend({

    /**
     * @param options
     * options.view - view to render inside this layer
     */
    init: function (options) {
        options.contentRetriever = new JIRA.ViewContentRetriever({
            view: options.view,
            cache: options.cache
        });
        this._super(options);
        options.view.dialog = this;
    },

    /**
     * Determines whether a click should close the inline layer or not.
     *
     * TODO: Roll this function and variable declaration at the bottom of the file
     * back into InlineLayer.js after 5.0.1 goes out.
     *
     * @param e The click event.
     */
    _validateClickToClose: function (e) {
        var event = jQuery.Event("InlineLayer.beforeHide"),
            hideReason = {
                clickOutside: "clickOutside"
            };
        AJS.$(e.target).trigger(event, [this.$layer, hideReason]);

        if (event.isDefaultPrevented()) {
            return false;
        } else if (e.target === this.offsetTarget()[0]) {
            return false;
        } else if (e.target === this.layer()[0]) {
            return false;
        } else if (this.offsetTarget().has(e.target).length > 0) {
            return false;
        } else if (this.layer().has(e.target).length > 0) {
            return false;
        }

        return true;
    }
});
