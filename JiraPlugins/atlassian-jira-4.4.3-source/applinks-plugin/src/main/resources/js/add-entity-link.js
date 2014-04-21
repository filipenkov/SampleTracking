AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {

        var selectedApplicationLinkId;
        var selectedApplicationName;
        var selectedApplicationType;
        //Flag which indicates, if the user has made a selection from the drop-down list
        //of entity links.
        var dropDownSelection = false;
        var applicationSupportsUAL = false;
        var typeSelect = $("#add-non-ual-entity-link-type");
        var entityInput = $('#add-entity-link-entity');
        var canCreateReciprocalLink = false;
        var selectedRemoteEntity;

        var showErrorMessage = function(message) {
            AppLinks.UI.showErrorBox(message);
        };

        var remoteEntities = new Array();
        var configuredEntities = new Array();

        var loadEntities = function(applicationLinkID, anonymousAccess) {
            var handleLoadEntities = function(data) {
                remoteEntities = new Array();
                //These types of entities are available from the remote system.
                var availableRemoteEntityTypes = {};
                for (var i = 0; i < data.entity.length; i++) {
                    var entity = data.entity[i];
                    var configured = false;
                    $(configuredEntities).each( function(index) {
                        if (configuredEntities[index].key === entity.key &&
                            configuredEntities[index].typeId === entity.typeId) {
                            configured = true;
                        }
                    });
                    if (!configured) {
                        remoteEntities.push(entity);
                    }
                }

                $.each(remoteEntities, function(i,v) {
                    if ( !availableRemoteEntityTypes[v.typeId]) {
                        availableRemoteEntityTypes[v.typeId] = 1;
                    } else {
                        var num = availableRemoteEntityTypes[v.typeId];
                        availableRemoteEntityTypes[v.typeId] = num + 1;
                    }
                });
                var message;
                $('.ual-entity-links').each(function() {
                    $(this).show();
                });
                $('.add-entity-link-single-value').hide();
                entityInput.show();

                var anonymousKey = "";
                if (anonymousAccess == true) {
                    anonymousKey = ".anonymous";
                }
                if (remoteEntities.length == 1) {
                    entityInput.val($.trim(remoteEntities[0].name));
                    entityInput.hide();
                    $('.add-entity-link-single-value').text(remoteEntities[0].name + ' (' + remoteEntities[0].key + ')');
                    $('.add-entity-link-single-value').css('display', '');
                    var entityLabel;

                    //availableRemoteEntityTypes is a JS object and works like a map.
                    //It contains in this case only one entry, which is the type of the unlinked entity.
                    $.each(availableRemoteEntityTypes, function(key) {
                        entityLabel = AppLinks.I18n.getEntityTypeName(key);
                    });
                    message = AppLinks.I18n.getText("applinks.entity.links.one.link.available" + anonymousKey, entityLabel);

                    if (!anonymousAccess && canCreateReciprocalLink) {
                        $('.add-entity-link-single-value').css('bottom', '125px');
                        $('.create-reciprocal-entity-link-label').text(AppLinks.I18n.getText('applinks.entity.links.create.reciprocal.link.yes', [entityLabel, remoteEntities[0].name, localEntity.attr('data-type-label'), localEntity.attr('data-name')]));
                        $('.no-reciprocal-entity-link-label').text(AppLinks.I18n.getText('applinks.entity.links.create.reciprocal.link.no', [entityLabel, remoteEntities[0].name, localEntity.attr('data-type-label'), localEntity.attr('data-name')]));
                        $('#create-reciprocal-entity-link').attr('checked', true);
                        $('.reciprocate-entity-link').show();
                    }

                    addEntityLinkWizard.enableSubmitBtn();
                } else if (remoteEntities.length == 0) {

                    if (configuredEntities.length == 0) {
                      message = AppLinks.I18n.getText("applinks.entity.links.no.links.accessible" + anonymousKey);
                    } else {
                      message = AppLinks.I18n.getText("applinks.entity.links.no.links.available" + anonymousKey);
                    }
                    $('.ual-entity-links').each( function() {
                        $(this).hide();
                    });
                    addEntityLinkWizard.disableSubmitBtn(false);
                } else {
                    var labels = "";
                    $.each(availableRemoteEntityTypes, function(i,v) {
                        if (labels != "") {
                            labels += ", ";
                        }
                        labels += v + " " + AppLinks.I18n.getPluralizedEntityTypeName(i);
                    });
                    message = AppLinks.I18n.getText("applinks.entity.links.multiple.links.available" + anonymousKey,labels);
                    addEntityLinkWizard.disableSubmitBtn(false);
                }
                $('#available-entities-msg').show();
                $('#available-entities-msg').text(message);
                $('.entity-loading').remove();
            }
            if (anonymousAccess) {
                AppLinks.SPI.getEntityLinksForApplicationUsingAnonymousAccess(applicationLinkID, handleLoadEntities, AppLinks.UI.displayValidationError('validation-ual-error', dialogRootEl))
            } else {
                AppLinks.SPI.getEntityLinksForApplication(applicationLinkID, handleLoadEntities, AppLinks.UI.displayValidationError('validation-ual-error', dialogRootEl));
            }
        };

        var loadEntityTypes = function(type) {

            var populateTypeSelect = function(data) {
                AppLinks.UI.hideLoadingIcon(typeSelect);
                var message = AppLinks.UI.prettyJoin(data.entityTypes, function(entityType) {
                    return AppLinks.I18n.getEntityTypeName(entityType.typeId);
                }, AppLinks.I18n.getText('applinks.or'));

                $('.non-ual-entity-msg').html( AppLinks.I18n.getText('applinks.entity.links.message', message) );
                var entityCount = data.entityTypes.length;
                if (entityCount < 1) {
                    AppLinks.failure(AppLinks.I18n.getText("applinks.entity.links.error.no.entity.types.for.application"));
                    return;
                } else if (entityCount === 1) {
                    var entityType = data.entityTypes[0];
                    typeSelect.html("<span class='field-value' id='add-non-ual-entity-link-type'>" + AppLinks.I18n.getEntityTypeName(entityType.typeId) + "</span> <input type='hidden' class='selected-entity-type' value='" + entityType.typeId +"'/>");
                    $('#add-non-ual-entity-link-entity').focus();
                } else {
                    var selectDropDown = $('<select tabindex="3" class="selected-entity-type"></select>');
                    typeSelect.html(selectDropDown);
                    for (var i = 0; i < data.entityTypes.length; i++) {
                        var entityType = data.entityTypes[i];
                        selectDropDown.append("<option value='" + entityType.typeId + "'>" + AppLinks.I18n.getEntityTypeName(entityType.typeId) + "</option>");
                    }
                }
                addEntityLinkWizard.enableSubmitBtn();
                $("div.non-ual-links").show();
            };
            AppLinks.SPI.getEntityTypesForApplicationType(type, populateTypeSelect, AppLinks.UI.displayValidationError('validation-error', dialogRootEl));
        };

        var localEntity = AJS.$("#add-entity-link-local");
        var localType = localEntity.attr("data-type");
        var localKey = localEntity.attr("data-key");

        var loadConfiguredEntities = function(applicationLinkId, successCallback) {
            configuredEntities = new Array();
            AppLinks.SPI.getConfiguredEntityLinks(localType, localKey, function(data) {
                $.each(data, function(index, value) {
                    var entitiesOfRemoteType = value;
                    $.each(entitiesOfRemoteType, function(i, v) {
                        if (v.applicationId == applicationLinkId) {
                            configuredEntities.push(v);
                        }
                    })
                });
                successCallback();
            }, AppLinks.UI.displayValidationError('validation-error', dialogRootEl));
        }

        var createLink = function(entity, applicationLinkId) {
            if (!entity) {
                $("#select-entity-ual div.error").text(AppLinks.I18n.getText("applinks.entity.links.error.not.found"));
                entityKeyAutoComplete.hide();
                return;
            }
            var reciprocateRButton = $("#create-reciprocal-entity-link");
            var reciprocate = !reciprocateRButton.attr("disabled") && reciprocateRButton.is(":checked");
            addEntityLinkWizard.disableSubmitBtn();
            AppLinks.SPI.createEntityLink(
            localType, localKey, AJS.$.extend({
                applicationId: applicationLinkId
            }, entity), reciprocate, function() {
                addEntityLinkWizard.enableSubmitBtn();
                addEntityLinkWizard.cancel();
                AppLinks.UI.listEntityLinks();
                var label = localEntity.attr('data-type-label');
                var name  = localEntity.attr('data-name');
                var remoteLabel = AppLinks.I18n.getEntityTypeName(entity.typeId);
                AppLinks.UI.showInfoBox(AJS.$('<span>').text(AppLinks.I18n.getText("applinks.entity.create", [label, name, remoteLabel, entity.name])));
            }, AppLinks.UI.displayValidationError('validation-ual-error', dialogRootEl, function() {
                addEntityLinkWizard.enableSubmitBtn();
            })
            );
        };

        entityInput.keyup(function(event, text) {
            if (event.keyCode == 13) {
                if (dropDownSelection == true) {
                	// Prevent an Enter keypress on a dropdown from submitting the form.
                    dropDownSelection = false;
                } else {
                    if (selectedRemoteEntity) {
                        createLink(selectedRemoteEntity, selectedApplicationLinkId);
                    } else if (entityKeyAutoComplete.dropDownLength()) {
                        // Automatically select the first item in the dropdown.
                        var item = entityKeyAutoComplete.dropDownItem(0);
                        $('.remote-entity', item).first().click();
                        dropDownSelection = false;
                    }
                }
                return AJS.stopEvent(event);
            }
            else {
                if (canCreateReciprocalLink) {
                    $('.reciprocate-entity-link').hide();

                    $('#create-reciprocal-entity-link').attr('checked', false);
                    $('#no-reciprocal-entity-link').attr('checked', false);
                }
                $("#select-entity-ual div.error").text('');
                entityKeyAutoComplete.change(entityInput.val());
            }
        });

        var initUALUI = function(anonymous) {
            $('#available-entities-msg').hide();
            $('.reciprocate-entity-link').hide();
            entityKeyAutoComplete.clearCache();

            $('.entity-loading-message').append('<div class="entity-loading loading">' + AppLinks.I18n.getText('applinks.entity.links.loading') + '</div>');
            var successFn = function () {
               loadEntities(selectedApplicationLinkId, anonymous);
            }
            addEntityLinkWizard.disableSubmitBtn(false);
            loadConfiguredEntities(selectedApplicationLinkId, successFn);
        }

        var gotoSelectEntity = function(canCreateLink, anonymous) {
            canCreateReciprocalLink = canCreateLink;
            addEntityLinkWizard.dialog.gotoPage(1);
            addEntityLinkWizard.dialog.gotoPanel(0);
            initUALUI(anonymous);
        }

        var authenticationRequired = function(authUrl) {
            if (authUrl) {
                var callbacks = {
                    before: function() {
                        addEntityLinkWizard.dialog.hide();
                    },
                    onSuccess: function() {
                        var settings = {
                            noPermission: function() {
                                gotoSelectEntity(false, false);
                                addEntityLinkWizard.dialog.show();
                            },
                            missing: function() {
                                gotoSelectEntity(false, false);
                                addEntityLinkWizard.dialog.show();
                            },
                            noAuthentication: function() {
                                gotoSelectEntity(false, true);
                                addEntityLinkWizard.dialog.show();
                            },
                            authenticationFailed: function() {
                                gotoSelectEntity(false, true);
                                addEntityLinkWizard.dialog.show();
                            },
                            credentialsRequired: function() {
                                gotoSelectEntity(false, true);
                                addEntityLinkWizard.dialog.show();
                            },
                            noConnection: function() {
                                addEntityLinkWizard.dialog.close();
                                showErrorMessage(AppLinks.I18n.getText("applinks.dialog.connection.failed", AppLinks.UI.sanitiseHTML(selectedApplicationName)));
                            },
                            allowed: function() {
                                gotoSelectEntity(true, false);
                                addEntityLinkWizard.dialog.show();
                            },
                            unrecognisedCode: function(code) {
                                addEntityLinkWizard.dialog.close();
                                showErrorMessage(AppLinks.I18n.getText("applinks.dialog.invalid.permission.code", code));
                            }
                        };
                        AppLinks.SPI.canCreateReciprocateEntityLink(selectedApplicationLinkId, AppLinks.SPI.processPermissionCode(settings));
                    },
                    onFailure: function() {
                        $('.applinks-error').remove();
                        $('<div class="error applinks-error">' + AppLinks.I18n.getText('applinks.dialog.entity.link.authorization.failed') + '</div>').insertAfter('#authorization-form-msg');
                        addEntityLinkWizard.dialog.show();
                    }
                }
                addEntityLinkWizard.dialog.gotoPage(0);
                addEntityLinkWizard.dialog.gotoPanel(0);
                addEntityLinkWizard.dialog.show();
                $('#skip-link').unbind('click');
                $('#skip-link').click(function() {
                    canCreateReciprocalLink = false;
                    entityKeyAutoComplete.clearCache();
                    addEntityLinkWizard.dialog.gotoPage(1);
                    initUALUI(true);
                });
                AppLinks.SPI.addAuthenticationTrigger("#authorize-link", authUrl, callbacks);
            } else {
                canCreateReciprocalLink = false;
                entityKeyAutoComplete.clearCache();
                addEntityLinkWizard.dialog.gotoPage(1);
                initUALUI(true);
                addEntityLinkWizard.dialog.show();
            }
        };

        var settings = {
            width: 500,
            height: 450,
            id: "add-entity-link-wizard",
            cancelLabel: AppLinks.I18n.getText("applinks.cancel"),
            onsubmit: function() {
                $('.applinks-error').remove();
                if (applicationSupportsUAL) {
                    if (remoteEntities.length == 1) {
                        selectedRemoteEntity = remoteEntities[0];
                    }
                    createLink(selectedRemoteEntity, selectedApplicationLinkId);
                } else {
                    var remoteKey = $.trim($('#add-non-ual-entity-link-entity').val());
                    if (remoteKey.length == 0) {
                        $('#select-entity-non-ual div.error').text(AppLinks.I18n.getText('applinks.entity.links.entity.key.is.empty'));
                        return; //just whitespace
                    }
                    var remoteName = $.trim($('#add-non-ual-entity-link-alias').val());
                    var exists = false;
                    $.each(configuredEntities, function(index, value) {
                        if (value.key == remoteKey) {
                            $('#select-entity-non-ual div.error').text(AppLinks.I18n.getText('applinks.entity.links.entity.key.exists'));
                            exists = true;
                        }
                    });
                    if (exists == true) {
                        return;
                    }
                    addEntityLinkWizard.disableSubmitBtn();
                    AppLinks.SPI.createNonUalEntityLink(localType, localKey, selectedApplicationLinkId, typeSelect.find('.selected-entity-type').val(), remoteKey, remoteName, function() {
                        addEntityLinkWizard.enableSubmitBtn();
                        AppLinks.UI.listEntityLinks();
                        var label = localEntity.attr('data-type-label');
                        var name  = localEntity.attr('data-name');
                        var remoteLabel = AppLinks.I18n.getEntityTypeName(typeSelect.find('.selected-entity-type').val());
                        AppLinks.UI.showInfoBox($('<span>').text(AppLinks.I18n.getText("applinks.entity.create", [label, name, remoteLabel, remoteName])));
                        addEntityLinkWizard.cancel();
                    }, AppLinks.UI.displayValidationError('validation-non-ual-error', dialogRootEl, function() {
                            addEntityLinkWizard.enableSubmitBtn();
                       }));
                }
            },
            onnext: function() {

                return true;
            },
            onprevious: function() {
                return true;
            },
            onshow: function(popup, configuration) {
                $('.applinks-error').remove();
                if (applicationSupportsUAL) {
                    entityInput.val("");
                    selectedRemoteEntity = null;
                    $("#select-entity-ual div.error").text('');
                } else {
                    $('#add-non-ual-entity-link-entity').val('');
                    $('#add-non-ual-entity-link-alias').val('');
                    $('#select-entity-non-ual div.error').text('');
                }

                var settings = {
                    noPermission: function() {
                        gotoSelectEntity(false, false);
                        addEntityLinkWizard.dialog.show();
                    },
                    missing: function() {
                        gotoSelectEntity(false, false);
                        addEntityLinkWizard.dialog.show();
                    },
                    noAuthentication: authenticationRequired,
                    authenticationFailed: authenticationRequired,
                    credentialsRequired: authenticationRequired,
                    noConnection: function() {
                        showErrorMessage(AppLinks.I18n.getText("applinks.dialog.connection.failed", AppLinks.UI.sanitiseHTML(selectedApplicationName)));
                    },
                    allowed: function() {
                        gotoSelectEntity(true, false);
                        addEntityLinkWizard.dialog.show();
                    },
                    unrecognisedCode: function(code) {
                        showErrorMessage(AppLinks.I18n.getText("applinks.dialog.invalid.permission.code", code));
                    }
                };
                if (applicationSupportsUAL) {

                    $('#create-reciprocal-entity-link').attr('checked', false);
                    $('#no-reciprocal-entity-link').attr('checked', false);

                    AppLinks.SPI.canCreateReciprocateEntityLink(selectedApplicationLinkId, AppLinks.SPI.processPermissionCode(settings));
                } else {
                    typeSelect.find('select').remove();
                    AppLinks.UI.showLoadingIcon(typeSelect);
                    var successFn = function () {
                        loadEntityTypes(selectedApplicationType);
                    }
                    addEntityLinkWizard.disableSubmitBtn();
                    loadConfiguredEntities(selectedApplicationLinkId, successFn);
                    addEntityLinkWizard.dialog.gotoPage(2);
                    addEntityLinkWizard.dialog.gotoPanel(0);
                    addEntityLinkWizard.dialog.show();
                }
                return false;
            }
        }
        var addEntityLinkWizard = $("#add-entity-link-dialog").wizard(settings);
        var dialogRootEl     = $(addEntityLinkWizard.dialog.popup.element);
        $("#add-entity-link-wizard h2.dialog-title").append(AppLinks.Docs.createDocLink("applinks.docs.adding.entity.link", null, 'dialog-help-link'));

        entityInput.click( function() {
            if (entityInput.val() == '') {
                entityKeyAutoComplete.change('', true);
            }
        });

        $("#dropDown-standard").dropDown("Standard");

        entityKeyAutoComplete = AppLinks.inputDrivenDropdown({
            ajsDropDownOptions: {
                selectionHandler: function(e, li) {
                    var entity = li.data("properties").entity;
                    if (entity) {
                        entityInput.val($.trim(entity.name));
                        selectedRemoteEntity = entity;
                        dropDownSelection = true;
                        entityKeyAutoComplete.hide();
                        if (canCreateReciprocalLink) {
                            $('.reciprocate-entity-link').show();

                            $('#create-reciprocal-entity-link').attr('checked', true);
                            $('#no-reciprocal-entity-link').attr('checked', false);

                            $('.create-reciprocal-entity-link-label').text(AppLinks.I18n.getText('applinks.entity.links.create.reciprocal.link.yes', [AppLinks.I18n.getEntityTypeName(entity.typeId), entity.name, localEntity.attr('data-type-label'), localEntity.attr('data-name')]));
                            $('.no-reciprocal-entity-link-label').text(AppLinks.I18n.getText('applinks.entity.links.create.reciprocal.link.no', [AppLinks.I18n.getEntityTypeName(entity.typeId), entity.name, localEntity.attr('data-type-label'), localEntity.attr('data-name')]));
                        }
                        addEntityLinkWizard.enableSubmitBtn();
                    }
                    return false;
                },
                alignment: "left"
            },
            getDataAndRunCallback: function(casedSearch, sink) {
                var search = casedSearch.toLowerCase();
                var matching = [];

                var MAX_RESULTS = 10;
                var possibleResults = 0;
                var MAX_NAME_LENGTH = 10;

                for (var i = 0; i < remoteEntities.length; i++) {
                    var entity = remoteEntities[i];
                    var configured = false;
                    $(configuredEntities).each( function(index, value) {
                        if (configuredEntities[index].displayUrl == entity.displayUrl){
                            configured = true;
                        }
                    });

                    if (!configured) {
                        if (((entity.key.toLowerCase().indexOf(search) >= 0 || entity.name.toLowerCase().indexOf(search) >= 0) && casedSearch != '') ||
                             (casedSearch == '')) {
                            possibleResults++;
                            if (matching.length < MAX_RESULTS) {
                                var name = entity.name;
                                if (entity.name.length  > MAX_NAME_LENGTH) {
                                   name = entity.name.substring(0, MAX_NAME_LENGTH) + "...";
                                }
                                matching.push({
                                    href: "#",
                                    name: name + " (" + entity.key + ")",
                                    icon: entity.iconUrl,
                                    entity: entity,
                                    className: "remote-entity"
                                });
                            }
                        }
                    }
                }

                if (matching.length) {
                    if (possibleResults > MAX_RESULTS) {
                        matching.push (
                        {
                            className: "max-entity-links",
                            name: '(' + AppLinks.I18n.getTextWithPrefix("entity.links.max", MAX_RESULTS) + ')',
                            href: "#"
                        });
                    }
                    sink.call(this, [matching], search);
                }
                else { //display no entity found
                    sink.call(this, [[{
                        html: '' + AppLinks.I18n.getTextWithPrefix("entity.links.no.entity.found") + ''}]], search, true);
                }
            },
            dropdownPlacement: function(dd) {
                var container = entityInput.parent().find(".aui-dd-parent");
                container.append(dd);
            },
            dropdownPostprocess : function(dd) {
                if ($('.max-entity-links').size() > 0) {
                    var  liEl= $('.max-entity-links').parent();
                    liEl.attr('id', 'max-entity-links');
                    liEl.find('a').unbind('click');
                    liEl.unbind('click');
                }
            }
        });
        /**
         * Attach on click event to all application links in select drop down.
         */
        $('.item-link').each(function(index) {
            $(this).click(function(e) {
                e.preventDefault();
                selectedApplicationLinkId = $(this).attr('data-key');
                selectedApplicationName   = $(this).attr('data-name');
                selectedApplicationType = $(this).attr('data-type')
                if ($(this).attr('data-ual')) {
                    applicationSupportsUAL = true;
                } else {
                    applicationSupportsUAL = false;
                }
                AppLinks.UI.hideInfoBox();
                addEntityLinkWizard.show();
            });
        });

        $('#add-non-ual-entity-link-entity').keydown(function(event) {
            //KeyCode 13 is the enter key.
            if (event.keyCode == '13') {
                addEntityLinkWizard.submit();
            }
        });

    })(AJS.$)
});
