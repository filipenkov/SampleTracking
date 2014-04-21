AJS.namespace("JIRA.Issues.QueryView");

/**
 * This renders the container for either the keyword search or the advanced search.
 *
 * @see JIRA.Issues.JqlQueryView
 * @see JIRA.Issues.BasicQueryView
 */
JIRA.Issues.QueryView = JIRA.Issues.BaseView.extend({

    events: {
        "submit" : "search"
    },

    initialize: function(options) {
        _.bindAll(this);

        this.basicQueryView = new JIRA.Issues.BasicQueryView({
            searcherCollection: options.searcherCollection,
            model: this.model
        });
        this.jqlQueryView = new JIRA.Issues.JqlQueryView({
            model: this.model
        });

        var switcherCollection = new JIRA.Issues.SwitcherCollection([{
            id: this.model.KEYWORD_SEARCH,
            name: "Keyword",
            text: "Advanced",
            view: this.basicQueryView
        }, {
            id: this.model.ADVANCED_SEARCH,
            name: "Advanced",
            text: "Simple",
            view: this.jqlQueryView
        }]);
        this.switcherViewModel = new JIRA.Issues.QuerySwitcherViewModel({
            collection: switcherCollection
        }, {
            searchPageModel: this.model
        });

        this.switcherView = new JIRA.Issues.SwitcherView({
            template: JIRA.Templates.IssueNav.searchSwitcher,
            model: this.switcherViewModel,
            containerClass: ".search-container"
        });

        this.model.bindSearchRequested(this.clearError);
        this.model.bind("searchError", this.showError);
        this.model.bind("change:awaitingResults", this.clearError);

        this.model.bindSearchRequested(_.bind(function() {
            this.switcherViewModel.enableSwitching()
        }, this));

        options.searcherCollection.bindJqlTooComplex(_.bind(function() {
            this.switcherViewModel.disableSwitching();
        }, this));
        this.model.bindSearchError(_.bind(function() {
            this.switcherViewModel.disableSwitching();
        }, this));
    },

    render: function() {
        this.$el.html(JIRA.Templates.IssueNav.queryView());

        this.switcherView.setElement(this.$el).render();
    },

    clearError: function() {
        this.$(".notifications").empty();
    },

    /**
     * Performs a search with a query defined by the value of the textarea.
     *
     * This calls search on the model, only if we don't have an active saved search,
     * or if the query was changed from the current active saved search.
     *
     * @param e {Event} The submit event.
     */
    search: function(e) {
        e.preventDefault();
        this.getView().search();
    },

    getView: function() {
        return this.switcherViewModel.getSelected().getView();
    },
    /**
     * Show an error in the view corresponding to the messsages in the given error response.
     * @param response the error response
     */
    showError: function(response) {
        for (var m in response.errorMessages) {
            var msg = response.errorMessages[m];
            AJS.$("<div>").addClass("aui-message warning").text(msg).appendTo(this.$(".notifications"));
        }
    }
});
