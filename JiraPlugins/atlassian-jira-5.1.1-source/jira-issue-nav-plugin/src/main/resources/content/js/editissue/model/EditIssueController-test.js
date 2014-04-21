AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module("EditIssueController", {
    setup: function () {

        this.editIssueController = new JIRA.Issues.EditIssueController({
            issueId: 10000,
            issueKey: "TEST-123",
            issueViewContext: jQuery("<div />"),
            issueEventBus: new JIRA.Issues.IssueEventBus()
        });

        this.editIssueController.update([
            {id: "summary", editHtml: "<div class='field-group'><input name='summary' /></div>"},
            {id: "description", editHtml: "<div class='field-group'><textarea name='description' /></div>"}
        ]);

        this.JIRAMessagesMock = sinon.mock(JIRA.Messages);
    },

    teardown: function() {
        this.JIRAMessagesMock.restore();
    }
});

test("JIRADEV-9916 Global response error messages are displayed", function () {
    var resp = {errorCollection:{
        errorMessages: ["Something went wrong."],
        errors: {}
    }};

    var spy = this.JIRAMessagesMock.expects("showErrorMsg");

    this.editIssueController._handleSaveError(this.editIssueController.getIssueId(), [], resp);

    equals(spy.callCount, 1);
    var html = spy.args[0][0];
    ok(html.indexOf("TEST-123") > 0);
    ok(html.indexOf("Something went wrong") > 0);
});

test("JRADEV-10218 I can initiate an edit (optimistic) while a save is in progress", function () {
    var summary = this.editIssueController.getFields().get("summary");
    var description = this.editIssueController.getFields().get("description");


    summary.getCurrentParams = function () {
        return {summary: "dirty"};
    };
    description.getCurrentParams = function () {
        return {description: "dirty"};
    };

    summary.edit();
    summary.save();

    description.edit();

    ok(summary.getEditing(), "Expected summary to still be editing");
    ok(description.getEditing(), "Expected description to still be editing");

    this.editIssueController._handleSaveSuccess(10000, ["summary"]);

    ok(!summary.getEditing(), "Expected [summary] to not be in edit after successful save");
    ok(description.getEditing(), "Expected description to still be editing");

    this.editIssueController._handleSaveSuccess(10000, ["description"]);

    ok(!summary.getEditing(), "Expected [summary] to not be in edit after successful save");
    ok(!description.getEditing(), "Expected [description] to not be in edit after successful save");

    summary.edit();
    description.edit();
    summary.save();
    description.save();

    ok(summary.getEditing(), "Expected [summary] to still be editing whilst saving");
    ok(description.getEditing(), "Expected [description] to still be editing whilst saving");

    this.editIssueController._handleSaveSuccess(10000, ["description", "summary"]);

    ok(!summary.getEditing(), "Expected [summary] to not be in edit after successful save");
    ok(!description.getEditing(), "Expected [description] to not be in edit after successful save");
});

test("Initiating an edit on another field causes cancel when clean", function () {
    var summary = this.editIssueController.getFields().get("summary"),
        summaryCancelSpy = sinon.spy(),
        summarySaveSpy = sinon.spy();

    summary.cancelEdit = summaryCancelSpy;
    summary.bindSave(summarySaveSpy);
    summary.edit();

    this.editIssueController.getFields().get("description").edit();

    equals(summaryCancelSpy.callCount, 1, "Expected editing to be cancelled if we click another field and it isn't dirty");
    equals(summarySaveSpy.callCount, 0, "Expected no save to occur. Field is not dirty");

});

test("Initiating an edit on another field causes save when dirty", function () {
    var summary = this.editIssueController.getFields().get("summary"),
            summaryCancelSpy = sinon.spy(),
            summarySaveSpy = sinon.spy();

    summary.cancelEdit = summaryCancelSpy;
    summary.bindSave(summarySaveSpy);
    summary.edit();

    summary.setParams({summary: "dirty"});

    this.editIssueController.getFields().get("description").edit();

    equals(summaryCancelSpy.callCount, 0, "Expected cancel to NOT occur since field is dirty");
    equals(summarySaveSpy.callCount, 1, "Expected save to occur since field is dirty");

});
