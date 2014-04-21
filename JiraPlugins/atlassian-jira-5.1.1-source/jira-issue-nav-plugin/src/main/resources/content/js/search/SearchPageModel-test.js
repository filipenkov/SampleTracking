AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module("JIRA.Issues.SearchPageModel", {
    setup: function() {
        this.searchSpy = sinon.spy();
        this.searchPageModel = JIRA.Issues.TestUtils.createSearchPageModel({
                "_search": this.searchSpy
            },
            [
                {id: 1, entity: {id: 1, key: "1"}},
                {id: 2, entity: {id: 2, key: "2"}},
                {id: 3, entity: {id: 3, key: "3"}},
                {id: 4, entity: {id: 4, key: "4"}}
            ]
        );
    },
    teardown: function () {
        jQuery(".aui-loading").remove();
    }
});

test("next/prev issue", function () {
    this.searchPageModel.setRealSelectedIssue(this.searchPageModel.getIssueCollection().first());
    this.searchPageModel.next();
    ok(this.searchPageModel.getRealSelectedIssue() === this.searchPageModel.getIssueCollection().at(1));
    ok(this.searchPageModel.next() === this.searchPageModel.getIssueCollection().at(2), "Next issue should be returned");

    this.searchPageModel.next();
    this.searchPageModel.next();


    ok(this.searchPageModel.getRealSelectedIssue() === this.searchPageModel.getIssueCollection().at(3), "ceiling limit should be hit");
    ok(this.searchPageModel.getSelectedIssue() === this.searchPageModel.getIssueCollection().at(3).getEntity().key);

    ok(this.searchPageModel.prev() === this.searchPageModel.getIssueCollection().at(2), "Testing the prev isue is returned");

    this.searchPageModel.prev();
    this.searchPageModel.prev();
    this.searchPageModel.prev();
    ok(this.searchPageModel.getRealSelectedIssue() === this.searchPageModel.getIssueCollection().at(0), "Floor limit should be hit");
});

test("Selecting same issue does not trigger event", function () {
    this.searchPageModel.setRealSelectedIssue(this.searchPageModel.getIssueCollection().first());
    var calls = 0;
    this.searchPageModel.bindIssueSelected(function () {
        ++calls;
    });
    this.searchPageModel.selectIssue(this.searchPageModel.getIssueCollection().first());
    equal(calls, 0, "Events should not have been triggered");
    this.searchPageModel.selectIssue(this.searchPageModel.getIssueCollection().at(1));
    equal(calls, 1, "Events should not have been triggered");
    this.searchPageModel.selectIssue(this.searchPageModel.getIssueCollection().at(1));
    equal(calls, 1, "Events should not have been triggered");
});

test("Searching success selects correct issue", function () {

    this.searchPageModel.setSelectedIssue("2");
    this.searchPageModel._searchSuccess(this.searchPageModel.getIssueCollection(), {});
    equals(this.searchPageModel.getSelectedIssue(), "2");
    ok(this.searchPageModel.getRealSelectedIssue() === this.searchPageModel.getIssueCollection().at(1));
    this.searchPageModel.setSelectedIssue(null); // reset selected issue
    this.searchPageModel._searchSuccess(this.searchPageModel.getIssueCollection(), {});
    ok(this.searchPageModel.getRealSelectedIssue() === this.searchPageModel.getIssueCollection().first(), "No selected issue, so first should be selected");
    equals(this.searchPageModel.getSelectedIssue(), "1", "No selected issue, so first should be selected");
});

test("Searching success selects correct mode", function () {
    var calls = 0;
    this.searchPageModel.bindSwitchedToDetailMode(function () {
        calls++;
    });

    this.searchPageModel._searchSuccess(this.searchPageModel.getIssueCollection(), {});
    equals(calls, 0, "Should not switch to detailed mode unless [displayMode] is set to [detailed]");
    this.searchPageModel.setDisplayMode(this.searchPageModel.DETAILED_MODE);
    this.searchPageModel._searchSuccess(this.searchPageModel.getIssueCollection(), {});
    equals(calls, 1, "Should switch to detail mode");
    this.searchPageModel.setDisplayMode(null);
    this.searchPageModel._searchSuccess(this.searchPageModel.getIssueCollection(), {});
    equals(calls, 1, "Should not have been switched to detailed mode as display mode was changed back to null");
});

test("Switching to detail mode does not trigger more than once", function () {
    var calls = 0;
    var firstIssue = this.searchPageModel.getIssueCollection().first();
    this.searchPageModel.bindSwitchedToDetailMode(function () {
        calls++;
    });

    this.searchPageModel.setSelectedIssue(firstIssue.getEntity().key);
    this.searchPageModel.setRealSelectedIssue(firstIssue);
    this.searchPageModel.switchToDetailedView();
    equals(calls, 1, "Expected event to fire");

    equals(this.searchPageModel.getRealDisplayMode(), this.searchPageModel.DETAILED_MODE, "Expected real display mode to be set");
    this.searchPageModel.switchToDetailedView();
    equals(calls, 1, "Expected event to not fire as we are already in detailed mode");
    this.searchPageModel.backToSearch();
    equals(this.searchPageModel.getRealDisplayMode(), null, "Expected real display mode to be reset");
    equals(this.searchPageModel.getDisplayMode(), null, "Expected real display mode to be reset");
    this.searchPageModel.switchToDetailedView();
    equals(calls, 2, "Expected event to fire");
    equals(this.searchPageModel.getRealDisplayMode(), this.searchPageModel.DETAILED_MODE, "Expected real display mode to be set");
    equals(this.searchPageModel.getDisplayMode(), this.searchPageModel.DETAILED_MODE, "Expected real display mode to be set");
});

