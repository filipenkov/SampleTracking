JIRA.InlineEdit.View = AJS.Control.extend({

    init: function (options) {
        this.bgColor = options.bgColor;
        this.noValueText = options.noValueText;
        this.$container = jQuery(options.element);
        this._assignEvents("container", this.$container);
    },

    reportUpdated: function () {

        var instance = this,
            $container = this.$container;

        $container.addClass("aui-inline-edit-prevent-hover");

        $container.css("backgroundColor", "#E0EFFF");

        window.setTimeout(function () {
            $container.animate({
                backgroundColor: instance.bgColor || "#fff",
                marginRight: 0
            }, 1000, function () {
                jQuery(document).one("mousemove", function () {
                    $container.removeClass("aui-inline-edit-prevent-hover");
                    $container.css("backgroundColor", "");
                });
            });
        }, 300);

    },

    setLabel: function (value) {
        if (value === "") {
            this.$container.text(this.noValueText)
        } else {
            this.$container.text(value);
        }
    },

    setValue: function (value) {
        this.$container.attr("data-value", value);
    },

    getValue: function () {
        return this.$container.attr("data-value");
    },

    getContainer: function () {
        return this.$container;
    },

    _events: {
        container: {
            click: function (e) {
                this.trigger("edit");
                e.preventDefault();
            }
        }
    }
});


JIRA.InlineEdit.DescriptionView = JIRA.InlineEdit.View.extend({

    init: function (options) {
        this._super(options);
        this.$descriptionHeader= jQuery("#project-config-description-header");
        this.$descriptionHeaderContainer = this.$descriptionHeader.parent();
        this.$descriptionSummary = jQuery("#project-config-description-summary");
        this.$descriptionSummaryContainer = this.$descriptionSummary.parent();
    },

    setValue: function () {},

    moveToSummary: function () {
        delete this.bgColor;
        this._unassignEvents("container", this.$container);
        this.$descriptionHeaderContainer.addClass("hidden");
        this.$descriptionSummaryContainer.removeClass("hidden");
        this.$container = this.$descriptionSummary;
        this._assignEvents("container", this.$container);
    },

    moveToHeader: function () {
        this.bgColor = "#F7F7F7";
        this._unassignEvents("container", this.$container);
        this.$descriptionSummaryContainer.addClass("hidden");
        this.$descriptionHeaderContainer.removeClass("hidden");
        this.$container = this.$descriptionHeader;
        this._assignEvents("container", this.$container);
    },

    setLabel: function (value) {
        if (value === "") {
            this.$container.html("");
            this.moveToHeader();
        } else {
            this.moveToSummary();
            this.$container.html(value);
        }
    },

    getValue: function () {
        return this.$container.html();
    },

    _events: {
        container: {
            click: function (e) {
                this.moveToSummary();
                this.trigger("edit");
                e.preventDefault();
            }
        }
    }

});