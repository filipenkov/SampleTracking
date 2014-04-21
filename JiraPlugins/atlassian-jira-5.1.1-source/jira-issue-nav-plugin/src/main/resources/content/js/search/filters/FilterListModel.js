AJS.namespace("JIRA.Issues.FilterListModel");

/**
 * Represents the list of favourite filters and system filters as well as the state of the current filter
 * (if any).
 */
JIRA.Issues.FilterListModel = JIRA.Issues.BaseModel.extend({
    properties: [ "currentFilter" ],

    initialize: function(attributes, options) {
        _.bindAll(this);
        this.searchPageModel = options.searchPageModel;
    },

    /**
     * Is the given JQL a new search or equivalent to the current filter.
     *
     * @param {string} jql to compare to current filter
     * @returns {boolean} true only if the given jql is different than currentSearch
     */
    isNewSearch: function(jql) {
        var currentFilter = this.getCurrentFilter();
        return !currentFilter || currentFilter.getJql() !== jql;
    },

    /**
     * Actually perform a search using the given filter.
     * @param {JIRA.Issues.FilterModel} filter a filter model to search.
     */
    searchWithFilter: function(filter) {
        this.searchPageModel.searchWithFilter(filter);
    }

});
