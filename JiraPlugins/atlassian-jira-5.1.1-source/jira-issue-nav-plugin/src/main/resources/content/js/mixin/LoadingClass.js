
/**
 * Mixin for views. Provides utility functions for adding & removing a loading class to the view's element
 */
JIRA.Issues.Mixin.LoadingClass = {

    addLoadingClass: function() {
        this.$el.addClass("loading");
    },

    removeLoadingClass: function() {
        this.$el.removeClass("loading");
    }
};
