AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testdata");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");

module('JIRA.Issues.IssueView', {
    setup: function() {
        this.el = AJS.$("<div class='result-panel'></div>").appendTo("body");
        this.issueModel = new JIRA.Issues.IssueViewModel({
            entity:mockIssueJSON,
            issueEventBus: new JIRA.Issues.IssueEventBus()
        });
        this.view = new JIRA.Issues.IssueView({
            model: this.issueModel,
            el:this.el
        });
    },
    teardown: function() {
        this.el.remove();
    }
});

test("Ensure issue view sets status correctly depending on the issue", function() {
    ok(!this.el.hasClass("status-open"), "Ensure the view element doesn't have a status color yet");

    this.view.render();

    ok(this.el.hasClass("status-open"), "After rendering we should have a status color");

    this.issueModel.getEntity().status.name = "Reopened";
    this.view.render();

    ok(!this.el.hasClass("status-open"), "Element should no longer have the open status");
    ok(this.el.hasClass("status-reopened"), "Instead we should now have the reopened status");
});

test("Clicking Issue Action performs save", function () {
    var spy = sinon.spy();
    this.issueModel.getIssueEventBus().bind("save", spy);
    this.view.$el.append("<a href='#' class='issueaction-something' id='edit-issue'/>");
    this.view.$el.find("#edit-issue").click();
    equals(spy.callCount, 1, 'Expected clicking edit issue to trigger a save');
});


/* TODO
also add an error case you want to test that it remains in edit mode on an invalid response (not sure why this is todo)
test("Model saving events call correct methods", function () {

    var editingStartedSpy = sinon.spy();
    var saveCompleteSpy = sinon.spy();
    var saveStartedSpy = sinon.spy();

    this.view = new (JIRA.Issues.IssueView.extend({
        handleEditingStarted: editingStartedSpy,
        handleSaveComplete: saveCompleteSpy,
        handleSaveStarted: saveStartedSpy
    }))({
        model: this.issueModel, el: this.el
    });

    this.issueModel.triggerEditingStarted();
    equals(editingStartedSpy.callCount, 1);
    this.issueModel.triggerSaveStarted();
    equals(saveStartedSpy.callCount, 1);
    this.issueModel.triggerSaveAndReloadComplete();
    this.issueModel.triggerSaveError();
    equals(saveCompleteSpy.callCount, 2);
});
*/
