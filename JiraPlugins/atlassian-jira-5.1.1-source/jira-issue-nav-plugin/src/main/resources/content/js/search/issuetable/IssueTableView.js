AJS.namespace("JIRA.Issues.IssueTableView");

JIRA.Issues.IssueTableView = JIRA.Issues.BaseView.extend({

    tagName: "table",

    events: {
        "click a.issue": "_onIssueClick"
    },

    rowView: JIRA.Issues.IssueTableRowView,
    template: JIRA.Templates.IssueNav.issueTable,
    errorTemplate: JIRA.Templates.IssueNav.issueResultsError,

    initialize: function(options, overrides) {
        _.extend(this, overrides);
        _.bindAll(this);

        this.model.getIssueCollection().bind("reset", this.addAll);
        this.model.getIssueCollection().bind("add", this.addOne);

        this.model.bindIssueNotFound(function (issueKey) {
            var issueAnchor = JIRA.Templates.Issues.Util.issueAnchor({issueKey:issueKey});
            JIRA.Messages.showWarningMsg(JIRA.Templates.Issues.Util.issueNotFound({
                issueAnchor: issueAnchor
            }), {
                closeable: true
            });
        });

        this.model.bind("change:totalResultCount", this._updateTotalResultCount);
        this.model.bind("change:awaitingResults", this.updateAwaitingResults);
    },

    /*
     * Appends a single issue to the element for this view
     */
    addOne: function(issue) {
        if (this.resultsElement) {
            var row = new this.rowView({
                model: issue
            }, {
                fieldConfig: this.fieldConfig
            });
            this.resultsElement.append(row.render());
        }
    },

    addAll: function() {
        if (this.resultsElement) {
            var issues = this.model.getIssueCollection();
            if (issues.length) {
                this.resultsElement.empty();
                issues.each(this.addOne);
            } else if (typeof this.model.getJql() === "string") {
                this.resultsElement.html(this.errorTemplate({
                    columnCount: this.fieldConfig.columnCount(),
                    errorText: AJS.I18n.getText("issue.nav.common.no.results")
                }));
            } else {
                this.resultsElement.empty();
            }
        }
    },

    /**
     * Renders the table element's contents.
     */
    render: function() {
        // TODO render column headers using fieldconfig
        this.$el.html(this.template());
        var $theadrow = this.$el.find("thead tr");

        // render the column headers in order
        _.each(this.fieldConfig.columns, function(field) {
            $theadrow.append(field.columnHeaderTemplate({
                contextPath: contextPath
            }));
        });

        this.resultsElement = this.$el.find("tbody");
        this.addAll();
        return this;
    },

    /**
     * This is really only used for testing to tell the webdriver tests when results should be
     * available!
     */
    updateAwaitingResults: function(model, waiting) {
        if(!waiting) {
            this.$el.removeClass("loading");
        } else {
            this.$el.addClass("loading");
        }

    },


    _onIssueClick: function(event) {
        event.preventDefault();
        var id = AJS.$(event.target).closest("tr").attr("data-id");

        this.model.switchToDetailedView(id);
    },

    /**
     * @private -- event handler for "change:totalResultCount" on SearchPageModel
     * @param searchPageModel
     * @param totalResultCount
     */
    _updateTotalResultCount: function(searchPageModel, totalResultCount) {
        var $resultCount = jQuery(".results-count").first();
        if (totalResultCount > 0) {
            $resultCount.text(AJS.I18n.getText("issue.nav.common.result.count", totalResultCount));
        } else {
            $resultCount.empty();
        }
    }

});
