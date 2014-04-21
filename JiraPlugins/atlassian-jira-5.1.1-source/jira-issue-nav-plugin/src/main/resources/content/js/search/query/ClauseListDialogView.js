AJS.namespace("JIRA.Issues.ClauseListDialogView");

/**
 * List of clauses (ie searchers with a value) that have been added to the search
 */
JIRA.Issues.ClauseListDialogView = JIRA.Issues.BaseView.extend({

    events: {
        "click .clauses .searcherValue": "_clauseSelected",
        "click .remove-filter": "_removeFilterRequested"
    },

    template: JIRA.Templates.IssueNav.enabledClauses,

    namedEvents: ["clauseSelected", "hideRequested"],

    initialize: function(options) {
        _.bindAll(this);
        this.searcherCollection = options.searcherCollection;
        // TODO: bind to valueUpdated to rerender on change?
        this.searcherCollection.bindCollectionChanged(this._renderAndCheckForClose);
    },

    render: function() {
        this.$el.html(this.template({
            clauses: this._getClauses()
        }));
    },

    /**
     * Re-renders and checks to
     */
    _renderAndCheckForClose: function() {
        this.render();
        if (!this._getClauses().length) {
            this.triggerHideRequested();
        }
    },

    _clauseSelected: function(e) {
        e.preventDefault();
        var $target = AJS.$(e.target),
            id = $target.parents("li").first().data("id");
        this.triggerClauseSelected(id);
    },

    _removeFilterRequested: function(e) {
        e.preventDefault();

        var instance = this;

        /*
         Need to defer otherwise InlineLayer will hide This happens because the inline dialog chooses to close
         if the target element clicked is not a child element of the InlineLayer. Because we switch the content in the
         dialog, the back link is no longer in the InlineLayer therefor not a child element.  To rectify the problem
         we delay the toggling of content.
         */
        _.defer(function () {
            var $target = AJS.$(e.target),
                $listElement = $target.parents("li").first();
            var id = $listElement.data("id");

            instance.searcherCollection.clearClause(id);

            $listElement.remove();
            instance.triggerHideRequested();
        });
    },

    _getClauses: function() {
        var clauses = this.searcherCollection.getVariableClauses();
        var clausesJson = _.map(clauses, function(searcherModel) {
            return searcherModel.toJSON();
        });
        _.each(clausesJson, this._formatForTemplate);
        return clausesJson;
    },

    _formatForTemplate: function(clauseJson) {
        if (clauseJson.validSearcher) {
            clauseJson.invalidMessage = "";
        }
        else {
            clauseJson.invalidMessage = AJS.I18n.getText("searcher.invalid.searcher");
        }
    }
});
