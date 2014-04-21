AJS.$(function () {

    function applyCommentControls(context) {

        context = context || document.body;

        new AJS.SecurityLevelSelect(AJS.$("#commentLevel", context));
        var wikiRenders = jQuery(".wiki-js-prefs", context);
        wikiRenders.each(function() {
            JIRA.wikiPreview(this, context).init();
        });
    }

    applyCommentControls();

     // Bind the init function so it runs within the dialogs
    AJS.$(document).bind("dialogContentReady", function (e, dialog) {
        applyCommentControls(dialog.get$popupContent());
    });
});
