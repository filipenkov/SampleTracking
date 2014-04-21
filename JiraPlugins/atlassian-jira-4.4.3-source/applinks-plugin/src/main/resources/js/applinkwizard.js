(function($) {
    AppLinks.Wizard = {
        initAuthenticationUI: function(element) {
            var root = $(element);
            var createTwoWayLinkCheckbox = root.find('.create-reciprocal-link');
            var ualArrow = root.find('.ual-arrow');
            var linkDetails = root.find('.two-way-link-details');
            var linkDescription = root.find('.reciprocal-link-description');
            var noLinkDescription = root.find('.no-reciprocal-link-description');
            createTwoWayLinkCheckbox.click(function() {
                if (createTwoWayLinkCheckbox.attr('checked')) {
                    ualArrow.removeClass('no-background');
                    linkDetails.show();
                    linkDescription.show();
                    noLinkDescription.hide();
                } else {
                    ualArrow.addClass('no-background');
                    linkDetails.hide();
                    linkDescription.hide();
                    noLinkDescription.show();
                }
            });
            var sameUserBtn = root.find('.same-user-radio-btn');
            var differentUserBtn = root.find('.different-user-radio-btn');
            var differentUserBaseImage = root.find('.different-userbase-image');
            var sameUserBaseImage = root.find('.same-userbase-image');

            sameUserBtn.click(function() {
                differentUserBaseImage.addClass('different-userbase-image-grey');
                sameUserBaseImage.removeClass('same-userbase-image-grey');
            });

            differentUserBtn.click(function() {
                sameUserBaseImage.addClass('same-userbase-image-grey');
                differentUserBaseImage.removeClass('different-userbase-image-grey');
            });
        },

        initNonUALUI : function(element) {
            var root = $(element);
            var applicationTypesEl = root.find('.application-types');
            for (var i = 0; i < nonAppLinksApplicationTypes.length; i++) {
                $('<option value=\"' + nonAppLinksApplicationTypes[i].typeId + '\">' + nonAppLinksApplicationTypes[i].label + '</option>').appendTo(applicationTypesEl);
            }
        },
        fetchManifest : function(wizard, dialogRootEl, ualAppCallbackFn, nonUALAppCallbackFn) {
            var applicationURLEl = dialogRootEl.find('#application-url');
            if (applicationURLEl.val() == '') {
                var applicationTypeEl = dialogRootEl.find('#application-types');
                if (applicationTypeEl.val() == '') {
                    $('<div class="error applinks-error">' + AppLinks.I18n.getText('applinks.error.url.field.empty') + '</div>').insertAfter(applicationURLEl);
                    return false;
                }
                var success = function(data) {
                    wizard.enableSubmitBtn();
                    wizard.enablePreviousBtn();
                    wizard.cancel();
                    AppLinks.UI.listApplicationLinks(data.applicationLink.id, 'new', data);
                };
                AppLinks.SPI.createStaticUrlAppLink(applicationTypeEl.val(), success, null);
                return true;
            }
            var appUrl = AppLinks.UI.addProtocolToURL(applicationURLEl.val());

            AppLinks.UI.showLoadingIcon(applicationURLEl);
            var success = function(data) {
                var manifest = data;
                wizard.enableNextBtn();
                dialogRootEl.find('.loading').remove();
                dialogRootEl.find('.reciprocal-rpc-url').val($('#baseUrl').val());
                if (typeof data.typeId != "undefined") {
                    AppLinks.Wizard.handleUALManifest(manifest, dialogRootEl);
                    wizard.dialog.gotoPage(2);
                    dialogRootEl.find('.reciprocal-link-username').focus();
                    if (ualAppCallbackFn) {
                      ualAppCallbackFn(manifest);
                    }
                }
                else {
                    AppLinks.Wizard.handleNonUALManifest(manifest, appUrl, dialogRootEl);
                    wizard.dialog.gotoPage(1);
                    dialogRootEl.find('.application-name').focus();
                    if (nonUALAppCallbackFn) {
                      nonUALAppCallbackFn(manifest);
                    }
                }
            };
            wizard.disableNextBtn();
            AppLinks.SPI.tryToFetchManifest(appUrl, success, AppLinks.UI.displayValidationError('manifest-validation-errors', dialogRootEl, function() {
                wizard.enableNextBtn();
            }));
            return appUrl;
        },
        handleUALManifest : function(manifest, element) {
            var root = $(element);
            root.find('.remote-app-image').removeClass( function(index, className) {
            var classes = className.split(' ');
            var classToRemove = "";
            $.each(classes, function(index, value) {
                if (value.indexOf('application-type-image-') != -1) {
                    classToRemove = value;
                }
            });
            return classToRemove;
            });
            root.find('.remote-app-image').addClass('application-type-image-' + manifest.typeId);
            root.find('.link-to-app-type').html(AppLinks.I18n.getText('applinks.create.title.link.to', AppLinks.I18n.getApplicationTypeName(manifest.typeId)));
            root.find('.remote-app-name').html(AppLinks.UI.shortenString(manifest.name, 20));
            root.find('.create-reciprocal-link').attr('checked','true');
            root.find('#reciprocal-link-back-to-server').html(AppLinks.I18n.getText('applinks.create.link.back.to.server', manifest.name));
            // For the two way link text, we need to specify different roles for the credentials depending on the remote app
            // For JIRA and Confluence, it is system administrator
            // For everything else, it is administrator
            var twoWayLinkParams = ["administrator", manifest.name,
                             '<a target="_blank" href="' + AppLinks.Docs.getDocHref("applinks.docs.adding.application.link") + '">', '</a>'];
            if (manifest.typeId == "jira" || manifest.typeId == "confluence") {
                twoWayLinkParams[0] = "system administrator";
            }
            root.find('.reciprocal-link-description').html(AppLinks.I18n.getText('applinks.create.two.way.link', twoWayLinkParams));
            root.find('.no-reciprocal-link-description').hide();
            root.find('.no-reciprocal-link-description').html(AppLinks.I18n.getText('applinks.create.two.way.no.link', manifest.name));
            root.find('.reciprocal-link-username').val('');
            root.find('.reciprocal-link-password').val('');
            root.find('.ual-arrow').removeClass('no-background');
            root.find('.two-way-link-details').show();
            root.find('.reciprocal-link-description').show();
            root.find('.no-reciprocal-link-description').hide();
        },
        handleNonUALManifest : function(data, appUrl, element) {
            var root = $(element);
            root.find('.application-name').val('');
            root.find(".application-types option:first-child").attr("selected", "selected");
            root.find('.non-ual-application-url').text(appUrl);
            if (data.warning) {
                root.find('.create-non-ual-warning').show();
                root.find('.create-non-ual-warning').html(data.warning);
            } else {
                root.find('.create-non-ual-warning').hide();
            }
        },
        checkReciprocalLinkForm : function(element, handleOneWayLinkFn, handleTwoWayLinkDetailsSuccess, appUrl, errorFn) {
            var root = $(element);
            if (root.find('.create-reciprocal-link').attr('checked')) {
                var reciprocalRPCURL = $.trim(root.find('.reciprocal-rpc-url').val());
                if (reciprocalRPCURL == '') {
                    $("<div class='error applinks-error'>" + AppLinks.I18n.getText('applinks.error.url.field.empty') + "</div>").insertAfter(root.find('.reciprocal-rpc-url'))
                    if (errorFn) {
                        errorFn();
                    }
                    return;
                }
                var reciprocalLinkUsername = root.find('.reciprocal-link-username');
                var reciprocalLinkPwd = root.find('.reciprocal-link-password');
                if (reciprocalLinkUsername.val() == '') {
                    $('<div class="error applinks-error">'+ AppLinks.I18n.getText('applinks.error.username.empty') +'</div>').insertAfter(reciprocalLinkUsername);
                    if (errorFn) {
                        errorFn();
                    }
                    return false;
                }

                //verify user has admin rights.
                var successFn = function(data) {
                    root.find('.same-user-description').find('input').attr('checked','true');
                    root.find(".trust-radio-btn").attr('checked', 'true');
                    root.find('.same-user-radio-btn').click();
                    handleTwoWayLinkDetailsSuccess(data);
                };
                reciprocalRPCURL = AppLinks.UI.addProtocolToURL(reciprocalRPCURL);
                AppLinks.SPI.verifyTwoWayLinkDetails(appUrl, reciprocalRPCURL, reciprocalLinkUsername.val(), reciprocalLinkPwd.val(), successFn, AppLinks.UI.displayValidationError('two-way-link-errors', element, errorFn));
                return false;
            } else {
                handleOneWayLinkFn();
                return false;
            }
        }
    }
})(AJS.$);
