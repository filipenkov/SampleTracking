(function($) {
    $.fn.htmlCatchExceptions = function(html) {
        try {
            this.html.apply(this, arguments);
        } catch (e) {
            if (console && console.error) {
                console.error('Error while inserting HTML: ' + e.message + ', in: ', html);
            }
        }
        return this;
    };
})(AJS.$);