AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testdata");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module('JIRA.Issues.IssueFieldView', {
    setup: function() {
        this.$el = jQuery("<div><a href='#'><span class='link-child'>initial</span><strong class='twixi'></strong></a></div>").appendTo("body");
        this.collection = new JIRA.Issues.IssueFieldCollection();
        this.collection.add({
            id: "summary",
            editHtml: "<div class='field-group'><input type=\"text\" name=\"myinput\" value=\"initial\"></div>"
        });
        this.model = this.collection.at(0);
        this.view = new JIRA.Issues.IssueFieldView({
            issueEventBus: new JIRA.Issues.IssueEventBus(),
            model: this.model,
            el: this.$el
        });

        this.assertInEditMode = _.bind(function () {
            equals(this.model.getEditing(), true, "Model says we are editing");
            ok(this.$el.find("[name=myinput]"), "Input element exists");
            equals(this.$el.hasClass("inactive"), false, "Inactive class is not present on the element");
        }, this);

        this.assertInReadMode = _.bind(function () {
            equals(!!this.model.getEditing(), false, "Model says we are not editing");
            equals(this.$el.find("span").html(), "initial", "Span exists");
            equals(this.$el.hasClass("inactive"), true, "Inactive class is present on the element");
        }, this);
    },
    teardown: function() {
        this.$el.remove();
    }
});

test("Readonly mode is default state", function () {
    this.assertInReadMode();
});

asyncTest("when readonly view is clicked, we switch to edit", function () {
    this.$el.click();
    var tester = this;
    setTimeout(function() {
        tester.assertInEditMode();
        start();
    }, 400);
});

test("when model is set to editing, so is the view", function () {
    this.model.edit();
    this.assertInEditMode();
});

test("when model is set to editing, inline edit started event is fired", function () {
    var spy = sinon.spy();
    JIRA.bind(JIRA.Events.INLINE_EDIT_STARTED, spy);
    this.model.edit();
    equals(spy.callCount, 1);
    equals(spy.args[0][1], "summary");
    var $inputEls = spy.args[0][3].children();
    equals($inputEls.length, 1);
    ok($inputEls.is(".field-group"));
});

test("when model is set to editing, inline edit receives multiple elements", function() {
    this.model.setEditHtml("<input type=\"text\" name=\"myinput1\" ><input type=\"text\" name=\"myinput2\" ><input type=\"text\" name=\"myinput3\" >");
    var spy = sinon.spy();
    JIRA.bind(JIRA.Events.INLINE_EDIT_STARTED, spy);
    this.model.edit();
    equals(spy.callCount, 1);
    var $inputEls = spy.args[0][3].children();
    equals($inputEls.length, 3);
});

test("When cancel event is triggered on collection we switch to readonly mode", function () {
    this.model.edit(); // switch to edit
    this.collection.cancelEdit();
    this.assertInReadMode();
});

test("Update causes params to be updated in model", function () {
    this.model.edit(); // switch to edit
    this.$el.find(":input").val("My New Val");
    this.model.update(this.$el);
    deepEqual(this.model.getParams(), {myinput: "My New Val"});
});

test("Cancel is triggered either by ESC or via onCancel call", function() {

    this.model.edit(); // switch to edit
    var spy = sinon.spy();

    this.model.cancelEdit = spy;
    var e = jQuery.Event("keydown");
    e.keyCode = 50;
    this.$el.trigger(e);
    equals(spy.callCount, 0, "Random keydown shouldn't cancel the edit.");
    e = jQuery.Event("keydown");
    e.keyCode = 27;
    this.$el.trigger(e);
    equals(spy.callCount, 1, "ESC should cancel the edit.");

    this.model.edit({}); // switch to edit

    this.$el.find(".cancel").click();
    equals(spy.callCount, 2, "Clicking on cancel should also cancel the edit.");
});

