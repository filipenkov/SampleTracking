AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {
        var localEntity = AJS.$("#add-entity-link-local");
        var localType = localEntity.attr("data-type");
        var localKey = localEntity.attr("data-key");
        AJS.$.extend(AppLinks.UI || {}, {
            listEntityLinks: function() {
                $('#entity-link-list-table').hide();
                $('.no-links').hide();
                $('.links-loading').show();
                AppLinks.SPI.getConfiguredEntityLinks(localType, localKey, function(data) {
                    $('#entity-link-list-table').show();

                    var tableBody = $("#entity-link-list");
                    tableBody.empty();

                    var createRow = function(entity) {
                        var extensions = {
                            typeLabel: AppLinks.I18n.getEntityTypeName(entity.typeId),
                            application: AJS.$(".item-link[data-key='"+ entity.applicationId +"']").text()
                        };
                        var row = AJS.$(AJS.template.load('el-row').fill(AJS.$.extend(extensions, entity)).toString());
                        row.data("entity", entity);

                        if (!entity.isPrimary) {
                            row.find(".entity-toggleprimary-action").show();
                            row.find(".entity-toggleprimary-link").click(function(e) {
                                e.preventDefault();
                                AppLinks.UI.hideInfoBox();
                                var entity = $(this).parents(".ual-row").data("entity");
                                AppLinks.SPI.makePrimaryEntityLink(localType, localKey, entity, function() {
                                    var entityLabel = AppLinks.I18n.getEntityTypeName(entity.typeId);
                                    AppLinks.UI.showInfoBox(AppLinks.I18n.getText('applinks.entity.new.primary', [entityLabel, AppLinks.UI.sanitiseHTML(entity.name), AppLinks.UI.sanitiseHTML(entity.key)] ));
                                    AppLinks.UI.listEntityLinks();
                                }, displayError);
                            });
                        }
                        else {
                            row.find(".entity-toggleprimary-action").hide();
                        }

                        row.find(".entity-delete-link").click(function(e) {
                            e.preventDefault();
                            AppLinks.UI.hideInfoBox();
                            var entity = $(this).parents(".ual-row").data("entity");
                            var deleteDialogSettings = {
                                title: AppLinks.I18n.getText("applinks.dialog.delete.title", [localKey, entity.key]),
                                applicationId: entity.applicationId,
                                confirmMessage: AppLinks.I18n.getText('applinks.dialog.delete.entity.link.text', [localKey, AppLinks.UI.sanitiseHTML(entity.key)]),
                                reciprocalLinkMessage: AppLinks.I18n.getText('applinks.dialog.delete.reciprocal.entity.link', [entity.key, localKey]),
                                noReciprocalLinkMessage: AppLinks.I18n.getText('applinks.dialog.delete.no.reciprocal.entity.link', [entity.key, localKey]),
                                deleteYesMessage : AppLinks.I18n.getText('applinks.dialog.delete.reciprocal.entity.link.yes', [entity.key, localKey]),
                                deleteNoMessage : AppLinks.I18n.getText('applinks.dialog.delete.reciprocal.entity.link.no', [entity.key, localKey]),
                                authenticationMessage: AppLinks.I18n.getText('applinks.dialog.authentication.required.entity.link'),
                                noConnectionMessage: AppLinks.I18n.getText('applinks.dialog.delete.entity.link.no.connection'),
                                authenticationFailedMessage: AppLinks.I18n.getText('applinks.dialog.delete.entity.link.authorization.failed', [entity.key, localKey]),
                                failedToDetectReciprocalLinkMessage: AppLinks.I18n.getText('applinks.dialog.application.failed.to.detect.reciprocal.entity.link', [entity.key, localKey]),
                                helpKey: 'applinks.docs.delete.entity.link',
                                doPermissionCheck: function(success, error) {
                                    AppLinks.SPI.canDeleteEntityLink(localType, localKey, entity, success, error);
                                },
                                doDelete: function(reciprocate, success, error) {
                                    AppLinks.SPI.deleteEntityLink(localType, localKey, entity, reciprocate, success, error);
                                },
                                callback: function() {
                                    AppLinks.UI.showInfoBox(AppLinks.I18n.getText("applinks.entity.delete", [AppLinks.I18n.getEntityTypeName(entity.typeId), AppLinks.UI.sanitiseHTML(entity.name)]));
                                    AppLinks.UI.listEntityLinks();
                                    return true;
                                }
                            };
                            AppLinks.showDeleteLinkDialog(deleteDialogSettings);
                        });

                        row.find(".entity-edit-name").click(function(e) {
                            e.preventDefault();
                            var entity = $(this).parents(".ual-row").data("entity");
                            AppLinks.editEntityLinkDialog(entity);
                        });
                        return row;
                    };

                    $('.links-loading').hide();
                    if ($.isEmptyObject(data)) {
                        $('#entity-link-list-table').hide();
                        $('.no-links').show();
                        $('.no-links').text(AppLinks.I18n.getText("applinks.no.entity.links.configured"))
                    } else {
                        $('.no-links').hide();
                        $('#entity-link-list-table').show();
                    }

                    $.each(data, function(index, value) {
                        var entitiesOfRemoteType = value;
                        var tempType = null;
                        var hasMultipleTypes = false;
                        $.each(entitiesOfRemoteType, function(i, v) {
                            v.isPrimary = (i == 0);
                            if (tempType == null) {
                                tempType = v.typeId;
                            } else if ( tempType == v.typeId) {
                                hasMultipleTypes = true;
                            }
                            tableBody.append(createRow(v));
                        })
                        if (hasMultipleTypes == true) {
                            $('.primary-column').show();
                        } else {
                            $('.primary-column').hide();
                        }
                    });
                }, function(data) {
                    $('.links-loading').remove();
                    var message = AppLinks.parseError(data);
                    AppLinks.UI.showErrorBox(message);
                });
            }
        });

        var displayError = function(data) {
            var message = AppLinks.parseError(data);
            AppLinks.UI.showErrorBox(message);
        };
        AppLinks.UI.listEntityLinks();
    })(AJS.$)
});
