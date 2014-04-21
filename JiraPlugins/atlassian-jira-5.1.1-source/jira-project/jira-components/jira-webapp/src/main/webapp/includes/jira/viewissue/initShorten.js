JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, $ctx) {
    AJS.$(".shorten", $ctx).shorten();
});