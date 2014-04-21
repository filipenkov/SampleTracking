/**
 * Capture some events that better explain how people use JIRA administration in general.
 */
(function($) {
    $(function() {
        var projectKey = $('meta[name="projectKey"]').attr('content'),
                contextPath = ((typeof AJS.contextPath == "function") ? AJS.contextPath() : contextPath) || "";

        /**
         * Convenience to create and return an object that represents a navigation action in administration.
         *
         * @param type    a unique name to represent the specific kind of navigation action used
         * @param opts    an object containing any particular properties of relevance for this navigation type.
         */
        function adminNavEvent(type, opts) {
            opts = (typeof opts != 'object') ? {} : opts;
            type = type || "unknown";
            var props = jQuery.extend({
                type: type
            }, opts);
            return { name: "administration.navigate" + "." + type, properties: props };
        }

        /**
         * Remove, anonymize and normalize any sensitive information in a URL for the purposes of
         * collection for statistical analysis.
         */
        function filterUri(href) {
            if (typeof href != 'string') return null;

            var uri = parseUri(href),
                    filtered;

            // Remove the protocol, domain and context path from the URL.
            filtered = uri.path.slice(contextPath.length);

            // Remove project keys
            projectKey && (filtered = filtered.replace(new RegExp("\\b" + projectKey + "\\b"), "PROJECTKEY"));

            return filtered;
        }

        $(document).delegate("#administration-suggestions .aui-list-item-link,#administration-quicksearch-suggestions .aui-list-item-link", "click", function() {
            if (AJS.EventQueue) {
                var el = $(this),
                        href = filterUri(el.attr('href'));
                AJS.EventQueue.push(adminNavEvent('keyboardshortcut', {
                    href: href,
                    title: el.attr('title')
                }));
            }
        });
    }); //onReady
})(AJS.$);
