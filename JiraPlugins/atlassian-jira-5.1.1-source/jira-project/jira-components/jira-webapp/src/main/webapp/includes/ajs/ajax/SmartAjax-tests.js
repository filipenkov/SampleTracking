AJS.test.require("jira.webresources:dialogs");
AJS.test.require("com.atlassian.jira.dev.func-test-plugin:sinon");

module("SmartAjax WebSudo tests", {
    teardown: function() {
        this.sandbox.restore();
    },
    setup: function() {
        this.sandbox = sinon.sandbox.create();
    }
});

test("makeWebSudoRequest with error callback", function() {
    var makeRequest = this.sandbox.stub(JIRA.SmartAjax, "makeRequest");
    var error = this.sandbox.spy();

    //make the request.
    JIRA.SmartAjax.makeWebSudoRequest({
        error: error,
        copy: "me"
    });

    ok(makeRequest.calledOnce, "makeRequest called.");

    //First time through we get an error.
    var newArgs = makeRequest.getCall(0).args;
    equal(newArgs.length, 1, "makeRequest called with 1 argument.");
    var newOptions = newArgs[0];
    equal(newOptions.copy, "me", "Make sure non-error options copied.");

    var xhr = {status: 38, responseText: "ignored"};
    var statusText = "sjdjakdjakda";
    var errorThrown = "4897589475893754983";
    var result = "rehsjfhdskjfhsdkhgtiu4y4";

    newOptions.error(xhr, statusText, errorThrown, result);

    ok(error.calledOnce, "Called Original Error");
    ok(error.getCall(0).calledWithExactly(xhr, statusText, errorThrown, result), "Called error with correct arguments.");

    xhr = {status: 401, responseText: "ignored"};

    newOptions.error(xhr, statusText, errorThrown, result);

    ok(error.calledTwice, "Called Original Error Again");
    ok(error.getCall(1).calledWithExactly(xhr, statusText, errorThrown, result), "Called error again with correct arguments.");
})

test("makeWebSudoRequest with error callback 2", function() {
    var makeRequest = this.sandbox.stub(JIRA.SmartAjax, "makeRequest");

    //make the request.
    JIRA.SmartAjax.makeWebSudoRequest({
        error: "",
        copy: "me"
    });

    ok(makeRequest.calledOnce, "makeRequest called.");

    //First time through we get an error.
    var newArgs = makeRequest.getCall(0).args;
    equal(newArgs.length, 1, "makeRequest called with 1 argument.");
    var newOptions = newArgs[0];
    equal(newOptions.copy, "me", "Make sure non-error options copied.");

    var xhr = {status: 38, responseText: "ignored"};
    var statusText = "sjdjakdjakda";
    var errorThrown = "4897589475893754983";
    var result = "rehsjfhdskjfhsdkhgtiu4y4";

    ok(jQuery.isFunction(newOptions.error));

    newOptions.error(xhr, statusText, errorThrown, result);
});

test("makeWebSudoRequest with websudo", function() {
    var makeRequest = this.sandbox.stub(JIRA.SmartAjax, "makeRequest");
    var formDialog = this.sandbox.stub(JIRA, "FormDialog").returns({
        show: function() {}
    });

    //make the request.
    var origOptions = {
        error: "",
        copy: "me"
    };
    JIRA.SmartAjax.makeWebSudoRequest(origOptions);

    ok(makeRequest.calledOnce, "makeRequest called.");

    var xhr = {status: 401, responseText: "{WebSudo}"};
    var statusText = "sjdjakdjakda";
    var errorThrown = "4897589475893754983";
    var result = "rehsjfhdskjfhsdkhgtiu4y4";

    //Force the error handler to run.
    var newOptions = makeRequest.getCall(0).args[0];
    newOptions.error(xhr, statusText, errorThrown, result);

    //Make sure we display dialog on this particular error.
    ok(formDialog.calledOnce);

    var submitHandler = formDialog.getCall(0).args[0].submitHandler;

    //Submit the dialog.
    submitHandler({
        target: "<form action=''/>",
        preventDefault: function() {}
    });

    //Make sure the dialog makes a request to authenticate the user for websudo.
    ok(makeRequest.calledTwice);
    newOptions = makeRequest.getCall(1).args[0];
    ok(jQuery.isFunction(newOptions.complete));

    //Always call back to makeWebSudoRequest with the websudo autentication result
    //so we can either open the dialog again or submit the original request.
    var self = this.sandbox.stub(JIRA.SmartAjax, "makeWebSudoRequest");
    newOptions.complete({
        getResponseHeader: function(str) {
            if (str == "X-Atlassian-WebSudo") {
                return "Has-Authentication";
            }
        }
    });

    ok(self.calledOnce);
    strictEqual(self.getCall(0).args[0], origOptions);
});