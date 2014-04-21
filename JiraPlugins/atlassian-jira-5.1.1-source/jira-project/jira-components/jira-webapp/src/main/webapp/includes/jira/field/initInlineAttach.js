(function () {

    function createInlineAttach(context) {
        context.find("input[type=file]:not('.ignore-inline-attach')").inlineAttach();
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            createInlineAttach(context);
        }
    });

})();

