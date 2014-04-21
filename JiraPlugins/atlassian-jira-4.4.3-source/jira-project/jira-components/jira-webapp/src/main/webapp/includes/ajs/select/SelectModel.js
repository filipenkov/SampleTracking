/**
 * Gives a JSON interface to a &lt;select list&gt;. Allowing you to add elements via JSON descriptors. It also
 * provides utility methods to retrieve collections of elements as JSON, for example selected options.
 *
 * @constructor AJS.SelectModel
 * @extends AJS.Control
 */
AJS.SelectModel = AJS.Control.extend({

    /**
     * Sets some defaults and parses all options, and option group into JSON. This json representation is bound
     * to it's corresponding DOM element, and can be accessed using jQuery([OPTION OR OPTGROUP HERE]).data("descriptor")
     *
     * @constructor
     * @param {jQuery | Selector | HTMLElement} options - <select> tag for the in dom represenation of model
     */
    init: function (options) {

        if (options.element) {
            options.element = AJS.$(options.element);
        } else {
            options.element = AJS.$(options);
        }

        this._setOptions(options);

        this.$element = this.options.element;

        this.type = this.$element.attr("multiple") ? "multiple" : "single";

        if (this.type === "single") {
            this.$element.attr("multiple", "multiple");
        }

        // Get a json representation of our <select>
        this._parseDescriptors();
    },

    /**
     * Gets default options
     *
     * @method _getDefaultOptions
     * @private
     * @return {Object}
     */
    _getDefaultOptions: function () {
        return {};
    },

    /**
     * Used to set selected state on option. In the case of a single select this will remove the selected property
     * from all other options.
     *
     * @method setSelected
     * @param {Object} descriptor - JSON object describing option. E.g. {label: "one", value: 1}
     */
    setSelected: function (descriptor) {
        var selectedItem = false;

        if (this.type === "single") {
            this.setAllUnSelected();
        }

        this.$element.find("option")
            .filter(function () {
                return AJS.$(this).attr("value") === descriptor.value();
            })
            .each(function () {
                selectedItem = true;
                AJS.$(this)
                    .attr("selected", "selected")
                    .data("descriptor").selected(true);
            });
        
        //if the option doesn't exist, create it! This may be useful for free text dropdowns like the labels
        if(!selectedItem) {
            descriptor.selected(true);
            var newOption = this._render("option", descriptor);
            newOption.attr("selected", "selected");
            this.$element.append(newOption);
        }
    },


    /**
     * Sets all options to unselected
     *
     * @method setAllUnSelected
     * @private
     */
    setAllUnSelected: function () {
        var instance = this;

        AJS.$(this.getSelectedDescriptors()).each(function () {
            instance.setUnSelected(this);
        });
    },


    /**
     * Used to set unselected an option. Note, this will unselect and option with the same value also.
     * 
     * @method setUnSelected
     * @param {Object} descriptor - JSON object decribing option
     */
    setUnSelected: function (descriptor) {

        var instance = this;

        this.$element.find("option")
            .filter(function () {
                return AJS.$(this).attr("value") === descriptor.value();
            })
            .each(function () {
                var $this = AJS.$(this);
                if (instance.options.removeOnUnSelect || $this.data("descriptor").removeOnUnSelect()) {
                    $this.remove();
                } else {
                    $this.attr("selected", "");
                    $this.data("descriptor").selected(false);
                }
            });
    },


    /**
     * Determines if ANY option with an identical value to our descriptor is present in the provided context.
     * 
     * @method _isOptionPresent
     * @private
     * @param {Object} descriptor - JSON object decribing option
     * @param {jQuery | HTMLElement} ctx - Context to search for option. If not provided will search entire select
     * @return {Boolean}
     */
    _isOptionPresent: function (descriptor, ctx) {
        var notFound = true;
        var value = descriptor.value();
        AJS.$("option", ctx || this.$element).each(function() {
            return notFound = (this.value !== value); // break loop if we find a match
        });
        return !notFound;
    },


    /**
     * Determines if ANY optiongroup with an identical label to our descriptor is present in the provided context.
     *
     * @method _isOptionGroupPresent
     * @private
     * @param  {Object} descriptor - JSON object decribing optgroup
     * @return {Boolean}
     */
    _isOptionGroupPresent: function (descriptor) {
        var $optgroup = this.$element.find("optgroup")
            .filter(function () {
                return AJS.$(this).attr("label") === descriptor.label();
            });
        return $optgroup.length > 0;
    },


    /**
     * Removes corresponding option from select
     *
     * @method remove
     * @param {Object} descriptor - JSON object decribing option
     */
    remove: function (descriptor) {
        if(descriptor && descriptor.model()) {
            descriptor.model().remove();
        }
    },

    /**
     * Loops over all unselected descriptors searching for one that either matches the value or label of argument
     *
     * @method getDescriptor
     * @param {String} value - value or label to test against
     * @return {Object} descriptor
     */
    getDescriptor: function (value) {

        var returnDescriptor;

        value = AJS.$.trim(value.toLowerCase());

        AJS.$.each(this.getAllDescriptors(false), function (e, descriptor) {
            if (value === AJS.$.trim(descriptor.label().toLowerCase()) ||
               value === AJS.$.trim(descriptor.value().toLowerCase())) {
                returnDescriptor = descriptor;
                return false; //bail out of loop, we are done
            }
        });

        return returnDescriptor;
    },

    /**
     * Appends options to select  using an array of JSON descriptors. ONLY options that have not already been defined.
     *
     * <pre>
     * // structure
     * [
     *  {
     *      label: "History Search",
     *      type: "optgroup",
     *      weight: 0, // order in list (starts from 0)
     *      options: [
     *          {
     *              icon: "/jira/images/icons/issue_subtask.gif",
     *              label: "HSP-2 - test,
     *              type: "option"
     *          },
     *          {
     *              icon: "/jira/images/icons/issue_subtask.gif",
     *              label: "HSP-1 - I am a iest Issue,
     *              type: "option"
     *          }
     *      ]
     *  },
     *  {
     *      label: "Current Search",
     *      type: "optgroup"
     *      weight: 1, // order in list (starts from 0)
     *      options: [
     *          {
     *              icon: "/jira/images/icons/issue_subtask.gif",
     *              label: "HSP-2 - test,
     *              type: "option"
     *          },
     *          {
     *              icon: "/jira/images/icons/issue_subtask.gif",
     *              label: "HSP-1 - I am a iest Issue,
     *              type: "option"
     *          }
     *      ]
     *   }
     * ]
     * </pre>
     *
     * @method appendOptionsFromJSON
     * @param {Array} optionDescriptors - JSON tree describing options and optgroups to be appended
     */
    appendOptionsFromJSON: function (optionDescriptors) {

        var instance = this;

        AJS.$.each(optionDescriptors, function (i, descriptor) {

            var optgroup;


            if (descriptor instanceof AJS.GroupDescriptor && (descriptor.replace() || !instance._isOptionGroupPresent(descriptor))) {

                if (descriptor.replace()) {

                    if (descriptor.id()) {
                        optgroup = instance.$element.find("#" + descriptor.id());
                    } else {
                        optgroup = instance.$element.find("optgroup[label='" + descriptor.label() + "']");
                    }

                    if (optgroup.length) {
                        optgroup.find("option:not(:selected)").remove();
                    }
                }

                if (!optgroup || !optgroup.length) {
                    optgroup = instance._render("optgroup", descriptor);
                }

                // make sure that we always update the descriptor
                optgroup.data("descriptor", descriptor);

                AJS.$.each(descriptor.items(), function (i, optDescriptor) {
                    if (!instance._isOptionPresent(optDescriptor, optgroup)) {
                        optgroup.append(instance._render("option", optDescriptor));
                    }
                });

                if (typeof descriptor.weight() !== "undefined") {
                    var target = instance.$element.children().eq(descriptor.weight());
                    if (target[0] !== optgroup[0]) {
                        if (target.length) {
                            optgroup.insertBefore(target);
                        } else {
                            optgroup.appendTo(instance.$element);
                        }
                    }
                } else {
                    optgroup.appendTo(instance.$element);
                }
            } else if (descriptor instanceof AJS.GroupDescriptor) {

                if (descriptor.id()) {
                    optgroup = instance.$element.find("#" + descriptor.id());
                } else {
                    optgroup = instance.$element.find("optgroup[label='" + descriptor.label() + "']");
                }

                optgroup.data("descriptor", descriptor);

                AJS.$.each(descriptor.items(), function (i, optDescriptor) {

                    var value = optDescriptor.value();

                    if (!instance._isOptionPresent(optDescriptor, optgroup)) {
                        optgroup.append(instance._render("option", optDescriptor));
                    } else {
                        optgroup.find('option[value="' + value.replace(/"/g, '\\"') + '"]').data("descriptor", optDescriptor);
                    }
                });
            } else if (descriptor instanceof AJS.ItemDescriptor && !instance._isOptionPresent(descriptor)) {
                instance._render("option", descriptor).appendTo(instance.$element);
            }
        });
    },

    /**
     * Supplied with an option element, a JSON object describing it is bound to it using jQuery's data method
     *
     * @method _parseOption
     * @private
     * @param {jQuery | HTMLElement} optionElem - element to which a json descriptor will be bound
     * @return {Object} descriptor - JSON object decribing option
     */
    _parseOption: function (optionElem) {

        var descriptor;

        optionElem = AJS.$(optionElem);

        if (this.options.removeNullOptions && this._hasNullValue(optionElem)) {
            optionElem.remove(); // thank you
            return null;
        }

        descriptor = new AJS.ItemDescriptor({
            styleClass: optionElem.attr("className"),
            value: optionElem.val(),
            title: optionElem.attr("title"),
            label: AJS.$.trim(optionElem.text()),
            icon: optionElem.css("backgroundImage").replace(/url\(['"]?(.*?)['"]?\)/,"$1"), // we just store the url
            selected: optionElem.attr("selected") ? true : false,
            model: optionElem
        });

        optionElem.data("descriptor", descriptor);

        return descriptor;
    },

    /**
     * Checks if given option element has 'null' value. As 'null' we understand any value lower than 0 that indicates
     * this option will be used to reset field value to null. 
     *
     * @method _hasNullValue
     * @private
     * @param {jQuery | HTMLElement} optionElement - element to check
     * @return {boolean} true, if value of the element is lowert than 0, false otherwise
     */
    _hasNullValue: function(optionElement) {
        return optionElement.val() < 0;
    },

    /**
     * Builds then binds a JSON representation to optgroups and options
     *
     * @method _parseDescriptors
     * @private
     */
    _parseDescriptors: function () {

        var instance = this,
            items = this.$element.children();

        function parseOptGroup (optionGroup) {
            optionGroup.data("descriptor", new AJS.GroupDescriptor({
                label: optionGroup.attr("label"),
                styleClass: optionGroup.attr("className"),
                model: optionGroup,
                items: retrieveAvailableOptions(optionGroup)
            }));
        }

        function retrieveAvailableOptions (parent) {

            var availableOptionElems = AJS.$("option", parent),
                arr = [];

            AJS.$.each(availableOptionElems, function () {
                arr.push(instance._parseOption(this));
            });

            return arr;
        }

        items.each(function (i) {
            var item = items.eq(i);
            if (item.is("optgroup")) {
                parseOptGroup(item);
            } else if (item.is("option")) {
                instance._parseOption(item);
            }
        });
    },

    /**
     * Gets an array of JSON descriptors for selected options
     *
     * @method getSelectedDescriptors
     * @return {Array} 
     */
    getSelectedDescriptors: function () {
        var descriptors = [];
        this.$element.find("option").each(function () {
            if (this.selected) {
                descriptors.push(AJS.$.data(this, "descriptor"));
            }
        });
        return descriptors;
    },

    /**
     * Gets an array of all descriptors, selected and unselected.
     *
     * @method getAllDescriptors
     * @param {boolean} showGroups - If set to false will not include group descriptors 
     * @return {Array}
     */
    getAllDescriptors: function (showGroups) {

        var properties,
            descriptors = [];

        this.$element.children().each(function () {
            var descriptor,
                elem = AJS.$(this);
            if (elem.is("option")) {
                descriptors.push(elem.data("descriptor"));
            } else if (elem.is("optgroup")) {
                if (showGroups !== false) {

                    properties = AJS.copyObject(elem.data("descriptor").allProperties(), false);
                    properties.items = [];
                    descriptor = new AJS.GroupDescriptor(properties);
                    descriptors.push(descriptor);
                }
                elem.children("option").each(function () {
                    var elem = AJS.$(this);
                    if (showGroups !== false) {
                        descriptor.addItem(elem.data("descriptor"));
                    } else {
                        descriptors.push(elem.data("descriptor"));
                    }
                });

            }
        });
        
        return descriptors;
    },

    /**
     * Removes all unselected options
     *
     * @method clearUnSelected
     */
    clearUnSelected: function () {
        this.$element.find("option:not(:selected)").remove();
    },

    /**
     * Gets an array of JSON descriptors for unselected options
     *
     * @method getUnSelectedDescriptors
     * @param {boolean} showGroups - If set to false will not include group descriptors 
     * @return {Array}
     */
    getUnSelectedDescriptors: function (showGroups) {

        var descriptors = [],
            selectedValues = {},
            addedValues = {};

        // this function looks for options already selected by comparing values. NOTE: not case sensitive
        function isValid(descriptor) {
            var descriptorVal = descriptor.value().toLowerCase();
            if (!selectedValues[descriptorVal] && (!addedValues[descriptorVal] ||
                                                                    descriptor.allowDuplicate() !== false)) {
                addedValues[descriptorVal] = true;
                return true;
            }
            return false;
        }

        AJS.$.each(this.getSelectedDescriptors(), function (i, descriptor) {
            selectedValues[descriptor.value().toLowerCase()] = true;
        });

        this.$element.children().each(function () {
            var descriptor,
                properties,
                elem = AJS.$(this);
            if (elem.is("option") && !this.selected) {
                descriptor = AJS.$.data(this, "descriptor");
                if (isValid(descriptor)) {
                    descriptors.push(descriptor);
                }
            } else if (elem.is("optgroup")) {
                if (showGroups !== false) {
                    properties = AJS.copyObject(elem.data("descriptor").allProperties(), false);
                    properties.items = [];
                    descriptor = new AJS.GroupDescriptor(properties);
                    descriptors.push(descriptor);
                }
                elem.find("option").each(function () {
                    if (this.selected) {
                        return;
                    }
                    var itemDescriptor = AJS.$.data(this, "descriptor");
                    if (isValid(itemDescriptor)) {
                        if (showGroups !== false) {
                            descriptor.addItem(itemDescriptor);
                        } else {
                            descriptors.push(itemDescriptor);
                        }
                    }
                });
            }
        });

        return descriptors;
    },

    _renders: {

        /**
         * Renders an option, built using descriptor.
         *
         * You call this method like this:
         *
         * <pre>
         * this.render("option", {...});
         * </pre>
         *
         * @method _renders.option
         * @private
         * @param {AJS.ItemDescriptor} descriptor
         * @return {jQuery}
         */
        option: function (descriptor) {

            var option  = new Option(descriptor.label(), descriptor.value());
            var $option = AJS.$(option);
            var iconUrl = descriptor.icon();

            option.title = descriptor.title();

            AJS.$.data(option, "descriptor", descriptor);

            descriptor.model($option);

            if (iconUrl) {
                $option.css("backgroundImage", "url(" + iconUrl + ")");
            }

            return $option;
        },

        /**
         * Renders an optgroup, built using descriptor.
         *
         * You call this method like this:
         *
         * <pre>
         * this.render("option", {...});
         * </pre>
         *
         * @method _renders.option
         * @param {AJS.GroupDescriptor} descriptor
         * @return {jQuery}
         */
        optgroup: function (descriptor) {

            var elem = AJS.$("<optgroup />")
                    .addClass(descriptor.styleClass())
                    .attr("label", descriptor.label());

            descriptor.model(elem);

            elem.data("descriptor", descriptor);

            if (descriptor.id()) {
                elem.attr("id", descriptor.id());
            }

            return elem;
        }
    }

});
