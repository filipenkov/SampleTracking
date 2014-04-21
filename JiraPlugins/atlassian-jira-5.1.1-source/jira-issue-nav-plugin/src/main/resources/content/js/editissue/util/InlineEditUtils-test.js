AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testdata");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module('JIRA.Issues.InlineEditUtils', {

    setup: function () {
        this.$randomInput = jQuery("<input />").appendTo("body");
        this.$elContainer = jQuery("<div>").appendTo("body");
        this.$el = jQuery("<div>").appendTo(this.$elContainer);
        this.clock = sinon.useFakeTimers();
    },

    teardown: function () {
        this.clock.restore();
        this.$el.remove();
        this.$randomInput.remove();
    }

});

test("Blur triggers INLINE_EDIT_BLURRED", function () {
    var $input = jQuery("<input />").appendTo(this.$el).focus();
    JIRA.trigger(JIRA.Events.INLINE_EDIT_STARTED, ["priority", null, this.$el, this.$elContainer]);

    var spy = sinon.spy();
    JIRA.bind(JIRA.Events.INLINE_EDIT_BLURRED, spy);
    $input.blur();
    this.clock.tick(JIRA.Issues.InlineEdit.BLUR_FOCUS_TIMEOUT + 1);

    equals(spy.callCount, 1);
    equals(spy.args[0][1], "priority");
});

test("INLINE_EDIT_BLURRED is triggered when input is root element", function () {
    var $input = jQuery("<input>").appendTo(this.$el).focus();
    JIRA.trigger(JIRA.Events.INLINE_EDIT_STARTED, ["priority", null, $input, this.$elContainer]);

    var spy = sinon.spy();
    JIRA.bind(JIRA.Events.INLINE_EDIT_BLURRED, spy);
    $input.blur();
    this.clock.tick(JIRA.Issues.InlineEdit.BLUR_FOCUS_TIMEOUT + 1);

    equals(spy.callCount, 1);
    equals(spy.args[0][1], "priority");
});

test("INLINE_EDIT_BLURRED is NOT triggered when switching between inputs in same field", function () {
    var $inputA = jQuery("<input>").appendTo(this.$el),
        $inputB = jQuery("<input>").appendTo(this.$el);
    JIRA.trigger(JIRA.Events.INLINE_EDIT_STARTED, ["priority", null, this.$el, this.$elContainer]);
    $inputA.focus();

    var spy = sinon.spy();
    JIRA.bind(JIRA.Events.INLINE_EDIT_BLURRED, spy);
    $inputA.blur();
    $inputB.focus();
    this.clock.tick(JIRA.Issues.InlineEdit.BLUR_FOCUS_TIMEOUT + 1);

    equals(spy.callCount, 0);
});

test("INLINE_EDIT_BLURRED is triggered when all inputs in field are blurred", function () {
    var $container = jQuery("<div>").appendTo(this.$el),
        $inputA = jQuery("<input>").appendTo($container),
        $inputB = jQuery("<input>").appendTo($container);

    JIRA.trigger(JIRA.Events.INLINE_EDIT_STARTED, ["priority",null, $container, this.$elContainer]);
    $inputA.focus();

    var spy = sinon.spy();
    JIRA.bind(JIRA.Events.INLINE_EDIT_BLURRED, spy);
    $inputA.blur();
    $inputB.focus();
    $inputB.blur();
    this.clock.tick(JIRA.Issues.InlineEdit.BLUR_FOCUS_TIMEOUT + 1);

    equals(spy.callCount, 1);
    equals(spy.args[0][1], "priority");
});

test("INLINE_EDIT_BLURRED is NOT triggered when inputs are blurred, but container has focus", function () {
    var $container = jQuery("<div>").appendTo(this.$el),
        $input = jQuery('<input>').appendTo($container);
    
    JIRA.trigger(JIRA.Events.INLINE_EDIT_STARTED, ["priority", null, $container, this.$elContainer]);
    $input.focus();

    var spy = sinon.spy();
    JIRA.bind(JIRA.Events.INLINE_EDIT_BLURRED, spy);
    $input.blur();
    $container.focus();
    this.clock.tick(JIRA.Issues.InlineEdit.BLUR_FOCUS_TIMEOUT + 1);

    equals(spy.callCount, 0);
});

test("INLINE_EDIT_BLURRED handlers are called with appropriate fieldId argument", function () {
    var $inputA = jQuery("<input>").appendTo(this.$el);
    var $inputB = jQuery("<input>").appendTo(this.$el);

    var spyFocused = sinon.spy();
    var spyBlurred = sinon.spy();

    JIRA.bind(JIRA.Events.INLINE_EDIT_FOCUSED, spyFocused);
    JIRA.bind(JIRA.Events.INLINE_EDIT_BLURRED, spyBlurred);

    JIRA.trigger(JIRA.Events.INLINE_EDIT_STARTED, ["summary", null, $inputA, this.$elContainer]);
    $inputA.focus();
    $inputA.blur();

    JIRA.trigger(JIRA.Events.INLINE_EDIT_STARTED, ["priority", null, $inputB, this.$elContainer]);
    $inputB.focus();
    $inputB.blur();

    this.clock.tick(JIRA.Issues.InlineEdit.BLUR_FOCUS_TIMEOUT + 1);

    equal(spyFocused.getCall(0).args[1], "summary", "Focused field is identified correctly");
    equal(spyBlurred.getCall(0).args[1], "summary", "Blurred field is identified correctly");

    equal(spyFocused.getCall(1).args[1], "priority", "Focused field is identified correctly");
    equal(spyBlurred.getCall(1).args[1], "priority", "Blurred field is identified correctly");
});
