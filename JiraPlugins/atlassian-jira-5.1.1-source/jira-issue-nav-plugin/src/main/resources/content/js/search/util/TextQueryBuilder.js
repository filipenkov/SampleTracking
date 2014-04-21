AJS.namespace("JIRA.Issues.TextQueryBuilder");

JIRA.Issues.TextQueryBuilder = {

    MULTIPLE_WHITESPACE_PATTERN: /\s+/g,

    buildJql: function(textQuery) {
        if (!textQuery) {
            return "";
        }
        textQuery = "" + textQuery;
        textQuery = AJS.$.trim(textQuery);
        textQuery = textQuery.replace(this.MULTIPLE_WHITESPACE_PATTERN, " "); // remove multiple whitespaces
        return _.map(textQuery.split(" "), this.createQueryClause).join(" AND ");
    },

    createQueryClause: function(searchTerm) {
        return "text ~ \"" + searchTerm + "\"";
    }
};
