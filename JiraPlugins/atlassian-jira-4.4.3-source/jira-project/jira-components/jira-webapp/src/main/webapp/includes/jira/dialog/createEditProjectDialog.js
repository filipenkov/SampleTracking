JIRA.createEditProjectDialog = function (trigger) {
    return new JIRA.FormDialog({
        type: "ajax",
        id: "project-config-project-edit-dialog",
        trigger: trigger,
        autoClose: true,
        stacked: true,
        onContentRefresh: function () {

            // clean up of old avatar picker. We get duplicate sections otherwise after opening and closign and opening again.
            jQuery("#avatar-dialog, #avatar-panel").remove();

            JIRA.AvatarPicker({
                triggerSelector: "#project_avatar_link",
                prefsSelector: "#avatar-prefs"
            });
        },
        onSuccessfulSubmit: function() {
            // causes dirty form warnings otherwise
            jQuery("#avatar-dialog, #avatar-panel").remove();
        }
    });
};

