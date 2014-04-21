/**
 * @constructor
 */
JIRA.EditProfileDialog = JIRA.UserProfileDialog.extend({
    _handleSubmitResponse: function (data, xhr, smartAjaxResult) {
        if (this.serverIsDone) {
            this._updateName();
            this._updateMail();
            this._super(data, xhr, smartAjaxResult);
        }
    },
    _updateName: function () {
        var oldName = AJS.$("#up-d-fullname").text();
        var name = AJS.$("#edit-profile-fullname").val();
        AJS.$("#up-d-fullname").text(name);
        AJS.$("#up-user-title-name").text(name);
        AJS.$("a[href*='ViewProfile.jspa']:contains('" + oldName + "')").text(name);
        if (window.frames['gadget-1'] && window.frames['gadget-1'].AJS){
            window.frames['gadget-1'].AJS.$("a[href*='ViewProfile.jspa']:contains('" + oldName + "')").text(name);
        }
    },
    _updateMail: function () {
        var email = AJS.$("#edit-profile-email").val();
        if (AJS.$("#up-d-email a").length === 0){
            var emailDiv = AJS.$("#up-d-email");
            if (/\sat\s.*\sdot\s/.test(emailDiv.text())){
                AJS.$("#up-d-email").text(email.replace(/@/g, " at ").replace(/\./g, " dot "));
            } else {
                AJS.$("#up-d-email").text(email);
            }
        } else {
            AJS.$("#up-d-email a").attr("href", "mailto:" + email).text(email);
        }
    }
});
