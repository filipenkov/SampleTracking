AJS.namespace("JIRA.Issues.FilterListView");

/**
 * Shows the user's favourite filters.
 */
JIRA.Issues.FilterListView = JIRA.Issues.BaseView.extend({

    template:JIRA.Templates.IssueNav.searchHeader,

    initialize: function(options) {
        _.bindAll(this);
        this.collection = options.collection;

        this.collection.bind("change", this.render);
        this.model.bind("change:currentFilter", this.renderHeader);
    },

    renderHeader: function() {
        this.$el.find(".saved-search-label").text(this._getFilterName());
    },

    /**
     * Renders the table element's contents.
     */
    render: function() {
        this.$el.html(this.template({
            "filterName": this._getFilterName(),
            "filters" : this.collection.toJSON()
        }));

        // need to bind this event straight away since the dropdown removes these elements
        // from this.$el
        this.$el.find(".aui-list a").bind("click", this.searchWithFilter);

        JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [this.$el]);

        return this.$el;
    },

    searchWithFilter: function(e) {
        this.model.searchWithFilter(this.collection.get(jQuery(e.target).data("id")));
        e.preventDefault();
    },

    _getFilterName: function() {
        if(this.model.getCurrentFilter()) {
            return this.model.getCurrentFilter().getName();
        } else {
            return AJS.I18n.getText("common.concepts.search");
        }
    }
});
