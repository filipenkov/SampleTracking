(function () {

    function initIssuePicker(el) {
        AJS.$(el || document.body).find('.aui-field-issuepicker').each(function () {
            new JIRA.IssuePicker({
                element: AJS.$(this),
                userEnteredOptionsMsg: AJS.I18n.getText('linkissue.enter.issue.key'),
                uppercaseUserEnteredOnSelect: true
            });
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            initIssuePicker(context);
        }
    });
})();
