AJS.namespace("JIRA.Issues.IssuePanelsModel");

JIRA.Issues.IssuePanelsModel = JIRA.Issues.BaseModel.extend({

    properties: [
    /**
     * Panels on the left side of view issue
     * @type Array
     */
        "leftPanels",
    /**
     * Panels on the right side of the view issue page
     */
        "rightPanels",
    /**
     * Panels in the middle of the view issue page
     */
        "infoPanels"
    ],

    namedEvents: [
    /**
     * Triggers if a response came back from the server which had a particular panel we had on the
     * client removed.
     */
        "panelRemoved",
    /**
     * Triggers when a response comes back from the server with panels that are unknown on the client.
     */
        "panelAdded"
    ],


    initialize: function() {
        this.set({
            leftPanels:[],
            rightPanels:[],
            infoPanels:[]
        }, {silent:true});
    },

    /*
     * Used when no description module was sent back by the server to provide a space to add a description.
     */
    DESC_MODULE: {
        "completeKey":"com.atlassian.jira.jira-view-issue-plugin:descriptionmodule",
        "headerLinks":{links:[],groups:[]},
        "html":"<div id='description-val'><em>" + AJS.I18n.getText("issue.nav.click.add.description") + "</em></div>",
        id:"descriptionmodule",
        label:"Description",
        prefix:"",
        renderHeader:true,
        styleClass:"",
        subpanelHtmls:[]
    },

    /**
     * Updates all the panel models with new data. Creates new ones that don't exist yet.
     *
     * @param {Object} data
     * @param {Object} props
     * ... {Array<String>} fieldsSaved - The update may come as the result of a save. This array includes the ids of any
     * fields that may have been saved before hand.
     * ... {Array<String>} fieldsInProgress - Array of fields that are still in edit mode or still saving.
     * ... {Boolean} initialize - parameter indicating if it is the first time the update has been called.
     */
    update:function(data, props) {
        var instance = this;
        if(props.editable) {
            //if we're editable insert a description module that can be edited if it doesn't already exist
            var containsDescriptionPanel = _.any(data.leftPanels, function(panel) {return panel.id === "descriptionmodule"});
            if(!containsDescriptionPanel) {
                var leftPanels = [];
                _.each(data.leftPanels, function(panel) {
                    leftPanels[leftPanels.length] = panel;
                    if(panel.id === "details-module") {
                        leftPanels[leftPanels.length] = instance.DESC_MODULE;
                    }
                });
                data.leftPanels = leftPanels;
            }
        }

        this.updatePanels("leftPanels", data.leftPanels, props);
        this.updatePanels("rightPanels", data.rightPanels, props);
        this.updatePanels("infoPanels", data.infoPanels, props);
    },

    addDescriptionPanel: function () {
        var leftPanels = [],
            instance = this;
        if (!_.any(this.getLeftPanels(), function(panel) {return panel.id === "descriptionmodule"})) {
            _.each(this.getLeftPanels(), function (panel) {
                leftPanels[leftPanels.length] = panel;
                if(panel.id === "details-module") {
                    var entity = instance.DESC_MODULE;
                    var descriptionPanel = new JIRA.Issues.IssuePanelModel({id: entity.id, entity:entity});
                    leftPanels[leftPanels.length] = descriptionPanel;
                    instance.triggerPanelAdded({location:"leftPanels", panel:descriptionPanel, index:leftPanels.length-1});
                }
            });

            this.setLeftPanels(leftPanels, {silent: true});
        }
    },

    /**
     * Updates all the panel models with new data. Creates new ones that don't exist yet.
     *
     * @param {String} location - property to update/add to
     * @param {Array} panelEntities
     * @param {Object} props
     * ... {Array<String>} fieldsSaved - The update may come as the result of a save. This array includes the ids of any
     * fields that may have been saved before hand.
     * ... {Array<String>} fieldsInProgress - Array of fields that are still in edit mode or still saving.
     * ... {Boolean} initialize - parameter indicating if it is the first time the update has been called.
     */
    updatePanels: function (location, panelEntities, props) {
        var instance = this;
        var currentPanels = this.get(location);
        var newPanels = [];

        //first trigger remove events for any currentPanels that don't exist any longer!
        _.each(currentPanels, function(currentPanel) {
            var panelId = currentPanel.getEntity().id;
            if(!_.any(panelEntities, function(panel) {return panel.id === panelId})) {
                instance.triggerPanelRemoved({location:location, panel:currentPanel});
            }
        });

        _.each(panelEntities, function (entity, index) {
            var panel = _.find(currentPanels, function (model) {
                return model.id === entity.id;
            });
            if (panel) {
                panel.update(entity, props);
            } else {
                panel = new JIRA.Issues.IssuePanelModel({id: entity.id, entity:entity});
                instance.triggerPanelAdded({location:location, panel:panel, index:index});
            }
            newPanels.push(panel);
        });
        var values = {};
        values[location] = newPanels;
        this.set(values, {silent:true});
    },

    /**
     * Creates a placeholder model. Used when we have found a pre-exisiting panel in the dom, happens when we render serverside.
     *
     * @param {String} pos
     * @param {String} id
     */
    createPlaceholderModel: function (pos, id) {
        var model,
                panels = this.get(pos);
        if (!_.any(panels, function(panel) {return panel.id === id})) {
            model = new JIRA.Issues.IssuePanelModel({
                id: id,
                entity: {id: id}
            });
            panels.push(model);
            return model;
        }
    }
});