AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:common");

module("JIRA.Issues.Mixin.SingleSelect", {

    setup: function() {
        this.collection = new (JIRA.Issues.BaseCollection.extend());

        this.modelClass = JIRA.Issues.BaseModel.extend({
            properties: ["selected"],
            mixins: [JIRA.Issues.Mixin.SingleSelect]
        });
        this.model = new this.modelClass({
            collection: this.collection
        });
    }
});

test("SingleSelect selectById", function() {
    this.collection.add([{
        id: "rick"
    }, {
        id: "vyvyan"
    }]);
    this.model.selectById("vyvyan");
    equals(this.model.getSelected().id, "vyvyan");
});

test("SingleSelect selectAt", function() {
    this.collection.add([{
        id: "rick"
    }, {
        id: "vyvyan"
    }]);
    this.model.selectAt(1);
    equals(this.model.getSelected().id, "vyvyan");
});

test("SingleSelect clearSelection", function() {
    this.collection.add([{
        id: "rick"
    }]);
    this.model.clearSelection();
    ok(!this.model.getSelected());
});

test("SingleSelect next", function() {
    this.collection.add([{
        id: "rick"
    }, {
        id: "vyvyan"
    }]);
    this.model.selectById("rick");
    this.model.next();
    equals(this.model.getSelected().id, "vyvyan");
});

test("SingleSelect next loops to start", function() {
    this.collection.add([{
        id: "rick"
    }, {
        id: "vyvyan"
    }]);
    this.model.selectAt(1);
    this.model.next();
    equals(this.model.getSelected().id, "rick");
});

test("SingleSelect next selects first when nothing is selected", function() {
    this.collection.add([{
        id: "rick"
    }, {
        id: "vyvyan"
    }]);
    this.model.clearSelection();
    this.model.next();
    equals(this.model.getSelected().id, "rick");
});

test("SingleSelect next succeeds when empty", function() {
    this.model.next();
    ok(!this.model.getSelected());
});

test("SingleSelect prev", function() {
    this.collection.add([{
        id: "rick"
    }, {
        id: "vyvyan"
    }]);
    this.model.selectAt(1);
    this.model.prev();
    equals(this.model.getSelected().id, "rick");
});


test("SingleSelect prev loops to end", function() {
    this.collection.add([{
        id: "rick"
    }, {
        id: "vyvyan"
    }]);
    this.model.selectAt(0);
    this.model.prev();
    equals(this.model.getSelected().id, "vyvyan");
});

test("SingleSelect prev selects last when nothing is selected", function() {
    this.collection.add([{
        id: "rick"
    }, {
        id: "vyvyan"
    }]);
    this.model.clearSelection();
    this.model.prev();
    equals(this.model.getSelected().id, "vyvyan");
});

test("SingleSelect last succeeds when empty", function() {
    this.model.prev();
    ok(!this.model.getSelected());
});
