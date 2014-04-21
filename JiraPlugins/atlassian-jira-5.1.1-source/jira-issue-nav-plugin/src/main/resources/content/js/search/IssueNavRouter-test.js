AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module('JIRA.Issues.IssueNavRouter', {
    setup: function() {
        this.clock = sinon.useFakeTimers();
        this.server = sinon.fakeServer.create();

        this.searchPageModel = JIRA.Issues.TestUtils.createSearchPageModel();

        this.searcherCollection = new JIRA.Issues.SearcherCollection();

        this.issueCollectionSearchSpy = this.searchPageModel.getIssueCollection().search;

        this.issueNavRouter = JIRA.Issues.TestUtils.createIssueNavRouter({
            searchPageModel: this.searchPageModel,
            searcherCollection: this.searcherCollection
        });

        this.verifyUrl = _.bind(function(url) {
            this.clock.tick(1); // tick to increment time as router uses settimeout to debounce calls
            var spy = this.issueNavRouter.navigate;
            equals(spy.callCount, 1, "Navigate was called once");
            equals(spy.firstCall.args[0], url, "Navigated to correct url");
        }, this);
    },
    teardown: function() {
        this.server.restore();
        this.clock.restore();
    }
});


test("Search URL with nada defaults to keyword search", function() {
    this.searcherCollection.restoreFromQueryString = sinon.mock().returns(AJS.$.Deferred().resolve());

    this.issueNavRouter.fakeLoad("/");

    equals(this.searchPageModel.get("searchMode"), "keyword");
    equals(this.issueCollectionSearchSpy.callCount, 1, "Search should not be called once query is not specified to deliver 'all results'");
});

test("Search URL with keyword search", function() {
    this.searcherCollection.restoreFromQueryString = sinon.mock().returns(AJS.$.Deferred().resolve());

    this.issueNavRouter.fakeLoad("keyword/", true);

    equals(this.searchPageModel.get("searchMode"), "keyword");
    equals(this.issueCollectionSearchSpy.callCount, 1, "Search should be called once if query is not specified to deliver 'all results'");
});

test("Search URL with keyword and query", function() {
    this.searcherCollection.restoreFromQueryString = sinon.mock().returns(AJS.$.Deferred().resolve());

    this.issueNavRouter.fakeLoad("keyword/?q=some%20thing", true);

    equals(this.searchPageModel.getSearchMode(), "keyword");
    equals(this.searcherCollection.restoreFromQueryString.callCount, 1);
    deepEqual(this.searcherCollection.restoreFromQueryString.getCall(0).args[0], { q: "some thing" });
    equals(this.searcherCollection.restoreFromQueryString.getCall(0).args[1], "?q=some thing");
    equals(this.issueCollectionSearchSpy.callCount, 1, "Search should be called if query is specified");
});

test("Search URL with keyword and empty query", function() {
    this.searcherCollection.restoreFromQueryString = sinon.mock().returns(AJS.$.Deferred().resolve());

    this.issueNavRouter.fakeLoad("keyword/?q=", true);

    equals(this.searchPageModel.getSearchMode(), "keyword");
    equals(this.searcherCollection.restoreFromQueryString.callCount, 1);
    deepEqual(this.searcherCollection.restoreFromQueryString.getCall(0).args[0], { q: "" });
    equals(this.searcherCollection.restoreFromQueryString.getCall(0).args[1], "?q=");
    equals(this.issueCollectionSearchSpy.callCount, 1, "Search should be called if query is specified");
});

// TODO: query with keyword + project param, mock out server response using sinon

test("Search URL with search mode", function() {
    this.issueNavRouter.fakeLoad("advanced", true);
    equals(this.searchPageModel.get("searchMode"), "advanced");
    equals(this.issueCollectionSearchSpy.callCount, 1, "Search should be called once if jql is not specified with 'all results'");
});

test("Search url with advanced search mode and jql", function() {
    this.issueNavRouter.fakeLoad("advanced/?jql=project%20%3D%20foo", true);

    equals(this.searchPageModel.get("searchMode"), "advanced");
    equals(this.searchPageModel.get("jql"), "project = foo");
    equals(this.issueCollectionSearchSpy.callCount, 1, "Search should be called if jql is specified");
});

test("Search URL with advanced search and jql and issue", function() {
    this.issueNavRouter.fakeLoad("advanced/?jql=project%20%3D%20foo&issue=FOO-2", true);

    equals(this.searchPageModel.get("searchMode"), "advanced");
    equals(this.searchPageModel.get("jql"), "project = foo");
    equals(this.searchPageModel.get("selectedIssue"), "FOO-2");
    equals(this.issueCollectionSearchSpy.callCount, 1, "Search should be called if jql is specified");
});

