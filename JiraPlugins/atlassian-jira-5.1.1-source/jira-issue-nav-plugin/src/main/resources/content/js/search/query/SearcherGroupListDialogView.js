AJS.namespace("JIRA.Issues.SearcherGroupListDialogView");

/**
 * List of searchers that can be added to a search
 */
JIRA.Issues.SearcherGroupListDialogView = JIRA.Issues.BaseView.extend({

    template: JIRA.Templates.IssueNav.searcherDropdownContent,

    events: {
        "click .searcher-option": "_searcherSelected",
        "click .aui-button-cancel": "_hide"
    },

    /**
     * searcherSelected(id): a searcher has been selected. id is the id of the searcher
     * hideRequested: dialog close has been requested
     */
    namedEvents: ["searcherSelected", "hideRequested"],

    initialize: function(options) {
        _.bindAll(this);
        this.searcherCollection = options.searcherCollection;
        this.searcherCollection.bindCollectionChanged(this.render);
    },

    // TODO This function currently runs twice when the dialog is first shown as InlineLayer runs refreshContent at this time
    // It would be better if we could just set the initial state which did the only content retriever.
    render: function() {
        var selectables = this.searcherCollection.getSearcherGroupsForAddMenu();
        this.$el.html(this.template({
            searcherGroups: selectables
        }));
    },

    _searcherSelected: function(e) {
        e.preventDefault();
        var id = AJS.$(e.target).data("id");
        this.triggerSearcherSelected(id);
    },

    _hide: function(e) {
        e.preventDefault();
        this.triggerHideRequested();
    }
});
