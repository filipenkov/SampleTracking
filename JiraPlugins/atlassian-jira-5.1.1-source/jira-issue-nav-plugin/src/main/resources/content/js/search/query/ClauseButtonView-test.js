AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module('JIRA.Issues.ClauseButtonView', {
    setup: function() {
        this.$el = jQuery("<div></div>");
        jQuery("body").append(this.$el);
    },
    teardown: function() {
        this.$el.remove();
    }
});

test("Add filters button doesn't exist when there aren't any unfixed un-utilised searchers", function() {

    var searcherGroupCollection = new JIRA.Issues.SearcherCollection();

    var view = new JIRA.Issues.ClauseButtonView({
        searcherCollection: searcherGroupCollection,
        el: this.$el
    });
    view.render();
    equals(this.$el.find(".add-filter:visible").length, 0, "Add filter button shouldn't exist.");
});

test("Add filters button exists when there are unfixed un-utilised searchers", function() {

    var searcherGroupCollection = new JIRA.Issues.SearcherCollection([
        {
            id: "selectable"
        }
    ]);

    var view = new JIRA.Issues.ClauseButtonView({
        searcherCollection: searcherGroupCollection,
        el: this.$el
    });
    view.render();
    equals(this.$el.find(".add-filter:visible").length, 1, "Add filter button should exist.");

});

test("Clear filters button exists when form is dirty", function() {
    var searcherGroupCollection = new JIRA.Issues.SearcherCollection([{
        id: "test",
        jql: "test"
    }]);

    var view = new JIRA.Issues.ClauseButtonView({
        searcherCollection: searcherGroupCollection,
        el: this.$el
    });
    view.render();
    equals(this.$el.find(".clear-filters:visible").length, 1, "Clear filters button isn't visible when it should be.");

});

test("Clear filters button doesn't exist when form is not dirty", function() {
    var searcherGroupCollection = new JIRA.Issues.SearcherCollection();

    var view = new JIRA.Issues.ClauseButtonView({
        searcherCollection: searcherGroupCollection,
        el: this.$el
    });
    view.render();
    equals(this.$el.find(".clear-filters:visible").length, 0, "Clear filters button is visibl ewhen it shouldn't be.");

});
