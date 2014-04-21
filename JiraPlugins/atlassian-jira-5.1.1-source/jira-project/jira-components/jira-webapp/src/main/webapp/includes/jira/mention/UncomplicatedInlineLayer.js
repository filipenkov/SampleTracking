/**
 * An InlineLayer that lets you just update its content directly.
 *
 * Think of #content() like jQuery#html() now.
 * If you need to initiate callbacks and whatnot after you change content, call #refreshContent().
 *
 * @constructor AJS.UncomplicatedInlineLayer
 * @extends AJS.InlineLayer
 */
AJS.UncomplicatedInlineLayer = AJS.InlineLayer.extend({
    init: function(options) {
        options || (options = {});
        options.contentRetriever = new AJS.ContentRetriever(); // It's just a dummy.
        AJS.InlineLayer.prototype.init.call(this, options);
    },
    content: function() {
        if (arguments.length) {
            this.$content = arguments[0];
        }
        return this.$content;
    },
    refreshContent: function(callback) {
        this.layer().empty().append(this.content());
        if (AJS.$.isFunction(callback)) {
            callback.call(this);
        }
        this.contentChange();
    }
});
