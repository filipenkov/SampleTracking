AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {
        var dialogDiv = $("#editAppLinkDialog"),
            currentApplication,
            editableDisplayUrl = $('#display-url'),
            editableApplicationName = $('#applicationName'),
            update = function() {
                $('.applinks-error', "#editAppLinkDialog").remove();
                var rawURL = editableDisplayUrl.val().trim();
                if (rawURL == '') {
                    $('<div class="error applinks-error">'+ AppLinks.I18n.getText('applinks.error.rpcurl') +'</div>').insertAfter(editableDisplayUrl);
                    return false;
                }
                if (editableApplicationName.val() == '') {
                    $('<div class="error applinks-error">'+ AppLinks.I18n.getText('applinks.error.appname') +'</div>').insertAfter(editableApplicationName);
                    return false;
                }
                // screen for the most common URL syntax errors
                // this needs to be upgraded to fully implement RFC1738 and refactored to a utility class for reuse
                var badIndex = rawURL.search(/[ \t\n\f\r\v]/);
                if (badIndex > -1) {
                    $('<div class="error applinks-error">'+ AppLinks.I18n.getText('applinks.error.badcharurl', badIndex) +'</div>').insertAfter(editableDisplayUrl);
                    return false;
                }
                if(rawURL.match(/^http/) && !rawURL.match(/^https?:\/\//)) {
                    $('<div class="error applinks-error">'+ AppLinks.I18n.getText('applinks.error.badurlnoslashes') +'</div>').insertAfter(editableDisplayUrl);
                    return false;
                }

                var updated = $.extend(true, {}, currentApplication); //deep copy in case this object changes
                updated.name = $("#applicationName").val();
                var displayURL = AppLinks.UI.addProtocolToURL($("#display-url").val());
                updated.displayUrl = displayURL;
                delete updated.hasOutgoing;
                delete updated.hasIncoming;
                delete updated.webPanels;
                delete updated.webItems;
                wizard.disableSubmitBtn();
                AppLinks.update(updated, function() {
                    currentApplication = updated;
                    wizard.enableSubmitBtn();
                    wizard.dialog.popup.element.find("h2").text(updated.name);
                    wizard.dialog.popup.hide();
                    currentUpdateCallback(currentApplication);
                }, AppLinks.UI.displayValidationError('update-validation-errors', dialogRootEl, function() {
                    wizard.enableSubmitBtn();
                }));
            },
            show = function() {
                $('.applinks-error').remove();
                var dialog = wizard.dialog.popup;
                dialog.element.find("h2").text(AppLinks.I18n.getText('applinks.configure') + " " + currentApplication.name);
                
                $('#applicationTypeValue').text(AppLinks.I18n.getApplicationTypeName(currentApplication.typeId));

                $("#rpc-url").text(currentApplication.rpcUrl);

                $("#applicationName").val(currentApplication.name);
                $('#display-url').val(currentApplication.displayUrl);

                if (currentApplication.isTwoWay) {
                    $("#hasApplinks").html(AppLinks.I18n.getText("applinks.true"));
                }
                else {
                    $("#hasApplinks").html(AppLinks.I18n.getText("applinks.false"));
                }
            },
            currentCancelCallback,
            currentUpdateCallback,
            settings = {
                showButtonId: "edit-application-link",
                cancelLabel: AppLinks.I18n.getTextWithPrefix("close"),
                width: 800,
                height: 520,
                id: "edit-application-link-dialog",
                submitLabel: AppLinks.I18n.getText("applinks.update"),
                onshow: function(popup) {
                    show();
                    var contextPath = AJS.contextPath();
                    if (currentApplication.hasOutgoing === true) {
                        $("#outgoing-auth").attr("src", contextPath + "/plugins/servlet/applinks/auth/conf/config-tabs?direction=OUTBOUND&applicationId=" + currentApplication.id);
                        $($("#edit-application-link-dialog .item-button")[1]).show();
                    } else {
                        $($("#edit-application-link-dialog .item-button")[1]).hide();
                    }
                    if (currentApplication.hasIncoming === true){
                        $("#incoming-auth").attr("src", contextPath + "/plugins/servlet/applinks/auth/conf/config-tabs?direction=INBOUND&applicationId=" + currentApplication.id);
                        $($("#edit-application-link-dialog .item-button")[2]).show();
                    } else {
                        $($("#edit-application-link-dialog .item-button")[2]).hide();
                    }
                    return true;
                },
                onsubmit: function(popup) {
                    dialogRootEl.find('.applinks-error').remove();
                    update();
                    return false;
                },
                oncancel: function() {
                    currentCancelCallback();
                    return true;
                }
            },
            wizard = dialogDiv.wizard(settings);
            var dialogRootEl     = $(wizard.dialog.popup.element);

        $('#edit-application-link-dialog .item-button').each(function(index) {
            var locationStr = "" + parent.location,
                $wizardSubmit = $('#edit-application-link-dialog .wizard-submit');
            if (locationStr.indexOf('#') != -1) {
                locationStr = locationStr.substring(0, locationStr.indexOf('#'));
            }
            if (index == 1 || index == 2) {
                $(this).click(function() {
                    $wizardSubmit.hide();
                    var $activePane = $(".active-pane", document.getElementById((index == 1 ? "outgoing-auth" : "incoming-auth")).contentWindow.document),
                        $iframe = $activePane.children("iframe"),
                        $loadIcon = $activePane.children(".loading.loading-tabs");
                    $loadIcon.show();
                    $iframe.attr('src', $iframe.attr('src'));
                });
            } else {
                $(this).click(function() {
                    $wizardSubmit.show();
                });
            }
        });
        
        AppLinks.editAppLink = function(application, authType, showIncoming, onupdate, oncancel) {
            currentApplication = application;
            currentUpdateCallback = onupdate || function() {
                AppLinks.UI.refreshOrphanedTrust();
                return true;
            };
            currentCancelCallback = oncancel || function() {
                AppLinks.UI.refreshOrphanedTrust();
                return true;
            };
            wizard.show();
            var itemButtonNumber;
            var authIframeId;
            
            if (showIncoming) {
                itemButtonNumber = 2;
                authIframeId = "incoming-auth";
            } else {
                itemButtonNumber = 1;
                authIframeId = "outgoing-auth";
            }
            
            if (authType != "undefined") {
            	$('#edit-application-link-dialog .item-button')[itemButtonNumber].click();
                $('#outgoing-auth').load( function() {
                    var iframeDocument = document.getElementById(authIframeId).contentWindow.document;
                    var $configTab = $("." + authType, iframeDocument);
                    $configTab.parent().addClass('active-tab').siblings().removeClass('active-tab');
                    var $configPane = $($configTab.attr('href'), iframeDocument);
                    $configPane.addClass('active-pane').siblings().removeClass('active-pane');
                });
            } else {
                $('#' + authIframeId).unbind('load');
            }
        };
    })(AJS.$);
});
