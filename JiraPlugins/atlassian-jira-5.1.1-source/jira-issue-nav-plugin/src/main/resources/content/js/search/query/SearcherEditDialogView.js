AJS.namespace("JIRA.Issues.SearcherEditDialogView");

JIRA.Issues.SearcherEditDialogView = JIRA.Issues.BaseView.extend({

    namedEvents: ["filterSelected", "backRequested", "hideRequested"],

    events: {
        "click .aui-button-cancel": "_onCancelClicked",
        "click .back": "_onBackClicked",
        "click .filter": "_onFilterClicked"
    },

    template: JIRA.Templates.IssueNav.lozengeDropdownContent,

    initialize: function (options) {
        _.bindAll(this);
        this.displayBackButton = options.displayBackButton;
        this.searcherId = options.searcherId;
        this.searcherCollection = options.searcherCollection;
        this.searcherCollection.bindCollectionChanged(this._bindToSearcher);
    },

    render: function() {
        // TODO: dirty - this should be bound in initialize(), but model.searcher() may not exist at that point
        if (!this.boundSearcher) {
            this._bindToSearcher();
        }

        // Ask the searcher to retrieve html (which will trigger readyForDisplay immediately if the editHtml is cached)
        this.boundSearcher.retrieveEditHtml();
    },

    setSearcherId: function(searcherId) {
        this.searcherId = searcherId;
        this._bindToSearcher();
    },

    _bindToSearcher: function() {
        if (this.boundSearcher) {
            this.boundSearcher.unbind("readyForDisplay", this._renderEditHtml);
        }
        this.boundSearcher = this.searcherCollection.get(this.searcherId);
        if (this.boundSearcher) {
            this.boundSearcher.bindReadyForDisplay(this._renderEditHtml);
        }
    },

    _renderEditHtml: function($editHtml) {
        var renderedContent = AJS.$(this.template({
            displayBackButton: this.displayBackButton
        }));

        this.$el.html(renderedContent);

        this.$el.find(".form-body").append($editHtml);

        // Trigger NEW_CONTENT_ADDED as searchers may need to add js to editHtml
        JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [this.$el]);
    },

    _onCancelClicked: function(e) {
        e.preventDefault();
        this.triggerHideRequested();
    },

    _onBackClicked: function(e) {
        e.preventDefault();
        this.triggerBackRequested();
    },

    _onFilterClicked: function(e) {
        e.preventDefault();
        this.triggerFilterSelected(this.searcherId);
    }

});
