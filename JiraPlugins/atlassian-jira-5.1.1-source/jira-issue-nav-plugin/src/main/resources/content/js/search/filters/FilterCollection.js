AJS.namespace("JIRA.Issues.FilterCollection");

/**
 * A collection of FilterModel objects.
 *
 */
JIRA.Issues.FilterCollection = JIRA.Issues.BaseCollection.extend({

    model: JIRA.Issues.FilterModel,
    url: contextPath + "/rest/api/2/filter/favourite",

    parse: function(resp, xhr) {
        return _(resp).map(JIRA.Issues.FilterModel.transformToModel);
    },

    fetchSearches: function() {
        var that = this;
        this.fetch({
            success:function() {
                that.trigger("change");
            }
        });
    }
});
