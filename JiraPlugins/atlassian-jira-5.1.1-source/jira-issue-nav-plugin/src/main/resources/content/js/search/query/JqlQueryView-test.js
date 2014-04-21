AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module('JIRA.Issues.JqlQueryView', {
    setup: function() {
        this.$el = jQuery("<div></div>");

        this.searchPageModel = JIRA.Issues.TestUtils.createSearchPageModel();

        this.view = new JIRA.Issues.JqlQueryView({
            model: this.searchPageModel
        });
    },
    teardown: function() {
        this.$el.remove();
    }
});

test("Jql query view template exists", function() {
    ok(JIRA.Templates.IssueNav.jqlQueryView, "Template does not exist");
});

test("View renders something", function() {
    this.view.setElement(this.$el).render();

    equals(this.$el.find("textarea").length, 1, "View renders something");
});

test("Empty text builds jql correctly", function() {
    this.view.setElement(this.$el).render();
    this.view.search();

    equals(this.searchPageModel.getJql(), "");
});

test("Setting text builds jql correctly", function() {
    this.view.setElement(this.$el).render();

    this.$el.find("textarea").val("project = 'world domination' AND fixVersion = 6.6.6");
    this.view.search();

    equals(this.searchPageModel.getJql(), "project = 'world domination' AND fixVersion = 6.6.6");
});

test("Resetting text to empty builds jql correctly", function() {
    this.view.setElement(this.$el).render();

    this.$el.find("input").val("text ~ ohyeah");
    this.$el.find("input").val("");
    this.view.search();

    equals(this.searchPageModel.getJql(), "");
});

test("Updating jql property updates text area", function() {
    this.view.setElement(this.$el).render();
    this.searchPageModel.setJql("this is some freakin jql");

    equals(this.$el.find("textarea").val(), "this is some freakin jql");
});

test("Updating jql property before rendering succeeds", function() {
    this.searchPageModel.setJql("this is some freakin jql");
    this.view.setElement(this.$el).render();

    equals(this.$el.find("textarea").val(), "this is some freakin jql");
});
