AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");

module('JIRA.Issues.FixedLozengeView', {
    setup: function() {
        this.searcherCollection = new JIRA.Issues.SearcherCollection();
        this.model = new JIRA.Issues.FixedLozengeModel({
            id: "test-id"
        });
        this.view = new JIRA.Issues.FixedLozengeView({
            model: this.model,
            searcherCollection: this.searcherCollection
        });
        this.server = sinon.fakeServer.create();
    },
    teardown: function() {
        AJS.$(".ajs-layer").remove();
        this.server.restore();
    }
});

test("Lozenge view renders correctly", function() {
    this.view.render();

    ok(this.view.$el.hasClass("lozenge"), "Lozenge has correct class");
    // TODO: I took out the anchor as i couldn't see a point to having a url that just returns json ...
    // var anchor = this.$el.find('li.lozenge a');
    // equals(anchor.attr("href"), "/test/content", "Lozenge has correct href");
});

test("Inline dialog renders clause editHtml if already loaded", function() {
    this.searcherCollection.add({
        id: "test-id",
        editHtml: "<div class='myEditHtml'></div>"
    });
    this.model.setSearcher(this.searcherCollection.get("test-id"));

    this.view.render();
    this.view.$el.click();
    equals(this.view.dialog.$layer.find(".myEditHtml").length, 1);
});

test("Inline dialog default renderer displays content returned from server", function() {
    this.searcherCollection.add({
        id: "test-id"
    });
    this.model.setSearcher(this.searcherCollection.get("test-id"));

    var content = '<p>my content</p>';

    this.view.render();
    this.server.respondWith("GET", /.*SearchRendererEdit.*/,
        [200, {},
            content]);

    this.view.$el.click();
    this.server.respond();

    equals(this.view.dialog.$layer.find("form").length, 1 ,"Inline dialog rendered into a form");
    equals(this.view.dialog.$layer.find("form p").text(),"my content", "Inline dialog contained response from server");
});
