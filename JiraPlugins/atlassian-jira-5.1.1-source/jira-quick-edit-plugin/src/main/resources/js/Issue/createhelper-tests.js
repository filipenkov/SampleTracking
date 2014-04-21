AJS.test.require("com.atlassian.jira.jira-quick-edit-plugin:quick-create-issue");
AJS.test.require("com.atlassian.jira.jira-quick-edit-plugin:test-util");


module("Serialize For Toggling Screens", {
    setup: function () {

        var currentFormSerializedResult;
        var activeFieldIds;

        this.formStub = {
            model: {
                hasRetainFeature: function () {return true;}
            },
            getForm: function () {
                return {
                    serializeArray: function () {return currentFormSerializedResult;}
                }
            },
            getActiveFieldIds: function () {
                return activeFieldIds;
            }
        };

        this.project = {name: "pid", value: "monkey"};
        this.issueType = {name: "issuetype", value: "1"};

        this.setFormSerializeResult = function (_currentFormSerializedResult) {
            currentFormSerializedResult = _currentFormSerializedResult;
        };

        this.setActiveFieldIds = function (_activeFieldIds) {
            activeFieldIds = _activeFieldIds;
        };

        this.helper = new JIRA.Forms.CreateIssueHelper(this.formStub);
    }
});

test("Serializing when toggling excludes doesn't retain project and issuetype but does serialize", function () {
    this.setActiveFieldIds(["summary", "priority"]);
    this.setFormSerializeResult([this.project, this.issueType, {name: "summary", value: "My Summary"}, {name: "priority", value: "minor"}])
    var result = this.helper.serializeForToggle();
    ok(result.indexOf("issuetype=1") !== -1, "Should still serialize");
    equals(result.indexOf("fieldsToRetain=issuetype"), -1, "Expected issue type not to be retained");
    ok(result.indexOf("pid=monkey") !== -1, "Should still serialize");
    equals(result.indexOf("fieldsToRetain=pid"), -1, "Expected project not to be retained");
});

test("The serialized value does not change if I call it twice", function () {

    this.setActiveFieldIds(["summary", "priority"]);
    this.setFormSerializeResult([this.project, this.issueType, {name: "summary", value: "My Summary"}, {name: "priority", value: "minor"}])

    equals(this.helper.serializeForToggle(), "retainValues=true&summary=My+Summary&priority=minor&pid=monkey&issuetype=1&fieldsToRetain=summary&fieldsToRetain=priority",
            "Only priority and summary fields should be retained");

    equals(this.helper.serializeForToggle(), "retainValues=true&summary=My+Summary&priority=minor&pid=monkey&issuetype=1&fieldsToRetain=summary&fieldsToRetain=priority",
            "Only priority and summary fields should be retained");
});


test("Fields that are not active do NOT get remembered but do get serialized", function () {

    this.setActiveFieldIds(["summary", "priority"]);
    this.setFormSerializeResult([this.project, this.issueType, {name: "summary", value: "My Summary"}, {name: "priority", value: "minor"}, {name: "dontremember", value: "bogus"}])

    var result = this.helper.serializeForToggle();
    equals(result.indexOf("fieldsToRetain=dontremember"), -1, "Expected issue type not to be retained");
});

test("If I change the active fields, the previous active should still be remembered", function () {

    var result;

    this.setActiveFieldIds(["summary", "priority", "components"]);
    this.setFormSerializeResult([this.project, this.issueType, {name: "summary", value: "My Summary"}, {name: "priority", value: "minor"}, {name: "components", value: "blah"}]);
    result = this.helper.serializeForToggle();

    // ok now we change them so next time we should remember the one we lost (components)
    this.setActiveFieldIds(["summary", "priority"]);
    result = this.helper.serializeForToggle();

    ok(result.indexOf("fieldsToRetain=components") !== -1, "Expected components still to be retained");
    ok(result.indexOf("fieldsToRetain=summary") !== -1);
    ok(result.indexOf("fieldsToRetain=priority") !== -1);
    ok(result.indexOf("components=blah") !== -1, "Value should be remembered");
});

test("Currently active field values replace previously active", function () {

    var result;

    this.setActiveFieldIds(["summary", "priority", "components"]);
    this.setFormSerializeResult([this.project, this.issueType, {name: "summary", value: "My Summary"}, {name: "priority", value: "minor"}, {name: "components", value: "blah"}]);
    result = this.helper.serializeForToggle();

    // ok now we change them so next time we should remember the one we lost (components)
    this.setActiveFieldIds(["summary", "priority", "components"]);
    this.setFormSerializeResult([this.project, this.issueType, {name: "summary", value: "My Summary"}, {name: "priority", value: "minor"}, {name: "components", value: "UPDATED"}]);
    result = this.helper.serializeForToggle();

    ok(result.indexOf("components=UPDATED") !== -1, "components field value should be the updated one");
});

