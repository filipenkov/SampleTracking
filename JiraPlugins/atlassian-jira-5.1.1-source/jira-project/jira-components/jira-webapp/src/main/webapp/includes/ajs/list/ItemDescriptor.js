/**
 * The item descriptor is used in @see AJS.QueryableDropdownSelect to define characteristics and
 * display of items added to suggestions dropdown and in the case of AJS.QueryableDropdownSelect
 * and AJS.SelectModel also.
 *
 * @constructor AJS.ItemDescriptor
 * @extends AJS.Descriptor
 */
AJS.ItemDescriptor = AJS.Descriptor.extend({

    /**
     * Defines properties required during invocation to form a valid descriptor.
     *
     * @property {Object} REQUIRED_PROPERTIES
     */
    REQUIRED_PROPERTIES: {
        label: true
    },

    /**
     * Defines default properties
     *
     * @method _getDefaultOptions
     * @return {Object};
     */
    _getDefaultOptions: function () {
        return {
            showLabel: true
        };
    },

    /**
     * Gets styleClass, in the case of @see AJS.QueryableDropdownSelect these are the classNames that will be applied to the
     * &lt;a&gt; surrounding suggestion.
     *
     * @method styleClass
     * @return {String}
     */
    styleClass: function () {
        return this.properties.styleClass;  
    },

    /**
     * Gets value, in the case of a @see AJS.QueryableDropdownSelect this will be the value set on the &lt;option&gt;
     *
     * @method value
     * @return {String}
     */
    value: function () {
        return this.properties.value;
    },

    /**
     * Gets label suffix, in the case of @see AJS.QueryableDropdownSelect where we mirror the users input as a suggestion we
     * use this to append some help text such as (Add Label).
     *
     * @method labelSuffix
     * @return {String}
     */
    labelSuffix: function (value) {
        if (typeof value !== "undefined") {
            this.properties.labelSuffix = value;
        }        
        return this.properties.labelSuffix;
    },

    /**
     * Gets title, in the case of @see AJS.MultiSelect we use this as the title tag applied to @see AJS.MultiSelect.Lozenge element
     *
     * @method title
     * @return {String}
     */
    title: function () {
        return this.properties.title;
    },

    /**
     * Gets label, in the case of @see AJS.QueryableDropdownSelect this is the label displayed in the suggestion items, unless
     * the html property of this descriptor has been set.
     *
     * @method label
     * @return {String}
     */
    label: function () {
        return this.properties.label;
    },

    /**
     * Asks whether or not to allow duplicates of this descriptor. This is used in @see AJS.QueryableDropdownSelect where there
     * is a suggetion appended that mirrors user input. In this case if we have another suggestion in the list that is
     * the same as this one, we do not want to show this one.
     *
     * @method allowDuplicate
     */
    allowDuplicate: function () {
        return this.properties.allowDuplicate;
    },


    /**
     * @method removeOnUnSelect
     */
    removeOnUnSelect: function () {
        return this.properties.removeOnUnSelect;
    },

    /**
     * Gets icon url
     *
     * @method icon
     * @return {String} 
     */
    icon: function () {
        return this.properties.icon;
    },

    /**
     * Gets or sets selected state.
     *
     * @method selected
     * @param {Boolean} value
     * @return {Boolean} 
     */
    selected: function (value) {
        if (typeof value !== "undefined") {
            this.properties.selected = value;
        }
        return this.properties.selected;
    },

    /**
     * Gets or sets model. The model in the case of @see AJS.QueryableDropdownSelect is the jQuery wrapped <option> element
     *
     * @param $model
     */
    model: function ($model) {
        if ($model) {
            this.properties.model = $model;
        } else {
            return this.properties.model;
        }
    },

    /**
     * Gets the keywords attribute
     *
     * @method keywords
     * @return {String}
     */
    keywords: function () {
        return this.properties.keywords;
    },

    /**
     * Gets the href attribute
     *
     * @method href
     * @return {String}
     */
    href: function () {
        return this.properties.href;
    },

    /**
     * Gets html, in the case of @see AJS.QueryableDropdownSelect this html will be shown as the suggestion item instead of [label]
     *
     * @method html
     * @return {String}
     */
    html: function () {
        return this.properties.html;
    },

    /**
     * @return {String} the text displayed in field when this item is selected
     */
    fieldText: function () {
        return this.properties.fieldText;
    },

    /**
     * @return true if this Item should not be selected in the dropdown, even if it's an exact text match for a query.
     */
    noExactMatch: function () {
        return this.properties.noExactMatch;
    }

});