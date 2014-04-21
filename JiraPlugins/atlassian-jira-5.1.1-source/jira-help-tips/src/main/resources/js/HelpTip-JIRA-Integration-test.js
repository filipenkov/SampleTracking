AJS.test.require("com.atlassian.plugins.helptips.jira-help-tips:common");
AJS.test.require("com.atlassian.plugins.helptips.jira-help-tips:sinon");

module('integration with JIRA', {
    setup: function() {
        AJS.$.fx.off = true;
        this.clock = sinon.useFakeTimers();
    }, teardown: function() {
        this.clock.restore();
        AJS.$.fx.off = false;
    }
});

test('open anchored tips do not stop dialogs from being closed', function() {
    var a = AJS.$("<a>thing</a>").appendTo(AJS.$("#qunit-fixture"));
    var content = AJS.$("<div>I'm a dialog!</div>");
    var dialog = new JIRA.Dialog({content: content});
    var tip = new AJS.HelpTip({body:"wheeeeee!", anchor:a});
    tip.show();
    this.clock.tick(0);
    ok(tip.isVisible());

    dialog.show();
    ok(dialog.$popup.is(":visible"), "dialog is visible");
    ok(tip.isVisible(), "tip is still visible after open of dialog");

    dialog.hide();
    ok(!dialog.$popup.is(":visible"), "dialog closed fine while tip was open");
    ok(tip.isVisible(), "tip is still visible after close of dialog");
});
