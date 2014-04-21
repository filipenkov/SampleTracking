AJS.$(function () {

    function initIssuePicker(el) {
        AJS.$(el || document.body).find('.aui-field-issuepicker').each(function () {
            new JIRA.IssuePicker({
                element: AJS.$(this),
                userEnteredOptionsMsg: AJS.params.enterIssueKey,
                uppercaseUserEnteredOnSelect: true
            });
        });
    }

    // Init the control
    initIssuePicker();

    // Bind the init function so it runs within the dialogs
    AJS.$(document).bind("dialogContentReady", function (e, dialog) {
        initIssuePicker(dialog.get$popupContent());
    });

});
