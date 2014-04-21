(function ($) {

    /**
     * A table who's entries/rows are can be retrieved, added and updated via rest (CRUD).
     * It uses backbone.js to sync the tables state back to the server and vice versa, avoiding page refreshes.
     *
     * @class RestfulTable
     */
    AJS.RestfulTable = Backbone.View.extend({

        /**
         * @constructor
         * @param {Object} options
         * ... {String} id - The id for the table. This id will be used to fire events specific to this instance.
         * ... {boolean} editable - Is the table editable. If true, clicking row will switch it to edit state.
         * ... {boolean} createDisabled - Can new entries be added to the table.
         * ... {boolean} reorderable - Can we drag rows to reorder them.
         * ... {String} noEntriesMsg - Message that will be displayed under the table header if it is empty.
         * ... {Array} entries - initial data set to be rendered. Each item in the array will be used to create a new instance of options.model.
         * ... {AJS.RestfulTable.EntryModel} model - backbone model representing a row.
         * ... {Object} views
         * ... ... {AJS.RestfulTable.EditRow} editRow - Backbone view that renders the edit & create row. Your view MUST extend AJS.RestfulTable.EditRow.
         * ... ... {AJS.RestfulTable.Row} row - Backbone view that renders the readonly row. Your view MUST extend AJS.RestfulTable.Row.
         */
        initialize: function (options) {

            var instance = this;


            // combine default and user options
            instance.options = $.extend(true, instance._getDefaultOptions(options), options);

            // Prefix events for this instance with this id.
            instance.id = this.options.id;

            // faster lookup
            instance._events = AJS.RestfulTable.Events;
            instance.classNames = AJS.RestfulTable.ClassNames;
            instance.dataKeys = AJS.RestfulTable.DataKeys;

            // shortcuts to popular elements
            this.$table = $(options.el)
                    .addClass(this.classNames.RESTFUL_TABLE)
                    .addClass(this.classNames.ALLOW_HOVER)
                    .addClass("aui")
                    .addClass(instance.classNames.LOADING);

            this.$table.wrapAll("<form class='aui' action='#' />");
            
            this.$thead = $("<thead/>");
            this.$theadRow = $("<tr />").appendTo(this.$thead);
            this.$tbody = $("<tbody/>");

            if (!this.options.columns) {
                throw new Error("AJS.RestfulTable: Init failed! You haven't provided any columns to render.")
            }

            // Let user know the table is loading
            this.showGlobalLoading();

             $.each(this.options.columns, function (i, column) {
                var header = $.isFunction(column.header) ? column.header() : column.header;
                if (typeof header === "undefined") {
                    console.warn("You have not specified [header] for column [" + column.id + "]. Using id for now...");
                    header = column.id;
                }

                instance.$theadRow.append("<th>" + header + "</th>");
            });

            // columns for submit buttons and loading indicator used when editing
            instance.$theadRow.append("<th></th><th></th>");



            // create a new Backbone collection to represent rows (http://documentcloud.github.com/backbone/#Collection)
            this._models = new this.options.Collection([], {
                comparator: function (row) { // sort models in colleciton based on dom ordering
                    var index;
                    $.each(instance.getRows(), function (i) {
                        if (this.model.id === row.id) {
                            index = i;
                            return false;
                        }
                    });
                    return index;
                }
            });

            // shortcut to the class we use to create rows
            this._rowClass = this.options.views.row;

            if (this.options.editable !== false) {
                this.editRows = []; // keep track of rows that are being edited concurrently
                if (!this.options.createDisabled) {

                    // Create row responsible for adding new entries ...
                    this._createRow = new this.options.views.editRow({
                            columns: this.options.columns,
                            model: this.options.model.extend({
                                url: function () {
                                    return instance.options.resources.self;
                                }
                            }),
                            cancelAccessKey: this.options.cancelAccessKey,
                            submitAccessKey: this.options.submitAccessKey,
                            reorderable: this.options.reorderable
                        })
                        .bind(this._events.CREATED, function (values) {
                            instance.addRow(values, 0);
                        })
                        .bind(this._events.VALIDATION_ERROR, function () {
                            this.trigger(instance._events.FOCUS);
                        })
                        .render({
                            errors: {},
                            values: {}
                        });

                    // ... and appends it as the first row
                    this.$create = $('<tbody class="' + this.classNames.CREATE + '" />')
                        .append(this._createRow.el);

                    // Manage which row has focus
                    this._applyFocusCoordinator(this._createRow);

                    // focus create row
                    this._createRow.trigger(this._events.FOCUS);
                }

                this.$table.closest("form").submit(function (e) {
                    if (instance.focusedRow) {
                        // Delegates saving of row. See AJS.RestfulTable.EditRow.submit
                        instance.focusedRow.trigger(instance._events.SAVE);
                    }
                    e.preventDefault();
                });

                if (this.options.reorderable) {

                    // Add allowance for another cell to the thead
                    this.$theadRow.prepend("<th />")

                    // Allow drag and drop reordering of rows
                    this.$tbody.sortable({
                        handle: "." +this.classNames.DRAG_HANDLE,
                        start: function (event, ui) {

                            var $this = instance._createRow.$el.find("td");

                            // Make sure that when we start dragging widths do not change
                            ui.item
                                .addClass(instance.classNames.MOVEABLE)
                                .children().each(function (i) {
                                    $(this).width($this.eq(i).width());
                                });

                            // Add a <td> to the placeholder <tr> to inherit CSS styles.
                            ui.placeholder
                                .html('<td colspan="' + instance.getColumnCount() + '">&nbsp;</td>')
                                .css("visibility", "visible");

                            // Stop hover effects etc from occuring as we move the mouse (while dragging) over other rows
                            instance.getRowFromElement(ui.item[0]).trigger(instance._events.MODAL);
                        },
                        stop: function (event, ui) {
                            ui.item
                                .removeClass(instance.classNames.MOVEABLE)
                                .children().attr("style", "");

                            ui.placeholder.removeClass(instance.classNames.ROW);

                            // Return table to a normal state
                            instance.getRowFromElement(ui.item[0]).trigger(instance._events.MODELESS);
                        },
                        update: function (event, ui) {

                            var nextModel,
                                nextRow,
                                data = {},
                                row = instance.getRowFromElement(ui.item[0])

                            if (row) {

                                nextRow = ui.item.next()[0];

                                if (nextRow) {

                                    nextModel = instance.getRowFromElement(nextRow).model;

                                    // Get the url of the for the entry befores rest endpoint.
                                    // The server will use this to determine position.
                                    data.after = nextModel.url();
                                } else {
                                    data.position = "First";
                                }

                                $.ajax({
                                    url: row.model.url() + "/move",
                                    type: "POST",
                                    dataType: "json",
                                    contentType: "application/json",
                                    data: JSON.stringify(data),
                                    complete: function () {
                                        // hides loading indicator (spinner)
                                        row.hideLoading();
                                    },
                                    success: function (xhr) {
                                        AJS.triggerEvtForInst(instance._events.REORDER_SUCCESS, instance, [xhr]);
                                    },
                                    error: function (xhr) {
                                        var responseData = $.parseJSON(xhr.responseText || xhr.data);
                                        AJS.triggerEvtForInst(instance._events.SERVER_ERROR, instance, [responseData, xhr]);
                                    }
                                });

                                // shows loading indicator (spinner)
                                row.showLoading();
                            }
                        },
                        axis: "y",
                        delay: 0,
                        containment: "document",
                        cursor: "move",
                        scroll: true,
                        zIndex: 8000
                    });

                    // Prevent text selection while reordering.
                    this.$tbody.bind("selectstart mousedown", function (event) {
                        return !$(event.target).is("." + instance.classNames.DRAG_HANDLE);
                    });
                }
            }

            // when a model is removed from the collection, remove it from the viewport also
            this._models.bind("remove", function (model) {
                $.each(instance.getRows(), function (i, row) {
                    if (row.model === model) {
                        if (row.hasFocus() && instance._createRow) {
                            instance._createRow.trigger(instance._events.FOCUS);
                        }
                        instance.removeRow(row);
                    }
                });
            });

            if ($.isFunction(this.options.resources.all)) {
                this.options.resources.all(function (entries) {
                    instance.populate(entries);
                });
            } else {
                $.get(this.options.resources.all, function (entries) {
                    instance.populate(entries);
                });
            }
        },

        /**
         * Refreshes table with entries
         *
         * @param entries
         */
        populate: function (entries) {

            if (this.options.reverseOrder) {
                entries.reverse();
            }

            this.hideGlobalLoading();
            if (entries && entries.length) {
                // Empty the models collection
                this._models.refresh([], { silent: true });
                // Add all the entries to collection and render them
                this.renderRows(entries);
                // show message to user if we have no entries
                if (this.isEmpty()) {
                    this.showNoEntriesMsg();
                }
            } else {
                this.showNoEntriesMsg();
            }

            // Ok, lets let everyone know that we are done...
            this.$table
                .append(this.$thead)
                .append(this.$create)
                .append(this.$tbody)
                .removeClass(this.classNames.LOADING)
                .trigger(this._events.INITIALIZED, [this]);

            AJS.triggerEvtForInst(this._events.INITIALIZED, this, [this]);

            if (this.options.autoFocus) {
                this.$table.find(":input:text:first").focus(); // set focus to first field
            }
        },

        /**
         * Shows loading indicator and text
         * 
         * @return {AJS.RestfulTable}
         */
        showGlobalLoading: function () {

            if (!this.$loading) {
                 this.$loading =  $('<div class="aui-restfultable-init"><span class="aui-restfultable-throbber">' +
                '</span><span class="aui-restfultable-loading">' + this.options.loadingMsg + '</span></div>');
            }
            if (!this.$loading.is(":visible")) {
                this.$loading.insertAfter(this.$table);
            }

            return this
        },

        /**
         * Hides loading indicator and text
         * @return {AJS.RestfulTable}
         */
        hideGlobalLoading: function () {
            if (this.$loading) {
                this.$loading.remove();
            }
            return this;
        },


        /**
         * Adds row to collection and renders it
         *
         * @param {Object} values
         * @param {number} index
         * @return {AJS.RestfulTable}
         */
        addRow: function (values, index) {

            var view,
                model;

            if (!values.id) {
                throw new Error("AJS.RestfulTable.addRow: to add a row values object must contain an id. "
                        + "Maybe you are not returning it from your restend point?"
                        + "Recieved:" + JSON.stringify(values));
            }

            model = new this.options.model(values);
            view = this._renderRow(model, index);

            this._models.add(model);
            this.removeNoEntriesMsg();

            // Let everyone know we added a row
            AJS.triggerEvtForInst(this._events.ROW_ADDED, this, [view, this]);
            return this;
        },

        /**
         * Provided a view, removes it from display and backbone collection
         *
         * @param {AJS.RestfulTable.Row}
         */
        removeRow: function (row) {

            this._models.remove(row.model);
            row.remove();

            if (this.isEmpty()) {
                this.showNoEntriesMsg();
            }

            // Let everyone know we removed a row
            AJS.triggerEvtForInst(this._events.ROW_REMOVED, this, [row, this]);
        },

        /**
         * Is there any entries in the table
         *
         * @return {Boolean}
         */
        isEmpty: function () {
            return this._models.length === 0;
        },

        /**
         * Gets all models
         *
         * @return {Backbone.Collection}
         */
        getModels: function () {
            return this._models;
        },

        /**
         * Gets table body
         *
         * @return {jQuery}
         */
        getTable: function () {
            return this.$table;
        },

        /**
         * Gets table body
         *
         * @return {jQuery}
         */
        getTableBody: function () {
            return this.$tbody;
        },

        /**
         * Gets create Row
         *
         * @return {B
         */
        getCreateRow: function () {
            return this._createRow;
        },

        /**
         * Gets the number of table colums
         *
         * @return {Number}
         */
        getColumnCount: function () {
            return this.options.columns.length + 2; // plus 2 accounts for the columns allocated to submit buttons and loading indicator
        },

        /**
         * Get the AJS.RestfulTable.Row that corresponds to the given <tr> element.
         *
         * @param {HTMLElement} tr
         * @return {?AJS.RestfulTable.Row}
         */
        getRowFromElement: function (tr) {
            return $(tr).data(this.dataKeys.ROW_VIEW);
        },

        /**
         * Shows message {options.noEntriesMsg} to the user if there are no entries
         *
         * @return {AJS.RestfulTable}
         */
        showNoEntriesMsg: function () {

            if (this.$noEntries) {
                this.$noEntries.remove();
            }

            this.$noEntries = $("<tr>")
                    .addClass(this.classNames.NO_ENTRIES)
                    .append($("<td>")
                        .attr("colspan", this.getColumnCount())
                        .text(this.options.noEntriesMsg)
                    )
                    .appendTo(this.$tbody);

            return this;
        },

        /**
         * Removes message {options.noEntriesMsg} to the user if there ARE entries
         *
         * @return {AJS.RestfulTable}
         */
        removeNoEntriesMsg: function () {
            if (this.$noEntries && this._models.length > 0) {
                this.$noEntries.remove();
            }
            return this;
        },

        /**
         * Gets the AJS.RestfulTable.Row from their associated <tr> elements
         *
         * @return {Array<AJS.RestfulTable.Row>}
         */
        getRows: function () {

            var instance = this,
                views = [];

            this.$tbody.find("." + this.classNames.READ_ONLY).each(function () {

                var $row = $(this),
                    view = $row.data(instance.dataKeys.ROW_VIEW);

                if (view) {
                    views.push(view);
                }
            });

            return views;
        },

        /**
         * Appends entry to end or specified index of table
         *
         * @param {AJS.RestfulTable.EntryModel} model
         * @param index
         * @return {jQuery}
         */
        _renderRow: function (model, index) {

            var instance = this,
                $rows = this.$tbody.find("." + this.classNames.READ_ONLY),
                $row,
                view;

            view = new this._rowClass({
                model: model,
                columns: this.options.columns,
                reorderable: this.options.reorderable
            });

            this.removeNoEntriesMsg();

            view.bind(this._events.EDIT_ROW, function (field) {
                instance.edit(this, field);
            });

            $row = view.render().$el;

            if (index !== -1) {

                if (typeof index === "number" && $rows.length !== 0) {
                    $row.insertBefore($rows[index]);
                } else {
                    this.$tbody.append($row);
                }
            }

            $row.data(this.dataKeys.ROW_VIEW, view);

            // deactivate all rows - used in the cases, such as opening a dropdown where you do not want the table editable
            // or any interactions
            view.bind(this._events.MODAL, function () {
                instance.$table.removeClass(instance.classNames.ALLOW_HOVER);
                instance.$tbody.sortable("disable");
                $.each(instance.getRows(), function () {
                    if (!instance.isRowBeingEdited(this)) {
                        this.delegateEvents({}); // clear all events
                    }
                });
            });

            view.bind(this._events.ANIMATION_STARTED, function () {
                instance.$table.removeClass(instance.classNames.ALLOW_HOVER);
            });

            view.bind(this._events.ANIMATION_FINISHED, function () {
                instance.$table.addClass(instance.classNames.ALLOW_HOVER);
            });

            // activate all rows - used in the cases, such as opening a dropdown where you do not want the table editable
            // or any interactions
            view.bind(this._events.MODELESS, function () {
                instance.$table.addClass(instance.classNames.ALLOW_HOVER);
                instance.$tbody.sortable("enable");
                $.each(instance.getRows(), function () {
                    if (!instance.isRowBeingEdited(this)) {
                        this.delegateEvents(); // rebind all events
                    }
                });
            });

            // ensure that when this row is focused no other are
            this._applyFocusCoordinator(view);

            this.trigger(this._events.ROW_INITIALIZED, view);

            return view;
        },

        /**
         * Returns if the row is edit mode or note
         *
         * @param {AJS.RestfulTable.Row} - read onyl row to check if being edited
         * @return {Boolean}
         */
        isRowBeingEdited: function (row) {

            var isBeingEdited = false;

            $.each(this.editRows, function () {
                if (this.el === row.el) {
                    isBeingEdited = true;
                    return false;
                }
            });

            return isBeingEdited;
        },

        /**
         * Ensures that when supplied view is focused no others are
         *
         * @param {Backbone.View} view
         * @return {AJS.RestfulTable}
         */
        _applyFocusCoordinator: function (view) {

            var instance = this;

            if (!view.hasFocusBound) {

                view.hasFocusBound = true;

                view.bind(this._events.FOCUS, function () {
                    if (instance.focusedRow && instance.focusedRow !== view) {
                        instance.focusedRow.trigger(instance._events.BLUR);
                    }
                    instance.focusedRow = view;
                    if (view instanceof AJS.RestfulTable.Row && instance._createRow) {
                        instance._createRow.enable();
                    }
                });
            }

            return this;
        },

        /**
         * Remove specificed row from collection holding rows being concurrently edited
         *
         * @param {AJS.RestfulTable.EditRow} editView
         * @return {AJS.RestfulTable}
         */
        _removeEditRow: function (editView) {
            var index = $.inArray(editView, this.editRows);
            this.editRows.splice(index, 1);
            return this;
        },

        /**
         * Focuses last row still being edited or create row (if it exists)
         *
         * @return {AJS.RestfulTable}
         */
        _shiftFocusAfterEdit: function () {

            if (this.editRows.length > 0) {
                this.editRows[this.editRows.length-1].trigger(this._events.FOCUS);
            } else if (this._createRow) {
                this._createRow.trigger(this._events.FOCUS);
            }

            return this;
        },

        /**
         * Evaluate if we save row when we blur. We can only do this when there is one row being edited at a time, otherwise
         * it causes an infinate loop JRADEV-5325
         *
         * @return {boolean}
         */
        _saveEditRowOnBlur: function () {
             return this.editRows.length <= 1;
        },

        /**
         * Dismisses rows being edited concurrently that have no changes
         */
        dismissEditRows: function () {
            var instance = this;
            $.each(this.editRows, function () {
                if (!this.hasUpdates()) {
                    this.trigger(instance._events.FINISHED_EDITING);
                }
            });
        },

        /**
         * Converts readonly row to editable view
         *
         * @param {Backbone.View} row
         * @param {String} field - field name to focus
         * @return {Backbone.View} editRow
         */
        edit: function (row, field) {

            var instance = this,
                editRow = new this.options.views.editRow({
                    el: row.el,
                    columns: this.options.columns,
                    isUpdateMode: true,
                    reorderable: this.options.reorderable,
                    model: row.model,
                    cancelAccessKey: this.options.cancelAccessKey,
                    submitAccessKey: this.options.submitAccessKey
                }),
                values = row.model.toJSON();
                values.update = true;
                editRow.render({
                    errors: {},
                    update: true,
                    values: values
                })
                .bind(instance._events.UPDATED, function (model, focusUpdated) {
                    instance._removeEditRow (this);
                    this.unbind();
                    row.render().delegateEvents(); // render and rebind events
                    row.trigger(instance._events.UPDATED); // trigger blur fade out
                    if (focusUpdated !== false) {
                        instance._shiftFocusAfterEdit();
                    }
                })
                .bind(instance._events.VALIDATION_ERROR, function () {
                    this.trigger(instance._events.FOCUS);
                })
                .bind(instance._events.FINISHED_EDITING, function () {
                    instance._removeEditRow(this);
                    row.render().delegateEvents();
                    this.unbind();  // avoid any other updating, blurring, finished editing, cancel events being fired
                })
                .bind(instance._events.CANCEL, function () {
                    instance._removeEditRow(this);
                    this.unbind();  // avoid any other updating, blurring, finished editing, cancel events being fired
                    row.render().delegateEvents(); // render and rebind events
                    instance._shiftFocusAfterEdit();
                })
                .bind(instance._events.BLUR, function () {
                    instance.dismissEditRows(); // dismiss edit rows that have no changes
                    if (instance._saveEditRowOnBlur()) {
                        this.trigger(instance._events.SAVE, false);  // save row, which if successful will call the updated event above
                    }
                });

            // Ensure that if focus is pulled to another row, we blur the edit row
            this._applyFocusCoordinator(editRow);

            // focus edit row, which has the flow on effect of blurring current focused row
            editRow.trigger(instance._events.FOCUS, field);

            // disables form fields
            if (instance._createRow) {
                instance._createRow.disable();
            }

            this.editRows.push(editRow);

            return editRow;
        },


        /**
         * Renders all specified rows
         *
         * @param {Array} array of objects describing Backbone.Model's to render
         * @return {AJS.RestfulTable}
         */
        renderRows: function (rows) {

            var model,
                $els = $();

            // Insert prepopulated entries
            for (var i = 0; i < rows.length; i++) {
                model = new this.options.model(rows[i]);
                $els = $els.add(this._renderRow(model, -1).el);
                this._models.add(model)
            }

            this.removeNoEntriesMsg();

            this.$tbody.append($els);

            return this;
        },

        /**
         * Gets default options
         *
         * @param {Object} options
         */
        _getDefaultOptions: function (options) {
            return {
                model: options.model || AJS.RestfulTable.EntryModel,
                views: {
                    editRow: AJS.RestfulTable.EditRow,
                    row: AJS.RestfulTable.Row
                },
                Collection: Backbone.Collection.extend({
                    url: options.resources.self,
                    model: options.model || AJS.RestfulTable.EntryModel
                }),
                reorderable: false,
                loadingMsg: options.loadingMsg || AJS.I18n.getText("aui.words.loading")
            }
        }

    });

    // jQuery data keys (http://api.jquery.com/jQuery.data/)
    AJS.RestfulTable.DataKeys = {
        ENABLED_SUBMIT: "enabledSubmit",
        ROW_VIEW: "RestfulTable_Row_View"
    };

    // CSS style classes. DON'T hard code
    AJS.RestfulTable.ClassNames = {
        NO_VALUE: "aui-restfultable-editable-no-value",
        NO_ENTRIES: "aui-restfultable-no-entires",
        RESTFUL_TABLE: "aui-restfultable",
        ROW: "aui-restfultable-row",
        READ_ONLY: "aui-restfultable-readonly",
        ACTIVE: "aui-restfultable-active",
        ALLOW_HOVER: "aui-restfultable-allowhover",
        FOCUSED: "aui-restfultable-focused",
        MOVEABLE: "aui-restfultable-movable",
        ANIMATING: "aui-restfultable-animate",
        DISABLED: "aui-resfultable-disabled",
        SUBMIT: "aui-restfultable-submit",
        EDIT_ROW: "aui-restfultable-editrow",
        CREATE: "aui-restfultable-create",
        DRAG_HANDLE: "aui-restfultable-draghandle",
        ORDER: "aui-restfultable-order",
        EDITABLE: "aui-restfultable-editable",
        ERROR: "error",
        DELETE: "aui-resfultable-delete",
        LOADING: "loading"
    };

    // Custom events
    AJS.RestfulTable.Events = {

        // AJS events
        REORDER_SUCCESS: "RestfulTable.reorderSuccess",
        ROW_ADDED: "RestfulTable.rowAdded",
        ROW_REMOVED: "RestfulTable.rowRemoved",
        EDIT_ROW: "RestfulTable.switchedToEditMode",
        SERVER_ERROR: "RestfulTable.serverError",

        // backbone events
        CREATED: "created",
        UPDATED: "updated",
        FOCUS: "focus",
        BLUR: "blur",
        SUBMIT: "submit",
        SAVE: "save",
        MODAL: "modal",
        MODELESS: "modeless",
        CANCEL: "cancel",
        CONTENT_REFRESHED: "contentRefreshed",
        RENDER: "render",
        FINISHED_EDITING: "finishedEditing",
        VALIDATION_ERROR: "validationError",
        SUBMIT_STARTED: "submitStarted",
        SUBMIT_FINISHED: "submitFinished",
        ANIMATION_STARTED: "animationStarted",
        ANIMATION_FINISHED: "animationFinisehd",
        INITIALIZED: "initialized",
        ROW_INITIALIZED: "rowInitialized"
    };

})(AJS.$);