AJS.namespace("JIRA.Issues.SwitcherModel");

JIRA.Issues.SwitcherModel = JIRA.Issues.BaseModel.extend({

    /**
     * id: id of the switcher
     * name: switcher name (displayed in switcher view)
     * view: backbone view object
     */
    properties: ["id", "name", "view", "text"]

});
