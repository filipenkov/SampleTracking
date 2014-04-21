AJS.test.require("com.atlassian.jira.jira-quick-edit-plugin:quick-create-issue");
AJS.test.require("com.atlassian.jira.jira-quick-edit-plugin:test-util");

test("visibility feature only for optional fields", function () {

    var model = new JIRA.Forms.CreateIssueModel();

    ok(!model.hasVisibilityFeature({
        editHtml: "<input />",
        label: "blah",
        required: true
    }), "Expected required fields NOT to have visibility feature");

    ok(model.hasVisibilityFeature({
        editHtml: "<input />",
        label: "blah"
    }), "Expected optional fields to have visibility feature");

});

test("getConfigurableFields", function () {

    var model = new JIRA.Forms.CreateIssueModel();

    model.setFields([
        {id: "project"},
        {id: "issuetype"},
        {id: "components"}
    ]);

    model.getConfigurableFields().done(function (fields) {
        deepEqual(fields, [{id: "components"}], "Expected project & issueType fields to be excluded as configurable fields");
    });
});


test("getIssueSetupFields", function () {

    var model = new JIRA.Forms.CreateIssueModel();

    model.setFields([
        {id: "project"},
        {id: "issuetype"},
        {id: "components"}
    ]);

    model.getIssueSetupFields().done(function (fields) {
        deepEqual(fields, [{id: "project"}, {id: "issuetype"}],
            "Expected project & issueType fields to be excluded as configurable fields");
    });
});