test("Validation error in model switches view to edit mode", function () {
    this.model.setValidationError("<input/>", "An error occur");
    equals(this.view.$el.find(":text").length, 1, "Expected field to be shown");
    equals(this.model.getEditing(), true, "Expected model to be in edit mode");
});

test("Validation error should be appended", function () {
    this.model.setValidationError("<input/><div class=\"error\">An error occur</div>", "An error occur");
    equals(this.view.$el.find(".error").length, 1, "Expected only on error message");
    equals(this.view.$el.find(".error").text(), "An error occur");
});

asyncTest("Clicking on link doesn't enter edit", function () {
    var spy = sinon.spy();
    var $el = this.$el;
    this.model.edit = spy;
    $el.find("span.link-child").click();
    setTimeout(function() {
        equals(spy.callCount, 0, "Expected edit not to be called because we clicked a child of a link");
        $el.find("summary").click();
        setTimeout(function() {
            equals(spy.callCount, 0, "Expected edit not to be called because we clicked a link");
            $el.click();
            setTimeout(function() {
                equals(spy.callCount, 1, "Expected edit to to be called because we DIDN'T click a link");
                start();
            }, 400);
        }, 400);
    }, 400);
});

asyncTest("No duplicate INLINE_EDIT_FOCUSED and INLINE_EDIT_BLURRED events are fired", function() {

    var focusedSpy = sinon.spy();
    var blurredSpy = sinon.spy();
    var blurDelay = JIRA.Issues.InlineEdit.BLUR_FOCUS_TIMEOUT + 1;

    JIRA.bind(JIRA.Events.INLINE_EDIT_FOCUSED, focusedSpy);
    JIRA.bind(JIRA.Events.INLINE_EDIT_BLURRED, blurredSpy);

    this.model.edit();

    var $input = this.$el.find("input");
    $input.focus();
    $input.focus();

    setTimeout(function() {
        $input.blur();
        setTimeout(function() {
            $input.blur();
            setTimeout(function() {
                equals(focusedSpy.callCount, 1, "Only one INLINE_EDIT_FOCUSED event fired");
                equals(blurredSpy.callCount, 1, "Only one INLINE_EDIT_BLURRED event fired");
                start();
            }, blurDelay);
        }, blurDelay);
    }, 0);
});

test("Clicking on element that prevents default doesn't enter edit", function () {
    var spy = sinon.spy();
    var handlerRan = false;

    this.model.edit = spy;

    this.$el.click(function (e) {
        handlerRan = true;
        e.preventDefault();
    });
    this.$el.find(".twixi").click();
    ok(handlerRan);
    equals(spy.callCount, 0, "Expected edit not to be called because we clicked a child of a link");
});

test("Cancel on blur when clean", function () {
    var cancelSpy = sinon.spy(),
        saveSpy = sinon.spy();

    this.model.cancelEdit = cancelSpy;
    this.model.bindSave(saveSpy);
    this.model.edit();

    JIRA.trigger(JIRA.Events.INLINE_EDIT_FOCUSED, ["summary"]);
    JIRA.trigger(JIRA.Events.INLINE_EDIT_BLURRED, ["summary"]);

    equals(cancelSpy.callCount, 1, "Expected cancel to occur since field is clean");
    equals(saveSpy.callCount, 0, "Expected save to NOT occur since field is clean");
});

test("Save on blur when dirty", function () {
    var cancelSpy = sinon.spy(),
        saveSpy = sinon.spy();

    this.model.cancelEdit = cancelSpy;
    this.model.bindSave(saveSpy);
    this.model.edit();
    this.$el.find('input').val('dirty');

    JIRA.trigger(JIRA.Events.INLINE_EDIT_FOCUSED, ["summary"]);
    JIRA.trigger(JIRA.Events.INLINE_EDIT_BLURRED, ["summary"]);

    equals(cancelSpy.callCount, 0, "Expected cancel to NOT occur since field is dirty");
    equals(saveSpy.callCount, 1, "Expected save to occur since field is dirty");
});

