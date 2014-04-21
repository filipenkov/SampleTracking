AJS.namespace("JIRA.Issues.IssueHeaderView");

(function($) {
    JIRA.Issues.IssueHeaderView = JIRA.Issues.BaseView.extend({
        tagName:"header",
        className: "issue-header js-stalker",
        template:JIRA.Templates.ViewIssue.Header.issueHeader,

        initialize: function() {
            var instance = this;
            _.bindAll(this);

            this.model.bindUpdated(function(props) {
                if (!props.initialize) {
                    if (!_.include(props.fieldsInProgress, "summary")) {
                        instance.render();
                        instance.updateWindowTitle();
                    } else {
                        instance.renderOpsBar();
                    }
                    instance.model.getIssueEventBus().triggerPanelRendered("header", instance.$el);
                }
            });

            this.model.getIssueEventBus().bindUpdateFromDom(this.updateFromDom);
        },

        updateFromDom: function ($ctx) {
            this.setElement($ctx.find(".issue-header"));
        },

        updateWindowTitle: function () {
            var key = this.$el.find("#key-val:first").text();
            var summary = this.$el.find("#summary-val:first").text();
            var appTitle = AJS.Meta.get("app-title");
            if (!appTitle || !summary || !key) {
                console.warn("JIRA.Issues.IssueHeaderView: Parsing of title failed");
            } else {
                document.title = "[#" + key + "] " + summary + " - " + appTitle;
            }
        },

        /**
         * Renders just the ops bar
         */
        renderOpsBar: function () {
            this.opsbarView = new JIRA.Issues.IssueOpsbarView({
                el:this.$el.find(".command-bar") ,
                model: this.model
            });
            this.opsbarView.render();
            JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [this.$el, JIRA.CONTENT_ADDED_REASON.panelRefreshed]);
        },

        /*
         * Renders the view.
         * Returns the element for this view following backbone's convention.
         */
        render: function () {
            var $commentButton,
                instance = this,
                html = this.template({issue:this.model.getEntity()}),
                $nav = this.$el.find(".page-navigation").remove(), // store pagination if it is there (standalone view issue)
                $commentForm = this.$el.find("#issue-comment-add").remove();

            this.$el.attr("class", this.className);
            this.$el.empty();
            this.$el.append(html);
            this.opsbarView = new JIRA.Issues.IssueOpsbarView({
                el:this.$el.find(".command-bar") ,
                model: this.model
            });
            this.opsbarView.render();
            this.$el.find(".issue-header-content").prepend($nav);

            $commentButton = this.$el.find("#comment-issue").addClass("inline-comment"); // Prevents comment from opening in dialog.

            if ($commentForm.length === 1) {
                this.$el.addClass("action").find(".ops-cont").append($commentForm);
                $commentButton.addClass("active");
            }
            JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [instance.$el, JIRA.CONTENT_ADDED_REASON.panelRefreshed]);
            return this.$el;
        },
        _onAssigneeUpdate : function() {
            this.model.getIssueEventBus().triggerRefreshIssue();
        }

    });
})(AJS.$);
