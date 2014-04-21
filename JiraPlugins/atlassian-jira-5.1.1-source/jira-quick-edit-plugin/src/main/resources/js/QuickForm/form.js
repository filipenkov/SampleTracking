/**
 * A View that contains common logic for configurable and unconfigurable forms.
 *
 * @class AbstractForm
 */
JIRA.Forms.AbstractForm = AJS.Control.extend({

    /**
     * Serialises form data and posts it to specified action. If the server returns with validation errors (400), they
     * will be added inline to the form. If the server returns success the window will be reloaded.
     */
    submit: function () {

        var instance = this;

        instance.getForm().addClass("submitting");

        return JIRA.SmartAjax.makeRequest({
            url: this.getAction(),
            type: "POST",
            beforeSend: function () {
                instance.disable();
            },
            data: this.serialize(),
            complete: function (xhr, textStatus, smartAjaxResult) {

                var data = smartAjaxResult.data;

                instance.getForm().find(".aui-message-context").remove(); // remove all previous messages

                instance.enable();

                // remove stale errors
                instance.getForm().find(".error").remove();

                if (smartAjaxResult.successful) {
                    if (data && data.fields) {
                        instance.invalidateFieldsCache();
                        instance.model.setFields(data.fields);
                    }

                    if (typeof data === "string") {
                        instance.$element.html(AJS.extractBodyFromResponse(smartAjaxResult.data));
                        instance.triggerEvent("rendered", [instance.$element]);
                    } else  {
                        instance.handleSubmitSuccess(smartAjaxResult.data);
                    }

                } else {

                    var errors;

                    try {
                        errors = JSON.parse(xhr.responseText);
                    } catch (e) {
                        // catch error
                    }

                    if (errors) {

                        if (errors.errorMessages && errors.errorMessages.length) {
                            JIRA.applyErrorMessageToForm(instance.getForm(), errors.errorMessages[0]);
                        }

                        if (errors && errors.errors && xhr.status === 400) {

                            // (JRADEV-6684) make sure they are all visibile before we apply the errors

                            if (instance.getFieldById) {
                                jQuery.each(errors.errors, function (id) {

                                    if (/^timetracking/.test(id)) {
                                        instance.getFieldById("timetracking").done(function (field) {
                                            JIRA.trigger(JIRA.Events.VALIDATE_TIMETRACKING, [instance.$element]);
                                            field.activate(true);
                                        });
                                    } else if (/^worklog/.test(id)) {
                                        instance.getFieldById("worklog").done(function (field) {
                                            JIRA.trigger(JIRA.Events.VALIDATE_TIMETRACKING, [instance.$element]);
                                            field.activate(true);
                                        });
                                    } else {
                                        instance.getFieldById(id).done(function (field) {
                                            field.activate(true);
                                        });
                                    }
                                });
                            }

                            JIRA.applyErrorsToForm(instance.getForm(), errors.errors);
                            instance.triggerEvent("validationError", [instance, errors.errors], true);
                        }
                    }
                }

                instance.getForm().removeClass("submitting");
            }
        });
    },

    /**
     * Disables all form fields
     */
    disable: function () {
        this.getForm().find(":input").attr("disabled", "disabled").trigger("disable");
        this.getForm().find(":submit").attr("disabled", "disabled");

    },

    /**
     * Enables all form fields
     */
    enable: function () {
        this.getForm().find(":input").removeAttr("disabled").trigger("enable");
        this.getForm().find(":submit").removeAttr("disabled");
    },

     /**
     * Gets array of active fields in DOM order
     *
     * @return Array<String>
     */
    getActiveFieldIds: function () {
       throw new Error("getActiveFieldIds: Abstract, must be implemented by sub class");
    },

    serialize: function (forceRetainAll) {

        var instance = this,
            postBody = this.getForm().serialize();

        if (this.model.hasRetainFeature()) {

            // we retain all values, except the ones filtered by the model
            jQuery.each(this.getActiveFieldIds(), function (i, fieldId) {
                instance.model.addFieldToRetainValue(fieldId, forceRetainAll);
            });

            jQuery.each(this.model.getFieldsWithRetainedValues(), function (i, id) {
                postBody = postBody + "&fieldsToRetain=" + id;
            });
        }

        return postBody;
    },

    /**
     * Delete fields reference which has the knock on effect of forcing us to go back to the model to get a fresh
     * version of fields.
     */
    invalidateFieldsCache: function () {
        delete this.fields;
    },

    /**
     * Sets initial field to be focused after rendering
     */
    setInitialFocus: function () {
        this.getFormContent().find(":input:first").focus();
    },

    /**
     * Reloads window after form has been successfully submitted
     */
    handleSubmitSuccess: function () {
        this.triggerEvent("submitted");
        AJS.reloadViaWindowLocation();
    },

     /**
     * Gets action to post form to
     *
     * @return String
     */
    getAction: function () {
        return this.action;
    },

    /**
     * Gets form content, this is where all the fields get appended to
     * @return {jQuery}
     */
    getFormContent: function () {
        return this.$element.find("div.content");
    },

    /**
     * Gets form
     * @return {jQuery}
     */
    getForm: function () {
        return this.$element.find("form");
    },

    /**
     * Creates Field View Class
     */
    createField: function () {
        throw new Error("JIRA.Forms.AbstractForm: You must implement [createField] method in subclass.");
    },

    /**
     * Renders complete form. If 'values' are defined then model will be refreshed (go to server) to get fields html
     * with populated values.
     *
     * @param {String} serialized values to populate as field values
     * @return jQuery.Promise
     */
    render: function (values) {

        var deferred = jQuery.Deferred(),
            instance = this;

        if (values) {
            this.invalidateFieldsCache(); // delete reference to fields cache so that we actually get the refreshed fields html
            this.model.refresh(values).done(function () {
                instance._render().done(function (el, scripts) {
                    instance.triggerEvent("rendered", [instance.$element]);
                    deferred.resolveWith(instance, [instance.$element, scripts]);
                });
            });
        } else {
            instance._render().done(function (el, scripts) {
                instance.triggerEvent("rendered", [instance.$element]);
                deferred.resolveWith(instance, [instance.$element, scripts]);
            });
        }

        return deferred.promise();
    }

});