test("Blurring twice does not trigger two saves", function () {
    var cancelSpy = sinon.spy(),
        saveSpy = sinon.spy();

    this.model.cancelEdit = cancelSpy;
    this.model.bindSave(function () {
        // mock out what the EditIssueController would do
        this.handleSaveStarted();
    });
    this.model.bindSave(saveSpy);
    this.model.edit();
    this.$el.find('input').val('dirty');

    JIRA.trigger(JIRA.Events.INLINE_EDIT_FOCUSED, ["summary"]);
    JIRA.trigger(JIRA.Events.INLINE_EDIT_BLURRED, ["summary"]);
    JIRA.trigger(JIRA.Events.INLINE_EDIT_BLURRED, ["summary"]);

    equals(cancelSpy.callCount, 0, "Expected cancel to NOT occur since field is dirty");
    equals(saveSpy.callCount, 1, "Expected save to occur only once");
});

test("Save or cancel is not triggered when save options gain focus", function() {
    var clock = sinon.useFakeTimers(),
        cancelSpy = sinon.spy(),
        saveSpy = sinon.spy();

    this.model.cancelEdit = cancelSpy;
    this.model.bindSave(saveSpy);
    this.model.edit();
    this.$el.find('input').val('dirty');

    this.$el.find("input").focus();
    this.$el.find("input").blur();
    this.$el.find('.save-options button:first').focus();

    clock.tick(JIRA.Issues.InlineEdit.BLUR_FOCUS_TIMEOUT + 1);

    equals(cancelSpy.callCount, 0, "Expected cancel to NOT occur since save options has focus");
    equals(saveSpy.callCount, 0, "Expected save to NOT occur since save options has focus");

    clock.restore();
});

test("Save or cancel is not triggered when an input regains focus from save options", function() {
    var clock = sinon.useFakeTimers(),
        cancelSpy = sinon.spy(),
        saveSpy = sinon.spy();

    this.model.cancelEdit = cancelSpy;
    this.model.bindSave(saveSpy);
    this.model.edit();
    this.$el.find('input').val('dirty');
    this.$el.find('.save-options button:first').focus();
    this.$el.find('.save-options button:first').blur();
    this.$el.find('input').focus();

    clock.tick(JIRA.Issues.InlineEdit.BLUR_FOCUS_TIMEOUT + 1);

    equals(cancelSpy.callCount, 0, "Expected cancel to NOT occur since save options has focus");
    equals(saveSpy.callCount, 0, "Expected save to NOT occur since save options has focus");

    clock.restore();
});

test("reveal is triggered on both edit and focus", function() {
    var revealSpy = sinon.spy();
    this.$el.bind("reveal", revealSpy);
    this.view.switchToEdit();
    equals(revealSpy.callCount, 1);
    this.view.focus();
    equals(revealSpy.callCount, 2);

});

test("JRA-28241 can prevent submit through event", function () {
    var cancelSpy = sinon.spy(),
        saveSpy = sinon.spy();

    this.model.cancelEdit = cancelSpy;
    this.model.bindSave(saveSpy);
    this.model.edit();
    this.$el.find('input').val('dirty');

    this.$el.find("form").bind("before-submit", function (e) {
        e.preventDefault();
    });

    this.$el.find("form").submit();

    equals(cancelSpy.callCount, 0, "Expected cancel to NOT occur since save options has focus");
    equals(saveSpy.callCount, 0, "Expected save to NOT occur since save options has focus");
});

asyncTest("JRADEV-12018 - Inline edit is not enabled when event is transmitted from within a parent with class uneditable", function () {
    var spy = sinon.spy();
    var $el = this.$el;
    this.model.edit = spy;
    var uneditableElement = jQuery("<div class='uneditable'>This is an uneditable div</div>");
    $el.append(uneditableElement);
    uneditableElement.click();
    setTimeout(function() {
        equals(spy.callCount, 0, "Expected edit not to be called because we clicked an uneditable element");
        start();
    }, 400);
});