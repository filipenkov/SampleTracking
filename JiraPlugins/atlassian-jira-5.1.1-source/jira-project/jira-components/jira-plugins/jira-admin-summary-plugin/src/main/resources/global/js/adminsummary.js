AJS.$(function(){
    new JIRA.ToggleBlock({
        blockSelector: "#admin-summary-panel .toggle-wrap",
        triggerSelector: ".mod-header h3",
        cookieCollectionName: "admin"
    });

    new JIRA.Dialog({
        id: "admin-project-intro-dialog",
        trigger: ".add-project-intro-trigger",
        width:830,

        // call soy template for dialog contents. Contains header cancel button etc.
        content: function (callback)
        {
            callback(JIRA.Templates.adminProjectIntroDialog({
                        iframeUrl:AJS.params.projectIntroUrl
                    }));
        },

        onContentRefresh: function () {
            var instance = this;
            //need to hookup the 'close' link to close the dialog.
            $cancel = AJS.$(".cancel", this.$content);
            $cancel.click(function (e) {
                if (instance.xhr)
                {
                    instance.xhr.abort();
                }
                instance.xhr = null;
                instance.cancelled = true;
                instance.hide();
                e.preventDefault();
            });
        }
    });
});
