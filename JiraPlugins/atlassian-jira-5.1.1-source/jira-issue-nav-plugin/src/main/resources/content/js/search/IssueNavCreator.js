JIRA.Issues.IssueNavCreator = function() {
};

JIRA.Issues.IssueNavCreator.prototype = {

    create: function($el) {

        // these are the data that drive the issuenavigator view, in column order
        var issueNavColumns = {
            getColumnFields:function () {
                var columnFields = [];
                _.each(this.columns, function(column)
                {
                    _.each(column.fields, function(field)
                    {
                        columnFields.push(field);
                    });
                });
                return columnFields;
            },
            // returns a comma-separated list of issue fields to be fetched
            getFieldList: function() {
                var columnFields = this.getColumnFields();
                // key is special, it's not in the field list that we fetch
                var fetchFields = _.without(columnFields, "key");
                return _.union(fetchFields, this.requiredFields).join(",");
            },
            columnCount: function() {
                return this.getColumnFields().length;
            },
            // status is required to render the row, even though the status column is not
            requiredFields: ["status"],
            columns: [
                {
                    fields: ["issuetype"],
                    columnI18nKey: "",
                    columnHeaderTemplate: JIRA.Templates.IssueNav.columnHeader_issuetype,
                    columnTemplate: JIRA.Templates.IssueNav.column_issuetype
                },
                {
                    // a column may require multiple fields worth of data to be present.
                    // TODO we may have to also provide a field dependency in case a column renderer for a fieldlist also requires another field to be present
                    fields: ["key", "summary"],
                    columnI18nKey: "",
                    columnHeaderTemplate: JIRA.Templates.IssueNav.columnHeader_keysummary,
                    columnTemplate: JIRA.Templates.IssueNav.column_keysummary
                },
                {
                    fields: ["assignee"],
                    columnHeaderTemplate: JIRA.Templates.IssueNav.columnHeader_assignee,
                    columnTemplate: JIRA.Templates.IssueNav.column_assignee
                },
                {
                    fields: ["updated"],
                    columnI18nKey: "",
                    columnHeaderTemplate: JIRA.Templates.IssueNav.columnHeader_updated,
                    columnTemplate: JIRA.Templates.IssueNav.column_updated
                },
                {
                    fields: ["resolution"],
                    columnI18nKey: "",
                    columnHeaderTemplate: JIRA.Templates.IssueNav.columnHeader_resolution,
                    columnTemplate: JIRA.Templates.IssueNav.column_resolution
                },
                {
                    fields: ["status"],
                    columnI18nKey: "",
                    columnHeaderTemplate: JIRA.Templates.IssueNav.columnHeader_status,
                    columnTemplate: JIRA.Templates.IssueNav.column_status
                }

            ]};

        var issueCollection = new JIRA.Issues.IssueCollection([], {
            fieldConfig: issueNavColumns
        });
        var searchPageModel = this.searchPageModel = new JIRA.Issues.SearchPageModel({
            issueCollection: issueCollection
        });

        /**
         * Order of lozenges which are always present.
         *
         * These are the IDs of searchers which are always present in the Simple Query View.
         */
        var FIXED_LOZENGES = [{
            id: "project",
            name: AJS.I18n.getText("searcher.project")
        }, {
            id: "issuetype",
            name: AJS.I18n.getText("searcher.issuetype")
        }, {
            id: "status",
            name: AJS.I18n.getText("searcher.status")
        }, {
            id: "assignee",
            name: AJS.I18n.getText("searcher.assignee")
        }];

        var searcherCollection = new JIRA.Issues.SearcherCollection([], {
            searchPageModel: searchPageModel,
            fixedLozenges: FIXED_LOZENGES
        });

        searchPageModel.bindSearchSuccess(function() {
            if (searchPageModel.getSearchMode() === searchPageModel.ADVANCED_SEARCH) {
                searcherCollection.restoreFromJql(searchPageModel.getJql());
            }
        });

        this.issueNavRouter = new JIRA.Issues.IssueNavRouter({
            searchPageModel: searchPageModel,
            searcherCollection: searcherCollection
        });

        var resultsTableView = new JIRA.Issues.IssueTableView({
            el: $el.find(".results-panel table.navigator-results").get(0),
            model: searchPageModel
        },{
            fieldConfig: issueNavColumns
        });

        var queryView = new JIRA.Issues.QueryView({
            el: $el.find(".content-body form.navigator-search"),
            model: searchPageModel,
            searcherCollection: searcherCollection
        });

        var saveInProgressManager = this.saveInProgressManager = new JIRA.Issues.SaveInProgressManager();

        var viewIssueLoader = this.viewIssueLoader =  new JIRA.Issues.ViewIssueController({
            viewIssueContext: $el.find(".content-container").find(".result-panel"),
            saveInProgressManager: saveInProgressManager
        });

        searchPageModel.bindSelectedInDetailedMode(function (model, data) {
            viewIssueLoader.load(model.getEntity(), data);
        });

        searchPageModel.bindDetailedModeDismissed(viewIssueLoader.dismiss);

        viewIssueLoader
            .refreshRowDataAfterUpdate()
            .bindIssueDataUpdated(searchPageModel.updateEntity)
            .bindReturnToSearch(searchPageModel.backToSearch)
            .bindIssueLoading(function (issueId, lastEditData) {
                searchPageModel.selectIssue(issueId, {
                    data: lastEditData,
                    mode: searchPageModel.DETAILED_MODE
                });
            })
            .bindUnhandledSaveError(function (issueId, attemptedSavedIds, response) {
                var issue = searchPageModel.getIssueCollection().get(issueId);
                if (issue) {
                    new JIRA.Issues.UnhandledSaveErrorView().render({
                        issueEntity: issue.getEntity(),
                        attemptedSavedIds: attemptedSavedIds,
                        response: response,
                        viewIssueLoader: viewIssueLoader,
                        isCurrentIssue: issue.getIssueKey() === searchPageModel.getSelectedIssue()
                    });
                }
            });

        // handles scroll bars and stalkers when in issue detail mode
        JIRA.Issues.ResultsScrollingHelper.setup(viewIssueLoader);

        var issueNavView = new JIRA.Issues.IssueNavView({
            el: $el.find(".content-container"),
            model: searchPageModel
        });

        var headerView = new JIRA.Issues.HeaderView({
            el: $el.find(".content-body header"),
            model: searchPageModel
        });

        this.issueNavRouter.bindRouted(function() {
            viewIssueLoader.deactivateViewIssueScrolling();
            headerView.render();
            queryView.render();
            resultsTableView.render();
        });

        // Special Keyboard shortcuts. Special keys such as ENTER cannot currently be handled by the keyboard shortcuts plugin.
        // (Refer to plugin xml)
        $el.keypress(function (e) {
            if (e.keyCode === 13 && !AJS.$(e.target).is(":input")) {
                searchPageModel.switchToDetailedView();
            }
        });

        return this;
    }
};
