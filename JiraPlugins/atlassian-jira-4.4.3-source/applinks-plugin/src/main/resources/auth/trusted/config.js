AJS.$(document).ready(function() {

    AJS.$("#auth-trusted-action-configure, #auth-trusted-action-change").click(function() {
        /**
         * Opens the edit view.
         */
        AJS.$(".auth-trusted-view").hide();
        AJS.$(".auth-trusted-view.edit").removeClass("hidden").show();
    });

    AJS.$("#auth-trusted-action-cancel").click(function() {
        AJS.$(".auth-trusted-view").hide();
        AJS.$(".auth-trusted-view." + AJS.$("meta[name=cancel]").attr("content")).removeClass("hidden").show();
    });

    // make the initial view visible:
    AJS.$(".auth-trusted-view." + AJS.$("meta[name=view]").attr("content")).removeClass("hidden").show();
});
