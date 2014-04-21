AJS.namespace("JIRA.Issues.QueryStringParser");


// Ruthlessly plundered from parseUri.js. Arrrrr me hearties!
JIRA.Issues.QueryStringParser = {
    parser: /(?:^|&)([^&=]*)=?([^&]*)/g,

    parse: function(queryString) {
        if (!queryString && queryString !== 0) {
            return {};
        }
        queryString = "" + queryString;
        if ("?" === queryString.charAt(0)) {
            queryString = queryString.substring(1);
        }
        var params = {};
        queryString.replace(this.parser, function ($0, $1, $2) {
            params[decodeURIComponent($1)] = decodeURIComponent($2);
        });
        return params;
    }
};
