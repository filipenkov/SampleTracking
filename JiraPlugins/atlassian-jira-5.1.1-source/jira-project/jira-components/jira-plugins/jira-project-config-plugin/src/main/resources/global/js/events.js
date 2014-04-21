JIRA.bind(AJS.RestfulTable.Events.SERVER_ERROR, function (e, data) {

    var serverErrorConsole = jQuery("#project-config-error-console");

    if (data && data.errorMessages) {

        // Replace any pre-existing messages
        serverErrorConsole.empty();

        new JIRA.FormDialog({
            id: "server-error-dialog",
            content: function (callback) {
                callback(JIRA.Templates.Common.serverErrorDialog({
                    message: data.errorMessages[0]
                }));
            }
        }).show();
    }
});
