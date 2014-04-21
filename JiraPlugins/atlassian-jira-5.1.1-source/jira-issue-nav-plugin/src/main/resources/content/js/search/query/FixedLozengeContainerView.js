AJS.namespace("JIRA.Issues.FixedLozengeContainerView");

JIRA.Issues.FixedLozengeContainerView = JIRA.Issues.BaseView.extend({

    template: JIRA.Templates.IssueNav.lozengeContainer,

    initialize: function(options) {
        _.bindAll(this);
        this.searcherCollection = options.searcherCollection;
    },

    render: function() {
        var instance = this;
        this.$el.html(this.template());
        var $lozengeContainer = this.$el.find(".filter-list");
        this.collection.each(function(fixedLozenge) {
            var fixedLozengeView = new JIRA.Issues.FixedLozengeView({
                searcherCollection: instance.searcherCollection,
                model: fixedLozenge
            });
            $lozengeContainer.append(fixedLozengeView.render().$el);
        });
    }
});
