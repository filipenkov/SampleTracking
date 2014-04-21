AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module('JIRA.Issues.CardView', {
    setup: function() {
        this.$el = jQuery("<div></div>");
        // View that simply renders its options.html
        var BaseView = JIRA.Issues.BaseView.extend({
            initialize: function(options) {
                this.html = options.html;
            },
            render: function() {
                this.$el.html(this.html);
            }
        });
        this.createBaseView = function(html) {
            return new BaseView({
                html: html
            });
        }
    },
    teardown: function() {
        this.$el.remove();
    }
});

test("initializes to first view if active view not specified", function() {
    var cardView = new JIRA.Issues.CardView({
        el: this.$el,
        views: {
            one: this.createBaseView("this is view one"),
            two: this.createBaseView("this is view two")
        }
    });

    cardView.render();

    equals(this.$el.html(), "this is view one");
});

test("initializes to activeview if active view specified", function() {
    var cardView = new JIRA.Issues.CardView({
        el: this.$el,
        views: {
            one: this.createBaseView("this is view one"),
            two: this.createBaseView("this is view two")
        },
        activeView: "two"
    });

    cardView.render();

    equals(this.$el.html(), "this is view two");
});

test("changeToView after render", function() {
    var cardView = new JIRA.Issues.CardView({
        el: this.$el,
        views: {
            one: this.createBaseView("this is view one"),
            two: this.createBaseView("this is view two")
        }
    });

    cardView.render();
    cardView.changeToView("two");
    equals(this.$el.html(), "this is view two");
});

test("changeToView before render", function() {
    var cardView = new JIRA.Issues.CardView({
        el: this.$el,
        views: {
            one: this.createBaseView("this is view one"),
            two: this.createBaseView("this is view two")
        }
    });

    cardView.changeToView("two");
    cardView.render();
    equals(this.$el.html(), "this is view two");
});