/**
 * A View class that renders a form. The form provides controls that allows a user to configure which fields are shown
 * using a picker (@see JIRA.Forms.FieldPicker). Users can also configure the order of these fields using
 * drag and drop.
 *
 * @class AbstractConfigurableForm
 */
JIRA.Forms.AbstractConfigurableForm = JIRA.Forms.AbstractForm.extend({


    /**
     * Gets all fields
     *
     * @param Array<JIRA.Forms.Field> fields
     * @return jQuery Promise
     */
    getFields: function () {

        var deferred = jQuery.Deferred(),
            instance = this;

        if (!this.fields) {

            this.fields = [];

            this.model.getConfigurableFields().done(function (fields) {
                jQuery.each(fields, function (i, descriptor) {
                    var field = instance.createField(descriptor);
                    instance.fields.push(field);

                });
                deferred.resolveWith(instance, [instance.fields]);
            });
        } else {
            deferred.resolveWith(this, [this.fields]);
        }

        return deferred.promise();
    },

    /**
     * Gets ids for all visible fields
     * @return Array
     */
    getActiveFieldIds: function () {

        var ids = [],
            els = this.$element.find(".qf-field.qf-field-active:not(.qf-required)");

        jQuery.each(els, function (i, el) {

            var $el = jQuery(el);

            // We get the id from the field control we attached using jQuery data.
            ids.push($el.data("model").getId());
        });

        return ids;
    },


    /**
     * Creates Field View
     *
     * @param descriptor
     * @return {JIRA.Forms.ConfigurableField}
     */
    createField: function (descriptor) {

        descriptor.hasVisibilityFeature = this.model.hasVisibilityFeature(descriptor);

        if (this.model.hasRetainFeature(descriptor)) {
            descriptor.hasRetainFeature = true;
            descriptor.retainValue = this.model.hasRetainedValue(descriptor);
        }

        var instance = this,
            field = new JIRA.Forms.ConfigurableField(descriptor);

        if (descriptor.hasVisibilityFeature) {

            // When we activate a field focus & pesist it
            field.bind("activated", function () {
                instance.model.setUserFields(instance.getActiveFieldIds());
                field.highlight();
                instance.triggerEvent("QuickForm.fieldAdded", [field]);
            }).bind("disabled", function () {
                instance.model.setUserFields(instance.getActiveFieldIds());
                instance.triggerEvent("QuickForm.fieldRemoved", [field]);
            });
        }

        return field;

    },

    /**
     * Gets the field view instance by id
     *
     * @param id
     * @return jQuery.Promise
     */
    getFieldById: function (id) {

        var instance = this,
            deferred = jQuery.Deferred();

        this.getFields().done(function (fields) {
            jQuery.each(fields, function (i, field) {
                if (field.getId() === id) {
                    deferred.resolveWith(instance, [field]);
                }
            });

            deferred.rejectWith(instance, []);
        });

        return deferred.promise();
    },

    /**
     * Determines if there are any visible fields
     *
     * @return Boolean
     */
    hasNoVisibleFields: function () {
        var deferred = jQuery.Deferred();
        deferred.resolve(this.getActiveFieldIds().length === 0);
        return deferred.promise();
    },

    /**
     * Renders form contents and applies sortable control
     *
     * @return jQuery.promise
     */
    renderFormContents: function () {

        var deferred = jQuery.Deferred(),
            scripts = jQuery(),
            instance = this;

        instance.getFields().done(function (fields) {

            instance.model.getActiveFieldIds().done(function (activeIds) {

                jQuery.each(fields, function () {
                    var result = this.render();
                    // JRADEV-9069 Build up collection of all script tags to be executed post render
                    // Look at JIRA.Forms.Container.render for actual execution
                    scripts = scripts.add(result.scripts);
                    instance.getFormContent().append(result.element);
                });

                // append active fields in prescribe order first
                jQuery.each(activeIds, function (i, fieldId) {
                    jQuery.each(fields, function () {
                        if (this.getId() === fieldId) {
                            this.activate(true);
                        }
                    });
                });

                // Now the inactive ones. We have to append as the field values need to be serialized. Also if there
                // are any js controls they can be bound so that when we toggle the visibility they actually work.
                jQuery.each(fields, function () {
                    if (!this.isActive()) {
                        this.disable(true);
                    }
                });

                // If we have no fields visible, append first 3 (JRADEV-6669)
                instance.hasNoVisibleFields().done(function (answer) {
                    if (answer === true) {
                        for (var i=0; i < 3; i++) {
                           if (fields[i]) {
                               fields[i].activate(true);
                           }
                        }
                    }

                    deferred.resolveWith(this, [instance.$element, scripts]);
                });
            });
        });

        return deferred.promise();
    }

});


