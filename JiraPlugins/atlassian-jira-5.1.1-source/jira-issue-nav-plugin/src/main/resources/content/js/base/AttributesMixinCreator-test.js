AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:backbone-ext");

test("Getter and setter are added to model", function() {
    var MaleModel = JIRA.Issues.BaseModel.extend({
        properties: ["look"]
    });

    var zoolander = new MaleModel();

    ok(zoolander.getLook);
    ok(zoolander.setLook);

    zoolander.setLook("Blue Steel");
    equals(zoolander.get("look"), "Blue Steel");

    equals(zoolander.getLook(), "Blue Steel");
});

test("ID property is always added to properties", function() {
    var MaleModel = JIRA.Issues.BaseModel.extend({
        properties: ["look"]
    });

    var zoolander = new MaleModel();

    deepEqual(zoolander.properties, ["id", "look"]);
    ok(zoolander.getId);
    ok(zoolander.setId);
});

test("Does not barf when user specifies id", function() {
    var MaleModel = JIRA.Issues.BaseModel.extend({
        properties: ["id", "look"]
    });

    var zoolander = new MaleModel();

    deepEqual(zoolander.properties, ["id", "look"]);
    ok(zoolander.getId);
    ok(zoolander.setId);
});

test("ID property is not added if model does not specify properties", function() {
    var MaleModel = JIRA.Issues.BaseModel.extend();

    var zoolander = new MaleModel();

    ok(!zoolander.properties);
    ok(!zoolander.getId);
    ok(!zoolander.setId);
});

test("Setting properties that does not exist fails", function() {
    var MaleModel = JIRA.Issues.BaseModel.extend({
        properties: ["look"]
    });

    var zoolander = new MaleModel();
    raises(function() {
        zoolander.set({
            mer: "man"
        });
    });
});

test("Setting property that does not exist fails", function() {
    var MaleModel = JIRA.Issues.BaseModel.extend({
        properties: ["look"]
    });

    var zoolander = new MaleModel();
    raises(function() {
        zoolander.set("mer", "man");
    });
});

test("Getting property that does not exist fails", function() {
    var MaleModel = JIRA.Issues.BaseModel.extend({
        properties: ["look"]
    });

    var zoolander = new MaleModel();
    raises(function() {
        zoolander.get("eugooglize");
    });
});

test("Setting any property when no property exists succeeds", function() {
    var MaleModel = JIRA.Issues.BaseModel.extend();

    var hansel = new MaleModel();
    hansel.set("derelique");
});

test("Getting any property when no property exists succeeds", function() {
    var MaleModel = JIRA.Issues.BaseModel.extend();

    var hansel = new MaleModel();
    hansel.get("derelique");
});

test("Mixin can apply properties to model with no properties", function() {
    var MyMixin = {
        properties: ["someProperty"]
    };
    var MyModel = JIRA.Issues.BaseModel.extend({
        mixins: [MyMixin]
    });
    var myModel = new MyModel();
    deepEqual(myModel.properties, ["id", "someProperty"]);
    ok(myModel.getSomeProperty);
    ok(myModel.setSomeProperty);
});

test("Mixin can apply properties to model with some properties", function() {
    var MyMixin = {
        properties: ["someProperty"]
    };
    var MyModel = JIRA.Issues.BaseModel.extend({
        mixins: [MyMixin],
        properties: ["someOtherProperty"]
    });
    var myModel = new MyModel();
    deepEqual(myModel.properties, ["id", "someOtherProperty", "someProperty"]);
    ok(myModel.getSomeOtherProperty);
    ok(myModel.setSomeOtherProperty);
    ok(myModel.getSomeProperty);
    ok(myModel.setSomeProperty);
});
