AJS.namespace("JIRA.Issues.IssueCollection");

JIRA.Issues.IssueCollection = JIRA.Issues.BaseCollection.extend({

    model: JIRA.Issues.IssueRowModel,
    url: contextPath + "/rest/api/2/search",

    initialize: function(models, options) {
        _.bindAll(this);
        this.isSelected = (options && options.isSelected) ? options.isSelected : function() { return false; };
        this.fieldConfig = (options && options.fieldConfig) ? options.fieldConfig: undefined;
    },

    findByIssueId: function(id) {
        return this.find(function(issueModel) {
            return issueModel.getEntity().id === id;
        });
    },

    transformToModel: function(restIssue) {
        var issueModel = new JIRA.Issues.IssueRowModel({
                id: restIssue.id,
                entity: restIssue
            }),
            instance = this;
        issueModel.isSelected = function() {
            return instance.isSelected(issueModel);
        };
        return issueModel;
    },

    parse: function(resp, xhr) {

        return _(AJS.$(resp.issues)).map(this.transformToModel);
    },

    search: function(jql, maxResults, startAt, successHandler, errorHandler) {
        return this.fetch({
            data: {
                jql: jql,
                startAt: startAt,
                maxResults: maxResults,
                fields: this.fieldConfig.getFieldList()
            },
            success: successHandler,
            error: errorHandler,
            add: startAt > 1
        });
    }

});
