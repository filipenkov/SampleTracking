// This file contains the logic for the upgrade a non-ual-link wizard (serverid substitution)
AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {

        var dialogDiv = $("#upgrade-legacy-link-dialog");

        var dialogSettings; // contains the applink-specific data
        var wizard;

        var wizardSettings = {
            api: true,
            submitLabel: AppLinks.I18n.getText("applinks.legacy.upgrade.wizard.submit"),
            onshow: function(popup) {
                var dialog = popup.popup.element;
                dialog.find("h2").text(AppLinks.I18n.getText('applinks.upgrade.wizard.title')).append(AppLinks.Docs.createDocLink(dialogSettings.helpKey, null, 'dialog-help-link'));
                $("#legacy-upgrade-text").text(dialogSettings.description);
                $(".legacy-upgrade-error").text("");
                AppLinks.UI.hideLoadingIcon("#legacy-upgrade-text");
                $(popup.popup.element).find(".wizard-submit").attr('disabled', false);
                return true;
            },
            onsubmit: function(popup) {
                $(".legacy-upgrade-error").text("");
                $(popup.popup.element).find(".wizard-submit").attr('disabled', true);
                AppLinks.UI.showLoadingIcon("#legacy-upgrade-text");
                dialogSettings.submit(
                        function(upgradedApplicationLink) {
                            wizard.cancel();
                            // refresh the application list and apply the new id
                            dialogSettings.callback(upgradedApplicationLink.applicationLink.id);
                        },
                        function(xhr) {
                            AppLinks.UI.displayValidationError('legacy-upgrade-error', dialogRootEl)(xhr);
                            $(popup.popup.element).find(".wizard-submit").attr('disabled', false);
                        }
                );
                return false;
            }
        };

        wizard = dialogDiv.wizard(wizardSettings);
        var dialogRootEl     = $(wizard.dialog.popup.element);

        AppLinks.showUpgradeLinkDialog = function(settings) {
            dialogSettings = settings;
            wizard.show();
        }

    })(AJS.$)
});