/**
 * A View class that renders a form that cannot be configured.
 *
 * @class AbstractConfigurableForm
 */
JIRA.Forms.AbstractUnconfigurableForm =  JIRA.Forms.AbstractForm.extend({

    /**
     * Gets HTML for fields. This includes tabs and tab panes if applicable.
     *
     * @return jQuery.Deferred
     */
    getFieldsHtml: function () {

        var instance = this,
            deferred = jQuery.Deferred(),
            data = {};

        this.model.getTabs().done(function (tabs) {

            if (tabs.length === 1) {
                data.fields = tabs[0].fields;

            } else {
                data.tabs = tabs;
                data.hasTabs = true;
            }

            deferred.resolveWith(instance, [JIRA.Templates.Issue.issueFields(data)]);

        });

        return deferred.promise();
    },

    /**
     * Gets ids for all fields
     *
     * @return {Array}
     */
    getActiveFieldIds: function () {

        var ids = [];

        this.model.getFields().done(function (fields) {
            jQuery.each(fields, function (i, field) {
                ids.push(field.id);
            });
        });

        return ids;
    },

    /**
     * Gets all fields
     *
     * @param Array<JIRA.Forms.Field> fields
     * @return jQuery Promise
     */
    getFields: function () {

        var deferred = jQuery.Deferred(),
            instance = this;

        if (!this.fields) {

            this.fields = [];

            this.model.getFields().done(function (fields) {
                jQuery.each(fields, function (i, descriptor) {
                    var field = instance.createField(descriptor);
                    instance.fields.push(field);

                });
                deferred.resolveWith(instance, [instance.fields]);
            });
        } else {
            deferred.resolveWith(this, [this.fields]);
        }

        return deferred.promise();
    }

});

/**
 * An abstract generic error message renderer
 *
 * @class Error
 */
JIRA.Forms.Error = AJS.Control.extend({

    /**
     * Gets the best reason for error it can from a smartAjaxResult
     *
     * @param smartAjaxResult
     * @return String
     */
    getErrorMessageFromSmartAjax: function (smartAjaxResult) {

        var message,
            data;

        if (smartAjaxResult.hasData && smartAjaxResult.status !== 401) {
            try {
                data = JSON.parse(smartAjaxResult.data);
                if (data.errorMessages && data.errorMessages.length > 0) {
                    message = JIRA.Templates.QuickForm.errorMessage({
                        message: data.errorMessages[0]
                    });
                } else {
                    message = JIRA.SmartAjax.buildDialogErrorContent(smartAjaxResult, true).html();
                }
            } catch (e) {
                message = JIRA.SmartAjax.buildDialogErrorContent(smartAjaxResult, true).html();
            }
        } else {
            message = JIRA.SmartAjax.buildDialogErrorContent(smartAjaxResult, true).html();
        }

        return message;
    },

    /**
     * Renders error message
     *
     * @param smartAjaxResult
     */
    render: function (smartAjaxResult) {
        var errorMessage = this.getErrorMessageFromSmartAjax(smartAjaxResult);
        return this._render(errorMessage);
    }
});