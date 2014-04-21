AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testdata");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");


module("IssueHeaderView", {
    setup: function () {
        this.issueModel = new JIRA.Issues.IssueViewModel({ entity:mockIssueJSON, issueEventBus: new JIRA.Issues.IssueEventBus() });
        this.headerView = new JIRA.Issues.IssueHeaderView({model:this.issueModel});
    }
});

test("IssueHeaderView structure", function() {

    var $el = this.headerView.render();

    var $projectAvatar = $el.find("#project-avatar");
    ok($projectAvatar.attr("src").indexOf("secure/projectavatar?pid=10000&avatarId=10011") >=0, "Project avatar image has the correct url");

    var $breadcrumbs = $el.find(".breadcrumbs");
    equal($breadcrumbs.find("#project-name-val").text(), "homosapien", "Breadcrumb has correct project name");
    equal($breadcrumbs.find("#key-val").text(), "HSP-1", "Breadcrumb has correct issue key");

    var $summary = $el.find("h1");
    equal($summary.text(), "Bug 1", "Issue summary heading is correct");

    ok($el.find(".command-bar").length > 0, "Opsbar is present");
});

test("Subtasks maintain parent issue links", function() {

    var entity = this.issueModel.getEntity();
    entity.parent = {
        id: "test-id",
        key: "TEST-1",
        summary: "parent summary"
    };

    var $el = this.headerView.render();

    var $parentSummary = $el.find("#parent_issue_summary");
    ok($parentSummary.length, "Parent link should exist");
    equal($parentSummary.text(), "TEST-1 parent summary");
    equal($parentSummary.attr("title"), "parent summary");

});
