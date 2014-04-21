AJS.namespace("JIRA.Issues.FixedLozengeCollection");

/**
 * Collection of fixed lozenges.
 */
JIRA.Issues.FixedLozengeCollection = JIRA.Issues.BaseCollection.extend({

    model: JIRA.Issues.FixedLozengeModel,

    setFixedLozenges: function(fixedLozenges) {
        var instance = this;
        _.each(fixedLozenges, function(fixedLozenge) {
            instance.add(fixedLozenge);
        });
    }
});