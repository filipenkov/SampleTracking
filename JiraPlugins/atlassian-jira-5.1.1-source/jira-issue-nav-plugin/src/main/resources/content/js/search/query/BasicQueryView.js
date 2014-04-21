AJS.namespace("JIRA.Issues.BasicQueryView");

/**
 * Renders the input box for a text query and a number of clauses.
 */
JIRA.Issues.BasicQueryView = JIRA.Issues.BaseView.extend({

    template: JIRA.Templates.IssueNav.basicQueryView,

    initialize: function(options) {
        _.bindAll(this);
        this.searcherCollection = options.searcherCollection;
        this.searcherCollection.bindRestoringFromQuery(function() {
            AJS.$(".navigator-search").addClass("searchersLoading");
        });

        this.fixedLozengeCollection = new JIRA.Issues.FixedLozengeCollection();
        this.fixedLozengeCollection.setFixedLozenges(this.searcherCollection.fixedLozenges);

        this.textFieldView = new JIRA.Issues.TextFieldView({
            collection: this.searcherCollection
        });

        // Subview for rendering fixed lozenges (project, assignee, etc)
        this.fixedLozengeContainerView = new JIRA.Issues.FixedLozengeContainerView({
            collection: this.fixedLozengeCollection,
            // Passed so FixedLozengeViews can create new Clauses.
            searcherCollection: this.searcherCollection
        });

        // Sub view for rendering the button edit additional, non-fixed filters
        this.variableClauseView = new JIRA.Issues.VariableClauseView({
            searcherCollection: this.searcherCollection
        });

        // The sub view for the clear all and add additional filters.
        this.clauseButtonView = new JIRA.Issues.ClauseButtonView({
            searcherCollection: this.searcherCollection
        });

        this.searcherCollection.bindCollectionChanged(function() {
            AJS.$(".navigator-search.searchersLoading").removeClass("searchersLoading");
        });
        this.searcherCollection.bindCollectionChanged(this.search);
    },

    /**
     * This render function is called only once when the BasicQueryView is first initialized.
     *
     * All further renders are handled at the sub-view level.
     */
    render: function() {
        this.$el.html(this.template());

        var lozengeContainer = this.$el.find(".lozenges");

        this.textFieldView.setElement(this.$el.find("input.search-entry"));
        this.fixedLozengeContainerView.setElement(lozengeContainer);
        this.variableClauseView.setElement(lozengeContainer);
        this.clauseButtonView.setElement(this.$el.find(".clause-buttons"));

        this.textFieldView.render();
        this.fixedLozengeContainerView.render();
        this.variableClauseView.render();
        this.clauseButtonView.render();
    },

    /**
     * Performs a search using the current state in the Simple Query View.
     *
     * This method is called prior to the render function and in the case that the
     * textFieldView has not rendered it will return false when asked for its query.
     *
     * At this time the real query is stored in the URL and has already been set by
     * IssueNavRouter.
     */
    search: function() {
        var textFieldValue = this.textFieldView.getQuery();
        if (textFieldValue !== false) {
            if (AJS.$.trim(textFieldValue) === "") {
                this.searcherCollection.clearTextQuery();
            } else {
                this.searcherCollection.setTextQuery(textFieldValue);
            }
        }
        this.model.searchWithJql(this.searcherCollection.createJql());
    }
});
