AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {
        var manifest = {};
        var appUrl;
        var wizardSettings = {
            showButtonId: "add-application-link",
            width: 510,
            height: 580,
            id: "add-application-link-dialog",
            onnext: function() {
                dialogRootEl.find('.applinks-error').remove();
                var page = createApplicationLinkWizard.dialog.curpage;
                if (page == 0) {
                    var handleManifest = function (man) {
                       manifest = man;
                    };
                    appUrl = AppLinks.Wizard.fetchManifest(createApplicationLinkWizard, dialogRootEl, handleManifest, handleManifest);
                    return false;
                } else if (page == 2) {
                    var createOneWayLinkFn = function() {
                        createApplicationLinkWizard.enableNextBtn();
                        createApplicationLinkWizard.enablePreviousBtn();
                        //Create the link if user decided not to create a reciprocal link
                        var applicationLink = {
                            id: manifest.id,
                            typeId: manifest.typeId,
                            name: manifest.name,
                            rpcUrl: appUrl,
                            displayUrl: manifest.url,
                            isPrimary: false
                        },
                            configFormValues = {
                                trustEachOther: false,
                                shareUserbase: false
                            },
                            successFn = function(data) {
                                createApplicationLinkWizard.enableSubmitBtn();
                                createApplicationLinkWizard.cancel();
                                AppLinks.UI.listApplicationLinks(data.applicationLink.id, 'new', data);
                            };
                        createApplicationLinkWizard.disableSubmitBtn();
                        AppLinks.SPI.createLink(applicationLink, "", "", false, false, "", configFormValues, successFn, AppLinks.UI.displayValidationError('create-validation-errors', dialogRootEl, function() {
                                createApplicationLinkWizard.enableSubmitBtn();
                            }));
                    }
                    var gotoNextPageFn = function() {
                        createApplicationLinkWizard.enableNextBtn();
                        createApplicationLinkWizard.enablePreviousBtn();
                        createApplicationLinkWizard.dialog.gotoPage(3);
                        if (manifest.publicSignup) {
                            dialogRootEl.find("#warning-without-public-signup").hide();
                            dialogRootEl.find("#warning-with-public-signup").show();
                        } else {
                            dialogRootEl.find("#warning-without-public-signup").show();
                            dialogRootEl.find("#warning-with-public-signup").hide();
                        }
                    }
                    createApplicationLinkWizard.disableNextBtn();
                    createApplicationLinkWizard.disablePreviousBtn();
                    var errorFn = function() {
                        createApplicationLinkWizard.enableNextBtn();
                        createApplicationLinkWizard.enablePreviousBtn();
                    }
                    AppLinks.Wizard.checkReciprocalLinkForm(dialogRootEl, createOneWayLinkFn, gotoNextPageFn, appUrl, errorFn);
                    return false;
                }
                return true;
            },
            onprevious: function() {
                if (createApplicationLinkWizard.dialog.curpage == 2) {
                    createApplicationLinkWizard.dialog.gotoPage(0);
                    return false;
                }
                return true;
            },
            onsubmit: function() {
                var successFn = function(data) {
                    createApplicationLinkWizard.enableSubmitBtn();
                    createApplicationLinkWizard.enablePreviousBtn();
                    createApplicationLinkWizard.cancel();
                    AppLinks.UI.listApplicationLinks(data.applicationLink.id, 'new', data);
                };
                if (typeof manifest.typeId == "undefined") {
                    var applicationLink = {
                        id: manifest.id,
                        typeId: dialogRootEl.find(".application-types").val(),
                        name: dialogRootEl.find(".application-name").val(),
                        rpcUrl: appUrl,
                        displayUrl: appUrl,
                        isPrimary: false
                    },
                        configFormValues = {
                            trustEachOther: false,
                            shareUserbase: false
                        };
                    //NON-UAL
                    createApplicationLinkWizard.disableSubmitBtn();
                    createApplicationLinkWizard.disablePreviousBtn();
                    AppLinks.SPI.createLink(applicationLink, '', '', false, false, "", configFormValues, successFn, AppLinks.UI.displayValidationError('create-non-ual-errors', dialogRootEl, function() {
                            createApplicationLinkWizard.enableSubmitBtn();
                            createApplicationLinkWizard.enablePreviousBtn();
                        }));
                } else {
                    var configFormValues = {
                        trustEachOther: !!(dialogRootEl.find('.trust-each-other-description').find('input').is(':checked')),
                        shareUserbase: !!(dialogRootEl.find('.same-user-description').find('input').is(':checked'))
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
                    createApplicationLinkWizard.disableSubmitBtn();
                    createApplicationLinkWizard.disablePreviousBtn();

                    //APL-548: Is the URL is not the same as the base URL then tell UAL that we are sending along a
                    // custom URL.
                    var isCustomUrl = rpcUrl && rpcUrl !== baseUrl;
                    AppLinks.SPI.createLink(applicationLink, reciprocalLinkUsernameEl.val(), reciprocalLinkPasswordEl.val(), /*create two way link*/ true, isCustomUrl, rpcUrl, configFormValues, successFn, AppLinks.UI.displayValidationError('create-validation-errors', dialogRootEl, function() {
                            createApplicationLinkWizard.enableSubmitBtn();
                            createApplicationLinkWizard.enablePreviousBtn();
                        }));
                }
                return false;
            },
            onshow: function() {
                dialogRootEl.find('.applinks-error').remove();
                AppLinks.UI.hideInfoBox();
                applicationURLEl.val('');
                rpcURLEl.val(baseUrl);
                return true;
            },
            aftershow: function() {
                applicationURLEl.focus();
            }
        },
            createApplicationLinkWizard = $("#create-application-link-container").wizard(wizardSettings),
            dialogRootEl     = $(createApplicationLinkWizard.dialog.popup.element),
            localAppNameEl   = dialogRootEl.find('.local-app-name'),
            applicationURLEl = dialogRootEl.find('#application-url'),
            rpcURLEl = dialogRootEl.find('.reciprocal-rpc-url'),
            reciprocalLinkUsernameEl = dialogRootEl.find('.reciprocal-link-username'),
            reciprocalLinkPasswordEl = dialogRootEl.find('.reciprocal-link-password'),
            baseUrl = $('#baseUrl').val();
            localAppNameEl.html(AppLinks.UI.shortenString(applicationName, 20));


        AppLinks.Wizard.initNonUALUI(dialogRootEl);
        AppLinks.Wizard.initAuthenticationUI(dialogRootEl);

        AppLinks.showAddApplicationLinkWizard = function(url) {
                createApplicationLinkWizard.show();
                if (url) {
                    applicationURLEl.val(url);
                }
            };

        applicationURLEl.keydown(function(event) {
                //KeyCode 13 is the enter key.
                if (event.keyCode == '13') {
                    createApplicationLinkWizard.nextPage();
                }
            });

        reciprocalLinkPasswordEl.keydown(function(event) {
                //KeyCode 13 is the enter key.
                if (event.keyCode == '13') {
                    createApplicationLinkWizard.nextPage();
                }
            });

        rpcURLEl.keydown(function(event) {
                //KeyCode 13 is the enter key.
                if (event.keyCode == '13') {
                    createApplicationLinkWizard.nextPage();
                }
            });
    })(AJS.$);
});
