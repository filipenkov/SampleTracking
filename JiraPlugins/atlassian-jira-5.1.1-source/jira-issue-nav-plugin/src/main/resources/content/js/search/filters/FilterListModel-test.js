AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");

module("JIRA.Issues.FilterListModel", {
});

test("Current Filter", function() {
    var filterListModel = new JIRA.Issues.FilterListModel({}, {searchPageModel: undefined});
    ok(filterListModel.isNewSearch("foo = bar"), "no current filter to begin with");
    var filter = new JIRA.Issues.FilterModel({jql: "foo = bar"});
    filterListModel.setCurrentFilter(filter);
    ok(!filterListModel.isNewSearch("foo = bar"), "same jql matches current filter");

});
