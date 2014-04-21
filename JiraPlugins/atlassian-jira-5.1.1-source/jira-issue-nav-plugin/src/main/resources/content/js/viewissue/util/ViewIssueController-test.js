AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");


module("JIRA.Issue.ViewIssueController", {
    setup: function () {
        this.issueEventBus = new JIRA.Issues.IssueEventBus();
        this.saveInProgressManager = new JIRA.Issues.SaveInProgressManager();
        this.editIssueController = new JIRA.Issues.EditIssueController({
            issueEventBus: this.issueEventBus
        });
        this.selectedIssueModel = new JIRA.Issues.IssueViewModel({id: 1000, entity: {id: 1000, key: "HSP-1"}, issueEventBus: this.issueEventBus});
        this.viewIssueController = new JIRA.Issues.ViewIssueController({
            selectedEditIssueController: this.editIssueController,
            selectedIssueModel: this.selectedIssueModel,
            saveInProgressManager: this.saveInProgressManager
        });
    },
    teardown: function () {
        this.viewIssueController.destroy();
        jQuery(".global-msg").remove();
        AJS.undim();
    }
});

test("Edits in progress return string on unload", function () {
    equals(this.viewIssueController._handleUnload(), undefined);
    this.editIssueController.getDirtyEditsInProgress = function () {
        return ["summary"]
    };
    equals(typeof this.viewIssueController._handleUnload(), "string");
});

test("Errors are handled globally for modals", function () {
    var spy = sinon.spy();
    AJS.dim(); // modal
    this.issueEventBus.bindSaveError(spy);
    this.saveInProgressManager.triggerSaveError(1000, ["summary"], {errorCollection:{errors: ["Summary was empty"], errorMessages: []}});
    ok(jQuery(".global-msg").text().indexOf("HSP-1") !== -1, "Expected error message to be about HSP-1");
    ok(jQuery(".global-msg").text().indexOf("Summary") !== -1, "Expected error message to be the summary field");
    equals(spy.callCount, 0, "Expected edit issue controller not to handle error");
});

test("Errors are handled globally if NOT selected issue", function () {
    // Selected issue is 1000
    var spy = sinon.spy();
    var unhandledErrorSpy = sinon.spy();
    this.issueEventBus.bindSaveError(spy);
    this.viewIssueController.bindUnhandledSaveError(unhandledErrorSpy);
    this.saveInProgressManager.triggerSaveError(1001, ["summary"], {errorCollection:{errors: ["Summary was empty"], errorMessages: []}});
    equals(spy.callCount, 0, "Expected edit issue controller not to handle error");
    equals(unhandledErrorSpy.callCount, 1, "Expected unhandled error to trigger event");
});

test("Errors are handled by edit controller if selected issue", function () {
    var spy = sinon.spy();
    this.issueEventBus.bindSaveError(spy);
    this.saveInProgressManager.triggerSaveError(1000, ["summary"], {errorCollection:{errors: ["Summary was empty"], errorMessages: []}, fields: [], panels: {}, issue: {}});
    ok(jQuery(".global-msg").length === 0, "Expected no global error message");
    equals(spy.callCount, 1, "Expected edit issue controller to handle error");
});

