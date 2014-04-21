AJS.namespace("JIRA.Issues.FixedLozengeModel");

/**
 * A FixedLozenge is a lozenge which is always present in the BasicQueryView
 * with or without a Clause.
 *
 * In the case that it has no clause it has no effect on the search results and
 * displays the "All" value.
 *
 * A Fixed Lozenge is backed by a Searcher where it gets it's i18n'd name.
 */
JIRA.Issues.FixedLozengeModel = JIRA.Issues.BaseModel.extend({
    properties: ["name", "searcher", "clause"]
});
