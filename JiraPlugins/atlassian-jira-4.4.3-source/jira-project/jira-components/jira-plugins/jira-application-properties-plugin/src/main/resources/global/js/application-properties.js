jQuery.namespace("JIRA.Admin.AppProperty");
/**
 * Edit/Create view of Application Property row
 *
 * @class AppProperty.EditRow
 */
JIRA.Admin.AppProperty.EditRow = JIRA.RestfulTable.EditRow.extend({

    /**
     * Handles all the rendering of the edit application property row. This includes handling validation errors if there is any
     *
     * @param {Object} renderData
     * ... {Object} errors - Errors returned from the server on validation
     * ... {Object} vales - Values of fields
     */
    render: function (renderData) {

        this.$el.attr("data-row-key", this.model.get("key"));

        this.$el.html(JIRA.Templates.AppProperty.editRow(renderData));
        return this;
    },

    submit: function () {
        JIRA.RestfulTable.EditRow.prototype.submit.apply(this, arguments);
    }


});

/**
 * Readonly view of Application Property row
 *
 * @class AppProperty.Row
 */
JIRA.Admin.AppProperty.Row = JIRA.RestfulTable.Row.extend({

    initialize: function () {

        // call super
        JIRA.RestfulTable.Row.prototype.initialize.apply(this, arguments);

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

    /**
     * Resets and renders application property row in table. This should be called whenever the model changes.
     */
    render: function () {

        var renderData = this.model,
            html = JIRA.Templates.AppProperty.view(renderData); // render using closure template

        this.$el.attr("data-row-key", this.model.get("key"));

        this.$el.html(html);
        return this;
    }
});

