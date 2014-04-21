AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module('JIRA.Issues.SwitcherView', {
    setup: function() {
        this.$el = jQuery('<form><div class="aui-group search-container"></div><div class="notifications"></div><div class="switcher"></div></form>');
        this.switchers = new JIRA.Issues.SwitcherCollection();
        var searchPageModel = JIRA.Issues.TestUtils.createSearchPageModel();
        searchPageModel.switchToSearchMode("x");
        this.switcherViewModel = new JIRA.Issues.QuerySwitcherViewModel({
            collection: this.switchers
        }, {
            searchPageModel: searchPageModel
        });
        this.switcherView = new JIRA.Issues.SwitcherView({
            el: this.$el,
            template: JIRA.Templates.IssueNav.searchSwitcher,
            model: this.switcherViewModel,
            containerClass: ".search-container"
        });
        this.twoSwitchersData = [{
            id: "x",
            name: "xxx",
            text: "xxx",
            view: {
                setElement: function(el) { this.$el = jQuery(el); return this; },
                render: function() { this.$el.append("<div>first</div>"); }
            }
        }, {
            id: "y",
            name: "yyy",
            text: "yyy",
            view: {
                setElement: function(el) { this.$el = jQuery(el); return this; },
                render: function() { this.$el.append("<div>second</div>"); }
            }
        }];
    },
    teardown: function() {
        this.$el.remove();
    }
});

test("Search switcher template exists", function() {
    ok(JIRA.Templates.IssueNav.searchSwitcher, "Template does not exist");
});

test("View renders a switcher element", function() {
    this.switcherView.render();

    equals(this.switcherView.switchEl.length, 1, "View rendered a switcher element");
});

test("View renders first member only", function() {
    this.switchers.reset(this.twoSwitchersData);
    this.switcherView.render();

    var switcher = this.switcherView.switchEl;
    equals(switcher.find(".switcher-item.active").length, 1, "Only 1 active element");
    ok(jQuery.trim(switcher.find(".switcher-item.active").text()).indexOf("xxx") > -1, "Shown element is not first in collection");
});

test("View switch renders second member only", function() {
    this.switchers.reset(this.twoSwitchersData);
    this.switcherView.render();
    this.switcherViewModel.selectById("y");

    var switcher = this.switcherView.switchEl;
    equals(switcher.find(".switcher-item.active").length, 1, "Only 1 active element");
    ok(jQuery.trim(switcher.find(".switcher-item.active").text()).indexOf("yyy") > -1, "Shown element is not first in collection");
});

test("Clicking on switcher element switches view", function() {
    this.switchers.reset(this.twoSwitchersData);
    this.switcherView.render();

    this.switcherView.switchEl.trigger('click');

    var switcher =this.switcherView.switchEl;
    equals(switcher.find(".switcher-item.active").length, 1, "Only 1 active element");
    ok(jQuery.trim(switcher.find(".switcher-item.active").text()).indexOf("yyy") > -1, "Shown element is not first in collection");
});

test("Switcher renders elements container", function() {
    this.switchers.reset(this.twoSwitchersData);
    this.switcherView.render();

    equals(this.$el.find(".search-container").children().length, 1, "Should only be one element in container");
    equals(this.$el.find(".search-container").children().text(),'first',"Shown element has incorrect children");
});

test("Switcher renders elements container after click", function() {
    this.switchers.reset(this.twoSwitchersData);
    this.switcherView.render();

    this.switcherView.switchEl.trigger('click');

    equals(this.$el.find(".search-container").children().length, 1, "Should only be one element in container") ;
    equals(this.$el.find(".search-container").children().text(), "second" ,"Shown element has incorrect children");
});

test("Selecting view before render renders correct item", function() {
    this.switchers.reset(this.twoSwitchersData);
    this.switcherViewModel.selectById("y");
    this.switcherView.render();

    var switcher = this.switcherView.switchEl;
    equals(switcher.find(".switcher-item.active").length, 1, "Only 1 active element");
    ok(jQuery.trim(switcher.find(".switcher-item.active").text()).indexOf("yyy") > -1, "Renders selected item");
    equals(this.$el.find(".search-container").children().length, 1, "Should only be one element in container") ;
    equals(this.$el.find(".search-container").children().text(), "second" ,"Shows selected view");
});

test("Cannot switch when switching is disabled", function() {
    this.switchers.reset(this.twoSwitchersData);
    this.switcherView.render();

    this.switcherViewModel.disableSwitching();

    var switcher = this.switcherView.switchEl;
    ok(switcher.hasClass("disabled"), "Switcher element has class disabled");

    switcher.trigger('click');
    ok(jQuery.trim(switcher.find(".switcher-item.active").text()).indexOf("xxx") > -1, "Initial element is still selected");
});
