AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module('JIRA.Issues.FilterListView', {
    setup: function() {
        var searchPageModel = JIRA.Issues.TestUtils.createSearchPageModel();
        this.filterData = [
            {
                id: 10000,
                name: "Sample Search 1",
                jql: "project = HSP"
            },
            {
                id: 10001,
                name: "Sample Search 2",
                jql: "project = TST"
            },
            {
                id: 10002,
                name: "Sample Search 3",
                jql: "project = HSP and type = bug"
            }
        ];
        var filterCollection = new JIRA.Issues.FilterCollection();
        filterCollection.add(this.filterData);
        this.filterListView = new JIRA.Issues.FilterListView({
            el: jQuery("<header>"),
            model: searchPageModel.filterListModel,
            collection: filterCollection
        });

    }
});

test("Dropdown shows filters", function() {
    this.filterListView.render();

    equal(this.filterListView.$(".saved-search-label").text(), "Search", "Header renders generic search name");
    equal(this.filterListView.$(".aui-list-item").length, 3, "3 filters are listed in the dropdown");
    equal(this.filterListView.$(".aui-list-item:first").text(), "Sample Search 1", "Dropdown item text is the name of a filter");
    equal(this.filterListView.model.getCurrentFilter(), undefined, "There's no current filter");

    this.filterListView.$(".aui-list-item a").first().click();

    deepEqual(this.filterListView.model.getCurrentFilter().toJSON(), this.filterData[0], "Clicking an item should set an active filter");
    equal(this.filterListView.$(".saved-search-label").text(), "Sample Search 1", "Header text is updated accordingly");
});

test("Don't render dropdown when no filters exist", function() {
    // Empty the search results collection, so we can render it with no results.
    this.filterListView.collection.reset();
    this.filterListView.render();

    equal(this.filterListView.$(".js-default-dropdown").length, 0, "Dropdown element is not rendered");
    equal(this.filterListView.$("h1").text(), "Search", "Header renders generic search name");
});
