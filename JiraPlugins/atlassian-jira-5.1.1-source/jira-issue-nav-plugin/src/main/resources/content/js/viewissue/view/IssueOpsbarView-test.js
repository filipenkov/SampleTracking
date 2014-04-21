AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testdata");


module('JIRA.Issues.IssueOpsbarView', {
    setup: function() {
        this.el = AJS.$("<div><a href='#' id='savebar-cancel'></a><a href='#' id='savebar-save'></a></div>");
        this.issueModel = new JIRA.Issues.IssueViewModel({ entity:mockIssueJSON });
        this.opsbarView = new JIRA.Issues.IssueOpsbarView({el:this.el, model:this.issueModel});
    },
    teardown: function() {
        this.el.remove();
    },

    groupContainsLinks: function(group, linkIds) {
        var $links = AJS.$(group).find(".toolbar-item>a");
        equal($links.length, linkIds.length, "Group has correct number of links");
        $links.each(function(i, link) {
            equal(AJS.$(link).attr("id"), linkIds[i], "Link " + linkIds[i] + " is correct");
        });
    }
});

test("Issue Opsbar top level groups", function() {
    var $el = this.opsbarView.render();

    equal($el.find(".toolbar-split").length, 2, "The opsbar is split into lhs and rhs toolbars");
});

test("Issue Opsbar LHS groups", function() {
    var $el = this.opsbarView.render();

    var leftGroups = $el.find(".toolbar-split-left .toolbar-group .toolbar-group");
    equal(leftGroups.length, 3, "LHS toolbar is split into 3 subgroups");

    var editGroup = leftGroups[0];
    this.groupContainsLinks(editGroup, ["edit-issue"]);

    var issueOperationsGroup = leftGroups[1];
    this.groupContainsLinks(issueOperationsGroup, ["assign-issue", "comment-issue"]);

    var $opsDropdown = AJS.$(issueOperationsGroup).find("#opsbar-operations_more");
    ok($opsDropdown.length > 0, "Issue Ops group contains dropdown");
    equal($opsDropdown.parent().find(".aui-list-item").length, 9, "Issue operations dropdown contains the correct number of links");

    var transitionsGroup = leftGroups[2];
    this.groupContainsLinks(transitionsGroup, ["action_id_4", "action_id_5"]);

    var $transitionsDropdown = AJS.$(transitionsGroup).find("#opsbar-transitions_more");
    ok($transitionsDropdown.length > 0, "Issue transitions group contains dropdown");
    equal($transitionsDropdown.parent().find(".aui-list-item").length, 1, "Issue transitions dropdown contains the correct number of links");
});

test("Issue Opsbar RHS groups", function() {
    var $el = this.opsbarView.render();

    var rightGroups = $el.find(".toolbar-split-right .toolbar-group");
    equal(rightGroups.length, 1, "RHS toolbar contains only one subgroup");

    this.groupContainsLinks(rightGroups[0], ["jira-share-trigger"]);

    var $viewsDropdown = rightGroups.find(".toolbar-trigger");
    ok($viewsDropdown.length > 0, "Ops group contains dropdown");
    equal($viewsDropdown.parent().find(".aui-list-item").length, 3, "Views dropdown contains correct number of views.");
});

test("Long label is truncated", function() {
    var entity = this.issueModel.getEntity();
    entity.operations.linkGroups[0].groups[0].links[0].label = "Editing an issue using a very long operation label";

    var $el = this.opsbarView.render();
    equal($el.find("#edit-issue").text(), "Editing an issue using...", "Long Edit issue label should have been truncted");
});

test("Disabled dropdowns", function() {
    var entity = this.issueModel.getEntity();
    // Remove contents from More Actions dropdown
    entity.operations.linkGroups[0].groups[1].groups[0].groups = [];
    // Remove links from workflow group
    entity.operations.linkGroups[0].groups[2].links = [];
    entity.operations.linkGroups[0].groups[2].groups[0].groups = [];

    var $el = this.opsbarView.render();

    var $moreDropdown = $el.find("span#opsbar-operations_more");
    ok($moreDropdown.closest(".toolbar-item").hasClass("disabled"), "Disabled dropdown has disabled class");
    ok(!$moreDropdown.hasClass("js-default-dropdown"), "Disabled dropdown has no js dropdown class");

    var $workflowDropdown = $el.find("span#opsbar-transitions_more");
    equal($workflowDropdown.length, 0, "Disabled dropdown with no links in the parent group should not be rendered");
});


