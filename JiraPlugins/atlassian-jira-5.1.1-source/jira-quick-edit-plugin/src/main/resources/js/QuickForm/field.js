/**
 * A View class for field inputs that can be added and removed to forms. Used in cojunction with
 * JIRA.Forms.ConfigurableForm
 *
 * @class ConfigurableField
 */
JIRA.Forms.ConfigurableField = AJS.Control.extend({

    /**
     * @constructor
     * @param descriptor
     * ... {String} id
     * ... {Boolean} required
     * ... {String} label
     * ... {String} editHtml
     */
    init: function (descriptor) {
        this.descriptor = descriptor;
        this.$element = jQuery("<div />").attr("id", "qf-field-" + this.getId());
    },

    /**
     * Focuses input field
     */
    focus: function () {
        this.$element.find(":input:first").focus();
    },

    highlight: function () {

        var instance = this;

        this.$element.css("backgroundColor", '#fff').animate({
            backgroundColor: "lightyellow"
        }, function () {
            instance.$element.animate({
                backgroundColor: "#fff"
            }, function () {
                instance.$element.css("backgroundColor", "");
            });
        });
    },

    /**
     * Gets field label
     * @return {String}
     */
    getLabel: function () {
        return this.descriptor.label;
    },

    /**
     * Gets field id
     * @return {String}
     */
    getId: function () {
        return this.descriptor.id;
    },

    /**
     * Activates field by showing it
     * @param {Boolean} silent - Fire event or not
     */
    activate: function (silent) {

        var result;

        this.active = true;
        this.$element.addClass("qf-field-active").show();
        this.$element.find("textarea").trigger("refreshInputHeight"); // So textarea expand to correct height. See expandOnInput.

        if (!silent) {
            result = this.render(); // reset value
            this.$element.append(result.scripts);
            this.focus();
            JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [this.$element]);
            this.$element.scrollIntoView();
            this.triggerEvent("activated");
        }
    },

    hasVisibilityFeature: function () {
        return !!this.descriptor.hasVisibilityFeature;
    },

    /**
     * 
     *
     * @return {Boolean}
     */
    hasRetainFeature: function () {
        return !!this.descriptor.hasRetainFeature;
    },

    /**
     * Is the field shown
     *
     * @return {Boolean}
     */
    isActive: function () {
        return this.active;
    },

    /**
     * Disables field by hiding it
     * @param {Boolean} silent - Fire event or not
     */
    disable: function (silent) {

        this.render(); // reset value

        this.active = false;
        this.$element.removeClass("qf-field-active").hide();

        if (!silent) {
            this.triggerEvent("disabled");
        }
    },

    /**
     * Renders field
     * @return {jQuery}
     */
    render: function () {
        var html = JIRA.Templates.QuickForm.field(this.descriptor),
            result = JIRA.extractScripts(html); // JRADEV-9069  Pull out custom field js to be executed post render

        this.$element.html(result.html)
            .addClass("qf-field")
            .data("model", this);
        return {
            element: this.$element,
            scripts: result.scripts
        };
    }
    
});