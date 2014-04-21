AJS.$(function() {

    var $emailLink = AJS.$("#project-config-email-change");

    if ($emailLink.length === 1) {
         new JIRA.FormDialog({
            id: "project-email-dialog",
            trigger: $emailLink
        });
    }

    JIRA.createEditProjectDialog("#edit_project");

    new JIRA.FormDialog({
        type: "ajax",
        id: "project-config-project-edit-lead-and-default-assignee-dialog",
        trigger: "#edit_project_lead,#edit_default_assignee",
        autoClose: true,
        onSuccessfulSubmit: function() {
        }
    });
});
