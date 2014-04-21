
/**
 * AJS.ContentRetriever that wraps a backbone view.
 * content() creates a div, sets the view's element to this div, then calls the view's render() function
 */
JIRA.ViewContentRetriever = AJS.ContentRetriever.extend({
    init: function (options) {
        this.options = options;
        this.view = options.view;
    },
    content: function (ready) {
        var $el = AJS.$("<div />");
        this.view.setElement($el);
        this.view.render();
        ready($el);
    },
    cache: function () {
        return this.options.cache;
    },
    isLocked: function () {},
    startingRequest: function () {},
    finishedRequest: function () {}
});