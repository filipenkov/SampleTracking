(function ($) {

    /**
     * An abstract class that gives the required behaviour for the creating and editing entries. Extend this class and pass
     * it as the {views.row} property of the options passed to AJS.RestfulTable in construction.
     *
     * @class EditRow
     * @namespace AJS.RestfulTable
     */
    AJS.RestfulTable.EditRow = Backbone.View.extend({

        tagName: "tr",

        // delegate events
        events: {
            "focusin" : "_focus",
            "click" : "_focus",
            "click .aui-button-cancel" : "_cancel",
            "keyup" : "_handleKeyUpEvent"
        },

        /**
         * @constructor
         * @param {Object} options
         */
        initialize: function (options) {

            var realRender,
                instance = this;

            this.$el = $(this.el);

            // faster lookup
            this._events = AJS.RestfulTable.Events;
            this.classNames = AJS.RestfulTable.ClassNames;
            this.dataKeys = AJS.RestfulTable.DataKeys;
            this.columns = options.columns;

            this.reorderable = options.reorderable;

            if (options.isUpdateMode) {
                this.isUpdateMode = true;
            } else {
                this._modelClass = options.model;
                this.model = new this._modelClass();
            }

            this.bind(this._events.CANCEL, function () {
                this.disabled = true;
            })
            .bind(this._events.SAVE, function (focusUpdated) {
                if (!this.disabled) {
                    this.submit(focusUpdated);
                }
            })
            .bind(this._events.FOCUS, function (name) {
                this.focus(name);
            })
            .bind(this._events.BLUR, function () {
                this.$el.removeClass(this.classNames.FOCUSED);
                this.disable();
            })
            .bind(this._events.SUBMIT_STARTED, function () {
                this._submitStarted();
            })
            .bind(this._events.SUBMIT_FINISHED, function () {
                this._submitFinished();
            });
        },

        /**
         * Renders default cell contents
         *
         * @param data
         */
        defaultColumnRenderer: function (data) {
            if (data.editable !== false) {
                return $("<input type='text' />")
                    .addClass("text")
                    .attr({
                        name: data.name,
                        value: data.value
                    });
            } else if (data.value) {
                return document.createTextNode(data.value);
            }
        },

        /**
         * Renders drag handle
         * @return jQuery
         */
        renderDragHandle: function () {
            return '<span class="' + this.classNames.DRAG_HANDLE + '"></span></td>';

        },

        /**
         * Executes cancel event if ESC is pressed
         *
         * @param {Event} e
         */
        _handleKeyUpEvent: function (e) {
            if (e.keyCode === 27) {
                this.trigger(this._events.CANCEL);
            }
        },

        /**
         * Fires cancel event
         *
         * @param {Event} e
         * @return AJS.RestfulTable.EditRow
         */
        _cancel: function (e) {
            this.trigger(this._events.CANCEL);
             e.preventDefault();
            return this;
        },


        /**
         * Disables events/fields and adds safe gaurd against double submitting
         *
         * @return AJS.RestfulTable.EditRow
         */
        _submitStarted: function () {
            this.submitting = true;
            this.showLoading()
                .disable()
                .delegateEvents({});

            return this;
        },

        /**
         * Enables events & fields
         *
         * @return AJS.RestfulTable.EditRow
         */
        _submitFinished: function () {
            this.submitting = false;
            this.hideLoading()
                .enable()
                .delegateEvents(this.events);

            return this;
        },

        /**
         * Handles dom focus event, by only focusing row if it isn't already
         *
         * @param {Event} e
         * @return AJS.RestfulTable.EditRow
         */
        _focus: function (e) {
            if (!this.hasFocus()) {
                this.trigger(this._events.FOCUS, e.target.name);
            }
            return this;
        },


        /**
         * Returns true if row has focused class
         *
         * @return Boolean
         */
        hasFocus: function () {
            return this.$el.hasClass(this.classNames.FOCUSED);
        },

        /**
         * Focus specified field (by name or id - first argument), first field with an error or first field (DOM order)
         *
         * @param name
         * @return AJS.RestfulTable.EditRow
         */
        focus: function (name) {

            var $focus,
                $error;

            this.enable();

            if (name) {
                $focus = this.$el.find(":input[name=" + name + "], #" + name);
            } else {

                $error = this.$el.find(this.classNames.ERROR + ":first");

                if ($error.length === 0) {
                    $focus = this.$el.find(":input:text:first");
                } else {
                    $focus = $error.parent().find(":input");
                }
            }

            this.$el.addClass(this.classNames.FOCUSED);

//            if (this.$el.find(":input").isInView()) {
                $focus.focus().trigger("select");
//            }

            return this;
        },

        /**
         * Disables all fields
         *
         * @return AJS.RestfulTable.EditRow
         */
        disable: function () {

            var $replacementSumit,
                $submit;

            // firefox does not allow you to submit a form if there are 2 or more submit buttons in a form, even if all but
            // one is disabled. It also does not let you change the type="submit' to type="button". Therfore he lies the hack.
            if ($.browser.mozilla) {

                $submit = this.$el.find(":submit");

                if ($submit.length) {

                    $replacementSumit = $("<input type='submit' class='" + this.classNames.SUBMIT + "' />")
                            .addClass($submit.attr("className"))
                            .val($submit.val())
                            .data(this.dataKeys.ENABLED_SUBMIT, $submit);

                    $submit.replaceWith($replacementSumit);
                }
            }

            this.$el.addClass(this.classNames.DISABLED)
                    .find(":submit")
                    .attr("disabled", "disabled");

            return this;
        },

         /**
         * Enables all fields
         *
         * @return AJS.RestfulTable.EditRow
         */
        enable: function () {

            var $placeholderSubmit,
                $submit;

            // firefox does not allow you to submit a form if there are 2 or more submit buttons in a form, even if all but
            // one is disabled. It also does not let you change the type="submit' to type="button". Therfore he lies the hack.
            if ($.browser.mozilla) {
                $placeholderSubmit = this.$el.find(this.classNames.SUBMIT),
                $submit = $placeholderSubmit.data(this.dataKeys.ENABLED_SUBMIT);

                if ($submit && $placeholderSubmit.length) {
                    $placeholderSubmit.replaceWith($submit);
                }
            }


            this.$el.removeClass(this.classNames.DISABLED)
                    .find(":submit")
                    .removeAttr("disabled");

            return this;
        },

        /**
         * Shows loading indicator
         * @return AJS.RestfulTable.EditRow
         */
        showLoading: function () {
            this.$el.addClass(this.classNames.LOADING);
            return this;
        },

        /**
         * Hides loading indicator
         * @return AJS.RestfulTable.EditRow
         */
        hideLoading: function () {
            this.$el.removeClass(this.classNames.LOADING);
            return this;
        },

        /**
         * If any of the fields have changed
         * @return {Boolean}
         */
        hasUpdates: function () {
            return !!this.mapSubmitParams(this.$el.serializeObject());
        },

        mapSubmitParams: function (params) {
            return this.model.changedAttributes(params);
        },

        /**
         *
         * Handle submission of new entries and editing of old.
         *
         * @param {Boolean} focusUpdated - flag of whether to focus read-only view after succssful submission
         * @return AJS.RestfulTable.EditRow
         */
        submit: function (focusUpdated) {


            var instance = this,
                values;

            // IE doesnt like it when the focused element is removed

            if (document.activeElement !== window) {
                $(document.activeElement).blur();
            }

            if (this.isUpdateMode) {

                values = this.mapSubmitParams(this.$el.serializeObject()); // serialize form fields into JSON

                if (!values) {
                    return instance.trigger(instance._events.CANCEL);
                }
            } else {
                this.model.clear();
                values = this.mapSubmitParams(this.$el.serializeObject()); // serialize form fields into JSON
            }

            this.trigger(this._events.SUBMIT_STARTED);

             /* Attempt to add to server model. If fail delegate to createView to render errors etc. Otherwise,
               add a new model to this._models and render a row to represent it. */
            this.model.save(values, {

                success: function () {

                    if (instance.isUpdateMode) {
                        instance.trigger(instance._events.UPDATED, instance.model, focusUpdated);
                    } else {

                        instance.render({errors: {}, values: {}})
                                .trigger(instance._events.CREATED, instance.model.toJSON())
                                .trigger(instance._events.FOCUS);

                        instance.model = new instance._modelClass(); // reset
                    }

                    instance.trigger(instance._events.SUBMIT_FINISHED);
                },

                error: function (model, data, xhr) {

                    if (xhr.status === 400) {

                        instance.renderErrors(data.errors)
                                .trigger(instance._events.VALIDATION_ERROR, data.errors);
                    }

                    instance.trigger(instance._events.SUBMIT_FINISHED);
                },

                silent: true
            });

            return this;
        },
        /**
         * Render an error message
         * @param msg
         * @return {jQuery}
         */
        renderError: function (name, msg) {
            return $("<div />").attr("data-field", name).addClass(this.classNames.ERROR).text(msg);
        },

        /**
         * Render and append error messages. The property name will be matched to the input name to determine which cell to
         * append the error message to. If this does not meet your needs please extend this method.
         *
         * @param errors
         */
        renderErrors: function (errors) {

            var instance = this;

            this.$("." + this.classNames.ERROR).remove(); // avoid duplicates

            if (errors) {
                $.each(errors, function (name, msg) {
                    instance.$el.find("[name='" + name + "']")
                            .closest("td")
                            .append(instance.renderError(name, msg));
                });
            }

            return this;
        },


        /**
         * Handles rendering of row
         *
         * @param {Object} renderData
         * ... {Object} vales - Values of fields
         */
        render: function  (renderData) {

            var instance = this;

            this.$el.empty();

            if (this.reorderable) {
                $('<td  class="' + this.classNames.ORDER + '" />').append(this.renderDragHandle()).appendTo(instance.$el);
            }

            $.each(this.columns, function (i, column) {

                var contents,
                    $cell,
                    value = renderData.values[column.id],
                    args = [
                        {name: column.id, value: value, editable: column.editable},
                        renderData.values,
                        instance.model
                    ],
                    methodName = column.id.replace(/(^\w{1})(.*)/, function (str, firstChar, theRest) {
                        return "render" + firstChar.toUpperCase() + theRest
                    });

                if (instance[methodName]) {
                    contents = instance[methodName].apply(instance, args);
                } else {
                    contents = instance.defaultColumnRenderer.apply(instance, args);
                }

                $cell = $("<td />").append(contents);

                if (column.styleClass) {
                    $cell.addClass(column.styleClass);
                }

                $cell.appendTo(instance.$el);

            });

            this.$el
                .append(this.renderOperations(renderData.update, renderData.values)) // add submit/cancel buttons
                .addClass(this.classNames.ROW + " " + this.classNames.EDIT_ROW);

            this.trigger(this._events.RENDER, this.$el, renderData.values);

            this.$el.trigger(this._events.CONTENT_REFRESHED, [this.$el]);

            return this;
        },

        /**
         *
         * Gets markup for add/update and cancel buttons
         *
         * @param {Boolean} update
         */
        renderOperations: function (update) {

            var $operations = $('<td class="aui-restfultable-operations" />');

            if (update) {
                $operations.append($('<input class="aui-button" type="submit" />').attr({
                    accesskey: this.submitAccessKey,
                    value: AJS.I18n.getText('aui.words.update')
                }))
                .append($('<a class="aui-button-cancel" href="#" />')
                .text(AJS.I18n.getText('aui.words.cancel'))
                .attr({
                    accesskey:  this.cancelAccessKey
                }));
            } else {
                $operations.append($('<input class="aui-button" type="submit" />').attr({
                    accesskey: this.submitAccessKey,
                    value: AJS.I18n.getText('aui.words.add')
                }))
            }
            return $operations.add($('<td class="aui-restfultable-throbber" />'));
        }
    });

})(AJS.$);