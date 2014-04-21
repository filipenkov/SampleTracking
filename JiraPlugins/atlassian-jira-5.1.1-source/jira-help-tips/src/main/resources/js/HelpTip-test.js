AJS.test.require("com.atlassian.plugins.helptips.jira-help-tips:common");
AJS.test.require("com.atlassian.plugins.helptips.jira-help-tips:sinon");

test('existence of the feature can be asserted', function() {
    ok(AJS.HelpTip, "the AJS.HelpTip object should exist (and be truthy)");
});

test('can create new tips', function() {
    var tip = new AJS.HelpTip();
    equal(typeof tip, "object", "should be an object");
});

test('get a unique client id (cid)', function() {
    var ids = [];
    for(var i=0; i<20; i++) {
        ids.push((new AJS.HelpTip()).cid);
    }
    equal(AJS.$.unique(ids).length, ids.length, "should be as many unique ids as tips created");
});

test('can be programmatically dismissed', function() {
    var tip = new AJS.HelpTip({body:"whee"});
    ok(!tip.isDismissed(), "has not been dismissed yet");
    tip.dismiss();
    ok(tip.isDismissed(), "should be dismissed");
});

test('can bind dismissal of tip to other elements', function() {
    var tip = new AJS.HelpTip({body:"whee"});
    var anchor = AJS.$("<a>click me to dismiss</a>");
    anchor.click(function() { tip.dismiss() });
    anchor.trigger('click');
    ok(tip.isDismissed(), "should be dismissed after clicking the anchor link");
});

// TODO: This isn't a pure unit test. Split out the components.
test('tips with the same id have a linked dismissed value', function() {
    var stub = sinon.stub(AJS.HelpTip.Manager, "sync");

    var tip1 = new AJS.HelpTip({id:"testtip",body:"firsttip"});
    var tip2 = new AJS.HelpTip({id:"testtip",body:"secondtip"});
    tip1.dismiss();
    ok(tip2.isDismissed(), "tip2 is considered dismissed because tip1 was.");

    stub.restore();
});

test('tips with no id are considered different', function() {
    var tip1 = new AJS.HelpTip({body:"firsttip"});
    var tip2 = new AJS.HelpTip({body:"secondtip"});
    tip1.dismiss();
    ok(!tip2.isDismissed(), "tip2 is different to tip1 as neither have the same id.");
});

module('anchored tip', {
    setup: function() {
        AJS.$.fx.off = true;
        this.fakeSync = sinon.stub(AJS.HelpTip.Manager, "sync");
        this.fakeShow = sinon.stub(AJS.HelpTip.Manager, "show", function(showTip) { showTip(); });
        this.clock = sinon.useFakeTimers();

        this.box = AJS.$("<div></div>").appendTo(AJS.$("#qunit-fixture"));
        this.anchor = AJS.$("<a>test</a>").appendTo(this.box);
        this.tip = new AJS.HelpTip({body:"test " + new Date(),anchor:this.anchor});
    }, teardown: function() {
        this.box.remove();
        this.tip = null;
        this.clock.restore();
        this.fakeSync.restore();
        this.fakeShow.restore();
        AJS.$.fx.off = false;
    }
});

test('can be anchored to an element', function() {
    this.tip.show();
    this.clock.tick(0);
    ok(this.tip.isVisible(), "tip should be visible");
});

test('cannot be dismissed by clicking outside the tip', function() {
    this.tip.show();
    this.clock.tick(0);
    AJS.$(document.body).trigger('click');
    ok(this.tip.isVisible(), "tip should still be visible");
    ok(!this.tip.isDismissed(), "not dismissed yet");
});

test('tip disappears when dismissed', function() {
    this.tip.show();
    this.clock.tick(0);
    this.tip.dismiss();
    this.clock.tick(0);
    ok(!this.tip.isVisible(), "should no longer be visible");
});

test('can click a close button to dismiss', function() {
    this.tip.show();
    this.clock.tick(0);
    var button = AJS.$(".helptip-close", this.tip.view.$el); // TODO: probably a better way to get this.
    ok(button.length, "there should be a button on the anchored tip to dismiss it");
    ok(!this.tip.isDismissed(), "is not yet dismissed");
    button.trigger('click');
    this.clock.tick(0);
    ok(this.tip.isDismissed(), "should be dismissed after clicking the close button");
});

test('once dismissed, cannot be re-opened', function() {
    this.tip.show();
    this.clock.tick(0);
    this.tip.dismiss();
    this.clock.tick(0);
    this.tip.show();
    ok(!this.tip.isVisible(), "should not be visible once closed");
});

module('persistence', {
    setup: function() {
        var requests = this.requests = [];
        this.xhr = sinon.useFakeXMLHttpRequest();
        this.xhr.onCreate = function(xhr) {
            requests.push(xhr);
        }
    }, teardown: function() {
        this.xhr.restore();
    }
});

test('sends nothing to the server if no ID was set', function() {
    var tip = new AJS.HelpTip({body:"I have no ID-ah!"});
    tip.dismiss();
    equal(this.requests.length, 0, "no request was made to the server.");
});

test('POSTs the dismissal of the tip to the server', function() {
    var tip = new AJS.HelpTip({id:"testtip2",body:"I am a test."});
    tip.dismiss();
    equal(this.requests.length, 1, "A request was sent to the server");
    equal(this.requests[0].requestBody, JSON.stringify({id:"testtip2"}));
});
