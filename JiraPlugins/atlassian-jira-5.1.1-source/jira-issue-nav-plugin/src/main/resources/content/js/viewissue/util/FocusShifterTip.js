AJS.namespace("JIRA.Issues.FocusShifterTip");

JIRA.Issues.FocusShifterTip = function() {
    var $message = JIRA.Messages.showMsg("", {
        closeable: true,
        type: JIRA.Templates.ViewIssue.Body.focusShifterTip
    });

    // Close the tip and show the shortcuts dialog when the "other keyboard
    // shortcuts" link is clicked (this also suppresses the tip).
    AJS.$("#focus-shifter-tip > a").click(function(e) {
        $message.remove();
        AJS.$("#keyshortscuthelp").click();
        JIRA.Issues.FocusShifter.suppressTip();
        e.preventDefault();
    });

    AJS.$("#focus-shifter-tip > span.icon-close").click(function() {
        JIRA.Issues.FocusShifter.suppressTip();
    });

    JIRA.bind("Dialog.show", function() {
        $message.remove();
    });
};