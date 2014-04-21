/**
 * A multiselect list that can be queried and suggestions selected via a dropdown. Suggestions are retrieved via AJAX.
 *
 * @constructor AJS.MultiSelect
 * @extends AJS.QueryableDropdownSelect
 */
AJS.MultiSelect = AJS.QueryableDropdownSelect.extend({

    /**
     * This constructor:
     * <ul>
     *  <li>Overrides default options with user options</li>
     *  <li>Inserts an input field before dropdown</li>
     *  <li>Adds items currently selected in the &lt;select&gt; as items sitting on the top of the textarea</li>
     * <ul>
     *
     * @param options
     */
    init: function (options) {

        var instance = this;

        if (this._setOptions(options) === this.INVALID) {
            return this.INVALID;
        }

        jQuery.extend(this, AJS.SelectHelper);

        this.options.element = AJS.$(this.options.element);

        AJS.$(this.options.element).hide();

        if (this.options.disabled) {
            this._createFurniture(true);
            return this;
        }

        this.model = new AJS.SelectModel({
            element: this.options.element,
            removeOnUnSelect: this.options.removeOnUnSelect
        });

        this.options.element.bind("updateOptions", function () {
            instance._setOptions(options);
        }).bind("selectOption", function (e, descriptor) {
            instance.addItem(descriptor);
        }).bind("removeOption", function (e, descriptor) {
            instance.removeItem(descriptor);
        }).bind("clearSelection", function (e) {
            instance.clearLozenges();
        });

        // Add the visual representation
        this._createFurniture();

        this.dropdownController = AJS.InlineLayer.create({
            alignment:AJS.LEFT,
            offsetTarget: this.$field,
            maxInlineResultsDisplayed: this.options.maxInlineResultsDisplayed,
            content: AJS.$(".aui-list", this.$container)
        });

        this.dropdownController.onhide(function () {
           instance.hideSuggestions();
        });

        this.listController = new AJS.List({
            containerSelector: AJS.$(".aui-list", this.$container),
            groupSelector: "ul.aui-list-section",
            matchingStrategy: this.options.matchingStrategy,
            maxInlineResultsDisplayed: this.options.maxInlineResultsDisplayed,
            selectionHandler: function (e) {
                instance._selectionHandler(this.getFocused(), e);
                return false;
            }
        });

        this._assignEventsToFurniture();

        this.lozengeGroup = this.options.itemGroup;

        this._assignEvents("lozengeGroup", this.lozengeGroup);

        if (this.options.width) {
            this.setFieldWidth(this.options.width)
        }

        this._restoreSelectedOptions();

        if (this.options.inputText) {
            this.$field.val(this.options.inputText);
            this.updateFreeInputVal();
        }


        this.model.$element.trigger("initialized", [this]);

        return this;
    },

    /**
     * Gets default options
     *
     * @method _getDefaultOptions
     * @protected
     * @return {Object}
     */
    _getDefaultOptions: function () {
        return AJS.$.extend(true, this._super(), {
            minRoomForText : 50,
            errorMessage: AJS.I18n.getText("jira.ajax.autocomplete.error"),
            ajaxOptions: {
                minQueryLength: 1
            },
            showDropdownButton: true,
            itemGroup: new AJS.MultiSelect.LozengeGroup(),
            itemBuilder: function (descriptor) {
                return new AJS.MultiSelect.Lozenge({
                    label: descriptor.label(),
                    title: descriptor.title(),
                    container: this.$selectedItemsContainer
                });
            }
        });
    },

    /**
     * Appends furniture around specified dropdown element. This includes:
     *
     * <ul>
     *  <li>errorMessage - A container for error messages is created but not appended until needed</li>
     *  <li>selectedItemsWrapper - A wrapper for selected items</li>
     *  <li>selectedItemsContainer - A container for selected items</li>
     * </ul>
     *
     * @method _createFurniture
     * @protected
     */
    _createFurniture: function (disabled) {
        var id = this.model.$element.attr("id");

        // remove placeholder if there is one. This placeholder, takes up the space that the multi-select control will
        // while the page is being loaded and the "real" control has not been inserted. i.e Stops the page jumping around.
        if (this.model.$element.prev().hasClass("ajs-multi-select-placeholder")) {
            this.model.$element.prev().remove();
        }

        if (disabled) {
            this.model.$element.replaceWith(this._render("disableSelectField", id));
        } else {
            this.$container = this._render("container", id);
            this.$field = this._render("field", id).appendTo(this.$container);
            this.$container.append(this._render("suggestionsContainer", id));
            this.$container.insertBefore(this.model.$element);
            this.$dropDownIcon = this._render("dropdownAndLoadingIcon", this._hasDropdownButton()).appendTo(this.$container);
            this.$errorMessage = this._render("errorMessage", id);
            this.$selectedItemsWrapper = this._render("selectedItemsWrapper").appendTo(this.$container);
            this.$selectedItemsContainer = this._render("selectedItemsContainer").appendTo(this.$selectedItemsWrapper);
        }
    },

    /**
     * Assigns events furniture
     *
     * @method _assignEventsToFurniture
     */
    _assignEventsToFurniture: function () {
        this._super();
        this._assignEvents("body", document);
        this._assignEvents("selectedItemsContainer", this.$selectedItemsContainer);
    },

    /**
     * Gets value for suggestion/option mirroring user input
     *
     * @method _getUserInputValue
     * @protected
     * @return {String}
     */
    _getUserInputValue: function () {
        return this.options.uppercaseUserEnteredOnSelect ? this.$field.val().toUpperCase(): this.$field.val();
    },

    /**
     * Adds the user's input as an option in the suggestions and the select model
     *
     * @method _handleUserInputOption
     */
    _handleUserInputOption: function () {

        var groupDescriptor;

        if (!this.hasUserInputtedOption() || this.$field.val().length === 0) {
            return;
        }

        groupDescriptor = new AJS.GroupDescriptor({
            type: "optgroup",
            label: "user inputted option",
            weight: 9999,
            showLabel: false,
            replace: true
        });

        groupDescriptor.addItem(new AJS.ItemDescriptor({
            value: this._getUserInputValue(),
            label: this.$field.val(),
            labelSuffix: " (" + this.options.userEnteredOptionsMsg + ")",
            title: this.$field.val(),
            allowDuplicate: false,
            noExactMatch: true          // this item doesn't count as an exact query match for selection purposes
        }));

        this.model.appendOptionsFromJSON([groupDescriptor]);
    },

    /**
     * Returns weather or not an option will be appended to the bottom of the suggestions, mirroring the user input.
     * The will be true if at invocation the userEnteredOptionMsg is set in the options object. The value of this property
     * will be displayed in brackets after the suggestion.
     *
     * @method hasUserInputtedOption
     * @return {Boolean}
     */
    hasUserInputtedOption: function () {
        return this.options.userEnteredOptionsMsg;
    },

    /**
     * Some special handling for user inputted option, and removal of item in select list if we are replacing them from
     * the server.
     *
     * @method _handleSuggestionResponse
     * @override
     * @protected
     * @param {Array} data
     */
    _handleServerSuggestions: function (data) {

        // If we are querying the server then the server will return the full result set to be displayed. We do not
        // want any linguring options in there.
        if (this.options.ajaxOptions.query) {
            this.model.clearUnSelected();
        }

        // we have to do this again as we clear all the unselected options above, which includes the user inputted option
        this._handleUserInputOption();
        this._super(data);
    },

    /**
     * Adds supplied suggestions to dropdown and &lt;select&gt; list
     *
     * @method _setSuggestions
     * @param {Object} data - JSON representing suggestions
     * @param {Object} context - JSON representing suggestion context, including group (if any) and filter flag
     */
    _setSuggestions: function (data, context) {
        if (data) {
            this.model.appendOptionsFromJSON(data);
            data = this.model.getUnSelectedDescriptors();
            this._super(data, context);
        } else {
            this.hideSuggestions();
        }
    },

    /**
     * Clears the control - selection(s) and field text.
     * Note: This does not clear the UI lozenges.
     */
    clear: function () {
        this.$field.val('');
        this._handleCharacterInput(true);
        this.clearSelection();
    },

    /**
     * Clears selection(s) and sets back to editing mode.
     * Note: This does not clear the UI lozenges.
     */
    clearSelection: function () {
        this.model.setAllUnSelected();
        this.updateItemsIndent();
        this.model.$element.trigger("unselect", [this]);
    },

    /**
     * Completely clears the field: the selection(s), the field text and the UI lozenges.
     */
    clearLozenges: function() {
        this.lozengeGroup.removeAllItems();
        this.clear();
    },

    /**
     * Unselects item in model and removes item from selectedItemsContainer
     *
     * @method removeItem
     * @param {Object} descriptor
     */
    removeItem: function (descriptor) {

        var instance = this;
        this.model.setUnSelected(descriptor);
        window.setTimeout(function () {
            instance.updateItemsIndent();
        }, 0);
        this.model.$element.trigger("unselect", [descriptor, this])
    },
    /**
     },
     * Adds items currently selected in the &lt;select&gt; as items sitting on the top of the textarea
     *
     * @method _restoreSelectedOptions
     */
    _restoreSelectedOptions: function () {
        var instance = this;

        // creates selected "button" style representation
        AJS.$.each(this.model.getDisplayableSelectedDescriptors(), function () {
            instance.addItem(this, true);
        });
        this.updateItemsIndent();
    },

    /**
     * Is true if:
     * <ol>
     *      <li>The text selection is at the start or there is no value in field</li>
     *      <li>Lozenge group has not already been enabled.</li>
     *      <li>There are items in the lozenge group</li>
     * </ol>
     *
     * @method _shouldEnableLozengeGroup
     * @return {Boolean}
     */
    _shouldEnableLozengeGroup: function () {
        return this.lozengeGroup.items.length > 0 && this.lozengeGroup.index < 0 && (this.$field.val().length === 0 || this.getCaret(this.$field[0]) === 0);
    },

    /**
     * Handling of backspace in textarea. If there is no characters will select the last item in the selectedItemsContainer.
     */
    _handleBackSpace: function () {
        var instance = this;
        if (this._shouldEnableLozengeGroup()) {
            setTimeout(function() {
                instance.lozengeGroup.shiftFocus(-1);
            }, 0);
        } else {
            this.$field.one("keyup", function() {
                instance._handleCharacterInput();
            });
        }
    },

     /**
     * Handling of delete in textarea.
     */
    _handleDelete: function () {
        if  (AJS.$.trim(this.$field.val()) !== "") {
            // delay until character is actually removed from textarea
            var instance = this;
            this.$field.one("keyup", function () {
                instance._handleCharacterInput();
            });
        }
    },

    /**
     * Handling of left key in textarea. If there is no characters will select the last item in the selectedItemsContainer.
     */
    _handleLeft: function () {
        if (this._shouldEnableLozengeGroup()) {
            var instance = this;
            setTimeout(function() {
                instance.lozengeGroup.shiftFocus(-1);
            }, 0);
        }
    },

    /**
     * Handle text paste event.
     */
    _handlePaste: function() {
        // Remove whitespace such as newlines, tabs, etc.
        this.$field.val(AJS.$.trim(this.$field.val()).replace(/\s+/g, " "));
        this._handleCharacterInput();
    },


    /**
     * Updates the padding left and padding top based on the area occupied by the selected items
     *
     * @method updateItemsIndent
     */
    updateItemsIndent: function () {

        var inputIndent = this._getInputIndent();

        // First set $field's new height, which may trigger vertical scrollbars when
        // in a dialog ...
        this.$field.css({
            paddingTop: inputIndent.top,
            paddingLeft: inputIndent.left
        });
        // ... *then* set $field's width based on the width of $container, which will
        // have shrunken after the scrollbars appeared.
        this.$field.css({
            height: inputIndent.top + 20
        });

        if (this.currentTopOffset && this.currentTopOffset !== inputIndent.top) {
            this.$container.trigger("multiSelectHeightUpdated", [this]);
        }

        // otherwise ie does not update indent
        if (AJS.$.browser.msie) {
            this.$field.val(this.$field.val() + " ");
            this.$field.val(this.$field.val().replace(/\s$/, ""));
        }

        this.currentTopOffset = inputIndent.top;
    },

    /**
     * Checks if the item has already been selected/added
     *
     * @method _isItemPresent
     * @param descriptor - JSON describing suggestion/option
     * @return {Boolean}
     */
    _isItemPresent: function (descriptor) {
        var duplicate = false;
        var value = descriptor.value();
        AJS.$.each(this.lozengeGroup.items, function () {
            if (this.value === value) {
                duplicate = true;
                return false; // bail
            }
        });
        return duplicate;
    },

    /**
     * Adds selected suggestion to the selected items, and marks it as selected in the model.
     *
     * @method _addItem
     * @param {Object} descriptor - JSON describing suggestion/option
     */
    addItem: function(descriptor, initialize) {

        // this descriptor is for the lozenge so we don't want to use the same descriptor but a copy instead. We don't
        // want our descriptor properties to change through a reference we were unaware of.
        if (descriptor instanceof AJS.ItemDescriptor) {
            descriptor = AJS.copyObject(descriptor.allProperties(), false);
        }

        descriptor.value = AJS.$.trim(descriptor.value);
        descriptor.label = AJS.$.trim(descriptor[this.options.itemAttrDisplayed]) || descriptor.value;
        descriptor.title = AJS.$.trim(descriptor.title) || descriptor.label;

        descriptor = new AJS.ItemDescriptor(descriptor);
        
        if (this._isItemPresent(descriptor)) {
            return;
        }

        var lozenge = this.options.itemBuilder.call(this, descriptor);

        this.lozengeGroup.addItem(lozenge);
        this._assignEvents("lozenge", lozenge);

        this.model.setSelected(descriptor);
        this.updateItemsIndent();

        this.dropdownController.setPosition(); // update position incase another line has been added

        lozenge.value = descriptor.value(); // we use this to prevent duplicates being added

        if (!initialize) {
            this.model.$element.trigger("selected", [descriptor, this]);
        }
    },

    /**
     * Adds multiple items
     *
     * @method _addMultipleItems
     * @param {Array} items - Array of item descriptors. e.g {value: "The val", label: "Label to be displayed in suggestions"}
     * @param {Boolean} removeOnUnSelect - If set to true, if the item is removed from the control (unselected), the option
     * will also be deleted from the select model also. This means it will not appear in the suggestions dropdown.
     */
    _addMultipleItems: function(items, removeOnUnSelect) {

        var instance = this;

        AJS.$.each(items, function (i, descriptor) {
            if (removeOnUnSelect) {
                descriptor.removeOnUnSelect = true;
            }
            instance.addItem(descriptor);
        });
    },

    /**
     * Calculates the width taken by selected items
     *
     * @method _getTargetItemContainerWidth
     * @return {Number} width
     */
    _getTargetItemContainerWidth: function () {

        var lozenges = this.lozengeGroup.items,
            width = parseInt(this.$selectedItemsContainer.css("paddingLeft"), 10) +
                    parseInt(this.$selectedItemsContainer.css("paddingRight"), 10);

        for (var i = lozenges.length-1; i >= 0; i--) {
            var $item = lozenges[i].$lozenge;
            width = width
                  + $item.outerWidth()
                  + parseInt($item.css("marginLeft"), 10)
                  + parseInt($item.css("marginRight"), 10);
        }
        return width;
    },

    /**
     * Determines correct top and left indent for textarea, based on the area taken by selected items
     *
     * @method _getInputIndent
     * @return {Object}
     */
    _getInputIndent: function () {

        // TODO incorporate min/max width options

        var top,
            left,
            iconArea = 21, // Icon width of 16px and 4px on the right and then 2px margin on the left of the items container
            paddingLeft = 2,
            paddingTop = 3,
            indent = { top: paddingTop, left: paddingLeft },
            lastLozengeIndex = this.lozengeGroup.items.length - 1,
            $last;

        if (lastLozengeIndex >= 0) {
            $last = this.lozengeGroup.items[lastLozengeIndex].$lozenge;
            top = $last.prop("offsetTop");
            left = $last.prop("offsetLeft") + $last.outerWidth();
            if (left > this.$container.width() - iconArea - this.options.minRoomForText) {
                top += $last.prop("offsetHeight");
                left = 0;
            }

            indent.top += top;
            indent.left += left;
        }

        return indent;
    },

    /**
     * @method _selectionHandler - Handle when a suggestion is accepted
     * @param selected
     * @param e
     */
    _selectionHandler: function (selected, e) {

        var instance = this;
        
        selected.each(function () {
            instance.addItem(AJS.$.data(this, "descriptor"));
        });

        this.$field.val("").focus().scrollIntoView({ margin: 20 });
        this.hideSuggestions();
        this.hideErrorMessage();
        this.updateFreeInputVal();
        this.model.$element.trigger("change");
        
        e.preventDefault();
    },

    /**
     * @method isValidItem - Determines whether the given value represents a valid item
     * @param {String} itemValue
     * @return {Boolean}
     */
    isValidItem: function(itemValue) {
        var suggestedItemDescriptor = this.listController.getFocused().data("descriptor");
        if (!suggestedItemDescriptor) {
            return false;
        }
        itemValue = itemValue.toLowerCase();
        return itemValue === AJS.$.trim(suggestedItemDescriptor.label.toLowerCase()) ||
               itemValue === AJS.$.trim(suggestedItemDescriptor.value.toLowerCase());
    },

    /**
     * @method handleFreeInput - Handle the case where text remains "unlozenged" in the text field
     */
    handleFreeInput: function() {
        var value = AJS.$.trim(this.$field.val()),
            descriptor;

        if (value) {
            descriptor = this.model.getDescriptor(value);
            if (descriptor) {
                this.addItem(descriptor);
                this.model.$element.trigger("change");
                this.$field.val("");
                this.hideErrorMessage();
            } else if (!this.options.submitInputVal) {
                this.showErrorMessage(value);
            }
        }
    },

    /**
     * Submits form
     * @method submitForm
     */
    submitForm: function () {
        if (this.$field.val().length === 0 && !this.suggestionsVisible) {
            AJS.$(this.$field[0].form).submit(); // submit on enter if field is empty
        }
    },

    _handleCharacterInput: function (ignoreBuffer, ignoreQueryLength) {
        this._super(ignoreBuffer, ignoreQueryLength);
        this.updateFreeInputVal();
    },

    _deactivate: function () {
        this.handleFreeInput();
        this.lozengeGroup.trigger("blur");
        this.hideSuggestions();
    },

    keys: {
        "Left": function () {
            this._handleLeft();
        },
        "Backspace": function () {
            this._handleBackSpace();
        },
        "Del": function () {
            this._handleDelete();
        },
        "Return": function (e) {
            this.submitForm();
            e.preventDefault();
        },
        "Tab" : function (e) {
            this.acceptFocusedSuggestion();
        }
    },

    _events: {
        body: {
            // handling for the case where control is in a tab, and as a result hidden.
            tabSelect: function () {
                if (this.$field.is(":visible")) {
                    this.updateItemsIndent();
                }
            },
            // Bulk operations don't yet use AUI forms.
            bulkTabSelect: function() {
                if (this.$field.is(":visible")) {
                    this.updateItemsIndent();
                }
            }
        },
        field: {
            paste: function() {
                // This must be delayed because the pasted text has not been added yet.
                setTimeout(AJS.$.proxy(this, "_handlePaste"), 0);
            },
            "aui:keydown aui:keypress": function(event) {
                if (this.lozengeGroup.index >= 0) {
                    if (event.key in this.lozengeGroup.keys) {
                        event.preventDefault();
                    } else if (event.key === "Return") {
                        this.submitForm();
                        event.preventDefault();
                    } else {
                        this.onEdit(event);
                        this.lozengeGroup.trigger("blur");
                    }
                }
            },
            click: function() {
                this.lozengeGroup.trigger("blur");
                this.$field.focus();
            }
        },
        lozengeGroup: {
            focus: function() {
                this.$field.focus();
                this.hideSuggestions();
                this._unassignEvents("keys", this.$field);
            },
            blur: function() {
                this._assignEvents("keys", this.$field);
                if (this.$field.val()) {
                    this._handleCharacterInput();
                }
            }
        },
        lozenge: {
            remove: function(event) {
                this.removeItem(this.model.getDescriptor(event.target.value));
                this.$field.focus();
            }
        },
        selectedItemsContainer: {
            click: function(event) {
                // Ignore clicks not directly on this.$selectedItemsContainer.
                if (event.target === event.currentTarget) {
                    this.lozengeGroup.trigger("blur");
                    this.$field.focus();
                }
            }
        }
    },

    _renders: {
        errorMessage: function (idPrefix) {
            return AJS.$('<div class="error" />').attr('id', idPrefix + "-error");
        },
        selectedItemsWrapper: function () {
            return AJS.$('<div class="representation"></div>');
        },
        selectedItemsContainer: function () {
            return AJS.$('<ul class="items" />');
        },
        field: function (idPrefix) {
             //  the wrap="off" attribute prevents text from growing under the labels. It doesn't prevent linebreaks
            return AJS.$('<textarea autocomplete="off" id="' + idPrefix + '-textarea" class="aui-field" wrap="off"></textarea>');
        },
        disableSelectField: function (id) {
            return AJS.$("<input type='text' class='long-field' name='" + id + "' id='" + id + "' />");
        },
        container : function (idPrefix) {
            return AJS.$('<div class="multi-select" id="' + idPrefix +'-multi-select">');
        },
        suggestionsContainer : function (idPrefix) {
            return AJS.$('<div class="aui-list" id="' + idPrefix + '-suggestions" tabindex="-1"></div>');
        }
    }
});
