// Standalone server rendered view issue
AJS.$(function () {

    var saveInProgressManager = new JIRA.Issues.SaveInProgressManager();
    var viewIssueController = new JIRA.Issues.ViewIssueController({
        viewIssueContext: jQuery(".content-container").find(".result-panel"),
        saveInProgressManager: saveInProgressManager
    });

    viewIssueController.applyToDom({
        key: AJS.$("#key-val").text(),
        id: AJS.$("#key-val").attr("rel"),
        statusColorSupport: false
    });

//    JIRA.Issues.overrideIssueApi(viewIssueController);
    JIRA.Issues.overrideIssueDialogs(viewIssueController);

    JIRA.Issues.Api = JIRA.Issues.Api || {};
    JIRA.Issues.Api.openFocusShifter = function () {
        viewIssueController.getSelectedIssueModel().getIssueEventBus().triggerOpenFocusShifter();
    };

    JIRA.Issues.Api.hasSavesInProgress = function () {
        return saveInProgressManager.hasSavesInProgress();
    };

});

// Standalone dynamically loaded view issue
//AJS.$(function () {
//
//    var saveInProgressManager = new JIRA.Issues.SaveInProgressManager();
//    var viewIssueController = new JIRA.Issues.ViewIssueController({
//        viewIssueContext: jQuery(".result-panel"),
//        saveInProgressManager: saveInProgressManager
//    });
//
//    viewIssueController.load({
//        key: AJS.Meta.get("issueKey"),
//        id: AJS.Meta.get("issueId")
//    });
//
//    JIRA.Issues.overrideIssueApi(viewIssueController);
//    JIRA.Issues.overrideIssueDialogs(viewIssueController)
//
//});