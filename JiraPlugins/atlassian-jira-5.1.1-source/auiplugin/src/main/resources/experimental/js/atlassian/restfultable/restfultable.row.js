(function ($) {

    /**
     * An abstract class that gives the required behaviour for RestfulTable rows.
     * Extend this class and pass it as the {views.row} property of the options passed to AJS.RestfulTable in construction.
     *
     * @class Row
     * @namespace AJS.RestfulTable
     */
    AJS.RestfulTable.Row = Backbone.View.extend({

        // Static Const
        tagName: "tr",

        // delegate events
        events: {
            "click .aui-restfultable-editable" : "edit"
        },

        /**
         * @constructor
         * @param {object} options
         */
        initialize: function (options) {

            var instance = this;

            options = options || {};

            // faster lookup
            this._events = AJS.RestfulTable.Events;
            this.classNames = AJS.RestfulTable.ClassNames;
            this.dataKeys = AJS.RestfulTable.DataKeys;

            this.columns = options.columns;

            if (!this.events["click .aui-restfultable-editable"]) {
                throw new Error("It appears you have overridden the events property. To add events you will need to use"
                        + "a work around. https://github.com/documentcloud/backbone/issues/244")
            }
            this.index = options.index || 0;
            this.reorderable = options.reorderable;

            this.$el = $(this.el);

            this.bind(this._events.CANCEL, function () {
                this.disabled = true;
            })
            .bind(this._events.FOCUS, function (field) {
                this.focus(field);
            })
            .bind(this._events.BLUR, function () {
                this.unfocus();
            })
            .bind(this._events.UPDATED, function () {
                this._showUpdated();
            })
            .bind(this._events.MODAL, function () {
                this.$el.addClass(this.classNames.ACTIVE);
            })
            .bind(this._events.MODELESS, function () {
                this.$el.removeClass(this.classNames.ACTIVE)
            });
        },

        /**
         * Renders drag handle
         * @return jQuery
         */
        renderDragHandle: function () {
            return '<span class="' + this.classNames.DRAG_HANDLE + '"></span></td>';

        },

        /**
         * Renders default cell contents
         *
         * @param data
         * @return {undefiend, String}
         */
        defaultColumnRenderer: function (data) {
            if (data.value) {
                return document.createTextNode(data.value.toString());
            }
        },

        /**
         * Fades row from blue to transparent
         */
        _showUpdated: function () {

            var instance = this,
                cells = this.$el
                        .addClass(this.classNames.ANIMATING)
                        .find("td")
                        .css("backgroundColor","#ebf1fd");

            this.trigger(this._events.ANIMATION_STARTED);

            instance.delegateEvents({});

            setTimeout(function () {
                cells.animate({
                    backgroundColor: "white"
                }, function () {
                    cells.css("backgroundColor", "");
                    instance.trigger(instance._events.ANIMATION_FINISHED);
                    $(document).one("mousemove", function () {
                        instance.delegateEvents();
                        instance.$el.removeClass(instance.classNames.ANIMATING);
                    });
                });
            }, 500)
        },

        /**
         * Save changed attributes back to server and re-render
         *
         * @param attr
         * @return {AJS.RestfulTable.Row}
         */
        sync: function (attr) {

            this.model.addExpand(attr);

            var instance = this;

            this.showLoading();

            this.model.save(attr, {
                success: function () {
                    instance.hideLoading().render();
                    instance.trigger(instance._events.UPDATED);
                },
                error: function () {
                    instance.hideLoading();
                }
            });

            return this;
        },

        /**
         * Get model from server and re-render
         *
         * @return {AJS.RestfulTable.Row}
         */
        refresh: function (success, error) {

            var instance = this;

            this.showLoading();

            this.model.fetch({
                success: function () {
                    instance.hideLoading().render();
                    if (success) {
                        success.apply(this, arguments);
                    }
                },
                error: function () {
                    instance.hideLoading();
                    if (error) {
                        error.apply(this, arguments);
                    }
                }
            });

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
         * Adds focus class (Item has been recently updated)
         *
         * @return AJS.RestfulTable.Row
         */
        focus: function () {
            $(this.el).addClass(this.classNames.FOCUSED);
            return this;
        },

        /**
         * Removes focus class
         *
         * @return AJS.RestfulTable.Row
         */
        unfocus: function () {
            $(this.el).removeClass(this.classNames.FOCUSED);
            return this;

        },

        /**
         * Adds loading class (to show server activity)
         *
         * @return AJS.RestfulTable.Row
         */
        showLoading: function () {
            this.$el.addClass(this.classNames.LOADING);
            return this;
        },

        /**
         * Hides loading class (to show server activity)
         *
         * @return AJS.RestfulTable.Row
         */
        hideLoading: function () {
            this.$el.removeClass(this.classNames.LOADING);
            return this;
        },

        /**
         * Switches row into edit mode
         *
         * @param e
         */
        edit: function (e) {
            var field;
            if ($(e.target).is("." + this.classNames.EDITABLE)) {
                field = $(e.target).attr("data-field-name");
            } else {
                field = $(e.target).closest("." + this.classNames.EDITABLE).attr("data-field-name");
            }
            this.trigger(AJS.RestfulTable.Events.EDIT_ROW, field);
            return this;
        },

        /**
         * Can be overriden to add custom options
         *
         */
        renderOperations: function () {
            var instance = this;
            return $("<a href='#' class='aui-button' />")
                .addClass(this.classNames.DELETE)
                .text(AJS.I18n.getText("delete")).click(function (e) {
                e.preventDefault();
                instance.destroy();
            });
        },

        /**
         * Removes entry from table
         */
        destroy: function () {
            this.model.destroy();
        },

        /**
         * Renders a generic edit row. You probably want to override this in a sub class.
         *
         * @return AJS.RestfulTable.Row
         */
        render: function  () {

            var instance = this,
                renderData = this.model.toJSON(),
                $opsCell = $("<td class='aui-restfultable-operations' />").append(this.renderOperations({}, renderData)),
                $throbberCell = $("<td class='aui-restfultable-throbber' />");

            // restore state
            this.$el
                .removeClass(this.classNames.DISABLED + " " + this.classNames.FOCUSED + " " + this.classNames.LOADING)
                .addClass(this.classNames.READ_ONLY)
                .empty();


            if (this.reorderable) {
                $('<td  class="' + this.classNames.ORDER + '" />').append(this.renderDragHandle()).appendTo(instance.$el);
            }

            $.each(this.columns, function (i, column) {

                var contents,
                    $cell = $("<td />"),
                    value = renderData[column.id],
                    fieldName = column.fieldName || column.id,
                    args = [{name: fieldName, value: value, editable: column.editable}, renderData, instance.model],
                    methodName = column.id.replace(/(^\w{1})(.*)/, function (str, firstChar, theRest) {
                        return "render" + firstChar.toUpperCase() + theRest
                    });

                if (instance[methodName]) {
                    contents = instance[methodName].apply(instance, args);
                } else {
                    contents = instance.defaultColumnRenderer.apply(instance, args);
                }

                if (column.editable !== false) {
                    var $editableRegion = $("<span />")
                        .addClass(instance.classNames.EDITABLE)
                        .append('<span class="aui-icon icon-edit-sml" />')
                        .append(contents)
                        .attr("data-field-name", fieldName);

                    $cell  = $("<td />").append($editableRegion).appendTo(instance.$el);

                    if (!contents || $.trim(contents) == "") {
                        $cell.addClass(instance.classNames.NO_VALUE);
                        $editableRegion.html($("<em />").text(this.emptyText || AJS.I18n.getText("aui.enter.value")));
                    }

                } else {
                    $cell.append(contents);
                }

                if (column.styleClass) {
                    $cell.addClass(column.styleClass);
                }

                $cell.appendTo(instance.$el);
            });

            this.$el
                .append($opsCell)
                .append($throbberCell)
                .addClass(this.classNames.ROW + " " + this.classNames.READ_ONLY)

            this.trigger(this._events.RENDER, this.$el, renderData);
            this.$el.trigger(this._events.CONTENT_REFRESHED, [this.$el]);
            return this;
        }
    });

})(AJS.$);