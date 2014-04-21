// This file contains the logic for the upgrade a non-ual-link-to-ual wizard
AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {
        var dialogSettings; // contains the applink-specific data, incl manifest
        var appUrl;
        var wizardSettings = {
            width: 510,
            height: 580,            
            id: "upgrade-application-link-dialog",
            onnext: function() {
                dialogRootEl.find('.applinks-error').remove();
                var page = upgradeApplicationLinkWizard.dialog.curpage;
                if (page == 0) {
                    AppLinks.Wizard.handleUALManifest(dialogSettings.manifest, dialogRootEl);
                    upgradeApplicationLinkWizard.dialog.gotoPage(1);
                    reciprocalLinkUsernameEl.focus();
                    return false;

                } else if (page == 1) {
                    var createOneWayLinkFn = function() {
                        //Create the link if user decided not to create a reciprocal link

                        // call the upgrade rest endpoint
                        var successFn = function(upgradedApplicationLink) {
                            upgradeApplicationLinkWizard.cancel();
                            dialogSettings.successCallback(upgradedApplicationLink);
                        };
                        var payload = {
                            createTwoWayLink: false,
                            reciprocateEntityLinks: false,
                            configFormValues: {
                                trustEachOther: false,
                                shareUserbase: false
                            }
                        };
                        AppLinks.SPI.ualUpgrade(dialogSettings.application, payload, successFn, AppLinks.UI.displayValidationError('create-validation-errors', dialogRootEl));
                    }
                    var gotoNextPageFn = function() {
                        upgradeApplicationLinkWizard.dialog.gotoPage(2);
                    }
                    AppLinks.Wizard.checkReciprocalLinkForm(dialogRootEl, createOneWayLinkFn, gotoNextPageFn, appUrl);
                    return false;
                }
                return true;
            },
            onprevious: function() {
                return true;
            },
            onsubmit: function() {
                var successFn = function(upgradedApplicationLink) {
                    upgradeApplicationLinkWizard.cancel();
                    dialogSettings.successCallback(upgradedApplicationLink);
                };
                var payload = {
                    rpcUrl: rpcURLEl.val(),
                    username: reciprocalLinkUsernameEl.val(),
                    password: reciprocalLinkPasswordEl.val(),
                    createTwoWayLink: true,
                    reciprocateEntityLinks: reciprocateEntityLinks.attr('checked'),
                    configFormValues: {
                        trustEachOther: !!(dialogRootEl.find('.trust-each-other-description').find('input').attr('checked')),
                        shareUserbase: !!(dialogRootEl.find('.same-user-description').find('input').attr('checked'))
                    }
                };
                AppLinks.SPI.ualUpgrade(dialogSettings.application, payload, successFn, AppLinks.UI.displayValidationError('create-validation-errors', dialogRootEl));
                return false;
            },
            onshow: function() {
                dialogRootEl.find('.applinks-error').remove();
                AppLinks.UI.hideInfoBox();
                rpcURLEl.val($('#baseUrl').val());
                $(".ual-upgrade-text").text(dialogSettings.description);
                appUrl = dialogSettings.application.rpcUrl;

                var success = function(entities) {
                    if (AJS.$.isArray(entities.entity) && entities.entity.length > 0) {

                        // get all unique entity type keys:
                        var uniqueNames = new Array();
                        AJS.$.each(entities.entity, function(index, entity) {
                            if (AJS.$.inArray(entity.typeId, uniqueNames) == -1) {
                                uniqueNames.push(entity.typeId);
                            }
                        });

                        // string them all in a coherent "1, 2 and 3" string:
                        var prettyString = AppLinks.UI.prettyJoin(uniqueNames, function(typeId) {
                            return AppLinks.I18n.getEntityTypeName(typeId);
                        });

                        // display, make visible and tick the checkbox
                        reciprocateEntityLinksDescription.text(AppLinks.I18n.getText("applinks.ual.upgrade.reciprocate.entitylinks.description", prettyString));
                        reciprocateEntityLinks
                                .attr('checked', 'true')
                                .closest('div')
                                .removeClass('hidden');
                    }
                };
                var failure = function(xhr) {
                    reciprocateEntityLinksDescription.text(AppLinks.I18n.getText("applinks.ual.upgrade.reciprocate.entitylinks.description", "project"));
                    reciprocateEntityLinks
                            .attr('checked', 'true')
                            .closest('div')
                            .removeClass('hidden');
                };
                reciprocateEntityLinks.closest('div').addClass('hidden');
                AppLinks.SPI.getLocalEntitiesWithLinksToApplication(dialogSettings.application.id, success, failure);
                return true;
            },
            aftershow: function() {
            }
        }
        var upgradeApplicationLinkWizard = $("#upgrade-application-link-container").wizard(wizardSettings);
        var dialogRootEl     = $(upgradeApplicationLinkWizard.dialog.popup.element);
        var localAppNameEl   = dialogRootEl.find('.local-app-name');
        var rpcURLEl = dialogRootEl.find('.reciprocal-rpc-url');
        var reciprocalLinkUsernameEl = dialogRootEl.find('.reciprocal-link-username');
        var reciprocalLinkPasswordEl = dialogRootEl.find('.reciprocal-link-password');
        var reciprocateEntityLinks = dialogRootEl.find('.reciprocate-entity-links');
        var reciprocateEntityLinksDescription = dialogRootEl.find('.reciprocate-entity-links-description');

        localAppNameEl.html(AppLinks.UI.shortenString(applicationName, 20));

        AppLinks.Wizard.initNonUALUI(dialogRootEl);
        AppLinks.Wizard.initAuthenticationUI(dialogRootEl);

        AppLinks.showUpgradeLinkToUALDialog = function(settings) {
            dialogSettings = settings;
            upgradeApplicationLinkWizard.show();
        };

        reciprocalLinkPasswordEl.keydown(function (event) {
             //KeyCode 13 is the enter key.
            if (event.keyCode == '13') {
               upgradeApplicationLinkWizard.nextPage();
            }
        });
    })(AJS.$);
});
