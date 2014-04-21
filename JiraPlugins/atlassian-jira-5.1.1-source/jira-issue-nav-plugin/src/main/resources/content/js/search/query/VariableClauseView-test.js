AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module('JIRA.Issues.VariableClauseView', {
    setup: function() {
        this.fixedLozenges = [{
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

        this.$el = jQuery("<div><ul class='filter-list'></ul></div>");
        jQuery("body").append(this.$el);
    },
    teardown: function() {
        this.$el.remove();
    }
});

test("More drop down lozenge doesn't appear when there are no variable clauses", function() {

    var searcherCollection = new JIRA.Issues.SearcherCollection(this.fixedLozenges, {
        fixedLozenges: this.fixedLozenges
    });

    var view = new JIRA.Issues.VariableClauseView({
        el: this.$el,
        searcherCollection: searcherCollection
    });

    view.render();

    equals(this.$el.find(".variable:visible").length, 0, "Variable lozenge is showing when there are no variable lozenges.");

});

test("More drop down lozenge appears when there are variable clauses", function() {
    var searcherCollection = new JIRA.Issues.SearcherCollection(this.fixedLozenges, {
        fixedLozenges: this.fixedLozenges
    });

    searcherCollection.add({
        id: "variabletypeclause",
        name: "Mr Variable",
        validSearcher: true,
        editHtml: "<input type='text' name='mr' value='visible' />"
    });

    var view = new JIRA.Issues.VariableClauseView({
        el: this.$el,
        searcherCollection: searcherCollection
    });

    view.render();

    equals(this.$el.find(".variable:visible").length, 1, "Variable lozenge is not showing when there are variable lozenges.");

});