AJS.test.require("com.atlassian.jira.jira-quick-edit-plugin:quick-create-issue");
AJS.test.require("com.atlassian.jira.jira-quick-edit-plugin:test-util");

test("visibility feature only for optional fields", function () {

    var model = new JIRA.QuickForm.CreateIssueModel();

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

test("updating field resource", function () {

    var model = new JIRA.QuickForm.CreateIssueModel();

    assertInvocationUsedAjax("Expected setting of issue type to invoke an ajax request to refresh fields",
        function () {
            model.setIssueType(10000);
        });

     assertInvocationUsedAjax("Expected setting of issue type to invoke an ajax request to refresh fields",
         function () {
            model.setProjectId(99999);
        });

    equals(model.getFieldsResource(), "/secure/QuickCreateIssue!default.jspa?decorator=none&issuetype=10000&pid=99999");

    assertInvocationUsedAjax("Expected setting of issue type to invoke an ajax request to refresh fields",
        function () {
            model.setIssueType(500);
        });

     assertInvocationUsedAjax("Expected setting of project to invoke an ajax request to refresh fields",
         function () {
            model.setProjectId(1);
        });

    equals(model.getFieldsResource(), "/secure/QuickCreateIssue!default.jspa?decorator=none&issuetype=500&pid=1");
});


test("getConfigurableFields", function () {

    var model = new JIRA.QuickForm.CreateIssueModel();

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

    var model = new JIRA.QuickForm.CreateIssueModel();

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
