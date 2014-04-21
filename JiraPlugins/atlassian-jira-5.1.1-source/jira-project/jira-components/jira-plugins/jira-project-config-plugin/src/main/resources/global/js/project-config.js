// use this constant for rest version to ease update
JIRA.REST_VERSION = "2";
JIRA.REST_BASE_URL = contextPath + "/rest/api/" + JIRA.REST_VERSION;


JIRA.ProjectConfig = function () {
    return {
        getKey: function () {
            return jQuery("meta[name=projectKey]").attr("content");
        },
        getId: function () {
            return jQuery("meta[name=projectId]").attr("content");
        }
    };
}();


AJS.convertXHRToSmartAjaxResult = function (xhr) {
    var textStatus = xhr.status >= 400 ? "error" : "success";
    return JIRA.SmartAjax.SmartAjaxResult(xhr, new Date().getTime(), textStatus, JSON.parse(xhr.responseText))
};

/**
* Creates and shows a JIRA.FormDialog. The default submit handler is overridden to serialize the form values and
* update the associated model with them. Updating of model triggers a change event which re-renders the table row.
*
* @param {Backbone.View} View - dialog contents
* @return {JIRA.FormDialog.}
*/
AJS.openDialogForRow = function (view, row, id) {

    var dialogForm = new view({
            model: row.model
        }),
        dialog = new JIRA.FormDialog({
            id: id,
            content: function (callback) {
                dialogForm.render(function (el) {
                    callback(el);
                });
            },
            submitHandler: function (e) {
                dialogForm.submit(this.$form.serializeObject(), row, function () {
                    dialog.hide();
                });
                e.preventDefault();
            }
        });
    dialog.show();

    return dialog;
};

AJS.$(function () {

    var $operations = AJS.$(".operation-menu");

    new JIRA.ToggleBlock({
        blockSelector: "#project-config-panel-summary .toggle-wrap",
        triggerSelector: ".mod-header h3",
        cookieCollectionName: "admin"
    });

    AJS.$("a.project-config-inlinedialog-trigger").each(function() {
        AJS.InlineDialog(
            AJS.$(this),
            "project-config-inlinedialog-" + AJS.escapeHtml(parseUri(this.href).queryKey.fieldId),
            this.href,
            {
                width: 200
            }
        );
    });

    // we are using AUI tabs classes for CSS but get the Javascript behaviour also. Removing JavaScript AJS-638
    AJS.$(".tabs-menu a").unbind("click");

    var dropdown = new AJS.Dropdown({
        trigger: $operations.find(".project-config-operations-trigger"),
        content: $operations.find(".aui-list"),
        alignment: AJS.RIGHT
    });

    var operationsMenuState = "closed";

    AJS.$(dropdown).bind({
        "showLayer": function() {
            operationsMenuState = "open";
        },
        "hideLayer": function() {
            operationsMenuState = "closed";
        }
    });

    AJS.$("#edit_project").bind({
        "mouseenter focus": function() {
            if (operationsMenuState === "open") {
                dropdown.hide();
                operationsMenuState = "willopen";
            }
        },
        "mouseleave blur": function() {
            setTimeout(function() {
                if (operationsMenuState === "willopen") {
                    operationsMenuState = "closed";
                }
            }, 50);
        }
    });

    dropdown.trigger().bind("mouseenter focus", function() {
        if (operationsMenuState === "willopen") {
            dropdown.show();
        }
    });

    new JIRA.ToggleBlock({
        blockSelector: ".project-config-scheme-item",
        triggerSelector: ".project-config-toggle,.project-config-icon-twixi",
        persist: false
    });

});
