AJS.$(document).bind(AppLinks.Event.READY, function() {
    (function($) {
    var dialogEl      = $("#edit-entity-dialog"),
        localTypeEl   = $("#add-entity-link-local"),
        submitEl,
        nameEl,
        updateSuccess = function() {
            AppLinks.UI.hideLoadingIcon(submitEl);
            submitEl.attr('disabled', '');
            AppLinks.UI.showInfoBox('Updated link to ' + AppLinks.I18n.getEntityTypeName(selectedEntity.typeId) + " '" + AppLinks.UI.sanitiseHTML(selectedEntity.name) + "' (" + AppLinks.UI.sanitiseHTML(selectedEntity.key) +")");
            AppLinks.UI.listEntityLinks();
            wizard.cancel();
        },
        selectedEntity,
        settings = {
            showButtonId: "edit-entity-link",
            cancelLabel: AppLinks.I18n.getTextWithPrefix("close"),
            width: 400,
            height: 200,
            id: "edit-entity-link-dialog",
            submitLabel: AppLinks.I18n.getText("applinks.update"),
            onshow: function() {
                AppLinks.UI.hideInfoBox();
                nameEl.val(selectedEntity.name);
                $('.edit-entity-link-description').text(AppLinks.I18n.getText('applinks.entity.links.entity.details.description', [AppLinks.I18n.getEntityTypeName(selectedEntity.typeId), selectedEntity.key]));
                submitEl.attr('disabled', '');
                return true;
            },
            aftershow: function() {
                nameEl.focus();
            },
            onsubmit: function() {
                $('.applinks-error').remove();
                var name = nameEl.val();
                if ($.trim(name) == '') {
                    $("<div class='error applinks-error'>" + AppLinks.I18n.getText('applinks.entity.links.entity.name.is.empty') + "</div>").insertAfter(nameEl);
                    return false;
                }
                return true;
            },
            aftersubmit: function() {
                var localType = localTypeEl.attr('data-type'),
                    localKey  = localTypeEl.attr('data-key');
                selectedEntity.name = nameEl.val();
                AppLinks.UI.showLoadingIcon(submitEl);
                submitEl.attr('disabled', 'true');
                AppLinks.SPI.createEntityLink(localType, localKey, selectedEntity, false, updateSuccess, AppLinks.UI.displayValidationError('update-validation-errors', dialogRootEl, function() {
                    submitEl.attr('disabled', '');
                    AppLinks.UI.hideLoadingIcon(submitEl);
                }));
            },
            oncancel: function() {
                return true;
            }
        }
        var wizard       = dialogEl.wizard(settings),
            dialogRootEl = $(wizard.dialog.popup.element),
            nameEl       = dialogRootEl.find('#entity-name'),
            submitEl     = dialogRootEl.find('.wizard-submit');

        nameEl.keydown(function(event) {
            //KeyCode 13 is the enter key.
            if (event.keyCode == '13') {
                wizard.submit();
            }
        });

        AppLinks.editEntityLinkDialog = function(entity) {
            selectedEntity = entity;
            wizard.show();
        }
    })(AJS.$);
});
