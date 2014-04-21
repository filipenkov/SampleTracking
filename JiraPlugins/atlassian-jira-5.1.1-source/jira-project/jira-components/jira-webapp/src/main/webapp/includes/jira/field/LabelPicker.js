/**
 * @constructor JIRA.LabelPicker
 * @extends AJS.MultiSelect
 */
JIRA.LabelPicker = AJS.MultiSelect.extend({

    _getDefaultOptions: function () {
        return AJS.$.extend(true, this._super(), {
            ajaxOptions: {
                url: contextPath + "/includes/js/ajs/layer/labeldata.js",
                query: true
            },
            removeOnUnSelect: true,
            userEnteredOptionsMsg: AJS.I18n.getText("label.new")
        });
    },

    isValidItem: function(itemValue) {
        return !/\s/.test(itemValue);
    },

    _handleServerSuggestions: function (data) {
        // if the suggestions brought back from the server include the original token and it doesn't match with the
        // token provided by the user disregard the suggestions
        if(data && data.token) {
            if(AJS.$.trim(this.$field.val()) !== data.token) {
                return;
            }
        }
        this._super(data);
    },

    // need to override because we want to force a request even though our min query length is 1
    _handleDown: function (e) {
        if (!this.suggestionsVisible) {
            this._requestThenResetSuggestions();
            e.stopPropagation();
        }
    },

    _handleSpace: function () {
        if(AJS.$.trim(this.$field.val()) !== "") {
            if(this.handleFreeInput()) {
                this.hideSuggestions();
            }
        }
    },

    keys: {

        //if the user presses space, turn the text entered into labels.
        //if they pressed enter and the dropdown is *not* visible, then also turn text into labels.  Otherwise if the
        //dropdown is visibly enter should just select the item from the dropdown.
        "Spacebar": function (event) {
            this._handleSpace();
            event.preventDefault();
        }
    },

    _formatResponse: function (data) {

        var optgroup = new AJS.GroupDescriptor({
            label: AJS.I18n.getText("common.words.suggestions"),
            type: "optgroup",
            weight: 1,
            styleClass: 'labels-suggested'
        });

        if (data && data.suggestions) {
            AJS.$.each(data.suggestions, function () {
                optgroup.addItem(new AJS.ItemDescriptor({
                    value: this.label,
                    label: this.label,
                    html: this.html
                }));
            });
        }
        return [optgroup];
    },

    handleFreeInput: function() {
        var values = AJS.$.trim(this.$field.val()).match(/\S+/g);

        if (values) {
            // If there are multiple space-separated values, add them separately.
            for (var value, i = 0; value = values[i]; i++) {
                this.addItem({ value: value, label: value });
            }
            this.model.$element.trigger("change");
        }

        this.$field.val("");
    }
});

/** Preserve legacy namespace
    @deprecated AJS.LabelPicker */
AJS.namespace("AJS.LabelPicker", null, JIRA.LabelPicker);
