AJS.$(document).bind(AppLinks.Event.PREREADY, function() {
    (function($) {
        AJS.$.extend(AppLinks.UI || {}, {
            listApplicationLinks: function(appLinkIdOrName, operation, status) {
                $('.no-links').hide();
                $('.links-loading').show();
                $('.relocate-warning').closest('.aui-message.warning').remove();
                $('.upgrade-warning').closest('.aui-message.warning').remove();
                $('#application-links-table').hide();
                $('#applicationsList').empty()
                AppLinks.SPI.getAllLinksWithAuthInfo(function(applicationList) {
                    var tableBody = $('#applicationsList'),
                        applicationLinksAndAuthInfo = applicationList.list,
                        createRow = function(application, appLinkState, incomingAuth, auth) {
                            var extensions = {
                                    typeLabel: AppLinks.I18n.getApplicationTypeName(application.typeId)
                                },
                                row = $(AJS.template.load('al-row')
                                        .fill($.extend(extensions, application))
                                        .fillHtml({configuredIncomingAuth: incomingAuth})
                                        .fillHtml({configuredAuth: auth})
                                        .fillHtml({iconTag: application.iconUrl ?
                                            AJS.template.load('al-row-application-icon').fill({iconUrl: application.iconUrl}).toString() :
                                            ""})
                                        .toString());

                            row.find(".app-delete-link").click(function(e) {
                                e.preventDefault();
                                AppLinks.UI.hideInfoBox();
                                AppLinks.SPI.getApplicationLinkInfo(application.id, function(appLinkInfo) {
                                    var confirmMessage = $('<div>');
                                    confirmMessage.append($('<div>').text(AppLinks.I18n.getText('applinks.dialog.delete.application.link', [application.name, AppLinks.I18n.getApplicationTypeName(application.typeId)])));
                                    if (appLinkInfo.configuredAuthProviders.length > 0){
                                        var authProviderMessage = AppLinks.UI.prettyJoin(appLinkInfo.configuredAuthProviders, function(value) {
                                            return AppLinks.I18n.getAuthenticationTypeName(value);
                                        });
                                        confirmMessage.append($('<div>').text(AppLinks.I18n.getText('applinks.dialog.delete.authentication', authProviderMessage)));

                                        if (appLinkInfo.numConfiguredEntities > 0) {
                                            var linkI18nKey;
                                            if (appLinkInfo.numConfiguredEntities > 1){
                                                linkI18nKey = "applinks.dialog.delete.entity.links.plural";
                                            } else {
                                                linkI18nKey = "applinks.dialog.delete.entity.links.singular";
                                            }

                                            var hostEntityTypeLabel = AppLinks.UI.prettyJoin(appLinkInfo.hostEntityTypes, function(value) {
                                                if (appLinkInfo.numConfiguredEntities > 1) {
                                                    return AppLinks.I18n.getPluralizedEntityTypeName(value);
                                                } else {
                                                    return AppLinks.I18n.getEntityTypeName(value);
                                                }
                                            });
                                            var remoteEntityTypeLabel = AppLinks.UI.prettyJoin(appLinkInfo.remoteEntityTypes, function(value) {
                                                return AppLinks.I18n.getPluralizedEntityTypeName(value);
                                             });
                                            var entityLinksMsg = AppLinks.I18n.getText(linkI18nKey, [appLinkInfo.numConfiguredEntities, hostEntityTypeLabel, remoteEntityTypeLabel, application.name]);
                                            confirmMessage.append($('<div>').text(entityLinksMsg));
                                        }
                                    }
                                    var deleteDialogSettings = {
                                        title: AppLinks.I18n.getText("applinks.delete.long", application.name),
                                        applicationId: application.id,
                                        confirmMessage: confirmMessage,
                                        reciprocalLinkMessage: AppLinks.I18n.getText("applinks.dialog.delete.reciprocal.application.link", [application.name, AppLinks.I18n.getApplicationTypeName(application.typeId)]),
                                        noReciprocalLinkMessage: AppLinks.I18n.getText("applinks.dialog.delete.reciprocal.no.application.link", [application.name, AppLinks.I18n.getApplicationTypeName(application.typeId)]),
                                        deleteYesMessage: AppLinks.I18n.getText("applinks.dialog.delete.reciprocal.application.link.yes", [application.name, AppLinks.I18n.getApplicationTypeName(application.typeId)]),
                                        deleteNoMessage: AppLinks.I18n.getText("applinks.dialog.delete.reciprocal.application.link.no", [application.name, AppLinks.I18n.getApplicationTypeName(application.typeId)]),
                                        authenticationMessage: AppLinks.I18n.getText('applinks.dialog.authentication.required.application.link', [application.name, AppLinks.I18n.getApplicationTypeName(application.typeId)]),
                                        noConnectionMessage: AppLinks.I18n.getText('applinks.dialog.delete.link.no.connection'),
                                        authenticationFailedMessage: AppLinks.I18n.getText('applinks.dialog.application.link.authorization.failed', [application.name, AppLinks.I18n.getApplicationTypeName(application.typeId)]),
                                        failedToDetectReciprocalLinkMessage : AppLinks.I18n.getText('applinks.dialog.application.failed.to.detect.reciprocal.link', [application.name, AppLinks.I18n.getApplicationTypeName(application.typeId)]),
                                        helpKey: 'applinks.docs.delete.application.link',
                                        doPermissionCheck: function(success, error) {
                                            AppLinks.SPI.canDeleteAppLink(application.id, success, error);
                                        },
                                        doDelete: function(reciprocate, success, error) {
                                            AppLinks.SPI.deleteLink(application, reciprocate, success, error);
                                        },
                                        callback: function() {
                                            AppLinks.UI.listApplicationLinks(application.name, 'delete');
                                            return true;
                                        }
                                    };
                                    AppLinks.showDeleteLinkDialog(deleteDialogSettings);
                                });
                            });

                            $.each(row.find('.configure-auth-type'), function(i, v) {
                                var authProvider = $(v).attr('data-auth-type');
                                var authDirection = $(v).attr('data-auth-direction');
                                var isInbound = authDirection == "incoming";
                            	$(v).click(function(e) {
                            		e.preventDefault();
                                    AppLinks.UI.hideInfoBox();
                                    AppLinks.editAppLink(application, authProvider, isInbound, function(updated) {
                                        AppLinks.UI.listApplicationLinks(updated.id, 'update');
                                        return true;
                                        }, function() {
                                        AppLinks.UI.listApplicationLinks();
                                        return true;
                                    });
                            	});
                            });

                            row.find(".app-edit-link").click(function(e) {
                                e.preventDefault();
                                AppLinks.UI.hideInfoBox();
                                AppLinks.editAppLink(application, "undefined", false, function(updated) {
                                    AppLinks.UI.listApplicationLinks(updated.id, 'update');
                                    return true;
                                    }, function() {
                                    AppLinks.UI.listApplicationLinks();
                                    return true;
                                });

                            });

                            if (!application.isPrimary) {
                                row.find(".app-toggleprimary-action").show();
                                row.find(".app-toggleprimary-link").click(function(e) {
                                    e.preventDefault();
                                    AppLinks.UI.hideInfoBox();
                                    AppLinks.SPI.makePrimary(application, function() {
                                        AppLinks.UI.listApplicationLinks(application.id, 'primary');
                                    });
                                });
                            } else {
                                row.find(".app-toggleprimary-action").hide();
                            }
                            if (appInfo.appLinkState == 'OFFLINE') {
                                var relocateLink = $("<div>");
                                relocateLink.text(AppLinks.I18n.getText("applinks.relocate.link", application.name) + " ");
                                $(relocateLink).append("<a class='relocate-" + application.id  + " relocate-from-" + application.rpcUrl + " relocate-warning' href='#'>" + AppLinks.I18n.getText("applinks.relocate.link.action") + "</a>");
                                AppLinks.UI.showWarningBox(relocateLink);
                                var relocateLink = $('.relocate-' + application.id);
                                relocateLink.click(function(e) {
                                    e.preventDefault();
                                    AppLinks.UI.hideInfoBox();
                                    var relocateDialogSettings = {
                                        description: AppLinks.I18n.getText("applinks.relocate.long", application.name),
                                        application: application,
                                        helpKey: 'applinks.docs.relocate.application.link',
                                        doRelocate: function(newUrl, success, error) {
                                            AppLinks.SPI.relocate(application, newUrl, false, success, error);
                                        },
                                        doForceRelocate: function(newUrl, success, error) {
                                            AppLinks.SPI.relocate(application, newUrl, true, success, error);
                                        },
                                        callback: function() {
                                            AppLinks.UI.listApplicationLinks(application.name, 'relocate');
                                        }
                                    };
                                    AppLinks.showRelocateLinkDialog(relocateDialogSettings);
                                });
                            } else if (appInfo.appLinkState != 'OK') {
                                var upgradeLink = $("<div>");
                                var message = AppLinks.I18n.getText("applinks.upgrade.link", application.name);
                                var action = AppLinks.I18n.getText("applinks.upgrade.link.action");
                                upgradeLink.text(message + " ");

                                upgradeLink.append("<a class='upgrade-" + application.id +" upgrade-warning' href='#'>" + action + "</a>");
                                AppLinks.UI.showWarningBox(upgradeLink);

                                var upgradeFn = function(appInfo) {
                                    return function (e) {
                                        e.preventDefault();
                                        AppLinks.UI.hideInfoBox();
                                        if (appInfo.appLinkState == 'UPGRADED_TO_UAL') {
                                            var upgradeDialogSettings = {
                                                application: application,
                                                description: AppLinks.I18n.getText("applinks.ual.upgrade.description", application.name),
                                                manifest: {
                                                    name: application.name,
                                                    typeId: application.typeId
                                                },
                                                successCallback: function(upgradedApplicationLink) {
                                                    // called when the upgrade completed successfully
                                                    AppLinks.UI.listApplicationLinks(upgradedApplicationLink.applicationLink.name, 'upgrade', upgradedApplicationLink);
                                                }
                                            };
                                            AppLinks.showUpgradeLinkToUALDialog(upgradeDialogSettings);
                                        } else {
                                            var localEntityTypeString = AppLinks.UI.prettyJoin(localEntityTypeIdStrings,
                                                    function(key) {
                                                        return AppLinks.I18n.getEntityTypeName(key);
                                                    }, AppLinks.I18n.getText("applinks.and"));
                                            var upgradeDialogSettings = {
                                                application: application,
                                                description: AppLinks.I18n.getText("applinks.legacy.upgrade.description", [application.name, localEntityTypeString]),
                                                helpKey: 'applinks.docs.upgrade.application.link',
                                                submit: function(success, error) {
                                                    AppLinks.SPI.legacyUpgrade(application, success, error);
                                                },
                                                callback: function(newApplicationId) {
                                                    application.applicationId = newApplicationId;
                                                    AppLinks.UI.listApplicationLinks(application.name, 'upgrade');
                                                }
                                            };
                                            AppLinks.showUpgradeLinkDialog(upgradeDialogSettings);
                                        }
                                    }
                                }

                                $('.upgrade-' + application.id).click(upgradeFn(appInfo));
                            }

                            var toAttrib = function(key, value) {
                                return (typeof value == "undefined" || value == null || value == "") ?
                                        "" : (key + "='" + value + "'");
                            };
                            var actions = row.find("ul.app-actions");
                            for (var i = 0; i < application.webItems.length; i++) {
                                var wi = application.webItems[i];
                                // resolve the label i18n key
                                wi.label = AppLinks.I18n.getText(wi.label);
                                // render the icon html (if needed)
                                if (wi.iconUrl) {
                                    wi.icon = AJS.template.load('al-row-op-webitem-icon').fill(wi).toString()
                                } else {
                                    wi.icon = "";
                                }
                                wi.id = toAttrib("id", wi.id);
                                wi.tooltip = toAttrib("tooltip", wi.tooltip);
                                wi.styleClass = toAttrib("styleClass", wi.styleClass);
                                wi.accessKey = toAttrib("accessKey", wi.accessKey);
                                // add the rendered web-item to the row
                                actions.append(AJS.template.load('al-row-op-webitem').fill(wi).toString());
                            }
                            for (var i = 0; i < application.webPanels.length; i++) {
                                var wp = application.webPanels[i];
                                actions.append($("<li>| " + wp.html + "</li>"));
                            }

                            return row;
                        };

                    var modifiedApplicationLinkAndAuthInfo;
                    $('.links-loading').hide();
                    if (applicationLinksAndAuthInfo.length == 0) {
                        $('#application-links-table').hide();
                        var nolinks = $('.no-links');
                        nolinks.text(AppLinks.I18n.getText("applinks.no.links.configured") + " ");
                        nolinks.append($("<div href='#' id='add-first-application-link'>").text(AppLinks.I18n.getText('applinks.create.first.link')));
                        nolinks.show();
                        $('#add-first-application-link').click(function(e) {
                            e.preventDefault();
                            $('#add-application-link').click();
                        });
                    } else {
                        $('#application-links-table').show();
                        $('.no-links').hide();
                    }

                    var multipleTypes = false,
                        tempTypeId;
                    for (var x = 0, xx = applicationLinksAndAuthInfo.length; x < xx; x++) {
                        var appInfo = applicationLinksAndAuthInfo[x],
                            application = appInfo.application;
                        application.hasIncoming = appInfo.hasIncomingAuthenticationProviders;
                        application.hasOutgoing = appInfo.hasOutgoingAuthenticationProviders;
                        application.webItems = appInfo.webItems;
                        application.webPanels = appInfo.webPanels;
                        if (!multipleTypes && tempTypeId && tempTypeId == application.typeId) {
                            multipleTypes = true;
                        }
                        tempTypeId = application.typeId;
                        if (appLinkIdOrName && application.id == appLinkIdOrName) {
                            modifiedApplicationLinkAndAuthInfo = appInfo;
                        }
                        var clickToConfigure = AppLinks.I18n.getText('applinks.click.to.configure');

                        var incomingAuth = "";
                        var numConfiguredIncomingAuth = appInfo.configuredInboundAuthenticators.length;
                        $.each(appInfo.configuredInboundAuthenticators, function(i, value) {
                            var authType = value.substring(value.lastIndexOf('.') + 1);
                            var incomingTemplate = AJS.template('<div><a href="#" class="configure-auth-type" data-auth-type="{authType}" data-auth-direction="incoming" title="{linkTitle}">{name}</a>');
                            
                            incomingAuth += incomingTemplate.fill({
                                authType: authType,
                                linkTitle: clickToConfigure + ' ' + AppLinks.I18n.getAuthenticationTypeName(value),
                                name: AppLinks.I18n.getAuthenticationTypeName(value)
                            });
                        });
                        if (numConfiguredIncomingAuth == 0) {
                            incomingAuth = "<div>" + AppLinks.I18n.getText('applinks.no.authentication.configured') + "</div>";
                        }
                        
                        var configuredAuth = "";
                        var numConfiguredAuth = appInfo.configuredOutboundAuthenticators.length;
                        $.each(appInfo.configuredOutboundAuthenticators, function(i, value) {
                        	if (i == 0 && (numConfiguredAuth > 1)){
                        		configuredAuth = '<div class="primary-auth-type-wrapper"><span class="primary-auth-type" title="' + AppLinks.I18n.getText('applinks.primary.auth') + '">&nbsp;</span>';
                        	} else if (numConfiguredAuth == 1) {
                        		configuredAuth += '<div>';
                        	} else {
                                configuredAuth += '<div class="configured-auth-type">';
                            }
                            var authType = value.substring(value.lastIndexOf('.') + 1);

                            var outboundTemplate = AJS.template('<a href="#" class="configure-auth-type" data-auth-type="{authType}" data-auth-direction="outgoing" title="{linkTitle}">{name}</a>')
                            
                        	configuredAuth += outboundTemplate.fill({
                                authType: authType,
                                linkTitle: clickToConfigure + ' ' + AppLinks.I18n.getAuthenticationTypeName(value),
                                name: AppLinks.I18n.getAuthenticationTypeName(value)
                            });
                        	
                        	configuredAuth += "</div>";
                        });
                        if (numConfiguredAuth == 0) {
                            configuredAuth = "<div>" + AppLinks.I18n.getText('applinks.no.authentication.configured') + "</div>";
                        }
                        tableBody.append(createRow(application, appInfo.appLinkState, incomingAuth, configuredAuth));
                    }

                    if (multipleTypes) {
                        $('.primary-column').show();
                    } else {
                        $('.primary-column').hide();
                    }

                    if (appLinkIdOrName) {

                        if (operation == 'new') {
                            var message;
                            if (!modifiedApplicationLinkAndAuthInfo || !modifiedApplicationLinkAndAuthInfo.configuredOutboundAuthenticators || modifiedApplicationLinkAndAuthInfo.configuredOutboundAuthenticators.length == 0) {
                                message = AppLinks.I18n.getText("applinks.link.create.success.no.authentication", AppLinks.UI.sanitiseHTML(modifiedApplicationLinkAndAuthInfo.application.name));
                            } else if (modifiedApplicationLinkAndAuthInfo.configuredOutboundAuthenticators && modifiedApplicationLinkAndAuthInfo.configuredOutboundAuthenticators.length == 1) {
                                message = AppLinks.I18n.getText("applinks.link.create.success.with.authentication", [AppLinks.UI.sanitiseHTML(modifiedApplicationLinkAndAuthInfo.application.name), AppLinks.I18n.getAuthenticationTypeName(modifiedApplicationLinkAndAuthInfo.configuredOutboundAuthenticators[0])]);
                            } else {
                                message = AppLinks.I18n.getText("applinks.link.create.success.authentication.types", [AppLinks.UI.sanitiseHTML(modifiedApplicationLinkAndAuthInfo.application.name), modifiedApplicationLinkAndAuthInfo.configuredOutboundAuthenticators.length]);
                            }

                            if (!status.autoConfigurationSuccessful) {
                                message += " " + AppLinks.I18n.getText("applinks.link.create.autoconfiguration.failed");
                            }
                            AppLinks.UI.showInfoBox(message);

                            if (modifiedApplicationLinkAndAuthInfo.configuredOutboundAuthenticators.length == 0) {
                                $(".page-info").append(" " + AppLinks.I18n.getText("applinks.link.create.configure.authentication", ["<a href='#' id='edit-new-link'>", "</a>"]));
                            }

                            /**
                             * Now render the suggestion for the next step:
                             * "Next you should link a Charlie to a FishEye Repository or Crucible Project. You can do this from the Charlie admin page."
                             */
                            var localEntityTypeString = AppLinks.UI.prettyJoin(localEntityTypeIdStrings,
                                    function(key) {
                                        return AppLinks.I18n.getEntityTypeName(key);
                                    }, AppLinks.I18n.getText("applinks.or"));
                            var remoteEntityTypeString = AppLinks.UI.prettyJoin(modifiedApplicationLinkAndAuthInfo.entityTypeIdStrings,
                                    function(key) {
                                        return AppLinks.I18n.getEntityTypeName(key);
                                    }, AppLinks.I18n.getText("applinks.or"));
                            var suggestion = AppLinks.I18n.getText("applinks.link.create.linksuggestion",
                                    [localEntityTypeString, remoteEntityTypeString]);
                            $(".page-info").append("<br>" + suggestion);

                            $('#edit-new-link').click(function(e) {
                                e.preventDefault();
                                AppLinks.UI.hideInfoBox();
                                AppLinks.editAppLink(modifiedApplicationLinkAndAuthInfo.application, 'undefined', false, function(updated) {
                                    AppLinks.UI.listApplicationLinks(updated.id, 'update');
                                    return true;
                                }, function() {
                                    AppLinks.UI.listApplicationLinks();
                                    return true;
                                });
                            });
                        } else if (operation == 'update') {
                            AppLinks.UI.showInfoBox($('<span>').text(AppLinks.I18n.getText('applinks.link.update.success', modifiedApplicationLinkAndAuthInfo.application.name)));
                        } else if (operation == 'relocate') {
                            AppLinks.UI.showInfoBox($('<span>').text(AppLinks.I18n.getText('applinks.link.relocate.success', appLinkIdOrName)));
                        } else if (operation == 'upgrade') {
                            AppLinks.UI.showInfoBox($('<span>').text(AppLinks.I18n.getText('applinks.link.upgrade.success', appLinkIdOrName)));
                            if (status && status.message && AJS.$.isArray(status.message) && status.message.length > 0) {
                                AppLinks.UI.showWarningBox(status.message);
                            }
                        } else if (operation == 'delete') {
                            AppLinks.UI.showInfoBox(AppLinks.I18n.getText('applinks.link.delete.success', AppLinks.UI.sanitiseHTML(appLinkIdOrName)));
                        } else if (operation == 'primary') {
                            var applicationTypeLabel = AppLinks.I18n.getApplicationTypeName(modifiedApplicationLinkAndAuthInfo.application.typeId);
                            AppLinks.UI.showInfoBox(AppLinks.I18n.getText('applinks.link.new.primary', [applicationTypeLabel, AppLinks.UI.sanitiseHTML(modifiedApplicationLinkAndAuthInfo.application.name)]));
                        }
                    }
                    tableBody.addClass("fully-loaded")
                }, function(data) {
                    $('.links-loading').hide();
                    var message = AppLinks.parseError(data);
                    AppLinks.UI.showErrorBox(message);
                });

                AppLinks.UI.refreshOrphanedTrust();
            }
        })
    })(AJS.$)
});
AJS.$(document).bind(AppLinks.Event.READY, function() {
    AppLinks.UI.listApplicationLinks();
});