test("Searching with jql setups up the model correctly", function() {
    this.searchPageModel.searchWithFilter(new JIRA.Issues.FilterModel({ id:10000, jql:"project = MKY" }));

    this.searchPageModel.searchWithJql("project = MKY");
    this.searchPageModel.selectIssueByKey("MKY-34");

    equal(this.searchPageModel.getSelectedIssue(), "MKY-34", "Currently have an issue selected.");
    equal(this.searchPageModel.getJql(),"project = MKY", "JQL is the jql from the filter.");

    this.searchPageModel.searchWithJql("project = HSP");
    equal(this.searchPageModel.getSelectedIssue(),undefined, "Should no longer have an issue selected");
    equal(this.searchPageModel.filterListModel.getCurrentFilter(), undefined, "Active search should have been cleared!");
    equal(this.searchPageModel.getJql(), "project = HSP", "JQL is the jql that was passed in.");

    equal(this.searchSpy.callCount, 2, "Performed 1 search");

    this.searchPageModel.filterListModel.setCurrentFilter(new JIRA.Issues.FilterModel({id:10012, jql:"project = HSP"}));
    this.searchPageModel.selectIssueByKey("MKY-34");
    this.searchPageModel.searchWithJql("project = HSP");


    equal(this.searchSpy.callCount, 2, "No extra search performed if query is unchanged");

    equal(this.searchPageModel.getSelectedIssue(), "MKY-34", "Currently have an issue selected.");
    ok(this.searchPageModel.filterListModel.getCurrentFilter(), "We have an active filter.");
    equal(this.searchPageModel.getJql(), "project = HSP", "JQL is the jql from the filter.");
});

test("Searching with filter setups up the model correctly", function() {

    this.searchPageModel.setSearchMode("keyword");

    var filterModel = new JIRA.Issues.FilterModel({jql:"project = HSP"});
    this.searchPageModel.searchWithFilter(filterModel);
    equal(this.searchPageModel.getSelectedIssue(),undefined, "Should no longer have an issue selected");
    equal(this.searchPageModel.filterListModel.getCurrentFilter(), filterModel, "Active search should have been set!");
    equal(this.searchPageModel.getJql(), "project = HSP", "JQL is the jql from the filter");
    equal(this.searchPageModel.getSearchMode(), "advanced", "Search mode was switched to advanced");
    equal(this.searchSpy.callCount, 1, "A search was performed");
});


test("Back to search event sets displayMode to null", function () {
    this.searchPageModel.setDisplayMode(this.searchPageModel.DETAILED_MODE);
    equal(this.searchPageModel.getDisplayMode(), this.searchPageModel.DETAILED_MODE);
    this.searchPageModel.backToSearch();
    equal(this.searchPageModel.getDisplayMode(), null);
});


test("JRADEV-9594: We can refresh the current search in JQL mode", function () {
    this.searchPageModel.searchWithJql("project = HSP");
    this.searchPageModel.searchWithJql("project = HSP");
    equal(this.searchSpy.callCount, 2, "Two searches where performed");
});

test("JRADEV-9594: We can refresh the current search in Filter mode", function () {

    var filterModel = new JIRA.Issues.FilterModel({id:10000, jql:"project = MKY" });

    this.searchPageModel.searchWithFilter(filterModel);
    this.searchPageModel.searchWithFilter(filterModel);
    equal(this.searchSpy.callCount, 2, "Two searches where performed");
});

test("JRADEV-9231: trigger issue not found event when issue cannot be found in search results", function () {

    var spy = sinon.spy();

    this.searchPageModel.setDisplayMode(this.searchPageModel.DETAILED_MODE);
    this.searchPageModel.setSelectedIssue("blahhh");
    this.searchPageModel.bindIssueNotFound(spy);
    this.searchPageModel._searchSuccess({}, {});
    equal(spy.callCount, 1, "Event should have been fired");
    equal(this.searchPageModel.getDisplayMode(), null, "Should be in table mode, not detailed view");


    this.searchPageModel.selectIssueByKey("1");
    this.searchPageModel._searchSuccess({}, {});
    equal(spy.callCount, 1, "Event should not have been fired if we have a valid issue key");
});

test("Switching out of detailedMode fires event", function () {
    var spy = sinon.spy();
    this.searchPageModel.bindDetailedModeDismissed(spy);
    this.searchPageModel.selectIssue(1000, {
        mode: this.searchPageModel.DETAILED_MODE
    });
    this.searchPageModel.backToSearch();
    equals(1, spy.callCount, "Expected event to be fired");
});
