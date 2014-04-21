AJS.namespace("JIRA.Issues.ClauseButtonView");

JIRA.Issues.ClauseButtonView = JIRA.Issues.BaseView.extend({

    template: JIRA.Templates.IssueNav.clauseButtonView,

    mixins: [JIRA.Issues.SearcherEditDialogManagerView],

    events: {
        "click .add-filter": "_showDialog",
        "click .clear-filters": "_resetClauses"
    },

    initialize: function(options) {
        _.bindAll(this);
        this.searcherCollection = options.searcherCollection;
        this.searcherCollection.bindCollectionChanged(this._showOrHideClauseButtons);
    },

    render: function() {
        this.$el.html(this.template());
        this._showOrHideClauseButtons();
        return this.$el;
    },

    _showOrHideClauseButtons: function() {
        var addFiltersButton = this.$el.find(".add-filter"),
            clearFiltersButton = this.$el.find(".clear-filters");

        var clearFiltersAction = this.searcherCollection.isDirty() ? "show" : "hide",
            addFiltersAction = this.searcherCollection.getSearcherGroupsForAddMenu().length > 0 ? "show" : "hide";

        clearFiltersButton[clearFiltersAction]();
        addFiltersButton[addFiltersAction]();
    },

    /**
     * Closes the dialog and returns true if the dialog exists, otherwise does nothing and returns false.
     */
    closeDialog: function(){
        if (this.dialog) {
            this.dialog.hide();
            return true;
        }
        return false;
    },

    _showDialog: function(e) {
        e.preventDefault();
        if (!this.dialog) {
            this._initDialog();
        }
        this.cardView.changeToView("list");
        this.dialog.show();
        this.trigger("open");
    },

    _initDialog: function() {
        var instance = this;

        var listView = new JIRA.Issues.SearcherGroupListDialogView({
            searcherCollection: this.searcherCollection
        });

        var editView = new JIRA.Issues.SearcherEditDialogView({
            displayBackButton: true,
            searcherCollection: this.searcherCollection
        });

        this.cardView = new JIRA.Issues.CardView({
            views: {
                "list": listView,
                "edit": editView
            }
        });

        var dialog = this.dialog = new JIRA.ViewInlineLayer({
            offsetTarget: this.$el.find(".add-filter"),
            view: this.cardView
        });

        listView.bindHideRequested(function() {
            dialog.hide();
        });
        listView.bindSearcherSelected(function(searcherId) {
            _.defer(function() {
                editView.setSearcherId(searcherId);
                instance.cardView.changeToView("edit");
            });
        });

        editView.bindBackRequested(function() {
            _.defer(function() {
                instance.cardView.changeToView("list");
            });
        });

        this.initSearcherEditDialog(editView, dialog, this.searcherCollection);
    },

    _resetClauses: function() {
        this.searcherCollection.clearSearchState();
    }

});
