AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:backbone-ext");

module("JIRA.Issues.EventsMixinCreator");

test("Bind and trigger are added to model", function() {
    var MaleModel = JIRA.Issues.BaseModel.extend({
        namedEvents: ["think"]
    });

    var zoolander = new MaleModel();

    ok(zoolander.bindThink);
    ok(zoolander.triggerThink);

    var triggered = false;
    var handler = function() {
        triggered = true;
    };
    zoolander.bindThink(handler);

    zoolander.triggerThink();
    ok(triggered);
});

test("Binding when no events specified succeeds", function() {
    var MaleModel = JIRA.Issues.BaseModel.extend();

    var hansel = new MaleModel();
    hansel.bind("derelique");
});

test("Triggering when no events specified succeeds", function() {
    var MaleModel = JIRA.Issues.BaseModel.extend();

    var hansel = new MaleModel();
    hansel.trigger("derelique");
});

test("Mixin can apply events to model with no events", function() {
    var MyMixin = {
        namedEvents: ["someEvent"]
    };
    var MyModel = JIRA.Issues.BaseModel.extend({
        mixins: [MyMixin]
    });
    var myModel = new MyModel();
    deepEqual(myModel.namedEvents, ["someEvent"]);
    ok(myModel.bindSomeEvent);
    ok(myModel.triggerSomeEvent);
});

test("Mixin can apply events to model with some events", function() {
    var MyMixin = {
        namedEvents: ["someEvent"]
    };
    var MyModel = JIRA.Issues.BaseModel.extend({
        mixins: [MyMixin],
        namedEvents: ["someOtherEvent"]
    });
    var myModel = new MyModel();
    deepEqual(myModel.namedEvents, ["someOtherEvent", "someEvent"]);
    ok(myModel.bindSomeOtherEvent);
    ok(myModel.triggerSomeOtherEvent);
    ok(myModel.bindSomeEvent);
    ok(myModel.triggerSomeEvent);
});
