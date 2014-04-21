AJS.namespace("JIRA.Issues.IssueNavView");


JIRA.Issues.IssueNavView = JIRA.Issues.BaseView.extend({
    threshold: 100,

    mixins: [JIRA.Issues.Mixin.PageTitleView],

    /**
     * @constructor
     */
    initialize: function() {
        _.bindAll(this);
        this.model.bindIssueSelected(this.checkMode);
        this.model.bind("change:realDisplayMode", this.checkMode);
        this.model.bindSwitchedToDetailMode(this.checkMode);
        this.model.bindBackToSearch(this.checkMode);
        this.model.bindBackToSearch(this._restoreTitle);
        this.end = AJS.$(document).scrollHeight;
        AJS.$(document).bind("scroll", this.onWindowScroll);
   },

    checkMode: function() {
        var issueKey = this.model.get("selectedIssue");
        if (issueKey) {
            if (this.model.getRealDisplayMode() === this.model.DETAILED_MODE) {
                this.viewIssueMode();
            }
            else {
                this.searchMode();
            }
        }
    },

    /**
     * Switch to view issue mode
     */
    viewIssueMode: function() {
        this.$el.addClass("navigator-collapsed");
        this.$el.find(".result-panel").empty();
        AJS.$(document).unbind("scroll", this.onWindowScroll);
        this.$el.find(".navigator-content")
                .unbind("scroll", this.onDomScroll) // Prevent double bind
                .bind("scroll", this.onDomScroll);


    },
    /**
     * Switch to search view
     */
    searchMode: function() {
        this.$el.removeClass("navigator-collapsed");
        this.$el.find(".navigator-group");
        this.$el.find(".result-panel").empty();
        AJS.$(document).unbind("scroll", this.onDomScroll) // Prevent double bind
                .bind("scroll", this.onDomScroll);

    },

    /**
     * Handle scrolling a DOM element to append more results
     *
     * @param {Event} e
     * @private
     */
    onDomScroll: function (e) {
        var $target = AJS.$(e.target),
            scrollY = $target.scrollTop(),
            height = $target.height(),
            bottom = $target.attr("scrollHeight"),
            d = bottom - scrollY - height;

        if (d < this.threshold) {
            this.model.appendMoreResults();
        }
    },

    /**
     * Handle scrolling a window element to append more results
     *
     * @param {Event} e
     * @private
     */
    onWindowScroll: function(e) {
        var scrollY = window.scrollY,
            innerHeight = window.innerHeight,
            bottom = this.$el.position().top + this.$el.height(),
            d = bottom - scrollY - innerHeight;
        if (d < this.threshold) {
            this.model.appendMoreResults();
        }
    },

    _restoreTitle: function() {
        this.restorePreviousPageTitle();
    }
});
