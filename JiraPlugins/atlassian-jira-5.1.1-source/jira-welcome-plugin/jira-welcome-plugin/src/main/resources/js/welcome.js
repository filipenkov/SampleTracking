(function($) {

    var restUrl = contextPath + "/rest/welcome/1.0/show",
        cancelTriggerClass = "welcome-screen-cancel-trigger";

    function welcomeDialogContents() {
        // NOTE: JIRA has no real concept of a first name. The full name will have to suffice.
        return JIRA.Templates.WelcomeScreen.welcomeScreenPanel({
            theirName: AJS.Meta.get('remote-user-fullname') || "",
            cancelLinkHtml: '<a class="' + cancelTriggerClass + '" href="' + contextPath + '/">'
        });
    }

    AJS.toInit(function ($) {
        var dialog;
        var createDialog = function() {
            var dialog = new AJS.Dialog({
                width: 860,
                height: 600,
                id: "welcome-dialog",
                onCancel: function () {}
            });
            dialog.addPanel("default", welcomeDialogContents());
            // stop the escape key from dismissing the dialog.
            JIRA.bind("Dialog.beforeHide", preventDialogClose);
            // ...only hyperlinks provided at this point may close the dialog.
            $("a", dialog.popup.element).click(function(e) {
                JIRA.unbind("Dialog.beforeHide", preventDialogClose);
            });
            // Allow for preventing the dialog from appearing again.
            $("."+cancelTriggerClass, dialog.popup.element).click(dismissForever);

            // Analytics to determine what action the user chooses.
            // Also dismiss the dialog so that we only "welcome" the user once
            $(".welcome-screen-action", dialog.popup.element).click(function() {
                AJS.EventQueue && AJS.EventQueue.push({name: "welcomescreen.action", properties: {
                    type: this.getAttribute('data-type')
                }});
                $.ajax({
                    url: restUrl,
                    type: "DELETE",
                    success: function () {
                        AJS.log("welcome dialog > action clicked > don't show dialog anymore");
                    }
                });
            });

            return dialog;
        },
        /**
         * Prevent the dialog from being closed by the escape button or other dialog/popup cancels.
         */
        preventDialogClose = function(e) {
            if (JIRA.Dialog && JIRA.Dialog.current
                && dialog && JIRA.Dialog.current.$popup == dialog.popup.element) {
                e.preventDefault();
            }
        },
        /**
         * Closes the dialog and sets a user preference to prevent the welcome screen from appearing again.
         */
        dismissForever = function(e) {
            e.preventDefault();
            dialog.hide().remove();
            AJS.EventQueue && AJS.EventQueue.push({name: "welcomescreen.dismiss"});
            $.ajax({
                url: restUrl,
                type: "DELETE",
                success: function () {
                    AJS.log("welcome dialog > don't show dialog anymore");
                }
            });
        };

        /**
         * Creates and shows the welcome dialog after checking if it should be.
         */
        if (AJS.Meta.getBoolean('show-welcome-screen')) {
            if (AJS.$("#gh").length) return;
            dialog = createDialog();
            dialog.show();
        }
    });
})(AJS.$);