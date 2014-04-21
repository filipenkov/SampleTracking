/**
 * A controller for all the different 'QuickForm' views. Determines what view to show (Configurable, Unconfigurable)
 * and what triggers them.
 *
 * @class Container
 */
JIRA.Forms.Container = AJS.Control.extend({

    /**
     * @constructor
     * @param options - This can either be specifed as an object or a function that returns an object
     * ... {Array<String>} globalEventNamespaces - Events will be triggered on these namespaces
     * e.g EditForm.switchedToConfigurableForm. EditForm being the specified global namespace.
     * ... {JIRA.Forms.Model} model
     * ... {JIRA.Forms.FieldPicker} fieldPicker
     * ... {JIRA.Forms.AbstractForm} configurableForm
     * ... {JIRA.Forms.AbstractForm} unconfigurableForm
     */
    init: function (options) {
        this.$element = jQuery("<div />").addClass("qf-container"); // Container element - All html will be appended here
        this.options = JIRA.makePropertyFunc(options); // Treat all options as option function to get uniformity
        this.fieldPicker = new JIRA.Forms.FieldPicker();
        this.successData = [];
    },

    /**
     * Wraps the render methods of each of the views to collect the html and append it to our container element ($element)
     * Also does things like binds/triggers events and decorates content.
     */
    decorateRenderers: function () {

        var instance = this;

        if (!this.fieldPicker.decorated) {

            this.fieldPicker.bind("switchedToAll", function () {
                instance.unconfigurableForm.render(instance.currentView.serialize(true)).done(function () {
                    instance.fieldPicker.show();
                })
            });

            this.fieldPicker.bind("switchedToCustom", function () {
                instance.configurableForm.render(instance.currentView.serialize(true)).done(function () {
                    instance.fieldPicker.show();
                });
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
                instance.triggerEvent("configurableFormRendered");
            });

            this.configurableForm.bind("submitted", function (e, data) {
                instance.successData.push(data);
                instance.triggerEvent("configurableFormSubmitted");
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
                instance.triggerEvent("unconfigurableFormRendered");
            });

            this.unconfigurableForm.bind("submitted", function (e, data) {
                instance.successData.push(data);
                instance.triggerEvent("unconfigurableFormSubmitted");
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

        function focus() {
            if (instance.currentView.setInitialFocus) {
                instance.currentView.setInitialFocus();
            }
        }

        var instance = this;

        // If contents is already in the dom when we do the .html call below all events will be unbound. Doing .detach avoids this.
        contents.detach();
        this.$element.html(contents);
        this.triggerEvent("contentRefreshed", [this.$element]);

        // JRADEV-8551 - Changing Issue Type on Create Subtask sends the focus to the page behind the dialog.
        if (jQuery.browser.msie) {
            window.setTimeout(focus, 0);
        } else {
            focus();
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
            dialog;

        this.dialog = dialog = new JIRA.FormDialog({
                id: options.id,
                trigger: options.trigger,
                windowTitle: options.windowTitle,
                width: JIRA.Dialog.WIDTH_PRESETS.large,
                delayShowUntil: JIRA.Dialogs.waitForSavesToComplete,
                content: function (ready) {
                    instance.render().done(function () {
                        ready(instance.$element);
                    });

                    instance.triggerEvent("dialogShown");
                },
                submitHandler: function (e, ready) {
                    e.preventDefault();
                    instance.currentView.submit().then(ready, ready);
                },
                stacked: true
            });

        this.dialog._focusFirstField = function() {
            JIRA.FormDialog.prototype._focusFirstField.apply(this, ["div.content :input:visible:first"]);
        };

        // Whenever the forms signal they have finished doing there thing (the user has dismissed them). Close the dialog also.
        this.bind("initialized", function () {
            this.unconfigurableForm.bind("sessionComplete", function () {
                dialog.hide();

            });
            this.configurableForm.bind("sessionComplete", function () {
                dialog.hide();
            });
        })
         /* Ensure that whenever the user config form updates it's content, for example switching to full form,
           Content is decorated (title put in header etc) and dialog is correctly positioned. */
        .bind("contentRefreshed", function () {
            dialog.notifyOfNewContent();
        });

        dialog.bind("Dialog.hide", function () {
            if (instance.successData.length) {
                instance.triggerEvent("sessionComplete", [instance.successData], true);
                instance.successData = [];
            }
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

        // invoke dirty form warning plugin
        dialog.dirtyFormWarning();

        return dialog;
    },

    /**
     * As our options object can be a function we re-evaluate our options whenever this method is called. This is useful
     * when you are using the same container to build different forms, for example on the issue navigator we want to change
     * what issue we are editing based on what issue is selected.
     */
    lazyInit: function () {

        var instance = this,
            options = this.options.call(this);

        this.model = options.model;
        this.errorHandler = options.errorHandler;
        this.globalEventNamespaces = options.globalEventNamespaces;
        this.configurableForm = options.configurableForm;
        this.unconfigurableForm = options.unconfigurableForm;
        this.sessionComplete = options.sessionComplete;

        // clear any fields that are still retained
        this.model.clearRetainedFields();
        this.configurableForm.invalidateFieldsCache();

        this.model.bind("serverError", function (e, smartAjaxResult) {
            instance.$element.html(instance.errorHandler.render(smartAjaxResult));
            instance.triggerEvent("contentRefreshed", [this.$element]);
        });

        this.triggerEvent("initialized");

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
                instance.configurableForm.render().done(function (el, scripts) {
                    deferred.resolveWith(instance, [instance.$element]);
                    instance.$element.append(scripts);
                });
            } else {
                instance.unconfigurableForm.render().done(function (el, scripts) {
                    deferred.resolveWith(instance, [instance.$element, scripts]);
                    instance.$element.append(scripts);
                });
            }
        }).fail(function () {
            deferred.resolveWith(instance, [instance.$element]);
        });

        return deferred.promise();
    }
});