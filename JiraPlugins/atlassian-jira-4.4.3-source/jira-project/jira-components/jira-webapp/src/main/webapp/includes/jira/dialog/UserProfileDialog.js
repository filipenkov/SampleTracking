JIRA.UserProfileDialog = JIRA.FormDialog.extend({
    _getDefaultOptions: function () {
        return AJS.$.extend(this._super(), {
            notifier: "#userdetails-notify"
        });
    },
    _handleSubmitResponse: function (data, xhr, smartAjaxResult) {
        if (this.serverIsDone) {
            if (this.options.autoClose) {
                this.hide();
            }
            this._reloadOrNotify();
        }
    },
    _reload: function () {
        return false;
    }
    ,
    show: function () {
        this._super();
        this._hideNotifier();
    },
    _reloadOrNotify: function () {
        if (this._reload()) {
            window.location.reload();
        } else {
            this._showNotifier();
        }
    },
    _showNotifier: function () {
        AJS.$(this.options.notifier).removeClass("hidden");
    },
    _hideNotifier: function () {
        AJS.$(this.options.notifier).addClass("hidden");
    }
});

/** Preserve legacy namespace
    @deprecated AJS.FormPopup */
AJS.namespace("AJS.UserProfilePopup", null, JIRA.UserProfileDialog);