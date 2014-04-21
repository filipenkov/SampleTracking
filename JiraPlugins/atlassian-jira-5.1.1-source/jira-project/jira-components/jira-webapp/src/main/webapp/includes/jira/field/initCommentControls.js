(function () {

    function applyCommentControls(context) {
        if (context.attr("id") === "addcomment") {
            context = AJS.$("#issue-comment-add")
        }

        var securityLevelSelects = jQuery("#commentLevel", context);
        securityLevelSelects.each(function() {
            new AJS.SecurityLevelSelect(AJS.$(this, context));
        });
        var wikiRenders = jQuery(".wiki-js-prefs", context);
        wikiRenders.each(function() {
            JIRA.wikiPreview(this, context).init();
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
            applyCommentControls(context);
    });
})();
