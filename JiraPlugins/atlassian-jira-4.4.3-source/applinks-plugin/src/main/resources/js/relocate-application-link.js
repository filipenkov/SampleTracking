AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {

        var dialogDiv = $("#relocate-link-dialog");

        var dialogSettings; // contains the applink-specific data
        var wizard;
        var newUrl;

        var wizardSettings = {
            nextLabel: AppLinks.I18n.getText("applinks.relocate.wizard.button"),
            submitLabel: AppLinks.I18n.getText("applinks.relocate.wizard.button"),
            api: true,
            width: 500,
            height: 250,
            onshow: function(popup) {
                var dialog = popup.popup.element;
                dialog.find("h2").text(AppLinks.I18n.getText('applinks.relocate.title')).append(AppLinks.Docs.createDocLink(dialogSettings.helpKey, null, 'dialog-help-link'));
                $("#relocate-text").text(dialogSettings.description);
                $("#relocate-error").text("");
                $('#relocate-url').val(dialogSettings.application.rpcUrl);
                AppLinks.UI.hideLoadingIcon($('#relocate-url'));
                AppLinks.UI.hideLoadingIcon($('#confirm-relocate-text'));
                return true;
            },
            onnext: function(popup) {
                $("#relocate-error").text("");
                newUrl = AppLinks.UI.addProtocolToURL($('#relocate-url').val());
                AppLinks.UI.showLoadingIcon($('#relocate-url'));
                dialogSettings.doRelocate(newUrl, wizardSettings.closeWizard, function(xhr) {
                    AppLinks.UI.hideLoadingIcon($('#relocate-url'));
                    if (xhr.status == 409) {
                        $("#confirm-relocate-text").text(AppLinks.I18n.getText("applinks.error.relocate.unavailable", newUrl));
                        popup.popup.element.find("h2").text(AppLinks.I18n.getText('applinks.relocate.title.confirm')).append(AppLinks.Docs.createDocLink(dialogSettings.helpKey, null, 'dialog-help-link'));
                        wizard.dialog.gotoPage(1);
                    } else {
                        $("#relocate-error").text(AppLinks.parseError(xhr));
                    }
                });
                return false;
            },
            onprevious: function(popup) {
                var dialog = popup.popup.element;
                dialog.find("h2").text(AppLinks.I18n.getText('applinks.relocate.title')).append(AppLinks.Docs.createDocLink(dialogSettings.helpKey, null, 'dialog-help-link'));
            },
            closeWizard: function() {
                wizard.cancel();
                dialogSettings.callback();  // refresh the application list
            },
            onsubmit: function() {
                AppLinks.UI.showLoadingIcon($('#confirm-relocate-text'));
                dialogSettings.doForceRelocate(newUrl, wizardSettings.closeWizard, function(xhr) {
                    AppLinks.UI.hideLoadingIcon($('#confirm-relocate-text'));
                    $("#confirm-relocate-error").text(AppLinks.parseError(xhr));
                });
                return false;
            }
        };

        wizard = dialogDiv.wizard(wizardSettings);

        AppLinks.showRelocateLinkDialog = function(settings) {
            dialogSettings = settings;
            wizard.show();
        }

    })(AJS.$)
});
