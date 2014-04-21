AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");


module("JIRA.Issues.IssueFieldModel", {
    setup: function() {
        this.$el = jQuery("<div>initial</div>");

        this.collection = new JIRA.Issues.IssueFieldCollection();

        this.model = new JIRA.Issues.IssueFieldModel({
            id: "a",
            editHtml: "<input type=\"text\" name=\"myinput\" value=\"initial\">"
        });

        this.collection.add(this.model);
    },
    teardown: function() {
        this.$el.remove();
    }
});



test("Writes to element correctly", function() {
    this.model.switchElToEdit(this.$el);
    equals(this.model.getViewHtml().text(), "initial");
    equals(this.$el.find("[name=myinput]").length, 1)
});

test("isDirty returns false when not edited", function() {
    this.model.switchElToEdit(this.$el);
    this.model.update(this.$el);
    ok(!this.model.isDirty());
});

test("isDirty returns true when edited", function() {
    this.model.switchElToEdit(this.$el);
    this.$el.find("input").val("changed");
    this.model.update(this.$el);
    ok(this.model.isDirty());
});

test("cancelEdit sets isDirty to false", function() {
    this.model.switchElToEdit(this.$el);
    this.$el.find("input").val("changed");
    this.model.update(this.$el);
    ok(this.model.isDirty());

    this.model.cancelEdit();
    ok(!this.model.isDirty());
});


test("Only update model if we are in edit mode", function () {
    var spy = sinon.spy();
    this.model.setParams = spy;
    this.model.update();
    equals(spy.callCount, 0, "Expected model not to be updated because we aren't in edit mode");
    this.model.edit();
    this.model.update(AJS.$("<input type=\"text\" value=\"foobar\"/>"));
    equals(spy.callCount, 1, "Expected model to be updated because we are in edit mode");
});

test("Blurring field saves if dirty", function () {
    var spy = sinon.spy();
    this.model.setParams({dirtyField: "dsgasd"}); // make field dirty
    this.model.setEditing(true);
    this.model.save = spy;
    this.model.blurEdit();
    equals(spy.callCount, 1);
});

test("Blurring field cancels if not dirty", function () {
    var spy = sinon.spy();
    this.model.setEditing(true);
    this.model.cancelEdit = spy;
    this.model.blurEdit();
    equals(spy.callCount, 1);
});