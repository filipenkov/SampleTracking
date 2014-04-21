AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {

        var trustDialog;
        var confirmDeleteDialog;
        var orphanTrustRow;

        var trustDialogDiv = $("#orphaned-trust-certificates");

        var trustDialogSettings = {
            showButtonId: "show-orphaned-trust",
            cancelLabel: AppLinks.I18n.getTextWithPrefix("close"),
            width: 800,
            height: 300,
            id: "orphaned-trust-certificates-dialog",
            onshow: function(popup) {
                var dialog = popup.popup.element;
            }
        };

        trustDialog = trustDialogDiv.wizard(trustDialogSettings);

        AppLinks.showOrphanedTrustCertificates = function() {
            trustDialog.show();
        };

        var deleteDialogDiv = $("#orphaned-trust-certificates-delete");

        var deleteDialogSettings = {
            submitLabel: AppLinks.I18n.getTextWithPrefix("delete"),
            cancelLabel: AppLinks.I18n.getTextWithPrefix("cancel"),
            width: 350,
            height: 175,
            id: "#orphaned-trust-certificates-delete-dialog",
            onshow: function(popup) {
                var dialog = popup.popup.element;
                $(dialog).find(".confirm-delete-text")
                        .text($.trim(AppLinks.I18n.getText("applinks.orphaned.trust.confirm.delete.text",
                                orphanTrustRow.find("td.description").text())));
            },
            onsubmit: function() {
                var success = function() {
                    orphanTrustRow.remove();

                    confirmDeleteDialog.dialog.hide();

                    if ($("tr.orphaned-trust-row").size() == 0) {
                        // all trust certs dealt with
                        $(".orphaned-trust-warning").hide();
                    } else {
                        // still some certs left - reshow the wizard
                        trustDialog.show();
                    }
                };

                var error = function(xhr, ajaxOptions, thrownError) {
                    confirmDeleteDialog.dialog.hide();
                    var msg = $.parseJSON(xhr.responseText).message;
                    AppLinks.UI.showErrorBox(AppLinks.I18n.getText("applinks.orphaned.trust.failed.to.delete") + ": " + msg);
                };

                var orphanedTrustId = orphanTrustRow.attr("data-id");
                var orphanedTrustType = orphanTrustRow.attr("data-type");

                AppLinks.SPI.deleteOrphanedTrust(orphanedTrustId, orphanedTrustType, success, error);
                return false;
            },
            oncancel: function() {
                trustDialog.show();
            }
        };

        confirmDeleteDialog = deleteDialogDiv.wizard(deleteDialogSettings);

        $(".orphaned-trust-create-link").click(function() {
            var row = $(this).parents("tr.orphaned-trust-row");
            var description = row.find("td.description").text();
            var id          = row.attr("data-id");
            var authType   = row.attr("data-type");
            trustDialog.dialog.hide();
            AppLinks.showUpgradeToApplicationLinkWizard(id, authType, AppLinks.UI.findApplicationType(id), AppLinks.UI.findUrl(description));
        });

        $(".orphaned-trust-delete").click(function() {
            orphanTrustRow = AJS.$(this).parents("tr.orphaned-trust-row");

            trustDialog.dialog.hide();
            confirmDeleteDialog.show();
        });

    })(AJS.$);
});
