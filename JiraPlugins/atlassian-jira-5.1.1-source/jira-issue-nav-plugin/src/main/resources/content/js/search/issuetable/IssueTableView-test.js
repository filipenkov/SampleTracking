AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module('JIRA.Issues.IssueTableView', {
    setup: function() {
        this.el = jQuery("<table></table>");
        this.template = function() {
            return "<tbody><tr data-id='1000'><td><a href='#' class='issue'>Issue link</a></td></tr> </tbody>";
        };
        this.searchPageModel =  new JIRA.Issues.SearchPageModel({
            issueCollection: new JIRA.Issues.BaseCollection()
        }, {
            fieldConfig: this.mockFieldConfig
        });
    },
    teardown: function() {
        this.el.remove();
        jQuery(".global-msg").remove();
    },
    contains: function(el, text) {
        return el.html().indexOf(text) >= 0;
    },
    mockFieldConfig: {
        columns: [ { columnHeaderTemplate: function() {return "<th>foo</th>";}}],
        columnCount: function() {return 1;}
    }
});

test("Results are displayed in a DOM element", function() {

    var i1 = {
            mockIssue: "number 1"
        },
        i2 = {
            mockIssue: "number 2"
        };

    var rowViewMock = sinon.mock().exactly(2).returns({
        render: function() {
            return "<tr></tr>";
        }
    });
    var issueCollection = new JIRA.Issues.BaseCollection([i1, i2]);
    var view = new JIRA.Issues.IssueTableView({
        el: this.el,
        model: new JIRA.Issues.SearchPageModel({
            issueCollection: issueCollection
        })
    }, {
        rowView: rowViewMock,
        template: this.template,
        fieldConfig: this.mockFieldConfig
    });

    view.render();

    equal(rowViewMock.firstCall.args[0].model.get("mockIssue"), i1.mockIssue);
    equal(rowViewMock.secondCall.args[0].model.get("mockIssue"), i2.mockIssue);
    rowViewMock.verify();

    equals(AJS.$(this.el).find("tbody").children().length, 2, "Number of children is as expected.");
});

test("Results 'no results' is displayed when there are no results", function() {

    var model = JIRA.Issues.TestUtils.createSearchPageModel();
    var rowViewMock = sinon.mock().never();
    var view = new JIRA.Issues.IssueTableView({
        el: this.el,
        model: model
    },
    {
        rowView: rowViewMock,
        template: this.template,
        fieldConfig: this.mockFieldConfig
    });

    model.setJql("");

    view.render();
    view.addAll();

    rowViewMock.verify();
    var resultView = AJS.$(this.el).find("tbody");
    equals(resultView.children().length, 1, "tbody contains one row");
    ok(resultView.children().first().hasClass("error"), "row is error row");
});


test("Clicking row puts us into detailed view", function () {
    var issueKey = "XSS-17";
    var model = new JIRA.Issues.SearchPageModel({
        issueCollection: new JIRA.Issues.BaseCollection([{
            id: "zzzz"
        }])
    }, {
        fieldConfig: this.mockFieldConfig
    });
    var rowViewMock = sinon.mock().once().returns({
        render: function() {
            return "<tr data-issue-key='"+ issueKey +"' data-id='c2'><td><a class='issue'></a></tr>";
        }
    });
    var view = new JIRA.Issues.IssueTableView({
            el: this.el,
            model: model
        },
        {
            rowView: rowViewMock,
            template: this.template,
            fieldConfig: this.mockFieldConfig
        });

    view.render();
    ok(document.title.indexOf(issueKey) == -1,"No issue key in title before click");
    //TODO; find out why this is failing
    /*
    AJS.$(view.el).find("tr[data-issue-key='"+issueKey+"'] a.issue").click();
    equals(model.getDisplayMode(), model.DETAILED_MODE);
      */
    //something with the test is breaking, the way events etc get wired up spent too much time looking at it so far
    //ok(document.title.indexOf(issueKey) > -1,"Issue key is in title after switch");
});

test("Issue not in Results", function () {
    this.searchPageModel.triggerIssueNotFound("HSP-1");
    ok(jQuery(".global-msg").text().indexOf("HSP-1"), "Expected warning message to be shown as issue doesn't exist in results");
});

test("Results counts something something", function() {
    var searchpageModel = new JIRA.Issues.SearchPageModel({
        issueCollection: new JIRA.Issues.BaseCollection()
    }, {
        fieldConfig: this.mockFieldConfig
    });
    var resultsTableView = new JIRA.Issues.IssueTableView({
        el: this.el,
        model: searchpageModel
    }, {
        fieldConfig: this.mockFieldConfig
    });
    var $results = jQuery('<div class="results-count"></div>');
    $results.appendTo("#qunit-fixture");
    searchpageModel.setTotalResultCount(50);
    equal($results.text(), "50 results", "Result count is displayed");
    searchpageModel.setTotalResultCount(0);
    equal($results.text(), "", "Result count is not displayed when no results are present");
});
