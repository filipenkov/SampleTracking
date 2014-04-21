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
        },
        submitHandler: function (e) {

            e.preventDefault();

            this.$form.find("#lead-field").blur();

            if (this.$form.find("#lead-single-select").parent().find(".error").length === 0) {
                this._submitForm(e);
            } else {
                this.$form.find("#lead-field").focus();
                this.$form.find(":submit").removeAttr("disabled");
            }
        }
    });
});
