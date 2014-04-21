AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testdata");

module('JIRA.Issues.IssueBodyView', {
    setup: function() {
        this.el = AJS.$("<div></div>");
        this.issuePanelsModel = new JIRA.Issues.IssuePanelsModel();
        this.issuePanelsModel.update(mockWebPanelsJSON, {editable:true});
        this.issueModel = new JIRA.Issues.IssueViewModel({ entity:mockIssueJSON , issueEventBus: new JIRA.Issues.IssueEventBus()});
        this.issueModel.setPanels(this.issuePanelsModel);
        this.bodyView = new JIRA.Issues.IssueBodyView({el:this.el, model:this.issueModel});
    },
    teardown: function() {
        this.el.remove();
    },

    assertHasModules:function(column, moduleIds) {
        var $modules = column.find(".module");
        equal($modules.length, moduleIds.length, "Column has correct number of modules");
        $modules.each(function(i, module) {
            equal(AJS.$(module).attr("id"), moduleIds[i], "Module " + moduleIds[i] + " is correct");
        });
    }
});

test("Issue Body View renders all web-panels", function() {
    var $el = this.bodyView.render();

    var $columns = $el.find(".aui-group .aui-item");
    equal($columns.length, 2, "Correct number of columns rendered");
    var $leftColumn = AJS.$($columns[0]);
    this.assertHasModules($leftColumn, ["details-module", "descriptionmodule", "attachmentmodule", "linkingmodule", "activitymodule", "addcomment"]);
    var $rightColumn = AJS.$($columns[1]);
    this.assertHasModules($rightColumn, ["peoplemodule", "datesmodule"]);
});

test("Issue Body can add and remove web-panels", function() {
    var $el = this.bodyView.render();
    var $columns = $el.find(".aui-group .aui-item");
    equal($columns.length, 2, "Correct number of columns rendered");
    var $leftColumn = AJS.$($columns[0]);
    this.assertHasModules($leftColumn, ["details-module", "descriptionmodule", "attachmentmodule", "linkingmodule", "activitymodule", "addcomment"]);
    var $rightColumn = AJS.$($columns[1]);
    this.assertHasModules($rightColumn, ["peoplemodule", "datesmodule"]);

    //now remove the attachment module!
    this.issuePanelsModel.update(mockWebPanelsJSONNoAttachments, {editable:true});
    $columns = $el.find(".aui-group .aui-item");
    equal($columns.length, 2, "Correct number of columns rendered");
    $leftColumn = AJS.$($columns[0]);
    this.assertHasModules($leftColumn, ["details-module", "descriptionmodule", "linkingmodule", "activitymodule", "addcomment"]);
    $rightColumn = AJS.$($columns[1]);
    this.assertHasModules($rightColumn, ["peoplemodule", "datesmodule"]);

    //now add the timetracking module!
    this.issuePanelsModel.update(mockWebPanelsJSONWithTimeTracking, {editable:true});
    $columns = $el.find(".aui-group .aui-item");
    equal($columns.length, 2, "Correct number of columns rendered");
    $leftColumn = AJS.$($columns[0]);
    this.assertHasModules($leftColumn, ["details-module", "descriptionmodule", "linkingmodule", "activitymodule", "addcomment"]);
    $rightColumn = AJS.$($columns[1]);
    this.assertHasModules($rightColumn, ["peoplemodule", "datesmodule", "timetracking"]);
});