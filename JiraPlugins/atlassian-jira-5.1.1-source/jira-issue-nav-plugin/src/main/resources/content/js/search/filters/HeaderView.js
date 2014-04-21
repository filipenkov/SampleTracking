AJS.namespace("JIRA.Issues.HeaderView");

/**
 * Renders the header, including current filter and filter list.
 */
JIRA.Issues.HeaderView = JIRA.Issues.BaseView.extend({

    // TODO KA2 make this view's model be FilterListModel and move FilterCollection into that

    initialize: function(options) {
        _.bindAll(this, "render");

        this.filterCollection = new JIRA.Issues.FilterCollection();
        //pass the collection to a search view
        this.filterListView = new JIRA.Issues.FilterListView({
            model: this.model.filterListModel,
            collection: this.filterCollection
        });
    },

    render: function() {
        this.$el.html(this.filterListView.render());

        this.filterCollection.fetchSearches();
        return this.$el;
    }
});
