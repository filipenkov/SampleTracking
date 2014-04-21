AJS.namespace("JIRA.Issues.FixedLozengeView");

/**
 * View that handles displaying a search lozenge and creating its associated dialog
 */
JIRA.Issues.FixedLozengeView = JIRA.Issues.BaseView.extend({

    mixins: [JIRA.Issues.SearcherEditDialogManagerView],

    tagName : 'li',
    className : 'lozenge drop-arrow',

    events: {
        "click": "_showDialog"
    },

    initialize: function(options) {
        _.bindAll(this);
        this.searcherCollection = options.searcherCollection;
        this.searcherCollection.bindCollectionChanged(this.render);
    },

    render: function() {
        var id = this.model.getId();
        this.$el.attr("data-id", id);

        this.$el.html(this._createContent());
        this.$el.removeClass("loading");

        return this;
    },

    _showDialog: function(e) {
        e.preventDefault();

        if(!this.dialog) {
            this._initDialog();
        }

        this.dialog.show();
    },

    _initDialog: function() {

        // TODO: refactor out all uses of this.searcherCollection into underlying models
        var view = new JIRA.Issues.SearcherEditDialogView({
            displayBackButton: false,
            searcherId: this.model.getId(),
            searcherCollection: this.searcherCollection
        });

        var dialog = this.dialog = new JIRA.ViewInlineLayer({
            offsetTarget: this.$el,
            cache: false,
            view: view
        });

        view.bindFilterSelected(_.bind(function() {
            this.$el.addClass("loading");
        }, this));

        this.initSearcherEditDialog(view, dialog, this.searcherCollection);
    },

    _createContent: function() {
        var searcher = this.searcherCollection.get(this.model.getId());
        if (searcher && searcher.getDisplayValue()) {
            return searcher.getDisplayValue();
        }
        else if (searcher) {
            return JIRA.Templates.IssueNav.lozengeContent({
                name: searcher.getName()
            });
        } else {
            // This is a dodgy case
            return JIRA.Templates.IssueNav.lozengeContent({
                name: this.model.getName()
            });
        }
    }
});
