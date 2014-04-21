AJS.namespace("JIRA.Issues.SwitcherView");

/**
 * View that allows switching between querying modes
 */
JIRA.Issues.SwitcherView = JIRA.Issues.BaseView.extend({

    tagName: "div",

    events: {
        "click .switcher": "_onSwitcherClick"
    },

    initialize: function(options) {
        _.bindAll(this);
        this.containerClass = options.containerClass;
        this.model = options.model;
        this.template = options.template;
        this.model.bindSelectionChanged(this._onSelect);
        this.switchEl = AJS.$();
        this.model.on("change:disabled", this._setSwitching);
    },

    /**
     * Renders the table element's contents.
     */
    render: function() {
        this.switchEl = this.$el.find(".switcher");
        this.switchEl.html(this.template({"items": this.model.getCollection().toJSON()}));
        this._onSelect();
    },

    _onSelect: function() {
        this.switchEl.find(".switcher-item").removeClass("active");
        var container = this.$el.find(this.containerClass);
        container.empty();

        var selected = this.model.getSelected();
        if (selected) {
            this.switchEl.find(".switcher-item[data-id=" + selected.id + "]").addClass("active");
            selected.getView().setElement(container).render();
        }
    },

    _onSwitcherClick: function(event) {
        event.preventDefault();
        if (!this.model.getDisabled()) {
            this.model.next();
        }
    },

    _setSwitching: function() {
        if (this.model.getDisabled()) {
            this.disableSwitching()
        } else {
            this.enableSwitching();
        }
    },

    disableSwitching: function(jql) {
        this.switchEl.addClass("disabled");
        this.$el.find(".switcher-item.active").attr("title", AJS.I18n.getText("jira.jql.query.too.complex"))
    },

    enableSwitching: function() {
        this.switchEl.removeClass("disabled")
        this.$el.find(".switcher-item").removeAttr("title")
    }
});
