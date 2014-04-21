AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:common");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");

module("JIRA.Issues.IssueViewModel", {
    setup: function() {
        this.el = jQuery("<div />");
        this.issueModel = new JIRA.Issues.IssueViewModel();

    },
    teardown: function() {
    }
});

test("GetViewPanels correctly inserts description panel", function() {
    var issueModel = new JIRA.Issues.IssueViewModel();

    issueModel.update({issue:{}, panels:{leftPanels:[{id:"details-module"}, {id:"activity"}], rightPanels:[], infoPanels:[]}}, {editable:true});

    var panels = issueModel.getPanels();
    equals(panels.getLeftPanels().length, 3, "Should have inserted description panel");
    equals(panels.getLeftPanels()[1].getEntity().id, "descriptionmodule", "Second panel is the description panel");

    //test that if there is already a desc panel we shouldn't insert a new one!
    var issueWithDescModel = new JIRA.Issues.IssueViewModel();
    issueWithDescModel.update({issue:{}, panels:{leftPanels:[{id:"details-module"},{id:"descriptionmodule", html:"foobar"}, {id:"activity"}], rightPanels:[], infoPanels:[]}}, {editable:true});
    panels = issueWithDescModel.getPanels();
    equals(panels.getLeftPanels().length, 3, "Should not have inserted additional description panel");
    equals(panels.getLeftPanels()[1].getEntity().id, "descriptionmodule", "Second panel is the description panel");
    equals(panels.getLeftPanels()[1].getEntity().html, "foobar", "Second panel has the correct html");

    //test that if the description is not editable we shouldn't insert one!
    var issueNotEditableModel = new JIRA.Issues.IssueViewModel();
    issueNotEditableModel.update({issue:{}, panels:{leftPanels:[{id:"details-module"}, {id:"activity"}], rightPanels:[], infoPanels:[]}}, {editable:false});

    panels = issueNotEditableModel.getPanels();
    equals(panels.getLeftPanels().length, 2, "Should not have inserted description panel");
    equals(panels.getLeftPanels()[0].getEntity().id, "details-module", "First panel should be details");
    equals(panels.getLeftPanels()[1].getEntity().id, "activity", "Second panel should be activity");
});
