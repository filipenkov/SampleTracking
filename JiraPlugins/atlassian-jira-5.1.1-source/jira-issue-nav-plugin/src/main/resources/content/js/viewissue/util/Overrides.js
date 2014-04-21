/**
 * As we do not do full page pops now, we wait for issue refresh instead of page reload
 */
JIRA.Messages.showMsgOnReload = function (msg, options) {
    JIRA.one(JIRA.Events.ISSUE_REFRESHED, function () {
        options.type = JIRA.Messages.Types[options.type];
        JIRA.Messages.showMsg(msg, options);
    });
};