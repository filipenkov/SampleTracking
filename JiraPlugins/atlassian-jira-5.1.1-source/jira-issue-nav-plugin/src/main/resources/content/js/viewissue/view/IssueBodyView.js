AJS.namespace("JIRA.Issues.IssueBodyView");

(function($) {
    JIRA.Issues.IssueBodyView = JIRA.Issues.BaseView.extend({
        tagName:"div",
        className: "content-container issue-body-content",

        panelLocations: {
            leftPanels: ".issue-main-column",
            rightPanels: ".issue-side-column",
            infoPanels: ".issue-body"
        },

        template:JIRA.Templates.ViewIssue.Body.issueBody,

        initialize: function() {
            _.bindAll(this);

            this.model.getPanels().bindPanelRemoved(this.removePanel);
            this.model.getPanels().bindPanelAdded(this.addPanel);
            this.model.getIssueEventBus().bindUpdateFromDom(this.updateFromDom);
        },

        /**
         * Creates the required models and views based of a prexisting dom
         *
         * @param $ctx
         */
        updateFromDom: function ($ctx) {
            var instance = this;
            this.setElement($ctx.find(".issue-body-content"));

            function createPanel (pos, el) {
                var id = el.id;
                if (id === "addcomment") {
                    // The id in the DOM is different to the entityId
                    id = "addcommentmodule";
                }
                var model = instance.model.getPanels().createPlaceholderModel(pos, id);
                if (model) {
                    new JIRA.Issues.IssuePanelView({
                        el: el,
                        model: model,
                        issueEventBus: instance.model.getIssueEventBus()
                    });
                }
            }

            this.$el.find(this.panelLocations.leftPanels + " .module").each(function () {
                createPanel("leftPanels", this);
            });
            this.$el.find(this.panelLocations.rightPanels + " .module").each(function () {
                createPanel("rightPanels", this);
            });
            this.$el.find(this.panelLocations.infoPanels + ". module").each(function () {
                createPanel("infoPanels", this);
            });

            if ($("#edit-issue").length === 1) {
                if (this.$el.find("#descriptionmodule").length === 0) {
                    this.model.getPanels().addDescriptionPanel();
                }
            }
        },

        /*
         * Renders the view.
         * Returns the element for this view following backbone's convention.
         */
        render: function () {

            var panels = this.model.getPanels();
            var $bodyHtml = $(this.template());
            this.$el.html($bodyHtml);
            this._renderPanels(panels.getLeftPanels(), "leftPanels");
            this._renderPanels(panels.getRightPanels(),"rightPanels");
            this._renderPanels(panels.getInfoPanels(), "infoPanels");
            this.expandToScreenEdge();

            return this.$el;
        },

        /**
         * Expands the content area of issue to the screen edge
         */
        expandToScreenEdge: function () {
            var cushion = 20;
            this.$el.css("height", AJS.$(window).height() - this.$el.offset().top - cushion);
        },

        removePanel: function(data) {
            var location = this._convertLocation(data.location);
            this.$(location).find("#" + data.panel.getPanelId()).remove();
        },

        addPanel: function(data) {
            var panelView = new JIRA.Issues.IssuePanelView({
                model:data.panel,
                issueEventBus: this.model.getIssueEventBus()
            });
            var $location = this.$(this._convertLocation(data.location));
            var currentPanels = $location.children();
            if(data.index >= currentPanels.length) {
                $location.append(panelView.render());
            } else {
                jQuery(currentPanels[data.index - 1]).after(panelView.render());
            }
            JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [$location, JIRA.CONTENT_ADDED_REASON.panelRefreshed]);
        },

        _convertLocation: function(locationId) {
            return this.panelLocations[locationId];
        },

        _renderPanels: function(panels, location) {
            var instance = this;
            _.each(panels, function(panel, index) {
                instance.addPanel({location:location, panel:panel, index:index});
            });
        }
    });
})(AJS.$);
