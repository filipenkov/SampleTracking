/**
 * @constructor
 */
JIRA.EditPreferencesDialog = JIRA.UserProfileDialog.extend({
    _getDefaultOptions: function () {
        return AJS.$.extend(this._super(), {
            notifier: "#userprofile-notify"
        });
    },
    _handleSubmitResponse: function (data, xhr, smartAjaxResult) {
        if (this.serverIsDone)
        {
            this._updatePageSize();
            this._updateEmail();
            this._updateSharing();
            this._updateOwnNotifications();
            // We update this last, because if there any updates we need to reload the page so that they are picked up
            // immediately. We override the magical "reload" function to true so that the super-class knows it needs to reload
            // the page. Damn it! I hate inheritance hierarchies.
            this._updateLocale();
            this._updateTimezone();
            this._updateKeyboardShortcutsNotifications();
            this._super(data, xhr, smartAjaxResult);
        }
    },
    _updatePageSize: function() {
        var pageSize = AJS.$("#update-user-preferences-pagesize").val();
        AJS.$("#up-p-pagesize").text(pageSize);
    },
    _updateEmail: function() {
        var email = AJS.$("#update-user-preferences-mailtype option:selected").text();
        AJS.$("#up-p-mimetype").text(email);
    },
    _updateSharing: function() {
        var sharing = AJS.$("#update-user-preferences-sharing option:selected").val();
        if (sharing !== "false"){
            AJS.$("#up-p-share-private").show();
            AJS.$("#up-p-share-public").hide();
        } else {
            AJS.$("#up-p-share-private").hide();
            AJS.$("#up-p-share-public").show();
        }
    },
    _updateOwnNotifications: function() {
        var ownNotifications = AJS.$("#update-user-preferences-own-notifications option:selected").val();
        if (ownNotifications !== "false"){
            AJS.$("#up-p-notifications_on").show();
            AJS.$("#up-p-notifications_off").hide();
        } else {
            AJS.$("#up-p-notifications_on").hide();
            AJS.$("#up-p-notifications_off").show();
        }
    },
    _updateLocale: function ()
    {
        var localeNewValue = AJS.$.trim(AJS.$("#update-user-preferences-locale option:selected").text());
        var localeOldValue = AJS.$.trim(AJS.$("#up-p-locale").text());

        if (localeOldValue !== localeNewValue) {
            this._reload = function() {
                return true;
            }
        }
    },
    _updateTimezone : function() {
        var timeZoneNewValue = AJS.$.trim(AJS.$("#defaultUserTimeZone option:selected").text());

        var timeZoneRegion = AJS.$("#defaultUserTimeZone option:selected").val();

        if (timeZoneRegion != 'JIRA') {
           AJS.$("#up-p-jira-default").hide();
        } else {
           AJS.$("#up-p-jira-default").show();
        }
        AJS.$("#up-p-timezone-label").text(timeZoneNewValue);
    },
    _updateKeyboardShortcutsNotifications: function() {
        var kbShortcutsNewValue = AJS.$("#update-user-preferences-keyboard-shortcuts option:selected").val();
        var kbShortcutsOldValue = AJS.$("#up-p-keyboard-shortcuts-enabled").is(":visible") ? "true" : "false";

        if (kbShortcutsOldValue !== kbShortcutsNewValue){
            if (kbShortcutsNewValue !== "false") {
                AJS.$("#up-p-keyboard-shortcuts-enabled").show();
                AJS.$("#up-p-keyboard-shortcuts-disabled").hide();
            }
            else {
                AJS.$("#up-p-keyboard-shortcuts-enabled").hide();
                AJS.$("#up-p-keyboard-shortcuts-disabled").show();
            }
            this._reload = function() {
                return true;
            }
        }
    }
});
