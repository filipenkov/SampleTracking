JIRA.createEditProjectDialog = function (trigger) {
    return new JIRA.FormDialog({
        type: "ajax",
        id: "project-config-project-edit-dialog",
        trigger: trigger,
        autoClose: true,
        stacked: true,
        width: 560
    });
};

