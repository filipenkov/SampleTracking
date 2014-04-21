/**
 This dialog is used to upgrade an existing oauth or trusted apps configuration to an Application Link.
 **/

AJS.$(document).bind(AppLinks.Event.READY, function() {
   (function($) {
        var authenticationType,
            authLabel,
            authId,
            appType,
            appTypeLabel,
            manifest,
            appUrl,
            upgradeDialogSettings = {
               cancelLabel:   AppLinks.I18n.getTextWithPrefix("cancel"),
               width:         510,
               height:        580,
               id:            "upgrade-orphaned-trust-dialog",
               onshow: function(popup) {
                   dialogRootEl.find('.applinks-error').remove();
                   return true;
               },
               onnext: function() {
                    dialogRootEl.find('.applinks-error').remove();
                    var page = upgradeTrustWizard.dialog.curpage;
                    if (page == 0) {
                        var ualAppFn = function(man) {
                            manifest = man;
                            dialogRootEl.find('.reciprocal-link-description').text(AppLinks.I18n.getTextWithPrefix("orphaned.trust.two.way", authLabel));
                            dialogRootEl.find('.no-reciprocal-link-description').text(AppLinks.I18n.getTextWithPrefix("orphaned.trust.one.way", authLabel));
                        }
                        var nonUALAppFn = function(man) {
                            dialogRootEl.find('.application-name').val(dialogRootEl.find(".non-ual-application-url").text());
                            if (authenticationType == "OAUTH_SERVICE_PROVIDER") {
                                dialogRootEl.find('.non-ual-description').text(AppLinks.I18n.getTextWithPrefix("orphaned.trust.oauth.outgoing"));
                            } else {
                                dialogRootEl.find('.non-ual-description').text(AppLinks.I18n.getTextWithPrefix("orphaned.trust.incoming", authLabel));
                            }
                            manifest = man;
                            if (appType) {
                                dialogRootEl.find('.application-types').val(appType);
                            }

                        }
                        appUrl= AppLinks.Wizard.fetchManifest(upgradeTrustWizard, dialogRootEl, ualAppFn, nonUALAppFn);
                    }
                    return false;
               },
               onprevious: function() {
                      upgradeTrustWizard.dialog.gotoPage(1);
               },
               onsubmit: function() {
                     var page = upgradeTrustWizard.dialog.curpage;
                     var successFn = function(data) {
                        upgradeTrustWizard.enableSubmitBtn();
                        upgradeTrustWizard.enablePreviousBtn();
                        upgradeTrustWizard.cancel();
                        AppLinks.UI.listApplicationLinks(data.applicationLink.id, 'new', data);
                     };
                     if (page == 1) {
                          //NON-UAL
                          var orphanedConfig = {
                              id:   authId,
                              type: authenticationType
                          },
                          applicationLink = {
                                id: manifest.id,
                                typeId: dialogRootEl.find(".application-types").val(),
                                name: dialogRootEl.find(".application-name").val(),
                                rpcUrl: dialogRootEl.find(".non-ual-application-url").text(),
                                displayUrl: dialogRootEl.find(".non-ual-application-url").text(),
                                isPrimary: false
                          },
                          configFormValues = {
                                trustEachOther: false,
                                shareUserbase: false
                          };
                          upgradeTrustWizard.disableSubmitBtn();
                          upgradeTrustWizard.disablePreviousBtn();
                          AppLinks.SPI.createLinkWithOrphanedTrust(applicationLink, '', '', false, false, "", configFormValues, orphanedConfig, successFn, AppLinks.UI.displayValidationError('create-non-ual-errors', dialogRootEl, function() {
                                upgradeTrustWizard.enableSubmitBtn();
                                upgradeTrustWizard.enablePreviousBtn();
                          }));
                     } else if (page == 2) {
                          var orphanedConfig = {
                              id:   authId,
                              type: authenticationType
                          };
                          var configFormValues = {
                                trustEachOther: false,
                                shareUserbase:  false
                          },
                          applicationLink = {
                                id: manifest.id,
                                typeId: manifest.typeId,
                                name: manifest.name,
                                rpcUrl: appUrl,
                                displayUrl: manifest.url,
                                isPrimary: false
                          },
                          rpcUrl = rpcURLEl.val();
                          upgradeTrustWizard.disableSubmitBtn();
                          upgradeTrustWizard.disablePreviousBtn();
                          var createTwoWayLink = dialogRootEl.find('.create-reciprocal-link').is(':checked');
                          var createLinkFn = function() {
                              AppLinks.SPI.createLinkWithOrphanedTrust(applicationLink, reciprocalLinkUsernameEl.val(), reciprocalLinkPasswordEl.val(), createTwoWayLink/*create two way link*/ , true, rpcUrl, configFormValues, orphanedConfig, successFn, AppLinks.UI.displayValidationError('two-way-link-errors', dialogRootEl, function() {
                                upgradeTrustWizard.enableSubmitBtn();
                                upgradeTrustWizard.enablePreviousBtn();
                              }));
                          }
                          var errorFn = function() {
                            upgradeTrustWizard.enableSubmitBtn();
                            upgradeTrustWizard.enablePreviousBtn();
                          }
                          AppLinks.Wizard.checkReciprocalLinkForm(dialogRootEl, createLinkFn, createLinkFn, appUrl, errorFn);
                          }
                     return false;
               },
               aftershow: function() {
                     applicationURLEl.focus();
               }
        }
        var upgradeTrustWizard = $("#upgrade-orphaned-trust-container").wizard(upgradeDialogSettings),
            dialogRootEl =  $(upgradeTrustWizard.dialog.popup.element),
            localAppNameEl   = dialogRootEl.find('.local-app-name'),
            applicationURLEl = dialogRootEl.find('#application-url'),
            rpcURLEl = dialogRootEl.find('.reciprocal-rpc-url'),
            reciprocalLinkUsernameEl = dialogRootEl.find('.reciprocal-link-username'),
            reciprocalLinkPasswordEl = dialogRootEl.find('.reciprocal-link-password');
            localAppNameEl.html(AppLinks.UI.shortenString(applicationName, 20));

       AppLinks.showUpgradeToApplicationLinkWizard = function(id, authType, applicationType, url) {
            authId = id;
            authenticationType = authType;
            appType = applicationType;
            appTypeLabel = AppLinks.I18n.getTextWithPrefix(applicationType);
            authLabel;
            if (authType === "TRUSTED_APPS") {
                authLabel = AppLinks.I18n.getTextWithPrefix("orphaned.trust.trusted.apps.name");
            } else if (authType == "OAUTH") {
                authLabel = AppLinks.I18n.getTextWithPrefix("orphaned.trust.oauth.name");
            }

            AppLinks.UI.removeCssClass(dialogRootEl.find('#remoteApp'), 'application-type-image');
            if (applicationType) {
                dialogRootEl.find('#remoteApp').removeClass('app-image unknown-app-image');
                dialogRootEl.find('#remoteApp').addClass('app-image remote-app-image application-type-image-' + applicationType);
            } else {
                dialogRootEl.find('#remoteApp').removeClass('app-image remote-app-image');
                dialogRootEl.find('#remoteApp').addClass('app-image unknown-app-image');
            }

            if (url && applicationType) {
               dialogRootEl.find('.upgrade-info').text(AppLinks.I18n.getTextWithPrefix("orphaned.trust.check.url.application.type", appTypeLabel));
               applicationURLEl.val(url);
            } else if (url && !applicationType) {
               dialogRootEl.find('.upgrade-info').text(AppLinks.I18n.getTextWithPrefix("orphaned.trust.check.url"));
               applicationURLEl.val(url);
            } else if (!url && applicationType) {
                dialogRootEl.find('.upgrade-info').text(AppLinks.I18n.getTextWithPrefix("orphaned.trust.enter.url.for.application.type", appTypeLabel));
            }
            else {
                dialogRootEl.find('.upgrade-info').text(AppLinks.I18n.getTextWithPrefix("orphaned.trust.enter.url", authLabel));
            }
            upgradeTrustWizard.show();
       };

        AppLinks.Wizard.initNonUALUI(dialogRootEl);
        AppLinks.Wizard.initAuthenticationUI(dialogRootEl);

        applicationURLEl.keydown(function(event) {
                //KeyCode 13 is the enter key.
                if (event.keyCode == '13') {
                    upgradeTrustWizard.nextPage();
                }
        });
        reciprocalLinkPasswordEl.keydown(function(event) {
                //KeyCode 13 is the enter key.
                if (event.keyCode == '13') {
                    upgradeTrustWizard.submit();
                }
        });
        rpcURLEl.keydown(function(event) {
                //KeyCode 13 is the enter key.
                if (event.keyCode == '13') {
                    upgradeTrustWizard.submit();
                }
        });

    })(AJS.$);
});
