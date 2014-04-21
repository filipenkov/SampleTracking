AJS.namespace("JIRA.Issues.FilterModel");

/**
 * Represents a saved (normally favourite) filter, also known as a "Saved Search".
 *
 */
JIRA.Issues.FilterModel = JIRA.Issues.BaseModel.extend({

    properties: ["id", "name", "jql", "favourite"],
    urlRoot: contextPath + "/rest/api/2/filter/",

    parse: function(resp, xhr) {
        return JIRA.Issues.FilterModel.transformToModel(resp);
    }
}, {
    /*
     * Class method to transform the REST entity to an instance of this model.
     * The REST endpoint returns more attributes than we're currently interested in.
     */
    transformToModel: function(entity) {
        return {
            id: entity.id,
            name: entity.name,
            jql: entity.jql,
            favourite: entity.favourite
        };
    }
});
