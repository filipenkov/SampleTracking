/**
 * A controller for all the different 'QuickForm' views. Determines what view to show (Configurable, Unconfigurable)
 * and what triggers them.
 *
 * @class Container
 */
JIRA.QuickForm.Container = AJS.Control.extend({

    /**
     * @constructor
     * @param options - This can either be specifed as an object or a function that returns an object
     * ... {Array<String>} globalEventNamespaces - Events will be triggered on these namespaces
     * e.g EditForm.switchedToConfigurableForm. EditForm being the specified global namespace.
     * ... {JIRA.QuickForm.Model} model
     * ... {JIRA.QuickForm.FieldPicker} fieldPicker
     * ... {JIRA.QuickForm.AbstractForm} configurableForm
     * ... {JIRA.QuickForm.AbstractForm} unconfigurableForm
     */
    init: function (options) {
        this.$element = jQuery("<div />").addClass("qf-container"); // Container element - All html will be appended here
        this.options = JIRA.makePropertyFunc(options); // Treat all options as option function to get uniformity
    },

    /**
     * Wraps the render methods of each of the views to collect the html and append it to our container element ($element)
     * Also does things like binds/triggers events and decorates content.
     */
    decorateRenderers: function () {

        var instance = this;

        if (!this.fieldPicker.decorated) {

            this.fieldPicker.bind("switchedToAll", function () {
                instance.unconfigurableForm.render(instance.currentView.serialize());
            });

            this.fieldPicker.bind("switchedToCustom", function () {
                instance.configurableForm.render(instance.currentView.serialize());
            });

            this.fieldPicker.decorated = true;
        }

        if (!this.configurableForm.decorated) {

            this.configurableForm.bind("rendered", function (e, contents) {
                instance.currentView = instance.configurableForm;
                instance.fieldPicker.switchToCustom(true);
                instance.fieldPicker.setForm(instance.configurableForm);
                instance.fieldPicker.bindInlineDialog();
                instance.applyGlobalDecorator(contents);
                instance.model.setUseConfigurableForm(true);
                instance.triggerEvent("QuickForm.configurableFormRendered");
            });

            this.configurableForm.bind("QuickForm.submitted", function () {
               instance.triggerEvent("QuickForm.configurableFormSubmitted");
            });

            this.configurableForm.decorated = true;
        }

        if (!this.unconfigurableForm.decorated) {
            this.unconfigurableForm.bind("rendered", function (e, contents) {
                instance.fieldPicker.switchToAll(true);
                instance.fieldPicker.setForm(instance.unconfigurableForm);
                instance.fieldPicker.bindInlineDialog();
                instance.currentView = instance.unconfigurableForm;
                instance.applyGlobalDecorator(contents);
                instance.model.setUseConfigurableForm(false);
                instance.triggerEvent("QuickForm.unconfigurableFormRendered");
            });

            this.unconfigurableForm.bind("QuickForm.submitted", function () {
                instance.triggerEvent("QuickForm.unconfigurableFormSubmitted");
            });

            this.unconfigurableForm.decorated = true;
        }
    },

    /**
     * Whenever we render a view we do these things
     *
     * @param {jQuery} contents
     */
    applyGlobalDecorator: function (contents) {

        // If contents is already in the dom when we do the .html call below all events will be unbound. Doing .detach avoids this.
        contents.detach();
        this.$element.html(contents);
        this.triggerEvent("contentRefreshed", [this.$element]);
        if (this.currentView.setInitialFocus) {
            this.currentView.setInitialFocus();
        }
    },

    /**
     * Sets up our form to be displayed in a dialog
     *
     * @param options
     * ... {String} id - dialog id
     * ... {String, jQuery, HTMLelement} - The element that when clicked opens dialog
     */
    asDialog: function (options) {

        var instance = this,
            dialog = new JIRA.FormDialog({
                id: options.id,
                trigger: options.trigger,
                width: 940,
                content: function (ready) {

                    instance.render().done(function () {
                        ready(instance.$element);
                    });

                    instance.triggerEvent("QuickForm.dialogShown");
                },
                submitHandler: function (e, ready) {
                    e.preventDefault();
                    instance.currentView.submit().then(ready, ready);
                }
            });

        dialog.bind("Dialog.hide", function () {
            instance.triggerEvent("QuickEdit.dialogHidden");
        });

         /* Ensure that whenever the user config form updates it's content, for example switching to full form,
           Content is decorated (title put in header etc) and dialog is correctly positioned. */
        this.bind("contentRefreshed", function () {
            dialog.notifyOfNewContent();
        });

        // Whenever ajax is happening show a loading indicator in the dialog
        instance.$element.ajaxStart(function () {
            dialog.showFooterLoadingIndicator();
        }).ajaxStop(function () {
            dialog.hideFooterLoadingIndicator();
        });

        // A way to get to the container instance from the form object
        dialog.getQuickForm = function () {
            return instance;
        };

        instance.dialog = dialog;

        return dialog;
    },

    /**
     * As our options object can be a function we re-evaluate our options whenever this method is called. This is useful
     * when you are using the same container to build different forms, for example on the issue navigator we want to change
     * what issue we are editing based on what issue is selected.
     */
    lazyInit: function () {

        var instance = this,
            options = this.options();

        this.model = options.model;
        this.errorHandler = options.errorHandler;
        this.globalEventNamespaces = options.globalEventNamespaces;
        this.configurableForm = options.configurableForm;
        this.unconfigurableForm = options.unconfigurableForm;
        this.fieldPicker = options.fieldPicker;

        // clear any fields that are still retained
        this.model.clearRetainedFields();
        this.configurableForm.invalidateFieldsCache();

        this.model.bind("serverError", function (e, smartAjaxResult) {
            instance.$element.html(instance.errorHandler.render(smartAjaxResult));
            instance.triggerEvent("contentRefreshed", [this.$element]);
        });

        // Make sure that the content our renderers produce is appended to our container.
        this.decorateRenderers();
    },

    /**
     * Renders quick form. What is actually rendered is based apon user configuration. Full form will be showed first,
     * but if user switches to configurable form that will be persisted.
     *
     * @return jQuery.Deferred
     */
    render: function () {

        var instance = this,
            deferred = jQuery.Deferred();

        // re-evaluate options
        this.lazyInit();

        instance.model.getUseConfigurableForm().done(function (answer) {
            if (answer === true) {
                instance.configurableForm.render().done(function () {
                    deferred.resolveWith(instance, [instance.$element]);
                    instance.configurableForm.setInitialFocus();
                });
            } else {
                instance.unconfigurableForm.render().done(function () {
                    deferred.resolveWith(instance, [instance.$element]);
                    instance.unconfigurableForm.setInitialFocus();
                });
            }
        }).fail(function () {
            deferred.resolveWith(instance, [instance.$element]);
        });

        return deferred.promise();
    }
});