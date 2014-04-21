AJS.$(document).bind(AppLinks.Event.PREREADY, function() {
    AppLinks = AJS.$.extend(window.AppLinks || {}, {
        confirmDialog: function(title, message, confirm, cancel) {
            if (!cancel) cancel = function() {};

            var popup = new AJS.Dialog({
                width: 450,
                height: 180,
                id: "confirm-dialog",
                onCancel: cancel
            });

            popup.addHeader(title);
            popup.addPanel(title);
            popup.getCurrentPanel().html(message);

            var complete = function(callback) {
                popup.remove();
                callback.call(popup);
            };

            popup.addButton(AJS.params.statusDialogAddButtonLabel || AppLinks.I18n.getTextWithPrefix("confirm"), function() {
                complete(confirm);
            }, "confirm");
            popup.addButton(AJS.params.statusDialogCancelButtonLabel || AppLinks.I18n.getTextWithPrefix("cancel"), function() {
                complete(cancel);
            }, "cancel");

            popup.show();
        }
    });

});
