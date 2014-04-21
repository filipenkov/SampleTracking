AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module("JIRA.Issues.SearcherCollection", {
    setup: function() {
        this.searcherCollection = new JIRA.Issues.SearcherCollection([], {
            fixedLozenges: []
        });
    }
});

test("getSearcherGroupsForAddMenu single group", function () {
    this.searcherCollection.add(new JIRA.Issues.SearcherModel({
        id: "panadol",
        groupId: "medicine"
    }));
    this.searcherCollection.add(new JIRA.Issues.SearcherModel({
        id: "mylanta",
        groupId: "medicine"
    }));

    var groups = this.searcherCollection.getSearcherGroupsForAddMenu();
    equals(groups.length, 1);
    equals(groups[0].searchers.length, 2);
    equals(groups[0].searchers[0].id, "panadol");
    equals(groups[0].searchers[1].id, "mylanta");
});

test("getSearcherGroupsForAddMenu multi group", function () {
    this.searcherCollection.add(new JIRA.Issues.SearcherModel({
        id: "panadol",
        groupId: "medicine"
    }));
    this.searcherCollection.add(new JIRA.Issues.SearcherModel({
        id: "mylanta",
        groupId: "medicine"
    }));
    this.searcherCollection.add(new JIRA.Issues.SearcherModel({
        id: "blah",
        groupId: "another group"
    }));

    var groups = this.searcherCollection.getSearcherGroupsForAddMenu();
    equals(groups.length, 2);
    equals(groups[0].searchers.length, 2);
    equals(groups[1].searchers.length, 1);
});

test("SearcherCollection.getVariableClauses", function() {
    var searcherCollection = new JIRA.Issues.SearcherCollection([], {
        fixedLozenges: [{
            id: "fixie"
        }]
    });

    searcherCollection.add(new JIRA.Issues.SearcherModel({
        id: "fixie"
    }));
    searcherCollection.add(new JIRA.Issues.SearcherModel({
        id: "no-query-string"
    }));
    searcherCollection.add(new JIRA.Issues.SearcherModel({
        id: "variable speed",
        validSearcher: true,
        editHtml: "<input type='text' name='speed' value='variable' />"
    }));

    var variableClauses = searcherCollection.getVariableClauses();
    equals(variableClauses.length, 1);
    equals(variableClauses[0].getId(), "variable speed");
});

// TODO: getSearcherGroupsForAddMenu does not return fixed lozenges
// TODO: getSearcherGroupsForAddMenu does not return already-used lozenges

test("SearcherCollection.createJql empty", function() {
    equals(this.searcherCollection.createJql(), "");
});

test("SearcherCollection.createJql 1", function() {
    this.searcherCollection.reset([{
        id: "1",
        displayValue: "some name",
        jql: "aaa = bbb"
    }]);

    equals(this.searcherCollection.createJql(), "aaa = bbb");
});

test("SearcherCollection.createJql 2", function() {
    this.searcherCollection.reset([{
        id: "1",
        jql: "aaa = bbb"
    }, {
        id: "2",
        jql: "ccc = ddd"
    }]);

    equals(this.searcherCollection.createJql(), "aaa = bbb AND ccc = ddd");
});

test("SearcherCollection.getQueryString empty", function() {
    deepEqual(this.searcherCollection.getQueryString(), "");
});

test("SearcherCollection.getQueryString text", function() {
    this.searcherCollection.setTextQuery("hello people");

    equals(this.searcherCollection.getQueryString(), "q=hello+people");
});

test("SearcherCollection.getQueryString multiple", function() {
    this.searcherCollection.add([{
        id: "wolverine",
        editHtml: "<input type='text' name='healing' value='super' /><br /><input type='text' name='claws' value='adamantium' />"
    }, {
        id: "xavier",
        editHtml: "<input type='text' name='mental' value='telepathy' />"
    }, {
        id: "magneto"  // SearcherModels with empty queryString values should not be included in the SearcherCollection's cumulative queryString.
    }]);

    equals(this.searcherCollection.getQueryString(), "healing=super&claws=adamantium&mental=telepathy");
});

test("SearcherCollection.getTextQuery", function() {
    this.searcherCollection.setTextQuery();
    equals(this.searcherCollection.getTextQuery(), "");

    this.searcherCollection.setTextQuery("");
    equals(this.searcherCollection.getTextQuery(), "");

    this.searcherCollection.setTextQuery("tacos and burritos");
    equals(this.searcherCollection.getTextQuery(), "tacos and burritos");
});

test("SearcherCollection.setTextQuery empty", function() {
    this.searcherCollection.setTextQuery();

    ok(!this.searcherCollection.get(this.searcherCollection.QUERY_ID));
});

test("SearcherCollection.setTextQuery 1", function() {
    this.searcherCollection.setTextQuery("hello");

    var queryModel = this.searcherCollection.get(this.searcherCollection.QUERY_ID);
    equals(queryModel.getDisplayValue(), "hello");
    equals(queryModel.getJql(), "text ~ \"hello\"");
});

test("SearcherCollection.setTextQuery 2", function() {
    this.searcherCollection.setTextQuery("hello people");

    var queryModel = this.searcherCollection.get(this.searcherCollection.QUERY_ID);
    equals(queryModel.getDisplayValue(), "hello people");
    equals(queryModel.getJql(), "text ~ \"hello\" AND text ~ \"people\"");
});

test("SearcherCollection.restoreFromQueryString empty", function() {
    this.searcherCollection.restoreFromQueryString({});

    ok(!this.searcherCollection.get(this.searcherCollection.QUERY_ID));
});

test("SearcherCollection.restoreFromQueryString empty", function() {
    this.searcherCollection.restoreFromQueryString({
        q: "hi"
    }, "q=hi");

    equals(this.searcherCollection.getTextQuery(), "hi");
});

// TODO: test when not only known parameters submitted

// TODO: test restoreFromQueryString with ? in front of queryString and without

test("SearcherCollection hasOnlyKnownParams", function() {

    ok(this.searcherCollection._hasOnlyKnownParams({}));

    var p = {};
    p[this.searcherCollection.QUERY_PARAM] = "it's hot on level 15 at night :(";

    ok(this.searcherCollection._hasOnlyKnownParams(p));

    ok(!this.searcherCollection._hasOnlyKnownParams({ bloody: "hot" }));
});
