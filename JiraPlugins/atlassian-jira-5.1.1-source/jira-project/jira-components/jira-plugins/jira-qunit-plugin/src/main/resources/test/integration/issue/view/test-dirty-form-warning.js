/**
 * Tests that dirty-form warnings are shown correctly on the
 * View Issue screen.
 */
AJS.$(function ($) {

    var commentArea = $('#comment');

    module('Dirty Form Warning Tests', {
        setup: function () {
            commentArea.val('');
            if (!commentArea.is(':visible')) {
                $('#footer-comment-button').click();
            }
            commentArea.val('This text makes the issue-edit form dirty. VERY dirty.');
        },

        teardown: function () {
            commentArea.val('');
        }
    });

    test("Dirty form should pick up comment change", 1, function () {
        var dirtyMessage = JIRA.DirtyForm.getDirtyWarning();
        var expectedMsg = "You have entered new data on this page. If you navigate away from this page without first saving your data, the changes will be lost.";
        QUtil.contains(dirtyMessage, expectedMsg, "The page dirty message should be displayed");
    });

    // JRA-23520
    asyncTest("Dirty manager should bind to page-reload-from-dialog events", 3, function () {
        $('#opsbar-operations_more').click();                               // open 'More Actions' menu
        $('#log-work').click();                                             // launch 'Log Work' dialog

        QUtil.waitUntilVisible('#log-work-dialog', function () {
            $('#log-work-time-logged').val('1');                            // just enough to make the form valid

            var server = sinon.sandbox.useFakeServer();
            server.respondWith(/CreateWorklog/, QUtil.JIRA.dialogOk());     // don't actually log any work
            var reloadStub = sinon.stub(AJS, 'reloadViaWindowLocation');    // don't actually do the reload
            $('#log-work-submit').click();
            server.respond();

            ok(reloadStub.called, "Window reload should have been triggered");

            var dirtyMessage = JIRA.DirtyForm.getDirtyWarning();
            var expectedMsg = "The data in the dialog box was successfully submitted, although this page requires a refresh to display the results.\n\nYou had entered new data on the page before you opened the dialog box. If you continue, your page data will be lost.";
            QUtil.contains(dirtyMessage, expectedMsg, "The confirm message displayed on reload should be the page-unload.refresh.from-dialog one");

            start();
        });
    });
});