AJS.namespace("JIRA.Issues.IssueFieldCollection");

JIRA.Issues.IssueFieldCollection = JIRA.Issues.BaseCollection.extend({
    model: JIRA.Issues.IssueFieldModel,


    initialize: function () {
        _.bindAll(this);
        this.bind("editingStarted", this._handleEditingStarted)
    },

    _handleEditingStarted: function (editModel, props) {
        props = props || {};
        if (props.ignoreBlur) {
            return;
        }
        // Let any field in edit mode know we want to edit another
        this.each(function(model) {
            if (editModel !== model && model.getEditing()) {
                model.blurEdit();
            }
        });
    },

    update:function(data) {
        var instance = this;
        _.each(data, function (modelData) {
            var existingModel = instance.get(modelData.id);
            if (existingModel) {
                // JRADEV-11518 Don't update the model's editHtml if it's an error,
                // since it will override the 'real' editHtml
                if (existingModel.hasValidationError()) {
                    delete modelData.editHtml;
                }
                existingModel.set(modelData, {silent: true});
                instance.trigger("updated", existingModel);
            } else {
                instance.add(modelData);
            }
        });
    },

    cancelEdit: function () {
        this.each(function (model) {
            model.cancelEdit();
        });
    },

    isDirty: function() {
        return this.any(function(item) { return item.isDirty() });
    },
    getDirtyFields: function() {
        return this.filter(function(item) { return item.isDirty()});
    }
});