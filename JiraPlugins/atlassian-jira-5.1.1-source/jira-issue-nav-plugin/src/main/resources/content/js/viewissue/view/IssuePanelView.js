AJS.namespace("JIRA.Issues.IssuePanelView");

(function($) {
    JIRA.Issues.IssuePanelView = JIRA.Issues.BaseView.extend({
        tagName:"div",
        template:JIRA.Templates.ViewIssue.Body.issuePanel,

        initialize: function(options) {
            _.bindAll(this);
            this.issueEventBus = options.issueEventBus;
            this.model.bindUpdated(this.applyDomUpdate);
        },

        /**
         * If edits are in progress will replace only elements that have been saved
         * If no edits are in progress will replace entire panel.
         * @param props
         */
        applyDomUpdate: function (props) {
            var updatedDom = this.model.applyUpdates(this._renderPanel(), this.$el, props.fieldsSaved, props.fieldsInProgress);
            if (updatedDom) {
                if (updatedDom.type === "replace") {
                    this.$el.replaceWith(updatedDom.$el);
                    this.setElement(updatedDom.$el);
                }
                JIRA.trigger(JIRA.Events.PANEL_REFRESHED, [this.model.id, this.$el, updatedDom.$existing]);
                this.issueEventBus.triggerPanelRendered(this.model.id, this.$el);
                _.each(updatedDom.updates, function (node) {
                    JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [node, JIRA.CONTENT_ADDED_REASON.panelRefreshed]);
                });
            }
        },

        /*
         * Renders the view.
         * Returns the element for this view following backbone's convention.
         */
        render: function () {
            return this.$el = this._renderPanel();
        },

        _renderPanel: function() {

            var $el;
            var entity = this.model.getEntity();

            if(entity.renderHeader) {
                // we are responsible for rendering the chrome
                $el = jQuery("<div/>");
                $el.attr("id", this.model.getPanelId());
                $el.addClass("module toggle-wrap");
                if(entity.styleClass) {
                    $el.addClass(entity.styleClass);
                }
                $el.html(this.template(entity));
            } else {
                // Server renders chrome
                $el = $(this.template(entity))
            }

            return $el;
        }
    });
})(AJS.$);
