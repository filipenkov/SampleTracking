AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module('JIRA.Issues.BasicQueryView', {
    setup: function() {
        this.$el = jQuery("<div></div>");
        this.searchPageModel = JIRA.Issues.TestUtils.createSearchPageModel();
        this.searcherCollection = new JIRA.Issues.SearcherCollection([], {
            searchPageModel: this.searchPageModel
        });
        this.view = new JIRA.Issues.BasicQueryView({
            model :this.searchPageModel,
            searcherCollection: this.searcherCollection
        });
    },
    teardown: function() {
        this.$el.remove();
    }
});

test("Template for simple query view exists", function() {
    ok(JIRA.Templates.IssueNav.basicQueryView, "Template does not exist");
});

test("View renders an input field", function() {
    this.view.setElement(this.$el).render();

    equals(this.$el.find("input[type='text']").length, 1, "View renders something");
});

test("Empty text builds jql correctly", function() {
    this.view.setElement(this.$el).render();
    this.view.search();
    equals(this.searcherCollection.getTextQuery(), "");
});

test("Setting text builds jql correctly", function() {
    this.view.setElement(this.$el).render();

    this.$el.find("input").val("jira 4ever");
    this.view.search();

    equals(this.searcherCollection.createJql(), "text ~ \"jira\" AND text ~ \"4ever\"");
});

test("Resetting text to empty builds jql correctly", function() {
    this.view.setElement(this.$el).render();

    this.$el.find("input").val("jira 4ever");
    this.view.search();
    this.$el.find("input").val("");
    this.view.search();

    equals(this.searcherCollection.getTextQuery(), "");
    equals(this.searchPageModel.getJql(), "");
});

test("Adding to query collection property updates input", function() {
    this.view.setElement(this.$el).render();
    this.searcherCollection.setTextQuery("this is a freakin query");

    equals(this.$el.find("input").val(), "this is a freakin query");
});

test("Adding to query collection property updates input", function() {
    this.view.setElement(this.$el).render();
    this.searcherCollection.setTextQuery("this is a freakin query");
    this.searcherCollection.setTextQuery("this is another freakin query");

    equals(this.$el.find("input").val(), "this is another freakin query");
});

test("Triggering a change on the searcherCollection reads the text input", function() {
    this.view.setElement(this.$el).render();
    var input = this.$el.find("input");
    input.val("This is a test search");
    this.searcherCollection.triggerCollectionChanged();
    equals(this.$el.find("input").val(), this.searcherCollection.getTextQuery());
});

test("Clearing the filters clears the input", function() {
    this.view.setElement(this.$el).render();
    var input = this.$el.find("input");
    input.val("This is a test search");
    this.searcherCollection.triggerCollectionChanged();
    equals(this.$el.find("input").val(), this.searcherCollection.getTextQuery());

    this.searcherCollection.clearSearchState();

    equals(this.$el.find("input").val(), "");
});

test("Removing from query collection property updates input", function() {
    this.view.setElement(this.$el).render();
    this.searcherCollection.remove(this.searcherCollection.QUERY_ID);

    equals(this.$el.find("input").val(), "");
});

test("Updating query property before rendering succeeds", function() {
    this.searcherCollection.setTextQuery("this is some freakin jql");
    this.view.setElement(this.$el).render();

    equals(this.$el.find("input").val(), "this is some freakin jql");
});
