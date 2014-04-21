function deleteConsumerInformation(title, message, confirm, cancel) {
    if (!cancel) {
        cancel = function() {};
    }
    var popup = new AJS.Dialog({
        width: 450,
        height: 180,
        id: "confirm-dialog",
        onCancel: cancel
    });

    popup.addHeader(title);
    popup.addPanel(title);
    popup.getCurrentPanel().html(message);

    popup.addButton(AppLinks.I18n.getText("applinks.delete"), function() {
        AJS.$("#oauth-incoming-enabled").val("false");
        popup.remove();
        AJS.$("#add-consumer-manually").submit();
    }, "confirm");
    popup.addButton(AppLinks.I18n.getText("applinks.cancel"), function() {
        popup.remove();
    }, "cancel");

    popup.show();
    return false;
}

function deleteServiceProviderInformation(title, message, confirm, cancel) {
    if (!cancel) {
        cancel = function() {};
    }
    var popup = new AJS.Dialog({
        width: 450,
        height: 180,
        id: "confirm-dialog",
        onCancel: cancel
    });

    popup.addHeader(title);
    popup.addPanel(title);
    popup.getCurrentPanel().html(message);

    popup.addButton("delete", function() {
        AJS.$("#oauth-outgoing-enabled").val("false");
        popup.remove();
        AJS.$("#add-serviceprovider").submit();
    }, "confirm");
    popup.addButton("cancel", function() {
        popup.remove();
    }, "cancel");

    popup.show();
    return false;
}


function enableDisableOAuthInRemoteApp(url)
{
    var enabled = AJS.$('#oauth-incoming-enabled').val();
    var remoteURL = url.replace(/ENABLE_DISABLE_OAUTH_PARAM/g, enabled);
    window.location = remoteURL;
    return false;
}