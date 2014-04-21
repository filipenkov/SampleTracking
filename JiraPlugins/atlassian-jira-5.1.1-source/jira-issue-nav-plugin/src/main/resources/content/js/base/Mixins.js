(function() {
    // ### JIRA.Issues.Mixins ###
    // Mixin utilities
    JIRA.Issues.Mixins = {

        /*
         * Creates a camelCased method name
         */
        createMethodName: function(prefix, suffix) {
            return prefix + suffix.charAt(0).toUpperCase() + suffix.substr(1);
        }
    }
})();
