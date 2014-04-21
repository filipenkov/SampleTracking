JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, $ctx) {
    if ($ctx.is(".js-stalker")) {
        $ctx.stalker();
    } else {
        AJS.$(".js-stalker", $ctx).stalker();
    }
});
