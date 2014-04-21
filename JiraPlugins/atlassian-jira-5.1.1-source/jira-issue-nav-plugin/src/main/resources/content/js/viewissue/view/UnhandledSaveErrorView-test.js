AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");

module('JIRA.Issues.UnhandledSaveErrorView', {
    setup: function () {
        this.view = new JIRA.Issues.UnhandledSaveErrorView();
    }
});

test("Is issue accessible", function () {
    var options = {
        response: {
            fields: [{}]
        }
    };
    equal(this.view._isAccessible(options), true, "Response with non-empty fields array is an accessible issue");

    options = {
        response: {
            fields: []
        }
    };
    equal(this.view._isAccessible(options), false, "Response with empty fields array is NOT an accessible issue");

    options = {
        response: {

        }
    };
    equal(this.view._isAccessible(options), false, "Response with missing fields property is NOT an accessible issue");
});

test("Get template based on resumability", function () {
    var options = {
        attemptedSavedIds: ["a"],
        response: {
            fields: [{}, { id:"a", editHtml: "html" }]
        }
    };
    equal(this.view._getTemplate(options), "resumableSaveErrorMessage", "Response with editHtml for attempted save field");

    options = {
        attemptedSavedIds: ["a"],
        response: {
            fields: [{ id: "a" }]
        }
    };
    equal(this.view._getTemplate(options), "saveErrorMessage", "Response without editHtml for attempted save field");

    options = {
        attemptedSavedIds: ["a"],
        response: {
            fields: []
        }
    };
    equal(this.view._getTemplate(options), "saveErrorMessage", "Response with empty fields array");
});