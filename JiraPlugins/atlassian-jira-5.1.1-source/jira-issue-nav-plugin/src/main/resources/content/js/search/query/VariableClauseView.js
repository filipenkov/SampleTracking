AJS.namespace("JIRA.Issues.VariableClauseView");

/**
 * View that handles displaying a roll up menu representing multiple search clauses, a list of which is displayed
 * on click.
 */
JIRA.Issues.VariableClauseView = JIRA.Issues.BaseView.extend({

    events: {
        "click .variable.lozenge": "_showDialog"
    },

    mixins: [JIRA.Issues.SearcherEditDialogManagerView],

    template: JIRA.Templates.IssueNav.variableLozenge,

    initialize: function(options) {
        _.bindAll(this);
        this.searcherCollection = options.searcherCollection;
        var instance = this;
        this.variableLozenges = _.filter(function(model) {
            var id = model.getId();
            return id !== instance.collection.QUERY_ID && !_.contains(instance.collection.fixedLozengeIds(), id);
        });
        this.searcherCollection.bindCollectionChanged(this._setLozengeDetails);
    },

    render: function() {
        var lozengeList = this.$el.find(".filter-list");
        this.variableLozengeButton = AJS.$(this.template());
        lozengeList.append(this.variableLozengeButton);
        this._setLozengeDetails();
        return this.$el;
    },

    _setLozengeDetails: function() {
        if (this.variableLozengeButton) {

            // we should never be called if the variable count is less than 1, BUT if we are called, it's not our problem.
            var clauses = this.searcherCollection.getVariableClauses(),
                variableCount = clauses.length;

            this.variableLozengeButton.removeClass("attention");
            if (variableCount > 0) {
                if (this._hasClauseWithError(clauses)) {
                    this.variableLozengeButton.addClass("attention");
                }
                this.variableLozengeButton.find(".fieldLabel").text(AJS.I18n.getText('issue.nav.xmore', variableCount));
                this.variableLozengeButton.show();
            } else {
                this.variableLozengeButton.hide();
            }
        }
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
        if(!this.dialog) {
            this._initDialog();
        }
        this.cardView.changeToView("list");
        this.dialog.show();
        this.trigger("open");
    },

    _initDialog: function() {
        var instance = this;
        var listView = new JIRA.Issues.ClauseListDialogView({
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
            offsetTarget: this.$el.find(".variable"),
            view: this.cardView
        });

        listView.bindHideRequested(function() {
            dialog.hide();
        });
        listView.bindClauseSelected(function(searcherId) {
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

    _hasClauseWithError: function(clauses) {
        return _.any(clauses, function(clause) {
            return clause.hasError();
        });
    }

});
