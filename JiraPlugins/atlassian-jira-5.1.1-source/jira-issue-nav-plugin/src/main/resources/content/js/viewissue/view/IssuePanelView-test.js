AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testdata");

module('JIRA.Issues.IssuePanelView', {
    setup: function() {
        this.el = AJS.$("<div></div>");
        this.issuePanelModel = new JIRA.Issues.IssuePanelModel({entity: mockWebPanelRefPluginPanel});
        this.issuePanelView = new JIRA.Issues.IssuePanelView({el:this.el, model: this.issuePanelModel, issueEventBus: new JIRA.Issues.IssueEventBus()});
    },
    teardown: function() {
        this.el.remove();
    }
});

test("Issue Web panel renders correctly with no header", function() {
    var issuePanelModel = new JIRA.Issues.IssuePanelModel({entity: mockWebPanelNoHeader});
    var issuePanelView = new JIRA.Issues.IssuePanelView({el:this.el, model: issuePanelModel});

    var $el = issuePanelView.render();

    //make sure that we don't render a header on the client-side.
    equal($el.find(".mod-header").length, 0, "No heading means no mod-header");

    //this is actually all stuff that's in the html returned by the module.
    ok($el.is("#addcomment"), "Rendered element contains correct id");
    equal($el.find(".mod-content").length, 1, "Rendered element contains mod-content");
    equal($el.find("#footer-comment-button").length, 1, "Rendered element contains module contents");
});

test("Issue Web panel renders correctly with header", function() {
    var issuePanelModel = new JIRA.Issues.IssuePanelModel({entity: mockWebPanelWithHeader});
    var issuePanelView = new JIRA.Issues.IssuePanelView({el:this.el, model: issuePanelModel});

    var $el = issuePanelView.render();

    //make sure that we do render a header on the client-side.
    ok($el.hasClass("toggle-wrap"));
    equal($el.find(".mod-header h3:contains(Details)").length, 1, "Header is rendered");

    //this is actually all stuff that's in the html returned by the module.
    equal($el.find(".mod-content #issuedetails").length, 1, "Rendered element contains correct mod-content");
});

test("Issue Web panel from the reference plugin renders correctly", function() {
    var issuePanelModel = new JIRA.Issues.IssuePanelModel({entity: mockWebPanelRefPluginPanel});
    var issuePanelView = new JIRA.Issues.IssuePanelView({el:this.el, model: issuePanelModel});

    var $el = issuePanelView.render();
    AJS.$("body").append($el);
    JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [$el]);

    //make sure that the header has the correct controls
    var $ops = $el.find("ul.ops");
    equals($ops.length, 1, "Operations list is present");
    var $opsList = $ops.children("li");
    equals($opsList.length, 3, "Correct number of operations is present");
    equals(AJS.$($opsList[0]).find("a.icon-add16").length, 1, "First operation is correct");
    equals(AJS.$($opsList[1]).find("a.icon-edit-sml").length, 1, "Second operation is correct");
    equals(AJS.$($opsList[2]).find("a.drop-menu").length, 1, "Third operation is a dropdown");


    var $dropdown = $ops.find(".aui-dropdown-content");
    equal($dropdown.children("ul.aui-list-section").length, 3, "Dropdown contains correct subgroups");
    equal($dropdown.children("h5:contains(Drop Section)").length, 1, "Dropdown contains correct subheading");


    //check opening the dropdown works
    equals(AJS.$("#refViewIssue-drop-add-link:visible").length, 0, "Dropdown is currently closed");
    var $ddTrigger = AJS.$($opsList[2]).find("a.drop-menu");
    $ddTrigger.click();
    equals(AJS.$("#refViewIssue-drop-add-link:visible").length, 1, "Dropdown is currently open");

    //cleanup (should close the dropdown)
    $ddTrigger.click();
});

test("JRADEV-10219: Only updating sections of panel that are not in edit", function () {

    this.issuePanelView.render();
    this.issuePanelModel.update(mockWebPanelRefPluginPanelUpdated, {
        fieldsSaved: ["summary", "components"],
        fieldsInProgress: ["versions"]
    });


    equals(this.issuePanelView.$el.find("#summary-val").text(), "Summary Updated");
    equals(this.issuePanelView.$el.find("#versions-val").text(), "");
    equals(this.issuePanelView.$el.find("#components-val").text(), "Components Updated");
});

test("JRADEV-10219: Update entire panel if no fields in edit", function () {
    this.issuePanelView.render();

    this.issuePanelModel.update(mockWebPanelRefPluginPanelUpdated, {
        fieldsSaved: ["summary"],
        fieldsInProgress: []
    });

    equals(this.issuePanelView.$el.find("#changed-body").text(), "Some changed body text");
    equals(this.issuePanelView.$el.find("#summary-val").text(), "Summary Updated");
    equals(this.issuePanelView.$el.find("#versions-val").text(), "Versions Updated");
    equals(this.issuePanelView.$el.find("#components-val").text(), "Components Updated");
});