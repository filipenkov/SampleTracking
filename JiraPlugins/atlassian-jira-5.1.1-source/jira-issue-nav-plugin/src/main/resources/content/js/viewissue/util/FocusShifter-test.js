AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");

module("JIRA.Issues.FocusShifter", {
    setup: function() {
        sessionStorage.removeItem("JIRA.IssuesFocusShifter.lastFieldId");

        /**
         * Creates and returns a mock FieldModel.
         *
         * @param {string} id The ID to use.
         * @param {boolean} isEditable whether the field is editable.
         * @param {boolean} matchesFieldSelector Whether the field is present.
         */
        this.mockFieldModel = function(id, isEditable, matchesFieldSelector) {
            return {
                id: id,
                getLabel: _.lambda("Label for " + id),
                isEditable: _.lambda(isEditable),
                matchesFieldSelector: _.lambda(matchesFieldSelector)
            };
        };

        // By returning an empty array, the focus shifter won't try to modify the DOM.
        var editIssueController = this.editIssueController = {
            getFields: _.lambda([])
        };

        this.viewIssueController = {
            getSelectedEditIssueController: function() {
                return editIssueController;
            }
        };

        this.focusShifter = new JIRA.Issues.FocusShifter({
            viewIssueController: this.viewIssueController,
            messageFactory: _.lambda(undefined)
        });
    }
});

test("Events", function() {
    var testEventsWith = _.bind(function (bindName, unbindName) {
        var receivedEvent = false;
        var receive = function () {
            receivedEvent = true
        };
        this.focusShifter[bindName]("foo", receive);
        this.focusShifter.trigger("foo");
        ok("should have received the event using " + bindName, receivedEvent);
        receivedEvent = false;
        this.focusShifter[unbindName]("foo", receivedEvent);
        this.focusShifter.trigger("foo");
        ok("should not have received the event using " + unbindName, !receivedEvent);
    }, this);

    testEventsWith("on", "off");
    testEventsWith("bind", "unbind");
});

test("Last edited field saving", function() {
    // We need the field's information to pre-fill the input.
    sessionStorage.setItem("JIRA.Issues.FocusShifter.lastFieldId", "a");
    this.editIssueController.getFields = _.lambda({
        models: [this.mockFieldModel("a", true, true)]
    });

    var spy = sinon.spy();
    this.focusShifter.queryableDropdownSelect = {
        $field: {val: spy},
        _handleCharacterInput: function() {}
    };

    this.focusShifter._loadLastField();
    ok(spy.calledWith("Label for a"));
});

test("Last edited field not present", function() {
    sessionStorage.setItem("JIRA.Issues.FocusShifter.lastFieldId", "b");
    this.editIssueController.getFields = _.lambda({
        models: [this.mockFieldModel("a", true, true)]
    });

    var spy = sinon.spy();
    var queryableDropdownSelect = {
        $field: {val: spy}
    };

    // The input shouldn't be pre-filled if the field isn't present.
    this.focusShifter._loadLastField(queryableDropdownSelect);
    ok(!spy.called);
});

test("Last edited field not saved", function() {
    var spy = sinon.spy();
    var queryableDropdownSelect = {
        $field: {val: spy}
    };

    this.focusShifter._loadLastField(queryableDropdownSelect);
    ok(!spy.called);
});

test("Show conditions", function() {
    // The focus shifter shouldn't show if there are no suggestions...
    ok(!JIRA.Issues.FocusShifter._shouldShow([]));
    ok(!JIRA.Issues.FocusShifter._shouldShow([
        this.mockFieldModel("a", true, false),
        this.mockFieldModel("b", false, true),
        this.mockFieldModel("c", false, false)
    ]));

    // ...but it should show if there is at least one.
    ok(JIRA.Issues.FocusShifter._shouldShow([
        this.mockFieldModel("a", true, true),
        this.mockFieldModel("b", true, false)
    ]));
});

test("Suggestion filtering", function() {
    // Fields that aren't editable or present should be filtered out.
    var suggestions = JIRA.Issues.FocusShifter._suggestions([
        this.mockFieldModel("a", true, true),
        this.mockFieldModel("b", true, false),
        this.mockFieldModel("c", false, true),
        this.mockFieldModel("d", false, false),
        this.mockFieldModel("e", true, true)
    ])();

    equals(suggestions[0].value(), "a");
    equals(suggestions[0].label(), "Label for a");
    equals(suggestions[1].value(), "e");
    equals(suggestions[1].label(), "Label for e");
    equals(suggestions.length, 2);
});