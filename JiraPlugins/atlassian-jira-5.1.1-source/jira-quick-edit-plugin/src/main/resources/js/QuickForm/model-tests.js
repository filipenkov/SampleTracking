AJS.test.require("com.atlassian.jira.jira-quick-edit-plugin:quick-form");
AJS.test.require("com.atlassian.jira.jira-quick-edit-plugin:test-util");

test("Construction does not go straight to server (lazy)", function () {

    assertInvocationNotToUseAjax("Expected construction of model not to use ajax, should be lazy", function () {
        new JIRA.Forms.Model({
            userFieldsResource: "http://www.example.com",
            fieldsResource: "http://www.example.com"
        });
    });
});

test("refresh", function () {

    var model = new JIRA.Forms.Model({
            userFieldsResource: "http://www.example.com",
            fieldsResource: "http://www.example.com"
        });

    var fields = [{}, {}],
        sortedTabs = [{}, {}],
        userPrefs = {showWelcomeScreen: true};

    expectSuccessfulAjax({
        fields: fields,
        sortedTabs: sortedTabs,
        userPreferences: userPrefs
    });

    model.refresh();

    equals(fields, model.fields);
    equals(sortedTabs, model.sortedTabs);
    equals(userPrefs, model.userPreferences);

    var updatedFields = [{}, {}, {}, {}],
        updatedSortedTabs = [{}, {}, {}, {}],
        updatedUserPrefs = {showWelcomeScreen: false};

    expectSuccessfulAjax({
        fields: updatedFields,
        sortedTabs: updatedSortedTabs,
        userPreferences: updatedUserPrefs
    });

    model.refresh();

    equals(updatedFields, model.fields);
    equals(updatedSortedTabs, model.sortedTabs);
    equals(updatedUserPrefs, model.userPreferences);
});

test("retainValue", function () {


    var model = new JIRA.Forms.Model({
            userFieldsResource: "http://www.example.com",
            fieldsResource: "http://www.example.com"
        });

    // ratain methods throw errors if feature not enabled

    ok(!model.hasRetainFeature(), "Expected not to have retain feature by default");

    raises(function () {
        model.addFieldToRetainValue()
    });

    raises(function () {
        model.removeFieldToRetainValue();
    });

    raises(function () {
        model.getFieldsWithRetainedValues();
    });

    raises(function () {
        model.hasRetainedValue();
    });

    model._hasRetainFeature = true;

    ok(model.hasRetainFeature(), "Expected to have retain feature");

    model.addFieldToRetainValue("summary");
    model.addFieldToRetainValue("summary");

    equals(model.getFieldsWithRetainedValues().join(","), "summary",
        "Expected addFieldToRetainValue NOT to add duplicates");

    ok(model.hasRetainedValue({id: "summary"}), "Expected [summary] value to be retained");
    ok(!model.hasRetainedValue({id: "blah"}), "Expected [blah] value NOT to be retained");
    
    model.removeFieldToRetainValue("summary");

    ok(!model.hasRetainedValue({id: "summary"}), "Expected [summary] value NOT to be retained, it should have been" +
        "removed");
});

test("mungeTabs", function () {

    var model = new JIRA.Forms.Model({
            userFieldsResource: "http://www.example.com",
            fieldsResource: "http://www.example.com"
        });

   var tabs = model._mungeTabs([
        {
            id: "summary",
            tab: {label: "tab 2", position: 2}
        },
        {
            id: "components",
            tab: {label: "tab 1", position: 1}
        },
        {
            id: "version",
            tab: {label: "tab 0", position: 0}
        }
    ]);

    deepEqual([
        {"label":"tab 0","position":0,"isFirst":true,"fields":[
            {"id":"version","tab":{"label":"tab 0","position":0}}
        ]},
        {"label":"tab 1","position":1,"fields":[
            {"id":"components","tab":{"label":"tab 1","position":1}}
        ]},
        {"label":"tab 2","position":2,"fields":[
            {"id":"summary","tab":{"label":"tab 2","position":2}}
        ]}
    ], tabs);

});


test("updating user preferences", function () {

    expect(3);

    var model = new JIRA.Forms.Model({
            userFieldsResource: "http://www.example.com",
            fieldsResource: "http://www.example.com"
        });

    var userFields = ["summary", "components"],
        showWelcomeScreen = true,
        useQuickForm = false;

    model.userPreferences = {};
    model.userPreferences.fields = userFields;
    model.userPreferences.showWelcomeScreen = showWelcomeScreen;
    model.userPreferences.useQuickForm = useQuickForm;

    expectSuccessfulAjax();

    model.updateUserPrefs({
        useQuickForm: true
    });

    model.getUseConfigurableForm().done(function (use) {
        ok(use, "Expected user pref useQuickForm to be updated");
    });

    model.getUserFields().done(function (currentUserFields) {
        deepEqual(userFields, currentUserFields, "Expected user pref fields NOT to be changed");
    });

    expectSuccessfulAjax();

    model.updateUserPrefs({
        fields: ["versions", "customfield"]
    });

    model.getUserFields().done(function (currentUserFields) {
        deepEqual(["versions", "customfield"], currentUserFields, "Expected user pref fields NOT to be changed");
    });

});