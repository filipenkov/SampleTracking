/**
 * A control for inline editing of fields
 *
 * See full doco here
 */
JIRA.InlineEdit = AJS.Control.extend({

    init: function (options) {

        this.save = options.save;
        this.viewComponent = options.view;
        this.editComponent = options.edit;

        this._assignEvents("editComponent", this.editComponent);
        this._assignEvents("viewComponent", this.viewComponent);
    },

    view: function () {
        this.editComponent.getContainer().hide();
        this.viewComponent.getContainer().show();
    },

    updated: function () {
        var instance = this;

        if (!this.locked) {

            this.locked = true;

            this.editComponent.showLoading();

            this.save(this.editComponent, function (value, label) {
                instance.editComponent.hideLoading();
                instance.view();
                instance.viewComponent.setValue(value);
                instance.viewComponent.setLabel(label);
                instance.viewComponent.reportUpdated();
                instance.locked = false;
            }, function (error) {
                instance.editComponent.showError(error);
                instance.editComponent.hideLoading();
                instance.locked = false;
            });

        }
    },

    edit: function () {

        // disable editing when there are errors
        if (AJS.InlineDialog.current) {
            return;
        }

        var editContainer = this.editComponent.getContainer();
            editContainer.show();
            editContainer.insertBefore(this.viewComponent.getContainer());


        this.viewComponent.getContainer().hide();
        this.editComponent.activate(this.viewComponent.getValue(), this.viewComponent.getContainer());

    },

    _events: {
        editComponent: {

            deactivate: function () {
                this.view();
            },

            updated: function () {
                this.updated();
            }
        },
        viewComponent: {
            edit: function () {
                this.edit();
            }
        }
    }
});


JIRA.InlineEdit.getInlineErrorDialog =  function (ctx, error) {

    var id = "aui-inline-edit-error-" + new Date().getTime(),
        inlineDialog = AJS.InlineDialog(ctx, id, function ($contents, control, show) {
            var $error = jQuery("<div class='aui-inline-edit-error' />").html(error),
                $revert = jQuery("<span class='aui-inline-edit-revert'> - <a href='#'>Cancel</a></span>");

            $revert.mousedown(function (e) {
                e.preventDefault();
            });

            $revert.click(function (e) {
                inlineDialog.trigger("revert");
                e.preventDefault();
            });

            $error.append($revert);

            $contents.empty();
            $contents.append($error);

            show();
    });

    AJS.$(document).bind("showLayer", function (e, type, hash) {
        if (hash && hash.id === id) {
            jQuery("body").unbind("click." + id + ".inline-dialog-check");
        }
    });

    return inlineDialog;
};