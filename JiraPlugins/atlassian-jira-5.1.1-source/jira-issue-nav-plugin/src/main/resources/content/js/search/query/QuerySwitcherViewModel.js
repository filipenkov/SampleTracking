AJS.namespace("JIRA.Issues.SwitcherModel");

/**
 * Model that represents a switcher collection with a selection for the switcher in the query view
 */
JIRA.Issues.QuerySwitcherViewModel = JIRA.Issues.BaseModel.extend({

    mixins: [JIRA.Issues.Mixin.SingleSelect],

    properties: ["disabled"],

    namedEvents: ["selectionChanged"],

    initialize: function(attributes, options) {
        this.searchPageModel = options.searchPageModel;
        this.searchPageModel.bind("change:searchMode", _.bind(function() {
            this.triggerSelectionChanged.apply(this, arguments);
        }, this));
    },

    getSelected: function() {
        var id = this.searchPageModel.getSearchMode();
        return id ? this.getCollection().get(id) : null;
    },

    setSelected: function(selected) {
        this.searchPageModel.switchToSearchMode(selected ? selected.id : null);
    },

    enableSwitching: function() {
        this.setDisabled(false);
    },

    disableSwitching: function() {
        this.setDisabled(true);
    }
});
