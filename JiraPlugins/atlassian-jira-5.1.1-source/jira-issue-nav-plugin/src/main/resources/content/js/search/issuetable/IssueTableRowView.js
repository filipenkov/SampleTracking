AJS.namespace("JIRA.Issues.IssueTableRowView");

JIRA.Issues.IssueTableRowView = JIRA.Issues.BaseView.extend({

    tagName: "tr",

    template: JIRA.Templates.IssueNav.issueRow,

    initialize: function(options, overrides) {
        _.bindAll(this, "render", "setSelected");
        _.extend(this, overrides);

        this.model.bind("change", this.render);
        this.model.bindSelected(this.setSelected);
    },

    /**
     * Renders the Results table.
     */
    render: function() {
        this.$el.empty();
        var issue = this.model.getEntity();
        // TODO: Replace hard-coded date formatting with true column HTML.
        var $TMP_FORMATTED_DATE = issue.fields.updated.slice(0, 10).split("-").reverse().join("/");
        // render the columns in order
        _.each(this.fieldConfig.columns, function(field) {
            this.$el.append(field.columnTemplate({
                issue: issue,
                TMP_FORMATTED_DATE: $TMP_FORMATTED_DATE,
                contextPath: contextPath
            }));
        }, this);

        // status is one of the required fields, so while the status column may not be present,
        // the status field will be in the entity.
        if(this.currentStatusClass) {
            this.$el.removeClass(this.currentStatusClass);
        }
        this.currentStatusClass = this.model.getStatusClass();
        this.$el.addClass(this.currentStatusClass);
        this.$el.attr("data-id", this.model.id);
        this.$el.attr("data-issue-key",this.model.getEntity().key);
        this.setSelected(false);

        return this.$el;
    },

    setSelected: function(scrollIntoView) {

        scrollIntoView = scrollIntoView !== false;

         if (scrollIntoView) {
             this.scrollIntoView();
         }
         if (this.model.isSelected()) {
             this.$el.addClass("active-result");
         } else {
            this.$el.removeClass("active-result");
         }
    },

    scrollIntoView: function () {
        this.$el.scrollIntoView({
            marginBottom: 200,
            marginTop: 200,
            scrollTarget: function () {
                if (this.closest(".navigator-collapsed").length) {
                    return AJS.$(".navigator-content");
                } else {
                    return AJS.$(window);
                }
            }
        });
    }

});


