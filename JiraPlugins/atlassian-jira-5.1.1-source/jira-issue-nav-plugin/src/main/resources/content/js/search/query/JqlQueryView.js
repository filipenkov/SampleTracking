AJS.namespace("JIRA.Issues.JqlQueryView");

/**
 * Renders the JQL textarea.
 */
JIRA.Issues.JqlQueryView = JIRA.Issues.BaseView.extend({

    template: JIRA.Templates.IssueNav.jqlQueryView,

    initialize: function() {
        _.bindAll(this);
        this.model.bind("change:jql", this._setQuery);
    },

    render: function() {
        // TODO: put in base class
        this.$el.html(this.template());
        this._setQuery(this.model, this.model.getJql());
    },

    search: function() {
        var jql = this.$el ? this._getInputField().val() : "";
        this.model.searchWithJql(jql);
    },

    _setQuery: function(model, jql) {
        var inputField = this._getInputField();
        inputField && inputField.val(jql);
    },

    _getInputField: function() {
        return this.$el ? this.$el.find("textarea") : null;
    }
});
