AJS.namespace("JIRA.Issues.TextFieldView");

/**
 * Simple Query View's text search field.
 */
JIRA.Issues.TextFieldView = JIRA.Issues.BaseView.extend({

    initialize: function() {
        _.bindAll(this);

        this.collection.on("remove change add", _.bind(function(model) {
            if (model.getId() === this.collection.QUERY_ID) {
                this.render();
            }
        }, this));

        /**
         * The keyword input is read whenever the collection changes, we need to
         * make an exception to this when the browser goes back or forwards. In these cases
         * we want to disregard what is in the keyword input and use the URL instead.
         */
        this.collection.bindRestoringFromQuery(_.bind(function(params) {
            this._setQuery(params[this.collection.QUERY_PARAM]);
        }, this));
    },

    render: function() {
        var model = this.collection.get(this.collection.QUERY_ID);
        this._setQuery(model ? model.getDisplayValue() : "");
    },

    _setQuery: function(query) {
        this.$el.val(query);
    },

    getQuery: function() {
        if (!this.$el.is("input")) {
            return false;
        }
        return this.$el.val();
    }

});