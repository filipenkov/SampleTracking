jQuery.namespace("JIRA.Admin.AppProperty");
/**
 * Edit/Create view of Application Property row
 *
 * @class AppProperty.EditRow
 */
JIRA.Admin.AppProperty.EditRow = AJS.RestfulTable.EditRow.extend({

    initialize: function () {

        // call super
        AJS.RestfulTable.EditRow.prototype.initialize.apply(this, arguments);

        this.bind(AJS.RestfulTable.Events.RENDER, function () {
            this.$el.attr("data-row-key", this.model.get("key"));
        });
    },

    renderKey: function (self, all) {
        return JIRA.Templates.AppProperty.key(all);
    },

    submit: function () {
        AJS.RestfulTable.EditRow.prototype.submit.apply(this, arguments);
    }
});

/**
 * Readonly view of Application Property row
 *
 * @class AppProperty.Row
 */
JIRA.Admin.AppProperty.Row = AJS.RestfulTable.Row.extend({

    initialize: function () {

        // call super
        AJS.RestfulTable.Row.prototype.initialize.apply(this, arguments);

        this.bind(AJS.RestfulTable.Events.RENDER, function () {
            this.$el.attr("data-row-key", this.model.get("key"));
        });

        // crap work around to handle backbone not extending events
        // (https://github.com/documentcloud/backbone/issues/244)
        this.events["click .application-property-revert"] = "_revert";
        this.delegateEvents();
    },


    _revert: function (e) {
        this.trigger("focus");

        var defaultValue = this.$el.find(".application-property-value-default").val();
        this.sync({value: defaultValue});
    },

    renderOperations: function (update, all) {
        return JIRA.Templates.AppProperty.operations(all);
    },

    renderKey: function (self, all) {
        return JIRA.Templates.AppProperty.key(all);
    }

});

