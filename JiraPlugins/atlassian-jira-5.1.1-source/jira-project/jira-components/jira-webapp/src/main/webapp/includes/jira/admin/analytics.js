/**
 * Capture some events that better explain how people use JIRA administration in general.
 */
(function($) {
    $(function() {
        var activeTab = $('meta[name="admin.active.tab"]').attr('content'),
            activeSection = $('meta[name="admin.active.section"]').attr('content'),
            projectKey = $('meta[name="projectKey"]').attr('content'),
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
                type: type,
                tab: activeTab,
                section: activeSection
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
            filtered = filtered.replace(/project-config\/(.*?)(\/|$)/,"project-config/PROJECTKEY$2");
            if (projectKey && projectKey.length) {
                filtered = filtered.split(projectKey).join("PROJECTKEY");
            }

            return filtered;
        }

        if (AJS.EventQueue) {
            // Capture clicks on project summary 'more' links
            $(document).delegate(".project-config-more-link", "click", function() {
                var el = $(this),
                    href = filterUri(el.attr('href'));
                AJS.EventQueue.push(adminNavEvent('morelink', {
                    href: href,
                    title: el.attr('title')
                }));
            });

            // Capture clicks on the navigation sidebar tabs
            $(".content-related.aui-tabs").delegate("a", "click", function() {
                var el = $(this),
                    href = filterUri(el.attr('href'));
                AJS.EventQueue.push(adminNavEvent('tabs', {
                    href: href,
                    title: el.text()
                }));
            });

            // Capture clicks on a project header (even if it wouldnt't do anything)
            // we wonder if people think it'll take them back to hte summary page
            $(document).delegate("#project-config-header-name", "click", function() {
                AJS.EventQueue.push(adminNavEvent('projectheader'));
            });

            // Capture clicks on a project avatar (even if it wouldn't do anything)
            $(document).delegate("#project-config-header-avatar", "click", function() {
                AJS.EventQueue.push(adminNavEvent('projectavatar'));
            });

            // Capture clicks on the 'back to project: X' back-links on some configuration pages.
            $(document).delegate(".back-to-proj-config", "click", function() {
                var el = $(this),
                    href = filterUri(el.attr('href'));
                AJS.EventQueue.push(adminNavEvent('backtoproject', {
                    href: href
                }));
            });

            // Capture clicks on selected scheme links
            $(".project-config-summary-scheme").delegate("a", "click", function() {
                var el = $(this),
                    href = filterUri(el.attr('href'));
                AJS.EventQueue.push(adminNavEvent('selectedscheme', {
                    href: href
                }));
            });

            $(".project-config-workflow-edit").delegate("", "click", function() {
                var el = $(this),
                    copy = el.hasClass('project-config-workflow-default');
                AJS.EventQueue.push(adminNavEvent('editworkflowfromproject', {
                    copy: copy
                }));
            });
        } // if (AJS.EventQueue)
    }); //onReady
})(AJS.$);
