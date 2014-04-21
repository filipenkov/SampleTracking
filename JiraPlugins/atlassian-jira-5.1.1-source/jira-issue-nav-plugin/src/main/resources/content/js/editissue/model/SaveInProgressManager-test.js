AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module("JIRA.Issues.SaveInProgressManager", {
    setup: function() {
        this.saveInProgressManager = new JIRA.Issues.SaveInProgressManager();
    }
});

test("savesInProgress is updated correctly", function() {

    var saveInProgress1 = {};
    var saveInProgress2 = {};

    ok(!this.saveInProgressManager.hasSavesInProgress(), "Should not have any saves in progress when initialized");
    this.saveInProgressManager.addSaveInProgress(saveInProgress1);
    this.saveInProgressManager.addSaveInProgress(saveInProgress2);
    ok(this.saveInProgressManager.hasSavesInProgress(), "We should have 2 saves in progress");
    this.saveInProgressManager.removeSaveInProgress(saveInProgress2);
    this.saveInProgressManager.removeSaveInProgress(saveInProgress1);
    ok(!this.saveInProgressManager.hasSavesInProgress(), "We should have no saves in progress");
    this.saveInProgressManager.addSaveInProgress(saveInProgress2);
    ok(this.saveInProgressManager.hasSavesInProgress());
});