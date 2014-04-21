AJS.namespace("JIRA.Issues.IssueView");

(function($) {
    JIRA.Issues.IssueView = JIRA.Issues.BaseView.extend({

        mixins: [
            JIRA.Issues.Mixin.PageTitleView,
            JIRA.Issues.Mixin.LoadingClass
        ],

        events: {
            "click #close-return": "_onBackToSearch"
        },

        ISSUE_ACTION_SELECTOR: "a[class*='issueaction-'], .toggle-title",

        initialize: function() {
            var instance = this;
            _.bindAll(this);
            $(document).delegate(this.ISSUE_ACTION_SELECTOR, "click", this._handleActionClicked);
            this.model.getIssueEventBus().bindUpdateStatusColor(this.updateStatusClass);
            this.model.getIssueEventBus().bindDismiss(this.destroy);
            this.model.getIssueEventBus().bindUpdateFromDom(this.updateFromDom);
            this.model.getIssueEventBus().bindRefreshIssue(this.addLoadingClass);
            this.model.getIssueEventBus().bindIssueRefreshed(this.removeLoadingClass);
            this.model.bindUpdated(function (props) {
                if (props.initialize) {
                    instance.render();
                }
            });

            // sub views
            this.header = new JIRA.Issues.IssueHeaderView({model:this.model});
            this.body = new JIRA.Issues.IssueBodyView({model:this.model});

            AJS.$("#return-to-search").click(this._onBackToSearch);
        },

        /**
         * Auto saves when action is clicked
         */
        _handleActionClicked: function () {
            this.model.getIssueEventBus().trigger("save");
        },

        /**
         * Sets elements based on a prexisting dom
         * @param $ctx
         */
        updateFromDom: function ($ctx) {
            this.setElement($ctx.find("#issue-content"));
            this.$form = this.$el;
        },

        destroy: function() {
            this.undelegateEvents();
            $(document).undelegate(this.ISSUE_ACTION_SELECTOR, "click", this._handleActionClicked);
        },

        /*
         * Renders the view.
         * Returns the element for this view following backbone's convention.
         */
        render: function () {

            AJS.log("IssueView: Render started for issue [" + this.model.getEntity().key + "]" );

            this.$el.empty();

            this.$form = $("<div />").attr({
                id: "issue-content"
            }).addClass("issue-edit-form").appendTo(this.$el);

            this.updateStatusClass();

            this.$form.prepend(this.header.render());
            this.$form.append(this.body.render());
            this.body.expandToScreenEdge();

            return this.$el;
        },
        _onBackToSearch: function(e) {
            if (!this.model.isStandalone()) {
                this.model.returnToSearch();
                e.preventDefault();
            }
        },
        updateStatusClass: function() {
            this.$el.attr("class", "result-panel " + this.model.getStatusClass());
        }
    });
})(AJS.$);