test("Search URL with advanced search and empty query", function() {
    this.issueNavRouter.fakeLoad("advanced/?jql=", true);

    equals(this.searchPageModel.get("searchMode"), "advanced");
    equals(this.searchPageModel.get("jql"), "");
    equals(this.issueCollectionSearchSpy.callCount, 1, "Search should be called if jql is specified");
});

test("Search URL with empty query and issue", function() {
    this.issueNavRouter.fakeLoad("advanced/?jql=&issue=FOO-2");

    equals(this.searchPageModel.get("searchMode"), "advanced");
    equals(this.searchPageModel.get("jql"), "");
    equals(this.searchPageModel.get("selectedIssue"), "FOO-2");
    equals(this.issueCollectionSearchSpy.callCount, 1, "Search should be called if jql is specified");
});


test("Issue view mode defaults to null", function () {
    this.issueNavRouter.fakeLoad("advanced/?jql=&issue=FOO-2");

    equals(this.searchPageModel.get("searchMode"), "advanced");
    equals(this.searchPageModel.get("jql"), "");
    equals(this.searchPageModel.get("selectedIssue"), "FOO-2");
    equals(this.searchPageModel.get("displayMode"), undefined);
});

test("Issue view mode is set", function () {
    this.issueNavRouter.fakeLoad("advanced/?jql=&issue=FOO-2&displayMode=detailed");

    equals(this.searchPageModel.get("searchMode"), "advanced");
    equals(this.searchPageModel.get("jql"), "");
    equals(this.searchPageModel.get("selectedIssue"), "FOO-2");
    equals(this.searchPageModel.get("displayMode"), "detailed");
});

test("Changing search mode to advanced updates url", function() {
    this.searchPageModel.switchToSearchMode("advanced");

    this.verifyUrl("advanced/");
});

test("Empty search works in advanced more", function () {
    this.searchPageModel.switchToSearchMode("advanced");
    this.searchPageModel.searchWithJql("");
    this.verifyUrl("advanced/?jql=");
});

test("Changing jql in advanced mode updates url", function() {
    this.searchPageModel.switchToSearchMode("advanced");
    this.searchPageModel.searchWithJql("blah");

    this.verifyUrl("advanced/?jql=blah");
});

test("Selecting issue in advanced mode updates url", function() {
    this.searchPageModel.getIssueCollection().reset([{
        id:"123",
        entity: {
            id: "123",
            key: "ABC-123"
        }
    }]);
    this.searchPageModel.setSearchMode("advanced");
    this.searchPageModel.searchWithJql("blah")

    this.searchPageModel.selectIssueByKey("ABC-123");

    this.verifyUrl("advanced/?jql=blah&issue=ABC-123");
});

test("Changing search mode to keyword updates url", function() {
    this.searchPageModel.switchToSearchMode("advanced");

    this.searchPageModel.switchToSearchMode("keyword");

    this.verifyUrl("keyword/");
});

test("Entering keyword search updates url", function() {
    this.searcherCollection.setTextQuery("sometext");
    this.searchPageModel.searchWithJql((this.searcherCollection.createJql()));

    this.verifyUrl("keyword/?q=sometext");
});

test("Entering multi keyword search updates url", function() {
    this.searcherCollection.setTextQuery("sometext");

    this.searcherCollection.add({
        id: "project",
        editHtml: "<input type='text' name='project' value='runway' />"
    });
    this.searchPageModel.searchWithJql((this.searcherCollection.createJql()));

    this.verifyUrl("keyword/?q=sometext&project=runway");
});

test("Routing works correctly when we need to go back to the server for JQL", function () {

    var queryString = "keyword/?q=&pid=10340&issue=STY-4";

    var spy = sinon.spy();

    this.searchPageModel.updateFromRouter = spy;

    var res = {
        searchers: [],
        values: {
            project: {
                jql: "project = \"STY\""
            }
        }
    };

    this.server.respondWith("GET", /.*Search.?.+/,
        [200, { "Content-Type": "application/json" }, JSON.stringify(res)]);


    this.issueNavRouter.search("keyword", queryString);

    this.server.respond();

    ok(spy.calledWith({
        selectedIssue: "STY-4",
        displayMode: undefined,
        jql: "project = \"STY\"",
        searchMode: "keyword"
    }));
});

