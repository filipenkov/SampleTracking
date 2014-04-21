AJS.namespace("JIRA.Issues.IssueOpsbarView");

(function($) {
    JIRA.Issues.IssueOpsbarView = JIRA.Issues.BaseView.extend({

        template: JIRA.Templates.ViewIssue.Header.opsbar,

        /*
         * Renders the view.
         * Returns the element for this view following backbone's convention.
         */
        render: function () {
            var html = this.template({issue:this.model.getEntity()});
            this.$el.empty();
            this.$el.append(html);
            return this.$el;
        }
    });
})(AJS.$);
