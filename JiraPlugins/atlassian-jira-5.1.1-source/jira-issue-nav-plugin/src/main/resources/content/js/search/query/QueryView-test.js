AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module('JIRA.Issues.QueryView', {
    setup: function() {
        this.el = AJS.$("<form></form>");

        this.searchPageModel = JIRA.Issues.TestUtils.createSearchPageModel();

        this.searcherCollection = new JIRA.Issues.SearcherCollection([], {
            searchPageModel: this.searchPageModel

        });
        
        this.queryView = new JIRA.Issues.QueryView({
            el: this.el,
            model: this.searchPageModel,
            searcherCollection: this.searcherCollection
        });

        this.queryView.render();
    },

    teardown: function() {
        this.el.remove();
    }
});

test("Model is updated when the switcher is changed", function() {
    this.queryView.switcherView.switchEl.trigger(new jQuery.Event("click"));

    equal(this.searchPageModel.getSearchMode(), "advanced");
});

test("Models are updated when the keyword search form is submitted", function() {
    var query = "some query";
    this.el.find("input").val(query);
    this.el.trigger(new jQuery.Event("submit"));

    equal(this.searcherCollection.getQueryString(), "q=some+query", "Search Results Models query params are updated");
    equal(this.searcherCollection.createJql(), "text ~ \"some\" AND text ~ \"query\"", "Search Results Models jql property and textarea's jql value are identical.");
});

test("Model jql property is updated when the advanced search form is submitted", function() {
    this.searchPageModel.setSearchMode("advanced");
    var query = "a new test query";
    this.el.find("textarea").val(query);
    this.el.trigger(new jQuery.Event("submit"));

    equal(this.searchPageModel.getJql(), query, "Search Results Models jql property and textarea's jql value are identical.");
});

// TODO move the guts of this test into SearchPageModel-test or
test("Performing JQL search resets current filter", function() {
    this.searchPageModel.setSearchMode("advanced");
    var filterModel = new JIRA.Issues.FilterModel({
        id:10020,
        name: "Some Filter",
        jql: "project = HSP"
    });
    this.searchPageModel.searchWithFilter(filterModel);

    this.el.find("textarea").val("project = HSP");
    this.queryView.search(new jQuery.Event("keydown"));
    equal(filterModel, this.searchPageModel.filterListModel.getCurrentFilter(), "When the JQL is the same as the current active filter, we should still have an active filter.")

    this.el.find("textarea").val("project = HSP and type = bug");
    this.queryView.search(new jQuery.Event("keydown"));
    equal(undefined, this.searchPageModel.filterListModel.getCurrentFilter(), "JQL was different from the old search so we now no longer have an active search");
    equal("project = HSP and type = bug", this.searchPageModel.getJql(), "JQL should have been set to the input");

    this.el.find("textarea").val("project = MKY");
    this.queryView.search(new jQuery.Event("keydown"));
    equal(undefined, this.searchPageModel.filterListModel.getCurrentFilter(), "Active Saved Search should still be undefined since there was none to start with!");
    equal("project = MKY", this.searchPageModel.getJql(), "JQL should have been set to the input");
});

test("When the model triggers an error even the view should render them", function() {
    this.queryView.render();

    equal(0, this.el.find(".notifications .aui-message").length, "No errors so far");
    this.searchPageModel._searchError({}, {responseText: JSON.stringify({errorMessages:["Something bad happened."]})});
    equal(1, this.el.find(".notifications .aui-message").length, "Should have one error now.");
    equal("Something bad happened.", this.el.find(".notifications .aui-message").text(), "Correct error is shown");

    var jsonParseError = false;
    try {
        this.searchPageModel._searchError({}, {responseText: "{invalid JSON}"});
    } catch(error) {
        jsonParseError = true;
    }
    ok(!jsonParseError, "Should not have thrown an exception!");
    equal(2, this.el.find(".notifications .aui-message").length, "Should have two errors now.");

});
