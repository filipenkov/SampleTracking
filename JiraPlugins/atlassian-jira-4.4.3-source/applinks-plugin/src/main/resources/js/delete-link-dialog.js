AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {
        /**
         *
         */
        var dialogSettings,
            showDeleteReciprocalLink,
            userHasToAuthenticate,
            authenticationUrl,
            removeLoadingIcon = function() {
                $('.delete-applink-loading').hide();
                wizard.enableSubmitBtn();
            }
            wizardSettings = {
                showButtonId: "delete-application-link",
                cancelLabel: AppLinks.I18n.getTextWithPrefix("cancel"),
                submitLabel: AppLinks.I18n.getText("applinks.dialog.delete.confirm"),
                width: 420,
                height: 330,
                id: "delete-application-link-dialog",
                aftershow: function() {

                    dialogRootEl.find('.applinks-error').remove();
                    var dialog = wizard.dialog.popup.element;
                    
                    dialog.find("h2").text(dialogSettings.title).append(AppLinks.Docs.createDocLink(dialogSettings.helpKey, null, 'dialog-help-link'));
                    var confirmMessage = dialogSettings.confirmMessage;
                    $('#delete-applink-text').html(confirmMessage);

                    if (showDeleteReciprocalLink == false) {
                        var processManifest = function(manifest) {
                            var isUAL = manifest.applinksVersion && true;

                            if (isUAL) {
                                var authenticationRequired = function(authUrl) {
                                    showDeleteReciprocalLink = true;
                                    userHasToAuthenticate = true;
                                    authenticationUrl = authUrl;
                                    removeLoadingIcon();
                                };
                                var noReciprocalLinkToDelete = function(errorMsg) {
                                    showDeleteReciprocalLink = false;
                                    userHasToAuthenticate = false;
                                    removeLoadingIcon();
                                }
                                var reciprocalLinkToDelete = function() {
                                    showDeleteReciprocalLink = true;
                                    userHasToAuthenticate = false;
                                    removeLoadingIcon();
                                }
                                dialogSettings.doPermissionCheck();
                                AppLinks.checkPermission(dialogSettings.doPermissionCheck, reciprocalLinkToDelete, authenticationRequired, noReciprocalLinkToDelete, AppLinks.UI.displayValidationError('delete-applink-error', dialogRootEl))
                            } else {
                                removeLoadingIcon();
                            }
                        };
                        $('.delete-applink-loading').show();
                        wizard.disableSubmitBtn(false);
                        AppLinks.SPI.getManifestFor(dialogSettings.applicationId, processManifest, AppLinks.UI.displayValidationError('delete-applink-error', dialogRootEl));
                    }
                },
                onnext: function(popup) {
                    $('.delete-reciprocal-applink-form').hide();
                    $('.no-reciprocal-applink-form').hide();
                    $('.authenticate-applink-form').hide();

                    var showDeleteReciprocalLinkForm = function(){
                        $('.authenticate-applink-form').hide();
                        $('.delete-reciprocal-applink-form').show();
                        $('#delete-reciprocal-applink-text').text(dialogSettings.reciprocalLinkMessage);
                        $('.yes-delete-reciprocal-link').text(dialogSettings.deleteYesMessage);
                        $('.no-delete-reciprocal-link').text(dialogSettings.deleteNoMessage);
                        $('#yes-delete-reciprocal-link').attr('checked', 'true');
                        $('.delete-reciprocal-applink-form').show();
                    }

                    if (userHasToAuthenticate) {
                        $('.authenticate-applink-form').show();
                        $('.authenticate-message').text(dialogSettings.authenticationMessage);
                        AppLinks.SPI.addAuthenticationTrigger("#authenticate-link", authenticationUrl, {
                            before: function() {
                                popup.hide();
                            },
                            onSuccess: function() {
                                popup.show();
                                var reciprocalLinkToDelete = function() {
                                    showDeleteReciprocalLinkForm();
                                }
                                var noReciprocalLinkToDelete = function(errorMsg) {
                                    $('.authenticate-applink-form').hide();
                                    $('.no-reciprocal-applink-form').show();
                                    if (!errorMsg) {
                                        $('.no-link-message').text(dialogSettings.noReciprocalLinkMessage);
                                    } else {
                                        $('.no-link-message').text(dialogSettings.failedToDetectReciprocalLinkMessage + " " + errorMsg);
                                    }
                                }
                                AppLinks.checkPermission(dialogSettings.doPermissionCheck, reciprocalLinkToDelete, userHasToAuthenticate, noReciprocalLinkToDelete, AppLinks.UI.displayValidationError('delete-reciprocal-applink-error', dialogRootEl))
                            },
                            onFailure: function() {
                                popup.show();
                                $('.authenticate-applink-form').hide();
                                $('.no-reciprocal-applink-form').show();
                                $('.no-link-message').text(dialogSettings.authenticationFailedMessage);
                            }
                        });
                    } else {
                      showDeleteReciprocalLinkForm();
                    }
                },
                onsubmit: function(popup) {
                    var success = function() {
                        wizard.enableSubmitBtn();
                        if (dialogSettings.callback()) {
                            popup.hide();
                        }
                    };
                    if (wizard.dialog.curpage == 1) {
                        var reciprocal = false;
                        var yesElement = $('#yes-delete-reciprocal-link');
                        var yesVisible = yesElement.is(':visible');
                        var yesChecked = yesElement.attr('checked');
                        if (yesVisible && yesChecked) {
                            reciprocal = true;
                        }
                        wizard.disableSubmitBtn();
                        dialogSettings.doDelete(reciprocal, success, AppLinks.UI.displayValidationError('delete-applink-error', dialogRootEl, function() {
                            wizard.enableSubmitBtn();
                        }));
                    } else {
                        if (showDeleteReciprocalLink) {
                            wizard.nextPage();
                        } else {
                            wizard.disableSubmitBtn();
                            dialogSettings.doDelete(false, success, AppLinks.UI.displayValidationError('delete-applink-error', dialogRootEl, function() {
                                wizard.enableSubmitBtn();
                            }));
                        }
                    }
                    return false;
                }
            };

        var wizard = $("#delete-link-dialog").wizard(wizardSettings);
        var dialogRootEl = $(wizard.dialog.popup.element);

        AppLinks.showDeleteLinkDialog = function(settings) {
                showDeleteReciprocalLink = false;
                userHasToAuthenticate = false;
                dialogSettings = settings;
                wizard.show();
            }
    })(AJS.$)
});
