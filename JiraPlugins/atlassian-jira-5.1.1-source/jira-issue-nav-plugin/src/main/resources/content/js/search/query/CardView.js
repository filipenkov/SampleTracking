AJS.namespace("JIRA.Issues.CardView");

/**
 * View that switching between a couple of views
 */
JIRA.Issues.CardView = JIRA.Issues.BaseView.extend({

    /**
     * viewChanged: fired when the active view is changed.
     */
    namedEvents: ["viewChanged"],

    /**
     * @param options
     * options.views: map of views by id (required)
     * options.activeView: id of view to show (optional, defaults to first in view)
     */
    initialize: function(options) {
        _.bindAll(this);
        this.views = options.views;
        if (options.activeView) {
            this.activeView = options.activeView;
        }
        else {
            var keys = _.keys(this.views);
            if (keys.length > 0) {
                this.activeView = keys[0];
            }
        }
    },

    /**
     * Renders the table element's contents.
     */
    render: function() {
        this._renderActiveView();
    },

    /**
     * Sets the active view. If id is the current active view, this is a no-op.
     * @param id id of view to make active
     */
    changeToView: function(id) {
        if (id === this.activeView) {
            return;
        }
        this.activeView = id;
        if (this.$el) {
            this._renderActiveView(id);
        }
        this.triggerViewChanged(id);
    },

    _renderActiveView: function() {
        var view = this.views[this.activeView];
        view.setElement(this.$el);
        view.render();
    }

});
